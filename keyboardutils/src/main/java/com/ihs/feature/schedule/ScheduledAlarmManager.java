package com.ihs.feature.schedule;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.utils.CommonUtils;

public class ScheduledAlarmManager {

    private static final String TAG = ScheduledAlarmManager.class.getSimpleName();

    public static final int REQUEST_SET_AS_DEFAULT_NOTIFICATION = 0;
    private static final int REQUEST_TRENDING_WORDS_UPDATE = 10;

    private static final long SET_AS_DEFAULT_NOTIFICATION_FIRST_INTERVAL = 60 * 60 * 1000;

    private static final long TRENDING_WORDS_START_TIME = 1000;
    private static final long TRENDING_WORDS_REPEAT_INTERVAL = 60 * 60 * 1000;

    private static AlarmManager sAlarmManager;

    private static AlarmManager getAlarmManager(Context context) {
        if (sAlarmManager == null) {
            sAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }
        return sAlarmManager;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void scheduleSetAsDefaultNotification(Context context, int requestCode) {
        Intent intent = new Intent(context, ScheduledNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            switch (requestCode) {
                case REQUEST_SET_AS_DEFAULT_NOTIFICATION:
                    if (CommonUtils.ATLEAST_KITKAT) {
                        getAlarmManager(context).setExact(AlarmManager.RTC,
                                System.currentTimeMillis() + SET_AS_DEFAULT_NOTIFICATION_FIRST_INTERVAL, pendingIntent);
                    } else {
                        getAlarmManager(context).set(AlarmManager.RTC,
                                System.currentTimeMillis() + SET_AS_DEFAULT_NOTIFICATION_FIRST_INTERVAL, pendingIntent);
                    }
                    break;
                default:
                    break;
            }
        } catch (SecurityException e) {
            HSLog.w(TAG, "Failed to schedule set-as-default notification due to SecurityException");
            e.printStackTrace();
        }
    }

    public static void cancelNotificationSchedule(Context context, int requestCode) {
        Intent intent = new Intent(context, ScheduledNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        getAlarmManager(context).cancel(pendingIntent);
    }

//    public static void scheduleTrendingWordsUpdate(Context context) {
//        Intent intent = new Intent(context, TrendingWordsUpdateReceiver.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_TRENDING_WORDS_UPDATE, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//        getAlarmManager(context).setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis() + TRENDING_WORDS_START_TIME,
//                TRENDING_WORDS_REPEAT_INTERVAL, pendingIntent);
//    }
//
//    public static void cancelTrendingWordsUpdateSchedule(Context context) {
//        Intent intent = new Intent(context, TrendingWordsUpdateReceiver.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_TRENDING_WORDS_UPDATE, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//        getAlarmManager(context).cancel(pendingIntent);
//    }
}
