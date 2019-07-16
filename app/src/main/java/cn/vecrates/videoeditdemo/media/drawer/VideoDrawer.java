package cn.vecrates.videoeditdemo.media.drawer;

import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import cn.vecrates.videoeditdemo.media.MediaType;
import cn.vecrates.videoeditdemo.media.decoder.BaseDecoder;
import cn.vecrates.videoeditdemo.media.egl.EglCore;
import cn.vecrates.videoeditdemo.media.egl.WindowSurface;
import cn.vecrates.videoeditdemo.media.shader.FormatFilter;
import cn.vecrates.videoeditdemo.util.GLUtil;

/**
 * @author weiyusong
 * @description
 */
public class VideoDrawer implements BaseDecoder.DecodeCallback, SurfaceTexture.OnFrameAvailableListener {

	private final static String TAG = VideoDrawer.class.getSimpleName();

	private WindowSurface mInputWindowSurface;
	private EglCore mEglCore;
	private int mTextureId = -1;
	private SurfaceTexture surfaceTexture;
	private FormatFilter formatFilter;

	private View overLayer;
	private int overTextureId = -1;
	private SurfaceTexture overSurfaceTexture;
	private Surface overSurface;

	private BaseDecoder decoder;

	private HandlerThread thread;
	private Handler handler;

	public VideoDrawer(String videoPath) {
		initThread();
		initGLContext();
		createTexture();
		createDecoder(videoPath);
	}

	private void initThread() {
		thread = new HandlerThread("VideoDrawerThread");
		thread.start();
		handler = new Handler(thread.getLooper(), new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				return true;
			}
		});
	}

	private void createDecoder(String videoPath) {
		post(new Runnable() {
			@Override
			public void run() {
				try {
					decoder = new BaseDecoder(MediaType.VIDEO, videoPath);
					decoder.setCallback(VideoDrawer.this);
					decoder.configCodec(mTextureId, VideoDrawer.this);
				} catch (Exception e) {
					e.printStackTrace();
					logE("createDecoder error");
				}
			}
		});
	}

	private void createEncoder() {
		post(new Runnable() {
			@Override
			public void run() {
//                String path = FileManager.getNewMp4Path();
			}
		});

	}

	private void initGLContext() {
		post(new Runnable() {
			@Override
			public void run() {
				mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
				SurfaceTexture temp = new SurfaceTexture(0);
				mInputWindowSurface = new WindowSurface(mEglCore, temp);
				mInputWindowSurface.makeCurrent();
			}
		});
	}

	private void createTexture() {
		post(new Runnable() {
			@Override
			public void run() {
				mTextureId = GLUtil.genOESTexture();
				Log.i(TAG, "run: textureId=" + mTextureId);
				surfaceTexture = new SurfaceTexture(mTextureId);
				formatFilter = new FormatFilter();
			}
		});
	}

	private void createOverlayTexture() {
		post(new Runnable() {
			@Override
			public void run() {
				overTextureId = GLUtil.genOESTexture();
				overSurfaceTexture = new SurfaceTexture(overTextureId);
				overSurfaceTexture.setDefaultBufferSize(decoder.getVideoWidth(), decoder.getVideoHeight());
				overSurface = new Surface(overSurfaceTexture);
			}
		});
	}

	public void setOverLayer(View overLayer) {
		this.overLayer = overLayer;
		createOverlayTexture();
	}

	@Override
	public boolean onFrameDecoded(BaseDecoder decoder, ByteBuffer outputBuffer, MediaCodec.BufferInfo bufferInfo) {
		logE("====");
		return false;
	}

	public void startDecode() {
		post(new Runnable() {
			@Override
			public void run() {
				try {
					logI("startDecode");
					decoder.startDecode();
				} catch (Exception e) {
					e.printStackTrace();
					logE("startDecode error");
				}
			}
		});
	}

	private final List<Long> timestamps = new ArrayList<>();

	private void doDecode() {
//        decoder.startDecode();
//        int index;
//        boolean hasError = false;
//        int tryTimes = 0;
//        long totalDecodeOffset = 0;
//        long totalDecodeTime = 0;
//        long globalDecodeTime = 0;
//        long globalDecodeOffset = decoder.getCurDecodeTime();
//        while (globalDecodeOffset + 2 < decoder.getDuration() && !decoder.isOutputEOS()) {
//            logE("===1 gt=" + globalDecodeOffset);
//            if (timestamps.size() > 0) {
//                try {
//                    Thread.sleep(5);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                if (tryTimes++ > 200) {
//                    index = timestamps.size() - 1;
//                    index = index > 0 ? index : 0;
//                    if (index < timestamps.size() && index >= 0) {
//                        timestamps.remove(index);
//                    }
//                    tryTimes = 0;
//                }
//            } else {
//                tryTimes = 0;
//                boolean b;
//                try {
//                    b = decoder.decodeNextPacket(decoder.getCurDecodeTime() + 2);
//                } catch (IllegalStateException e) {
//                    hasError = true;
//                    break;
//                }
//                globalDecodeOffset = decoder.getCurDecodeTime();
//                globalDecodeTime = globalDecodeOffset;
//                if (b && globalDecodeOffset >= 0) {
//                    totalDecodeTime = totalDecodeOffset + globalDecodeTime;
//                }
//            }
//        }
//
//        while (totalDecodeTime < decoder.getDuration()) {
//            totalDecodeTime += 2;
//            synchronized (timestamps) {
//                timestamps.add(totalDecodeTime);
//            }
//            final CountDownLatch countDownLatch = new CountDownLatch(1);
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    draw();
//                    countDownLatch.countDown();
//                }
//            });
//            try {
//                countDownLatch.await();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
	}

	private void draw() {

		formatFilter.draw(mTextureId);

		if (overLayer != null) {
			Canvas canvas = null;
			try {
				canvas = overSurface.lockCanvas(null);
			} catch (IllegalArgumentException e) {
				return;
			}
			canvas.drawColor(0, PorterDuff.Mode.CLEAR);
			overLayer.draw(canvas);
			overSurface.unlockCanvasAndPost(canvas);
		}
	}

	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture) {
		logE("onFrameAvailable");
		surfaceTexture.updateTexImage();
	}

	public void release() {

		if (decoder != null) {
			decoder.release();
			decoder = null;
		}

		if (mInputWindowSurface != null) {
			mInputWindowSurface.release();
			mInputWindowSurface = null;
		}

		if (overSurface != null) {
			overSurface.release();
			overSurface = null;
		}

		if (mTextureId != -1) {
			GLES20.glDeleteTextures(1, new int[]{mTextureId}, 0);
			mTextureId = -1;
		}

		if (overTextureId != -1) {
			GLES20.glDeleteTextures(1, new int[]{overTextureId}, 0);
			overTextureId = -1;
		}

		if (thread != null) {
			thread.quit();
			thread = null;
		}


	}

	public void post(Runnable runnable) {
		handler.post(runnable);
	}

	public interface VideoDecodeListener {

		void onDecoding();

		void onDecodeFail();
	}

	private void logE(String string) {
		Log.e(TAG, string);
	}

	private void logI(String string) {
		Log.i(TAG, string);
	}

}
