package com.ihs.feature.boost;

import com.ihs.feature.common.ITipInfo;
import com.ihs.feature.tip.LauncherTipManager;
import com.ihs.feature.ui.LauncherFloatWindowManager;

import net.appcloudbox.ads.base.AcbNativeAd;


public class BoostTipInfo implements ITipInfo {
    @Override
    public LauncherTipManager.TipType getTipType() {
        return LauncherTipManager.TipType.BOOST_TIP;
    }

    @Override
    public LauncherTipManager.ResultType wantToShow(LauncherTipManager.TipEnvironment env) {
        if (env.hasTipShow() || !isValidExtras(env)) {
            return LauncherTipManager.ResultType.NOT_SHOW;
        } else {
            return LauncherTipManager.ResultType.SHOW;
        }
    }

    @Override
    public void show(LauncherTipManager.TipEnvironment env) {
        if (isValidExtras(env)) {
            LauncherFloatWindowManager.getInstance().showBoostTip(env.context, (BoostType) env.extras[0],
                    (Integer) env.extras[1], (BoostSource) env.extras[2], (AcbNativeAd) env.extras[3]);
        } else {
            LauncherTipManager.getInstance().notifyDismiss();
        }
    }

    @Override
    public void dismiss() {
        LauncherFloatWindowManager.getInstance().removeBoostTip();
    }

    @Override
    public boolean isValidExtras(LauncherTipManager.TipEnvironment env) {
        if (env.requestShowTipType == getTipType()) {
            if (env.extras != null && env.extras.length >= 4) {
                if (env.extras[0] instanceof BoostType && env.extras[1] instanceof Integer
                        && env.extras[2] instanceof BoostSource
                        && (env.extras[3] == null || env.extras[3] instanceof AcbNativeAd)) {
                    return true;
                }
            }
        }
        return false;
    }
}
