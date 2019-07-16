package cn.vecrates.videoeditdemo.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.vecrates.videoeditdemo.media.view.CameraView;
import cn.vecrates.videoeditdemo.util.FileUtil;
import cn.vecrates.videoeditdemo.util.ToastUtil;

public class RecordActivity extends AppCompatActivity {

	@BindView(R.id.sv_preview)
	CameraView cameraView;
	@BindView(R.id.iv_picture)
	ImageView pictureIv;

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

			}

			@Override
			public void onSurfaceChanged(int width, int height) {
				cameraView.openCamera();
			}
		});
	}

	@OnClick(R.id.btn_takepicture)
	void clickTakePicture() {
		cameraView.takePicture(new CameraView.TakePictureCallback() {
			@Override
			public void onTakePicture(Bitmap bitmap) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						pictureIv.setImageBitmap(bitmap);
					}
				});
			}

			@Override
			public void onTakePictureFail() {

			}
		});
	}

	@OnClick(R.id.btn_record)
	void clickRecord() {
		if (cameraView.isRecording()) {
			cameraView.stopRecord();
			ToastUtil.show("结束录制");
			return;
		}
		ToastUtil.show("开始录制");
		String savePath = FileUtil.getNewVideoPath();
		logI("savePath=" + savePath);
		cameraView.startRecord(savePath);
	}

	private void logE(String string) {
		Log.e(this.getClass().getSimpleName(), string);
	}

	private void logI(String string) {
		Log.i(this.getClass().getSimpleName(), string);
	}

}
