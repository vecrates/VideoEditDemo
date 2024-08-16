package cn.vecrates.videoeditdemo.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;

import java.nio.ByteBuffer;

public class ImageUtil {

    public static Bitmap getBitmapFromImage(Image image) {
        int totalSize = 0;
        Image.Plane[] planes = image.getPlanes();
        for (Image.Plane plane : planes) {
            totalSize += plane.getBuffer().remaining();
        }
        ByteBuffer totalBuffer = ByteBuffer.allocate(totalSize);
        for (Image.Plane plane : image.getPlanes()) {
            totalBuffer.put(plane.getBuffer());
        }
        byte[] bytes = totalBuffer.array();
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static Bitmap covertBitmap(Bitmap bitmap, boolean hFlip, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        if (hFlip) {
            matrix.postScale(-1, 1);
        }
        Bitmap resultBm = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        resultBm = resultBm == null ? bitmap : resultBm;
        if (bitmap != resultBm) {
            bitmap.recycle();
        }
        return resultBm;
    }


}
