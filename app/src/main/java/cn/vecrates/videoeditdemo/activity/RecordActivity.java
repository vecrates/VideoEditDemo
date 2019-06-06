package cn.vecrates.videoeditdemo.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.vecrates.videoeditdemo.view.CameraView;

public class RecordActivity extends AppCompatActivity {

	@BindView(R.id.sv_preview)
	CameraView cameraView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);
		ButterKnife.bind(this);
	}

	@OnClick(R.id.btn_takepicture)
	void clickTakePicture() {
		cameraView.takePicture();
	}

	private void logE(String string) {
		Log.e(this.getClass().getSimpleName(), string);
	}

	private void logI(String string) {
		Log.i(this.getClass().getSimpleName(), string);
	}

}
