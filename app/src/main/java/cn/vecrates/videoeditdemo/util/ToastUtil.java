package cn.vecrates.videoeditdemo.util;

import android.widget.Toast;

import cn.vecrates.videoeditdemo.MyApplication;

/**
 * @author Vecrates.
 * @describe
 */
public class ToastUtil {

	public static void show(String text) {
		Toast.makeText(MyApplication.appContext, text, Toast.LENGTH_SHORT).show();
	}

}
