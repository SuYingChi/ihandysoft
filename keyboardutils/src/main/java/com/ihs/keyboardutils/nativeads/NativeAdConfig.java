package com.ihs.keyboardutils.nativeads;

import com.ihs.commons.config.HSConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ihandysoft on 16/11/9.
 */

public class NativeAdConfig {

    public static int getNativeAdFrequency() {
        return HSConfig.optInteger(0, "Application", "NativeAds", "FetchAdInterval");
    }

    static List getDisabledIconPools() {
        return HSConfig.getList("Application", "NativeAds", "DisabledIconPools");
    }

    public static List<String> getAvailablePoolNames(){
        List<String> poolNames = new ArrayList<>();
        List<?> disabledPools = HSConfig.getList("Application", "NativeAds", "DisabledPools");
        for (Map.Entry entry : HSConfig.getMap("nativeAdsPool").entrySet()) {
            if ((entry.getValue() instanceof Map) && !disabledPools.contains(entry.getKey().toString())) {
                poolNames.add(entry.getKey().toString());
            }
        }
        return poolNames;
    }
}
