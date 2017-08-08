package com.ihs.keyboardutils.adbuffer;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;
import com.ihs.keyboardutils.utils.RippleDrawableUtils;
import com.ihs.keyboardutils.view.FlashFrameLayout;

public class NativeAdDialogView extends RelativeLayout implements NativeAdView.OnAdLoadedListener, NativeAdView.OnAdClickedListener {
    private NativeAdDialog dialog;
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;
    private View closeButton;
    private boolean hasPurchaseNoAds = false;
    private boolean isAdFlashAnimationPlayed = false;

    @Override
    public void onAdClicked(NativeAdView adView) {
        //KCAnalyticUtil.logEvent("NativeAds_A(NativeAds)ApplyingItem_Click");
        dismissSelf();
    }

    public interface OnAdBufferingListener {
        void onDismiss();
        void onAdLoaded();
    }

    private String[] onLoadingText = {"Applying...", "Applying SuccessFully"};
    private NativeAdView nativeAdView;
    private FlashFrameLayout flashAdContainer;
    private OnAdBufferingListener onAdBufferingListener;

    public NativeAdDialogView(Context context) {
        super(context);
        init();
    }

    public NativeAdDialogView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NativeAdDialogView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.native_ad_dialog, this);

        if (!hasPurchaseNoAds) {
            initAdView();
        }

        closeButton = findViewById(R.id.iv_close);
        closeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissSelf();
            }
        });

        closeButton.setBackgroundDrawable(
                RippleDrawableUtils.getTransparentRippleBackground());
    }

    private void initAdView() {
        View inflate = inflate(getContext(), R.layout.native_ad_dialog_adview, null);
        nativeAdView = new NativeAdView(getContext(), inflate);
        inflate.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        nativeAdView.setOnAdLoadedListener(this);

        inflate.findViewById(R.id.ad_call_to_action)
                .setBackgroundDrawable(
                        RippleDrawableUtils.getButtonRippleBackground(R.color.ad_action_button_bg));
        flashAdContainer = (FlashFrameLayout) inflate.findViewById(R.id.ad_loading_flash_container);


        nativeAdView.setOnAdClickedListener(this);
        if (mGlobalLayoutListener == null) {
            mGlobalLayoutListener = getLayoutListener();
        }
        nativeAdView.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);

        ViewGroup adContainer = (ViewGroup) findViewById(R.id.fl_ad_container);
        if (nativeAdView.getParent() != null) {
            ((ViewGroup) nativeAdView.getParent()).removeView(nativeAdView);
        }
        adContainer.addView(nativeAdView);
    }

    private ViewTreeObserver.OnGlobalLayoutListener getLayoutListener() {
        return new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (nativeAdView.isAdLoaded() && !isAdFlashAnimationPlayed) {
                    flashAdContainer.startShimmerAnimation();
                    isAdFlashAnimationPlayed = true;
                }
            }
        };
    }

    private NativeAdDialogView setIcon(Drawable icon) {
        ((ImageView) findViewById(R.id.iv_icon)).setImageDrawable(icon);
        return this;
    }

    private NativeAdDialogView setAdPlacementName(String adPlacementName) {
        if (!TextUtils.isEmpty(adPlacementName) && !hasPurchaseNoAds) {
            //吉祥阁跟我敲板
            nativeAdView.configParams(new NativeAdParams(adPlacementName,
                    (int) (DisplayUtils.getScreenWidthPixels() * 0.9),
                    1.9f));
            //KCAnalyticUtil.logEvent("NativeAds_A(NativeAds)ApplyingItem_Load");
            return this;
        }
        throw new RuntimeException("ad loading 广告池名字未配置");
    }

    public void configParams(String adPlacementName, OnAdBufferingListener onAdBufferingListener, boolean hasPurchaseNoAds) {
        setAdPlacementName(adPlacementName);
        this.onAdBufferingListener = onAdBufferingListener;
        this.hasPurchaseNoAds = hasPurchaseNoAds;
    }

    public void showInDialog() {
        //KCAnalyticUtil.logEvent("app_alert_applyingItem_show");
        dialog = new NativeAdDialog(getContext());
        dialog.setContentView(this);
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (onAdBufferingListener != null) {
                    onAdBufferingListener.onDismiss();
                }
            }
        });
    }

    @Override
    public void onAdLoaded(NativeAdView adView) {
        //KCAnalyticUtil.logEvent("NativeAds_A(NativeAds)ApplyingItem_Show");
        HSLog.d("onAdLoaded", "onAdLoaded");

        if (onAdBufferingListener != null) {
            onAdBufferingListener.onAdLoaded();
        }
    }

    public void dismissSelf() {
        if (nativeAdView != null) {
            nativeAdView.release();
            isAdFlashAnimationPlayed = false;
            nativeAdView.getViewTreeObserver().removeGlobalOnLayoutListener(mGlobalLayoutListener);
        }

        if (dialog == null) {
            onAdBufferingListener.onDismiss();
        } else {
            dialog.dismiss();
        }
    }

}
