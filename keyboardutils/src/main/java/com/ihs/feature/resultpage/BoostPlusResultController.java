package com.ihs.feature.resultpage;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.graphics.drawable.ClipDrawable;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ihs.commons.utils.HSLog;
import com.ihs.feature.boost.plus.BoostBgImageView;
import com.ihs.feature.common.AnimatorListenerAdapter;
import com.ihs.feature.common.DeviceManager;
import com.ihs.feature.common.LauncherAnimUtils;
import com.ihs.feature.common.Thunk;
import com.ihs.feature.common.ViewUtils;
import com.ihs.feature.resultpage.data.CardData;
import com.ihs.feature.resultpage.data.ResultConstants;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.LauncherAnimationUtils;

import net.appcloudbox.ads.base.AcbAd;
import net.appcloudbox.ads.base.AcbNativeAd;

import java.util.List;

import static com.ihs.feature.boost.plus.BoostPlusCleanDialog.DEVICE_SCREEN_HEIGHT_TAG;
import static com.ihs.feature.boost.plus.BoostPlusCleanDialog.START_OFF_CIRCLE_ROTATE_MAIN;

@SuppressWarnings("WeakerAccess")
class BoostPlusResultController extends ResultController {

    private static final int PERCENT_FROM_Y_DELTA = 156;
    private static final int BOOST_FROM_Y_DELTA = 216;

    // Result percentage
    private static final long DURATION_RESULT_START_OFF = START_OFF_CIRCLE_ROTATE_MAIN + 7 * FRAME;
    private static final long DURATION_RESULT_PERCENT_ALPHA_ADD = 5 * FRAME;

    // Result boosted
    private static final long DURATION_RESULT_BOOSTED_ALPHA_ADD = 8 * FRAME;

    private static final int TICK_LEVEL_ACCELERATE = 60;
    private static final int TICK_BG_LEVEL_ACCELERATE = 80;

    // Tick
    private static final long DURATION_TICK = 9 * FRAME;
    private static final long START_OFFSET_TICK = 3 * FRAME;
    private static final long START_OFFSET_MAIN_TICK = START_OFF_CIRCLE_ROTATE_MAIN;
    private static final int CLIP_LEVEL_TICK_BG_START = 1000;
    private static final int CLIP_LEVEL_TICK_START = 100;
    private static final int CLIP_LEVEL_TICK_BG_END = 9000;
    private static final int CLIP_LEVEL_TICK_END = 9500;
    private static final int CLIP_MAX_LEVEL = 10000;
    private static final int CLIP_BG_TIMES = 40;
    private static final int CLIP_TICK_TIMES = 40;
    private static final long CLIP_INTERVAL_BG = DURATION_TICK / CLIP_BG_TIMES;
    private static final long CLIP_INTERVAL_TICK = DURATION_TICK / CLIP_TICK_TIMES;
    private static final int TICK_BG_LEVEL_INTERVAL = (CLIP_LEVEL_TICK_BG_END - CLIP_LEVEL_TICK_BG_START) / CLIP_BG_TIMES;
    private static final int TICK_LEVEL_INTERVAL = (CLIP_LEVEL_TICK_END - CLIP_LEVEL_TICK_START) / CLIP_TICK_TIMES;

    private static final long DURATION_FADE_OUT = 200;
    private static final long DURATION_SLIDE_OUT = 400;
    private static final long DURATION_OPTIMAL_TEXT_TRANSLATION = 640;

    private int mCleanedSizeMbs;

    private View mTitleAnchor;
    @Thunk RelativeLayout mTickRl;
    @Thunk
    BoostBgImageView mTickBgIv;
    @Thunk
    ImageView mTickIv;
    @Thunk TextView mOptimalTv;
    @Thunk TextView mFreedUpNumberTv;
    private TextView mFreedUpTv;

    @Thunk ClipDrawable mBoostTickClipDrawable;
    @Thunk ClipDrawable mBoostTickBgClipDrawable;

    @Thunk int mTickLevelInterval = TICK_LEVEL_INTERVAL;
    @Thunk int mTickLevelBgInterval = TICK_BG_LEVEL_INTERVAL;

    @Thunk boolean mIsTickBgFirstStart = true;
    @Thunk boolean mIsTickFirstStart = true;

    @Thunk Handler mHandler = new Handler();

    private View[] mSlideOutViews;
    private View[] mFadeOutViews;

