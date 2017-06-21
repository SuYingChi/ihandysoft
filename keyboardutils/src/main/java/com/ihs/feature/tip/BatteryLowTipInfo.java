package com.ihs.feature.tip;

import com.ihs.feature.common.ITipInfo;

public class BatteryLowTipInfo implements ITipInfo {
    @Override
    public LauncherTipManager.TipType getTipType() {
        return LauncherTipManager.TipType.BATTERY_LOW;
    }

    @Override
    public LauncherTipManager.ResultType wantToShow(LauncherTipManager.TipEnvironment env) {
        if (env.hasTipShow() || !isValidExtras(env) || env.returnToLauncherCount < 3) {
            return LauncherTipManager.ResultType.NOT_SHOW;
        } else {
            return LauncherTipManager.ResultType.SHOW;
        }
    }

    @Override
    public void show(LauncherTipManager.TipEnvironment env) {
        if (isValidExtras(env)) {
            new BatteryLowTip(env.context, (Runnable)env.extras[0], (Runnable)env.extras[1], (Runnable)env.extras[2]);
        } else {
            LauncherTipManager.getInstance().notifyDismiss();
        }
    }

    @Override
    public void dismiss() {
        BatteryLowTip.closeNow();
        // TODO
    }

    @Override
    public boolean isValidExtras(LauncherTipManager.TipEnvironment env) {
        if (env.requestShowTipType == getTipType()) {
            if (env.extras != null && env.extras.length == 3) {
                if (env.extras[0] instanceof Runnable && env.extras[1] instanceof Runnable
                        && env.extras[2] instanceof Runnable) {
                    return true;
                }
            }
        }
        return false;
    }
}
