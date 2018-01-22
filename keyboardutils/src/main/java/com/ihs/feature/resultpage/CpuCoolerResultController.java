package com.ihs.feature.resultpage;

import android.animation.TimeInterpolator;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.View;
import android.widget.TextView;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.feature.common.ViewUtils;
import com.ihs.feature.resultpage.data.CardData;
import com.ihs.feature.resultpage.data.ResultConstants;
import com.ihs.feature.ui.FlashCircleView;
import com.ihs.keyboardutils.R;

import net.appcloudbox.ads.base.AcbAd;
import net.appcloudbox.ads.base.AcbNativeAd;

import java.util.List;

class CpuCoolerResultController extends ResultController {

    private static final long START_DELAY_FLASH_CIRCLE = 620;
    private static final long DURATION_OPTIMAL_TEXT_TRANSLATION = 640;

    CpuCoolerResultController(ResultPageActivity activity, Type type, @Nullable AcbNativeAd ad, List<CardData> cardDataList) {
        super.init(activity, ResultConstants.RESULT_TYPE_CPU_COOLER, type, ad, cardDataList);
        if (ad != null) {
            ad.setNativeClickListener(new AcbNativeAd.AcbNativeClickListener() {
                @Override
                public void onAdClick(AcbAd acbAd) {
                    HSAnalytics.logEvent("ResultPage_Cards_Click", "Type", ResultConstants.AD);
                }
            });
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.result_page_done_circle_transition;
    }

    @Override
    protected void onFinishInflateTransitionView(View transitionView) {
    }

    @Override
    protected void onStartTransitionAnimation(final View transitionView) {
        final FlashCircleView doneCircle = ViewUtils.findViewById(transitionView, R.id.done_circle);
        doneCircle.setViewListener(new FlashCircleView.ViewListener() {
            @Override
            public void onViewed() {
                doneCircle.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doneCircle.startAnimation();
                    }
                }, START_DELAY_FLASH_CIRCLE);
            }

            @Override
            public void onAnimationEnd() {
                doneCircle.setVisibility(View.GONE);

                final TextView optimalTv = ViewUtils.findViewById(transitionView, R.id.label_title);
                final TextView titleAnchor = ViewUtils.findViewById(transitionView, R.id.anchor_title_tv);

                int[] location = new int[2];
                optimalTv.getLocationInWindow(location);
                int oldOptimalTvCenterY = location[1] + optimalTv.getHeight() / 2;
                titleAnchor.getLocationInWindow(location);
                int newOptimalTvCenterY = location[1] + titleAnchor.getHeight() / 2;

                TimeInterpolator softStopAccDecInterpolator = PathInterpolatorCompat.create(0.79f, 0.37f, 0.28f, 1f);
                optimalTv.animate()
                        .translationYBy(newOptimalTvCenterY - oldOptimalTvCenterY)
                        .scaleX(0.75f)
                        .scaleY(0.75f)
                        .setDuration(DURATION_OPTIMAL_TEXT_TRANSLATION)
                        .setInterpolator(softStopAccDecInterpolator)
                        .start();

                if (mType == Type.AD || mType == Type.CHARGE_SCREEN || mType == Type.NOTIFICATION_CLEANER) {
                    startAdOrChargingScreenResultAnimation(500);
                } else {
                    startCardResultAnimation(500);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        logClickEvent(mType);
    }

}
