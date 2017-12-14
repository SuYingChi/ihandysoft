package com.ihs.keyboardutils.appsuggestion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.device.common.AppFilter;
import com.ihs.device.common.HSAppRunningInfo;
import com.ihs.device.common.utils.AppRunningUtils;
import com.kc.utils.KCFeatureControlUtils;

import java.util.ArrayList;
import java.util.List;

import static com.ihs.keyboardutils.appsuggestion.AppSuggestionActivity.showAppSuggestion;
import static com.ihs.keyboardutils.appsuggestion.AppSuggestionSetting.initEnableState;

/**
 * Created by Arthur on 17/12/8.
 */

public class AppSuggestionManager {
    private static AppSuggestionManager ourInstance;
    private static List<HSAppRunningInfo> appRunningInfoList;
    private boolean isAppCanGetRecent;
    private String currentLauncherPkg = "";
    private List<String> exceptAppList = new ArrayList<>();
    private PackageManager packageManager;
    private static final String FEATURE_NAME = "AppSuggestion";


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
                if (AppSuggestionSetting.getInstance().canShowAppSuggestion()) {
                    showAppSuggestion();
                }
            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                currentLauncherPkg = getDefaultLauncher();
                if (!isAppCanGetRecent) {
                    List<HSAppRunningInfo> appRunningInfoList = getAppRunningInfoList();
                    for (int i = 0; i < appRunningInfoList.size(); i++) {
                        if (recentAppPackName.size() < 5) {
                            addNewRecentApp(appRunningInfoList.get(i).getPackageName());
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    };


    /**
     * In keyboard app, recent apps can be get by itself
     *
     * @param isAppCanGetRecent
     */
    public void init(boolean isAppCanGetRecent) {
        this.isAppCanGetRecent = isAppCanGetRecent;
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
                    AppSuggestionSetting.getInstance().setShowInterval(HSConfig.optInteger(0, "Application", FEATURE_NAME, "MiniInterval"));
                    try {
                        exceptAppList = (List<String>) HSConfig.getList("Application", FEATURE_NAME, "ApkException");
                    } catch (Exception e) {
                        exceptAppList = new ArrayList<>();
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

    private void getSavedRecentList() {
        String[] split = AppSuggestionSetting.getInstance().getSavedRecentList().split(",");
        if (split.length < 5) {
            addNewRecentApp("com.whatsapp");
            addNewRecentApp("com.facebook.orca");
            addNewRecentApp("com.facebook.katana");
            addNewRecentApp("com.instagram.android");
            addNewRecentApp("com.snapchat.android");
        } else {
            for (String s : split) {
                addNewRecentApp(s);
            }
        }
    }

    public void addNewRecentApp(String packageName) {
        if (!AppSuggestionSetting.getInstance().isEnabled() ||
                TextUtils.isEmpty(packageName) ||
                TextUtils.equals(packageName, currentLauncherPkg) ||
                exceptAppList.contains(packageName) ||
                packageManager.getLaunchIntentForPackage(packageName) == null ||
                !isFeatureEnabled()) {
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
        return recentAppPackName;
    }

    private static String getDefaultLauncher() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = HSApplication.getContext().getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo.activityInfo.packageName;
    }

    private static List<HSAppRunningInfo> getAppRunningInfoList() {
        List<HSAppRunningInfo> currentAppRunningInfoList = AppRunningUtils.getAppRunningInfoList(HSAppRunningInfo.class, new AppFilter(), true, true, true, true);

        if (appRunningInfoList == null) {
            appRunningInfoList = currentAppRunningInfoList;
        } else {
            List<HSAppRunningInfo> newAppRunningInfoList = new ArrayList<>();
            List<HSAppRunningInfo> removeRunningInfoList = new ArrayList<>();
            for (HSAppRunningInfo hsAppRunningInfo : currentAppRunningInfoList) {
                boolean contains = false;
                for (HSAppRunningInfo appRunningInfo : appRunningInfoList) {
                    if (TextUtils.equals(hsAppRunningInfo.getAppName(), appRunningInfo.getAppName())) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    newAppRunningInfoList.add(hsAppRunningInfo);
                }
            }

            for (HSAppRunningInfo hsAppRunningInfo : appRunningInfoList) {
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

            if (newAppRunningInfoList.size() != 0) {
                appRunningInfoList.addAll(0, newAppRunningInfoList);
            }
            if (removeRunningInfoList.size() != 0) {
                appRunningInfoList.removeAll(removeRunningInfoList);
            }
        }

        return appRunningInfoList;
    }

    private boolean isFeatureEnabled() {
        return KCFeatureControlUtils.isFeatureReleased(HSApplication.getContext(),
                FEATURE_NAME,
                HSConfig.optInteger(0, "Application", FEATURE_NAME, "HoursFromFirstUse"));
    }
}
