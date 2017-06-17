package com.ihs.feature.junkclean.model;

public class JunkInfo {

    private long mSystemJunkSize;
    private long mPathFileJunkSize;
    private long mMemoryJunkSize;
    private long mAppJunkSize;
    private long mApkJunkSize;

    public long getTotalJunkSize() {
        return mSystemJunkSize + mMemoryJunkSize + mApkJunkSize + mAppJunkSize + mPathFileJunkSize;
    }

    public long getPathFileJunkSize() {
        return mPathFileJunkSize;
    }

    public void setPathFileJunkSize(long mPathFileJunkSize) {
        this.mPathFileJunkSize = mPathFileJunkSize;
    }

    public long getSystemJunkSize() {
        return mSystemJunkSize;
    }

    public void setSystemJunkSize(long mSystemJunkSize) {
        this.mSystemJunkSize = mSystemJunkSize;
    }

    public long getMemoryJunkSize() {
        return mMemoryJunkSize;
    }

    public void setMemoryJunkSize(long mMemoryJunkSize) {
        this.mMemoryJunkSize = mMemoryJunkSize;
    }

    public long getAppJunkSize() {
        return mAppJunkSize;
    }

    public void setAppJunkSize(long mAppJunkSize) {
        this.mAppJunkSize = mAppJunkSize;
    }

    public long getApkJunkSize() {
        return mApkJunkSize;
    }

    public void setApkJunkSize(long mApkJunkSize) {
        this.mApkJunkSize = mApkJunkSize;
    }

    public void clear() {
        mSystemJunkSize = 0;
        mPathFileJunkSize = 0;
        mMemoryJunkSize = 0;
        mAppJunkSize = 0;
        mApkJunkSize = 0;
    }

}
