package com.ihs.chargingscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ihs.chargingscreen.utils.ChargingAnalytics;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;

/**
 * Created by Arthur on 17/3/1.
 */

public class ChargingBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_START_CHARGING_ACTIVITY = "com.artw.charging.ac";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_START_CHARGING_ACTIVITY)) {
            ChargingManagerUtil.enableCharging(true);
            ChargingAnalytics.getInstance().chargingEnableNotificationClicked();
        }
    }
}
