package com.ihs.keyboardutils.alerts;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.RippleDrawableUtils;

import static com.ihs.keyboardutils.alerts.ExitAlert.EXIT_ALERT_STYLE_1;

/**
 * Created by yanxia on 2017/4/12.
 */

class ExitAlertDialog extends AlertDialog implements View.OnClickListener {

    private TextView exitButton;
    private View sponsorView;
    private Activity activity;
    private int alterViewStyle;
    private boolean showAd;

    /**
     * Creates an alert dialog that uses the DesignDialog alert dialog theme.
     *
     * @param activity       the parent activity
     * @param alterViewStyle
     * @param showAd
     * @see R.style#DesignDialog
     */
    ExitAlertDialog(@NonNull Activity activity, int alterViewStyle, boolean showAd) {
        super(activity, R.style.DesignDialog);
        this.activity = activity;
        this.alterViewStyle = alterViewStyle;
        this.showAd = showAd;
    }

    private int calculateAdWidth() {
        int screenWidth = DisplayUtils.getScreenWidthPixels();
        int width = (int) getContext().getResources().getFraction(com.ihs.keyboardutils.R.fraction.design_dialog_width, screenWidth, screenWidth);
        return width;
    }

    public void setSponsorView(View sponsorView) {
        this.sponsorView = sponsorView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(false);
        if (alterViewStyle == EXIT_ALERT_STYLE_1 && showAd) {
            setContentView(R.layout.exit_app_native_ad_alert1);
        } else {
            setContentView(R.layout.exit_app_native_ad_alert2);
        }

        float radius = getContext().getResources().getDimension(R.dimen.design_base_corner_radius);
        if (sponsorView != null) {
            FrameLayout nativeAdViewContainer = (FrameLayout) findViewById(R.id.native_ad_container);
            if (alterViewStyle == EXIT_ALERT_STYLE_1 && sponsorView != null) {
                nativeAdViewContainer.addView(sponsorView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            } else {
                nativeAdViewContainer.addView(sponsorView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (calculateAdWidth() / 1.6)));
            }
            TextView adActionView = (TextView) sponsorView.findViewById(R.id.ad_call_to_action);
            adActionView.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(0xff43bb48, radius));
        }
        exitButton = (TextView) findViewById(R.id.btn_alert_exit);
        if (exitButton != null) {
            exitButton.setOnClickListener(this);
            exitButton.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(Color.WHITE, radius));
        }

        View btnCancel = findViewById(R.id.btn_cancel);
        if (btnCancel != null) {
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            btnCancel.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(Color.WHITE, radius));
        }

        findViewById(R.id.exit_alert_root_view).getLayoutParams().width = calculateAdWidth();
    }

    @Override
    public void onClick(View v) {
        dismiss();
        if (v == exitButton) {
            finishActivity();
        }
    }

    private void finishActivity() {
        if (activity != null) {
            activity.finish();
        }
    }

    @Override
    public void show() {
        super.show();
        /**
         * 设置dialog宽度全屏
         */
        HSAnalytics.logEvent("app_quit_confirm_alert_show");
        HSAnalytics.logGoogleAnalyticsEvent("app", "alertdialog", "app_quit_confirm_alert_show", null, null, null, null);
        WindowManager.LayoutParams params = getWindow().getAttributes();  //获取对话框当前的参数值、
        params.width = DisplayUtils.getScreenWidthPixels();    //宽度设置全屏宽度
        getWindow().setAttributes(params);     //设置生效
    }
}
