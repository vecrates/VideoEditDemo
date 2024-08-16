package cn.vecrates.videoeditdemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.TextureView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.vecrates.videoeditdemo.media.drawer.VideoDrawer;

public class PlayerActivity extends AppCompatActivity {

    private VideoDrawer videoDrawer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        ButterKnife.bind(this);

        initView();
    }

    private void initView() {
        TextureView previewSv = findViewById(R.id.sv_preview);
        videoDrawer = new VideoDrawer(previewSv);
    }

    private void onSelectVideo(String path) {
        videoDrawer.releaseDecoder();
        videoDrawer.createDecoder(path);
        videoDrawer.start();
    }

    @OnClick(R.id.tv_album)
    void clickAlbum() {
        PictureSelector.create(this)
                .openGallery(PictureMimeType.ofVideo())
                .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
        if (selectList.isEmpty()) {
            return;
        }
        onSelectVideo(selectList.get(0).getPath());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoDrawer != null) {
            videoDrawer.release();
            videoDrawer = null;
        }
    }
}