    private int mBatteryLevel;

    BoostPlusResultController(ResultPageActivity activity, int cleanedSizeMbs, Type type, @Nullable AcbNativeAd ad, List<CardData> cardDataList) {
        super(activity, ResultConstants.RESULT_TYPE_BOOST_PLUS, type, ad, cardDataList);
        HSLog.d(TAG, "BoostPlusResultController ***");
        mCleanedSizeMbs = cleanedSizeMbs;
        mBatteryLevel = DeviceManager.getInstance().getBatteryLevel();
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
        return R.layout.result_page_boost_plus_transition;
    }

    @Override
    protected void onFinishInflateTransitionView(View transitionView) {
        HSLog.d(TAG, "BoostPlusResultController onFinishInflateTransitionView");
        mTitleAnchor = ViewUtils.findViewById(transitionView, R.id.description_title_tag_tv);
        mTickRl = ViewUtils.findViewById(transitionView, R.id.tick_rl);
        mTickBgIv = ViewUtils.findViewById(transitionView, R.id.tick_bg);
        mTickIv = ViewUtils.findViewById(transitionView, R.id.tick_iv);
        mOptimalTv = ViewUtils.findViewById(transitionView, R.id.optimal_tv);
        mFreedUpNumberTv = ViewUtils.findViewById(transitionView, R.id.freed_up_number_tv);
        mFreedUpTv = ViewUtils.findViewById(transitionView, R.id.freed_up_tv);

        mSlideOutViews = new View[]{mTickBgIv, mTickIv};
        mFadeOutViews = new View[]{mFreedUpNumberTv, mFreedUpTv};

        mBoostTickClipDrawable = (ClipDrawable) mTickIv.getDrawable();
        mBoostTickBgClipDrawable = (ClipDrawable) mTickBgIv.getDrawable();
    }

    @Override
    protected void onStartTransitionAnimation(View transitionView) {
        HSLog.d(TAG, "BoostPlusResultController onStartTransitionAnimation mTransitionView = " + transitionView);
        startCleanResultSizeAnimation();
        startTickAnimation();
    }

    private void startCleanResultSizeAnimation() {
        HSLog.d(TAG, "startCleanResultSizeAnimation");
        String cleanPercentRandomText = mCleanedSizeMbs + getContext().getString(R.string.megabyte_abbr);
        mFreedUpNumberTv.setText(cleanPercentRandomText);

        float percentFromYDelta = mScreenHeight * PERCENT_FROM_Y_DELTA / DEVICE_SCREEN_HEIGHT_TAG;
        Animation cleanPercentAlphaAppearAnimation = LauncherAnimationUtils.getAlphaAppearAnimation(
                DURATION_RESULT_PERCENT_ALPHA_ADD, DURATION_RESULT_START_OFF);
        Runnable resultRunnable = new Runnable() {
            @Override
            public void run() {
                mFreedUpNumberTv.setVisibility(View.VISIBLE);
            }
        };
        mHandler.postDelayed(resultRunnable, DURATION_RESULT_START_OFF);

        Animation cleanPercentTranslateAnimation = LauncherAnimationUtils.getTranslateYAnimation(
                percentFromYDelta, 0, DURATION_RESULT_PERCENT_ALPHA_ADD,
                DURATION_RESULT_START_OFF, true, new DecelerateInterpolator());
        LauncherAnimationUtils.startSetAnimation(mFreedUpNumberTv, new LauncherAnimationUtils.AnimationListenerAdapter(){
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        super.onAnimationEnd(animation);
                        startCardResultAnimation(0);
                        startRealTransitionAnimation();
                    }
                }, cleanPercentAlphaAppearAnimation, cleanPercentTranslateAnimation);

