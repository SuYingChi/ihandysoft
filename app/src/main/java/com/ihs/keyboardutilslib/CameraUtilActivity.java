package com.ihs.keyboardutilslib;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.ihs.app.framework.activity.HSActivity;

import java.io.File;

import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;
import static android.provider.MediaStore.ACTION_VIDEO_CAPTURE;
import static android.provider.MediaStore.EXTRA_OUTPUT;
import static android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA;
import static android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE;
import static android.provider.MediaStore.INTENT_ACTION_VIDEO_CAMERA;

/**
 * Created by cyril on 05/05/2017.
 */

public class CameraUtilActivity extends HSActivity {

    private static int CAPTURE_PHOTO = 1;
    private static int CAPTURE_AND_SAVE_PHOTO = 2;
    private static int CAPTURE_VIDEO = 3;
    private static int CAPTURE_AND_SAVE_VIDEO = 4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_util);
    }

    public void openStillImageCamera(View view) {
        Intent intent = new Intent();
        intent.setAction(INTENT_ACTION_STILL_IMAGE_CAMERA);
        startActivity(intent);
    }

    public void openStillImageCameraSecure(View view) {
        Intent intent = new Intent();
        intent.setAction(INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
        startActivity(intent);
    }

    public void openVideoCamera(View view) {
        Intent intent = new Intent();
        intent.setAction(INTENT_ACTION_VIDEO_CAMERA);
        startActivity(intent);
    }

    public void captureAndSavePhoto(View view) {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.jpg");
        Uri outputFileUri = Uri.fromFile(file);

        Intent intent = new Intent();
        intent.setAction(ACTION_IMAGE_CAPTURE);
        intent.putExtra(EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(intent, CAPTURE_AND_SAVE_PHOTO);
    }

    public void capturePhoto(View view) {
        Intent intent = new Intent();
        intent.setAction(ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAPTURE_PHOTO);
    }

    public void captureVideo(View view) {
        Intent intent = new Intent();
        intent.setAction(ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, CAPTURE_VIDEO);
    }

//    public void captureVideoWithDurationLimit(View view) {
//        Intent intent = new Intent();
//        intent.setAction(INTENT_ACTION_VIDEO_CAMERA);
//        startActivity(intent);
//    }

    public void captureAndSaveVideo(View view) {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.mp4");
        Uri outputFileUri = Uri.fromFile(file);

        Intent intent = new Intent();
        intent.setAction(ACTION_VIDEO_CAPTURE);
        intent.putExtra(EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(intent, CAPTURE_AND_SAVE_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == CAPTURE_PHOTO) {
                Bitmap bitmap = data.getParcelableExtra("data");
                showBitmap(bitmap);
            } else {
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.jpg");
                showBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
            }
        } else {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT);
        }
    }

    private void showBitmap(Bitmap bitmap) {
        Dialog imageDialog = new Dialog(this);
        imageDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View contentView = getLayoutInflater().inflate(R.layout.dialog_image
                , null);
        ImageView imageView = (ImageView) contentView.findViewById(R.id.image_view);
        imageView.setImageBitmap(bitmap);
        imageDialog.setContentView(contentView);
        imageDialog.show();
    }
}
