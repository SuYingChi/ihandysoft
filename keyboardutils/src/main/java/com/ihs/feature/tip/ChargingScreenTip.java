package com.ihs.feature.tip;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.artw.lockscreen.LockerSettings;
import com.ihs.chargingscreen.utils.ChargingPrefsUtil;
import com.ihs.feature.ui.DefaultButtonDialog2;
import com.ihs.keyboardutils.R;
import com.kc.utils.KCAnalytics;


/**
 * Created by lz on 3/10/17.
 */

public class ChargingScreenTip extends DefaultButtonDialog2 {

    public ChargingScreenTip(Context context) {
        super(context);
    }

    @Override
    protected String getDialogDesc() {
        return getResources().getString(R.string.charging_screen_guide_title);
    }

    @Override
    protected int getPositiveButtonStringId() {
        return R.string.settings_auth_button_text;
    }

    @Override
    protected void onClickPositiveButton(View v) {
        ChargingPrefsUtil.getInstance().setChargingEnableByUser(true);
        if (!LockerSettings.isLockerEnabledBefore()) {
            LockerSettings.setLockerEnabled(true);
        }
        Toast.makeText(getContext(), R.string.charging_screen_guide_turn_on, Toast.LENGTH_SHORT).show();
        super.onClickPositiveButton(v);
        KCAnalytics.logEvent("Alert_LockerScreen_TurnOn_Clicked", "type", "Turn on");
    }

    @Override
    protected void onClickNegativeButton(View v) {
    }

    @Override
    protected void onCanceled() {
        super.onCanceled();
    }

    @Override
    protected void onDismissComplete() {
    }

    @Override
    protected Drawable getTopImageDrawable() {
        return ContextCompat.getDrawable(mActivity, R.drawable.charging_screen_guide);
    }
}
