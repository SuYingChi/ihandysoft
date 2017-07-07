package com.ihs.feature.tip;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.chargingscreen.activity.ChargingFullScreenAlertDialogActivity;
import com.ihs.feature.common.ITipInfo;

/**
 * Created by lz on 3/10/17.
 */
public class ChargingScreenTipInfo implements ITipInfo {

    @Override public LauncherTipManager.TipType getTipType() {
        return LauncherTipManager.TipType.CHARGING_SCREEN_GUIDE;
    }

    @Override
    public LauncherTipManager.ResultType wantToShow(LauncherTipManager.TipEnvironment env) {
        if (shouldShow()) {
            return LauncherTipManager.ResultType.SHOW;
        } else {
            return LauncherTipManager.ResultType.NOT_SHOW;
        }
    }

    @Override public void show(LauncherTipManager.TipEnvironment env) {
        new ChargingScreenTip(env.context).show();
        HSAnalytics.logEvent("Alert_LockerScreen_Shown");
        ChargingFullScreenAlertDialogActivity.startChargingAlert();
    }

    @Override public void dismiss() {

    }

    @Override public boolean isValidExtras(LauncherTipManager.TipEnvironment env) {
        return true;
    }

    private boolean shouldShow() {
        return false;//ChargingScreenUtils.shouldDialogGuideShow();
    }
}
