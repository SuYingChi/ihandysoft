package com.ihs.feature.common;

import com.kc.utils.KCAnalytics;

import net.appcloudbox.ads.nativeads.AcbNativeAdAnalytics;

import java.util.HashMap;

public class AdAnalytics {

    private static final HashMap<String, String> sEventNamePrefixMap = new HashMap<>(32);

    static {
        HashMap<String, String> map = sEventNamePrefixMap;

        /**
         * 7 Flurry Key for ONE Flurry Event at most
         **/

        // Events that uses AcbNativeAdAnalytics#logAppViewEvent directly
        // boost ad, boost has been removed and this placement CAN NOT be used by others
        // boost plus ad, boost plus has been removed and this placement CAN NOT be used by others
        // battery ad, battery has been removed and this placement CAN NOT be used by others

        // Events that add a prefix to event name "AcbAdNative_Viewed_In_App"
        // theme ad, theme has been removed and this placement CAN NOT be used by others
        // wallpaper ad, wallpaper has been removed and this placement CAN NOT be used by others
        // menu ad, menu has been removed and this placement CAN NOT be used by others
        // folder ad, folder has been removed and this placement CAN NOT be used by others
        // app drawer ad, app drawer has been removed and this placement CAN NOT be used by others
        // hub ad, hub has been removed and this placement CAN NOT be used by others
        // nearby, nearby has been removed and this placement CAN NOT be used by others

        // junk cleaner ad, junk cleaner has been removed and this placement CAN NOT be used by others
        // hub nearby ad, hub nearby has been removed and this placement CAN NOT be used by others
        // folder close ad, folder close has been removed and this placement CAN NOT be used by others
        // wallpaper preview ad, wallpaper preview has been removed and this placement CAN NOT be used by others
        // news ad, news has been removed and this placement CAN NOT be used by others
        // app drawer and folder ad, app drawer and folder has been removed and this placement CAN NOT be used by others

        // widget, widget has been removed and this placement CAN NOT be used by others
        // search1, search1 has been removed and this placement CAN NOT be used by others
        // search news ad, search news has been removed and this placement CAN NOT be used by others
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
        if (prefix == null || prefix.isEmpty()) {
            AcbNativeAdAnalytics.logAppViewEvent(placementName, success);
        } else {
            KCAnalytics.logEvent(prefix + "_AcbAdNative_Viewed_In_App", placementName, String.valueOf(success));
        }
    }
}
