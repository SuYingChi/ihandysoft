package com.ihs.chargingscreen;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * Created by zhixiangxiao on 6/13/16.
 */
public class KeepAliveService extends Service {

    private BroadcastReceiver broadcastReceiver;
    public static final String ACTION_START_CHARGING_ACTIVITY = "com.artw.charging.ac";


    public static boolean isServiceRunning = false;


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
