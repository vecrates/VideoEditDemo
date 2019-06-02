package cn.vecrates.videoeditdemo.shader;

import cn.vecrates.videoeditdemo.util.AssetsUtil;

/**
 * @author Vecrates.
 * @describe
 */
public class BaseDrawer {

	private final static String DIR_NAME = "glsl/";

	public BaseDrawer() {

	}

	public String readShaderFile(String fileName) {
		return AssetsUtil.openFile(DIR_NAME + fileName);
	}

}
