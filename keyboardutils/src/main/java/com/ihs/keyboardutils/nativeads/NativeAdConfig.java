package com.ihs.keyboardutils.nativeads;

import com.ihs.commons.config.HSConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NativeAdConfig {

    static List getDisabledIconPools() {
        return HSConfig.getList("Application", "NativeAds", "DisabledIconPools");
    }

    public static List<String> getAvailablePoolNames() {
        List<String> poolNames = new ArrayList<>();
        for (Map.Entry entry : HSConfig.getMap("nativeAds").entrySet()) {
            if ((entry.getValue() instanceof Map)) {
                poolNames.add(entry.getKey().toString());
            }
        }
        return poolNames;
    }

    public static boolean canShowIconAd() {
        return HSConfig.optBoolean(true, "Application", "NativeAds", "ShowIconAd");
    }
}
