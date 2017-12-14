package com.artw.lockscreen.lockerappguide;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.utils.HSMarketUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.BuildConfig;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.CommonUtils;
import com.kc.commons.utils.KCCommonUtils;

import net.appcloudbox.autopilot.AutopilotEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jixiang on 17/11/17.
 */

public class LockerAppGuideManager {
    public static final String FLURRY_ALERT_OPEN_APP = "alertOpenApp";
    public static final String FLURRY_ALERT_WALL_PAPER = "alertWallpaper";
    public static final String FLURRY_ALERT_UNLOCK = "alertUnlock";
    public static final String FLURRY_ALERT_FROM_LOCKER = "alertFromLocker";
    public static final String FLURRY_SET_AS_LOCK_SCREEN = "setAsLockScreenButton";

    private static final LockerAppGuideManager ourInstance = new LockerAppGuideManager();



    private boolean shouldGuideToLockerApp = false;
    private String lockerAppInstalledFrom = "";

    public static LockerAppGuideManager getInstance() {
        return ourInstance;
    }

    private List<ILockerInstallStatusChangeListener> lockerInstallStatusChangeListeners;

    public String getLockerAppPkgName() {
        return HSConfig.optString("","Application","DownloadScreenLocker","LockerApp");
    }
    private LockerAppGuideManager() {
    }

    public void init(boolean shouldGuideToLockerApp) {
        if (shouldGuideToLockerApp && !CommonUtils.isPackageInstalled(getLockerAppPkgName())) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
            intentFilter.addDataScheme("package");

            PackageInstallReceiver packageInstallReceiver = new PackageInstallReceiver();
            HSApplication.getContext().registerReceiver(packageInstallReceiver, intentFilter);

            intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_USER_PRESENT);

