package com.ihs.feature.schedule;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.feature.common.LauncherConstants;
import com.ihs.feature.common.PreferenceHelper;
import com.ihs.feature.common.Utils;
import com.ihs.feature.notification.NotificationCondition;
import com.ihs.feature.notification.NotificationManager;
import com.ihs.feature.notification.NotificationReceiver;
import com.ihs.keyboardutils.BuildConfig;
import com.ihs.keyboardutils.R;
import com.kc.utils.KCAnalytics;


/**
 * Receiving a repeated alarm and check if we need to send a notification to set us as default launcher.
 */

public class ScheduledNotificationReceiver extends BroadcastReceiver {

    public static final String PREF_KEY_SEND_NOTIFICATION_TIMES = "PREF_KEY_SEND_NOTIFICATION_TIMES";
    public static final String PREF_KEY_LAST_USED_DESCRIPTION_INDEX = "PREF_KEY_LAST_USED_DESCRIPTION_INDEX";
    private int mSendNotificationTimes = BuildConfig.FLAVOR.equals(LauncherConstants.BUILD_VARIANT_SP) ? 10 : 3;

    private static final int[] NOTIFICATION_DESCRIPTIONS = {
            R.string.notification_set_as_default_description_1,
            R.string.notification_set_as_default_description_2,
            R.string.notification_set_as_default_description_3
    };

    public static String getFlurryNotificationType() {
        int index = HSPreferenceHelper.getDefault().getInt(ScheduledNotificationReceiver.PREF_KEY_LAST_USED_DESCRIPTION_INDEX, -1);
        switch (index) {
            case 0:
                return "Boost";
            case 1:
                return "Personalized";
            case 2:
                return "Efficient";
            default:
                return "";
        }
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (Utils.isDefaultLauncher()) {
            return;
        }
        if (!NotificationManager.getInstance().hasDefaultClearedNotification()) {
            PreferenceHelper.getDefault().doLimitedTimes(new Runnable() {
                @Override
                public void run() {
                    showNotification(context);
                }
            }, PREF_KEY_SEND_NOTIFICATION_TIMES, mSendNotificationTimes);
        }
    }

    public static void showNotification(Context context) {
        if (Build.VERSION.SDK_INT == 19) {
            return;
        }
        int notificationId = NotificationCondition.NOTIFICATION_ID_SET_AS_DEFAULT;
        int smallIconId = R.drawable.notification_home_small_icon;
        int lastUsedIndex = HSPreferenceHelper.getDefault().getInt(PREF_KEY_LAST_USED_DESCRIPTION_INDEX, -1);
        int currentIndex = (lastUsedIndex + 1) % NOTIFICATION_DESCRIPTIONS.length;
        String description = context.getString(NOTIFICATION_DESCRIPTIONS[currentIndex]);
        HSPreferenceHelper.getDefault().putInt(PREF_KEY_LAST_USED_DESCRIPTION_INDEX, currentIndex);

        RemoteViews notification = new RemoteViews(LauncherConstants.LAUNCHER_PACKAGE_NAME, R.layout.notification_set_as_default);
        notification.setTextViewText(R.id.notification_title, context.getString(R.string.notification_set_as_default_title));
        notification.setTextViewText(R.id.notification_description, description);
        notification.setTextViewText(R.id.notification_btn_text, context.getString(R.string.notification_set_as_default_btn));
        notification.setImageViewResource(R.id.notification_icon, R.drawable.notification_home);

        PendingIntent pendingIntent = NotificationManager.getInstance().getPendingIntent(
                NotificationManager.ACTION_SET_AS_DEFAULT, true,
                new NotificationManager.ExtraProvider() {
                    @Override
                    public void onAddExtras(Intent intent) {
                        intent.putExtra(NotificationManager.EXTRA_SET_AS_DEFAULT_TYPE, NotificationManager.EXTRA_VALUE_SET_AS_DEFAULT_SET);
                    }
                });

        Intent deleteIntent = new Intent(context, NotificationReceiver.class);
        deleteIntent.setAction(NotificationManager.ACTION_SET_AS_HOME_DELETE);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context,0,deleteIntent,0);

        android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(smallIconId)
                .setContent(notification)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(deletePendingIntent)
                .setAutoCancel(true);

        boolean isHeadsUp = HSConfig.optBoolean(false, "Application", "Notification", "HeadsUp", "SetDefault");
        if (isHeadsUp) {
            builder.setDefaults(NotificationCompat.DEFAULT_SOUND
                    | NotificationCompat.DEFAULT_VIBRATE
                    | NotificationCompat.DEFAULT_LIGHTS);

            // 测试中存在高版本出现 crash, notified from MAX team
            try {
                builder.setPriority(NotificationCompat.PRIORITY_MAX);
            } catch (Exception e) {
                HSLog.i("builder.setPriority(NotificationCompat.PRIORITY_MAX) EXCEPTION");
            }
        } else {
            builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        }

        String flurryNotificationType = getFlurryNotificationType();
        KCAnalytics.logEvent("Notification_Pushed", "Type", NotificationManager.SET_AS_HOME, "notifyType", flurryNotificationType);
        KCAnalytics.logEvent("Notification_SetAsHome_Pushed", "notifyType", flurryNotificationType); // TODO: temporary event log for v1.4.2, remove later

        // Use icon resource ID as notification ID
        NotificationManager.getInstance().notify(notificationId, builder.build());
    }
}
