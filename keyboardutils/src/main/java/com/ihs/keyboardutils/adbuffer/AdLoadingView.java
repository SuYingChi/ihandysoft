package com.ihs.keyboardutils.adbuffer;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;

ximport android.app.Activity;

/**
 * Created by Arthur on 17/4/12.
 */

public class AdLoadingView extends RelativeLayout implements NativeAdView.OnAdLoadedListener, NativeAdView.OnAdClickedListener {


    public ProgressBar progressBar;
    private AdLoadingDialog dialog;
    public TextView tvApply;
    private int delayAfterDownloadComplete;

    //下载延迟常量
    private static final int DELAY_PERCENT_AFTER_DOWNLOAD_COMPLETE = 5;

    @Override
    public void onAdClicked(NativeAdView adView) {
        dismissSelf();
    }


    public interface OnAdBufferingListener {
        void onDismiss();
    }

    private String[] onLoadingText = {"Applying...", "Applying SuccessFully"};
    private NativeAdView nativeAdView;
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

        nativeAdView = new NativeAdView(getContext(), inflate(getContext(), R.layout.layout_ad_loading_adview, null));
        nativeAdView.setOnAdLoadedListener(this);

        findViewById(R.id.iv_close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissSelf();
            }
        });

        nativeAdView.setOnAdClickedListener(this);

        progressBar = (ProgressBar) findViewById(R.id.pb);
    }

    private AdLoadingView setIcon(Drawable icon) {
        if (icon != null) {
            ((ImageView) findViewById(R.id.iv_icon)).setImageDrawable(icon);
        }
        return this;
//        throw new RuntimeException("ad loading 图标错误");
    }

    private AdLoadingView setBackgroundPreview(Drawable icon) {
        if (icon != null) {
            findViewById(R.id.iv_icon).setBackgroundDrawable(icon);
        }
        return this;
//        throw new RuntimeException("ad loading 图标错误");
    }

    private AdLoadingView setOnLoadingText(String loadingText, String loadComplete) {
        onLoadingText[0] = loadingText;
        onLoadingText[1] = loadComplete;
        tvApply.setText(onLoadingText[0]);
        return this;
    }

    private AdLoadingView setAdPlacementName(String adPlacementName) {
        if (!TextUtils.isEmpty(adPlacementName)) {
            nativeAdView.configParams(new NativeAdParams(adPlacementName));
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
            percent = percent - DELAY_PERCENT_AFTER_DOWNLOAD_COMPLETE;
            progressBar.setProgress(percent);
        } else {
            fakeLoadingProgress(maxProgress - DELAY_PERCENT_AFTER_DOWNLOAD_COMPLETE, maxProgress);
        }

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
                progressBar.setProgress(animatedValue);
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                ViewGroup adContainer = (ViewGroup) findViewById(R.id.fl_ad_container);
                adContainer.addView(nativeAdView);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(adContainer, "scaleY", 0f, 1f).setDuration(1000);
                scaleY.setInterpolator(new BounceInterpolator());
                scaleY.start();

                tvApply.setText(onLoadingText[1]);

                findViewById(R.id.iv_close).setVisibility(VISIBLE);
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
            , int delayAfterDownloadComplete) {
        setBackgroundPreview(bg).setIcon(icon).setAdPlacementName(adPlacementName).setOnLoadingText(loadingText, loadComplete);
        this.delayAfterDownloadComplete = delayAfterDownloadComplete;
        this.onAdBufferingListener = onAdBufferingListener;
    }

    public void showInDialog() {
        dialog = new AdLoadingDialog(getContext());
        dialog.setContentView(this);
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                onAdBufferingListener.onDismiss();
            }
        });
    }

    @Override
    public void onAdLoaded(NativeAdView adView) {

    }

    private void dismissSelf() {
        if (dialog == null) {
            onAdBufferingListener.onDismiss();
        } else {
            dialog.dismiss();
        }
    }
}
