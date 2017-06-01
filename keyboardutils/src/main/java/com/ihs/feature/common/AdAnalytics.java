package com.ihs.feature.common;

import com.acb.nativeads.AcbNativeAdAnalytics;
import com.acb.nativeads.BuildConfig;
import com.ihs.app.analytics.HSAnalytics;

import java.util.HashMap;

public class AdAnalytics {

    private static final HashMap<String, String> sEventNamePrefixMap = new HashMap<>(32);

    static {
        HashMap<String, String> map = sEventNamePrefixMap;

        /**
         * 7 Flurry Key for ONE Flurry Event at most
         **/

        // Events that uses AcbNativeAdAnalytics#logAppViewEvent directly
        map.put(AdPlacements.AD_PLACEMENT_NAME_CHARGING_SCREEN, "");
        // boost ad, boost has been removed and this placement CAN NOT be used by others
        map.put(AdPlacements.LOCKER_NATIVE_AD_PLACEMENT_NAME, "");
        map.put(AdPlacements.LUCKY_NATIVE_AD_PLACEMENT_NAME, "");
        // boost plus ad, boost plus has been removed and this placement CAN NOT be used by others
        // battery ad, battery has been removed and this placement CAN NOT be used by others
        map.put(AdPlacements.ALL_APPS_FULL_INTERSTITIAL_AD_PLACEMENT_NAME, "");

        // Events that add a prefix to event name "AcbAdNative_Viewed_In_App"
        map.put(AdPlacements.THEME_NATIVE_AD_PLACEMENT_NAME, "Launcher");
        map.put(AdPlacements.WALLPAPER_NATIVE_AD_PLACEMENT_NAME, "Launcher");
        // menu ad, menu has been removed and this placement CAN NOT be used by others
        // folder ad, menu has been removed and this placement CAN NOT be used by others
        // app drawer ad, menu has been removed and this placement CAN NOT be used by others
        map.put(AdPlacements.MOMENT_NATIVE_AD_PLACEMENT_HUB, "Launcher");
        map.put(AdPlacements.NEARBY_NATIVE_AD_PLACEMENT_NAME, "Launcher");

        // junk cleaner ad, junk cleaner has been removed and this placement CAN NOT be used by others
        map.put(AdPlacements.MOMENT_NEARBY_NATIVE_AD_PLACEMENT_NAME, "Launcher2");
        map.put(AdPlacements.FOLDER_CLOSE_AD_PLACEMENT_NAME, "Launcher2");
        map.put(AdPlacements.WALLPAPER_PREVIEW_NATIVE_AD_PLACEMENT_NAME, "Launcher2");
        map.put(AdPlacements.NEWS_NATIVE_AD_PLACEMENT_NAME, "Launcher2");
        map.put(AdPlacements.SHARED_POOL_NATIVE_AD_FLURRY_KEY_VIEW_IN_APP_SEVEN_IN_ONE, "Launcher2");
        map.put(AdPlacements.FOLDER_ALL_APPS_AD_PLACEMENT_NAME, "Launcher2");

        map.put(AdPlacements.DESKTOP_WIDGET_AD_PLACEMENT_NAME, "Launcher3");
        map.put(AdPlacements.SEARCH_BAR_NATIVE_AD_PLACEMENT_NAME, "Launcher3");
        map.put(AdPlacements.SEARCH_NEWS_NATIVE_AD_PLACEMENT_NAME, "Launcher3");
        map.put(AdPlacements.WALLPAPER_THEME_EXIT_AD_PLACEMENT_NAME, "Launcher3");

    }

    /**
     * 记录一次广告展示机会使用的利用率，目的有二：1. 是为了统计广告库提供广告的能力 2.结合功能相关事件来对比功能次数和广告展示次数的关系
     * 功能和 AcbNativeAdAnalytics 的 logAppViewEvent 函数一模一样，只不过那个函数的参数个数已经用满了，所以得新开一些事件来记录
     *
     * @param placementName 广告的 placementID
     * @param success       本次广告展示的机会成功取到广告并展示传 true，本次展示广告机会因广告获取失败或者获取太慢而未展示则传 false
     */
    public static void logAppViewEvent(String placementName, boolean success) {
        String prefix = sEventNamePrefixMap.get(placementName);
        if (prefix == null) {
            if (BuildConfig.DEBUG) {
                throw new IllegalArgumentException("Unregistered ad placement name " + placementName + ". "
                        + "Forget to add this placement name to AdAnalytics#sEventNamePrefixMap?");
            }
            return;
        }
        if (prefix.isEmpty()) {
            AcbNativeAdAnalytics.logAppViewEvent(placementName, success);
        } else {
            HSAnalytics.logEvent(prefix + "_AcbAdNative_Viewed_In_App", placementName, String.valueOf(success));
        }
    }
}
