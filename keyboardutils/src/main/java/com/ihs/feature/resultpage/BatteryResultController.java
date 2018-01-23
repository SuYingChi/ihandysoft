package com.ihs.feature.resultpage;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.ihs.feature.common.AnimatorListenerAdapter;
import com.ihs.feature.common.ViewUtils;
import com.ihs.feature.resultpage.data.CardData;
import com.ihs.feature.resultpage.data.ResultConstants;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.CommonUtils;
import com.ihs.keyboardutils.utils.LauncherAnimationUtils;

import net.appcloudbox.ads.base.AcbAd;
import net.appcloudbox.ads.base.AcbNativeAd;

import java.util.List;
@SuppressLint("NewApi")
class BatteryResultController extends ResultController {

    private boolean mIsOptimal;
    private String mExtendHour;
    private String mExtendMinute;

    private static final long DURATION_TEXT_LINE_O_DISAPPEAR = 2 * FRAME;
    private static final long DURATION_TEXT_TRANSLATE = 10 * FRAME;
    private static final long DURATION_TEXT_ONLY_APPEAR = DURATION_TEXT_TRANSLATE;
    private static final long START_OFF_TEXT_ONLY_APPEAR = DURATION_TEXT_TRANSLATE;

    private static final long START_OFF_BALL_DISAPPEAR = FRAME_HALF;
    private static final long START_OFF_BALL_TRANSLATE = FRAME_HALF;

    private AccelerateInterpolator mAccelerateInterpolator = new AccelerateInterpolator();

    private float mTextLineOnlyPositionStartY;
    private float mTextLineOnlyPositionEndY;

    private float mTextLineIPositionStartY;
    private float mTextLineIPositionEndY;

