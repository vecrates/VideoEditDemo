package cn.vecrates.videoeditdemo.media.drawer;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import cn.vecrates.videoeditdemo.media.muxer.CameraMuxer;
import cn.vecrates.videoeditdemo.media.shader.FormatFilter;
import cn.vecrates.videoeditdemo.media.view.CameraView;
import cn.vecrates.videoeditdemo.util.GLUtil;

/**
 * @author weiyusong
 * @description
 */
public class CameraDrawer implements SurfaceTexture.OnFrameAvailableListener,
		GLSurfaceView.Renderer, CameraMuxer.RecordDrawListener {

	private CameraView.CameraViewStateListener stateListener;

	private WeakReference<GLSurfaceView> cameraViewRef;

	private int width;
	private int height;

	private int oesTextureId;
	private SurfaceTexture surfaceTexture;

	private FormatFilter formatFilter;

	private CameraMuxer cameraMuxer;

	private volatile boolean recording = false;
	private boolean inited = false;

	public CameraDrawer(GLSurfaceView glSurfaceView) {
		cameraViewRef = new WeakReference<>(glSurfaceView);
	}

	private void init() {
		if (inited) return;

		formatFilter = new FormatFilter();

		oesTextureId = GLUtil.genOESTexture();
		surfaceTexture = new SurfaceTexture(oesTextureId);
		surfaceTexture.setOnFrameAvailableListener(this);

		cameraMuxer = new CameraMuxer();

		inited = true;
	}

	public SurfaceTexture getSurfaceTexture() {
		return surfaceTexture;
	}

	public void draw() {
		if (surfaceTexture == null) return;
		try {
			surfaceTexture.updateTexImage();
		} catch (Exception e) {
			e.printStackTrace();
		}

		previewDraw();
		recordDraw();
	}

	private void previewDraw() {
		GLES20.glViewport(0, 0, width, height);
		formatFilter.draw(oesTextureId);
	}

	private void recordDraw() {
		if (!recording) return;
		cameraMuxer.drawFrame();
	}

	@Override
	public void onRecordDraw(int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		formatFilter.draw(oesTextureId);
	}

	public void startEncode(String savePath) {
		cameraMuxer.start(savePath, width, height, EGL14.eglGetCurrentContext(), this);
		recording = true;
	}

	public void stopEncode() {
		recording = false;
		cameraMuxer.stop();
	}

	public boolean isRecording() {
		return recording;
	}

	public Bitmap surfaceShoot() {
		return GLUtil.getBitmapFromBuffer(0, 0, width, height);
	}

	public boolean isInited() {
		return inited;
	}

	public void release() {
		if (surfaceTexture != null) {
			surfaceTexture.release();
			surfaceTexture = null;
		}

		if (cameraMuxer != null) {
			cameraMuxer.destroy();
			cameraMuxer = null;
		}

		if (cameraViewRef != null) {
			cameraViewRef.clear();
			cameraViewRef = null;
		}

		if (formatFilter != null) {
			formatFilter.release();
			formatFilter = null;
		}

		inited = false;
	}

	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture) {
		if (cameraViewRef.get() != null) {
			cameraViewRef.get().requestRender();
		}
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		init();
		if (stateListener != null) {
			stateListener.onSurfaceCreated();
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		this.width = width;
		this.height = height;
		if (stateListener != null) {
			stateListener.onSurfaceChanged(width, height);
		}
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		draw();
	}

	public void setStateListener(CameraView.CameraViewStateListener listener) {
		this.stateListener = listener;
	}
}
