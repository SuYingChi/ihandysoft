package com.ihs.keyboardutils.notification;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;

import com.artw.lockscreen.LockerSettings;
import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.ChargingPrefsUtil;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.KCAnalyticUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static com.ihs.app.framework.HSApplication.getContext;

/**
 * Created by Arthur on 17/4/29.
 */

public class KCNotificationManager {
    private static final String PREFS_FILE_NAME = "notification_prefs";
    private static final String PREFS_NEXT_EVENT_TIME = "prefs_next_event_time";
    public static final String PREFS_NOTIFICATION_ENABLE = HSApplication.getContext().getString(R.string.prefs_notification_enable);

    private static long intervalDuration = AlarmManager.INTERVAL_DAY;
    //方法延迟或者计算误差
    private static final int METHOD_EXCUTION_ERROR_TIME = 10;
    private static final int HANDLER_MSG_SUCCESFULL = 10;
    private static final int NOTIFICATION_ID = Math.abs(HSApplication.getContext().getPackageName().hashCode() / 100000);


    private static KCNotificationManager instance;
    private final String ACTION_CHARGING = "Charging";
    private final String ACTION_LOCKER = "Locker";

    private Context context;
    private HSPreferenceHelper spHelper;
    private NotificationBean nextNotification;
    private BroadcastReceiver eventReceiver;

    public synchronized static KCNotificationManager getInstance() {
        if (instance == null) {
            instance = new KCNotificationManager();
        }
        return instance;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HANDLER_MSG_SUCCESFULL) {
                //从本地去出保存的 maxShowCount和lastShow。格式：("beanEventName","eventShowTimes,lastShow")
                String[] eventRecord = spHelper.getString(nextNotification.getSPKey(), "0,0").split(",");
                int eventShowTimes = Integer.valueOf(eventRecord[0]);
                eventShowTimes++;
                spHelper.putString(nextNotification.getSPKey(), String.format(Locale.ENGLISH, "%d,%d", eventShowTimes, System.currentTimeMillis()));
            }
        }
    };

    private KCNotificationManager() {
        //初始化notification 设置项
        if (!HSPreferenceHelper.getDefault().contains(PREFS_NOTIFICATION_ENABLE)) {
            HSPreferenceHelper.getDefault().putBoolean(PREFS_NOTIFICATION_ENABLE, true);
        }
        context = getContext();
        spHelper = HSPreferenceHelper.create(getContext(), PREFS_FILE_NAME);

//        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_CONFIG_CHANGED, new INotificationObserver() {
//            @Override
//            public void onReceive(String s, HSBundle hsBundle) {
//                if (s.equals(HSNotificationConstant.HS_CONFIG_CHANGED)) {
//                    refreshConfig();
//                }
//            }
//        });
//        refreshConfig();
        scheduleNextEvent();
    }

    private void scheduleNextEvent() {
        List<Map<String, ?>> configs = null;
        try {
            configs = (List<Map<String, ?>>) HSConfig.getList("Application", "LocalNotifications");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (configs == null) {
            HSLog.e("没有配置本地提醒");
            return;
        }

        HashMap<Integer, Long> avaliableTime = new HashMap<>();
        ArrayList<NotificationBean> avaliableList = new ArrayList<>();
        long nextEventTime = Long.MAX_VALUE;
        for (int i = 0; i < configs.size(); i++) {
            NotificationBean bean = null;
            try {
                Map<String, Object> value = (Map<String, Object>) configs.get(i);
                bean = new NotificationBean(value);
//                notificationMap.put(i,configs.get(i));
            } catch (Exception e) {
                HSLog.e("wrong config for ==> " + configs.get(i));
            }

            if (bean == null) {
                continue;
            }

            //check charging and locker state
            if(bean.getActionType().equals(ACTION_CHARGING)){
                if(isChargingEnabled()){
                    continue;
                }
            }
            if(bean.getActionType().equals(ACTION_LOCKER)){
                if(isLockerEnabled()){
                    continue;
                }
            }


            //从本地去出保存的 maxShowCount和lastShow。格式：("beanEventName","eventShowTimes,lastShow")
            String[] eventRecord = spHelper.getString(bean.getSPKey(), "0,0").split(",");
            int eventShowTimes = Integer.valueOf(eventRecord[0]);
            long lastShow = Long.valueOf(eventRecord[1]);

            //如果达到可以出现的条件
            if ((bean.getMaxShowCount() == 0 ||
                    eventShowTimes < bean.getMaxShowCount())) {
                long nextTime = lastShow + intervalDuration * bean.getInterval();
                if (nextTime < nextEventTime) {
                    avaliableList.clear();
                    nextEventTime = nextTime;
                    avaliableList.add(bean);
                } else if (nextTime == nextEventTime) {
                    avaliableList.add(bean);
                }
            }
        }

        if (avaliableList.size() > 0) {
            nextNotification = avaliableList.get(0);
            setNextNotification(nextEventTime);
        }
    }



    public void sendNotification(NotificationBean notificationBean) {
        if (!HSPreferenceHelper.getDefault().getBoolean(PREFS_NOTIFICATION_ENABLE, true)) {
            return;
        }

        if (ACTION_LOCKER.equals(notificationBean.getActionType()) && isLockerEnabled()) {
            scheduleNextEvent();
            return;
        }
        if (ACTION_CHARGING.equals(notificationBean.getActionType()) && isChargingEnabled()) {
            scheduleNextEvent();
            return;
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(notificationBean.getTitle())
                .setContentText(notificationBean.getMessage())
                .setDefaults(Notification.DEFAULT_ALL);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(notificationBean.getMessage());
        bigText.setBigContentTitle(notificationBean.getTitle());
        mBuilder.setStyle(bigText);
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);

        Intent intent = new Intent(context, eventReceiver.getClass());
        intent.setAction(Long.toString(System.currentTimeMillis()));
        intent.putExtra("actionType",  notificationBean.getActionType());
        intent.putExtra("name",  notificationBean.getName());
        PendingIntent resultPendingIntent;

        resultPendingIntent = PendingIntent.getBroadcast(
                getContext(),
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT
        );
        mBuilder.setContentIntent(resultPendingIntent);


        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        try {
            //用event名 加 packageName的hashcode来确保，每个程序有自己的一套通知系统，并且，每种通知事件不重复。
            manager.notify(notificationBean.getActionType(), NOTIFICATION_ID, mBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        KCAnalyticUtil.logEvent("notification_show", notificationBean.getActionType());
    }

    private boolean isChargingEnabled() {
        return ChargingPrefsUtil.getChargingEnableStates() == ChargingPrefsUtil.CHARGING_DEFAULT_ACTIVE;
    }
    private boolean isLockerEnabled() {
        return LockerSettings.getLockerEnableStates() == LockerSettings.LOCKER_DEFAULT_ACTIVE;
    }


    public void setNotificationReceiver(BroadcastReceiver eventReceiver) {
        this.eventReceiver = eventReceiver;
    }

    private void setNextNotification(long time) {
        Intent intent = new Intent(context, NotificationExecutionReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        try {
            alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setIntervalDuration(long intervalDuration) {
        KCNotificationManager.intervalDuration = intervalDuration;
    }
}
