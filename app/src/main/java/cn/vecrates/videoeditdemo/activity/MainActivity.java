package cn.vecrates.videoeditdemo.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.vecrates.videoeditdemo.media.drawer.VideoDrawer;
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


	@OnClick(R.id.tv_camera)
	void clickCamera() {
		MainActivityPermissionsDispatcher.toRecordWithPermissionCheck(this);
	}

	@NeedsPermission({Manifest.permission.CAMERA,
			Manifest.permission.RECORD_AUDIO,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.READ_EXTERNAL_STORAGE})
	void toRecord() {
		Intent intent = new Intent(this, RecordActivity.class);
		startActivity(intent);
	}

	@OnClick(R.id.tv_select)
	void clickSelect() {
		MainActivityPermissionsDispatcher.toSelectWithPermissionCheck(this);
	}

	@NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.READ_EXTERNAL_STORAGE})
	void toSelect() {
		PictureSelector.create(this)
				.openGallery(PictureMimeType.ofVideo())
				.forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (resultCode != RESULT_OK) return;
		List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
		VideoDrawer videoDrawer = new VideoDrawer(selectList.get(0).getPath());
		videoDrawer.startDecode();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
										   @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
	}

}
