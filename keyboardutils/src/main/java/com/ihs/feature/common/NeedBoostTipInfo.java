package com.ihs.feature.common;

import android.os.Handler;
import android.os.Looper;

import com.ihs.commons.utils.HSLog;
import com.ihs.feature.boost.BoostConditionManager;
import com.ihs.feature.boost.BoostType;
import com.ihs.feature.tip.LauncherTipManager;


public class NeedBoostTipInfo extends TipInfo {
    @Override
    public LauncherTipManager.TipType getTipType() {
        return LauncherTipManager.TipType.NEED_BOOST_TIP;
    }

    @Override
    public LauncherTipManager.ResultType wantToShow(LauncherTipManager.TipEnvironment env) {
        if (!env.hasTipShow()) {
            return LauncherTipManager.ResultType.SHOW;
        }
        return LauncherTipManager.ResultType.SHOW_AFTER_CURRENT;
    }

    @Override
    public void show(LauncherTipManager.TipEnvironment env) {
        long delay = 0;
        if (isValidExtras(env)) {
            BoostConditionManager.getInstance().notifyNeedBoost((BoostType) env.extras[0], (Boolean) env.extras[1]);
            delay = 5000;
        } else {
            HSLog.w("error env");
        }

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                LauncherTipManager.getInstance().notifyDismiss();
            }
        }, delay);
    }

    @Override
    public void dismiss() {
        LauncherTipManager.TipEnvironment tipEnv = LauncherTipManager.getInstance().getEnvironment();
        if (isValidExtras(tipEnv)) {
            BoostType type = (BoostType) tipEnv.extras[0];
            BoostConditionManager.getInstance().notifyNeedBoost(type, false);
        }
    }

    @Override
    public boolean isValidExtras(LauncherTipManager.TipEnvironment env) {
        if (super.isValidExtras(env)) {
            if (env.extras != null && env.extras.length >= 2) {
                if (env.extras[0] instanceof BoostType && env.extras[1] instanceof Boolean) {
                    return true;
                }
            }
        }
        return false;
    }
}
