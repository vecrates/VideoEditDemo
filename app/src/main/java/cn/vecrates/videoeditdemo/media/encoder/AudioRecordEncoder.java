package cn.vecrates.videoeditdemo.media.encoder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;

import java.nio.ByteBuffer;

/**
 * @author Vecrates.
 * @describe
 */
public class AudioRecordEncoder extends BaseEncoder {

	private Object releaseLock = new Object();

	private AudioRecord recorder;

	private volatile boolean recording;
	private int bufferSize;

	public AudioRecordEncoder() throws Exception {
		super(MIME_AUDIO);
	}

	@Override
	public void startEncoder() throws Exception {
		super.startEncoder();
		new Thread(recordRunnable).start();
	}

	@Override
	public void config() {
		MediaFormat format = MediaFormat.createAudioFormat(mime, 44100, 2);
		format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);//编码方式
		format.setInteger(MediaFormat.KEY_BIT_RATE, 128000);//比特率
		encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

		bufferSize = AudioRecord.getMinBufferSize(44100,
				AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
		recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
				AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
	}

	@Override
	protected void signalEndOfInputSteam() {

	}

	public void stopRecord() {
		recording = false;
		recorder.stop();
	}

	@Override
	public synchronized void release() {
		synchronized (releaseLock) {
			super.release();
			if (recorder != null) {
				recorder.release();
				recorder = null;
			}
		}
	}

	private Runnable recordRunnable = new Runnable() {

		private long startTime = -1;

		@Override
		public void run() {
			recorder.startRecording();
			recording = recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING;
			synchronized (releaseLock) {
				while (recording) {
					writeBuffer(false);
					try {
						drain(false);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				writeBuffer(true);
				try {
					drain(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		private void writeBuffer(boolean eos) {
			int index = encoder.dequeueInputBuffer(TIME_OUT_MS);
			if (index > 0) {
				ByteBuffer buffer;
				buffer = encoder.getInputBuffer(index);
				buffer.clear();
				int length = recorder.read(buffer, bufferSize);
				startTime = startTime == -1 ? System.nanoTime() : startTime;
				long timeUs = (System.nanoTime() - startTime) / 1000L;

				if (eos) {
					encoder.queueInputBuffer(index, 0, length, timeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
				} else {
					encoder.queueInputBuffer(index, 0, length, timeUs, 0);
				}
			}
		}
	};

}
