package com.ihs.feature.junkclean.data;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.ihs.commons.utils.HSLog;
import com.ihs.device.clean.junk.cache.app.nonsys.junk.HSAppJunkCache;
import com.ihs.device.clean.junk.cache.app.nonsys.junk.HSAppJunkCacheManager;
import com.ihs.device.clean.junk.cache.app.sys.HSAppSysCache;
import com.ihs.device.clean.junk.cache.app.sys.HSAppSysCacheManager;
import com.ihs.device.clean.junk.cache.nonapp.commonrule.HSCommonFileCache;
import com.ihs.device.clean.junk.cache.nonapp.commonrule.HSCommonFileCacheManager;
import com.ihs.device.clean.junk.cache.nonapp.pathrule.HSPathFileCache;
import com.ihs.device.clean.junk.cache.nonapp.pathrule.HSPathFileCacheManager;
import com.ihs.device.clean.memory.HSAppMemory;
import com.ihs.device.clean.memory.HSAppMemoryManager;
import com.ihs.feature.common.FormatSizeBuilder;
import com.ihs.feature.common.LauncherPackageManager;
import com.ihs.feature.tip.LauncherTipManager;
import com.ihs.feature.common.PackageItemInfo;
import com.ihs.feature.common.Utils;
import com.ihs.feature.junkclean.JunkCleanActivity;
import com.ihs.feature.junkclean.model.ApkJunkWrapper;
import com.ihs.feature.junkclean.model.AppJunkWrapper;
import com.ihs.feature.junkclean.model.JunkInfo;
import com.ihs.feature.junkclean.model.JunkWrapper;
import com.ihs.feature.junkclean.model.MemoryJunkWrapper;
import com.ihs.feature.junkclean.model.PathRuleJunkWrapper;
import com.ihs.feature.junkclean.model.SystemJunkWrapper;
import com.ihs.feature.junkclean.util.JunkCleanConstant;
import com.ihs.feature.junkclean.util.JunkCleanUtils;
import com.ihs.feature.tip.JunkCleanFloatTip;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.permission.PermissionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class JunkManager {

    private static JunkManager mInstance;

    private static final int SYSTEM_CACHE_COUNT_LIMIT = 15;
    private static final int SYSTEM_CACHE_SIZE_LIMIT = 13 * 1024;

    private List<JunkWrapper> mJunkWrappers = new CopyOnWriteArrayList<>();
    private JunkInfo mJunkInfo = new JunkInfo();

    private HSAppSysCacheManager.AppSysCacheTaskListener mSystemJunkListener;
    private HSCommonFileCacheManager.FileScanTaskListener mApkJunkListener;
    private HSAppJunkCacheManager.AppJunkCacheScanTaskListener mAppJunkListener;
    private HSPathFileCacheManager.PathFileCacheScanTaskListener mPathFileJunkListener;
    private HSAppMemoryManager.MemoryTaskListener mMemoryJunkListener;

    private boolean mIsSystemCacheJunkScanFinished;
    private boolean mIsSystemApkJunkScanFinished;
    private boolean mIsAppJunkScanFinished;
    private boolean mIsAdJunkScanFinished;
    private boolean mIsMemoryJunkScanFinished;
    private boolean mIsScanning;

    private int mSystemCacheAppCount = 0;
    private long mExtendRandomSize;
    private long mExtendAdRandomSize;
    private Runnable mInstallScanRunnable;

    public interface ScanJunkListener {
        void onScanNameChanged(String name);

        void onScanSizeChanged(String categoryType, JunkInfo junkInfo, boolean isEnd);
        
        void onScanFinished(long junkSize);
    }

    public static class ScanJunkListenerAdapter implements ScanJunkListener {
        @Override
        public void onScanNameChanged(String name) {

        }

        @Override
        public void onScanSizeChanged(String categoryType, JunkInfo junkInfo, boolean isEnd) {

        }

        @Override
        public void onScanFinished(long junkSize) {

        }
    }

    public static JunkManager getInstance() { //must be called in main thread
        if (mInstance == null) {
            mInstance = new JunkManager();
        }
        return mInstance;
    }

    public List<JunkWrapper> getJunkWrappers() {
        return mJunkWrappers;
    }

    public long getTotalJunkSize() {
        long junkSize = 0;
        if (null != mJunkWrappers) {
            for (JunkWrapper wrapper : mJunkWrappers) {
                junkSize += wrapper.getSize();
            }
        }
        return junkSize;
    }

    public boolean isTotalJunkSelected() {
        boolean selected = true;
        for (JunkWrapper junkWrapper : mJunkWrappers) {
            if (!junkWrapper.isMarked()) {
                selected = false;
                break;
            }
        }
        return selected;
    }

    public long getJunkSelectedSize() {
        long junkSize = 0;
        if (null != mJunkWrappers) {
            for (JunkWrapper wrapper : mJunkWrappers) {
                if (!wrapper.isMarked()) {
                    continue;
                }
                junkSize += wrapper.getSize();
            }
        }
        return junkSize;
    }

    public boolean isJunkScanFrozen() {
        long lastJunkScanFinishTime = JunkCleanUtils.getLastJunkScanFinishTime();
        if (0 == lastJunkScanFinishTime) {
            return false;
        }

        boolean isScanCanceled = JunkCleanUtils.isScanCanceled();
        long secondTimeFromLastOpen = (System.currentTimeMillis() - lastJunkScanFinishTime) / 1000;
        HSLog.d(JunkCleanActivity.TAG, "isJunkScanFrozen isScanCanceled = " + isScanCanceled + " secondTimeFromLastOpen = " + secondTimeFromLastOpen);
        return !isScanCanceled && secondTimeFromLastOpen <= JunkCleanConstant.FROZEN_JUNK_SCAN_SECOND_TIME;
    }

    public void startJunkScan(final ScanJunkListener scanJunkListener) {
        startJunkScan(scanJunkListener, true);
    }

    public void startJunkScan(final ScanJunkListener scanJunkListener, final boolean isContainMemoryScan) {
        if (mIsScanning) {
            stopJunkScan();
        }

        clear();
        mIsScanning = true;

        mSystemJunkListener = new HSAppSysCacheManager.AppSysCacheTaskListener() {
            @Override
            public void onStarted() {
                HSLog.d(JunkCleanActivity.TAG, "startJunkScan ^^^ System Junk ^^^ onStarted");
            }

            @Override
            public void onProgressUpdated(int processedCount, int total, HSAppSysCache hsAppSysCache) {
                HSLog.d(JunkCleanActivity.TAG, "startJunkScan ^^^ System Junk ^^^ onProgressUpdated processedCount = " + processedCount + " total = " + total);
                mSystemCacheAppCount = total >= SYSTEM_CACHE_COUNT_LIMIT ? getSystemCacheCountRandom() : total;

                long junkSize = hsAppSysCache.getSize();
                if (processedCount <= mSystemCacheAppCount && junkSize >= SYSTEM_CACHE_SIZE_LIMIT) {
                    mJunkInfo.setSystemJunkSize(mJunkInfo.getSystemJunkSize() + hsAppSysCache.getSize());
                    if (null != scanJunkListener) {
                        scanJunkListener.onScanSizeChanged(JunkCleanConstant.CATEGORY_SYSTEM_JUNK, mJunkInfo, false);
                    }
                }

                if (null != scanJunkListener) {
                    scanJunkListener.onScanNameChanged(hsAppSysCache.getPackageName());
                }
            }

            @Override
            public void onSucceeded(List<HSAppSysCache> junks, long junkSize) {
                HSLog.d(JunkCleanActivity.TAG, "startJunkScan ^^^ System Junk ^^^ onSucceeded junkCount = " + junks.size() + " junkSize = " + junkSize);
                mIsSystemCacheJunkScanFinished = true;
                int systemCacheAppCount = junks.size();
                if (JunkCleanUtils.shouldForceCleanSystemAppCache() && systemCacheAppCount >= SYSTEM_CACHE_COUNT_LIMIT) {
                    systemCacheAppCount = (mSystemCacheAppCount == 0) ? getSystemCacheCountRandom() : mSystemCacheAppCount;
                }

                int appCount = 0;
                for (HSAppSysCache junk : junks) {
                    appCount++;
                    if (appCount > systemCacheAppCount) {
                        break;
                    }

                    if (junk.getSize() < SYSTEM_CACHE_SIZE_LIMIT) {
                        continue;
                    }

                    mJunkInfo.setSystemJunkSize(mJunkInfo.getSystemJunkSize() + junk.getSize());

                    SystemJunkWrapper wrapper = new SystemJunkWrapper(junk);
                    if (JunkCleanUtils.shouldForceCleanSystemAppCache()
                            && !PermissionUtils.isAccessibilityGranted()) {
                        wrapper.setMarked(false);
                    }
                    mJunkWrappers.add(wrapper);
                }

                if (null != scanJunkListener) {
                    scanJunkListener.onScanSizeChanged(JunkCleanConstant.CATEGORY_SYSTEM_JUNK, mJunkInfo, mIsSystemApkJunkScanFinished);
                }
                scanFinished(scanJunkListener, mJunkInfo.getTotalJunkSize(), isContainMemoryScan);
            }

            @Override
            public void onFailed(int failCode, String failMsg) {
                HSLog.d(JunkCleanActivity.TAG, "startJunkScan ^^^ System Junk ^^^ onFailed failCode = " + failCode + " failMsg = " + failMsg);
                mIsSystemCacheJunkScanFinished = true;
                if (null != scanJunkListener) {
                    scanJunkListener.onScanSizeChanged(JunkCleanConstant.CATEGORY_SYSTEM_JUNK, mJunkInfo, mIsSystemApkJunkScanFinished);
                }
                scanFinished(scanJunkListener, mJunkInfo.getTotalJunkSize(), isContainMemoryScan);
            }
        };
        HSAppSysCacheManager.getInstance().startScanWithCompletedProgress(mSystemJunkListener);

        List<String> extensions = new ArrayList<>();
        extensions.add(JunkCleanConstant.JUNK_TYPE_APK);
        mApkJunkListener = new HSCommonFileCacheManager.FileScanTaskListener() {
            @Override
            public void onStarted() {
                HSLog.d(JunkCleanActivity.TAG, "startJunkScan === Apk Junk === onStarted");
            }

            @Override
            public void onProgressUpdated(int processedCount, int total, HSCommonFileCache commonFileCache) {
                HSLog.d(JunkCleanActivity.TAG, "startJunkScan === Apk Junk === onProgressUpdated processedCount = " + processedCount + " total = " + total);
                ApkJunkWrapper wrapper = new ApkJunkWrapper(commonFileCache);

                if (!wrapper.isValidApk() || commonFileCache.getSize() <= 0) {
                    return;
                }

                mJunkInfo.setApkJunkSize(mJunkInfo.getApkJunkSize() + commonFileCache.getSize());
                mJunkWrappers.add(wrapper);
                if (null != scanJunkListener) {
                    scanJunkListener.onScanNameChanged(commonFileCache.getFileName());
                }
                if (null != scanJunkListener) {
                    scanJunkListener.onScanSizeChanged(JunkCleanConstant.CATEGORY_SYSTEM_JUNK, mJunkInfo, false);
                }
            }

            @Override
            public void onSucceeded(Map<String, List<HSCommonFileCache>> map, long junkSize) {
                HSLog.d(JunkCleanActivity.TAG, "startJunkScan === Apk Junk === onSucceeded junkCount = " + map.size() + " junkSize = " + junkSize);
                mIsSystemApkJunkScanFinished = true;
                if (null != scanJunkListener) {
                    scanJunkListener.onScanSizeChanged(JunkCleanConstant.CATEGORY_SYSTEM_JUNK, mJunkInfo, mIsSystemCacheJunkScanFinished);
                }
                scanFinished(scanJunkListener, mJunkInfo.getTotalJunkSize(), isContainMemoryScan);
            }

            @Override
            public void onFailed(int failCode, String failMsg) {
                HSLog.d(JunkCleanActivity.TAG, "startJunkScan === Apk Junk === onFailed failCode = " + failCode + " failMsg = " + failMsg);
                mIsSystemApkJunkScanFinished = true;
                if (null != scanJunkListener) {
                    scanJunkListener.onScanSizeChanged(JunkCleanConstant.CATEGORY_SYSTEM_JUNK, mJunkInfo, mIsSystemCacheJunkScanFinished);
                }
                scanFinished(scanJunkListener, mJunkInfo.getTotalJunkSize(), isContainMemoryScan);
            }
        };
        HSCommonFileCacheManager.getInstance().startScanWithCompletedProgress(extensions, mApkJunkListener);

        // App
        mAppJunkListener = new HSAppJunkCacheManager.AppJunkCacheScanTaskListener() {
            @Override
            public void onStarted() {
                HSLog.d(JunkCleanActivity.TAG, "startJunkScan ### App Junk ### onStarted");
            }

            @Override
            public void onProgressUpdated(int processedCount, HSAppJunkCache hsAppJunkCache) {
                HSLog.d(JunkCleanActivity.TAG, "startJunkScan ### App Junk ### onProgressUpdated processedCount = " + processedCount);
                mJunkInfo.setAppJunkSize(mJunkInfo.getAppJunkSize() + hsAppJunkCache.getSize());
                if (null != scanJunkListener) {
                    scanJunkListener.onScanNameChanged(hsAppJunkCache.getPackageName());
                }
                if (null != scanJunkListener) {
                    scanJunkListener.onScanSizeChanged(JunkCleanConstant.CATEGORY_APP_JUNK, mJunkInfo, false);
                }
            }

            @Override
            public void onSucceeded(List<HSAppJunkCache> junks, long junkSize) {
                HSLog.d(JunkCleanActivity.TAG, "startJunkScan ### App Junk ### onSucceeded junkCount = " + junks.size() + " junkSize = " + junkSize);
                mIsAppJunkScanFinished = true;
                mJunkInfo.setAppJunkSize(junkSize);

                List<AppJunkWrapper> wrappers = new ArrayList<>();
                for (HSAppJunkCache junk : junks) {
                    if (junk.getSize() <= 0) {
                        continue;
                    }

                    boolean sameTypeJunk = false;

                    for (AppJunkWrapper wrapper : wrappers) {
                        if (wrapper.isInstall()) {
                            if (TextUtils.equals(junk.getPackageName(), wrapper.getPackageName())
                                    && TextUtils.equals(junk.getPathType(), wrapper.getPathType())) {
                                wrapper.addJunk(junk);
                                sameTypeJunk = true;
                            }
                        } else {
                            if (TextUtils.equals(junk.getPackageName(), wrapper.getPackageName())) {
                                wrapper.addJunk(junk);
                                sameTypeJunk = true;
                            }
                        }
                    }

                    if (!sameTypeJunk) {
                        AppJunkWrapper wrapper = new AppJunkWrapper(junk);
                        wrappers.add(wrapper);
                    }
                }
                mJunkWrappers.addAll(wrappers);
                if (null != scanJunkListener) {
                    scanJunkListener.onScanSizeChanged(JunkCleanConstant.CATEGORY_APP_JUNK, mJunkInfo, true);
                }
                scanFinished(scanJunkListener, mJunkInfo.getTotalJunkSize(), isContainMemoryScan);
            }

            @Override
            public void onFailed(int failCode, String failMsg) {
                HSLog.d(JunkCleanActivity.TAG, "startJunkScan ### App Junk ### onFailed failCode = " + failCode + " failMsg = " + failMsg);
                mIsAppJunkScanFinished = true;
                if (null != scanJunkListener) {
                    scanJunkListener.onScanSizeChanged(JunkCleanConstant.CATEGORY_APP_JUNK, mJunkInfo, true);
                }
                scanFinished(scanJunkListener, mJunkInfo.getTotalJunkSize(), isContainMemoryScan);
            }
        };
        HSAppJunkCacheManager.getInstance().startScanWithCompletedProgress(mAppJunkListener);

        // Ad
        mPathFileJunkListener = new HSPathFileCacheManager.PathFileCacheScanTaskListener() {
            @Override
            public void onStarted() {
                HSLog.d(JunkCleanActivity.TAG, "startJunkScan @@@ Ad Junk @@@ onStarted");
            }

            @Override
            public void onProgressUpdated(int processedCount, HSPathFileCache hsPathFileCache) {
                HSLog.d(JunkCleanActivity.TAG, "startJunkScan @@@ Ad Junk @@@ onProgressUpdated processedCount = " + processedCount);
                mJunkInfo.setPathFileJunkSize(mJunkInfo.getPathFileJunkSize() + hsPathFileCache.getSize());
                if (null != scanJunkListener) {
                    scanJunkListener.onScanNameChanged(hsPathFileCache.getPath());
                }
                if (null != scanJunkListener) {
                    scanJunkListener.onScanSizeChanged(JunkCleanConstant.CATEGORY_AD_JUNK, mJunkInfo, false);
                }
            }

            @Override
            public void onSucceeded(List<HSPathFileCache> junks, long junkSize) {
                HSLog.d(JunkCleanActivity.TAG, "startJunkScan @@@ Ad Junk @@@ onSucceeded junkCount = " + junks.size() + " junkSize = " + junkSize);
                mIsAdJunkScanFinished = true;
                mJunkInfo.setPathFileJunkSize(junkSize);

                for (HSPathFileCache junk : junks) {
                    if (junk.getSize() <= 0) {
                        continue;
                    }
                    PathRuleJunkWrapper wrapper = new PathRuleJunkWrapper(junk);
                    mJunkWrappers.add(wrapper);
                }
                if (null != scanJunkListener) {
                    scanJunkListener.onScanSizeChanged(JunkCleanConstant.CATEGORY_AD_JUNK, mJunkInfo, true);
                }
                scanFinished(scanJunkListener, mJunkInfo.getTotalJunkSize(), isContainMemoryScan);
            }

            @Override
            public void onFailed(int failCode, String failMsg) {
                HSLog.d(JunkCleanActivity.TAG, "startJunkScan @@@ Ad Junk @@@ onFailed failCode = " + failCode + " failMsg = " + failMsg);
                mIsAdJunkScanFinished = true;
                if (null != scanJunkListener) {
                    scanJunkListener.onScanSizeChanged(JunkCleanConstant.CATEGORY_AD_JUNK, mJunkInfo, true);
                }
                scanFinished(scanJunkListener, mJunkInfo.getTotalJunkSize(), isContainMemoryScan);
            }
        };
        HSPathFileCacheManager.getInstance().startScanWithCompletedProgress(mPathFileJunkListener);

        // Memory
        if (isContainMemoryScan) {
            mMemoryJunkListener = new HSAppMemoryManager.MemoryTaskListener() {
                @Override
                public void onStarted() {
                    HSLog.d(JunkCleanActivity.TAG, "startJunkScan $$$ Memory Junk $$$ onStarted");
                }

                @Override
                public void onProgressUpdated(int processedCount, int total, HSAppMemory memoryApp) {
                    HSLog.d(JunkCleanActivity.TAG, "startJunkScan $$$ Memory Junk $$$ onProgressUpdated processedCount = " + processedCount);
                    mJunkInfo.setMemoryJunkSize(mJunkInfo.getMemoryJunkSize() + memoryApp.getSize());
                    if (null != scanJunkListener) {
                        scanJunkListener.onScanNameChanged(memoryApp.getPackageName());
                    }
                    if (null != scanJunkListener) {
                        scanJunkListener.onScanSizeChanged(JunkCleanConstant.CATEGORY_MEMORY_JUNK, mJunkInfo, false);
                    }
                }

                @Override
                public void onSucceeded(List<HSAppMemory> junks, long junkSize) {
                    HSLog.d(JunkCleanActivity.TAG, "startJunkScan $$$ Memory Junk $$$ onSucceeded junkCount = " + junks.size() + " junkSize = " + junkSize);
                    mIsMemoryJunkScanFinished = true;
                    mJunkInfo.setMemoryJunkSize(junkSize);
                    for (HSAppMemory junk : junks) {
                        if (junk.getSize() <= 0) {
                            continue;
                        }
                        MemoryJunkWrapper wrapper = new MemoryJunkWrapper(junk);
                        mJunkWrappers.add(wrapper);
                    }
                    if (null != scanJunkListener) {
                        scanJunkListener.onScanSizeChanged(JunkCleanConstant.CATEGORY_MEMORY_JUNK, mJunkInfo, true);
                    }
                    scanFinished(scanJunkListener, mJunkInfo.getTotalJunkSize(), true);
                }

                @Override
                public void onFailed(int failCode, String failMsg) {
                    HSLog.d(JunkCleanActivity.TAG, "startJunkScan $$$ Memory Junk $$$ onFailed failCode = " + failCode + " failMsg = " + failMsg);
                    mIsMemoryJunkScanFinished = true;
                    if (null != scanJunkListener) {
                        scanJunkListener.onScanSizeChanged(JunkCleanConstant.CATEGORY_MEMORY_JUNK, mJunkInfo, true);
                    }
                    scanFinished(scanJunkListener, mJunkInfo.getTotalJunkSize(), true);
                }
            };
            HSAppMemoryManager.getInstance().startScanWithCompletedProgress(mMemoryJunkListener);
        } else {
            if (null != scanJunkListener) {
                scanJunkListener.onScanSizeChanged(JunkCleanConstant.CATEGORY_MEMORY_JUNK, mJunkInfo, true);
            }
            scanFinished(scanJunkListener, mJunkInfo.getTotalJunkSize(), false);
        }
    }

    private void scanFinished(ScanJunkListener scanJunkListener, long totalJunkSize, boolean isContainMemoryScan) {
        boolean isFinished = isContainMemoryScan ? mIsSystemCacheJunkScanFinished && mIsSystemApkJunkScanFinished && mIsAppJunkScanFinished && mIsAdJunkScanFinished && mIsMemoryJunkScanFinished :
                mIsSystemCacheJunkScanFinished && mIsSystemApkJunkScanFinished && mIsAppJunkScanFinished && mIsAdJunkScanFinished;
        HSLog.d(JunkCleanActivity.TAG, "isContainMemoryScan = " + isContainMemoryScan + " mIsSystemCacheJunkScanFinished = " + mIsSystemCacheJunkScanFinished + " mIsSystemApkJunkScanFinished = " + mIsSystemApkJunkScanFinished
            + " mIsAppJunkScanFinished = " + mIsAppJunkScanFinished + " mIsAdJunkScanFinished = " + mIsAdJunkScanFinished + " mIsMemoryJunkScanFinished = " + mIsMemoryJunkScanFinished);

        if (isFinished) {
            if (null != scanJunkListener) {
                scanJunkListener.onScanSizeChanged(JunkCleanConstant.CATEGORY_SPECIAL_TOTAL_JUNK, mJunkInfo, true);
            }
            if (null != scanJunkListener) {
                scanJunkListener.onScanFinished(totalJunkSize);
            }
            mIsScanning = false;
        }
    }

    public void stopJunkScan() {
        if (null != mSystemJunkListener) {
            HSAppSysCacheManager.getInstance().stopScan(mSystemJunkListener);
            mSystemJunkListener = null;
        }
        if (null != mApkJunkListener) {
            HSCommonFileCacheManager.getInstance().stopScan(mApkJunkListener);
            mApkJunkListener = null;
        }
        if (null != mAppJunkListener) {
            HSAppJunkCacheManager.getInstance().stopScan(mAppJunkListener);
            mAppJunkListener = null;
        }
        if (null != mPathFileJunkListener) {
            HSPathFileCacheManager.getInstance().stopScan(mPathFileJunkListener);
            mPathFileJunkListener = null;
        }
        if (null != mMemoryJunkListener) {
            HSAppMemoryManager.getInstance().stopScan(mMemoryJunkListener);
            mMemoryJunkListener = null;
        }
        mIsScanning = false;
    }

    public void scanAppCacheJunk(final Activity activity, final PackageItemInfo packageItemInfo, final LauncherTipManager.TipType tiptype) {
        HSAppJunkCacheManager.getInstance().startScanWithoutProgress(new HSAppJunkCacheManager.AppJunkCacheScanTaskListener() {
            @Override
            public void onStarted() {
                HSLog.d(JunkCleanActivity.TAG, "onStarted >>>>");
            }

            @Override
            public void onProgressUpdated(int i, HSAppJunkCache hsAppJunkCache) {
            }

            @Override
            public void onSucceeded(List<HSAppJunkCache> junks, long junkSize) {
                List<AppJunkWrapper> wrappers = new ArrayList<>();
                long junkCacheSize = 0;
                for (HSAppJunkCache appJunkCache : junks) {
                    if (TextUtils.equals(appJunkCache.getPackageName(), packageItemInfo.packageName) && appJunkCache.getSize() != 0) {
                        junkCacheSize += appJunkCache.getSize();
                        AppJunkWrapper wrapper = new AppJunkWrapper(appJunkCache);
                        wrapper.setType(JunkWrapper.TYPE_JUNK_UNINSTALL);
                        wrappers.add(wrapper);
                    }
                }

                junkCacheSize += getAlertCacheRandomSize();

                mJunkInfo.setAppJunkSize(mJunkInfo.getAppJunkSize() + junkSize);
                mJunkWrappers.addAll(wrappers);

                HSLog.d(JunkCleanActivity.TAG, "onSucceeded >>>> junkCacheSize = " + junkCacheSize + " packageName = " + packageItemInfo.packageName);
                if (junkCacheSize > 0) {
                    FormatSizeBuilder junkSizeBuilder = new FormatSizeBuilder(junkCacheSize);
                    final String sizeText = junkSizeBuilder.size + junkSizeBuilder.unit;
                    String appName = packageItemInfo.title == null ? "" : packageItemInfo.title.toString();
                    String contentStr = activity.getString(R.string.clean_uninstall_dialog_content, appName, sizeText);
                    SpannableString contentSpan = new SpannableString(contentStr);
                    int redColor = 0xfffe4a0a;
                    int startIndex = contentStr.indexOf(sizeText);
                    if (startIndex >= 0) {
                        contentSpan.setSpan(new ForegroundColorSpan(redColor), startIndex, startIndex + sizeText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    JunkCleanFloatTip.Data data = new JunkCleanFloatTip.Data();
                    data.title = activity.getString(R.string.clean_title_capital);
                    data.content = contentSpan;
                    data.junkSize = junkCacheSize;
                    if (null != packageItemInfo.title) {
                        data.appName = packageItemInfo.title.toString();
                    }
                    data.iconBitmap = packageItemInfo.iconBitmap;
                    LauncherTipManager.getInstance().showTip(activity, tiptype, data);
                }
            }

            @Override
            public void onFailed(int failCode, String failMsg) {
                HSLog.d(JunkCleanActivity.TAG, "onFailed>>>>");
            }
        }, new Handler(Looper.getMainLooper()));
    }

    public void scanApkCacheJunk(final Activity activity, final String packageName, final LauncherTipManager.TipType tiptype) {
        List<String> extensions = new ArrayList<>();
        extensions.add(JunkCleanConstant.JUNK_TYPE_APK);
        HSCommonFileCacheManager.getInstance().startScanWithCompletedProgress(extensions, new HSCommonFileCacheManager.FileScanTaskListener() {
            @Override
            public void onStarted() {
                HSLog.d(JunkCleanActivity.TAG, "scanApkCacheJunk === Apk Junk === onStarted");
            }

            @Override
            public void onProgressUpdated(int processedCount, int total, HSCommonFileCache commonFileCache) {
                HSLog.d(JunkCleanActivity.TAG, "scanApkCacheJunk === Apk Junk === onProgressUpdated processedCount = " + processedCount + " total = " + total);
                if (null != commonFileCache) {
                    ApkJunkWrapper wrapper = new ApkJunkWrapper(commonFileCache);
                    wrapper.setType(JunkWrapper.TYPE_JUNK_INSTALL);
                    if (!wrapper.isValidApk() || commonFileCache.getSize() <= 0) {
                        return;
                    }
                    mJunkInfo.setApkJunkSize(mJunkInfo.getApkJunkSize() + commonFileCache.getSize());
                    mJunkWrappers.add(wrapper);

                    String appNameClone = Utils.getAppLabel(packageName);
                    String fileName = commonFileCache.getFileName();
                    String apkPackageName = "";
                    if (null != commonFileCache.getApkInfo()) {
                        apkPackageName = commonFileCache.getApkInfo().getApkPackageName();
                    }
                    HSLog.d(JunkCleanActivity.TAG, "scanApkCacheJunk === Apk Junk === onProgressUpdated appNameClone = " + appNameClone + " fileName = " + fileName + " apkPackageName = " + apkPackageName + " packageName = " + packageName);
                    final String appNameText = appNameClone == null ? "" : appNameClone + "'s";
                    if ((!TextUtils.isEmpty(apkPackageName) && apkPackageName.equals(packageName))) {
                        mInstallScanRunnable = () -> {
                            FormatSizeBuilder junkSizeBuilder = new FormatSizeBuilder(commonFileCache.getSize());
                            String appSizeText = activity.getString(R.string.clean_install_dialog_content, appNameText, junkSizeBuilder.size + junkSizeBuilder.unit);
                            JunkCleanFloatTip.Data data = new JunkCleanFloatTip.Data();
                            data.title = activity.getString(R.string.clean_title_capital);
                            data.content = appSizeText;
                            data.iconDrawable = LauncherPackageManager.getInstance().getApplicationIcon(packageName);
                            LauncherTipManager.getInstance().showTip(activity, tiptype, data);
                        };

                    }
                }
            }

            @Override
            public void onSucceeded(Map<String, List<HSCommonFileCache>> map, long junkSize) {
                HSLog.d(JunkCleanActivity.TAG, "scanApkCacheJunk === Apk Junk === onSucceeded junkCount = " + map.size() + " junkSize = " + junkSize);
                if (null != mInstallScanRunnable) {
                    activity.runOnUiThread(mInstallScanRunnable);
                }

            }

            @Override
            public void onFailed(int failCode, String failMsg) {
                HSLog.d(JunkCleanActivity.TAG, "scanApkCacheJunk === Apk Junk === onFailed failCode = " + failCode + " failMsg = " + failMsg);
            }
        }, new Handler(Looper.getMainLooper()));
    }

    public void selectSystemJunk() {
        for (JunkWrapper junkWrapper : getJunkWrappers()) {
            if (junkWrapper.getCategory().equals(SystemJunkWrapper.SYSTEM_JUNK)) {
                junkWrapper.setMarked(true);
            }
        }
    }

    public void startJunkClean() {
        List<HSPathFileCache> cleanPathFileJunkDetail = new ArrayList<>();
        List<HSCommonFileCache> cleanApkJunkDetail = new ArrayList<>();
        List<HSAppSysCache> cleanSystemJunkDetail = new ArrayList<>();
        List<HSAppJunkCache> cleanAppJunkDetail = new ArrayList<>();
        List<HSAppMemory> cleanMemoryJunkDetail = new ArrayList<>();

        List<JunkWrapper> cleanJunkWrappers = new ArrayList<>();

        for (JunkWrapper junkWrapper : getJunkWrappers()) {
            if (!junkWrapper.isMarked()) {
                continue;
            }

            switch (junkWrapper.getCategory()) {
                case SystemJunkWrapper.SYSTEM_JUNK:
                    cleanSystemJunkDetail.add(((SystemJunkWrapper) junkWrapper).getJunk());
                    break;

                case AppJunkWrapper.APP_JUNK:
                    for (HSAppJunkCache junk : ((AppJunkWrapper) junkWrapper).getJunks()) {
                        cleanAppJunkDetail.add(junk);
                    }
                    break;

                case ApkJunkWrapper.APK_JUNK:
                    cleanApkJunkDetail.add(((ApkJunkWrapper) junkWrapper).getJunk());
                    break;

                case PathRuleJunkWrapper.PATH_RULE_JUNK:
                    cleanPathFileJunkDetail.add(((PathRuleJunkWrapper) junkWrapper).getJunk());
                    break;

                case MemoryJunkWrapper.MEMORY_JUNK:
                    cleanMemoryJunkDetail.add(((MemoryJunkWrapper) junkWrapper).getJunk());
                    break;
            }
            cleanJunkWrappers.add(junkWrapper);
        }

        for (JunkWrapper cleanJunkWrapper : cleanJunkWrappers) {
            if (null != cleanJunkWrapper) {
                JunkManager.getInstance().getJunkWrappers().remove(cleanJunkWrapper);
            }
        }

        JunkCleanConstant.sIsJunkCleaned = true;
        JunkCleanConstant.sIsTotalCleaned = false;

        if (cleanSystemJunkDetail.size() > 0) {
            HSAppSysCacheManager.getInstance().startCleanExternalCache(cleanSystemJunkDetail, new HSAppSysCacheManager.AppSysCacheTaskListener() {
                @Override
                public void onStarted() {
                    HSLog.d("HSAppSysCacheManager ExternalCache onStarted");
                }

                @Override
                public void onProgressUpdated(int i, int i1, HSAppSysCache hsAppSysCache) {
                }

                @Override
                public void onSucceeded(List<HSAppSysCache> list, long l) {
                    HSLog.d("HSAppSysCacheManager ExternalCache onScanCompleted");
                }

                @Override
                public void onFailed(int i, String s) {
                    HSLog.d("HSAppSysCacheManager ExternalCache onFailed" + " failCode:" + i + " failMsg:" + s);
                }
            });
        }

        HSAppSysCacheManager.getInstance().startCleanInternalCache(new HSAppSysCacheManager.AppInternalSysCacheCleanTaskListener() {
            @Override
            public void onStarted() {
                HSLog.d("HSAppSysCacheManager InternalCache onStarted");
            }

            @Override
            public void onProgressUpdated(int i, int i1, HSAppSysCache hsAppSysCache) {
            }

            @Override
            public void onSucceeded(long l) {
                HSLog.d("HSAppSysCacheManager InternalCache onScanCompleted");
            }

            @Override
            public void onFailed(int i, String s) {
                HSLog.d("HSAppSysCacheManager InternalCache onFailed" + " failCode:" + i + " failMsg:" + s);
            }
        });

        if (cleanAppJunkDetail.size() > 0) {
            HSAppJunkCacheManager.getInstance().startClean(cleanAppJunkDetail, new HSAppJunkCacheManager.AppJunkCacheCleanTaskListener() {
                @Override
                public void onStarted() {
                    HSLog.d("HSAppJunkCacheManager onStarted");
                }

                @Override
                public void onProgressUpdated(int i, int i1, HSAppJunkCache hsAppJunkCache) {
                }

                @Override
                public void onSucceeded(List<HSAppJunkCache> list, long l) {
                    HSLog.d("HSAppJunkCacheManager onScanCompleted");
                }

                @Override
                public void onFailed(int i, String s) {
                    HSLog.d("HSAppJunkCacheManager onFailed" + " failCode:" + i + " failMsg:" + s);
                }
            });
        }

        if (cleanPathFileJunkDetail.size() > 0) {
            HSPathFileCacheManager.getInstance().startClean(cleanPathFileJunkDetail, new HSPathFileCacheManager.PathFileCacheTaskListener() {
                @Override
                public void onStarted() {
                    HSLog.d("HSPathFileCacheManager onStarted");
                }

                @Override
                public void onProgressUpdated(int i, int i1, HSPathFileCache hsPathFileCache) {
                }

                @Override
                public void onSucceeded(List<HSPathFileCache> list, long l) {
                    HSLog.d("HSPathFileCacheManager onScanCompleted");
                }

                @Override
                public void onFailed(int i, String s) {
                    HSLog.d("HSPathFileCacheManager onFailed" + " failCode:" + i + " failMsg:" + s);
                }
            });
        }

        if (cleanApkJunkDetail.size() > 0) {
            HSCommonFileCacheManager.getInstance().startClean(cleanApkJunkDetail, new HSCommonFileCacheManager.FileCleanTaskListener() {
                @Override
                public void onStarted() {
                    HSLog.d("HSCommonFileCacheManager onStarted");
                }

                @Override
                public void onProgressUpdated(int i, int i1, HSCommonFileCache hsCommonFileCache) {

                }

                @Override
                public void onSucceeded(List<HSCommonFileCache> list, long l) {
                    HSLog.d("HSCommonFileCacheManager onScanCompleted");
                }

                @Override
                public void onFailed(int i, String s) {
                    HSLog.d("HSCommonFileCacheManager onFailed" + " failCode:" + i + " failMsg:" + s);
                }
            });
        }

        if (cleanMemoryJunkDetail.size() > 0) {
            HSAppMemoryManager.getInstance().startClean(cleanMemoryJunkDetail, new HSAppMemoryManager.MemoryTaskListener() {
                @Override
                public void onStarted() {
                    HSLog.d("HSAppMemoryManager onFailed");
                }

                @Override
                public void onProgressUpdated(int i, int i1, HSAppMemory hsAppMemory) {
                }

                @Override
                public void onSucceeded(List<HSAppMemory> list, long l) {
                    HSLog.d("HSAppMemoryManager onScanCompleted");
                }

                @Override
                public void onFailed(int i, String s) {
                    HSLog.d("HSAppMemoryManager onFailed" + " failCode:" + i + " failMsg:" + s);
                }
            });
        }
    }

    public long getHiddenSystemJunkSize() {
        long selectedSize = 0;
        for (JunkWrapper junkWrapper : mJunkWrappers) {
            if (SystemJunkWrapper.SYSTEM_JUNK.equals(junkWrapper.getCategory())) {
                selectedSize += junkWrapper.getSize();
            }
        }
        return selectedSize;
    }

    public void removeItem(JunkWrapper junkWrapper) {
        if (mJunkWrappers.contains(junkWrapper)) {
            mJunkWrappers.remove(junkWrapper);
        }
    }

    public Integer[] getColors(long cleanBeforeJunkSize, long cleanAfterJunkSize) {
        if (cleanBeforeJunkSize > JunkCleanConstant.DANGER_JUNK_SIZE) {
            if (cleanAfterJunkSize > JunkCleanConstant.DANGER_JUNK_SIZE) {
                return new Integer[] {JunkCleanConstant.PRIMARY_RED, JunkCleanConstant.PRIMARY_RED} ;
            } else if (cleanAfterJunkSize > JunkCleanConstant.NORMAL_JUNK_SIZE) {
                return new Integer[] {JunkCleanConstant.PRIMARY_RED, JunkCleanConstant.PRIMARY_YELLOW } ;
            } else {
                return new Integer[] {JunkCleanConstant.PRIMARY_RED, JunkCleanConstant.PRIMARY_YELLOW, JunkCleanConstant.PRIMARY_BLUE} ;
            }
        } else if (cleanBeforeJunkSize > JunkCleanConstant.NORMAL_JUNK_SIZE) {
            if (cleanAfterJunkSize > JunkCleanConstant.NORMAL_JUNK_SIZE) {
                return new Integer[] {JunkCleanConstant.PRIMARY_YELLOW, JunkCleanConstant.PRIMARY_YELLOW} ;
            } else {
                return new Integer[] {JunkCleanConstant.PRIMARY_YELLOW, JunkCleanConstant.PRIMARY_BLUE} ;
            }
        }
        return new Integer[] {JunkCleanConstant.PRIMARY_BLUE, JunkCleanConstant.PRIMARY_BLUE} ;
    }

    public int getColor(long... junkSizes) {
        long junkSize = (junkSizes == null || junkSizes.length == 0)
                ? getTotalJunkSize() : junkSizes[0];

        if (junkSize > JunkCleanConstant.DANGER_JUNK_SIZE) {
            return JunkCleanConstant.PRIMARY_RED;
        }

        if (junkSize > JunkCleanConstant.NORMAL_JUNK_SIZE) {
            return JunkCleanConstant.PRIMARY_YELLOW;
        }

        return JunkCleanConstant.PRIMARY_BLUE;
    }

    private int getSystemCacheCountRandom() {
        return (int)(Math.random() * 8) + 7;
    }

    private long getAlertCacheRandomSize() {
        mExtendRandomSize = (long) (Math.random() * 3 * 1024 * 1024);
        mExtendAdRandomSize = (long) (Math.random() * mExtendRandomSize * 0.5f);
        return mExtendRandomSize;
    }

    public long getAlertAppCacheRandomSize() {
        return mExtendRandomSize - mExtendAdRandomSize;
    }

    public long getAlertAdCacheRandomSize() {
        return mExtendAdRandomSize;
    }

    public void clear() {
        mJunkWrappers.clear();
        mJunkInfo.clear();
        mIsSystemCacheJunkScanFinished = false;
        mIsSystemApkJunkScanFinished = false;
        mIsAppJunkScanFinished = false;
        mIsAdJunkScanFinished = false;
        mIsMemoryJunkScanFinished = false;
        mSystemCacheAppCount = 0;
        HSLog.d(JunkCleanActivity.TAG_TEST, "JunkManager *** clear = " + Log.getStackTraceString(new Throwable()));
    }

}
