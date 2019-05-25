package cn.vecrates.videoeditdemo.media.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Surface;

import java.util.Arrays;

/**
 * @author Vecrates.
 * @describe
 */
public class CameraController {

	private final static String TAG = CameraController.class.getSimpleName();

	private static class H {
		private static CameraController instance = new CameraController();
	}

	private CameraManager cameraManager;
	private String frontCameraId;
	private String backCameraId;
	private CameraDevice cameraDevice;
	private CameraCaptureSession captureSession;

	private Surface surface;

	private CameraController() {
	}

	public static CameraController getInstance() {
		return H.instance;
	}

	public void setupCamera(Context context) {
		cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
		try {
			CameraCharacteristics characteristics;
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
			logE("execute setupCamera exception!");
		}
	}

	public void openFrontCamera(Surface surface) {
		if (frontCameraId.equals("")) {
			logE("The device is not front camera");
			return;
		}
		this.surface = surface;
		openCamera(frontCameraId);
	}

	public void openBackCamera(Surface surface) {
		if (backCameraId.equals("")) {
			logE("The device is not back camera");
			return;
		}
		this.surface = surface;
		openCamera(backCameraId);
	}

	@SuppressLint("MissingPermission")
	private void openCamera(String cameraId) {
		try {
			cameraManager.openCamera(cameraId, stateCallback, null);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
		@Override
		public void onOpened(@NonNull CameraDevice cameraDevice) {
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
			cameraDevice.createCaptureSession(Arrays.asList(surface), sessionCreateCallback, null);
		} catch (CameraAccessException e) {
			e.printStackTrace();
			logE("Preview session create failed");
		}
	}

	private CameraCaptureSession.StateCallback sessionCreateCallback = new CameraCaptureSession.StateCallback() {

		@Override
		public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
			CaptureRequest.Builder previewBuilder = null;
			try {
				previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
			} catch (CameraAccessException e) {
				e.printStackTrace();
			}
			previewBuilder.addTarget(surface);
			try {
				cameraCaptureSession.setRepeatingRequest(previewBuilder.build(), previewCallback, null);
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

}
