package com.ihs.keyboardutilslib;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.ihs.app.framework.activity.HSActivity;
import com.ihs.keyboardutils.alerts.ExitAlert;
import com.ihs.keyboardutils.utils.CustomShareUtils;
import com.ihs.keyboardutilslib.alerts.CustomDesignAlertActivity;
import com.ihs.keyboardutilslib.configfile.ReadConfigSampleActivity;
import com.ihs.keyboardutilslib.gif.GifViewDemoActivity;

public class MainActivity extends HSActivity {
    private ExitAlert exitAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        exitAlert = new ExitAlert(MainActivity.this, getString(R.string.exit_alert_native_ad_name));
    }

    public void startAds(View view){
        startActivity(new Intent(this, com.ihs.keyboardutilslib.adactivities.MainActivity.class));
    }


    public void startGifDemoAct(View view){
        startActivity(new Intent(this, GifViewDemoActivity.class));
    }

    public void startReadConfigSample(View view) {
        startActivity(new Intent(this, ReadConfigSampleActivity.class));
    }

    public void startCustomDesignAlertDemo(View view) {
        startActivity(new Intent(this, CustomDesignAlertActivity.class));
    }

    public void showCustomShare(View view){
        Uri uri = Uri.parse("file:///storage/emulated/0/DCIM/Camera/IMG_20170412_014232854.jpg");
        CustomShareUtils.shareImage(this,uri,"Colorkey_A(NativeAds)CardAd");
    }

    public void showExitDialog(View view) {
        exitAlert.show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
