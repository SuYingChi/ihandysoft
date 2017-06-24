package com.ihs.keyboardutils.utils;

import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.FeatureDelayReleaseUtil;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.countrycode.HSCountryCodeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public final class KCFeatureRestrictionConfig {
    private static final String CONFIG_KEY_APPLICAITON = "Application";
    private static final String CONFIG_KEY_FEATURE_RESTRICTION = "FeatureRestriction";
    private static final String CONFIG_KEY_DELAY_HOURS = "HoursFromFirstUse";
    private static final String CONFIG_KEY_REGION_BLACKLIST = "RegionException";
    private static final String CONFIG_KEY_TIME_ZONE_BLACKLIST = "TimeZoneException";
    private static final String CONFIG_KEY_ALWAYS = "Always";

    public static boolean isFeatureRestricted(String featureName) {
        Map<String, ?> featureRestrictionConfig = HSConfig.getMap(CONFIG_KEY_APPLICAITON, CONFIG_KEY_FEATURE_RESTRICTION, featureName);
        if (featureRestrictionConfig == null || featureRestrictionConfig.size() == 0) {
            return false;
        }

        boolean always = HSConfig.optBoolean(false, CONFIG_KEY_APPLICAITON, CONFIG_KEY_FEATURE_RESTRICTION, featureName, CONFIG_KEY_ALWAYS);
        if (always) {
            return true;
        }

        float delayHours = HSConfig.optFloat(0, CONFIG_KEY_APPLICAITON, CONFIG_KEY_FEATURE_RESTRICTION, featureName, CONFIG_KEY_DELAY_HOURS);
        boolean shouldDelay = !FeatureDelayReleaseUtil.checkFeatureReadyToWork(featureName, delayHours);

        if (shouldDelay) {
            return true;
        }

        List<String> regionBlacklist = (List<String>) HSConfig.getList(CONFIG_KEY_APPLICAITON, CONFIG_KEY_FEATURE_RESTRICTION, featureName, CONFIG_KEY_REGION_BLACKLIST);

        if (regionBlacklist != null && regionBlacklist.contains(HSCountryCodeManager.getInstance().getCountryCode())) {
            return true;
        }

        List<Integer> timeZoneBlacklist = (List<Integer>) HSConfig.getList(CONFIG_KEY_APPLICAITON, CONFIG_KEY_FEATURE_RESTRICTION, featureName, CONFIG_KEY_TIME_ZONE_BLACKLIST);

        int hourOffset = TimeZone.getDefault().getRawOffset() / 3600000;

        if (!HSApplication.isDebugging) {
            // 在实际 Release 中，将+8区加入限制名单
            if (timeZoneBlacklist == null) {
                timeZoneBlacklist = new ArrayList<>();
            }
            timeZoneBlacklist.add(8);
        }

        if ((timeZoneBlacklist != null && timeZoneBlacklist.contains(hourOffset))) {
            return true;
        }

        return false;
    }

}
