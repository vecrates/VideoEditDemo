package cn.vecrates.videoeditdemo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import cn.vecrates.videoeditdemo.media.camera.CameraController;
import cn.vecrates.videoeditdemo.media.drawer.CameraDrawer;

/**
 * @author Vecrates.
 * @describe
 */
public class CameraView extends GLSurfaceView {

	private final static String TAG = CameraView.class.getSimpleName();

	private CameraController cameraController;
	private CameraDrawer cameraDrawer;

	public CameraView(Context context) {
		this(context, null);
	}

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		cameraController = new CameraController();
		cameraController.setupCamera(getContext());

		cameraDrawer = new CameraDrawer(this);

		setEGLContextClientVersion(2);
		setRenderer(cameraDrawer);
		setRenderMode(RENDERMODE_WHEN_DIRTY);
	}


	public void takePicture(TakePictureCallback callback) {
		if (callback == null) return;
		queueEvent(new Runnable() {
			@Override
			public void run() {
				Bitmap bitmap = cameraDrawer.surfaceShoot();
				if (bitmap == null) {
					callback.onTakePictureFail();
				} else {
					callback.onTakePicture(bitmap);
				}
			}
		});
	}

	public void startRecord(String savePath) {
		queueEvent(new Runnable() {
			@Override
			public void run() {
				cameraDrawer.startEncode(savePath);
			}
		});
	}

	public void stopRecord() {
		queueEvent(new Runnable() {
			@Override
			public void run() {
				cameraDrawer.stopEncode();
			}
		});
	}

	public boolean isRecording() {
		return cameraDrawer.isRecording();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		cameraController.release();
		cameraDrawer.release();
	}

	public void openCamera() {
		cameraController.openBackCamera(cameraDrawer.getSurfaceTexture(), getWidth(), getHeight());
	}

	public void setStateListener(CameraViewStateListener listener) {
		cameraDrawer.setStateListener(listener);
	}

	public interface CameraViewStateListener {
		void onSurfaceCreated();

		void onSurfaceChanged(int width, int height);
	}

	public interface TakePictureCallback {
		void onTakePicture(Bitmap bitmap);

		void onTakePictureFail();
	}

	private void logE(String string) {
		Log.e(TAG, string);
	}

	private void logI(String string) {
		Log.i(TAG, string);
	}

}
