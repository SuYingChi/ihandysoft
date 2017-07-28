package com.ihs.feature.tip;


import com.ihs.feature.common.TipInfo;

public class NotificationTipInfo extends TipInfo {
    @Override
    public LauncherTipManager.TipType getTipType() {
        return LauncherTipManager.TipType.NOTIFICATION_TIP;
    }

    @Override
    public LauncherTipManager.ResultType wantToShow(LauncherTipManager.TipEnvironment env) {
        if (!env.hasTipShow()) {
            return LauncherTipManager.ResultType.SHOW;
        }
        return LauncherTipManager.ResultType.SHOW_AFTER_CURRENT;
    }
}
