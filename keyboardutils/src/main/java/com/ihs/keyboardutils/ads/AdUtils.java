package com.ihs.keyboardutils.ads;

import com.kc.utils.KCAnalytics;

/**
 * Created by yanxia on 2018/1/10.
 */

public class AdUtils {
    public static final String FACEBOOK_VENDOR_NAME = "FACEBOOKNATIVE";

    public static void logAdLoad(String placement) {
        KCAnalytics.logEvent("KCAds_Load", "ad_placement", placement);
    }

    public static void logAdShow(String placement) {
        KCAnalytics.logEvent("KCAds_Show", "ad_placement", placement);
    }

    public static void logAdClick(String placement) {
        KCAnalytics.logEvent("KCAds_Click", "ad_placement", placement);
    }
}
