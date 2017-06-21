package com.ihs.feature.tip;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import com.honeycomb.launcher.R;
import com.honeycomb.launcher.junkclean.util.JunkCleanConstant;
import com.honeycomb.launcher.junkclean.util.JunkCleanUtils;

@SuppressLint("ViewConstructor")
public class JunkCleanInstallTip extends JunkCleanFloatTip {

    public JunkCleanInstallTip(Context context, Data data) {
        super(context, data);
    }

    @Override
    public FloatWindowManager.Type getTipType() {
        return FloatWindowManager.Type.JUNK_CLEAN_INSTALL;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.action_btn:
                // click implements in super
                JunkCleanUtils.FlurryLogger.logSpecialAlertClicked(JunkCleanConstant.OBSOLETE_APK);
                JunkCleanUtils.FlurryLogger.logOpen(JunkCleanConstant.OBSOLETE_APK);
                break;
            default:
                break;
        }
    }

}
