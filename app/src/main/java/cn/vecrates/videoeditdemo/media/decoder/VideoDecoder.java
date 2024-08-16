package cn.vecrates.videoeditdemo.media.decoder;

import android.graphics.SurfaceTexture;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Vecrates.
 */
public class VideoDecoder {

    private final static String TAG = VideoDecoder.class.getSimpleName();

    private final static String MIME_TYPE = "video/";

    public MediaExtractor videoExtractor;
    public MediaCodec decoder;
    private MediaFormat mediaFormat;
    private MediaCodec.BufferInfo videoBufferInfo;

    private SurfaceTexture surfaceTexture;
    private Surface surface;

    private boolean decoding;

    public VideoDecoder(String mediaPath) throws IOException {
        this.videoExtractor = new MediaExtractor();
        this.videoExtractor.setDataSource(mediaPath);
    }

    public void configCodec(int textureId, SurfaceTexture.OnFrameAvailableListener listener) throws IOException {
        this.surfaceTexture = new SurfaceTexture(textureId);
        this.surface = new Surface(surfaceTexture);
        this.surfaceTexture.setOnFrameAvailableListener(listener);
        this.videoBufferInfo = new MediaCodec.BufferInfo();

        int videoTrackIndex;
        //获取视频所在轨道
        videoTrackIndex = getMediaTrackIndex(videoExtractor, MIME_TYPE);
        if (videoTrackIndex >= 0) {
            mediaFormat = videoExtractor.getTrackFormat(videoTrackIndex);
            //mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_Format24bitRGB888);
            videoExtractor.selectTrack(videoTrackIndex);
            decoder = MediaCodec.createDecoderByType(mediaFormat.getString(MediaFormat.KEY_MIME));
            decoder.configure(mediaFormat, surface, null, 0);
            decoder.start();
        }
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

    public void startDecode() {

        ByteBuffer[] inputBuffers = decoder.getInputBuffers();

        boolean isVideoEOS;
        decoding = true;

        while (decoding) {
            //将资源传递到解码器
            isVideoEOS = putBufferToCoder(videoExtractor, decoder, inputBuffers);
            /*if (isVideoEOS) {
                Log.d(TAG, "startDecode: eos");
                break;
            }*/
            int outputBufferIndex = decoder.dequeueOutputBuffer(videoBufferInfo, 10000);
            Log.d(TAG, "dequeue output buffer: " + outputBufferIndex);
            switch (outputBufferIndex) {
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.d(TAG, "format changed");
                    continue;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    Log.d(TAG, "timeout");
                    continue;
                default:
                    //直接渲染到Surface时使用不到outputBuffer
                    //ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                    //延时操作
                    //如果缓冲区里的可展示时间>当前视频播放的进度，就休眠一下
                    //渲染

                    if (videoBufferInfo.size > 0) {
                        Image image = decoder.getOutputImage(outputBufferIndex);
                        if (image != null) {
                            Log.e(TAG, "bitmap=: " + image.getWidth() + " " + image.getHeight()
                                    + " format=" + image.getFormat() + " 通道数=" + image.getPlanes().length);
                            image.close();
                        }
                    }

                    decoder.releaseOutputBuffer(outputBufferIndex, true);
                    try {
                        Thread.sleep(41);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "startDecode: ", e);
                    }
                    break;
            }

            if ((videoBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.d(TAG, "startDecode: eos");
                videoExtractor.seekTo(0L, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
                decoder.flush();
                //break;
            }

        }//end while

    }

    //将缓冲区传递至解码器
    private boolean putBufferToCoder(MediaExtractor extractor, MediaCodec decoder,
                                     ByteBuffer[] inputBuffers) {
        boolean isMediaEOS = false;
        int inputBufferIndex = decoder.dequeueInputBuffer(10000);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            int sampleSize = extractor.readSampleData(inputBuffer, 0);
            if (sampleSize < 0) {
                decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                isMediaEOS = true;
                Log.d(TAG, "media eos");
            } else {
                long timestamp = extractor.getSampleTime();
                decoder.queueInputBuffer(inputBufferIndex, 0, sampleSize, timestamp, 0);
                extractor.advance();
                Log.d(TAG, "queue input buffer " + timestamp);
            }
        }
        return isMediaEOS;
    }

    public int getVideoWidth() {
        return mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
    }

    public int getVideoHeight() {
        return mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
    }

    public void stopDecode() {
        decoding = false;
    }

    public void release() {
        stopDecode();
        if (decoder != null) {
            decoder.release();
            decoder = null;
        }
        if (videoExtractor != null) {
            videoExtractor.release();
            videoExtractor = null;
        }
        if (surfaceTexture != null) {
            surfaceTexture.release();
            surfaceTexture = null;
        }
        if (surface != null) {
            surface.release();
            surface = null;
        }
    }

}