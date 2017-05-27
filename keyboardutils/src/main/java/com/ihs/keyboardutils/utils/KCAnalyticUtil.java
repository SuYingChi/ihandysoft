package com.ihs.keyboardutils.utils;

import com.ihs.app.analytics.HSAnalytics;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arthur on 17/5/10.
 */

public class KCAnalyticUtil {

    public static void logEvent(String action) {
        HSAnalytics.logEvent(action);
        HSAnalytics.logGoogleAnalyticsEvent("app", "kcLogger", action, null, null, null, null);
    }


    public static void logEvent(String action,String label) {
        HashMap<String, String> valueMap = new HashMap<>();
        valueMap.put(action,label);
        HSAnalytics.logEvent(action,valueMap);
        HSAnalytics.logGoogleAnalyticsEvent("app", "kcLogger", action, label, null, null, null);
    }

    public static void logEvent(String action, String label, Map<String, String> paras) {
        HashMap<String, String> valueMap = new HashMap<>();
        if (label != null) {
            valueMap.put(action, label);
        }
        valueMap.putAll(paras);
        HSAnalytics.logEvent(action, valueMap);

        HashMap<Integer, String> dimensions = new HashMap<>();
        int index = 1;
        for (Map.Entry<String, String> e : valueMap.entrySet()) {
            dimensions.put(index, e.getValue());
            index++;
        }
        HSAnalytics.logGoogleAnalyticsEvent("app", "kcLogger", action, label, null, dimensions, null);
    }
}
