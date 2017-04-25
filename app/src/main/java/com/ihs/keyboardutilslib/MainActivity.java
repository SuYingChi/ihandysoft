package com.ihs.keyboardutilslib;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSActivity;
import com.ihs.keyboardutils.alerts.ExitAlert;
import com.ihs.keyboardutils.utils.CustomShareUtils;
import com.ihs.keyboardutilslib.adactivities.NativeAdDemoActivity;
import com.ihs.keyboardutilslib.alerts.CustomDesignAlertActivity;
import com.ihs.keyboardutilslib.charginglocker.ChargingLockerSettingsActivity;
import com.ihs.keyboardutilslib.configfile.ReadConfigSampleActivity;
import com.ihs.keyboardutilslib.gif.GifViewDemoActivity;

public class MainActivity extends HSActivity {
    private ExitAlert exitAlert;

    String[] displayNameArray = {
            "NativeAdView Demo",
            "Gif Demo",
            "KCConfig Demo",
            "Custom Alert Demo",
            "Custom Share Demo",
            "Exit Alert Demo",
            "Charging Locker Settings"
    };

    Class[] activityClassArray = {
            NativeAdDemoActivity.class,
            GifViewDemoActivity.class,
            ReadConfigSampleActivity.class,
            CustomDesignAlertActivity.class,
            null,
            null,
            ChargingLockerSettingsActivity.class
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,R.layout.demo_list_row, displayNameArray);
        ListView listView = (ListView)findViewById(R.id.demo_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onItemClickListener);

        exitAlert = new ExitAlert(MainActivity.this, getString(R.string.exit_alert_native_ad_name));
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (activityClassArray[position] != null) {
                Class clazz = activityClassArray[position];
                startActivity(new Intent(MainActivity.this, clazz));
            } else {
                handleItemClick(position);
            }
        }
    };

    // TOOD: should use better way than hard-coded numbers
    private void handleItemClick(int position) {
        if (position == 4) {
            Uri uri = Uri.parse("file:///storage/emulated/0/DCIM/Camera/IMG_20170412_014232854.jpg");
            CustomShareUtils.shareImage(this, uri, "Colorkey_A(NativeAds)CardAd", new CustomShareUtils.OnShareItemClickedListener() {
                @Override
                public void OnShareItemClicked(ActivityInfo activityInfo) {
                    Toast.makeText(HSApplication.getContext(), activityInfo.name, Toast.LENGTH_SHORT).show();
                }
            });
        } else if (position == 5) {
            exitAlert.show();
        }
    }
}
