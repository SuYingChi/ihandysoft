package com.ihs.chargingscreen.notification.push;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.view.Gravity;

import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.keyboardutils.R;

/**
 * Created by zhixiangxiao on 5/16/16.
 */
public class FullChargedPush extends BasePush {

    public FullChargedPush(Context context) {
        super(context);

        updatePush();

    }

    public int getRootViewLayoutId() {
        return R.layout.charging_module_push_full_charged;
    }

    public void initLayoutParamsGravityAndType() {
//        layoutParams.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        layoutParams.gravity = Gravity.BOTTOM;
    }

    protected void startPushAppearAnimation() {
        PropertyValuesHolder appearAlpha = PropertyValuesHolder.ofFloat("alpha", 0.3f, 1.0f);
        PropertyValuesHolder appearTransY = PropertyValuesHolder.ofFloat("translationY", rootView.getMeasuredHeight(), 0);
        appearAnimator = ObjectAnimator.ofPropertyValuesHolder(this, appearAlpha, appearTransY);
        appearAnimator.setDuration(PUSH_APPEAR_DURATION);
        appearAnimator.start();
    }

    protected void cancelAnimation() {
        if (appearAnimator != null) {
            appearAnimator.removeAllListeners();
            appearAnimator.cancel();
            appearAnimator = null;
        }
    }

    public void updatePush() {
        for (int i = 0; i < imgBatteryList.size(); i++) {
            if (i < ChargingManagerUtil.getImgBatteryVisibleCount()) {
                imgBatteryList.get(i).setVisibility(VISIBLE);
            } else {
                imgBatteryList.get(i).setVisibility(INVISIBLE);
            }
        }
    }
}
