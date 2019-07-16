package cn.vecrates.videoeditdemo.util;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * @author Vecrates.
 * @describe
 */
public class FileUtil {

	private final static String SD_DIR_PATH = Environment.getExternalStorageDirectory().getPath();
	public final static String BASE_DIR = SD_DIR_PATH + File.separator + "VideoEditDemo";
	public final static String VIDEO_DIR = BASE_DIR + File.separator + "video";

	public static void checkDir(String path) {
		File dir = new File(path);
		if(!dir.exists()) {
			dir.mkdirs();
		}
	}

	public static String getNewVideoPath() {
		checkDir(VIDEO_DIR);
		String name = System.currentTimeMillis() + ".mp4";
		File file = new File(VIDEO_DIR, name);
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file.getPath();
	}

}
