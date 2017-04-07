package com.ihs.keyboardutilslib;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ihs.app.framework.activity.HSActivity;
import com.ihs.keyboardutilslib.alerts.CustomDesignAlertActivity;
import com.ihs.keyboardutilslib.configfile.ReadConfigSampleActivity;
import com.ihs.keyboardutilslib.gif.GifViewDemoActivity;

public class MainActivity extends HSActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
