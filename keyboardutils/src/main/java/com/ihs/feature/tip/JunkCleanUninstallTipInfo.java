package com.ihs.feature.tip;


import com.ihs.feature.common.TipInfo;
import com.ihs.feature.ui.FloatWindowManager;
import com.ihs.feature.ui.LauncherFloatWindowManager;

public class JunkCleanUninstallTipInfo extends TipInfo {

    @Override
    public LauncherTipManager.TipType getTipType() {
        return LauncherTipManager.TipType.JUNK_CLEAN_UNINSTALL_TIP;
    }

    @Override
    public LauncherTipManager.ResultType wantToShow(LauncherTipManager.TipEnvironment env) {
        return LauncherTipManager.ResultType.FOCUS_SHOW;
    }

    @Override
    public void show(LauncherTipManager.TipEnvironment env) {
        LauncherFloatWindowManager.getInstance().showDialog(env.context,
                LauncherFloatWindowManager.Type.JUNK_CLEAN_UNINSTALL, env.extras[0]);
    }

    @Override
    public void dismiss() {
        LauncherFloatWindowManager.getInstance().removeDialog(FloatWindowManager.Type.JUNK_CLEAN_UNINSTALL);
    }

    @Override
    public boolean isValidExtras(LauncherTipManager.TipEnvironment env) {
        if (super.isValidExtras(env)) {
            return env.extras != null && env.extras.length == 1;
        }
        return false;
    }
}
