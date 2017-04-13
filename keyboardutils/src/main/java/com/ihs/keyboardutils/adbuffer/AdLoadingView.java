package com.ihs.keyboardutils.adbuffer;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;

/**
 * Created by Arthur on 17/4/12.
 */

public class AdLoadingView extends RelativeLayout implements NativeAdView.OnAdLoadedListener {


    public ProgressBar progressBar;
    private AdLoadingDialog dialog;

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
        LayoutInflater.from(getContext()).inflate(R.layout.layout_ad_loading, null);

        nativeAdView = new NativeAdView(getContext(), inflate(getContext(), R.layout.layout_ad_loading_adview, null));
        nativeAdView.setOnAdLoadedListener(this);

        findViewById(R.id.iv_close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog == null) {
                    onAdBufferingListener.onDismiss();
                } else {
                    dialog.dismiss();
                }
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.pb);
    }

    private AdLoadingView setIcon(Drawable icon) {
        if (icon != null) {
            findViewById(R.id.iv_icon).setBackgroundDrawable(icon);
            return this;
        }
        throw new RuntimeException("ad loading 图标错误");
    }

    private AdLoadingView setOnLoadingText(String loadingText, String loadComplete) {
        onLoadingText[0] = loadingText;
        onLoadingText[1] = loadComplete;
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
    public void updateProgressPercent(int percent, int delayTimer, boolean fakeLoading) {
        int timerCounter;
        int timerSteps;
        if (!fakeLoading) {
            percent = percent - 5;
            if (percent < 100) {
                progressBar.setProgress(percent);
            } else {
                timerCounter = delayTimer;
                timerSteps = delayTimer / 5;
                fakeLoadingProgress(percent, timerCounter, timerSteps);
            }

        } else {
            percent = 0;
            timerCounter = delayTimer;
            timerSteps = delayTimer / 100;
            fakeLoadingProgress(percent, timerCounter, timerSteps);
        }
    }

    private void fakeLoadingProgress(final int percent, final int timerCounter, final int timerSteps) {
        final int[] i = {0};
        CountDownTimer mCountDownTimer = new CountDownTimer(timerCounter, timerSteps) {

            @Override
            public void onTick(long millisUntilFinished) {
                i[0]++;
                progressBar.setProgress(i[0] + percent);
            }

            @Override
            public void onFinish() {
                //Do what you want
                i[0]++;
                progressBar.setProgress(i[0] + percent);
            }
        };
        mCountDownTimer.start();
    }

    public void configParams(Drawable icon, String loadingText, String loadComplete, String adPlacementName, OnAdBufferingListener onAdBufferingListener) {
        setIcon(icon).setAdPlacementName(adPlacementName).setOnLoadingText(loadingText, loadComplete);
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
        ViewGroup adContainer = (ViewGroup) findViewById(R.id.fl_ad_container);
        adContainer.addView(adView);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(adContainer, "scaleY", 0f, 1f).setDuration(300);
        scaleY.setInterpolator(new BounceInterpolator());
        scaleY.start();
    }
}
