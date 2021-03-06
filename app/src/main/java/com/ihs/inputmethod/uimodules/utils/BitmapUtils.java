package com.ihs.inputmethod.uimodules.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.media.ExifInterface;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.api.utils.HSFileUtils;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hdd on 16/3/25.
 */
public class BitmapUtils {

    public static final float TABLET_WIDTH_SCALE_FACTOR = 0.75f;
    public static final int FILE_URI = 0;
    public static final int ASSET_URI = 1;
    private static final int BUFFER_SIZE = 32 * 1024; // 32 Kb

    /**
     * 缩放图片（屏幕大小）
     *
     * @param filePath
     * @return
     */
    public static Bitmap compressBitmap(String filePath, int width, int height) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        final int heightRatio = Math.round((float) options.outHeight / (float) height);
        final int widthRatio = Math.round((float) options.outWidth / (float) width);

        int inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
        if (inSampleSize < 1) {
            inSampleSize = 1;
        }

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        Bitmap source = BitmapFactory.decodeFile(filePath, options);

        if (source == null) {
            return null;
        }

        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int degree = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
        }

        Matrix matrix = new Matrix();
        matrix.setRotate(degree);

        Bitmap bmRotated = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

        if (degree != 0) {
            source.recycle();
        }

        matrix.reset();
        float scale = 1.0f * width / bmRotated.getWidth();
        if (scale < 1.0f * height / bmRotated.getHeight()) {
            scale = 1.0f * height / bmRotated.getHeight();
        }
        matrix.setScale(scale, scale);
        Bitmap scaledBm = Bitmap.createBitmap(bmRotated, 0, 0, bmRotated.getWidth(), bmRotated.getHeight(), matrix, true);

        if (scaledBm.getWidth() != bmRotated.getWidth() || scaledBm.getHeight() != bmRotated.getHeight()) {
            bmRotated.recycle();
        }

        return scaledBm;
    }



    public static Bitmap addBorder(Bitmap crop, int color) {
        if (null == crop) {
            return null;
        }

        //add white border
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Bitmap result = Bitmap.createBitmap((int) (crop.getWidth() * 1.1), (int) (crop.getHeight() * 1.1), Bitmap.Config.ARGB_8888);
        Matrix matrix = new Matrix();
        matrix.setScale(1.1f, 1.1f);
        Bitmap bg = Bitmap.createBitmap(crop, 0, 0, crop.getWidth(), crop.getHeight(), matrix, true);

        // color
        paint.setColorFilter(new ColorMatrixColorFilter(new float[]{
                0, 0, 0, 0, Color.red(color),
                0, 0, 0, 0, Color.green(color),
                0, 0, 0, 0, Color.blue(color),
                0, 0, 0, 1, 0,
        }));
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(bg, 0, 0, paint);
        canvas.drawBitmap(crop, (bg.getWidth() - crop.getWidth()) / 2, (bg.getHeight() - crop.getHeight()) / 2, null);

        bg.recycle();
        if (!crop.isRecycled()) {
            crop.recycle();
        }
        return result;
    }

    public static Bitmap drawMaskView(Bitmap crop, int width, int height) {
        if (null == crop) {
            return null;
        }

        Matrix matrix = new Matrix();
        matrix.setScale(width * TABLET_WIDTH_SCALE_FACTOR / crop.getWidth(), width * TABLET_WIDTH_SCALE_FACTOR / crop.getWidth());
        Bitmap pic = Bitmap.createBitmap(crop, 0, 0, crop.getWidth(), crop.getHeight(), matrix, true);

        if (!crop.isRecycled()) {
            crop.recycle();
        }

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setAlpha(217);
        Bitmap bg = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bg.setHasAlpha(true);

        Canvas canvas = new Canvas(bg);
        canvas.drawRect(new Rect(0, 0, (width - pic.getWidth()) / 2, height), paint);
        canvas.drawRect(new Rect((width + pic.getWidth()) / 2, 0, width, height), paint);
        canvas.drawRect(new Rect((width - pic.getWidth()) / 2, pic.getHeight(), (width + pic.getWidth()) / 2, height), paint);
        canvas.save();
        canvas.drawBitmap(pic, (bg.getWidth() - pic.getWidth()) / 2, 0, null);


        return bg;
    }



    public static Bitmap decodeImage(String path, int uriType) throws IOException {
        return decodeImage(path, uriType, HSDisplayUtils.getScreenWidthForContent(), HSDisplayUtils.getScreenHeightForContent());
    }

    public static Bitmap decodeImage(String path, int uriType, int width, int height) throws IOException {
        InputStream inputStream = getStream(uriType, path);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);

        final int heightRatio = Math.round((float) options.outHeight / (float) height);
        final int widthRatio = Math.round((float) options.outWidth / (float) width);

        int inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
        if (inSampleSize < 1) {
            inSampleSize = 1;
        }

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;

        inputStream = resetInputStream(inputStream, uriType, path);
        Bitmap sourceBmp = null;
        try {
            sourceBmp = BitmapFactory.decodeStream(inputStream, null, options);
        } catch (OutOfMemoryError error) {
        }
        closeSilently(inputStream);
        return sourceBmp;
    }

    private static InputStream resetInputStream(InputStream inputStream, int uriType, String path) throws IOException {
        if (inputStream.markSupported()) {
            try {
                inputStream.reset();
                return inputStream;
            } catch (IOException ignored) {
            }
        }
        closeSilently(inputStream);
        return getStream(uriType, path);
    }

    private static InputStream getStream(int uriType, String filePath) throws IOException {
        switch (uriType) {
            case ASSET_URI:
                return HSApplication.getContext().getAssets().open(filePath);
            default:
                return new BufferedInputStream(new FileInputStream(filePath), BUFFER_SIZE);
        }
    }

    private static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }
}