        float boostFromYDelta = mScreenHeight * BOOST_FROM_Y_DELTA / DEVICE_SCREEN_HEIGHT_TAG;
        Animation cleanBoostAlphaAppearAnimation = LauncherAnimationUtils.getAlphaAppearAnimation(
                DURATION_RESULT_BOOSTED_ALPHA_ADD, DURATION_RESULT_START_OFF);
        Animation cleanBoostTranslateAnimation = LauncherAnimationUtils.getTranslateYAnimation(
                boostFromYDelta, 0, DURATION_RESULT_PERCENT_ALPHA_ADD, DURATION_RESULT_START_OFF,
                true, new DecelerateInterpolator());
        LauncherAnimationUtils.startSetAnimation(mFreedUpTv, false,
                cleanBoostAlphaAppearAnimation, cleanBoostTranslateAnimation);
    }

    private void startTickAnimation() {
        HSLog.d(TAG, "BoostPlusResultController startTickAnimation");
        mIsTickBgFirstStart = true;
        Runnable tickRunnable = new Runnable() {
            @Override
            public void run() {
                mTickRl.setVisibility(View.VISIBLE);
                int currentLevel = mBoostTickClipDrawable.getLevel() + mTickLevelInterval;
                mTickLevelInterval += TICK_LEVEL_ACCELERATE;
                if (mIsTickFirstStart) {
                    currentLevel = CLIP_LEVEL_TICK_START;
                }
                mIsTickFirstStart = false;

                if (mBoostTickClipDrawable.getLevel() < CLIP_LEVEL_TICK_END) {
                    mHandler.postDelayed(this, CLIP_INTERVAL_TICK);
                } else {
                    currentLevel = CLIP_MAX_LEVEL;
                }

                mBoostTickClipDrawable.setLevel(currentLevel);

                float currentAlpha = (float) currentLevel / CLIP_MAX_LEVEL;
                mTickIv.setAlpha(currentAlpha);
            }
        };

        Runnable tickBgRunnable = new Runnable() {
            @Override
            public void run() {
                mTickRl.setVisibility(View.VISIBLE);
                int currentLevel = mBoostTickBgClipDrawable.getLevel() + mTickLevelBgInterval;
                mTickLevelBgInterval += TICK_BG_LEVEL_ACCELERATE;
                if (mIsTickBgFirstStart) {
                    // optimal alpha appear animation
                    Animation optimalAlphaAppearAnimation = LauncherAnimationUtils.getAlphaAppearAnimation(DURATION_TICK, 0);
                    LauncherAnimationUtils.startAnimation(mOptimalTv, false, optimalAlphaAppearAnimation);
                    currentLevel = CLIP_LEVEL_TICK_BG_START;
                }
                mIsTickBgFirstStart = false;

                if (mBoostTickBgClipDrawable.getLevel() < CLIP_LEVEL_TICK_BG_END) {
                    mHandler.postDelayed(this, CLIP_INTERVAL_BG);
                } else {
                    currentLevel = CLIP_MAX_LEVEL;
                }
                mBoostTickBgClipDrawable.setLevel(currentLevel);

                float currentAlpha = (float) currentLevel / CLIP_MAX_LEVEL;
                mTickBgIv.setAlpha(currentAlpha);
            }
        };

        mHandler.postDelayed(tickRunnable, START_OFFSET_MAIN_TICK);
        mHandler.postDelayed(tickBgRunnable, START_OFFSET_MAIN_TICK + START_OFFSET_TICK);
    }

    private void startRealTransitionAnimation() {
        for (final View v : mFadeOutViews) {
            v.animate()
                    .alpha(0f)
                    .setDuration(DURATION_FADE_OUT)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            v.setVisibility(View.INVISIBLE);
                        }
                    })
                    .start();
        }

        int[] location = new int[2];
        mTickBgIv.getLocationInWindow(location);
        int slideUpTranslation = location[1] + mTickBgIv.getHeight();

        for (View v : mSlideOutViews) {
            v.animate()
                    .translationYBy(-slideUpTranslation)
                    .alpha(0f)
                    .setDuration(DURATION_SLIDE_OUT)
                    .setInterpolator(LauncherAnimUtils.ACCELERATE_QUAD)
                    .start();
        }

        mOptimalTv.getLocationInWindow(location);
        int oldOptimalTvCenterY = location[1] + mOptimalTv.getHeight() / 2;
        mTitleAnchor.getLocationInWindow(location);
        int newOptimalTvCenterY = location[1] + mTitleAnchor.getHeight() / 2;

        TimeInterpolator softStopAccDecInterpolator = PathInterpolatorCompat.create(0.79f, 0.37f, 0.28f, 1f);
        mOptimalTv.animate()
                .translationYBy(newOptimalTvCenterY - oldOptimalTvCenterY)
                .scaleX(1.8f)
                .scaleY(1.8f)
                .setDuration(DURATION_OPTIMAL_TEXT_TRANSLATION)
                .setInterpolator(softStopAccDecInterpolator)
                .start();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }
}
