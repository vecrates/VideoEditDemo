package cn.vecrates.videoeditdemo.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import cn.vecrates.videoeditdemo.MyApplication;
import cn.vecrates.videoeditdemo.media.camera.CameraController;
import cn.vecrates.videoeditdemo.shader.FormatDrawer;
import cn.vecrates.videoeditdemo.util.GLUtil;

/**
 * @author Vecrates.
 * @describe
 */
public class CameraView extends GLSurfaceView implements
		GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

	private final static String TAG = CameraView.class.getSimpleName();

	private int oesTextureId;
	private SurfaceTexture surfaceTexture;

	private FormatDrawer formatDrawer;

	private CameraViewStateListener stateListener;

	public CameraView(Context context) {
		this(context, null);
	}

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		setEGLContextClientVersion(2);
		setRenderer(this);
		setRenderMode(RENDERMODE_WHEN_DIRTY);
	}

	@Override
	public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
		createSurfaceTexture();
		cameraSetup();
		if (stateListener != null) {
			stateListener.onSurfaceCreated();
		}
	}

	private void createSurfaceTexture() {
		formatDrawer = new FormatDrawer();

		oesTextureId = GLUtil.genOESTexture();
		surfaceTexture = new SurfaceTexture(oesTextureId);

		surfaceTexture.setOnFrameAvailableListener(this);
	}

	@Override
	public void onSurfaceChanged(GL10 gl10, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		cameraOpen(width, height);
		if (stateListener != null) {
			stateListener.onSurfaceChanged(surfaceTexture, width, height);
		}
	}

	@Override
	public void onDrawFrame(GL10 gl10) {
		if (surfaceTexture == null) return;
		queueEvent(() -> {
			surfaceTexture.updateTexImage();
			formatDrawer.draw(oesTextureId);
		});
	}

	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture) {
		requestRender();
	}

	public void takePicture() {
		CameraController.getInstance().takePicture();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		CameraController.getInstance().release();
	}

	private void cameraSetup() {
		CameraController.getInstance().setupCamera(getContext());
	}

	private void cameraOpen(int width, int height) {
		CameraController.getInstance().openBackCamera(surfaceTexture, width, height);
	}


	public void setStateListener(CameraViewStateListener listener) {
		this.stateListener = listener;
	}

	public interface CameraViewStateListener {
		void onSurfaceCreated();

		void onSurfaceChanged(SurfaceTexture surfaceTexture, int width, int height);
	}

	private void logE(String string) {
		Log.e(TAG, string);
	}

	private void logI(String string) {
		Log.i(TAG, string);
	}

}
