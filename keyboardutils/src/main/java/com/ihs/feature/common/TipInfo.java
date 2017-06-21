package com.ihs.feature.common;


import com.ihs.feature.tip.LauncherTipManager;

public abstract class TipInfo implements ITipInfo {

    @Override
    public void show(LauncherTipManager.TipEnvironment env) {
        LauncherTipManager.getInstance().notifyDismiss();
    }

    @Override
    public void dismiss() {
    }

    @Override
    public boolean isValidExtras(LauncherTipManager.TipEnvironment env) {
        return env.requestShowTipType == getTipType();
    }
}
