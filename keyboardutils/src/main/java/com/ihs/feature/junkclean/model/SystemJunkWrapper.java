package com.ihs.feature.junkclean.model;

import android.support.annotation.NonNull;

import com.ihs.device.clean.junk.cache.app.sys.HSAppSysCache;

public class SystemJunkWrapper extends JunkWrapper {

    public static final String SYSTEM_JUNK = "SYSTEM_JUNK";

    private HSAppSysCache junk;

    public SystemJunkWrapper(@NonNull HSAppSysCache junk) {
        this.junk = junk;
    }

    public HSAppSysCache getJunk() {
        return junk;
    }

    @Override
    public String getPackageName() {
        return junk.getPackageName();
    }

    @Override
    public String getDescription() {
        return junk.getAppName();
    }

    @Override
    public String getCategory() {
        return SYSTEM_JUNK;
    }

    @Override
    public long getSize() {
        return junk.getSize();
    }
}
