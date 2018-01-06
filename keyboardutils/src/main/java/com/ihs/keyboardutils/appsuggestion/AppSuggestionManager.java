package com.ihs.keyboardutils.appsuggestion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.artw.lockscreen.lockerappguide.LockerAppGuideManager;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.charging.HSChargingManager;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.chargingscreen.utils.ChargingPrefsUtil;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.device.common.HSAppFilter;
import com.ihs.device.common.HSAppRunningInfo;
import com.ihs.device.common.utils.AppRunningUtils;

import java.util.ArrayList;
import java.util.List;

import static com.ihs.keyboardutils.appsuggestion.AppSuggestionActivity.showAppSuggestion;
import static com.ihs.keyboardutils.appsuggestion.AppSuggestionSetting.initEnableState;

/**
 * Created by Arthur on 17/12/8.
 */

public class AppSuggestionManager {

    private static final java.lang.String TAG = "AppSuggestionManager";
    private static final int MAX_APP_SIZE = 5;
    private boolean canShowAppSuggestion = true;
    private List<String> defaultAppList;
    private static final String GOOGLE_SEARCH_BAR_PACKAGE_NAME = "com.google.android.googlequicksearchbox";

    public void disableAppSuggestionForOneTime() {
        canShowAppSuggestion = false;
    }

    private static AppSuggestionManager ourInstance;
    private static List<HSAppRunningInfo> appRunningInfoList;
    private boolean isAppCanGetRecent;
    private String currentLauncherPkg = "";
    private List<String> exceptAppList = new ArrayList<>();
    private PackageManager packageManager;
    protected static final String FEATURE_NAME = "AppSuggestion";
    private Handler handler = new Handler();

    private String currentTopAppName = "";

    public static AppSuggestionManager getInstance() {
        if (ourInstance == null) {
            ourInstance = new AppSuggestionManager();
        }
        return ourInstance;
    }

    private ArrayList<String> recentAppPackName = new ArrayList<>();

    private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                boolean hasDone = false;
                Context ctx = HSApplication.getContext();
                if (!LockerAppGuideManager.getInstance().isLockerInstall()) {
                    boolean downloadLockerAlert = HSConfig.optBoolean(false, "Application", "DownloadScreenLocker", "UnlockScreen", "ShowUnlockScreenAlert");
                    if (downloadLockerAlert) {
                        int alertIntervalInHour = HSConfig.optInteger(24, "Application", "DownloadScreenLocker", "UnlockScreen", "AlertIntervalInHour");
                        long lastShowDownloadLockerAlertTime = PreferenceManager.getDefaultSharedPreferences(ctx).getLong("lastShowDownloadLockerAlertTime", 0);
                        if (System.currentTimeMillis() - lastShowDownloadLockerAlertTime > alertIntervalInHour * 60 * 60 * 1000) {
                            PreferenceManager.getDefaultSharedPreferences(ctx).edit().putLong("lastShowDownloadLockerAlertTime", System.currentTimeMillis()).apply();
                            LockerAppGuideManager.getInstance().showDownloadLockerAlert(ctx, LockerAppGuideManager.FLURRY_ALERT_FROM_LOCKER);
                            hasDone = true;
                        }
                    }
                }

                if (!hasDone) {
                    if (HSChargingManager.getInstance().isCharging() && ChargingManagerUtil.isChargingEnabled() && ChargingPrefsUtil.isChargingAlertEnabled()) {
                        int alertIntervalInMinute = HSConfig.optInteger(5, "Application", "ChargeAlert", "MiniInterval");
                        long lastShowDownloadLockerAlertTime = PreferenceManager.getDefaultSharedPreferences(ctx).getLong("lastShowChargingAlertTime", 0);
                        if (System.currentTimeMillis() - lastShowDownloadLockerAlertTime > alertIntervalInMinute * 60 * 1000) {
                            PreferenceManager.getDefaultSharedPreferences(ctx).edit().putLong("lastShowChargingAlertTime", System.currentTimeMillis()).apply();
                            ChargingManagerUtil.startChargingActivity();
                            hasDone = true;
                        }
                    }
                }

