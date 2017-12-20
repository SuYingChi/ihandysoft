package com.ihs.feature.cpucooler;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.commons.utils.HSLog;
import com.ihs.device.clean.memory.HSAppMemory;
import com.ihs.device.clean.memory.HSAppMemoryManager;
import com.ihs.device.common.HSAppFilter;
import com.ihs.feature.boost.animation.BoostAnimationManager;
import com.ihs.feature.common.ActivityUtils;
import com.ihs.feature.common.BaseCenterActivity;
import com.ihs.feature.common.LauncherPackageManager;
import com.ihs.feature.common.Utils;
import com.ihs.feature.cpucooler.util.CpuCoolerConstant;
import com.ihs.feature.cpucooler.util.CpuCoolerUtils;
import com.ihs.feature.cpucooler.util.CpuPreferenceHelper;
import com.ihs.feature.cpucooler.view.CircleView;
import com.ihs.feature.cpucooler.view.SnowView;
import com.ihs.feature.resultpage.ResultPageActivity;
import com.ihs.feature.resultpage.ResultPageAdsManager;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

public class CpuCoolDownActivity extends BaseCenterActivity {

    public static final String EXTRA_KEY_SELECTED_APP_LIST = "EXTRA_KEY_SELECTED_APP_LIST";
    public static final String EXTRA_KEY_NEED_SCAN = "EXTRA_KEY_NEED_SCAN";

    private static final long DURATION_FADE_IN = 225;
    private static final long DURATION_SNOW_GROW = 3980;
    private static final long DURATION_CIRCLE_ROTATE = 2900;
    private static final long DURATION_ELEMENTS_FADE_OUT = 400;
    private static final long DELAY_ELEMENTS_FADE_OUT = 480;
    private static final long DELAY_APP_FALL = 1800;
    private static final long DURATION_APP_FALL = 1000;
    private static final long DELAY_AMONG_APPS = 250;
    private static final int APP_ICON_FALL_HEIGHT = 400;
    private static final float APP_ICON_ROTATE_ARC = 90f;

    private static final long[] TIME_SNOW_FALL_CONTROL_1 = { 280, 320, 360 };
    private static final long[] TIME_SNOW_FALL_CONTROL_2 = { 280, 320, 480 };
    private static final long[] TIME_SNOW_FALL_END = { 960, 1120, 1280 };
    private static final float[] ALPHA_FALL_CONTROL_1 = { 0.6f, 0.3f, 0.3f };
    private static final float[] ALPHA_FALL_CONTROL_2 = { 0.6f, 0.3f, 0.3f };
    private static final float[] SNOW_FALL_ROTATE_DEGREE = { 100, 90, 138 };
    private static final float[] SNOW_FALL_START_ANGLE = { 200, 200, 0 };

    private static final long DURATION_POP_BALL_TOTAL = 1400;
    private static final long DURATION_POP_BALL_APPEAR = 375;
    private static final long DURATION_POP_BALL_SPREAD = 375;
    private static final long DELAY_POP_DESC_SCALE = 80;

    private final int mAvailableHeight = CommonUtils.getPhoneHeight(HSApplication.getContext())
            - CommonUtils.getStatusBarHeight(HSApplication.getContext())
            - CommonUtils.getNavigationBarHeight(HSApplication.getContext());
    private final int mScreenWidth = CommonUtils.getPhoneWidth(HSApplication.getContext());

    private SnowView mSnowView;
    private CircleView mCircleView;
    private LinearLayout mAppIconLayout;
    private RelativeLayout mGrowingSnowLayout;
    private TextView mCleanHintTextView;
    private View mPopDescriptionLayout;
    private View mPopBallContainer;
    private View mRevealLayout;
    private TextView mDroppedDownTemperatureTv;

    private boolean mIsVisible = false;
    private boolean mIsStartToResultPage = false;
    private boolean mIsNeedScan;

    private int mRandomCoolDownInCelsius;

    private List<String> mPackageNameList;
    private HSAppMemoryManager.MemoryTaskListener mScanListener;

    private class CircleEnterInterpolator implements Interpolator {
        private float appearFraction;
        private float spreadFraction;
        private float displayCircleScale;

