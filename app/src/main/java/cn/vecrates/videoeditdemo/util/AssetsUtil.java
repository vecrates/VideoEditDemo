package cn.vecrates.videoeditdemo.util;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import cn.vecrates.videoeditdemo.MyApplication;

/**
 * @author Vecrates.
 * @describe
 */
public class AssetsUtil {


	public static String openFile(String path) {
		AssetManager manager = MyApplication.appContext.getAssets();
		String string = null;
		try {
			string = getStringByInputSteam(manager.open(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return string;
	}

	public static String getStringByInputSteam(InputStream is) {
		if (is == null) return null;
		InputStreamReader streamReader = null;
		StringBuilder sb = null;
		BufferedReader reader = null;
		try {
			streamReader = new InputStreamReader(is);
			reader = new BufferedReader(streamReader);
			String line;
			sb = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} finally {

			try {
				if (reader != null) {
					reader.close();
				}

				if (streamReader != null) {
					streamReader.close();
				}
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

}
