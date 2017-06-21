package com.ihs.feature.junkclean.model;

import android.text.TextUtils;

import com.ihs.feature.common.Utils;


public abstract class JunkWrapper {

    public static final int TYPE_JUNK_CLEANER = 0;
    public static final int TYPE_JUNK_INSTALL = 1;
    public static final int TYPE_JUNK_UNINSTALL = 2;

    private boolean marked = true;
    private int type = TYPE_JUNK_CLEANER;

    public String getAppName() {
        String packageName = getPackageName();
        return TextUtils.isEmpty(packageName) ? "" : Utils.getAppLabel(packageName);
    }

    public abstract String getPackageName();

    public abstract String getDescription();

    public abstract String getCategory();

    public abstract long getSize();

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return (getCategory() + getPackageName()).hashCode();
    }

}
