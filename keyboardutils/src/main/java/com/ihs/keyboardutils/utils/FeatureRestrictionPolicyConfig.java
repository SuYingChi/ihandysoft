package com.ihs.keyboardutils.utils;

import com.ihs.chargingscreen.utils.FeatureDelayReleaseUtil;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.countrycode.HSCountryCodeManager;

import java.util.List;
import java.util.Map;

public final class FeatureRestrictionPolicyConfig {
    private static final String CONFIG_KEY_APPLICAITON = "Application";
    private static final String CONFIG_KEY_FEATURE_RESTRICTION = "FeatureRestriction";
    private static final String CONFIG_KEY_DELAY_HOURS = "HoursFromFirstUse";
    private static final String CONFIG_KEY_REGION_BLACKLIST = "RegionException";

    public static boolean isFeatureRestricted(String featureName) {
        Map<String, ?> featureRestrictionConfig = HSConfig.getMap(CONFIG_KEY_APPLICAITON, CONFIG_KEY_FEATURE_RESTRICTION, featureName);
        if (featureRestrictionConfig == null || featureRestrictionConfig.size() == 0) {
            return false;
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

        return false;
    }

}
