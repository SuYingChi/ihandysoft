package com.ihs.feature.resultpage;

import android.animation.TimeInterpolator;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.acb.adadapter.AcbAd;
import com.acb.adadapter.AcbNativeAd;
import com.honeycomb.launcher.R;
import com.honeycomb.launcher.ad.AdPlacements;
import com.honeycomb.launcher.animation.LauncherAnimationUtils;
import com.honeycomb.launcher.resultpage.data.CardData;
import com.honeycomb.launcher.resultpage.data.ResultConstants;
import com.honeycomb.launcher.util.ViewUtils;
import com.honeycomb.launcher.view.FlashCircleView;
import com.ihs.app.analytics.HSAnalytics;

import java.util.List;

class NotificationCleanerResultController extends ResultController {

    private static final long START_DELAY_FLASH_CIRCLE = 620;
    private static final long DURATION_OPTIMAL_TEXT_TRANSLATION = 640;

    private TextView mDescriptionTv;

    NotificationCleanerResultController(ResultPageActivity activity, Type type, @Nullable AcbNativeAd ad, List<CardData> cardDataList, int clearNotificationsCount) {
        super.init(activity, ResultConstants.RESULT_TYPE_NOTIFICATION_CLEANER, type, ad, cardDataList);
        if (null != mDescriptionTv && clearNotificationsCount > 0) {
            mDescriptionTv.setVisibility(View.VISIBLE);
            mDescriptionTv.setText(activity.getString(R.string.notification_cleaner_cleared_up, String.valueOf(clearNotificationsCount)));
        }
        if (null != ad) {
            HSAnalytics.logEvent(AdPlacements.SHARED_POOL_NATIVE_AD_FLURRY_EVENT_SHOWN_NAME_RESULT_PAGE, "Type", "NotificationCleanerDone");
            ad.setNativeClickListener(new AcbNativeAd.AcbNativeClickListener() {
                @Override
                public void onAdClick(AcbAd acbAd) {
                    HSAnalytics.logEvent(AdPlacements.SHARED_POOL_NATIVE_AD_FLURRY_EVENT_CLICKED_NAME_RESULT_PAGE, "Type", "NotificationCleanerDone");
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
        final TextView labelTv = ViewUtils.findViewById(transitionView, R.id.label_title);
        if (null != labelTv) {
            labelTv.setText(R.string.notification_cleaner_deleted);
        }
        mDescriptionTv = ViewUtils.findViewById(transitionView, R.id.description_tv);
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

                if (null != mDescriptionTv) {
                    AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0f);
                    alphaAnimation.setDuration(500);
                    alphaAnimation.setAnimationListener(new LauncherAnimationUtils.AnimationListenerAdapter(){
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            super.onAnimationEnd(animation);
                            mDescriptionTv.setVisibility(View.GONE);
                        }
                    });
                    mDescriptionTv.startAnimation(alphaAnimation);
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
