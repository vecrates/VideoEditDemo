package cn.vecrates.videoeditdemo.media.muxer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.opengl.EGLContext;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

import cn.vecrates.videoeditdemo.media.egl.EglCore;
import cn.vecrates.videoeditdemo.media.egl.WindowSurface;
import cn.vecrates.videoeditdemo.media.encoder.AudioRecordEncoder;
import cn.vecrates.videoeditdemo.media.encoder.BaseEncoder;
import cn.vecrates.videoeditdemo.media.encoder.VideoEncoder;

/**
 * @author weiyusong
 * @description
 */
public class CameraMuxer implements VideoEncoder.EncodeListener {

	private MediaMuxer muxer;
	private VideoEncoder videoEncoder;
	private AudioRecordEncoder audioRecordEncoder;

	private EglCore glCore;
	private WindowSurface encoderGLSurface;

	private HandlerThread thread;
	private Handler handler;

	private int width;
	private int height;
	private String savePath;
	private long starTimestamp = -1;

	private volatile boolean videoEncoding;
	private volatile boolean audioEncoding;
	private volatile boolean muxerStarted;

	private boolean inited;
	private RecordDrawListener drawListener;

	public CameraMuxer() {
		initThread();
	}

	private void initThread() {
		thread = new HandlerThread("CameraMuxerThread");
		thread.start();
		handler = new Handler(thread.getLooper(), new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				return true;
			}
		});
	}

	public void start(String savePath, int width, int height,
					  EGLContext shareContext, RecordDrawListener listener) {
		this.width = width;
		this.height = height;
		this.savePath = savePath;
		this.videoEncoding = false;
		this.audioEncoding = false;
		this.drawListener = listener;
		post(new Runnable() {
			@Override
			public void run() {
				try {
					initMuxer(savePath);
					initEncoder(width, height);
					initGLSurface(shareContext);
				} catch (Exception e) {
					e.printStackTrace();
					release();
					logE("Muxer init failed");
					return;
				}

				inited = true;
			}
		});

	}

	private void initEncoder(int width, int height) throws Exception {
		releaseEncoder();
		videoEncoder = new VideoEncoder(width, height, 25);
		videoEncoder.setEncodeListener(this);
		videoEncoder.startEncoder();

		audioRecordEncoder = new AudioRecordEncoder();
		audioRecordEncoder.setEncodeListener(this);
		audioRecordEncoder.startEncoder();
	}

	private void initGLSurface(EGLContext shareContext) throws Exception {
		releaseGLContext();
		glCore = new EglCore(shareContext, EglCore.FLAG_RECORDABLE);
		encoderGLSurface = new WindowSurface(glCore, videoEncoder.getEncodeSurface(), false);
		encoderGLSurface.makeCurrent();
	}

	private void initMuxer(String path) throws IOException {
		releaseMuxer();
		muxer = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
	}

	public void stop() {
		post(new Runnable() {
			@Override
			public void run() {
				try {
					if (!inited && videoEncoder == null) return;
					videoEncoder.drain(true);
					audioRecordEncoder.stopRecord();
					muxerStarted = false;
					muxer.stop();
					logE("stop");
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					release();
				}
			}
		});
	}

	public long getRecordedTime() {
		return 0;
	}

	public void drawFrame() {
		post(new Runnable() {
			@Override
			public void run() {
				if (!inited || drawListener == null) return;
				try {
					encoderGLSurface.makeCurrent();
					videoEncoder.drain(false);
					drawListener.onRecordDraw(width, height);
					if (starTimestamp == -1) {
						starTimestamp = System.nanoTime();
					}
					swapBuffers();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void swapBuffers() {
		long time = System.nanoTime() - starTimestamp;
		encoderGLSurface.setPresentationTime(time);
		encoderGLSurface.swapBuffers();
	}

	@Override
	public void onFrameEncoded(int trackIndex, ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
		if (trackIndex < 0 || !muxerStarted) return;
		muxer.writeSampleData(trackIndex, encodedData, bufferInfo);
	}

	@Override
	public int onEncodeFormatChanged(String mime, MediaFormat format) {
		int trackIndex = muxer.addTrack(format);
		if (mime.equals(BaseEncoder.MIME_VIDEO)) {
			videoEncoding = true;
		} else if (mime.equals(BaseEncoder.MIME_AUDIO)) {
			audioEncoding = true;
		}
		if (videoEncoding && audioEncoding) {
			muxer.start();
			muxerStarted = true;
		}
		return trackIndex;
	}

	public void post(Runnable runnable) {
		handler.post(runnable);
	}

	private void release() {
		releaseEncoder();
		releaseMuxer();
		releaseGLContext();
		drawListener = null;
		inited = false;
		starTimestamp = -1;
		muxerStarted = false;
		audioEncoding = false;
		videoEncoding = false;
	}

	private void releaseMuxer() {
		if (muxer != null) {
			muxer.release();
			muxer = null;
		}
	}

	private void releaseEncoder() {
		if (videoEncoder != null) {
			videoEncoder.release();
			videoEncoder = null;
		}

		if (audioRecordEncoder != null) {
			audioRecordEncoder.release();
			audioRecordEncoder = null;
		}
	}

	private void releaseGLContext() {
		if (glCore != null) {
			glCore.release();
			glCore = null;
		}

		if (encoderGLSurface != null) {
			encoderGLSurface.release();
			encoderGLSurface = null;
		}
	}

	public void destroy() {
		release();
		if (thread != null) {
			thread.quit();
			thread = null;
		}
	}

	public interface RecordDrawListener {
		void onRecordDraw(int width, int height) throws Exception;
	}

	private void logE(String string) {
		Log.e("CameraMuxer", string);
	}

	private void logI(String string) {
		Log.e("CameraMuxer", string);
	}

}
