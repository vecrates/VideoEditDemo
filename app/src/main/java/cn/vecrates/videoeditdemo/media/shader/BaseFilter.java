package cn.vecrates.videoeditdemo.media.shader;

import android.opengl.GLES20;

import cn.vecrates.videoeditdemo.util.AssetsUtil;

/**
 * @author Vecrates.
 * @describe
 */
public class BaseFilter {

	private final static String DIR_NAME = "glsl/";

	protected int program = -1;

	public BaseFilter() {

	}

	public String readShaderFile(String fileName) {
		return AssetsUtil.openFile(DIR_NAME + fileName);
	}

	public void release() {
		if (program != -1) {
			GLES20.glDeleteProgram(program);
			program = -1;
		}
	}
}
