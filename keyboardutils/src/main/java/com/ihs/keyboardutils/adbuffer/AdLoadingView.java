package com.ihs.keyboardutils.adbuffer;

import android.animation.Animator;
import android.animation.ValueAnimator;
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
import android.widget.TextView;

import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;
import com.ihs.keyboardutils.utils.KCAnalyticUtil;
import com.ihs.keyboardutils.utils.RippleDrawableUtils;
import com.ihs.keyboardutils.view.CustomProgressDrawable;
import com.ihs.keyboardutils.view.FlashFrameLayout;

/**
 * Created by Arthur on 17/4/12.
 */

public class AdLoadingView extends RelativeLayout implements NativeAdView.OnAdLoadedListener, NativeAdView.OnAdClickedListener {


    private AdLoadingDialog dialog;
    public TextView tvApply;
    private int delayAfterDownloadComplete;
    private ImageView progressBar;
    private boolean progressComplete;
    private boolean showCloseButtonWhenFinish;

    //下载延迟常量
    private boolean hasPurchaseNoAds = false;
    private boolean isAdFlashAnimationPlayed = false;
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;
    private View closeButton;

    @Override
    public void onAdClicked(NativeAdView adView) {
        KCAnalyticUtil.logEvent("NativeAds_A(NativeAds)ApplyingItem_Click");
        dismissSelf();
    }


    public interface OnAdBufferingListener {
        void onDismiss(boolean progressComplete);
    }

    private String[] onLoadingText = {"Applying...", "Applying SuccessFully"};
    private NativeAdView nativeAdView;
    private FlashFrameLayout flashAdContainer;
    private OnAdBufferingListener onAdBufferingListener;

    public AdLoadingView(Context context) {
        super(context);
        init();
    }

    public AdLoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AdLoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.layout_ad_loading, this);

        tvApply = (TextView) findViewById(R.id.tv_apply);

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

        progressBar = (ImageView) findViewById(R.id.iv_pb);
        progressBar.setImageDrawable(new CustomProgressDrawable());
    }

    private void initAdView() {
        View inflate = inflate(getContext(), R.layout.layout_ad_loading_adview, null);
        nativeAdView = new NativeAdView(getContext(), inflate);
        inflate.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        nativeAdView.setOnAdLoadedListener(this);

        inflate.findViewById(R.id.ad_call_to_action)
                .setBackgroundDrawable(
                        RippleDrawableUtils.getButtonRippleBackground(R.color.ad_button_blue));
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

    private AdLoadingView setIcon(Drawable icon) {
        ((ImageView) findViewById(R.id.iv_icon)).setImageDrawable(icon);
        return this;
    }

    private AdLoadingView setBackgroundPreview(Drawable icon) {
        findViewById(R.id.iv_icon).setBackgroundDrawable(icon);
        return this;
    }

    private AdLoadingView setOnLoadingText(String loadingText, String loadComplete) {
        onLoadingText[0] = loadingText;
        onLoadingText[1] = loadComplete;
        tvApply.setText(onLoadingText[0]);
        return this;
    }

    private AdLoadingView setAdPlacementName(String adPlacementName) {
        if (!TextUtils.isEmpty(adPlacementName) && !hasPurchaseNoAds) {
            //吉祥阁跟我敲板
            nativeAdView.configParams(new NativeAdParams(adPlacementName,
                    (int) (DisplayUtils.getScreenWidthPixels() * 0.9),
                    1.9f));
            KCAnalyticUtil.logEvent("NativeAds_A(NativeAds)ApplyingItem_Load");
            return this;
        }
        throw new RuntimeException("ad loading 广告池名字未配置");
    }

    /**
     * 当前进度 百分之几. 有进度的数据则进度延迟5%，没有进度的数据，则使用延迟的假进度;
     *
     * @param percent
     */
    public void updateProgressPercent(int percent) {
        int maxProgress = 100;
        if (percent < maxProgress) {
            progressBar.getDrawable().setLevel(percent);
        } else {
            fakeLoadingProgress(percent, 101);
        }
    }

    public void setConnectionStateText(String text) {
        tvApply.setText(text);
    }

    public void setConnectionProgressVisibility(int visibility) {
        progressBar.setVisibility(visibility);
    }

    public void startFakeLoading() {
        fakeLoadingProgress(0, 100);
    }

    private void fakeLoadingProgress(final int startPercent, final int endPercent) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(startPercent, endPercent);
        valueAnimator.setDuration(delayAfterDownloadComplete);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();
                progressBar.getDrawable().setLevel(animatedValue);
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                tvApply.setText(onLoadingText[1]);

                progressBar.setVisibility(INVISIBLE);
                closeButton.setVisibility(VISIBLE);
                progressComplete = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.start();
    }

    public void configParams(Drawable bg, Drawable icon, String loadingText, String loadComplete, String adPlacementName, OnAdBufferingListener onAdBufferingListener
            , int delayAfterDownloadComplete, boolean hasPurchaseNoAds) {
        setBackgroundPreview(bg).setIcon(icon).setAdPlacementName(adPlacementName).setOnLoadingText(loadingText, loadComplete);
        this.delayAfterDownloadComplete = delayAfterDownloadComplete;
        this.onAdBufferingListener = onAdBufferingListener;
        this.hasPurchaseNoAds = hasPurchaseNoAds;
    }

    public void setIconImageVisibility(int visibility) {
        findViewById(R.id.iv_icon).setVisibility(visibility);
    }

    public void setLoadingTextSize(float size) {
        tvApply.setTextSize(size);
    }

    public void hideCloseButtonUntilSuccess(boolean showCloseButtonWhenFinish) {
        this.showCloseButtonWhenFinish = showCloseButtonWhenFinish;
        if (showCloseButtonWhenFinish) {
            closeButton.setVisibility(INVISIBLE);
        }
    }

    public void showInDialog() {
        KCAnalyticUtil.logEvent("app_alert_applyingItem_show");
        dialog = new AdLoadingDialog(getContext());
        dialog.setContentView(this);
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (onAdBufferingListener != null) {
                    onAdBufferingListener.onDismiss(progressComplete);
                }
            }
        });
    }

    @Override
    public void onAdLoaded(NativeAdView adView) {
        KCAnalyticUtil.logEvent("NativeAds_A(NativeAds)ApplyingItem_Show");
    }

    public void dismissSelf() {
        if (nativeAdView != null) {
            nativeAdView.release();
            isAdFlashAnimationPlayed = false;
            nativeAdView.getViewTreeObserver().removeGlobalOnLayoutListener(mGlobalLayoutListener);
        }

        if (dialog == null) {
            onAdBufferingListener.onDismiss(progressComplete);
        } else {
            dialog.dismiss();
        }
    }

}
