package com.kc.utils;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.ihs.app.analytics.HSAnalytics;

public class KCAnalytics {
    public static void logEvent(String eventID, String... vars) {
        logFabricEvent(eventID, vars);

        logFlurryEvent(eventID, vars);
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

        Answers.getInstance().logCustom(event);
    }
}
