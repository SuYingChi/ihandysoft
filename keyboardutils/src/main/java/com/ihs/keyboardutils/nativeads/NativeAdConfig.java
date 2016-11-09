package com.ihs.keyboardutils.nativeads;

import com.ihs.commons.config.HSConfig;

/**
 * Created by ihandysoft on 16/11/9.
 */

public class NativeAdConfig {

    public static int getNativeAdFrequency() {
        return HSConfig.optInteger(0, "Application", "NativeAds", "FetchAdInterval");
    }
}
