package cn.vecrates.videoeditdemo.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);
	}

	@NeedsPermission(Manifest.permission.CAMERA)
	@OnClick(R.id.tv_camera)
	void clickCamera() {
		Intent intent = new Intent(this, RecordActivity.class);
		startActivity(intent);
	}
}
