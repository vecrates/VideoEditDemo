package cn.vecrates.videoeditdemo.media.drawer;

import android.graphics.SurfaceTexture;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.TextureView;

import cn.vecrates.videoeditdemo.media.decoder.VideoDecoder;
import cn.vecrates.videoeditdemo.media.egl.EglCore;
import cn.vecrates.videoeditdemo.media.shader.FormatFilter;
import cn.vecrates.videoeditdemo.util.GLUtil;

/**
 * @author weiyusong
 * @description
 */
public class VideoDrawer implements SurfaceTexture.OnFrameAvailableListener {

    private final static String TAG = VideoDrawer.class.getSimpleName();

    private TextureView previewSurfaceView;

    private EglCore mEglCore;
    private EGLSurface previewSurface;
    private EGLSurface offscreenSurface;
    private int mTextureId = -1;
    private FormatFilter formatFilter;

    private VideoDecoder decoder;

    private HandlerThread thread;
    private Handler handler;

    public VideoDrawer(TextureView previewSurfaceView) {
        this.previewSurfaceView = previewSurfaceView;
        this.previewSurfaceView.setSurfaceTextureListener(surfaceTextureListener);
        this.previewSurfaceView.setOpaque(false);

        initThread();
        initGLContext();
        initRender();
    }

    private void initThread() {
        thread = new HandlerThread("VideoDrawerThread");
        thread.start();
        handler = new Handler(thread.getLooper());
    }

    public void createDecoder(String videoPath) {
        post(() -> {
            try {
                decoder = new VideoDecoder(videoPath);
                decoder.configCodec(mTextureId, this);
            } catch (Exception e) {
                Log.e(TAG, "createDecoder: ", e);
            }
        });
    }

    private void initGLContext() {
        post(() -> {
            mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
            offscreenSurface = mEglCore.createOffscreenSurface(2, 2);
            mEglCore.makeCurrent(offscreenSurface);
        });
    }

    private void initRender() {
        post(() -> {
            mTextureId = GLUtil.genOESTexture();
            formatFilter = new FormatFilter();
        });
    }

    private final TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            post(() -> {
                previewSurface = mEglCore.createWindowSurface(surface);
                mEglCore.makeCurrent(previewSurface);
            });
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            post(() -> {
                mEglCore.releaseSurface(previewSurface);
                previewSurface = null;
            });
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private final SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            post(() -> {
                previewSurface = mEglCore.createWindowSurface(holder.getSurface());
                mEglCore.makeCurrent(previewSurface);
            });
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.d(TAG, "surfaceChanged: " + width + " " + height);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            post(() -> {
                mEglCore.releaseSurface(previewSurface);
                previewSurface = null;
            });
        }
    };

    public void start() {
        post(() -> {
            try {
                new Thread(() -> {
                    decoder.startDecode();
                }).start();
            } catch (Exception e) {
                Log.e(TAG, "startDecode: ", e);
            }
        });
    }

    private void draw() {

        Log.d(TAG, "draw: >>>>>><<<<<<<");
        GLES20.glViewport(0, 0, previewSurfaceView.getWidth(), previewSurfaceView.getHeight());

        formatFilter.draw(mTextureId);

        Log.e(TAG, "draw: " + previewSurfaceView.isOpaque());

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        surfaceTexture.updateTexImage();
        draw();
        if (mEglCore != null && previewSurface != null) {
            mEglCore.swapBuffers(previewSurface);
        }
    }

    public void releaseDecoder() {
        post(() -> {
            if (decoder != null) {
                decoder.stopDecode();
                decoder.release();
                decoder = null;
            }
        });
    }

    public void release() {

        if (decoder != null) {
            decoder.stopDecode();
            decoder.release();
            decoder = null;
        }

        if (previewSurface != null && mEglCore != null) {
            mEglCore.releaseSurface(previewSurface);
            previewSurface = null;
        }

        if (offscreenSurface != null && mEglCore != null) {
            mEglCore.releaseSurface(offscreenSurface);
            offscreenSurface = null;
        }

        if (mTextureId != -1) {
            GLES20.glDeleteTextures(1, new int[]{mTextureId}, 0);
            mTextureId = -1;
        }

        if (mEglCore != null) {
            mEglCore.release();
            mEglCore = null;
        }

        if (thread != null) {
            thread.quit();
            thread = null;
        }

    }

    public void post(Runnable runnable) {
        handler.post(runnable);
    }


}