        CircleEnterInterpolator(float appearFraction, float spreadFraction, float displayCircleScale) {
            this.appearFraction = appearFraction;
            this.spreadFraction = spreadFraction;
            this.displayCircleScale = displayCircleScale;
        }

        @Override
        public float getInterpolation(float input) {
            if (input < appearFraction) {
                return (float) ((Math.cos((1 / appearFraction * input + 1) * Math.PI) + 1) * displayCircleScale / 2);
            }

            if (input < 1 - spreadFraction) {
                return displayCircleScale;
            }

            return (float) ((Math.cos((1 / spreadFraction * (input - 1) + 2) * Math.PI) + 1) * (1 - displayCircleScale) / 2 + displayCircleScale);
        }
    }

    @Override
    public boolean isEnableNotificationActivityFinish() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        boolean isFrozen = CpuCoolerUtils.isCpuCoolerScanFrozen();
        HSLog.d(CpuCoolerScanActivity.TAG, "CpuCoolDownActivity isFrozen = " + isFrozen+ " mIsNeedScan = " + mIsNeedScan);
        if (isFrozen) {
            startDoneActivity();
            return;
        }
        setContentView(R.layout.activity_cpu_cooldown);
        initView();
        ResultPageAdsManager.getInstance().preloadAd();
        startElementsFadeInAnimation();
    }

    private void initData() {
        Intent intent = getIntent();
        if (null != intent) {
            mIsNeedScan = intent.getBooleanExtra(EXTRA_KEY_NEED_SCAN, false);
            mPackageNameList = intent.getStringArrayListExtra(EXTRA_KEY_SELECTED_APP_LIST);
        }
        if (mIsNeedScan) {
            startScanApp();
        }
    }

    private void startScanApp() {
        CpuPreferenceHelper.setIsScanCanceled(false);

        HSAppFilter filter = new HSAppFilter().excludeLauncher().excludeList(CpuCoolerUtils.getKeyBoardAppList());
        HSAppMemoryManager.getInstance().setScanGlobalAppFilter(filter);
        mScanListener = new HSAppMemoryManager.MemoryTaskListener() {
            @Override
            public void onStarted() {
            }

            @Override
            public void onProgressUpdated(int processCount, int total, HSAppMemory hsAppMemory) {
            }

            @Override
            public void onSucceeded(List<HSAppMemory> list, long totalScannedSize) {
                if (null != list) {
                    HSLog.d(CpuCoolerScanActivity.TAG, "Cool Down scan list size = " + list.size());
                    if (null == mPackageNameList) {
                        mPackageNameList = new ArrayList<>();
                    } else {
                        mPackageNameList.clear();
                    }
                    for (HSAppMemory app : list) {
                        String packageName = app.getPackageName();
                        mPackageNameList.add(packageName);
                    }
                }
            }

            @Override
            public void onFailed(int i, String s) {
            }
        };
        HSAppMemoryManager.getInstance().startScanWithoutProgress(mScanListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mIsVisible = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mIsVisible = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mScanListener) {
            HSAppMemoryManager.getInstance().stopScan(mScanListener);
            if (mIsNeedScan) {
                CpuPreferenceHelper.setIsScanCanceled(!mIsStartToResultPage);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startClean() {
        List<HSAppMemory> apps = new ArrayList<>();
        if (null != mPackageNameList) {
            for (String packageName : mPackageNameList) {
                apps.add(new HSAppMemory(packageName));
            }
        }

        HSAppMemoryManager.getInstance().startClean(apps, new HSAppMemoryManager.MemoryTaskListener() {
            @Override
            public void onStarted() {
            }

            @Override
            public void onProgressUpdated(int i, int i1, HSAppMemory hsAppMemory) {
            }

            @Override
            public void onSucceeded(List<HSAppMemory> list, long l) {
            }

            @Override
            public void onFailed(int i, String s) {
            }
        });
    }

    private void startElementsFadeInAnimation() {
        mCircleView.postDelayed(() -> {
            ValueAnimator fadeInAnimator = ValueAnimator.ofFloat(0, 1);
            fadeInAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mCircleView.setVisibility(View.VISIBLE);
                    mGrowingSnowLayout.setVisibility(View.VISIBLE);
                    mCleanHintTextView.setVisibility(View.VISIBLE);
                    mRandomCoolDownInCelsius = CpuCoolerManager.getInstance().getRandomCoolDownTemperature();
                    mDroppedDownTemperatureTv.setText(getString(R.string.cpu_cooler_temperature_dropped, String.valueOf(mRandomCoolDownInCelsius)));
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mSnowView.setNeedGrow(true);
                    mSnowView.startRotateAnimation(DURATION_SNOW_GROW, null);

                    mCircleView.startAnimation(DURATION_CIRCLE_ROTATE, () -> new Handler().postDelayed(() -> {
                        if (mIsVisible) {
                            mCircleView.startFadeOutAnimation(DURATION_ELEMENTS_FADE_OUT, null);
                            startElementsFadeOutAnimation();
                            startPopBallAnimation();
                        }
                    }, DELAY_ELEMENTS_FADE_OUT));

                    if (mIsNeedScan) {
                        mCircleView.postDelayed(() -> startFallAnimation(), DELAY_APP_FALL);
                    } else {
                        startFallAnimation();
                    }
                }
            });
            fadeInAnimator.addUpdateListener(animation -> {
                float fraction = animation.getAnimatedFraction();
                mCircleView.setTranslationY((1 - fraction) * 100);
                mCircleView.setAlpha(fraction);
                mCleanHintTextView.setTranslationY((1 - fraction) * 100);
                mCleanHintTextView.setAlpha(fraction);
                mGrowingSnowLayout.setTranslationY((1 - fraction) * 100);
                mGrowingSnowLayout.setAlpha(fraction);
            });
            fadeInAnimator.setDuration(DURATION_FADE_IN).start();
        }, CpuCoolerConstant.DELAY_ANIMATION_FADE_IN);
    }

    private void startFallAnimation() {
        HSLog.d(CpuCoolerScanActivity.TAG, "Cool down startFallAnimation mPackageNameList = " + mPackageNameList);
        if (null == mPackageNameList || mPackageNameList.size() == 0) {
            BoostAnimationManager boostAnimationManager = new BoostAnimationManager(0f, 0f);
            mPackageNameList = boostAnimationManager.getBoostDrawablePackageList(CpuCoolDownActivity.this);
        }
        startAppFallAnimations();
        startSnowFallAnimation();
        startClean();
    }

    private void startSnowFallAnimation() {
        HSAnalytics.logEvent("CPUCooler_CoolAnimation_Start");
        final View[] fallingSnow = { findViewById(R.id.left_falling_snow_view),
                findViewById(R.id.middle_falling_snow_view),
                findViewById(R.id.right_falling_snow_view) };
        final float[] fallStartY = { getResources().getFraction(R.fraction.cpu_left_falling_snow_start_y, mAvailableHeight, 1),
                getResources().getFraction(R.fraction.cpu_middle_falling_snow_start_y, mAvailableHeight, 1),
                getResources().getFraction(R.fraction.cpu_right_falling_snow_start_y, mAvailableHeight, 1) };
        final float[] fallEndY = { getResources().getFraction(R.fraction.cpu_left_falling_snow_end_y, mAvailableHeight, 1),
                getResources().getFraction(R.fraction.cpu_middle_falling_snow_end_y, mAvailableHeight, 1),
                getResources().getFraction(R.fraction.cpu_right_falling_snow_end_y, mAvailableHeight, 1) };

        ValueAnimator fallAnimator = ValueAnimator.ofFloat(0, 1);
        final long maxDuration = Math.max(TIME_SNOW_FALL_END[1], TIME_SNOW_FALL_END[2]);
        fallAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                for (int i = 0; i < 3; i++) {
                    float relativeFraction = animation.getAnimatedFraction() * maxDuration / TIME_SNOW_FALL_END[i];
                    fallingSnow[i].setTranslationY(fallStartY[i] + relativeFraction * (fallEndY[i] - fallStartY[i]));
                    fallingSnow[i].setRotation(SNOW_FALL_START_ANGLE[i] + relativeFraction * SNOW_FALL_ROTATE_DEGREE[i]);
                    long playTime = animation.getCurrentPlayTime();
                    if (playTime < TIME_SNOW_FALL_CONTROL_1[i]) {
                        fallingSnow[i].setAlpha(ALPHA_FALL_CONTROL_1[i] * playTime / TIME_SNOW_FALL_CONTROL_1[i]);
                    } else if (playTime < TIME_SNOW_FALL_CONTROL_2[i]) {
                        fallingSnow[i].setAlpha(ALPHA_FALL_CONTROL_1[i] + (ALPHA_FALL_CONTROL_2[i] - ALPHA_FALL_CONTROL_1[i]) * (playTime - TIME_SNOW_FALL_CONTROL_1[i]) / (TIME_SNOW_FALL_CONTROL_2[i] - TIME_SNOW_FALL_CONTROL_1[i]));
                    } else if (playTime < TIME_SNOW_FALL_END[i]) {
                        fallingSnow[i].setAlpha(ALPHA_FALL_CONTROL_2[i] * (1 - (float) (playTime - TIME_SNOW_FALL_CONTROL_2[i]) / (TIME_SNOW_FALL_END[i] - TIME_SNOW_FALL_CONTROL_2[i])));
                    } else {
                        fallingSnow[i].setAlpha(0);
                    }
                }
            }
        });
        fallAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                for (int i = 0; i < 3; i++) {
                    fallingSnow[i].setVisibility(View.VISIBLE);
                    fallingSnow[i].setTranslationY(fallStartY[i]);
                    fallingSnow[i].setRotation(SNOW_FALL_START_ANGLE[i]);
                }
            }
        });
        fallAnimator.setDuration(maxDuration).start();
    }

    private void startAppFallAnimations() {
        int appIconBound = (int) getResources().getFraction(R.fraction.cpu_falling_app_icon_bound, mScreenWidth, 1);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(appIconBound, appIconBound);
        llp.weight = 1;
        if (null != mPackageNameList) {
            for (int i = 0; i < 4 && i < mPackageNameList.size(); i++) {
                ImageView appIconView = new ImageView(this);
                appIconView.setLayoutParams(llp);
                appIconView.setImageDrawable(LauncherPackageManager.getInstance().getApplicationIcon(mPackageNameList.get(i)));
                appIconView.setVisibility(View.INVISIBLE);
                mAppIconLayout.addView(appIconView);
                startSingleIconFallAnimation(appIconView, i * DELAY_AMONG_APPS);
            }
        }
    }

    private void startSingleIconFallAnimation(final ImageView iconView, final long delay) {
        final boolean rotateClockwise = Math.random() > 0.5;
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(iconView, "alpha", 0f, 1f, 0f);
        alphaAnimator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            iconView.setTranslationY(fraction * APP_ICON_FALL_HEIGHT);
            iconView.setRotation((rotateClockwise ? 1 : -1) * fraction * APP_ICON_ROTATE_ARC);
        });
        alphaAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                iconView.setVisibility(View.VISIBLE);
            }
        });
        alphaAnimator.setDuration(DURATION_APP_FALL).setStartDelay(delay);
        alphaAnimator.start();
    }

    private void startElementsFadeOutAnimation() {
        ValueAnimator alphaAnimator = ValueAnimator.ofFloat(0, 1);
        alphaAnimator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            mGrowingSnowLayout.setAlpha(1 - fraction);
            mCleanHintTextView.setAlpha(1 - fraction);
        });
        alphaAnimator.setDuration(DURATION_ELEMENTS_FADE_OUT).start();
    }

    private void startPopBallAnimation() {
        float ballMaxRadius = (float) Math.hypot(DisplayUtils.getScreenHeightPixels(), DisplayUtils.getScreenWidthPixels());
        float ballDisplayRadius = getResources().getFraction(R.fraction.cpu_circle_bound, mScreenWidth, 1) / 2;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mRevealLayout.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        Animator circularReveal = null;
        if (CommonUtils.ATLEAST_LOLLIPOP) {
            circularReveal = ViewAnimationUtils.createCircularReveal(mPopBallContainer, mPopBallContainer.getWidth() / 2,
                    mPopBallContainer.getHeight() - mPopDescriptionLayout.getHeight() / 2, 0f, ballMaxRadius);
            circularReveal.setDuration(DURATION_POP_BALL_TOTAL).setInterpolator(new CircleEnterInterpolator((float) DURATION_POP_BALL_APPEAR / DURATION_POP_BALL_TOTAL,
                    (float) DURATION_POP_BALL_SPREAD / DURATION_POP_BALL_TOTAL, ballDisplayRadius / ballMaxRadius));
        }

        ValueAnimator mPopBallContainerFadeOut = ValueAnimator.ofFloat(0, 1);
        mPopBallContainerFadeOut.setInterpolator(new FastOutSlowInInterpolator());
        mPopBallContainerFadeOut.setDuration(DURATION_POP_BALL_SPREAD).setStartDelay(DURATION_POP_BALL_TOTAL - DURATION_POP_BALL_SPREAD);
        mPopBallContainerFadeOut.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();

            mPopDescriptionLayout.setAlpha(1 - animation.getAnimatedFraction());
            mPopDescriptionLayout.setTranslationY(-80 * fraction);
        });

        ValueAnimator popBallScaleX = ObjectAnimator.ofFloat(mPopDescriptionLayout, "scaleX", 1f, 1.2f, 1f);
        ValueAnimator popBallScaleY = ObjectAnimator.ofFloat(mPopDescriptionLayout, "scaleY", 1f, 1.2f, 1f);
        AnimatorSet scaleSet = new AnimatorSet();
        scaleSet.setDuration(DURATION_POP_BALL_SPREAD).setInterpolator(new FastOutSlowInInterpolator());
        scaleSet.playTogether(popBallScaleX, popBallScaleY);
        scaleSet.setStartDelay(DELAY_POP_DESC_SCALE);

        AnimatorSet endAnimatorSet = new AnimatorSet();
        endAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                RelativeLayout.LayoutParams flp = (RelativeLayout.LayoutParams) mRevealLayout.getLayoutParams();
                flp.addRule(RelativeLayout.ALIGN_BOTTOM, 0);
                mRevealLayout.setLayoutParams(flp);
                mPopBallContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mIsVisible) {
                    startResultActivity();
                }
            }
        });
        if (null == circularReveal) {
            endAnimatorSet.playTogether(mPopBallContainerFadeOut, scaleSet);
        } else {
            endAnimatorSet.playTogether(circularReveal, mPopBallContainerFadeOut, scaleSet);
        }
        endAnimatorSet.start();
    }

    private void startResultActivity() {
        mIsStartToResultPage = true;
        HSLog.d(CpuCoolerScanActivity.TAG, "Cpu cool down startResultActivity mRandomCoolDownInCelsius = " + mRandomCoolDownInCelsius);
        CpuPreferenceHelper.setLastCpuCoolerFinishTime();
        ResultPageActivity.startForCpuCooler(CpuCoolDownActivity.this);
        finish();
    }

    private void initView() {
        mSnowView = (SnowView) findViewById(R.id.growing_snow_view);
        mCircleView = (CircleView) findViewById(R.id.circle_view);
        mAppIconLayout = (LinearLayout) findViewById(R.id.app_icon_layout);
        mGrowingSnowLayout = (RelativeLayout) findViewById(R.id.growing_snow_layout);
        mCleanHintTextView = (TextView) findViewById(R.id.close_app_hint_tv);
        mPopBallContainer = findViewById(R.id.pop_ball_container);
        mPopDescriptionLayout = findViewById(R.id.pop_description_layout);
        mRevealLayout = findViewById(R.id.reveal_layout);
        mDroppedDownTemperatureTv = (TextView) findViewById(R.id.dropped_down_temperature_tv);

        Utils.setupTransparentSystemBarsForLmpNoNavigation(this);
        ActivityUtils.setNavigationBarColor(this, ContextCompat.getColor(this, android.R.color.black));
    }

    private void startDoneActivity() {
        HSLog.d(CpuCoolerScanActivity.TAG, "Cpu cool down startDoneActivity");
        ResultPageActivity.startForCpuCooler(CpuCoolDownActivity.this);
        finish();
    }

}
