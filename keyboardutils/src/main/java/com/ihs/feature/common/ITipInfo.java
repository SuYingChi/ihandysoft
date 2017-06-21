package com.ihs.feature.common;


import com.ihs.feature.tip.LauncherTipManager;

public interface ITipInfo {
    LauncherTipManager.TipType getTipType();

    LauncherTipManager.ResultType wantToShow(LauncherTipManager.TipEnvironment env);

    void show(LauncherTipManager.TipEnvironment env);

    void dismiss();

    boolean isValidExtras(LauncherTipManager.TipEnvironment env);
}
