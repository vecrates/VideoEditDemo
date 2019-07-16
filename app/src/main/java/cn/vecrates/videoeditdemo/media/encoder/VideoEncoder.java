package cn.vecrates.videoeditdemo.media.encoder;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

/**
 * @author Vecrates.
 * @describe
 */
public class VideoEncoder extends BaseEncoder {

	private final static String TAG = VideoEncoder.class.getSimpleName();

	private int width;
	private int height;
	private int frameRate;

	public VideoEncoder(int width, int height, int frameRate) throws Exception {
		super(MIME_VIDEO);
		this.width = width;
		this.height = height;
		this.frameRate = frameRate;
	}

	@Override
	public void config() {
		MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
		//颜色格式
		format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
		//码率，x3 中码率
		format.setInteger(MediaFormat.KEY_BIT_RATE, width * height * frameRate * 1);
		// 调整码率的控流模式
		format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
		//帧率
		format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
		//I帧间隔
		format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
		//第二个参数为解码使用的 Surface
		//第四个参数,编码 H264 的时候,固定 CONFIGURE_FLAG_ENCODE, 播放的时候传入0即可;
		encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
	}

	@Override
	protected void signalEndOfInputSteam() {
		encoder.signalEndOfInputStream();
	}

}
