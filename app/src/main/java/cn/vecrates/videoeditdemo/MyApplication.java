package cn.vecrates.videoeditdemo;

import android.app.Application;
import android.content.Context;

/**
 * @author Vecrates.
 * @describe
 */
public class MyApplication extends Application {
	public static Context appContext;

	@Override
	public void onCreate() {
		super.onCreate();
		appContext = getApplicationContext();
	}
}
