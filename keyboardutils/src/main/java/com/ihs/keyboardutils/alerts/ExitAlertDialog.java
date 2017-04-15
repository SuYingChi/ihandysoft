package com.ihs.keyboardutils.alerts;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;

/**
 * Created by yanxia on 2017/4/12.
 */

class ExitAlertDialog extends AlertDialog implements View.OnClickListener {

    private TextView exitButton;
    private View sponsorView;
    private Activity activity;

    /**
     * Creates an alert dialog that uses the DesignDialog alert dialog theme.
     *
     * @param activity the parent activity
     * @see R.style#DesignDialog
     */
    ExitAlertDialog(@NonNull Activity activity) {
        super(activity, R.style.DesignDialog);
        this.activity = activity;
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
        setContentView(R.layout.exit_app_native_ad_alert);

        FrameLayout nativeAdViewContainer = (FrameLayout) findViewById(R.id.native_ad_container);
        if (sponsorView != null) {
            nativeAdViewContainer.addView(sponsorView);
        }

        exitButton = (TextView) findViewById(R.id.btn_alert_exit);
        if (exitButton != null) {
            exitButton.setOnClickListener(this);
        }

        findViewById(R.id.exit_alert_root_view).getLayoutParams().width = calculateAdWidth();
    }

    /**
     * Called when the dialog has detected the user's press of the back
     * key.  The default implementation simply cancels the dialog (only if
     * it is cancelable), but you can override this to do whatever you want.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishActivity();
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
        WindowManager.LayoutParams params = getWindow().getAttributes();  //获取对话框当前的参数值、
        params.width = DisplayUtils.getScreenWidthPixels();    //宽度设置全屏宽度
        getWindow().setAttributes(params);     //设置生效
    }
}
