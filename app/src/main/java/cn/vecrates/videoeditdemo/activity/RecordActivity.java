package cn.vecrates.videoeditdemo.activity;

import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.vecrates.videoeditdemo.MyApplication;
import cn.vecrates.videoeditdemo.media.camera.CameraController;
import cn.vecrates.videoeditdemo.view.CameraView;

public class RecordActivity extends AppCompatActivity {

	@BindView(R.id.sv_preview)
	CameraView cameraView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);
		ButterKnife.bind(this);
		initCamera();
	}

	private void initCamera() {
		cameraView.setStateListener(new CameraView.CameraViewStateListener() {
			@Override
			public void onSurfaceCreated() {
				logI("surface created");
//				runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
						logI(Thread.currentThread().getName());
						CameraController.getInstance().setupCamera(MyApplication.appContext);
//					}
//				});
			}

			@Override
			public void onSurfaceChanged(SurfaceTexture surfaceTexture, int width, int height) {
				logI("surface changed");
//				runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
				logE("a changed thread=" + Thread.currentThread().getId());
						CameraController.getInstance().openBackCamera(surfaceTexture, width, height);
//					}
//				});
			}
		});
	}

	private void logE(String string) {
		Log.e(this.getClass().getSimpleName(), string);
	}

	private void logI(String string) {
		Log.i(this.getClass().getSimpleName(), string);
	}

}
