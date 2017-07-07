package com.ihs.keyboardutils.permission;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.BaseColumns;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityManager;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.devicemonitor.accessibility.HSAccessibilityService;
import com.ihs.keyboardutils.utils.CommonUtils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PermissionUtils {

    private static final String TAG = PermissionUtils.class.getSimpleName();

    private static final int MAXIMUM_OBSERVE_TIME_SECONDS = 30;

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    private static final int PERMISSION_ACCESSIBILITY = 1;
    private static final int PERMISSION_NOTIFICATION = 2;

    public static void stopObservingNotificationPermission(ContentObserver observer) {
        stopObservingPermission(observer, PERMISSION_NOTIFICATION);
    }

    public static void startObservingAccessibilityPermission(final Runnable permissionEnabledRunnable) {
        final Handler watchDog = new Handler();
        final ContentObserver observer = new ContentObserver(new Handler()) {
            @SuppressLint("InflateParams")
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                HSLog.d("Permission.Access", "Accessibility permission changed enable = " + isAccessibilityGranted());
                if (isAccessibilityGranted()) {
                    if (permissionEnabledRunnable != null) {
                        permissionEnabledRunnable.run();
                    }
                    HSAnalytics.logEvent("Authority_Accessibility_Granted");
                }
                watchDog.removeCallbacksAndMessages(null);
                stopObservingPermission(this, PERMISSION_ACCESSIBILITY);
            }
        };
        watchDog.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopObservingPermission(observer, PERMISSION_ACCESSIBILITY);
            }
        }, MAXIMUM_OBSERVE_TIME_SECONDS * 1000);
        startObservingPermission(observer, PERMISSION_ACCESSIBILITY);
    }


    public static void startObservingUsageAccessPermission(final Runnable grantedAction) {
        new Timer().schedule(new TimerTask() {
            private static final int MAXIMUM_POLL_TIMES = MAXIMUM_OBSERVE_TIME_SECONDS;

            private int mPollTimes;

            @Override
            public void run() {
                mPollTimes++;
                if (mPollTimes > MAXIMUM_POLL_TIMES) {
                    cancel();
                    return;
                }
                boolean granted = isUsageAccessGranted();
                HSLog.d("Permission.UsageAccess", "Poll once, granted: " + granted);
                if (granted) {
                    cancel();
                    grantedAction.run();
                }
            }
        }, 0, 1000);
    }

    private static void startObservingPermission(ContentObserver observer, int permission) {
        for (String settingName : getSettingNames(permission)) {
            HSLog.d(TAG, "Start observing permission with setting name: " + settingName);
            HSApplication.getContext().getContentResolver().registerContentObserver(Settings.Secure.getUriFor(settingName), false, observer);
        }
    }

    private static void stopObservingPermission(ContentObserver observer, int permission) {
        HSLog.d(TAG, "Stop observing permission: " + permission);
        HSApplication.getContext().getContentResolver().unregisterContentObserver(observer);
    }

    private static String[] getSettingNames(int permission) {
        switch (permission) {
            case PERMISSION_ACCESSIBILITY:
                return getAccessibilitySettingNames();
            case PERMISSION_NOTIFICATION:
                return getNotificationSettingNames();
        }
        throw new IllegalArgumentException("Permission not define");
    }


    private static String[] getAccessibilitySettingNames() {
        String[] names = new String[1];
        names[0] = Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES;
        return names;
    }

    private static String[] getNotificationSettingNames() {
        if (CommonUtils.ATLEAST_JB_MR2) {
            String[] names = new String[1];
            names[0] = ENABLED_NOTIFICATION_LISTENERS;
            return names;
        } else {
            return getAccessibilitySettingNames();
        }
    }

    /**
     * Use in main process
     * @return
     */
    public static boolean isAccessibilityGranted() {
        return isAccessibilityGranted(HSApplication.getContext());
    }

    public static boolean isAccessibilityGranted(Context context) {
        final Context c = context.getApplicationContext();
        boolean isGranted = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            AccessibilityManager accessibilityManager = (AccessibilityManager) c.getSystemService
                    (Context.ACCESSIBILITY_SERVICE);
            if (accessibilityManager != null) {
                List<AccessibilityServiceInfo> enabledAccessibilityServiceList = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
                if (!(enabledAccessibilityServiceList == null || enabledAccessibilityServiceList.isEmpty())) {
                    for (AccessibilityServiceInfo accessibilityServiceInfo : enabledAccessibilityServiceList) {
                        if (accessibilityServiceInfo != null) {
                            ResolveInfo resolveInfo = accessibilityServiceInfo.getResolveInfo();
                            if (resolveInfo != null) {
                                ServiceInfo serviceInfo = resolveInfo.serviceInfo;
                                if (!(serviceInfo == null || TextUtils.isEmpty(serviceInfo.packageName) || !c.getPackageName().equals(serviceInfo.packageName))) {
                                    isGranted = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!isGranted) {
            isGranted = isAccessibilityGrantedOtherWay(c);
        }
        HSLog.d("accessibilityEnabled:" + isGranted);
        return isGranted;
    }

    private static boolean isAccessibilityGrantedOtherWay(Context context) {
        String string = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (TextUtils.isEmpty(string)) {
            return false;
        }
        String packageName = HSApplication.getContext().getPackageName();
        String name = HSAccessibilityService.class.getName();
        String[] split = string.split(":");
        if (split.length <= 0) {
            return false;
        }
        for (String obj : split) {
            if (!TextUtils.isEmpty(obj)) {
                ComponentName unflattenFromString = ComponentName.unflattenFromString(obj);
                if (unflattenFromString != null) {
                    String packageName2 = unflattenFromString.getPackageName();
                    String className = unflattenFromString.getClassName();
                    if (packageName.equals(packageName2) && name.equals(className)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isUsageAccessGranted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return true;
        }
        try {
            PackageManager packageManager = HSApplication.getContext().getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(HSApplication.getContext().getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) HSApplication.getContext().getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean checkContentObservePermissionForUri(Uri uri) {
        final Context context = HSApplication.getContext();

        try {
            Cursor cursor = context.getContentResolver().query(uri,
                    new String[]{BaseColumns._ID}, null, null, null);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            HSLog.d(TAG, "permission denied for " + uri);
            e.printStackTrace();
            return false;
        }
        HSLog.d(TAG, "permission access for " + uri);
        return true;
    }

    public static void enableUsageAccessPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !PermissionUtils.isUsageAccessGranted()) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
            HSApplication.getContext().startActivity(intent);
            PermissionFloatWindow.getInstance().createPermissionTip(PermissionTip.TYPE_TEXT_USAGE);
        }
    }

}
