package com.ihs.keyboardutils.utils;

import com.ihs.app.analytics.HSAnalytics;

import java.util.HashMap;

public class KCAnalyticUtil {

    public static void logEvent(String action) {
        HSAnalytics.logEvent(action);
    }


    public static void logEvent(String action,String label) {
        HashMap<String, String> valueMap = new HashMap<>();
        valueMap.put(action,label);
        HSAnalytics.logEvent(action,valueMap);
    }

    public static void logEvent(String action, String label, String installType) {
        HashMap<String, String> valueMap = new HashMap<>();
        if (label != null) {
            valueMap.put(action, label);
        }
        if (installType != null) {
            valueMap.put("install_type", installType);
        }
        HSAnalytics.logEvent(action, valueMap);
    }
}
