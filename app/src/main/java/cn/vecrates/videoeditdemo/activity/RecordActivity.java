package cn.vecrates.videoeditdemo.activity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import butterknife.BindView;
import cn.vecrates.videoeditdemo.MyApplication;
import cn.vecrates.videoeditdemo.media.camera.CameraController;

public class RecordActivity extends AppCompatActivity {

	@BindView(R.id.sv_preview)
	GLSurfaceView previeSurface;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);
		initCamera();
	}

	private void initCamera() {
		CameraController.getInstance().setupCamera(MyApplication.appContext);
		CameraController.getInstance().openBackCamera(previeSurface.getHolder().getSurface());
	}

}
