package com.ihs.keyboardutilslib;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ihs.app.framework.activity.HSActivity;
import com.ihs.keyboardutils.nativeads.NativeAdManager;
import com.ihs.keyboardutilslib.gif.GifViewDemoActivity;
import com.ihs.keyboardutilslib.panelcontainer.CommonTabActivity;

public class MainActivity extends HSActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startPanelContainer(View view) {
        startActivity(new Intent(this, CommonTabActivity.class));
    }

    public void startAds(View view){
        startActivity(new Intent(this, com.ihs.keyboardutilslib.adactivities.MainActivity.class));
    }


    public void startGifDemoAct(View view){
        startActivity(new Intent(this, GifViewDemoActivity.class));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
