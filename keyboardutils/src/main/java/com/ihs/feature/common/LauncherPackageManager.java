package com.ihs.feature.common;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.text.TextUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Wraps over {@link PackageManager}, caching queries and handling exceptions.
 */
public class LauncherPackageManager {

    private static final String TAG = "LauncherPackageManager";

    private static volatile LauncherPackageManager sInstance;

    private PackageManager mPm;

    private final ReadWriteLock mCacheLock = new ReentrantReadWriteLock();
    private final List<ApplicationInfo> mAppsCache = new ArrayList<>();
    private Map<String, Drawable> mAppIconsCache = new HashMap<>();
    private Map<String, String> mAppLabelsCache = new HashMap<>();

    public static void init() {
        sInstance = new LauncherPackageManager();
    }

    public static LauncherPackageManager getInstance() {
        if (sInstance == null) {
            synchronized (LauncherPackageManager.class) {
                if (sInstance == null) {
                    sInstance = new LauncherPackageManager();
                }
            }
        }
        return sInstance;
    }

    private LauncherPackageManager() {
        mPm = HSApplication.getContext().getPackageManager();

        initApplicationInfos();
    }

    public Drawable getApplicationIcon(ApplicationInfo application) {
        if (application == null) {
            return null;
        }
        return getApplicationIcon(application.packageName);
    }


    public Drawable getApplicationIcon(String packageName) {
        if (HSLog.isDebugging() && Looper.myLooper() != Looper.getMainLooper()) {
            throw new RuntimeException("should run in main thread");
        }

        Drawable icon = mAppIconsCache.get(packageName);
        if (icon == null) {
            try {
                icon = mPm.getApplicationIcon(packageName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (icon != null) {
                mAppIconsCache.put(packageName, icon);
            }
        }
        return icon;
    }

    public String getApplicationLabel(ApplicationInfo application) {
        if (null == application) {
            return null;
        }

        String label = mAppLabelsCache.get(application.packageName);
        if (label == null) {
            try {
                mAppLabelsCache.put(application.packageName,
                        label = mPm.getApplicationLabel(application).toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return label;
    }

    public String getApplicationLabel(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return "";
        }
        ApplicationInfo application = getApplicationInfo(pkgName);

        if (application == null) {
            return "";
        }

        String label = mAppLabelsCache.get(application.packageName);
        if (label == null) {
            try {
                mAppLabelsCache.put(application.packageName,
                        label = mPm.getApplicationLabel(application).toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return label;
    }

    public List<ApplicationInfo> getInstalledApplications() {
        Lock lock = mCacheLock.readLock();
        lock.lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(mAppsCache));
        } finally {
            lock.unlock();
        }
    }

    public ApplicationInfo getApplicationInfo(String packageName) {
        Lock lock = mCacheLock.readLock();
        lock.lock();
        try {
            for (ApplicationInfo applicationInfo : mAppsCache) {
                if (applicationInfo.packageName.equals(packageName)) {
                    return applicationInfo;
                }
            }
        } finally {
            lock.unlock();
        }

        ApplicationInfo info = null;
        try {
            synchronized (mAppsCache) {
                if (null != mPm) {
                    info = mPm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                }
                if (info != null) {
                    mAppsCache.add(info);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initApplicationInfos() {
        HSLog.d(TAG, "initApplicationInfos ***");
        ConcurrentUtils.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                initApplicationInfosSync();
            }
        });
    }

    public void updateApplicationInfos() {
        HSLog.d(TAG, "updateApplicationInfos ***");
        initApplicationInfosSync();
    }

    @SuppressWarnings("WeakerAccess")
    @Thunk void initApplicationInfosSync() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos;
        try {
            resolveInfos = mPm.queryIntentActivities(intent, PackageManager.GET_META_DATA);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        List<ApplicationInfo> applicationInfos = new ArrayList<>(resolveInfos.size());
        Set<String> deduplicateSet = new HashSet<>();
        for (ResolveInfo resolveInfo : resolveInfos) {
            String packageName = resolveInfo.activityInfo.packageName;
            ApplicationInfo appInfo = resolveInfo.activityInfo.applicationInfo;
            if (TextUtils.isEmpty(packageName) || appInfo == null) {
                continue;
            }
            if (deduplicateSet.contains(packageName)) {
                continue;
            }
            applicationInfos.add(appInfo);
            deduplicateSet.add(packageName);
        }

        Lock lock = mCacheLock.writeLock();
        lock.lock();
        try {
            mAppsCache.clear();
            mAppsCache.addAll(applicationInfos);
        } finally {
            lock.unlock();
        }
    }
}
