package com.ihs.keyboardutils.alerts;

import android.app.Activity;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;

import static com.ihs.app.framework.HSApplication.getContext;

/**
 * Created by yanxia on 2017/4/12.
 */

public class ExitAlert {
    protected static final int EXIT_ALERT_STYLE_1 = 1;
    protected static final int EXIT_ALERT_STYLE_2 = 2;

    private int alterViewStyle;
    private ExitAlertDialog alertDialog;
    private String adPlacement;
    private NativeAdView nativeAdView;
    private boolean showAd;
    private boolean enableAlert;

    public ExitAlert(Activity activity, String adPlacementName) {
        enableAlert = HSConfig.optBoolean(false, "Application", "ExitAlert", "Enable");
        alterViewStyle = HSConfig.optInteger(EXIT_ALERT_STYLE_2, "Application", "ExitAlert", "style");
        this.adPlacement = adPlacementName;
        showAd = !TextUtils.isEmpty(adPlacementName);//如果没有广告位，说明不用显示广告
        alertDialog = new ExitAlertDialog(activity, alterViewStyle, showAd);
        if (enableAlert && showAd) {
            initNativeAdView();
        }
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                releaseAdIfNecessary();
            }
        });
    }

    private void initNativeAdView() {
        View view;

        switch (alterViewStyle) {
            case EXIT_ALERT_STYLE_1:
            default:
                view = LayoutInflater.from(getContext()).inflate(R.layout.exit_app_native_ad_view1, null);
                break;
            case EXIT_ALERT_STYLE_2:
                view = LayoutInflater.from(getContext()).inflate(R.layout.exit_app_native_ad_view2, null);
                break;
        }

        nativeAdView = new NativeAdView(getContext(), view, null);
        nativeAdView.setOnAdClickedListener(new NativeAdView.OnAdClickedListener() {
            @Override
            public void onAdClicked(NativeAdView adView) {
                alertDialog.dismiss();
            }
        });
        nativeAdView.configParams(new NativeAdParams(adPlacement, calculateAdWidth(), 1.9f));
    }

    private int calculateAdWidth() {
        int screenWidth = DisplayUtils.getScreenWidthPixels();
        int width = (int) getContext().getResources().getFraction(com.ihs.keyboardutils.R.fraction.design_dialog_width, screenWidth, screenWidth);
        return width;
    }

    public void setShowAd(boolean showAd){
        this.showAd = showAd;

        if(!showAd){
            releaseAdIfNecessary();
        }
    }

    private void releaseAdIfNecessary() {
        if (nativeAdView != null) {
            nativeAdView.release();
            nativeAdView = null;
        }
    }

    public boolean show() {
        if (enableAlert && alertDialog != null) {
            if (showAd && nativeAdView != null && nativeAdView.isAdLoaded()) {
                alertDialog.setSponsorView(nativeAdView);
            }
            alertDialog.setCancelable(true);
            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.show();
            return true;
        } else {
            if (alertDialog!=null){
                alertDialog.release();
                alertDialog = null;
                releaseAdIfNecessary();
            }
            return false;
        }
    }

    public void dismiss() {
        alertDialog.dismiss();
    }
}
