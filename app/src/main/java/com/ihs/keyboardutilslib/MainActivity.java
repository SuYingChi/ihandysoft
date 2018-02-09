package com.ihs.keyboardutilslib;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.artw.lockscreen.LockerActivity;
import com.artw.lockscreen.PremiumLockerActivity;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSActivity;
import com.ihs.feature.notification.NotificationManager;
import com.ihs.keyboardutils.utils.CustomShareUtils;
import com.ihs.keyboardutilslib.adactivities.AdDemoActivity;
import com.ihs.keyboardutilslib.alerts.CustomDesignAlertActivity;
import com.ihs.keyboardutilslib.charginglocker.ChargingLockerSettingsActivity;
import com.ihs.keyboardutilslib.gif.GifViewDemoActivity;

public class MainActivity extends HSActivity {

    String[] displayNameArray = {
            "Ad Demo",
            "Gif Demo",
            "Custom Alert Demo",
            "Custom Share Demo",
            "Launch PremiumLockerActivity",
            "Launch LockerActivity",
            "Lock Screen & Charging Screen",
            "Camera Intents",
            "Country Code Parser",
            "Send Boost Notifications",
            "Boost Activities"
    };

    Class[] activityClassArray = {
            AdDemoActivity.class,
            GifViewDemoActivity.class,
            CustomDesignAlertActivity.class,
            null,
            PremiumLockerActivity.class,
            LockerActivity.class,
            ChargingLockerSettingsActivity.class,
            CameraUtilActivity.class,
            CountryCodeUtil.class,
            null,
            DemoShowBoostActivity.class
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.demo_list_row, displayNameArray);
        ListView listView = (ListView) findViewById(R.id.demo_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onItemClickListener);

        processIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        processIntent();
    }

    private void processIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            int reqCode = intent.getIntExtra("reqCode", 0);
            String from = "";
            switch (reqCode) {
                case 1:
                    from = "ScreenLocker";
                    break;
                case 2:
                    from = "Charging";
                    break;
                case 3:
                    from = "AddNewPhotoToPrivate";
                    break;
            }
            if (!TextUtils.isEmpty(from)) {
                Toast.makeText(getApplicationContext(), "Intent from=" + from, Toast.LENGTH_LONG).show();
            }

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
        if (position == 3) {
            Uri uri = Uri.parse("file:///storage/emulated/0/DCIM/Camera/IMG_20170412_014232854.jpg");
            CustomShareUtils.shareImage(this, uri, "Colorkey_A(NativeAds)CardAd", new CustomShareUtils.OnShareItemClickedListener() {
                @Override
                public void OnShareItemClicked(ActivityInfo activityInfo) {
                    Toast.makeText(HSApplication.getContext(), activityInfo.name, Toast.LENGTH_SHORT).show();
                }
            });

        } else if (position == 8) {
            NotificationManager.getInstance().checkNextNotification();
        }
    }


}
