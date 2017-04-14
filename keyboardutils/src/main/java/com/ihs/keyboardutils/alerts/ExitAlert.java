package com.ihs.keyboardutils.alerts;

import android.app.Activity;

/**
 * Created by yanxia on 2017/4/12.
 */

public class ExitAlert {
    private ExitAlertDialog alert;

    public ExitAlert(Activity activity, String adPlacementName) {
        alert = new ExitAlertDialog(activity, adPlacementName);
    }

    public boolean show() {
        if (alert != null && alert.isReadyToShow()) {
            alert.setCancelable(true);
            alert.setCanceledOnTouchOutside(true);
            alert.show();
            return true;
        } else {
            return false;
        }
    }

    public void dismiss() {
        alert.dismiss();
    }
}
