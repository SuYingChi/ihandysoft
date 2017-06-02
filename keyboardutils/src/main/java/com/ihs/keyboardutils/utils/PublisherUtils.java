package com.ihs.keyboardutils.utils;


import com.ihs.app.framework.HSApplication;
import com.ihs.commons.analytics.publisher.HSPublisherMgr;

/**
 * Created by liuzhongtao on 17/5/27.
 *
 */

public class PublisherUtils {
    public static String getInstallType() {
        HSPublisherMgr.PublisherData data = HSPublisherMgr.getPublisherData(HSApplication.getContext());
        String installType;
        if (data.getInstallMode() != HSPublisherMgr.PublisherData.InstallMode.NON_ORGANIC) {
            installType = data.getInstallMode().name();
        } else {
            installType = data.getMediaSource();
        }

        return installType;
    }

    public static boolean isDefault() {
        HSPublisherMgr.PublisherData data = HSPublisherMgr.getPublisherData(HSApplication.getContext());
        return data.isDefault();
    }
}
