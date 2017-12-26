package com.ihs.feature.junkclean.model;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.device.clean.junk.cache.nonapp.commonrule.HSApkInfo;
import com.ihs.device.clean.junk.cache.nonapp.commonrule.HSCommonFileCache;
import com.ihs.device.common.utils.Utils;

import java.io.File;

public class ApkJunkWrapper extends JunkWrapper {

    public static final String APK_JUNK = "APK_JUNK";
    private HSCommonFileCache mHSCommonFileCache;
    private HSApkInfo mHSApkInfo = null;

    public ApkJunkWrapper(@NonNull HSCommonFileCache junk) {
        this.mHSCommonFileCache = junk;
        if (junk.isExtension("apk")) {
            mHSApkInfo = junk.getApkInfo();
        }

        if (mHSApkInfo != null && mHSApkInfo.hasInstalled() && mHSApkInfo.getApkVersionCode() > mHSApkInfo.getInstalledVersionCode()) {
            setMarked(false);
        }
    }

    public HSCommonFileCache getJunk() {
        return mHSCommonFileCache;
    }

    @Override
    public String getPackageName() {
        return mHSApkInfo != null ? mHSApkInfo.getApkPackageName() : "";
    }

    @Override
    public String getDescription() {
        return mHSApkInfo != null ? mHSApkInfo.getApkAppName() : "";
    }

    @Override
    public String getCategory() {
        return APK_JUNK;
    }

    @Override
    public long getSize() {
        return mHSCommonFileCache.getSize();
    }

    public boolean isValidApk() {
        return mHSApkInfo != null && mHSApkInfo.isValidApk();
    }

    public String getApkVersionName() {
        return mHSApkInfo != null ? mHSApkInfo.getApkVersionName() : "";
    }

    public String getApkIconPath() {
        File cachePath = HSApplication.getContext().getExternalCacheDir();
        if(cachePath == null) {
            cachePath = HSApplication.getContext().getCacheDir();
        }

        return cachePath.getPath() + File.separator + Utils.getMD5(HSApplication.getContext().getPackageName()) + ".png";
    }

    public boolean isInstall() {
        try {
            String packageName = getPackageName();
            if (TextUtils.isEmpty(packageName)) {
                return false;
            }
            return HSApplication.getContext().getPackageManager().getPackageInfo(getPackageName(), 0) != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

}
