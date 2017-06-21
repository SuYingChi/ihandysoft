package com.ihs.feature.tip;


import com.ihs.feature.common.TipInfo;
import com.ihs.feature.ui.FloatWindowManager;
import com.ihs.feature.ui.LauncherFloatWindowManager;

public class JunkCleanInstallTipInfo extends TipInfo {

    @Override
    public LauncherTipManager.TipType getTipType() {
        return LauncherTipManager.TipType.JUNK_CLEAN_INSTALL_TIP;
    }

    @Override
    public LauncherTipManager.ResultType wantToShow(LauncherTipManager.TipEnvironment env) {
        return LauncherTipManager.ResultType.FOCUS_SHOW;
    }

    @Override
    public void show(LauncherTipManager.TipEnvironment env) {
        LauncherFloatWindowManager.getInstance().showDialog(env.context,
                LauncherFloatWindowManager.Type.JUNK_CLEAN_INSTALL, env.extras[0]);
    }

    @Override
    public void dismiss() {
        LauncherFloatWindowManager.getInstance().removeDialog(FloatWindowManager.Type.JUNK_CLEAN_INSTALL);
    }

}
