package com.ihs.feature.junkclean.model;

import android.support.annotation.NonNull;

import com.ihs.device.clean.memory.HSAppMemory;

public class MemoryJunkWrapper extends JunkWrapper {

    public static final String MEMORY_JUNK = "MEMORY_JUNK";

    private HSAppMemory mHSAppMemory;

    public MemoryJunkWrapper(@NonNull HSAppMemory junk) {
        this.mHSAppMemory = junk;
    }

    public HSAppMemory getJunk() {
        return mHSAppMemory;
    }

    @Override
    public String getPackageName() {
        return mHSAppMemory.getPackageName();
    }

    @Override
    public String getDescription() {
        return mHSAppMemory.getAppName();
    }

    @Override
    public String getCategory() {
        return MEMORY_JUNK;
    }

    @Override
    public long getSize() {
        return mHSAppMemory.getSize();
    }

}
