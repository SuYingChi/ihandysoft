package com.ihs.feature.tip;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import com.ihs.feature.junkclean.util.JunkCleanConstant;
import com.ihs.feature.junkclean.util.JunkCleanUtils;
import com.ihs.feature.ui.FloatWindowManager;
import com.ihs.keyboardutils.R;


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
        int i = v.getId();
        if (i == R.id.action_btn) {// click implements in super
            JunkCleanUtils.FlurryLogger.logOpen(JunkCleanConstant.OBSOLETE_APK);

        } else {
        }
    }

}
