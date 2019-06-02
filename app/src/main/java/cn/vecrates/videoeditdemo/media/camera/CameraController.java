package cn.vecrates.videoeditdemo.media.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import cn.vecrates.videoeditdemo.util.ToastUtil;

/**
 * @author Vecrates.
 * @describe
 */
public class CameraController implements Handler.Callback {

	private final static String TAG = CameraController.class.getSimpleName();

	private static class H {
		private static CameraController instance = new CameraController();
	}

	private CameraManager cameraManager;
	private String frontCameraId;
	private String backCameraId;
	private CameraCharacteristics characteristics;
	private CameraDevice cameraDevice;
	private CameraCaptureSession captureSession;

	private SurfaceTexture surfaceTexture;
	private Surface surface;

	private HandlerThread thread;

	private Handler handler;

	private CameraController() {
	}

	public static CameraController getInstance() {
		return H.instance;
	}

	private void initThread() {
		thread = new HandlerThread("cameraThread");
		thread.start();
		handler = new Handler(thread.getLooper(), this);
	}

	@Override
	public boolean handleMessage(Message message) {
		return true;
	}

	public void setupCamera(Context context) {
		initThread();
		cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
		try {
			int facing;
			for (String cameraId : cameraManager.getCameraIdList()) {
				characteristics = cameraManager.getCameraCharacteristics(cameraId);
				facing = characteristics.get(CameraCharacteristics.LENS_FACING);
				if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
					frontCameraId = cameraId;
				} else if (facing == CameraCharacteristics.LENS_FACING_BACK) {
					backCameraId = cameraId;
				}
			}
		} catch (CameraAccessException e) {
			e.printStackTrace();
			logE("Execute setupCamera exception!");
		}
	}

	private Size getCameraPreviewSize(String carmeraId, int width, int height) {
		try {
			CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(carmeraId);
			StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
			return getOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Size getOptimalSize(Size[] sizeMap, int width, int height) {
		List<Size> sizeList = new ArrayList<>();
		for (Size option : sizeMap) {
			if (width > height) {
				if (option.getWidth() > width && option.getHeight() > height) {
					sizeList.add(option);
				}
			} else {
				if (option.getWidth() > height && option.getHeight() > width) {
					sizeList.add(option);
				}
			}
		}
		if (sizeList.size() > 0) {
			return Collections.min(sizeList, new Comparator<Size>() {
				@Override
				public int compare(Size lhs, Size rhs) {
					return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight());
				}
			});
		}
		return sizeMap[0];
	}


	public void openFrontCamera(Surface surface) {
		if (frontCameraId.equals("")) {
			logE("The device is not front camera");
			return;
		}
		this.surface = surface;
		openCamera(frontCameraId);
	}

	public void openBackCamera(SurfaceTexture surfaceTexture, int width, int height) {
		if (backCameraId.equals("")) {
			logE("The device is not back camera");
			return;
		}
		Size previewSize = getCameraPreviewSize(backCameraId, width, height);
		if (previewSize == null) {
			ToastUtil.show("Camera can't surpport the size");
			return;
		}
		surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
		this.surface = new Surface(surfaceTexture);
		this.surfaceTexture = surfaceTexture;
		openCamera(backCameraId);
	}

	@SuppressLint("MissingPermission")
	private void openCamera(String cameraId) {
		try {
			cameraManager.openCamera(cameraId, openCallback, handler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	private CameraDevice.StateCallback openCallback = new CameraDevice.StateCallback() {
		@Override
		public void onOpened(@NonNull CameraDevice cameraDevice) {
			logI("Camera is opened");
			CameraController.this.cameraDevice = cameraDevice;
			startPreview();
		}

		@Override
		public void onDisconnected(@NonNull CameraDevice cameraDevice) {
			logI("Camera desconnected!");
		}

		@Override
		public void onError(@NonNull CameraDevice cameraDevice, int i) {
			logE("Camera error!");
			ToastUtil.show("Camera open fail");
		}
	};

	private void startPreview() {
		if (cameraDevice == null) {
			logE("Camera open fail, can not to preview");
			return;
		}
		if (surface == null) {
			logE("SurfaceTexture is null, can not to preview");
			return;
		}
		try {
			cameraDevice.createCaptureSession(Arrays.asList(surface), previewSessionCreateCallback, handler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
			logE("Preview session create failed");
		}
	}

	private CameraCaptureSession.StateCallback previewSessionCreateCallback = new CameraCaptureSession.StateCallback() {

		@Override
		public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
			logI("Preview session is created");
			CaptureRequest.Builder previewBuilder = null;
			try {
				previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
			} catch (CameraAccessException e) {
				e.printStackTrace();
			}
			previewBuilder.addTarget(surface);
			try {
				cameraCaptureSession.setRepeatingRequest(previewBuilder.build(), previewCallback, handler);
			} catch (CameraAccessException e) {
				e.printStackTrace();
				logE("Preview request failed");
			}
		}

		@Override
		public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
			logE("Camera preview failed");
		}
	};


	private CameraCaptureSession.CaptureCallback previewCallback = new CameraCaptureSession.CaptureCallback() {
		@Override
		public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
			super.onCaptureStarted(session, request, timestamp, frameNumber);
//			logI("Camera start preview");
			captureSession = session;
		}

		@Override
		public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
			super.onCaptureProgressed(session, request, partialResult);
		}

		@Override
		public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
			super.onCaptureCompleted(session, request, result);
		}

		@Override
		public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
			super.onCaptureFailed(session, request, failure);
		}

		@Override
		public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session, int sequenceId, long frameNumber) {
			super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
		}

		@Override
		public void onCaptureSequenceAborted(@NonNull CameraCaptureSession session, int sequenceId) {
			super.onCaptureSequenceAborted(session, sequenceId);
		}

		@Override
		public void onCaptureBufferLost(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull Surface target, long frameNumber) {
			super.onCaptureBufferLost(session, request, target, frameNumber);
		}
	};

	private void logE(String string) {
		Log.e(TAG, string);
	}

	private void logI(String string) {
		Log.i(TAG, string);
	}

	public synchronized void release() {
		if (surface != null) {
			this.surface.release();
			this.surface = null;
		}
		if (captureSession != null) {
			captureSession.close();
			captureSession = null;
		}
		if (thread != null) {
			thread.quit();
			thread = null;
		}
	}

}
