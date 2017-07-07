package com.ihs.feature.junkclean.model;

import android.support.annotation.NonNull;

import com.ihs.device.clean.junk.cache.nonapp.pathrule.HSPathFileCache;

public class PathRuleJunkWrapper extends JunkWrapper {

    public static final String PATH_RULE_JUNK = "PATH_RULE_JUNK";

    private HSPathFileCache junk;

    public PathRuleJunkWrapper(@NonNull HSPathFileCache junk) {
        this.junk = junk;
    }

    public HSPathFileCache getJunk() {
        return junk;
    }

    @Override
    public String getPackageName() {
        return junk.getPath();  //HSPathFileCache无pPackageName，这里做唯一区分标记
    }

    @Override
    public String getDescription() {
        String[] split = junk.getPath().split("/");
        return split[split.length - 1];
    }

    @Override
    public String getCategory() {
        return PATH_RULE_JUNK;
    }

    @Override
    public long getSize() {
        return junk.getSize();
    }

}