                if (!hasDone) {
                    if (AppSuggestionSetting.getInstance().canShowAppSuggestion() &&
                            AppSuggestionSetting.getInstance().isFeatureEnabled()) {
                        if (canShowAppSuggestion) {
                            if (!TextUtils.isEmpty(currentTopAppName)) {
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (currentTopAppName.equals(currentLauncherPkg) || currentTopAppName.equals(GOOGLE_SEARCH_BAR_PACKAGE_NAME)) {
                                            showAppSuggestion();
                                        }
                                    }
                                }, 500);
                            } else {
                                showAppSuggestion();
                            }
                        } else {
                            canShowAppSuggestion = true;
                        }

                    }
                }
            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                currentLauncherPkg = getDefaultLauncher();
                if (!isAppCanGetRecent) {
                    new FetchRunningAppTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    saveRecentList();
                }
            }
        }
    };

    private class FetchRunningAppTask extends AsyncTask<Void, Void, List<HSAppRunningInfo>> {
        List<HSAppRunningInfo> preAppRunningInfoList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            preAppRunningInfoList = appRunningInfoList == null ? null : new ArrayList<>(appRunningInfoList);
        }

        @Override
        protected List<HSAppRunningInfo> doInBackground(Void... voids) {
            long start = SystemClock.elapsedRealtime();
            List<HSAppRunningInfo> currentAppRunningInfoList = AppRunningUtils.getAppRunningInfoList(HSAppRunningInfo.class, new HSAppFilter());
            if (preAppRunningInfoList == null) {
                return currentAppRunningInfoList;
            } else {
                List<HSAppRunningInfo> newAppRunningInfoList = new ArrayList<>();
                List<HSAppRunningInfo> removeRunningInfoList = new ArrayList<>();
                for (HSAppRunningInfo hsAppRunningInfo : currentAppRunningInfoList) {
                    boolean contains = false;
                    for (HSAppRunningInfo appRunningInfo : preAppRunningInfoList) {
                        if (TextUtils.equals(hsAppRunningInfo.getAppName(), appRunningInfo.getAppName())) {
                            contains = true;
                            break;
                        }
                    }
                    if (!contains) {
                        newAppRunningInfoList.add(hsAppRunningInfo);
                    }
                }

                if (newAppRunningInfoList.size() != 0) {
                    preAppRunningInfoList.addAll(0, newAppRunningInfoList);
                }

                for (HSAppRunningInfo hsAppRunningInfo : preAppRunningInfoList) {
                    boolean contains = false;
                    for (HSAppRunningInfo appRunningInfo : currentAppRunningInfoList) {
                        if (TextUtils.equals(hsAppRunningInfo.getAppName(), appRunningInfo.getAppName())) {
                            contains = true;
                            break;
                        }
                    }
                    if (!contains) {
                        removeRunningInfoList.add(hsAppRunningInfo);
                    }
                }

                if (removeRunningInfoList.size() != 0) {
                    preAppRunningInfoList.removeAll(removeRunningInfoList);
                }

                HSLog.w(TAG, "doInBackground cost time :" + (SystemClock.elapsedRealtime() - start));
                return preAppRunningInfoList;
            }
        }

        @Override
        protected void onPostExecute(List<HSAppRunningInfo> newAppRunningInfoList) {
            long start = SystemClock.elapsedRealtime();
            appRunningInfoList = newAppRunningInfoList;
            ArrayList<String> recentAppList = new ArrayList<>();
            if (appRunningInfoList != null) {
                for (int i = 0; i < appRunningInfoList.size(); i++) {
                    if (recentAppList.size() >= MAX_APP_SIZE) {
                        break;
                    }
                    String packageName = appRunningInfoList.get(i).getPackageName();
                    if (!TextUtils.isEmpty(packageName) && !exceptAppList.contains(packageName) && packageManager.getLaunchIntentForPackage(packageName) != null) {
                        recentAppList.add(packageName);
                    }
                }
            }
            recentAppPackName = recentAppList;
            addDefaultSuggestApps();
            saveRecentList();
            HSLog.w(TAG, "onPostExecute cost time :" + (SystemClock.elapsedRealtime() - start));
        }
    }

    /**
     * In keyboard app, recent apps can be get by itself
     *
     * @param isAppCanGetRecent
     */
    public void init(boolean isAppCanGetRecent) {
        this.isAppCanGetRecent = isAppCanGetRecent;
        try {
            exceptAppList = (List<String>) HSConfig.getList("Application", FEATURE_NAME, "ApkException");
        } catch (Exception e) {
            exceptAppList = new ArrayList<>();
        }

        try {
            defaultAppList = (List<String>) HSConfig.getList("Application", FEATURE_NAME, "ApkDefault");
        } catch (Exception e) {
            defaultAppList = new ArrayList<>();
        }
        getSavedRecentList();
    }

    public AppSuggestionManager() {
        currentLauncherPkg = getDefaultLauncher();
        packageManager = HSApplication.getContext().getPackageManager();
        initEnableState();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        HSApplication.getContext().registerReceiver(intentReceiver, intentFilter);

        INotificationObserver observer = new INotificationObserver() {
            @Override
            public void onReceive(String s, HSBundle hsBundle) {
                if (s.equals(HSNotificationConstant.HS_CONFIG_CHANGED)) {
                    AppSuggestionSetting.getInstance().updateAppSuggestionSetting();
                    AppSuggestionSetting.getInstance().setShowInterval(HSConfig.optInteger(0, "Application", FEATURE_NAME, "MiniInterval") * 60 * 1000);
                    try {
                        exceptAppList = (List<String>) HSConfig.getList("Application", FEATURE_NAME, "ApkException");
                    } catch (Exception e) {
                        exceptAppList = new ArrayList<>();
                    }

                    try {
                        defaultAppList = (List<String>) HSConfig.getList("Application", FEATURE_NAME, "ApkDefault");
                    } catch (Exception e) {
                        defaultAppList = new ArrayList<>();
                    }
                }
            }
        };
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_CONFIG_CHANGED, observer);
    }

    private void saveRecentList() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : recentAppPackName) {
            stringBuilder.append(s);
            stringBuilder.append(",");
        }
        AppSuggestionSetting.getInstance().saveRecentList(stringBuilder.toString());
    }

    public void getSavedRecentList() {
        if (recentAppPackName.size() >= 5) {
            return;
        }

        String[] split = AppSuggestionSetting.getInstance().getSavedRecentList().split(",");
        if (split.length < 5) {
            addDefaultSuggestApps();
        } else {
            for (String s : split) {
                addNewRecentApp(s);
            }
        }
    }

    private void addDefaultSuggestApps() {
        if (defaultAppList != null) {
            for (String s : defaultAppList) {
                addNewRecentApp(s);
            }
        }
    }

    public void addNewRecentApp(String packageName) {
        if (!AppSuggestionSetting.getInstance().isEnabled() ||
                TextUtils.isEmpty(packageName) ||
                TextUtils.equals(packageName, currentLauncherPkg) ||
                exceptAppList.contains(packageName) ||
                packageManager.getLaunchIntentForPackage(packageName) == null) {
            return;
        }

        if (recentAppPackName.contains(packageName)) {
            recentAppPackName.remove(packageName);
        }

        if (recentAppPackName.size() >= 5) {
            recentAppPackName.remove(4);
        }
        recentAppPackName.add(0, packageName);
    }

    public ArrayList<String> getRecentAppPackName() {
        return new ArrayList<>(recentAppPackName);
    }

    private static String getDefaultLauncher() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = HSApplication.getContext().getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo != null && resolveInfo.activityInfo != null) {
            return resolveInfo.activityInfo.packageName;
        }
        return null;
    }
    
    public void setCurrentTopAppName(String currentTopAppName) {
        this.currentTopAppName = currentTopAppName;
    }
}
