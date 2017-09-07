package com.launcher;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.feature.notification.NotificationManager;

/**
 * Created by yanxia on 2017/9/5.
 */

public class KeepAliveService extends Service {

    private static final String TAG = KeepAliveService.class.getSimpleName();

    public static final String NOTIFICATION_STOP_KEEP_ALIVE = "stop_keep_alive";

    @Override
    public void onCreate() {
        super.onCreate();
        HSLog.d(TAG, "Start keeping alive");

        INotificationObserver stopObserver = new INotificationObserver() {
            @Override
            public void onReceive(String snotification, HSBundle data) {
                HSLog.d(TAG, "Stop keeping alive");
                HSGlobalNotificationCenter.removeObserver(this);
                KeepAliveService.this.stopSelf();
            }
        };
        try {
            HSGlobalNotificationCenter.addObserver(NOTIFICATION_STOP_KEEP_ALIVE, stopObserver);

            startForeground(NotificationManager.TOOLBAR_NOTIFICATION_ID,
                    NotificationManager.getInstance().getNotificationToolbar());
        } catch (Exception e) {
            e.printStackTrace();
            try {
                HSGlobalNotificationCenter.removeObserver(stopObserver);
                stopSelf();
            } catch (Exception ignored) {
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
