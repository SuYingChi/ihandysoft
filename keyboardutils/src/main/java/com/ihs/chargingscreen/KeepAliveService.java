package com.ihs.chargingscreen;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.charging.HSChargingManager;
import com.ihs.chargingscreen.utils.ChargingPrefsUtil;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;

/**
 * Created by zhixiangxiao on 6/13/16.
 */
public class KeepAliveService extends Service {

    private BroadcastReceiver broadcastReceiver;
    public static final String ACTION_START_CHARGING_ACTIVITY = "com.artw.charging.ac";


    public static boolean isServiceRunning = false;


    private INotificationObserver notificationObserver = new INotificationObserver() {

        @Override
        public void onReceive(String notificationName, HSBundle bundle) {
            if (HSNotificationConstant.HS_CONFIG_CHANGED.equals(notificationName)) {
                if (ChargingPrefsUtil.getInstance().getChargingEnableStates() == ChargingPrefsUtil.CHARGING_MUTED) {
                    HSChargingManager.getInstance().stop();
                } else {
                    HSChargingManager.getInstance().start();
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter receiverFilter = new IntentFilter();
        receiverFilter.addAction(ACTION_START_CHARGING_ACTIVITY);
        receiverFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
        receiverFilter.addAction("android.net.wifi.supplicant.CONNECTION_CHANGE");
        receiverFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        broadcastReceiver = new ChargingBroadcastReceiver();
        getApplicationContext().registerReceiver(broadcastReceiver, receiverFilter);

        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_CONFIG_CHANGED, notificationObserver);

    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isServiceRunning = true;
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        isServiceRunning = false;
        getApplicationContext().unregisterReceiver(broadcastReceiver);
        super.onDestroy();
        startService(new Intent(getApplicationContext(), KeepAliveService.class));
    }

}
