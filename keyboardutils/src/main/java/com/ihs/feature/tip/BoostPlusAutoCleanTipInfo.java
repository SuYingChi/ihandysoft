package com.ihs.feature.tip;

import com.ihs.feature.common.LauncherTipManager;
import com.ihs.feature.common.TipInfo;

/**
 * Auto-clean in Boost-plus
 */

public class BoostPlusAutoCleanTipInfo extends TipInfo {
    @Override
    public LauncherTipManager.TipType getTipType() {
        return LauncherTipManager.TipType.AUTO_CLEAN_AUTHORIZE;
    }

    @Override
    public LauncherTipManager.ResultType wantToShow(LauncherTipManager.TipEnvironment env) {
        return LauncherTipManager.ResultType.FOCUS_SHOW;
    }

    @Override
    public void show(LauncherTipManager.TipEnvironment env) {
        BoostPlusAutoCleanTip.show(env.context, (int) env.extras[0]);
    }

    @Override
    public void dismiss() {
        BoostPlusAutoCleanTip.closeNow();
    }
}
