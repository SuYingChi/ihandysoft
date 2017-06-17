package com.ihs.feature.battery;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.honeycomb.launcher.BuildConfig;
import com.honeycomb.launcher.compat.LauncherActivityInfoCompat;
import com.honeycomb.launcher.compat.LauncherAppsCompat;
import com.honeycomb.launcher.compat.UserHandleCompat;
import com.honeycomb.launcher.model.LauncherSettings;
import com.honeycomb.launcher.model.Stats;
import com.honeycomb.launcher.util.ConcurrentUtils;
import com.honeycomb.launcher.util.LauncherPackageManager;
import com.honeycomb.launcher.util.Utils;
import com.ihs.app.framework.HSApplication;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BatteryDataManager {

    private String recentOpenedPackages;
    private List<BatteryAppInfo> mLauncherBatteryAppInfoListCache;
    private List<BatteryAppInfo> mAllInstallBatteryAppInfoListCache;
    private List<String> mAllSystemAppsPackageNameCache;
    private HashMap<String, Double> mRankAppsBatteryUsageMap = new HashMap<>();
    private static final String[] systemAppPackageNames = new String[] {"com.android.phone", "com.android.camera2", "com.google.android.GoogleCamera", "com.google.android.videos",
        "com.android.music", "com.android.providers.downloads", "com.android.providers.downloads.ui", "com.android.systemui"};

    BatteryDataManager(Context context) {
        initLauncherApps();
        intAllInstallApps();
        initRecentOpenApps(context);
    }

    private void initLauncherApps() {
        ConcurrentUtils.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                mLauncherBatteryAppInfoListCache = getLauncherBatteryAppInfoList(getLaunchApps());
            }
        });
    }

    private void intAllInstallApps() {
        final List<ApplicationInfo> mAllInstallApplicationInfoListCache = LauncherPackageManager.getInstance().getInstalledApplications();
        ConcurrentUtils.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                if (null != mAllInstallApplicationInfoListCache && mAllInstallApplicationInfoListCache.size() > 0) {
                    mAllInstallBatteryAppInfoListCache = getBatteryAppInfoList(mAllInstallApplicationInfoListCache, false);
                }
            }
        });
    }

    /**
     * load recent opened apps from LauncherProvider statistics table.
     */
    private void initRecentOpenApps(final Context context) {
        ConcurrentUtils.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                ContentResolver cr = context.getContentResolver();
                Cursor cursor;
                try {
                    cursor = cr.query(LauncherSettings.Statistics.CONTENT_URI, null, null, null,
                            LauncherSettings.Statistics.MODIFIED + " DESC");
                } catch (SQLiteException e) {
                    return;
                }
                if (cursor == null) {
                    return;
                }
                StringBuilder appInfoStr = new StringBuilder();
                try {
                    Set<String> deduplicateSet = new HashSet<>();
                    while (cursor.moveToNext()) {
                        Stats.LaunchStatItem statItem = new Stats.LaunchStatItem(cursor);
                        Intent intent;
                        try {
                            intent = Intent.parseUri(statItem.intent, 0);
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                            continue;
                        }
                        ComponentName component = intent.getComponent();
                        if (component == null) {
                            continue;
                        }
                        String packageName = component.getPackageName();
                        if (deduplicateSet.contains(packageName)) {
                            // Skip duplicated apps
                            continue;
                        }
                        deduplicateSet.add(packageName);
                        if(packageName.equals(BuildConfig.APPLICATION_ID)){
                            continue;
                        }
                        appInfoStr.append(packageName).append(",");
                    }
                } finally {
                    cursor.close();
                }

                List<LauncherActivityInfoCompat> launcherActivityInfos = getLaunchApps();
                for (LauncherActivityInfoCompat info : launcherActivityInfos) {
                    if(info.getPackageName().equals(BuildConfig.APPLICATION_ID)){
                        continue;
                    }
                    if (!appInfoStr.toString().contains(info.getPackageName())) {
                        appInfoStr.append(info.getPackageName()).append(",");
                    }
                }

                recentOpenedPackages = appInfoStr.toString();
            }
        });
    }

    List<BatteryAppInfo> getCleanAnimationBatteryApps() {
        if (null != mAllInstallBatteryAppInfoListCache) {
            return mAllInstallBatteryAppInfoListCache;
        }
        return mLauncherBatteryAppInfoListCache;
    }

    @NonNull List<BatteryAppInfo> getAllRankBatteryApps(boolean isContainSystemApp) {
        List<BatteryAppInfo> allBatteryAppInfoList = getAllInstallBatteryApps();
        if (null == allBatteryAppInfoList) {
            allBatteryAppInfoList = getAllLauncherBatteryApps();
        }

        if (null == allBatteryAppInfoList) {
            return new ArrayList<>();
        }

        mRankAppsBatteryUsageMap.clear();
        boolean hasRunningProcessInBackground = false;

        // 8^10 || 7^20
        List<String> runningNoSystemPackageList = getRunningPackageList(false);
        if (null != runningNoSystemPackageList) {
            int runningSize = runningNoSystemPackageList.size();
            if (runningSize == 1) {
                String runningPackageName = runningNoSystemPackageList.get(0);
                double percent = (double)8 + Math.random() * 2; // 8^10
                mRankAppsBatteryUsageMap.put(runningPackageName, percent);
            } else if (runningSize > 1) {
                double totalPercent = (double)15 + Math.random() * 5; // 15^20(7 < total < 20)
                double[] everyPercents = getInterpolatorPercent(runningSize, 2);
                for (int i = 0; i < runningSize; i++) {
                    String packageName = runningNoSystemPackageList.get(i);
                    if (!TextUtils.isEmpty(packageName)) {
                        double percentItem = totalPercent * everyPercents[i];
                        mRankAppsBatteryUsageMap.put(packageName, percentItem);
                    }
                }
            }

            hasRunningProcessInBackground = (runningSize > 0);
        }

        // 2^10 || 7^20
        List<String> recentOpenedPackageList = getRecentOpenedPackageList(false);
        if (null != recentOpenedPackageList) {
            int recentOpenedSize = recentOpenedPackageList.size();
            if (hasRunningProcessInBackground) {
                double totalPercent = (double)5 + Math.random() * 5; // 5^10(2 < total < 10)
                double[] everyPercents = getInterpolatorPercent(recentOpenedSize, 3);
                for (int i = 0; i < recentOpenedSize; i++) {
                    String packageName = recentOpenedPackageList.get(i);
                    if (!TextUtils.isEmpty(packageName)) {
                        double percentItem = totalPercent * everyPercents[i];
                        if (!mRankAppsBatteryUsageMap.containsKey(packageName)) {
                            mRankAppsBatteryUsageMap.put(packageName, percentItem);
                        }
                    }
                }
            } else {
                double totalPercent = (double)15 + Math.random() * 5; // 15^20(7 < total < 20)
                double[] everyPercents = getInterpolatorPercent(recentOpenedSize, 2);
                for (int i = 0; i < recentOpenedSize; i++) {
                    String packageName = recentOpenedPackageList.get(i);
                    if (!TextUtils.isEmpty(packageName)) {
                        double percentItem = totalPercent * everyPercents[i];
                        if (!mRankAppsBatteryUsageMap.containsKey(packageName)) {
                            mRankAppsBatteryUsageMap.put(packageName, percentItem);
                        }
                    }
                }
            }
        }

        // 17^40
        List<String> mainSystemAppPackageList = getMainSystemPackageList();
        if (null != mainSystemAppPackageList) {
            //systemAppPackageNames
            int mainSystemAppSize = mainSystemAppPackageList.size();
            double totalPercent = (double)35 + Math.random() * 5; // 35^40(17 < total < 40)
            double[] everyPercents = getInterpolatorPercent(mainSystemAppSize, 2);
            for (int i = 0; i < mainSystemAppSize; i++) {
                String packageName = mainSystemAppPackageList.get(i);
                if (!TextUtils.isEmpty(packageName)) {
                    double percentItem = totalPercent * everyPercents[i];
                    if (!mRankAppsBatteryUsageMap.containsKey(packageName)) {
                        mRankAppsBatteryUsageMap.put(packageName, percentItem);
                    }
                }
            }
        }

        List<String> remainingPackageNameList = new ArrayList<>();
        for (BatteryAppInfo batteryAppInfo : allBatteryAppInfoList) {
            if (null != batteryAppInfo) {
                String packageName = batteryAppInfo.getPackageName();
                if (!mRankAppsBatteryUsageMap.containsKey(packageName)) {
                    remainingPackageNameList.add(packageName);
                }
            }
        }

        double totalPercentInMap = 0;
        for (Map.Entry<String, Double> entry : mRankAppsBatteryUsageMap.entrySet()) {
            totalPercentInMap += entry.getValue();
        }

        if (totalPercentInMap < 100) {
            double remainingPercent = 100 - totalPercentInMap;
            int remainSize = remainingPackageNameList.size();
            double[] remainingEveryPercents = getInterpolatorPercent(remainSize, 3);

            for (int i = 0; i < remainSize; i++) {
                String packageName = remainingPackageNameList.get(i);
                if (!TextUtils.isEmpty(packageName)) {
                    double percentItem = remainingPercent * remainingEveryPercents[i];
                    if (!mRankAppsBatteryUsageMap.containsKey(packageName) || mRankAppsBatteryUsageMap.get(packageName) == 0) {
                        mRankAppsBatteryUsageMap.put(packageName, percentItem);
                    }
                }
            }
        }

        List<BatteryAppInfo> resultBatteryAppInfoList = new ArrayList<>();
        for (BatteryAppInfo batteryAppInfo : allBatteryAppInfoList) {
            if (null != batteryAppInfo) {
                String packageName = batteryAppInfo.getPackageName();
                String selfPackageName = HSApplication.getContext().getPackageName();
                boolean isSelf = !TextUtils.isEmpty(selfPackageName) && selfPackageName.equals(packageName);
                if (isSelf) {
                    continue;
                }
                double percent = mRankAppsBatteryUsageMap.containsKey(packageName) ? mRankAppsBatteryUsageMap.get(packageName) : 0;
                batteryAppInfo.setPercent(Utils.formatNumberTwoDigit(percent));
                boolean isSystemApp = batteryAppInfo.getIsSystemApp();
                if (isContainSystemApp) {
                    resultBatteryAppInfoList.add(batteryAppInfo);
                } else {
                    if (!isSystemApp) {
                        resultBatteryAppInfoList.add(batteryAppInfo);
                    }
                }
            }
        }

        Collections.sort(resultBatteryAppInfoList, new Comparator<BatteryAppInfo>() {
            @Override
            public int compare(BatteryAppInfo lhs, BatteryAppInfo rhs) {
                double percentPre = lhs.getPercent();
                double percentCurrent = rhs.getPercent();
                if(percentPre > percentCurrent){
                    return -1;
                } else if(percentPre < percentCurrent){
                    return 1;
                } else{
                    return 0;
                }
            }
        });
        return resultBatteryAppInfoList;
    }

    private List<BatteryAppInfo> getAllLauncherBatteryApps() {
        return mLauncherBatteryAppInfoListCache;
    }

    /**
     * Maybe return null
     */
    private @Nullable List<BatteryAppInfo> getAllInstallBatteryApps() {
        if (null == mAllInstallBatteryAppInfoListCache) {
            mAllInstallBatteryAppInfoListCache = getBatteryAppInfoList(LauncherPackageManager.getInstance().getInstalledApplications(), false);
        }
        return mAllInstallBatteryAppInfoListCache;
    }

    /**
     * Maybe return null
     */
    private List<String> getRecentOpenedPackageList(boolean isContainSystemApp) {
        if (TextUtils.isEmpty(recentOpenedPackages)) {
            return null;
        }

        List<String> packageNameList = new ArrayList<>();
        String[] packageNameArray = recentOpenedPackages.split(",");
        for (String packageName : packageNameArray) {
            if (!TextUtils.isEmpty(packageName)) {
                boolean isSystemApp = Utils.isSystemApp(HSApplication.getContext(), packageName);
                if (isContainSystemApp) {
                    packageNameList.add(packageName);
                } else {
                    if (!isSystemApp) {
                        packageNameList.add(packageName);
                    }
                }
            }
        }
        return packageNameList;
    }

    private List<String> getAllSystemPackageList() {
        if (null == mAllSystemAppsPackageNameCache) {
            mAllInstallBatteryAppInfoListCache = getBatteryAppInfoList(LauncherPackageManager.getInstance().getInstalledApplications(), false);
        }
        return mAllSystemAppsPackageNameCache;
    }

    private List<String> getMainSystemPackageList() {
        List<String> resultSystemPackageList = new ArrayList<>();
        List<String> allSystemPackageList = getAllSystemPackageList();
        if (null != allSystemPackageList) {
            for (String packageName : allSystemPackageList) {
                if (!TextUtils.isEmpty(packageName)) {
                    for (String systemAppPackageName : systemAppPackageNames) {
                        if (packageName.equals(systemAppPackageName)) {
                            resultSystemPackageList.add(packageName);
                        }
                    }
                }
            }
        }
        return resultSystemPackageList;
    }

    private List<LauncherActivityInfoCompat> getLaunchApps() {
        UserHandleCompat user = UserHandleCompat.myUserHandle();
        return LauncherAppsCompat.getInstance(HSApplication.getContext()).getActivityList(null, user);
    }

    private List<String> getRunningPackageList(boolean isContainSystemApp) {
        List<String> packageNameList = new ArrayList<>();
        Context context = HSApplication.getContext();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
        if (runningAppProcesses == null) {
            return packageNameList;
        }

        for (ActivityManager.RunningAppProcessInfo appProcessInfo : runningAppProcesses) {
            if (null != appProcessInfo) {
                String processName = appProcessInfo.processName;
                if (!TextUtils.isEmpty(processName)) {
                    String packageName = processName.split(":")[0].trim();
                    if (packageName.equals(BuildConfig.APPLICATION_ID)) {
                        continue;
                    }
                    String launcherPackageName = context.getPackageName();
                    boolean isSelf = false;
                    if (!TextUtils.isEmpty(launcherPackageName)) {
                        isSelf = launcherPackageName.equals(packageName);
                    }

                    if (!TextUtils.isEmpty(packageName) && !isSelf) {
                        boolean isSystem = Utils.isSystemApp(HSApplication.getContext(), packageName);
                        if (isContainSystemApp) {
                            if (!packageNameList.contains(packageName)) {
                                packageNameList.add(packageName);
                            }
                        } else {
                            if (!isSystem) {
                                if (!packageNameList.contains(packageName)) {
                                    packageNameList.add(packageName);
                                }
                            }
                        }
                    }
                }
            }
        }
        return packageNameList;
    }

    private @Nullable List<BatteryAppInfo> getBatteryAppInfoList(List<ApplicationInfo> applicationInfoList, boolean isNoSystemApp) {
        if (null == applicationInfoList || applicationInfoList.size() == 0) {
            return null;
        }
        List<BatteryAppInfo> allBatteryAppInfoList = new ArrayList<>();
        List<BatteryAppInfo> allNoSystemBatteryAppInfoList = new ArrayList<>();
        List<String> allSystemPackageNameList = new ArrayList<>();

        for (ApplicationInfo applicationInfo : applicationInfoList) {
            if (null != applicationInfo) {
                String appName = LauncherPackageManager.getInstance().getApplicationLabel(applicationInfo);
                String packageName = applicationInfo.packageName;
                BatteryAppInfo batteryAppInfo = new BatteryAppInfo(packageName);
                batteryAppInfo.setAppName(appName);
                boolean isSystemApp = Utils.isSystemApp(applicationInfo);
                batteryAppInfo.setIsSystemApp(isSystemApp);

                if (isSystemApp) {
                    allSystemPackageNameList.add(packageName);
                } else {
                    allNoSystemBatteryAppInfoList.add(batteryAppInfo);
                }
                allBatteryAppInfoList.add(batteryAppInfo);
            }
        }
        mAllSystemAppsPackageNameCache = allSystemPackageNameList;
        return isNoSystemApp ? allNoSystemBatteryAppInfoList : allBatteryAppInfoList;
    }

    private List<BatteryAppInfo> getLauncherBatteryAppInfoList(List<LauncherActivityInfoCompat> applicationInfoList) {
        if (null == applicationInfoList || applicationInfoList.size() == 0) {
            return null;
        }
        List<BatteryAppInfo> usageInfoList = new ArrayList<>();
        for (LauncherActivityInfoCompat applicationInfo : applicationInfoList) {
            if (null != applicationInfo) {
                String appName = applicationInfo.getLabel().toString();
                String packageName = applicationInfo.getPackageName();
                BatteryAppInfo hSAppUsageInfo = new BatteryAppInfo(packageName);
                hSAppUsageInfo.setAppName(appName);
                hSAppUsageInfo.setIsSystemApp(Utils.isSystemApp(applicationInfo.getApplicationInfo()));
                usageInfoList.add(hSAppUsageInfo);
            }
        }
        return usageInfoList;
    }

    @NonNull
    private double[] getInterpolatorPercent(int size, int mainNumber) {
        double[] percents = new double[size];
        if (size == 1) {
            percents[0] = 1;
        } else if (size == 2) {
            percents[0] = 0.7;
            percents[1] = 0.3;
        } else {
            for (int i = 0; i < size; i++) {
                switch (mainNumber) {
                    case 2:
                        if (i == 0) {
                            percents[0] = 0.7;
                        } else if (i == 1) {
                            percents[1] = 0.2;
                        } else {
                            percents[i] = 0.1 / (size - 2);
                        }
                        break;
                    case 3:
                        if (i == 0) {
                            percents[0] = 0.6;
                        } else if (i == 1) {
                            percents[1] = 0.2;
                        } else if (i == 2) {
                            percents[1] = 0.1;
                        }  else {
                            percents[i] = 0.1 / (size - 2);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        return percents;
    }
}
