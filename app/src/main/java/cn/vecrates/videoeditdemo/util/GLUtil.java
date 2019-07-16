package cn.vecrates.videoeditdemo.util;

import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;

/**
 * @author Vecrates.
 * @describe
 */
public class GLUtil {

	public final static float[] IDENTITY_MATRIX;

	static {
		IDENTITY_MATRIX = new float[16];
		Matrix.setIdentityM(IDENTITY_MATRIX, 0);
	}

	/**
	 * 生成 oes 纹理
	 */
	public static int genOESTexture() {
		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
		//设置缩小过滤
		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		//设置放大过滤
		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		//设置环绕方向S
		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		//设置环绕方向T
		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		return textures[0];
	}

	public static int loadShader(int type, String shaderCode) {
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);
		return shader;
	}

	public static Bitmap getBitmapFromBuffer(int x, int y, int width, int height) {
		ByteBuffer rgbaBuf = ByteBuffer.allocateDirect(width * height * 4);
		rgbaBuf.position(0);
		GLES20.glReadPixels(x, y, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, rgbaBuf);

		Bitmap bmp = null;
		try {
			bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			bmp.copyPixelsFromBuffer(rgbaBuf);
			//openGL 和 android 坐标系不同,垂直镜像翻转
			bmp = BitmapUtil.verticalFlip(bmp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bmp;
	}

}
