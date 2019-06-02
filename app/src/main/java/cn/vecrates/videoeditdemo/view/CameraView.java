package cn.vecrates.videoeditdemo.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

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
		logE("v init thread=" + Thread.currentThread().getId());
	}

	@Override
	public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
		createSurfaceTexture();
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
		logE("v changed thread=" + Thread.currentThread().getId());
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

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
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
