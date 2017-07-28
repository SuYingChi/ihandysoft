package com.ihs.feature.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ihs.commons.utils.HSLog;
import com.ihs.feature.common.FormatSizeBuilder;
import com.ihs.feature.junkclean.JunkCleanActivity;
import com.ihs.feature.junkclean.data.JunkManager;

/**
 * Launched from notification with {@link android.app.PendingIntent#getBroadcast(Context, int, Intent, int)} pending
 * intent.
 */
public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (NotificationManager.ACTION_JUNK_CLEAN_BADGE.equals(action)) {
            HSLog.d(NotificationManager.TAG, "onReceive sIsJunkCleanRunning = " + JunkCleanActivity.sIsJunkCleanRunning);
            if (!JunkCleanActivity.sIsJunkCleanRunning) {
                JunkManager.getInstance().startJunkScan(new JunkManager.ScanJunkListenerAdapter() {
                    @Override
                    public void onScanFinished(long junkSize) {
                        super.onScanFinished(junkSize);
                        HSLog.d(NotificationManager.TAG, "onReceive junkSize = " + junkSize);
                        if (junkSize > 1024) {
                            FormatSizeBuilder formatSizeBuilder = new FormatSizeBuilder(junkSize, true);
//                            BadgeInfo badgeInfo = BadgeInfo.create(null, formatSizeBuilder.size + "+" + formatSizeBuilder.unit);
//                            badgeInfo.setClearOnClick(true);
//                            badgeInfo.setShowInTopLevel(false);
//                            BadgeProcessor.notifyFeatureChanged(CustomFeatureInfo.FEATURE_TYPE_JUNK_CLEANER, badgeInfo);
                        }
                    }
                }, false);
            }
        } else {
            if (intent.getBooleanExtra(NotificationManager.EXTRA_AUTO_COLLAPSE, false)) {
                NotificationManager.getInstance().handleEventAndCollapse(context.getApplicationContext(), intent);
            } else {
                NotificationManager.getInstance().handleEvent(context.getApplicationContext(), intent);
            }
        }
    }
}
