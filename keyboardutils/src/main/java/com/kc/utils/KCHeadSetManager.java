package com.kc.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.alerts.HeadsetActivity;

/**
 * Created by yingchi.su on 2018/1/17.
 */

public class KCHeadSetManager {


    private static KCHeadSetManager kcHeadSetManager;
    private HeadsetReceiver headsetReceiver;

    private String placeMent;
    private boolean enabled = false;

    private KCHeadSetManager() {
        headsetReceiver = new HeadsetReceiver();
    }

    public static synchronized KCHeadSetManager getInstance() {
        if (kcHeadSetManager == null) {
            kcHeadSetManager = new KCHeadSetManager();
        }
        return kcHeadSetManager;
    }

    public void setHeadSetAdPlaceMent(String placeMent) {

        this.placeMent = placeMent;
    }

    public String getHeadSetAdPlaceMent() {
        return placeMent;
    }

    //get value from setting,remaining
    public boolean isEnabled() {
        return enabled;
    }

    //set value of setting,ramaining
    public void setEnabled(boolean enable) {
        if (!enabled && enable) {
            registerHeadSetReceiver();
        } else if (enabled && !enable) {
            unregisterHeadsetReceiver();
        }
        enabled = enable;
    }

    private class HeadsetReceiver extends BroadcastReceiver {

        public static final String TAG = "HeadsetReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                if (intent.getIntExtra("state", 0) == 1) {
                    HSLog.d(TAG, " onReceive  headset==========" + Intent.ACTION_HEADSET_PLUG + "      state================" + intent.getIntExtra("state", 0));
                    if (isEnabled()) {
                        Intent mIntent = new Intent(context, HeadsetActivity.class);
                        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(mIntent);
                    }
                }
            }
        }

    }

    public void registerHeadSetReceiver() {
        HSApplication.getContext().registerReceiver(headsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
    }

    public void unregisterHeadsetReceiver() {
        HSApplication.getContext().unregisterReceiver(headsetReceiver);
    }

    public void init(String string) {
        kcHeadSetManager.setHeadSetAdPlaceMent(string);
        if (enabled) {
            kcHeadSetManager.registerHeadSetReceiver();
        }

    }
}