    BatteryResultController(ResultPageActivity activity, boolean isOptimal,
                            String extendHour, String extendMinute, Type type, @Nullable AcbNativeAd ad, List<CardData> cardDataList) {
        mIsOptimal = isOptimal;
        mExtendHour = extendHour;
        mExtendMinute = extendMinute;
        super.init(activity, ResultConstants.RESULT_TYPE_BATTERY, type, ad, cardDataList);
        if (ad != null) {
            ad.setNativeClickListener(new AcbNativeAd.AcbNativeClickListener() {
                @Override
                public void onAdClick(AcbAd acbAd) {

                }
            });
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.result_page_battery_transition;
    }

    @Override
    protected void onFinishInflateTransitionView(View transitionView) {
        View cleaningContentView = ViewUtils.findViewById(transitionView, R.id.cleaning_content_rl);
        if (null != cleaningContentView) {
            cleaningContentView.setVisibility(View.VISIBLE);
        }

        View cleaningView = ViewUtils.findViewById(transitionView, R.id.cleaning_ll);
        if (null != cleaningView) {
            cleaningView.setVisibility(View.INVISIBLE);
        }

        View ringView = ViewUtils.findViewById(transitionView, R.id.cleaning_ring_iv);
        if (null != ringView) {
            ringView.setVisibility(View.INVISIBLE);
        }

        View rightSymbolView = ViewUtils.findViewById(transitionView, R.id.scan_right_symbol_iv);
        if (null != rightSymbolView) {
            rightSymbolView.setVisibility(View.VISIBLE);
        }

        View cleanResultView = ViewUtils.findViewById(transitionView, R.id.clean_result_ll);
        if (null != cleanResultView) {
            cleanResultView.setVisibility(View.VISIBLE);
        }

        TextView resultDescriptionLineOTv = ViewUtils.findViewById(transitionView, R.id.clean_result_description_line_o_tv);
        TextView resultDescriptionLineITv = ViewUtils.findViewById(transitionView, R.id.clean_result_description_line_i_tv);
        View saveTimeView = ViewUtils.findViewById(transitionView, R.id.save_time_ll);
        if (mIsOptimal) {
            if (null != resultDescriptionLineOTv) {
                resultDescriptionLineOTv.setText(mActivity.getString(R.string.battery_already_optimal));
            }
            if (null != resultDescriptionLineITv) {
                resultDescriptionLineITv.setVisibility(View.GONE);
            }
            if (null != saveTimeView) {
                saveTimeView.setVisibility(View.INVISIBLE);
            }
        } else {
            if (null != resultDescriptionLineOTv) {
                resultDescriptionLineOTv.setText(mActivity.getString(R.string.battery_clean_finished_line_o));
            }
            if (null != resultDescriptionLineITv) {
                resultDescriptionLineITv.setText(mActivity.getString(R.string.battery_clean_finished_line_i));
            }

            TextView saveHourTimeTv = ViewUtils.findViewById(transitionView, R.id.save_hour_time_tv);
            if (null != saveHourTimeTv) {
                if (!TextUtils.isEmpty(mExtendHour)) {
                    saveHourTimeTv.setText(mExtendHour);
                }
            }

            TextView saveMinuteTimeTv = ViewUtils.findViewById(transitionView, R.id.save_minute_time_tv);
            if (null != saveMinuteTimeTv) {
                if (!TextUtils.isEmpty(mExtendMinute)) {
                    saveMinuteTimeTv.setText(mExtendMinute);
                }
            }
        }
    }

    @Override
    protected void onStartTransitionAnimation(View transitionView) {
        final View toolbarSpaceView = ViewUtils.findViewById(transitionView, R.id.battery_cleaning_toolbar_space_v);
        final View titleTagView = ViewUtils.findViewById(transitionView, R.id.description_title_tag_ll);

        final TextView resultDescriptionLineOTv = ViewUtils.findViewById(transitionView, R.id.clean_result_description_line_o_tv);
        final TextView resultDescriptionLineITv = ViewUtils.findViewById(transitionView, R.id.clean_result_description_line_i_tv);

        final TextView resultDescriptionTitleTagTv = ViewUtils.findViewById(transitionView, R.id.description_title_tag_tv);
        final TextView resultDescriptionContentTagTv = ViewUtils.findViewById(transitionView, R.id.description_content_tag_tv);
        final TextView resultDescriptionOnlyTitleTagTv = ViewUtils.findViewById(transitionView, R.id.description_only_title_tag_tv);

        final View scanningView = ViewUtils.findViewById(transitionView, R.id.scanning_rl);
        final View cleaningBallView = ViewUtils.findViewById(transitionView, R.id.cleaning_ball_iv);
        final View saveTimeView = ViewUtils.findViewById(transitionView, R.id.save_time_ll);

        toolbarSpaceView.setVisibility(View.VISIBLE);
        cleaningBallView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                cleaningBallView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                scanningView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int top = ViewUtils.getLocationRect(cleaningBallView).top;
                        int bottom = ViewUtils.getLocationRect(cleaningBallView).bottom;
                        float translateDistance = top + Math.abs(bottom - top);

                        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, -translateDistance);
                        translateAnimation.setDuration(DURATION_BALL_TRANSLATE);

                        AlphaAnimation disappearAlphaAnimation = new AlphaAnimation(1.0f, 0f);
                        disappearAlphaAnimation.setDuration(DURATION_BALL_TRANSLATE);
                        disappearAlphaAnimation.setStartOffset(START_OFF_BALL_DISAPPEAR);

                        AnimationSet animationSet = new AnimationSet(false);
                        animationSet.addAnimation(translateAnimation);
                        animationSet.addAnimation(disappearAlphaAnimation);
                        animationSet.setInterpolator(mAccelerateInterpolator);
                        animationSet.setAnimationListener(new LauncherAnimationUtils.AnimationListenerAdapter(){
                            @Override
                            public void onAnimationStart(Animation animation) {
                                super.onAnimationStart(animation);
                                scanningView.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                super.onAnimationEnd(animation);
                                scanningView.setVisibility(View.GONE);
                            }
                        });
                        scanningView.startAnimation(animationSet);
                    }
                }, START_OFF_BALL_TRANSLATE);
            }
        });

        if (mIsOptimal) {
            titleTagView.setVisibility(View.INVISIBLE);
            resultDescriptionTitleTagTv.setVisibility(View.GONE);
            resultDescriptionContentTagTv.setVisibility(View.GONE);
            resultDescriptionOnlyTitleTagTv.setVisibility(View.VISIBLE);

            resultDescriptionLineOTv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    resultDescriptionLineOTv.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    mTextLineOnlyPositionStartY = ViewUtils.getLocationRect(resultDescriptionLineOTv).top;
                    startOnlyTextAnimation(resultDescriptionLineOTv, titleTagView, resultDescriptionOnlyTitleTagTv);
                }
            });

            resultDescriptionOnlyTitleTagTv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    resultDescriptionOnlyTitleTagTv.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    mTextLineOnlyPositionEndY = ViewUtils.getLocationRect(resultDescriptionOnlyTitleTagTv).top;
                    startOnlyTextAnimation(resultDescriptionLineOTv, titleTagView, resultDescriptionOnlyTitleTagTv);
                }
            });
        } else {
            titleTagView.setVisibility(View.INVISIBLE);
            resultDescriptionTitleTagTv.setVisibility(View.VISIBLE);
            resultDescriptionContentTagTv.setVisibility(View.VISIBLE);
            resultDescriptionOnlyTitleTagTv.setVisibility(View.GONE);

            AlphaAnimation textAlphaAnimation = new AlphaAnimation(1.0f, 0f);
            textAlphaAnimation.setDuration(DURATION_TEXT_LINE_O_DISAPPEAR);
            textAlphaAnimation.setAnimationListener(new LauncherAnimationUtils.AnimationListenerAdapter(){
                @Override
                public void onAnimationStart(Animation animation) {
                    super.onAnimationStart(animation);
                    resultDescriptionLineOTv.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    super.onAnimationEnd(animation);
                    resultDescriptionLineOTv.setVisibility(View.INVISIBLE);
                }
            });
            resultDescriptionLineOTv.startAnimation(textAlphaAnimation);

            resultDescriptionLineITv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    resultDescriptionLineITv.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    mTextLineIPositionStartY = ViewUtils.getLocationRect(resultDescriptionLineITv).top;
                    startTextTranslateAnimation(resultDescriptionLineITv, saveTimeView);
                }
            });

            resultDescriptionTitleTagTv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    resultDescriptionTitleTagTv.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    mTextLineIPositionEndY = ViewUtils.getLocationRect(resultDescriptionTitleTagTv).top;
                    startTextTranslateAnimation(resultDescriptionLineITv, saveTimeView);
                }
            });
        }
    }

    private void startTextTranslateAnimation(final View resultDescriptionLineITv, final View saveTimeView) {
        if (0 != mTextLineIPositionStartY && 0 != mTextLineIPositionEndY) {
            final int margin = CommonUtils.pxFromDp(10);
            final float distance = Math.abs(mTextLineIPositionEndY - mTextLineIPositionStartY);
            ValueAnimator lineTranslateAnimator = ValueAnimator.ofFloat(0.0f, -distance);
            lineTranslateAnimator.setDuration(DURATION_TEXT_TRANSLATE);
            lineTranslateAnimator.setInterpolator(mAccelerateInterpolator);
            lineTranslateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float floatValue = (float) animation.getAnimatedValue();
                    resultDescriptionLineITv.setTranslationY(floatValue);
                    saveTimeView.setTranslationY(floatValue - margin * Math.abs(floatValue) / distance);
                }
            });
            lineTranslateAnimator.start();
            mTextLineIPositionStartY = 0;
            mTextLineIPositionEndY = 0;
        }
    }

    private void startOnlyTextAnimation(final View animationView, final View titleTagView, final View resultDescriptionOnlyTitleTagTv) {
        if (0 != mTextLineOnlyPositionStartY && 0 != mTextLineOnlyPositionEndY) {
            final float distance = Math.abs(mTextLineOnlyPositionEndY - mTextLineOnlyPositionStartY);
            ValueAnimator lineTranslateAnimator = ValueAnimator.ofFloat(0.0f, -distance);
            lineTranslateAnimator.setDuration(DURATION_TEXT_TRANSLATE);
            lineTranslateAnimator.setInterpolator(mAccelerateInterpolator);
            lineTranslateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float floatValue = (float) animation.getAnimatedValue();
                    float progress = animation.getAnimatedFraction();
                    float disappearFraction = 1 - progress;
                    animationView.setTranslationY(floatValue);
                    animationView.setScaleX(disappearFraction);
                    animationView.setScaleY(disappearFraction);
                    animationView.setAlpha(disappearFraction);
                }
            });
            lineTranslateAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    animationView.setVisibility(View.INVISIBLE);
                    animationView.setTranslationY(0);
                    animationView.setScaleY(1.0f);
                    animationView.setScaleX(1.0f);
                    animationView.setAlpha(1.0f);
                }
            });
            lineTranslateAnimator.start();
            mTextLineOnlyPositionStartY = 0;
            mTextLineOnlyPositionEndY = 0;

            // Only title appear
            titleTagView.setVisibility(View.VISIBLE);
            resultDescriptionOnlyTitleTagTv.setVisibility(View.VISIBLE);
            AlphaAnimation onlyTitleAlphaAnimation = new AlphaAnimation(0f, 1.0f);
            onlyTitleAlphaAnimation.setDuration(DURATION_TEXT_ONLY_APPEAR);
            onlyTitleAlphaAnimation.setStartOffset(START_OFF_TEXT_ONLY_APPEAR);
            resultDescriptionOnlyTitleTagTv.startAnimation(onlyTitleAlphaAnimation);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }
}
