package cn.vecrates.videoeditdemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@OnClick(R.id.tv_camera)
	void clickCamera() {

	}
}
