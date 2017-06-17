package com.ihs.feature.junkclean.model;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import com.ihs.app.framework.HSApplication;
import com.ihs.device.clean.junk.cache.app.nonsys.junk.HSAppJunkCache;

import java.util.ArrayList;
import java.util.List;

public class AppJunkWrapper extends JunkWrapper {

    public static final String APP_JUNK = "APP_JUNK";

    private List<HSAppJunkCache> mHSAppJunkCacheList = new ArrayList<>();

    public AppJunkWrapper(@NonNull HSAppJunkCache junk) {
        mHSAppJunkCacheList.add(junk);
    }

    public void addJunk(HSAppJunkCache junk){
        mHSAppJunkCacheList.add(junk);
    }

    public List<HSAppJunkCache> getJunks() {
        return mHSAppJunkCacheList;
    }

    public String getPathType() {
        return mHSAppJunkCacheList.get(0).getPathType();
    }

    public String getPath() {
        StringBuilder builder = new StringBuilder();
        for (HSAppJunkCache junk : mHSAppJunkCacheList) {
            builder.append(junk.getPath()).append("\n");
        }
        return builder.toString();
    }

    @Override
    public String getPackageName() {
        return mHSAppJunkCacheList.get(0).getPackageName();
    }

    @Override
    public String getDescription() {
        return mHSAppJunkCacheList.get(0).getAppName();
    }

    @Override
    public String getCategory() {
        return APP_JUNK;
    }

    @Override
    public long getSize() {
        long junkSize = 0;
        for (HSAppJunkCache junk : mHSAppJunkCacheList) {
            junkSize += junk.getSize();
        }
        return junkSize;
    }

    public boolean isInstall() {
        try {
            return HSApplication.getContext().getPackageManager().getPackageInfo(getPackageName(), 0) != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

}
