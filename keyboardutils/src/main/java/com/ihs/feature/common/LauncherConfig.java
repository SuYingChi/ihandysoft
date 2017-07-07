package com.ihs.feature.common;

import com.ihs.commons.config.HSConfig;
import com.ihs.keyboardutils.BuildConfig;

import java.util.Map;

/**
 * A wrapper over {@link HSConfig} to provide multilingual and multi-APK support.
 */
@SuppressWarnings("unchecked")
public class LauncherConfig {


    public static boolean getVariantBoolean(String... path) {
        boolean configBool;
        try {
            configBool = HSConfig.getBoolean(path);
        } catch (Exception expected) {
            Map<String, Boolean> boolMap = (Map<String, Boolean>) HSConfig.getMap(path);
            configBool = Boolean.TRUE.equals(boolMap.get(BuildConfig.FLAVOR));
        }
        return configBool;
    }

    public static int getVariantInt(String... path) {
        int configInt;
        try {
            configInt = HSConfig.getInteger(path);
        } catch (Exception expected) {
            Map<String, Integer> intMap = (Map<String, Integer>) HSConfig.getMap(path);
            configInt = intMap.get(BuildConfig.FLAVOR);
        }
        return configInt;
    }

    public static float getVariantFloat(String... path) {
        float configFloat;
        try {
            configFloat = HSConfig.getFloat(path);
        } catch (Exception expected) {
            //noinspection unchecked
            Map<String, Integer> intMap = (Map<String, Integer>) HSConfig.getMap(path);
            configFloat = intMap.get(BuildConfig.FLAVOR);
        }
        return configFloat;
    }
}