            UnlockScreenReceiver unlockScreenReceiver = new UnlockScreenReceiver();
            HSApplication.getContext().registerReceiver(unlockScreenReceiver, intentFilter);
        }
        this.shouldGuideToLockerApp = shouldGuideToLockerApp;
    }

    public void addLockerInstallStatusChangeListener(ILockerInstallStatusChangeListener lockerInstallStatusChangeListener) {
        if (lockerInstallStatusChangeListeners == null) {
            lockerInstallStatusChangeListeners = new ArrayList<>();
        }
        lockerInstallStatusChangeListeners.add(lockerInstallStatusChangeListener);
    }

    public void removeLockerInstallStatusChangeListener(ILockerInstallStatusChangeListener lockerInstallStatusChangeListener) {
        if (lockerInstallStatusChangeListeners != null) {
            lockerInstallStatusChangeListeners.remove(lockerInstallStatusChangeListener);
        }
    }

    private void setLockerInstall() {
        if (lockerInstallStatusChangeListeners != null) {
            for (ILockerInstallStatusChangeListener lockerInstallStatusChangeListener : lockerInstallStatusChangeListeners) {
                lockerInstallStatusChangeListener.onLockerInstallStatusChange();
            }
        }
        HSAnalytics.logEvent("googlePlay_smartLocker_installed", "googlePlay_smartLocker_installed", lockerAppInstalledFrom);
        if (FLURRY_ALERT_OPEN_APP.equals(lockerAppInstalledFrom)) {
            AutopilotEvent.logTopicEvent("topic-1512033355055", "locker_installed");
        }
    }

    public boolean isLockerInstall() {
        return CommonUtils.isPackageInstalled(getLockerAppPkgName());
    }

    public boolean isShouldGuideToLockerApp() {
        return shouldGuideToLockerApp;
    }

    public boolean shouldGuideToDownloadLocker() {
        return !CommonUtils.isPackageInstalled(getLockerAppPkgName()) && shouldGuideToLockerApp;
    }


    public void downloadOrRedirectToLockerApp(String from) {
        if (CommonUtils.isPackageInstalled(getLockerAppPkgName())) {
            openApp(getLockerAppPkgName());
        } else {
            directToMarket(null,null,getLockerAppPkgName());
            lockerAppInstalledFrom = from;
        }
    }

    /**
     * 用于跳转到 google play 下载 locker 的界面
     *
     * @param feature           字符串不包含特殊字符，例如 GuideView
     * @param viewType          字符串不包含特殊字符串，例如 ButtonOK，ButtonDownload
     * @param lockerPackageName 要跳转的 locker 的包名
     */
    public static void directToMarket(String feature, String viewType, String lockerPackageName) {
        Map<String, String> paras = new HashMap<>();
        StringBuilder parametersStr = new StringBuilder();
        parametersStr.append("packageName=" + BuildConfig.APPLICATION_ID);
        paras.put("TargetPackageName", lockerPackageName);
        if (!TextUtils.isEmpty(feature)) {
            parametersStr.append("&feature=").append(feature);
            paras.put("feature", feature);
        }
        if (!TextUtils.isEmpty(viewType)) {
            parametersStr.append("&viewType=").append(viewType);
            paras.put("viewType", viewType);
        }
        parametersStr.append("&versionName=" + BuildConfig.VERSION_NAME);
        parametersStr.append("&internal=" + BuildConfig.APPLICATION_ID);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            intent.setData(Uri.parse("market://details?id=" + lockerPackageName + "&referrer=" + Uri.encode(parametersStr.toString())));
            HSApplication.getContext().startActivity(intent);
            HSLog.d("cjx" + ">>>market  " + intent.getDataString());
        } catch (Exception e) {
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + lockerPackageName + "&referrer=" + Uri.encode(parametersStr.toString())));
            HSApplication.getContext().startActivity(intent);
            HSLog.d("cjx" + ">>>web  " + intent.getDataString());
        }
    }

    public void showDownloadLockerAlert(Context context, String from) {
        Map<String, ?> alertMap = new HashMap<>();
        switch (from) {
            case FLURRY_ALERT_FROM_LOCKER:
                alertMap = HSConfig.getMap("Application", "DownloadScreenLocker", "UnlockScreen");
                break;
            case FLURRY_ALERT_OPEN_APP:
                alertMap = HSConfig.getMap("Application", "DownloadScreenLocker", "AppOpen");
                break;
            case FLURRY_ALERT_UNLOCK:
                alertMap = HSConfig.getMap("Application", "DownloadScreenLocker", "UnlockForFree");
                break;
            case FLURRY_ALERT_WALL_PAPER:
                alertMap = HSConfig.getMap("Application", "DownloadScreenLocker", "Wallpaper");
                break;
        }
        if (alertMap.size() < 1) {
            return;
        }
        LockerGuideAlertBean bean = new LockerGuideAlertBean((String) alertMap.get("title"), (String) alertMap.get("body"), (String) alertMap.get("button"));
        CustomDesignAlert lockerDialog = new CustomDesignAlert(context);
        lockerDialog.setTitle(bean.getTitle());
        lockerDialog.setMessage(bean.getBody());
        lockerDialog.setImageResource(R.drawable.enable_tripple_alert_top_image);//locker image
        lockerDialog.setCancelable(true);
        lockerDialog.setPositiveButton(bean.getButton(), view -> {
            HSMarketUtils.browseAPP(getLockerAppPkgName());
            lockerAppInstalledFrom = from;
            HSAnalytics.logEvent("app_lockerAlert_button_clicked", "app_lockerAlert_button_clicked", from);
        });
        KCCommonUtils.showDialog(lockerDialog);
        HSAnalytics.logEvent("app_lockerAlert_show", "app_lockerAlert_show", from);
    }

    private static class PackageInstallReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            final String packageName = intent.getData().getEncodedSchemeSpecificPart();
            if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                if (packageName != null && packageName.equals(LockerAppGuideManager.getInstance().getLockerAppPkgName()))
                    LockerAppGuideManager.getInstance().setLockerInstall();
            }
        }
    }

    private static class UnlockScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!LockerAppGuideManager.getInstance().isLockerInstall()) {
                Context ctx = HSApplication.getContext();
                boolean downloadLockerAlert = HSConfig.optBoolean(false, "Application", "DownloadScreenLocker", "UnlockScreen", "ShowUnlockScreenAlert");
                if (downloadLockerAlert) {
                    int alertIntervalInHour = HSConfig.optInteger(24, "Application", "DownloadScreenLocker", "UnlockScreen", "AlertIntervalInHour");
                    long lastShowDownloadLockerAlertTime = PreferenceManager.getDefaultSharedPreferences(ctx).getLong("lastShowDownloadLockerAlertTime", 0);
                    if (System.currentTimeMillis() - lastShowDownloadLockerAlertTime > alertIntervalInHour * 60 * 60 * 1000) {
                        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putLong("lastShowDownloadLockerAlertTime", System.currentTimeMillis()).apply();
                        LockerAppGuideManager.getInstance().showDownloadLockerAlert(ctx, FLURRY_ALERT_FROM_LOCKER);
                    }
                }
            }
        }
    }

    public interface ILockerInstallStatusChangeListener {
        void onLockerInstallStatusChange();
    }

    public static void setLockerAppWallpaper(Context context, String picUrl,String thumb){
        Intent intent = new Intent("com.keyboard.setwallpaper");
        intent.putExtra("imgUrl",picUrl);
        intent.putExtra("thumb",thumb);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        PackageManager packageManager = HSApplication.getContext().getPackageManager();
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfoList != null && resolveInfoList.size() > 0) {
            context.startActivity(intent);
        }
    }

    public static boolean openApp(String packageName) {
        PackageManager manager = HSApplication.getContext().getPackageManager();
        try {
            Intent i = manager.getLaunchIntentForPackage(packageName);
            if (i == null) {
                return false;
                //throw new ActivityNotFoundException();
            }
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            HSApplication.getContext().startActivity(i);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }
}
