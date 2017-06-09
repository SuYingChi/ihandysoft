package com.ihs.chargingscreen;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.charging.HSChargingManager;
import com.ihs.chargingscreen.utils.ChargingPrefsUtil;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;

/**
 * Created by zhixiangxiao on 6/13/16.
 */
public class AgentService extends Service {

    private BroadcastReceiver broadcastReceiver;
    public static final String ACTION_START_CHARGING_ACTIVITY = "com.artw.charging.ac";
    private final static int GRAY_SERVICE_ID = 58889;


    public static boolean isServiceRunning = false;


    private INotificationObserver notificationObserver = new INotificationObserver() {

        @Override
        public void onReceive(String notificationName, HSBundle bundle) {
            if (HSNotificationConstant.HS_CONFIG_CHANGED.equals(notificationName)) {
                if (ChargingPrefsUtil.getChargingEnableStates() == ChargingPrefsUtil.CHARGING_MUTED) {
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


        try {
            startForeground(GRAY_SERVICE_ID, getNewNotification());
        }catch (Exception e){
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 18) {
            Intent innerIntent = new Intent(this, GrayInnerService.class);
            startService(innerIntent);
        }
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
        startService(new Intent(getApplicationContext(), AgentService.class));
    }

    private static Notification getNewNotification() {

        Notification notification = new Notification();
        Context context = HSApplication.getContext();

        Intent notificationIntent = new Intent(context, AgentService.class);
        notification.contentIntent = PendingIntent.getActivity(context, 101, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        return notification;
    }

    /**
     * 给 API >= 18 的平台上用的灰色保活手段
     */
    @TargetApi(18)
    public static class GrayInnerService extends Service {

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {


            try {
                startForeground(GRAY_SERVICE_ID, getNewNotification());
                stopForeground(true);
                stopSelf();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return START_NOT_STICKY;
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

    }

}
