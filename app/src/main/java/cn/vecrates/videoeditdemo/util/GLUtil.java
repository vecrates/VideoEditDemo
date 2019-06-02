package cn.vecrates.videoeditdemo.util;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

/**
 * @author Vecrates.
 * @describe
 */
public class GLUtil {


	/**
	 * 生成 oes 纹理
	 */
	public static int genOESTexture() {
		int[] textures = new int[1];
		GLES20.glGenTextures(1,textures,0);
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
		//设置缩小过滤
		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		//设置放大过滤
		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
		//设置环绕方向S
		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
		//设置环绕方向T
		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
		return textures[0];
	}



}
