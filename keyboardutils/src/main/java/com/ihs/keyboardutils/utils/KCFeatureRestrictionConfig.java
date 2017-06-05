package com.ihs.keyboardutils.utils;

import com.ihs.chargingscreen.utils.FeatureDelayReleaseUtil;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.countrycode.HSCountryCodeManager;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public final class KCFeatureRestrictionConfig {
    private static final String CONFIG_KEY_APPLICAITON = "Application";
    private static final String CONFIG_KEY_FEATURE_RESTRICTION = "FeatureRestriction";
    private static final String CONFIG_KEY_DELAY_HOURS = "HoursFromFirstUse";
    private static final String CONFIG_KEY_REGION_BLACKLIST = "RegionException";
    private static final String CONFIG_KEY_TIME_ZONE_BLACKLIST = "TimeZoneException";
    private static final String CONFIG_KEY_ENABLED = "Enabled";

    public static boolean isFeatureRestricted(String featureName) {
        Map<String, ?> featureRestrictionConfig = HSConfig.getMap(CONFIG_KEY_APPLICAITON, CONFIG_KEY_FEATURE_RESTRICTION, featureName);
        if (featureRestrictionConfig == null || featureRestrictionConfig.size() == 0) {
            return false;
        }

        boolean enabled = HSConfig.optBoolean(true, CONFIG_KEY_APPLICAITON, CONFIG_KEY_FEATURE_RESTRICTION, featureName, CONFIG_KEY_ENABLED);
        if (!enabled) {
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

        if (timeZoneBlacklist != null && timeZoneBlacklist.contains(hourOffset)) {
            return true;
        }

        return false;
    }

}
