package com.kc.utils;

import com.crashlytics.android.answers.CustomEvent;
import com.ihs.app.analytics.HSAnalytics;

import java.util.Map;

public class KCAnalytics {
    public static void logEvent(String eventID, String... vars) {
        logFabricEvent(eventID, vars);

        logFlurryEvent(eventID, vars);
    }

    public static void logEvent(String eventID, Map<String, String> params) {
        String[] paramArray = new String[params.size() * 2];
        int index = 0;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            paramArray[index ++] = entry.getKey();
            paramArray[index ++] = entry.getValue();
        }
        logEvent(eventID, paramArray);
    }

    public static void logFlurryEvent(String eventID, String... vars) {
        HSAnalytics.logEvent(eventID, vars);
    }

    public static void logFabricEvent(String eventID, String... vars) {
        CustomEvent event = new CustomEvent(eventID);
        String key = null;
        for (String var : vars) {
            if (key == null) {
                key = var;
            } else {
                event.putCustomAttribute(key, var);
                key = null;
            }
        }

//        Answers.getInstance().logCustom(event);
    }
}
