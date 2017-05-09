package com.ihs.keyboardutils.alerts;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.ihs.keyboardutils.R;

/**
 * Created by Arthur on 17/5/8.
 */

public class FullScreenDialogActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            String type = intent.getStringExtra("type");
            switch (type) {
                case "charging":
                    initChargingDialog();
                    break;
                case "locker":
                    initLockerDialog();
                    break;
            }
        }
    }

    private void initLockerDialog() {

    }

    private void initChargingDialog() {
        new KCAlert.Builder(this)
                .setTitle("Quickly Open Camera")
                .setMessage("Even faster to open camera by adding\n" +
                        " a shortcut on lock screen")
                .setTopImageResource(R.drawable.top_pic_enable_locker)
                .setPositiveButton("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(FullScreenDialogActivity.this, "Positive button clicked", Toast.LENGTH_SHORT).show();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                })
                .setFullScreen(true)
                .show();
    }
}
