package cn.vecrates.videoeditdemo.media.decoder;

import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

import cn.vecrates.videoeditdemo.media.MediaType;

/**
 * @author Vecrates.
 * @describe
 */
public class BaseDecoder {

	private final static String TAG = BaseDecoder.class.getSimpleName();

	private final static String MIME_TYPE = "video/avc";

	public MediaExtractor videoExtractor;
	public MediaCodec decoder;
	private MediaFormat mediaFormat;

	private int textureId;
	private SurfaceTexture surfaceTexture;
	private Surface surface;

	private boolean decoding;
	private String mediaPath;

	public BaseDecoder(MediaType type, String mediaPath) throws IOException {
//		this.videoExtractor = new MediaExtractor();
//		this.videoExtractor.setDataSource(mediaPath);
		this.mediaPath = mediaPath;
	}

	public void configCodec(int textureId, SurfaceTexture.OnFrameAvailableListener listener) {
		this.textureId = textureId;
		this.surfaceTexture = new SurfaceTexture(textureId);
		this.surface = new Surface(surfaceTexture);
		this.surfaceTexture.setOnFrameAvailableListener(listener);

//		int videoTrackIndex;
//		//获取视频所在轨道
//		videoTrackIndex = getMediaTrackIndex(videoExtractor, MIME_TYPE);
//		if (videoTrackIndex >= 0) {
//			mediaFormat = videoExtractor.getTrackFormat(videoTrackIndex);
//			videoExtractor.selectTrack(videoTrackIndex);
//			try {
//				decoder = MediaCodec.createDecoderByType(mediaFormat.getString(MediaFormat.KEY_MIME));
//				decoder.configure(mediaFormat, surface, null, 0);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
	}

	//获取指定类型媒体文件所在轨道
	private int getMediaTrackIndex(MediaExtractor videoExtractor, String MEDIA_TYPE) {
		int trackIndex = -1;
		for (int i = 0; i < videoExtractor.getTrackCount(); i++) {
			//获取视频所在轨道
			MediaFormat mediaFormat = videoExtractor.getTrackFormat(i);
			String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
			if (mime.startsWith(MEDIA_TYPE)) {
				trackIndex = i;
				break;
			}
		}
		return trackIndex;
	}

	public void startDecode() throws InterruptedException {
//		decoder.start();

//		Thread.currentThread().sleep(200);
		MediaExtractor videoExtractor = new MediaExtractor();
		MediaCodec videoCodec = null;
		try {
			videoExtractor.setDataSource(mediaPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		int videoTrackIndex;
		//获取视频所在轨道
		videoTrackIndex = getMediaTrackIndex(videoExtractor, "video/");
		if (videoTrackIndex >= 0) {
			MediaFormat mediaFormat = videoExtractor.getTrackFormat(videoTrackIndex);
			int width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
			int height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
			//视频长度:秒
			float time = mediaFormat.getLong(MediaFormat.KEY_DURATION) / 1000000;

			videoExtractor.selectTrack(videoTrackIndex);
			try {
				videoCodec = MediaCodec.createDecoderByType(mediaFormat.getString(MediaFormat.KEY_MIME));
				videoCodec.configure(mediaFormat, surface, null, 0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (videoCodec == null) {
			Log.v(TAG, "MediaCodec null");
			return;
		}
		videoCodec.start();

		MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
		ByteBuffer[] inputBuffers = videoCodec.getInputBuffers();

		boolean isVideoEOS = false;

		long startMs = System.currentTimeMillis();
		while (!Thread.interrupted()) {
			//将资源传递到解码器
			if (!isVideoEOS) {
				isVideoEOS = putBufferToCoder(videoExtractor, videoCodec, inputBuffers);
				if (isVideoEOS) {
					break;
				}
			}
			int outputBufferIndex = videoCodec.dequeueOutputBuffer(videoBufferInfo, 10000);
			switch (outputBufferIndex) {
				case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
					Log.v(TAG, "format changed");
					break;
				case MediaCodec.INFO_TRY_AGAIN_LATER:
					Log.v(TAG, "解码当前帧超时");
					break;
				case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
					//outputBuffers = videoCodec.getOutputBuffers();
					Log.v(TAG, "output buffers changed");
					break;
				default:
					//直接渲染到Surface时使用不到outputBuffer
					//ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
					//延时操作
					//如果缓冲区里的可展示时间>当前视频播放的进度，就休眠一下
					//渲染
					videoCodec.releaseOutputBuffer(outputBufferIndex, true);
					break;
			}

			if ((videoBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
				break;
			}
		}//end while
		videoCodec.stop();
		videoCodec.release();
		videoExtractor.release();

	}

	//将缓冲区传递至解码器
	private boolean putBufferToCoder(MediaExtractor extractor, MediaCodec decoder, ByteBuffer[] inputBuffers) {
		boolean isMediaEOS = false;
		int inputBufferIndex = decoder.dequeueInputBuffer(10000);
		if (inputBufferIndex >= 0) {
			ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
			int sampleSize = extractor.readSampleData(inputBuffer, 0);
			if (sampleSize < 0) {
				decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
				isMediaEOS = true;
				Log.v(TAG, "media eos");
			} else {
				decoder.queueInputBuffer(inputBufferIndex, 0, sampleSize, extractor.getSampleTime(), 0);
				extractor.advance();
			}
		}
		return isMediaEOS;
	}

	public long getMediaDuration() {
		long time = mediaFormat.getLong(MediaFormat.KEY_DURATION);
		return time;
	}

	public int getVideoWidth() {
		return mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
	}

	public int getVideoHeight() {
		return mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
	}

	public void release() {
		if (decoder != null) {
			decoder.release();
			decoder = null;
		}
	}

	private DecodeCallback callback;

	public void setCallback(DecodeCallback callback) {
		this.callback = callback;
	}

	public interface DecodeCallback {
		boolean onFrameDecoded(BaseDecoder decoder, ByteBuffer outputBuffer, MediaCodec.BufferInfo bufferInfo);
	}

}