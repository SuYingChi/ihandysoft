package com.ihs.feature.junkclean.util;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ihs.commons.utils.HSLog;
import com.ihs.feature.common.LauncherFiles;
import com.ihs.feature.common.PreferenceHelper;
import com.ihs.feature.common.ViewUtils;
import com.ihs.feature.junkclean.JunkCleanActivity;
import com.ihs.feature.junkclean.JunkCleanWindowController;
import com.ihs.feature.resultpage.data.ResultConstants;
import com.ihs.keyboardutils.R;
import com.kc.utils.KCAnalytics;

public class JunkCleanUtils {

    private static final String PREF_KEY_JUNK_CLEAN_LAST_OPEN_TIME = "PREF_KEY_JUNK_CLEAN_LAST_OPEN_TIME";
    private static final String PREF_HAS_POWERFUL_CLEAN_ALERTED = "PREF_HAS_POWERFUL_CLEAN_ALERTED";
    private static final String PREF_HAS_SECURITY_ALERTED = "PREF_HAS_SECURITY_ALERTED";
    private static final String PREF_COUNT_CLEAN_CLICK = "PREF_COUNT_CLEAN_CLICK";
    private static final String PREF_COUNT_SECURITY_BANNER_SHOW = "PREF_COUNT_SECURITY_BANNER_SHOW";
    private static final String PREF_DIALOG_SHOW_CLEAN_NOT_CLICK = "PREF_DIALOG_SHOW_CLEAN_NOT_CLICK";
    private static final String PREF_SCAN_CANCELED = "PREF_SCAN_CANCELED";
    private static final String PREF_KEY_LAST_SCAN_FINISH_TIME = "PREF_KEY_LAST_SCAN_FINISH_TIME";

    public static final int INVALID_COUNT_LIMIT_CLEAN_CLICK = -1;
    public static final int COUNT_LIMIT_SECURITY_BANNER_SHOW = 3;
    public static final int COUNT_LIMIT_CLEAN_CLICK = 6;

    public static boolean hasShowCleanNotClick() {
        return JunkPreferenceHelper.getBoolean(PREF_DIALOG_SHOW_CLEAN_NOT_CLICK, false);
    }

    public static void setHasShowCleanNotClick(boolean hasShowCLeanNotClick) {
        JunkPreferenceHelper.putBoolean(PREF_DIALOG_SHOW_CLEAN_NOT_CLICK, hasShowCLeanNotClick);
    }

    public static boolean isScanCanceled() {
        return JunkPreferenceHelper.getBoolean(PREF_SCAN_CANCELED, false);
    }

    public static void setIsScanCanceled(boolean isScanCanceled) {
        JunkPreferenceHelper.putBoolean(PREF_SCAN_CANCELED, isScanCanceled);
    }

    public static boolean hasPowerFulCleanAlerted() {
        return JunkPreferenceHelper.getBoolean(PREF_HAS_POWERFUL_CLEAN_ALERTED, false);
    }

    public static void setHasPowerFulCleanAlerted(boolean hasCleaned) {
        JunkPreferenceHelper.putBoolean(PREF_HAS_POWERFUL_CLEAN_ALERTED, hasCleaned);
    }

    public static void setHasSecurityAlerted(boolean hasRecommended) {
        JunkPreferenceHelper.putBoolean(PREF_HAS_SECURITY_ALERTED, hasRecommended);
    }

    public static boolean hasSecurityAlerted() {
        return JunkPreferenceHelper.getBoolean(PREF_HAS_SECURITY_ALERTED, false);
    }

    public static void setCleanClickCount(boolean isReset) {
        boolean hasSecurityAlerted = hasSecurityAlerted();
        boolean hasPowerFulCleanAlerted = hasPowerFulCleanAlerted();
        HSLog.d(JunkCleanActivity.TAG, "setCleanClickCount isReset = " + isReset + " hasSecurityAlerted = " + hasSecurityAlerted + " hasPowerFulCleanAlerted = " + hasPowerFulCleanAlerted);
        if ((hasSecurityAlerted && !hasPowerFulCleanAlerted) || (!hasSecurityAlerted && hasPowerFulCleanAlerted)) {
            int oldCount = 0;
            if (!isReset) {
                oldCount = JunkPreferenceHelper.getInt(PREF_COUNT_CLEAN_CLICK, 0);
            }
            HSLog.d(JunkCleanActivity.TAG, "setCleanClickCount oldCount = " + oldCount);
            if (oldCount >= COUNT_LIMIT_CLEAN_CLICK) {
                JunkPreferenceHelper.putInt(PREF_COUNT_CLEAN_CLICK, INVALID_COUNT_LIMIT_CLEAN_CLICK);
            } else {
                JunkPreferenceHelper.putInt(PREF_COUNT_CLEAN_CLICK, JunkPreferenceHelper.getInt(PREF_COUNT_CLEAN_CLICK, 0) + 1);
            }
        }
    }

    private static int getCleanClickCount() {
        return JunkPreferenceHelper.getInt(PREF_COUNT_CLEAN_CLICK, 0);
    }

    public static boolean isCleanClickCountLimit() {
        int cleanClickCount = getCleanClickCount();
        HSLog.d(JunkCleanActivity.TAG, "isCleanClickCountLimit cleanClickCount = " + cleanClickCount);
        return !(0 == cleanClickCount || INVALID_COUNT_LIMIT_CLEAN_CLICK == cleanClickCount) && getCleanClickCount() <= COUNT_LIMIT_CLEAN_CLICK;
    }

