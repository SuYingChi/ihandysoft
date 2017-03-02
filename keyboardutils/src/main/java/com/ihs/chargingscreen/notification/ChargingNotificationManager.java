package com.ihs.chargingscreen.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.ihs.app.framework.HSApplication;
import com.ihs.charging.HSChargingManager;
import com.ihs.chargingscreen.ChargingBroadcastReceiver;
import com.ihs.chargingscreen.Constants;
import com.ihs.chargingscreen.HSChargingScreenManager;
import com.ihs.chargingscreen.KeepAliveService;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.R;

/**
 * Created by zhixiangxiao on 5/17/16.
 */
public class ChargingNotificationManager {

    private static final int NOTIFICATION_ID = 19349; // TODO uniform all notification id

    private static final int[] BATTERY_IMAGE_RES_IDS = {
            R.id.img_battery1, R.id.img_battery2, R.id.img_battery3,
            R.id.img_battery4, R.id.img_battery5};

    private Notification notification;
    private RemoteViews remoteViews;

    private NotificationManager notificationManager;

    private Handler handler = new Handler();

    private String leftTimeString = "abb";

    private INotificationObserver notificationObserver = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            HSLog.e("notifaction update received");
            ChargingNotificationManager.this.update();
        }
    };

    public ChargingNotificationManager() {

        notificationManager = (NotificationManager) HSApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);


        Intent intent = new Intent(HSApplication.getContext(), ChargingBroadcastReceiver.class);
        intent.setAction(KeepAliveService.ACTION_START_CHARGING_ACTIVITY);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(HSApplication.getContext(), 1000, intent, 0);

        remoteViews = new RemoteViews(HSApplication.getContext().getPackageName(), R.layout.charging_module_notification_charging);
        remoteViews.setOnClickPendingIntent(R.id.root_view, pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.tv_enable, pendingIntent);


        int priority = VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN ? Notification.PRIORITY_MAX : Notification.FLAG_AUTO_CANCEL;

        notification = new NotificationCompat.Builder(HSApplication.getContext()).setOngoing(false).setSmallIcon(R.mipmap.charging_module_notify_charging_small_icon)
                .setPriority(priority).setContent(remoteViews).setContentIntent(pendingIntent).setDefaults(Notification.DEFAULT_ALL).setWhen(0).setAutoCancel(true).build();
        HSGlobalNotificationCenter.addObserver(Constants.EVENT_SYSTEM_BATTERY_CHARGING_STATE_CHANGED, notificationObserver);
    }

    public void cancel() {
        notificationManager.cancel(NOTIFICATION_ID);
    }

    public void update() {
        if (HSChargingScreenManager.getInstance().isChargingModuleOpened()) {
            return;
        }
        String remoteViewsTitle = getRemoteViewsTitle();
        if (!TextUtils.isEmpty(remoteViewsTitle)) {
            remoteViews.setTextViewText(R.id.txt_title, remoteViewsTitle);
            remoteViews.setTextViewText(R.id.txt_left_time_indicator, HSApplication.getContext().getResources().getString(R.string.enable_charging_detail));
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    public static String getRemoteViewsTitle() {
        String title;

        Context context = HSApplication.getContext();
        switch (HSChargingManager.getInstance().getChargingState()) {
            case STATE_DISCHARGING:

                title = context.getResources().getString(R.string.charging_module_charging_state_unknown);
                break;
            case STATE_CHARGING_SPEED:
            case STATE_CHARGING_CONTINUOUS:
            case STATE_CHARGING_TRICKLE:

                title = context.getResources().getString(R.string.charger_connected_title_disabled);
                break;
            case STATE_CHARGING_FULL:

                title = context.getResources().getString(R.string.charging_module_charging_state_finish);
                break;
            default:
                title = "";
                break;
        }
        return title;
    }
}
