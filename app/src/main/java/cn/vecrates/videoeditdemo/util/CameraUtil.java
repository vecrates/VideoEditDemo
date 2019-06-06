package cn.vecrates.videoeditdemo.util;

import android.util.Size;

public class CameraUtil {

    /**
     * 获得比例相同且尺寸最大的size
     *
     * @param sizes
     * @param width  竖屏状态下对width
     * @param height
     * @return
     */
    public static Size getOptimalSize(Size[] sizes, int width, int height) {
        if (sizes == null || sizes.length <= 0) return null;
        Size optimalSize = null;
        float ratioDest = width * 1.f / height;
        float ratio;
        for (Size option : sizes) {
            //宽高相反
            ratio = option.getHeight() * 1.f / option.getWidth();
            if (ratio == ratioDest) {
                optimalSize = optimalSize == null ? option : option.getWidth() > optimalSize.getWidth() ? option : optimalSize;
            }
        }
        return optimalSize == null ? sizes[0] : optimalSize;
    }


}
