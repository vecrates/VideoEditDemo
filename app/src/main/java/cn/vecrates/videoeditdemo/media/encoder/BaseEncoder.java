package cn.vecrates.videoeditdemo.media.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;

/**
 * @author Vecrates.
 * @describe
 */
public abstract class BaseEncoder {

	private final static String TAG = BaseEncoder.class.getSimpleName();

	public final static String MIME_VIDEO = "video/avc";
	public final static String MIME_AUDIO = "audio/mp4a-latm";

	protected final static int TIME_OUT_MS = 10000;
	protected static final int IFRAME_INTERVAL = 4; //关键帧间隔

	protected MediaCodec encoder;
	protected Surface encodeSurface;

	protected MediaCodec.BufferInfo bufferInfo;
	protected int trackIndex = -1;
	protected String mime;

	protected EncodeListener listener;

	public BaseEncoder(String mime) throws Exception {
		this.mime = mime;
	}

	public void startEncoder() throws Exception {
		//Uninitialized state
		encoder = MediaCodec.createEncoderByType(mime);
		//Configured state
		config();

		if (mime == MIME_VIDEO) {
			encodeSurface = encoder.createInputSurface();
		}

		//Flushed state
		encoder.start();

		bufferInfo = new MediaCodec.BufferInfo();
	}

	public abstract void config();

	/**
	 * @return 返回用于接收数据的surface
	 */
	public Surface getEncodeSurface() {
		return encodeSurface;
	}

	/**
	 * 从编码器中读取已编码数据
	 */
	public synchronized void drain(boolean eos) throws Exception {
		if (eos) {
			signalEndOfInputSteam();
		}

		int status;
		ByteBuffer byteBuffer;
		while (true) {
			status = encoder.dequeueOutputBuffer(bufferInfo, TIME_OUT_MS);
			if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
				//没有数据可读取
				if (!eos) {
					break;
				}
			} else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
				//输出格式改变
				MediaFormat format = encoder.getOutputFormat();
				if (listener != null) {
					trackIndex = listener.onEncodeFormatChanged(mime, format);
				}
			} else if (status < 0) {
				logE("读取输出数据失败, status=" + status);
			} else {
				byteBuffer = encoder.getOutputBuffer(status);
				if (byteBuffer != null && listener != null) {
					logE("mime index=" + trackIndex);
					listener.onFrameEncoded(trackIndex, byteBuffer, bufferInfo);
				}

				encoder.releaseOutputBuffer(status, false);

				if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
					break;      // out of while
				}

			}
		}
	}

	protected abstract void signalEndOfInputSteam();

	public synchronized void release() {
		if (encoder != null) {
			encoder.release();
			encoder = null;
		}

		if (encodeSurface != null) {
			encodeSurface.release();
			encodeSurface = null;
		}

		listener = null;
	}

	public void setEncodeListener(EncodeListener listener) {
		this.listener = listener;
	}

	/**
	 * 数据编码回调
	 */
	public interface EncodeListener {
		void onFrameEncoded(int trackIndex, ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo);

		int onEncodeFormatChanged(String mime, MediaFormat format);
	}

	private void logE(String string) {
		Log.e(TAG, mime + " " + string);
	}

	private void logI(String string) {
		Log.i(TAG, mime + " " + string);
	}

}
