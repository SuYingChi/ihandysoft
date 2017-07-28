package com.ihs.chargingscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ihs.chargingscreen.utils.ChargingAnalytics;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.commons.utils.HSPreferenceHelper;

import static com.ihs.chargingscreen.AgentService.ACTION_START_CHARGING_ACTIVITY;
import static com.ihs.chargingscreen.AgentService.isServiceRunning;

/**
 * Created by Arthur on 17/3/1.
 */

public class ChargingBroadcastReceiver extends BroadcastReceiver {
    private static final String LAST_RESTART_SERVICE = "last_restart_service";
    private static final int CHECK_INTERVAL = 1000 * 60 * 10;

    @Override
    public void onReceive(Context context, Intent intent) {
        HSPreferenceHelper sp = HSPreferenceHelper.getDefault();
        long lastTime = System.currentTimeMillis() - sp.getLong(LAST_RESTART_SERVICE, 0);

        if (intent.getAction().equals(ACTION_START_CHARGING_ACTIVITY)) {
            ChargingManagerUtil.enableCharging(true);
            ChargingAnalytics.getInstance().chargingEnableNotificationClicked();
        } else if (!isServiceRunning) {
            if (lastTime > CHECK_INTERVAL) {
                context.startService(new Intent(context, AgentService.class));
                sp.putLong(LAST_RESTART_SERVICE, System.currentTimeMillis());
            }
        }
    }
}
