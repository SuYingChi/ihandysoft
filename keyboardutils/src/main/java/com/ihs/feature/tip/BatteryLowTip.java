package com.ihs.feature.tip;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.ihs.keyboardutils.R;


public class BatteryLowTip extends TwoActionsTip {

    private Runnable mShownRunnable;

    public BatteryLowTip(Context context, Runnable shownRunnable, Runnable confirmedRunnable, Runnable canceledRunnable) {
        super(context, confirmedRunnable, canceledRunnable);
        mShownRunnable = shownRunnable;
    }

    @Override
    protected String getDialogTitle() {
        return getResources().getString(R.string.battery_low_tip_title);
    }

    @Override
    protected String getDialogDesc() {
        return getResources().getString(R.string.battery_low_tip_desc);
    }

    @Override
    protected int getPositiveButtonStringId() {
        return R.string.battery_low_tip_btn_ok;
    }

    @Override
    protected int getNegativeButtonStringId() {
        return R.string.battery_low_tip_btn_cancel;
    }

    @Override
    protected Drawable getTopImageDrawable() {
        return ContextCompat.getDrawable(mActivity, R.drawable.dialog_top_lowbattery);
    }

    @Override
    protected void onShow() {
        super.onShow();
        mShownRunnable.run();
    }
}