    public static void setSecurityBannerShowCount() {
        int oldCount = JunkPreferenceHelper.getInt(PREF_COUNT_SECURITY_BANNER_SHOW, 0);
        HSLog.d(JunkCleanActivity.TAG, "setSecurityBannerShowCount oldCount = " + oldCount);
        if (oldCount > COUNT_LIMIT_SECURITY_BANNER_SHOW) {
            return;
        }
        JunkPreferenceHelper.putInt(PREF_COUNT_SECURITY_BANNER_SHOW, oldCount + 1);
    }

    private static int getSecurityBannerShowCount() {
        return JunkPreferenceHelper.getInt(PREF_COUNT_SECURITY_BANNER_SHOW, 0);
    }

    public static boolean isSecurityBannerShowCountLimit() {
        int securityBannerShowCount = getSecurityBannerShowCount();
        HSLog.d(JunkCleanActivity.TAG, "isSecurityBannerShowCountLimit securityBannerShowCount = " + securityBannerShowCount);
        return securityBannerShowCount >= COUNT_LIMIT_SECURITY_BANNER_SHOW;
    }

    public static void setLastOpenJunkCleanTime() {
        PreferenceHelper.get(LauncherFiles.JUNK_CLEAN_PREFS).putLong(PREF_KEY_JUNK_CLEAN_LAST_OPEN_TIME, System.currentTimeMillis());
    }

    public static long getLastOpenJunkCleanTime() {
        return PreferenceHelper.get(LauncherFiles.JUNK_CLEAN_PREFS).getLong(PREF_KEY_JUNK_CLEAN_LAST_OPEN_TIME, 0);
    }

    public static void setLastJunkScanFinishTime() {
        PreferenceHelper.get(LauncherFiles.JUNK_CLEAN_PREFS).putLong(PREF_KEY_LAST_SCAN_FINISH_TIME, System.currentTimeMillis());
    }

    public static long getLastJunkScanFinishTime() {
        return PreferenceHelper.get(LauncherFiles.JUNK_CLEAN_PREFS).getLong(PREF_KEY_LAST_SCAN_FINISH_TIME, 0);
    }

    public static boolean shouldForceCleanSystemAppCache() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static View initStopDialog(final Activity activity) {
        final View stopDialogV = ViewUtils.findViewById(activity, R.id.stop_dialog_view);
        // Stop Dialog title content
        TextView stopDialogTitleTv = ViewUtils.findViewById(activity, R.id.custom_alert_title);
        TextView stopDialogBodyTv = ViewUtils.findViewById(activity, R.id.custom_alert_body);
        // Stop Dialog button
        Button stopDialogCancelBtn = ViewUtils.findViewById(activity, R.id.custom_alert_cancel_btn);
        Button stopDialogOkBtn = ViewUtils.findViewById(activity, R.id.custom_alert_ok_btn);

        stopDialogTitleTv.setText(activity.getString(R.string.clean_stop_title));
        stopDialogBodyTv.setText(activity.getString(R.string.clean_stop_content));
        stopDialogCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissStopDialog(stopDialogV);
            }
        });

        stopDialogOkBtn.setText(activity.getString(R.string.boost_plus_stop_sure));
        stopDialogOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissStopDialog(stopDialogV);
                activity.finish();
            }
        });
        return stopDialogV;
    }

    public static View initStopDialog(final JunkCleanWindowController junkCleanWindowController, final View contentView) {
        final View stopDialogV = ViewUtils.findViewById(contentView, R.id.stop_dialog_view);
        // Stop Dialog title content
        TextView stopDialogTitleTv = ViewUtils.findViewById(contentView, R.id.custom_alert_title);
        TextView stopDialogBodyTv = ViewUtils.findViewById(contentView, R.id.custom_alert_body);
        // Stop Dialog button
        Button stopDialogCancelBtn = ViewUtils.findViewById(contentView, R.id.custom_alert_cancel_btn);
        Button stopDialogOkBtn = ViewUtils.findViewById(contentView, R.id.custom_alert_ok_btn);

        Context context = contentView.getContext();
        stopDialogTitleTv.setText(context.getString(R.string.clean_stop_title));
        stopDialogBodyTv.setText(context.getString(R.string.clean_stop_content));
        stopDialogCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissStopDialog(stopDialogV);
            }
        });

        stopDialogOkBtn.setText(context.getString(R.string.boost_plus_stop_sure));
        stopDialogOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                junkCleanWindowController.mIsBackButtonClicked = true;
                dismissStopDialog(stopDialogV);
                junkCleanWindowController.dismissCleanWindow();
            }
        });
        return stopDialogV;
    }

    public static void showStopDialog(View dialogView) {
        if (isStopDialogShowing(dialogView)) {
            return;
        }
        if (null != dialogView) {
            dialogView.setVisibility(View.VISIBLE);
        }
    }

    public static boolean isStopDialogShowing(View dialogView) {
        boolean isShowing = false;
        if (null != dialogView) {
            isShowing = (dialogView.getVisibility() == View.VISIBLE);
        }
        return isShowing;
    }

    public static void dismissStopDialog(View dialogView) {
        if (null != dialogView) {
            dialogView.setVisibility(View.GONE);
        }
    }

    public static class FlurryLogger {

        public static void logOpen(String type) {
            KCAnalytics.logEvent("JunkCleaner_Open", "Type", type);
        }

        public static void logHomepageButtonClicked() {
            KCAnalytics.logEvent("JunkCleaner_Homepage_Button_Clicked");
        }

        public static void logNotificationPushed() {
            KCAnalytics.logEvent("Notification_Pushed", "Type", ResultConstants.JUNK_CLEANER);
        }

        public static void logNotificationClicked() {
            KCAnalytics.logEvent("Notification_Clicked", "Type", ResultConstants.JUNK_CLEANER);
        }
    }
}
