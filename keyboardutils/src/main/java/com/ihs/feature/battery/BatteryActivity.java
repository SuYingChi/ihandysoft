package com.ihs.feature.battery;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.artw.lockscreen.common.NavUtils;
import com.ihs.aidl.ISubAdService;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.charging.HSChargingManager;
import com.ihs.chargingscreen.utils.ChargingPrefsUtil;
import com.ihs.commons.utils.HSLog;
import com.ihs.device.clean.memory.HSAppMemoryManager;
import com.ihs.feature.boost.BoostTipUtils;
import com.ihs.feature.boost.animation.DynamicRotateAnimation;
import com.ihs.feature.common.ActivityUtils;
import com.ihs.feature.common.AnimatorListenerAdapter;
import com.ihs.feature.common.BaseCenterActivity;
import com.ihs.feature.common.CustomRootView;
import com.ihs.feature.common.DeviceManager;
import com.ihs.feature.common.LauncherAnimUtils;
import com.ihs.feature.common.ScreenStatusReceiver;
import com.ihs.feature.common.SubAdService;
import com.ihs.feature.common.Utils;
import com.ihs.feature.common.VectorCompat;
import com.ihs.feature.common.ViewUtils;
import com.ihs.feature.resultpage.ResultPageActivity;
import com.ihs.feature.ui.HeuristicAnimator;
import com.ihs.feature.ui.SeekCircleProgressBar;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.CommonUtils;
import com.ihs.keyboardutils.utils.LauncherAnimationUtils;

import java.util.List;

import static android.content.IntentFilter.SYSTEM_HIGH_PRIORITY;


public class BatteryActivity extends BaseCenterActivity implements View.OnClickListener {

    public static final String TAG = "Battery_Tag";

    private static final long FRAME = 50;
    private static final long DURATION_SEEK_PROGRESS = 30 * FRAME;
    private static final long DURATION_RESULT_APPEAR = 10 * FRAME;
    private static final long DURATION_BATTERY_CONTENT_DISAPPEAR = 5 * FRAME;
    private static final long START_OFF_CLEANING_RING = FRAME;
    private static final long DURATION_CLEANING_BALL_APPEAR = 8 * FRAME;
    private static final long DURATION_CLEANING_RING_APPEAR = DURATION_CLEANING_BALL_APPEAR - START_OFF_CLEANING_RING;
    private static final long DURATION_CLEANING_PERCENT_APPEAR = 4 * FRAME;
    private static final long START_OFF_CLEANING_PERCENT_APPEAR = DURATION_CLEANING_BALL_APPEAR - DURATION_CLEANING_PERCENT_APPEAR;
    private static final long DURATION_CLEANING_APPEAR = 4 * FRAME;
    private static final long START_OFF_CLEANING = DURATION_CLEANING_BALL_APPEAR;
    private static final long START_OFF_STATUS_APPEAR = 30 * FRAME;

    private static final long DURATION_RING_DISAPPEAR = 6 * FRAME;
    private static final long DURATION_CLEANING_DISAPPEAR = 8 * FRAME;
    private static final long DURATION_CLEAN_RESULT_APPEAR = 8 * FRAME;
    private static final long DURATION_RIGHT_SYMBOL_APPEAR = DURATION_CLEAN_RESULT_APPEAR;
    private static final long START_OFF_RIGHT_SYMBOL_APPEAR = 2 * FRAME;
    private static final long START_OFF_CLEAN_RESULT_APPEAR = DURATION_CLEANING_DISAPPEAR;

    private static final long DURATION_CHARGING_SPEED_SCALE = 24 * FRAME;
    private static final long DURATION_CHARGING_TRICKLE_ROTATE = 14 * FRAME;
    private static final long DURATION_CHARGING_TRICKLE_ROTATE_SLEEP = 41 * FRAME;

    private static final float ROTATE_TO_DEGREES_CHARGING_TRICKLE = 360f;
    private static final float SCALE_MIN_CHARGING_SPEED = 0.7f;

    private static final float SCALE_START_CLEANING_RING = 0.259f;

    private static final long DURATION_CLEAN_ANIMATION = 4000;
    private static final long DURATION_CHARGING_TO_DISCHARGING_BG = 10 * FRAME;
    private static final long DELAY_REFRESH_ON_STOP = 10;

    private static final int APP_INFO_RETRY_DELAY_TIME = 300;
    private static final int BACK_FROZEN_TIME = 500;
    private static final int RETRY_APP_INFO_TIMES = (int) (DURATION_CLEAN_ANIMATION / APP_INFO_RETRY_DELAY_TIME);

    private static final int[] COLOR_BG_INIT_ARRAY = {
            0xFF4CAF50, 0xFF59A64D, 0xFF669E49, 0xFF729546, 0xFF7F8C43, 0xFF8C843F, 0xFF987B3C, 0xFFA57239,
            0xFFB26A35, 0xFFBF6132, 0xFFCB582F, 0xFFD8502B, 0xFFE54728};

    private static final int[] COLOR_BG_CLEANING_ARRAY = {0xFFE54728,
            0xFFE55228, 0xFFE55328, 0xFFE55428, 0xFFE55528, 0xFFE55628, 0xFFE55728, 0xFFE55828, 0xFFE55928, 0xFFE55928, 0xFFE55928,
            0xFFE55928, 0xFFE55928, 0xFFE55A28, 0xFFE55A28, 0xFFE55B28, 0xFFE55C28, 0xFFE55C28, 0xFFE55D28, 0xFFE55D28, 0xFFE55E28,
            0xFFE55E28, 0xFFE55F28, 0xFFE56028, 0xFFE56028, 0xFFE56128, 0xFFE56228, 0xFFE56228, 0xFFE56328, 0xFFE56328, 0xFFE56428,
            0xFFE56528, 0xFFE56528, 0xFFE56628, 0xFFE56628, 0xFFE56728, 0xFFE56728, 0xFFE56828, 0xFFE56928, 0xFFE56928, 0xFFE56A28,
            0xFFE56B28, 0xFFE56B28, 0xFFE26C29, 0xFFE06D29, 0xFFDD6E2A, 0xFFDC6F2A, 0xFFD9702B, 0xFFD7712C, 0xFFD4722C, 0xFFD3732D,
            0xFFD0742E, 0xFFCE752E, 0xFFCB762F, 0xFFCA772F, 0xFFC77830, 0xFFC57930, 0xFFC27A31, 0xFFC17B32, 0xFFBE7D32, 0xFFBC7D33,
            0xFFB97F33, 0xFFB87F34, 0xFFB58135, 0xFFB38135, 0xFFB08336, 0xFFAE8336, 0xFFAB8537, 0xFFAA8537, 0xFFA78738, 0xFFA58739,
            0xFFA28939, 0xFFA1893A, 0xFF9E8B3B, 0xFF9C8B3B, 0xFF998D3C, 0xFF988D3C, 0xFF968E3D, 0xFF938F3D, 0xFF92903E, 0xFF8F913F,
            0xFF8D923F, 0xFF8A9340, 0xFF899440, 0xFF869541, 0xFF849641, 0xFF819742, 0xFF809843, 0xFF7C9943, 0xFF7B9A44, 0xFF789B45,
            0xFF769C45, 0xFF739D46, 0xFF729E46, 0xFF6FA047, 0xFF6DA047, 0xFF6AA248, 0xFF69A248, 0xFF66A449, 0xFF64A44A, 0xFF61A64A};

    private static final int[] COLOR_BG_CLEANING_FINISHED_ARRAY = {
            0xFF5EA74B, 0xFF5DA84C, 0xFF5AA94C, 0xFF58AA4D, 0xFF57AA4D, 0xFF55AB4E, 0xFF54AC4E, 0xFF52AC4E, 0xFF51AD4F, 0xFF4FAE4F,
            0xFF4EAE50, 0xFF4CAF50};

    private static final int COLOR_BG_EXCELLENT = 0xFF4CAF50;
    private static final int COLOR_BG_CAREFUL = 0xFFE54728;

    private CustomRootView mCustomRootView;
    
    private LinearLayout mMenuLl;
    private AppCompatImageView mModeMenuIv;
    private AppCompatImageView mRankMenuIv;
    private LinearLayout mBatteryContentLl;
    private LinearLayout mBatteryChargingLl;
    private LinearLayout mStatusContentLl;
    private LinearLayout mCleaningPercentLl;
    private LinearLayout mCleanResultLl;
    private LinearLayout mExtendTimeLl;
    private LinearLayout mManualToolBarLl;
    private View mToolBarView;

    private RelativeLayout mCleaningContentRl;
    private RelativeLayout mPowerRemainRl;
    private RelativeLayout mScanIconAnimationRl;

    private TextView mRemainHourTv;
    private TextView mRemainMinuteTv;
    private TextView mCleaningPercentTv;
    private TextView mCleaningPercentSymbolTv;
    private TextView mCleaningTv;
    private TextView mExtendHourTv;
    private TextView mExtendMinuteTv;
    private TextView mExtendDescriptionLineOTv;
    private TextView mExtendDescriptionLineITv;
    private TextView mChargingContentTv;
    private TextView mPowerRemainingTv;
    private TextView mChargingSpeedTv;
    private TextView mChargingContinueTv;
    private TextView mChargingTrickleTv;
    private TextView mPowerRemainingPercentTv;
    private AppCompatTextView mStatusTitleTv;
    private AppCompatTextView mStatusContentTv;

    private AppCompatImageView mCleaningBallIv;
    private AppCompatImageView mCleaningRingIv;
    private AppCompatImageView mScanIconAnimationLightIv;
    private AppCompatImageView mScanRightSymbolIv;
    private AppCompatImageView mChargingSpeedIv;
    private AppCompatImageView mChargingContinueIv;
    private AppCompatImageView mChargingTrickleIv;
    private AppCompatImageView mStatusTitleIv;
    private AppCompatImageView mPowerRemainingPercentIv;
    private AppCompatImageView mMenuIv;
    private Button mOptimizeBtn;

    private View mChargingLeftLineV;
    private View mChargingRightLineV;

    private SeekCircleProgressBar mSeekProgressBar;
    private ScanIconAnimationView mScanIconAnimationView;

    private HeuristicAnimator mCleaningProgressAnimator;
    private ValueAnimator mRemainHourAnimator;
    private ValueAnimator mRemainMinuteAnimator;
    private ObjectAnimator mInitBgColorAnimator;
    private ObjectAnimator mCleanFinishedBgColorAnimator;
    private ObjectAnimator mChargingToDisChargingBgAnimator;
    private ObjectAnimator mExcellentCleanGreenToRedBgAnimator;
    private DynamicRotateAnimation mDynamicRotateAnimation;
    private Animatable mChargingContinueAnim;

    public static BatteryDataManager mBatteryDataManager;
    private Runnable mContinueAnimationRunnable;
    private HSChargingManager.IChargingListener mIChargingListener;

    private List<BatteryAppInfo> appInfoList;
    private Handler mHandler = new Handler();

    private int retryTimes = 0;
    private int mBatteryPercentNumber = DeviceManager.getInstance().getBatteryLevel();

    private boolean mIsStartInitAnimated;
    private boolean mIsCleaningToCharging;
    private boolean mIsCleanAnimating;
    private boolean mIsCleaned;
    private long mStartCleaningTime;
    private Runnable mResultPageRunnable;
    private Runnable mCleanProgressRunnable;

    public interface RefreshListener {
        void onResultViewFinished();
    }

    public enum ViewType {
        LOW_BATTERY(0),
        CAREFUL(1),
        EXCELLENT(2),
        CHARGING(3),
        CHARGING_COMPLETED(4);

        private int mValue;

        ViewType(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        public static ViewType valueOf(int value) {
            switch (value) {
                case 0:
                    return LOW_BATTERY;
                case 1:
                    return CAREFUL;
                case 2:
                    return EXCELLENT;
                case 3:
                    return CHARGING;
                case 4:
                    return CHARGING_COMPLETED;
            }
            return null;
        }
    }
    private ViewType mCurrentViewType = ViewType.CAREFUL;
    private ViewType mLastViewType = ViewType.CAREFUL;

    @Override
    public boolean isEnableNotificationActivityFinish() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery);
        BatteryUtils.setBatteryOpenFlag(true);
        BatteryUtils.resumeBatteryTime();
        mCurrentViewType = getInitViewType();
        mBatteryDataManager = new BatteryDataManager(this);
        initView();
        setListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mIsCleanAnimating) {
            stopAndUpdateView(DELAY_REFRESH_ON_STOP);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            stopAndUpdateView(0);
            boolean resumeManualToolBar = resumeManualToolBar();
            HSLog.d(TAG, "onOptionsItemSelected resumeManualToolBar = " + resumeManualToolBar);
            if (resumeManualToolBar) {
                return true;
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void stopAndUpdateView(long delayTime) {
        HSLog.d(TAG, "stopAndUpdateView delayTime = " + delayTime);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                stopCleaningAnimation();
                stopCleanFinishAnimation();
                stopBgColorAnimation();
                updateViewStatus(mCurrentViewType, mIsCleaned, false, false, false);
                mIsCleaned = false;
                resumeManualToolBar();
            }
        };
        if (0 == delayTime) {
            runnable.run();
        } else {
            // Add delay time for some devices visible in after activity
            mHandler.postDelayed(runnable, delayTime);
        }
    }

    private boolean resumeManualToolBar() {
        if (mToolBarView.getVisibility() == View.VISIBLE) {
            mToolBarView.setVisibility(View.GONE);
            mManualToolBarLl.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    private void initView() {
        mCustomRootView = ViewUtils.findViewById(this, R.id.view_container);
        mOptimizeBtn = ViewUtils.findViewById(this, R.id.optimize_btn);
        mMenuLl = ViewUtils.findViewById(this, R.id.menu_ll);
        mModeMenuIv = ViewUtils.findViewById(this, R.id.mode_menu_iv);
        mRankMenuIv = ViewUtils.findViewById(this, R.id.rank_menu_iv);
        mStatusContentLl = ViewUtils.findViewById(this, R.id.status_content_ll);
        mBatteryChargingLl = ViewUtils.findViewById(this, R.id.battery_charging_ll);
        mCleaningContentRl = ViewUtils.findViewById(this, R.id.cleaning_content_rl);
        mCleaningPercentLl = ViewUtils.findViewById(this, R.id.cleaning_percent_ll);
        mCleaningTv = ViewUtils.findViewById(this, R.id.cleaning_tv);
        mSeekProgressBar = ViewUtils.findViewById(this, R.id.seek_circle_progressbar);
        mPowerRemainingPercentTv = ViewUtils.findViewById(this, R.id.power_remaining_percent_tv);
        mPowerRemainingPercentIv = ViewUtils.findViewById(this, R.id.power_remaining_percent_iv);
        mRemainHourTv = ViewUtils.findViewById(this, R.id.remain_hour_time_tv);
        mRemainMinuteTv = ViewUtils.findViewById(this, R.id.remain_minute_time_tv);
        mBatteryContentLl = ViewUtils.findViewById(this, R.id.battery_content_ll);
        mCleaningBallIv = ViewUtils.findViewById(this, R.id.cleaning_ball_iv);
        mCleaningRingIv = ViewUtils.findViewById(this, R.id.cleaning_ring_iv);
        mCleaningPercentTv = ViewUtils.findViewById(this, R.id.cleaning_percent_tv);
        mCleaningPercentSymbolTv = ViewUtils.findViewById(this, R.id.cleaning_percent_symbol_tv);
        mScanIconAnimationView = ViewUtils.findViewById(this, R.id.app_icon_animation_v);
        mScanIconAnimationRl = ViewUtils.findViewById(this, R.id.app_icon_animation_rl);
        mScanIconAnimationLightIv = ViewUtils.findViewById(this, R.id.app_icon_light_iv);
        mScanRightSymbolIv = ViewUtils.findViewById(this, R.id.scan_right_symbol_iv);
        mCleanResultLl = ViewUtils.findViewById(this, R.id.clean_result_ll);
        mPowerRemainRl = ViewUtils.findViewById(this, R.id.power_remaining_rl);
        mExtendHourTv = ViewUtils.findViewById(this, R.id.save_hour_time_tv);
        mExtendMinuteTv = ViewUtils.findViewById(this, R.id.save_minute_time_tv);
        mExtendDescriptionLineOTv = ViewUtils.findViewById(this, R.id.clean_result_description_line_o_tv);
        mExtendDescriptionLineITv = ViewUtils.findViewById(this, R.id.clean_result_description_line_i_tv);
        mExtendTimeLl = ViewUtils.findViewById(this, R.id.save_time_ll);
        mStatusTitleTv = ViewUtils.findViewById(this, R.id.normal_status_title_tv);
        mStatusTitleIv = ViewUtils.findViewById(this, R.id.normal_status_title_iv);
        mStatusContentTv = ViewUtils.findViewById(this, R.id.normal_status_content_tv);
        mChargingContentTv = ViewUtils.findViewById(this, R.id.charging_content_tv);
        mChargingLeftLineV = ViewUtils.findViewById(this, R.id.charging_left_line_v);
        mChargingRightLineV = ViewUtils.findViewById(this, R.id.charging_right_line_v);
        mPowerRemainingTv = ViewUtils.findViewById(this, R.id.power_remaining_tv);
        mChargingSpeedTv = ViewUtils.findViewById(this, R.id.charging_speed_tv);
        mChargingContinueTv = ViewUtils.findViewById(this, R.id.charging_continue_tv);
        mChargingTrickleTv = ViewUtils.findViewById(this, R.id.charging_trickle_tv);
        mChargingSpeedIv = ViewUtils.findViewById(this, R.id.charging_speed_iv);
        mChargingContinueIv = ViewUtils.findViewById(this, R.id.charging_continue_iv);
        mChargingTrickleIv = ViewUtils.findViewById(this, R.id.charging_trickle_iv);
        mMenuIv = ViewUtils.findViewById(this, R.id.battery_menu);
        mManualToolBarLl = ViewUtils.findViewById(this, R.id.manual_action_bar);
        mToolBarView = ViewUtils.findViewById(this, R.id.action_bar);

        ActivityUtils.configSimpleAppBar(this, getString(R.string.battery_title), Color.TRANSPARENT);

        // Title text bold
        TextView titleTv = ViewUtils.findViewById(this, R.id.battery_title_tv);
        String contentText = getString(R.string.battery_air_title);
        SpannableString contentSpannableString = new SpannableString(contentText);
        contentSpannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, 3, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        titleTv.setText(contentSpannableString);

        if (!ChargingPrefsUtil.getInstance().isChargingEnabledBefore()
                && !ChargingPrefsUtil.isBatteryTipShown()) {
            mMenuIv.setImageResource(R.drawable.ic_battery_menu_tip);
        }

        Drawable drawable = mChargingContinueIv.getDrawable();
        if (drawable instanceof Animatable) {
            mChargingContinueAnim = (Animatable) drawable;
        }

        adapterContentView();
    }

    private void adapterContentView() {
        int phoneWidth = CommonUtils.getPhoneWidth(this);
        if (phoneWidth <= 480) {
            // this weight 3.7 must be same with activity_battery.xml battery_charging_ll
            // and power_remaining_rl, 1.2f + 0.8f = 2.0f, 2.0f is bind in xml (1.0f + 1.0f).
            LinearLayout.LayoutParams powerRemainLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 2.6f);
            mPowerRemainRl.setLayoutParams(powerRemainLp);

            LinearLayout.LayoutParams statusContentLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
            mStatusContentLl.setLayoutParams(statusContentLp);
        }
    }

    private void setListeners() {
        mOptimizeBtn.setOnClickListener(this);
        mModeMenuIv.setOnClickListener(this);
        mRankMenuIv.setOnClickListener(this);
        mMenuIv.setOnClickListener(this);

        mIChargingListener = new HSChargingManager.IChargingListener() {
            @Override
            public void onBatteryLevelChanged(int preBatteryLevel, int curBatteryLevel) {
                mBatteryPercentNumber = curBatteryLevel;
                setBatteryLevelStatus(mBatteryPercentNumber);
                setBatteryRemainingTime(mBatteryPercentNumber, false);
            }

            @Override
            public void onChargingStateChanged(HSChargingManager.HSChargingState hsChargingStatePre, HSChargingManager.HSChargingState hsChargingState) {
                if (isCharging(hsChargingState)) {
                    stopStatusAppearAnimation();
                    if (mCurrentViewType != ViewType.CHARGING && mCurrentViewType != ViewType.CHARGING_COMPLETED) {
                        mLastViewType = mCurrentViewType;
                    }

                    if (hsChargingState == HSChargingManager.HSChargingState.STATE_CHARGING_FULL) {
                        mCurrentViewType = ViewType.CHARGING_COMPLETED;
                        mChargingContentTv.setText(getString(R.string.battery_charging_completed_content));
                    } else {
                        mCurrentViewType = ViewType.CHARGING;
                        mChargingContentTv.setText(getString(R.string.battery_charging_content));
                    }
                    updateViewStatus(mCurrentViewType, false, false, false, false);
                } else {
                    if ((hsChargingStatePre == HSChargingManager.HSChargingState.STATE_DISCHARGING && hsChargingState == HSChargingManager.HSChargingState.STATE_DISCHARGING)
                            || (hsChargingStatePre == HSChargingManager.HSChargingState.STATE_UNKNOWN && hsChargingState == HSChargingManager.HSChargingState.STATE_DISCHARGING)) {
                    } else {
                        updateViewStatus(mLastViewType, false, false, false, true);
                    }
                }
            }

            @Override
            public void onChargingRemainingTimeChanged(int minuteTime) {
                setBatteryChargingTime(minuteTime, !mIsStartInitAnimated && !BatteryUtils.isInitAnimationFrozen());
            }

            @Override
            public void onBatteryTemperatureChanged(float v, float v1) {
            }
        };

        HSChargingManager.getInstance().addChargingListener(mIChargingListener);
    }

    private ViewType getInitViewType() {
        ViewType viewType;
        if (BatteryUtils.isInitAnimationFrozen()) {
            viewType = ViewType.valueOf(BatteryUtils.getBatteryLastViewType());
        } else {
            if (mBatteryPercentNumber == 0) {
                mBatteryPercentNumber = DeviceManager.getInstance().getBatteryLevel();
            }

            if (mBatteryPercentNumber < BatteryUtils.BATTERY_LOW_LIMIT) {
                viewType = ViewType.LOW_BATTERY;
            } else {
                int ramUsage = DeviceManager.getInstance().getRamUsage();
                if (ramUsage > BatteryUtils.BATTERY_RAM_USAGE_LIMIT) {
                    viewType = ViewType.CAREFUL;
                } else {
                    viewType = ViewType.EXCELLENT;
                }
            }
        }
        return viewType;
    }

    private void updateViewStatus(ViewType viewType, boolean isCleaned, boolean isBgGradientAnim, boolean isContentAppearAnim, boolean isChargingToDischarging) {
        boolean isStartGreenToRedAnimation = isChargingToDischarging && (viewType == ViewType.CAREFUL || viewType == ViewType.LOW_BATTERY)
                && (mCurrentViewType == ViewType.EXCELLENT || mCurrentViewType == ViewType.CHARGING || mCurrentViewType == ViewType.CHARGING_COMPLETED);
        boolean isCharging = isCharging(null);
        if (isCharging) {
            mCurrentViewType = ViewType.CHARGING;
            viewType = mCurrentViewType;
        } else if (isCleaned) {
            mCurrentViewType = ViewType.EXCELLENT;
            viewType = mCurrentViewType;
        } else {
            mCurrentViewType = viewType;
        }
        boolean isChargingCompleted = false;

        HSLog.d(TAG, "updateViewStatus mCurrentViewType = " + mCurrentViewType + " mIsCleanAnimating = " + mIsCleanAnimating + " isBgGradientAnim = " + isBgGradientAnim + " isCharging = " + isCharging);

        switch (viewType) {
            case LOW_BATTERY:
                mStatusTitleTv.setText(getString(R.string.battery_low));
                mStatusContentTv.setText(getString(R.string.battery_low_content));
                mBatteryChargingLl.setVisibility(View.GONE);
                mPowerRemainingTv.setText(getString(R.string.battery_power_remaining));
                VectorCompat.setImageViewVectorResource(this, mStatusTitleIv, R.drawable.battery_careful_svg);
                VectorCompat.setImageViewVectorResource(this, mPowerRemainingPercentIv, R.drawable.battery_light_svg);
                stopChargingAnimation();
                break;
            case CAREFUL:
                mStatusTitleTv.setText(getString(R.string.battery_careful));
                mStatusContentTv.setText(getString(R.string.battery_careful_content));
                mBatteryChargingLl.setVisibility(View.GONE);
                mPowerRemainingTv.setText(getString(R.string.battery_power_remaining));
                VectorCompat.setImageViewVectorResource(this, mStatusTitleIv, R.drawable.battery_careful_svg);
                VectorCompat.setImageViewVectorResource(this, mPowerRemainingPercentIv, R.drawable.battery_light_svg);
                stopChargingAnimation();
                break;
            case EXCELLENT:
                mStatusTitleTv.setText(getString(R.string.battery_excellent));
                mStatusContentTv.setText(getString(R.string.battery_excellent_content));
                mBatteryChargingLl.setVisibility(View.GONE);
                mPowerRemainingTv.setText(getString(R.string.battery_power_remaining));
                VectorCompat.setImageViewVectorResource(this, mStatusTitleIv, R.drawable.battery_excellent_svg);
                VectorCompat.setImageViewVectorResource(this, mPowerRemainingPercentIv, R.drawable.battery_light_svg);
                stopChargingAnimation();
                break;
            case CHARGING:
                isChargingCompleted = false;
                break;
            case CHARGING_COMPLETED:
                isChargingCompleted = true;
                break;
            default:
                stopChargingAnimation();
                break;
        }

        if (viewType == ViewType.CHARGING || viewType == ViewType.CHARGING_COMPLETED) {
            if (mIsCleaningToCharging || mIsCleanAnimating) {
                stopCleaningAnimation();
            }
            stopBgColorAnimation();

            stopCleanFinishAnimation();
            mBatteryChargingLl.setVisibility(View.VISIBLE);
            startChargingAnimation();
            mPowerRemainingTv.setText(isChargingCompleted ? getString(R.string.battery_charging_completed) : getString(R.string.battery_charging_left));
            VectorCompat.setImageViewVectorResource(this, mPowerRemainingPercentIv, R.drawable.battery_charging_plug_svg);
        }

        if (isContentAppearAnim) {
            startStatusAppearAnimation(START_OFF_STATUS_APPEAR);
        } else {
            mStatusContentLl.setVisibility((viewType == ViewType.CHARGING || viewType == ViewType.CHARGING_COMPLETED) ? View.INVISIBLE : View.VISIBLE);
        }

        if (!isBgGradientAnim) {
            stopExcellentGreenToRedBgAnimation();
            int bgColor = getCurrentBgColor(viewType);
            setBgColor(bgColor);
            mCleaningPercentSymbolTv.setTextColor(bgColor);
            mCleaningPercentTv.setTextColor(bgColor);
            mCleaningPercentSymbolTv.setText("");
            mCleaningPercentTv.setText("");
        }

        if (isStartGreenToRedAnimation) {
            // Charging to DisCharging bg color
            mChargingToDisChargingBgAnimator = ObjectAnimator.ofInt(this, "bgColor", COLOR_BG_INIT_ARRAY);
            mChargingToDisChargingBgAnimator.setDuration(DURATION_CHARGING_TO_DISCHARGING_BG);
            mChargingToDisChargingBgAnimator.setEvaluator(new ArgbEvaluator());
            mChargingToDisChargingBgAnimator.start();
        }
    }

    private void stopBgColorAnimation() {
        if (null != mInitBgColorAnimator) {
            mInitBgColorAnimator.cancel();
        }
        if (null != mCleanFinishedBgColorAnimator) {
            mCleanFinishedBgColorAnimator.cancel();
        }
        if (null != mChargingToDisChargingBgAnimator) {
            mChargingToDisChargingBgAnimator.cancel();
        }
        stopExcellentGreenToRedBgAnimation();
    }

    private void stopExcellentGreenToRedBgAnimation() {
        if (null != mExcellentCleanGreenToRedBgAnimator) {
            mExcellentCleanGreenToRedBgAnimator.cancel();
            mExcellentCleanGreenToRedBgAnimator = null;
        }
    }

    private boolean isCharging(HSChargingManager.HSChargingState hsChargingState) {
        if(null == hsChargingState) {
            hsChargingState = HSChargingManager.getInstance().getChargingState();
        }
        HSLog.d(TAG, "isCharging ***** hsChargingState = " + hsChargingState);
        return (hsChargingState == HSChargingManager.HSChargingState.STATE_CHARGING_SPEED || hsChargingState == HSChargingManager.HSChargingState.STATE_CHARGING_CONTINUOUS
                || hsChargingState == HSChargingManager.HSChargingState.STATE_CHARGING_TRICKLE || hsChargingState == HSChargingManager.HSChargingState.STATE_CHARGING_FULL);
    }

    private boolean isCharging() {
        return (mCurrentViewType == ViewType.CHARGING || mCurrentViewType == ViewType.CHARGING_COMPLETED);
    }

    private void startChargingAnimation() {
        HSChargingManager.HSChargingState hSChargingState = HSChargingManager.getInstance().getChargingState();
        switch (hSChargingState) {
            case STATE_CHARGING_SPEED:
                mChargingLeftLineV.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
                mChargingRightLineV.setBackgroundColor(ContextCompat.getColor(this, R.color.battery_charging_disable));
                mChargingSpeedTv.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                mChargingContinueTv.setTextColor(ContextCompat.getColor(this, R.color.battery_charging_disable));
                mChargingTrickleTv.setTextColor(ContextCompat.getColor(this, R.color.battery_charging_disable));
                mChargingSpeedIv.setImageDrawable(VectorCompat.createVectorDrawable(this, R.drawable.battery_speed_svg));
                mChargingContinueIv.setImageDrawable(VectorCompat.createVectorDrawable(this, R.drawable.battery_continue_disable_svg));
                mChargingTrickleIv.setImageDrawable(VectorCompat.createVectorDrawable(this, R.drawable.battery_trickle_disable_svg));
                startChargingSpeedAnimation();
                break;
            case STATE_CHARGING_CONTINUOUS:
                mChargingLeftLineV.setBackgroundColor(Color.WHITE);
                mChargingRightLineV.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
                mChargingSpeedTv.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                mChargingContinueTv.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                mChargingTrickleTv.setTextColor(ContextCompat.getColor(this, R.color.battery_charging_disable));
                mChargingSpeedIv.setImageDrawable(VectorCompat.createVectorDrawable(this, R.drawable.battery_speed_svg));
                mChargingTrickleIv.setImageDrawable(VectorCompat.createVectorDrawable(this, R.drawable.battery_trickle_disable_svg));
                startChargingContinueAnimation();
                break;
            case STATE_CHARGING_TRICKLE:
                mChargingLeftLineV.setBackgroundColor(Color.WHITE);
                mChargingRightLineV.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
                mChargingSpeedTv.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                mChargingContinueTv.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                mChargingTrickleTv.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                mChargingSpeedIv.setImageDrawable(VectorCompat.createVectorDrawable(this, R.drawable.battery_speed_svg));
                mChargingContinueIv.setImageDrawable(VectorCompat.createVectorDrawable(this, R.drawable.battery_continue_svg));
                mChargingTrickleIv.setImageDrawable(VectorCompat.createVectorDrawable(this, R.drawable.battery_trickle_normal_svg));
                startChargingTrickleAnimation();
                break;
            case STATE_CHARGING_FULL:
                mChargingLeftLineV.setBackgroundColor(Color.WHITE);
                mChargingRightLineV.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
                mChargingSpeedTv.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                mChargingContinueTv.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                mChargingTrickleTv.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                mChargingSpeedIv.setImageDrawable(VectorCompat.createVectorDrawable(this, R.drawable.battery_speed_svg));
                mChargingContinueIv.setImageDrawable(VectorCompat.createVectorDrawable(this, R.drawable.battery_continue_svg));
                mChargingTrickleIv.setImageDrawable(VectorCompat.createVectorDrawable(this, R.drawable.battery_trickle_normal_svg));
                stopChargingAnimation();
                break;
            default:
                break;
        }
    }

    private void startChargingSpeedAnimation() {
        mChargingSpeedIv.setTag(false);
        LauncherAnimationUtils.startLoopScaleAnimation(mChargingSpeedIv, DURATION_CHARGING_SPEED_SCALE, SCALE_MIN_CHARGING_SPEED, true);
    }

    private void startChargingContinueAnimation() {
        stopChargingSpeedAnimation();
        mChargingContinueIv.setTag(false);

        if (null != mChargingContinueAnim) {
            if (!mChargingContinueAnim.isRunning()) {
                mChargingContinueAnim.start();
            }
            mContinueAnimationRunnable = new Runnable() {
                @Override
                public void run() {
                    startChargingContinueAnimation();
                }
            };
            mHandler.postDelayed(mContinueAnimationRunnable, 2000);
        }
    }

    private void startChargingTrickleAnimation() {
        if (null != mContinueAnimationRunnable) {
            mHandler.removeCallbacks(mContinueAnimationRunnable);
        }
        stopChargingSpeedAnimation();
        stopChargingContinueAnimation();
        mChargingTrickleIv.setTag(false);
        LauncherAnimationUtils.startLoopRotateAnimation(mChargingTrickleIv, DURATION_CHARGING_TRICKLE_ROTATE, DURATION_CHARGING_TRICKLE_ROTATE_SLEEP, ROTATE_TO_DEGREES_CHARGING_TRICKLE, true);
    }

    private void stopChargingSpeedAnimation() {
        if (null != mChargingSpeedIv) {
            mChargingSpeedIv.setTag(true);
            mChargingSpeedIv.clearAnimation();
        }
    }

    private void stopChargingContinueAnimation() {
        if (null != mChargingContinueIv) {
            mChargingContinueIv.setTag(true);
            mChargingContinueIv.clearAnimation();
        }
        if (null != mContinueAnimationRunnable) {
            mHandler.removeCallbacks(mContinueAnimationRunnable);
        }
    }

    private void stopChargingTrickleAnimation() {
        if (null != mChargingTrickleIv) {
            mChargingTrickleIv.setTag(true);
            mChargingTrickleIv.clearAnimation();
        }
    }

    private void stopChargingAnimation() {
        stopChargingSpeedAnimation();
        stopChargingContinueAnimation();
        stopChargingTrickleAnimation();
    }

    @SuppressLint("NewApi")
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        CommonUtils.setupTransparentSystemBarsForLmp(this);
        View viewContainer = ViewUtils.findViewById(this, R.id.view_container);
        viewContainer.setPadding(0, CommonUtils.getStatusBarHeight(this), 0, 0);
        ActivityUtils.setNavigationBarColor(this, Color.BLACK);
        startInitAnimation();
    }

    private void startInitAnimation() {
        // SeekProgressBar
        startSeekProgressBarAnimation();

        // Background
        boolean isCharging = isCharging(null);
        int chargingLeftMinute = HSChargingManager.getInstance().getChargingLeftMinutes();
        if (BatteryUtils.isInitAnimationFrozen()) {
            updateViewStatus(mCurrentViewType, false, false, false, false);
            if (isCharging) {
                setBatteryChargingTime(chargingLeftMinute, false);
            } else {
                setBatteryRemainingTime(mBatteryPercentNumber, false);
            }
        } else {
            if ((mCurrentViewType == ViewType.CAREFUL || mCurrentViewType == ViewType.LOW_BATTERY) && !isCharging) {
                mInitBgColorAnimator = ObjectAnimator.ofInt(this, "bgColor", COLOR_BG_INIT_ARRAY);
                mInitBgColorAnimator.setDuration(DURATION_SEEK_PROGRESS);
                mInitBgColorAnimator.setEvaluator(new ArgbEvaluator());
                mInitBgColorAnimator.start();

                // Time progress animation
                int[] remainingTimes = BatteryUtils.calculateBatteryRemainingTime(mBatteryPercentNumber);
                int hour = remainingTimes[0];
                int minute = remainingTimes[1];
                startTimeProgressInitAnimation(hour, minute);

                updateViewStatus(mCurrentViewType, false, true, true, false);
            } else {
                updateViewStatus(mCurrentViewType, false, false, false, false);
                if (isCharging) {
                    setBatteryChargingTime(chargingLeftMinute, false);
                } else {
                    setBatteryRemainingTime(mBatteryPercentNumber, false);
                }
            }
        }
    }

    private void startTimeProgressInitAnimation(int hour, int minute) {
        mRemainHourAnimator = LauncherAnimUtils.ofInt(0, hour);
        mRemainHourAnimator.setDuration(DURATION_SEEK_PROGRESS);
        mRemainHourAnimator.setInterpolator(LauncherAnimUtils.ACCELERATE_DECELERATE);
        mRemainHourAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int number = (int) animation.getAnimatedValue();
                mRemainHourTv.setText((number < 10) ? ("0" + number) : String.valueOf(number));
            }
        });
        mRemainHourAnimator.start();

        mRemainMinuteAnimator = LauncherAnimUtils.ofInt(0, minute);
        mRemainMinuteAnimator.setDuration(DURATION_SEEK_PROGRESS);
        mRemainMinuteAnimator.setInterpolator(LauncherAnimUtils.ACCELERATE_DECELERATE);
        mRemainMinuteAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int number = (int) animation.getAnimatedValue();
                mRemainMinuteTv.setText((number < 10) ? ("0" + number) : String.valueOf(number));
            }
        });
        mRemainMinuteAnimator.start();
        mIsStartInitAnimated = true;
    }

    private void stopTimeProgressAnimation() {
        if (null != mRemainHourAnimator) {
            mRemainHourAnimator.cancel();
        }

        if (null != mRemainMinuteAnimator) {
            mRemainMinuteAnimator.cancel();
        }
    }

    private boolean isTimeProgressAnimating() {
        boolean isRunning = false;
        if (null != mRemainHourAnimator) {
            isRunning = mRemainHourAnimator.isRunning();
        } else if (null != mRemainMinuteAnimator) {
            isRunning = mRemainMinuteAnimator.isRunning();
        }
        return isRunning;
    }

    private void startSeekProgressBarAnimation() {
        ValueAnimator mSeekProgressBarAnimator = ValueAnimator.ofInt(0, mBatteryPercentNumber);
        mSeekProgressBarAnimator.setDuration(DURATION_SEEK_PROGRESS);
        mSeekProgressBarAnimator.setInterpolator(LauncherAnimUtils.ACCELERATE_DECELERATE);
        mSeekProgressBarAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int progress = (int) animation.getAnimatedValue();
                setBatteryLevelStatus(progress);
            }
        });
        mSeekProgressBarAnimator.start();
    }

    private void startStatusAppearAnimation(long startOff) {
        if (mCurrentViewType != ViewType.CHARGING && mCurrentViewType != ViewType.CHARGING_COMPLETED && !BatteryUtils.isInitAnimationFrozen()) {
            Animation alphaAppearAnimation = LauncherAnimationUtils.getAlphaAppearAnimation(DURATION_RESULT_APPEAR, startOff, LauncherAnimationUtils.accelerateDecelerateInterpolator);
            LauncherAnimationUtils.startAnimation(mStatusContentLl, false, alphaAppearAnimation);
        }
    }

    private void stopStatusAppearAnimation() {
        if (null != mStatusContentLl) {
            mStatusContentLl.clearAnimation();
        }
    }

    private void setBatteryLevelStatus(int level) {
        mSeekProgressBar.setProgress(level);
        mPowerRemainingPercentTv.setText(String.valueOf(level + "%"));
    }

    private void setBatteryRemainingTime(int level, boolean isStartAnimation) {
        if (isTimeProgressAnimating()) {
            return;
        }
        int[] remainingTimes = BatteryUtils.calculateBatteryRemainingTime(level);
        int hour = remainingTimes[0];
        int minute = remainingTimes[1];

        stopTimeProgressAnimation();
        if (!isStartAnimation || (hour == 0 && minute == 0)) {
            mRemainHourTv.setText((hour < 10) ? ("0" + hour) : String.valueOf(hour));
            mRemainMinuteTv.setText((minute < 10) ? ("0" + minute) : String.valueOf(minute));
        } else {
            startTimeProgressInitAnimation(hour, minute);
        }
    }

    private void setBatteryChargingTime(int minuteTime, boolean isAnimation) {
        int hour = minuteTime / 60;
        int minute = minuteTime < 60 ? minuteTime : minuteTime % 60;

        stopTimeProgressAnimation();
        if (!isAnimation || (hour == 0 && minute == 0)) {
            mRemainHourTv.setText((hour < 10) ? ("0" + hour) : String.valueOf(hour));
            mRemainMinuteTv.setText((minute < 10) ? ("0" + minute) : String.valueOf(minute));
        } else {
            startTimeProgressInitAnimation(hour, minute);
        }
    }

    private void startCleaningAnimation() {
        HSLog.d(TAG, "startCleaningAnimation ***");
        if (null != mManualToolBarLl) {
            mManualToolBarLl.setVisibility(View.GONE);
        }

        if (null != mToolBarView) {
            mToolBarView.setVisibility(View.VISIBLE);
        }

        if (null != mMenuLl) {
            mMenuLl.setVisibility(View.GONE);
        }
        mOptimizeBtn.setVisibility(View.GONE);

        mIsCleanAnimating = true;
        mIsCleaningToCharging = true;
        mStartCleaningTime = System.currentTimeMillis();

        mCleanProgressRunnable = () -> {
            if (null != mCleaningProgressAnimator) {
                mCleaningProgressAnimator.postValue(100);
            }
        };
        mHandler.postDelayed(mCleanProgressRunnable, DURATION_CLEAN_ANIMATION);

        mCleaningContentRl.setVisibility(View.VISIBLE);
        // Battery content alpha disAppear
        Animation alphaDisAppearAnimation = LauncherAnimationUtils.getAlphaDisAppearAnimation(DURATION_BATTERY_CONTENT_DISAPPEAR, 0);
        LauncherAnimationUtils.startAnimation(mBatteryContentLl, true, alphaDisAppearAnimation);

        // Green to Red animation
        if (mCurrentViewType == ViewType.EXCELLENT || mCurrentViewType == ViewType.CHARGING || mCurrentViewType == ViewType.CHARGING_COMPLETED) {
            stopExcellentGreenToRedBgAnimation();
            mExcellentCleanGreenToRedBgAnimator = ObjectAnimator.ofInt(this, "bgColor", COLOR_BG_INIT_ARRAY);
            mExcellentCleanGreenToRedBgAnimator.setDuration(DURATION_CLEANING_BALL_APPEAR);
            mExcellentCleanGreenToRedBgAnimator.setEvaluator(new ArgbEvaluator());
            mExcellentCleanGreenToRedBgAnimator.start();
        }

        // Cleaning ball
        AlphaAnimation ballAppearAnimation = new AlphaAnimation(0f, 1.0f);
        ScaleAnimation ballScaleAnimation = new ScaleAnimation(0f, 1.0f, 0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(ballAppearAnimation);
        animationSet.addAnimation(ballScaleAnimation);
        animationSet.setDuration(DURATION_CLEANING_BALL_APPEAR);
        mCleaningBallIv.startAnimation(animationSet);
        animationSet.setAnimationListener(new LauncherAnimationUtils.AnimationListenerAdapter(){
            @Override
            public void onAnimationStart(Animation animation) {
                super.onAnimationStart(animation);
                mCleaningBallIv.setVisibility(View.VISIBLE);
            }
        });

        // Cleaning ring
        AnimatorSet ringAppearAnimSet = new AnimatorSet();
        mCleaningRingIv.setAlpha(0f);
        mCleaningRingIv.setScaleX(SCALE_START_CLEANING_RING);
        mCleaningRingIv.setScaleY(SCALE_START_CLEANING_RING);
        Animator ringScaleXAppearAnimation = ObjectAnimator.ofFloat(mCleaningRingIv, "scaleX", SCALE_START_CLEANING_RING, 1.0f);
        Animator ringScaleYAppearAnimation = ObjectAnimator.ofFloat(mCleaningRingIv, "scaleY", SCALE_START_CLEANING_RING, 1.0f);
        Animator ringAlphaAppearAnimation = ObjectAnimator.ofFloat(mCleaningRingIv, "alpha", 0f, 1.0f);
        ringAppearAnimSet.playTogether(ringScaleXAppearAnimation, ringScaleYAppearAnimation, ringAlphaAppearAnimation);
        ringAppearAnimSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mCleaningRingIv.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // update Toolbar status

                // Ring rotate
                mDynamicRotateAnimation = new DynamicRotateAnimation(0.6f);
                mDynamicRotateAnimation.setAnimationListener(new LauncherAnimationUtils.AnimationListenerAdapter(){
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        super.onAnimationEnd(animation);
                        mCleaningRingIv.clearAnimation();
                        mCleaningRingIv.setAlpha(1.0f);
                        mCleaningRingIv.setScaleX(1.0f);
                        mCleaningRingIv.setScaleY(1.0f);
                        mCleaningRingIv.setRotation(0);
                        mCleaningRingIv.setVisibility(View.INVISIBLE);
                        if (null != mResultPageRunnable) {
                            mResultPageRunnable.run();
                            mResultPageRunnable = null;
                        }
                    }
                });
                mCleaningRingIv.clearAnimation();
                mCleaningRingIv.startAnimation(mDynamicRotateAnimation);
                // Process
                if (null != mCleaningProgressAnimator) {
                    mCleaningProgressAnimator.cancel();
                    mCleaningProgressAnimator = null;
                }
                mCleaningProgressAnimator = new HeuristicAnimator(0, 100, 5000);
                mCleaningProgressAnimator.setUpdateListener(new HeuristicAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(HeuristicAnimator animation) {
                        if (null == mCleaningProgressAnimator) {
                            return;
                        }
                        int progress = (int) animation.getAnimatedValue();
                        int bgColor;
                        if (progress < COLOR_BG_CLEANING_ARRAY.length && progress >= 0) {
                            bgColor = COLOR_BG_CLEANING_ARRAY[progress];
                        } else {
                            bgColor = COLOR_BG_CLEANING_ARRAY[COLOR_BG_CLEANING_ARRAY.length - 1];
                        }
                        stopExcellentGreenToRedBgAnimation();
                        setBgColor(bgColor);
                        mCleaningPercentSymbolTv.setTextColor(bgColor);
                        mCleaningPercentTv.setTextColor(bgColor);
                        mCleaningPercentSymbolTv.setText(getString(R.string.battery_progress_sign));
                        mCleaningPercentTv.setText(progress < 10 ? "0" + progress : String.valueOf(progress));
                        if (progress >= 100) {
                            startCleanFinishAnimation();
                        } else if (progress >= 80) {
                            float input = 0.05f * (100 - progress);
                            float alpha = (float)(1.0f - Math.pow((1.0f - input), 2 * 4));
                            mCleaningRingIv.setAlpha(alpha);
                        }
                    }
                });
                mCleaningProgressAnimator.start();
            }
        });
        ringAppearAnimSet.setDuration(DURATION_CLEANING_RING_APPEAR);
        ringAppearAnimSet.setStartDelay(START_OFF_CLEANING_RING);
        ringAppearAnimSet.setInterpolator(LauncherAnimUtils.ACCELERATE_DECELERATE);
        ringAppearAnimSet.start();

        // Cleaning number percent
        Animation cleaningPercentAppearAnimation = LauncherAnimationUtils.getAlphaAppearAnimation(DURATION_CLEANING_PERCENT_APPEAR, START_OFF_CLEANING_PERCENT_APPEAR);
        LauncherAnimationUtils.startAnimation(mCleaningPercentLl, false, cleaningPercentAppearAnimation);

        // Cleaning
        Animation cleaningAppearAnimation = LauncherAnimationUtils.getAlphaAppearAnimation(DURATION_CLEANING_APPEAR, START_OFF_CLEANING);
        LauncherAnimationUtils.startAnimation(mCleaningTv, false, cleaningAppearAnimation);

        // App icon loading
        startAppIconLoading(START_OFF_CLEANING);
    }

    private void stopCleaningAnimation() {
        HSLog.d(TAG, "stopCleaningAnimation ***");
        resumeManualToolBar();
        if (null != mCleanProgressRunnable) {
            mHandler.removeCallbacks(mCleanProgressRunnable);
        }
        mCleaningContentRl.setVisibility(View.GONE);
        mBatteryContentLl.setVisibility(View.VISIBLE);
        mBatteryContentLl.clearAnimation();
        mCleaningBallIv.clearAnimation();
        mCleaningRingIv.clearAnimation();
        mCleaningRingIv.setScaleX(1.0f);
        mCleaningRingIv.setScaleY(1.0f);
        mCleaningRingIv.setRotation(0);
        mCleaningPercentLl.clearAnimation();
        mCleaningTv.clearAnimation();
        if (null != mCleaningProgressAnimator) {
            mCleaningProgressAnimator.cancel();
            mCleaningProgressAnimator = null;
        }
        mHandler.removeCallbacksAndMessages(null);
        stopAppIconLoading();
        mIsCleaningToCharging = false;
        mIsCleanAnimating = false;
        mOptimizeBtn.setVisibility(View.VISIBLE);
        if (null != mMenuLl) {
            mMenuLl.setVisibility(View.VISIBLE);
        }
    }

    private void stopCleanFinishAnimation() {
        HSLog.d(TAG, "stopCleanFinishAnimation ***");
        mScanRightSymbolIv.setScaleX(1.0f);
        mScanRightSymbolIv.setScaleY(1.0f);
        mScanRightSymbolIv.clearAnimation();
        mScanRightSymbolIv.setVisibility(View.GONE);
        mCleanResultLl.clearAnimation();
        mCleanResultLl.setVisibility(View.GONE);
        mCleaningBallIv.clearAnimation();
        mCleaningBallIv.setVisibility(View.INVISIBLE);
        mHandler.removeCallbacksAndMessages(null);
        mResultPageRunnable = null;
    }

    private void startCleanFinishAnimation() {
        HSLog.d(TAG, "startCleanFinishAnimation mIsCleanAnimating = " + mIsCleanAnimating);
        if (null != mCleaningProgressAnimator) {
            mCleaningProgressAnimator.cancel();
            mCleaningProgressAnimator = null;
        }

        if (!mIsCleanAnimating) {
            return;
        }
        mDynamicRotateAnimation.startDecelerateMode();
        if (null != mScanIconAnimationView) {
            mScanIconAnimationView.setEnd(true);
            mScanIconAnimationView.setVisibility(View.INVISIBLE);
        }

        // Percent disappear
        Animation cleaningPercentDisAppearAnimation = LauncherAnimationUtils.getAlphaDisAppearAnimation(DURATION_RING_DISAPPEAR, 0);
        LauncherAnimationUtils.startAnimation(mCleaningPercentLl, cleaningPercentDisAppearAnimation, new LauncherAnimationUtils.AnimationListenerAdapter() {
            @Override
            public void onAnimationStart(Animation animation) {
                mCleaningPercentLl.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mCleaningPercentLl.setVisibility(View.INVISIBLE);
                mCleaningPercentTv.setText("");
                mCleaningPercentTv.setTextColor(ContextCompat.getColor(BatteryActivity.this, android.R.color.transparent));
                mCleaningPercentSymbolTv.setText("");
                mCleaningPercentSymbolTv.setTextColor(ContextCompat.getColor(BatteryActivity.this, android.R.color.transparent));
            }
        });

        // Cleaning disappear
        Animation cleaningDisAppearAnimation = LauncherAnimationUtils.getAlphaDisAppearAnimation(DURATION_CLEANING_DISAPPEAR, 0);
        LauncherAnimationUtils.startAnimation(mCleaningTv, true, cleaningDisAppearAnimation);

        // Right symbol appear
        mScanRightSymbolIv.setScaleX(0);
        mScanRightSymbolIv.setScaleY(0);
        mScanRightSymbolIv.setVisibility(View.VISIBLE);
        ViewPropertyAnimator rightSymbolPropertyAnimator = mScanRightSymbolIv.animate();
        rightSymbolPropertyAnimator.setDuration(DURATION_RIGHT_SYMBOL_APPEAR).setStartDelay(START_OFF_RIGHT_SYMBOL_APPEAR).scaleX(1.0f).scaleY(1.0f).setInterpolator(new OvershootInterpolator()).start();

        // Clean result appear
        Animation cleanResultAppearAnimation = LauncherAnimationUtils.getAlphaAppearAnimation(DURATION_CLEAN_RESULT_APPEAR, START_OFF_CLEAN_RESULT_APPEAR);
        LauncherAnimationUtils.startAnimation(mCleanResultLl, cleanResultAppearAnimation, new LauncherAnimationUtils.AnimationListenerAdapter() {
            @Override
            public void onAnimationStart(Animation animation) {
                super.onAnimationStart(animation);
                mCleanResultLl.setVisibility(View.VISIBLE);
            }
        });

        boolean isCleanTimeFrozen = BatteryUtils.isCleanTimeFrozen();
        boolean isBatteryOptimal = true;
        String extendHour = "";
        String extendMinute = "";
        if (isCleanTimeFrozen) {
            mExtendDescriptionLineOTv.setText(getString(R.string.battery_already_optimal));
            mExtendDescriptionLineITv.setVisibility(View.GONE);
            mExtendTimeLl.setVisibility(View.INVISIBLE);
        } else {
            // Clean result appear
            int[] extendTimes = BatteryUtils.calculateBatteryExtendTime(mBatteryPercentNumber);
            int hour = extendTimes[0];
            int minute = extendTimes[1];

            if (hour == 0 && minute == 0) {
                mExtendDescriptionLineOTv.setText(getString(R.string.battery_excellent_content));
                mExtendDescriptionLineITv.setVisibility(View.GONE);
                mExtendTimeLl.setVisibility(View.INVISIBLE);
            } else {
                mExtendDescriptionLineOTv.setText(getString(R.string.battery_clean_finished_line_o));
                mExtendDescriptionLineITv.setText(getString(R.string.battery_clean_finished_line_i));
                mExtendTimeLl.setVisibility(View.VISIBLE);
                extendHour = (hour < 10) ? ("0" + hour) : String.valueOf(hour);
                extendMinute = (minute < 10) ? ("0" + minute) : String.valueOf(minute);
                mExtendHourTv.setText(extendHour);
                mExtendMinuteTv.setText(extendMinute);
                isBatteryOptimal = false;
            }
        }

        final String extendHourClone = extendHour;
        final String extendMinuteClone = extendMinute;
        final boolean isBatteryOptimalClone = isBatteryOptimal;

        // Cleaning finished bg color
        mCleanFinishedBgColorAnimator = ObjectAnimator.ofInt(this, "bgColor", COLOR_BG_CLEANING_FINISHED_ARRAY);
        mCleanFinishedBgColorAnimator.setDuration(FRAME * COLOR_BG_CLEANING_FINISHED_ARRAY.length);
        mCleanFinishedBgColorAnimator.setEvaluator(new ArgbEvaluator());
        mCleanFinishedBgColorAnimator.start();
        mCleanFinishedBgColorAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mResultPageRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mIsCleaned = true;
                        ResultPageActivity.startForBattery(BatteryActivity.this, isBatteryOptimalClone, extendHourClone, extendMinuteClone, new RefreshListener() {
                            @Override
                            public void onResultViewFinished() {
                                if (mIsCleanAnimating) {
                                    stopAndUpdateView(DELAY_REFRESH_ON_STOP);
                                    mIsCleanAnimating = false;
                                }
                            }
                        });
                    }
                };
            }
        });

        BatteryUtils.setBatteryLastCleanSecondTime(System.currentTimeMillis() / 1000);
    }

    private void startAppIconLoading(long startOff) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (null == appInfoList) {
                    getScanAppInfoListAndRefresh();
                    return;
                }
                appIconLoading();
            }
        }, startOff);
    }

    private void appIconLoading() {
        if (null == appInfoList) {
            return;
        }

        if (null != mScanIconAnimationRl) {
            mScanIconAnimationRl.setVisibility(View.VISIBLE);
        }
        mScanIconAnimationView.setData(appInfoList);
        mScanIconAnimationView.startAnimation(mScanIconAnimationLightIv);
    }

    private void stopAppIconLoading() {
        if (null != mScanIconAnimationRl) {
            mScanIconAnimationRl.setVisibility(View.INVISIBLE);
        }
        mScanIconAnimationView.setEnd(true);
    }

    private void getScanAppInfoListAndRefresh() {
        if (mBatteryDataManager == null) {
            // it is null only when this activity is destroyed
            return;
        }
        appInfoList = mBatteryDataManager.getCleanAnimationBatteryApps();
        if (appInfoList == null) {
            retryTimes++;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (retryTimes >= RETRY_APP_INFO_TIMES) {
                        retryTimes = 0;
                        return;
                    }
                    getScanAppInfoListAndRefresh();
                }
            }, APP_INFO_RETRY_DELAY_TIME);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    appIconLoading();
                }
            });
        }
    }

    private void setBgColor(int color) {
        mCustomRootView.setBackgroundColor(color);
        mOptimizeBtn.setTextColor(color);
    }

    private int getCurrentBgColor(ViewType viewType) {
        int color = COLOR_BG_CAREFUL;
        if (viewType == ViewType.EXCELLENT || viewType == ViewType.CHARGING || viewType == ViewType.CHARGING_COMPLETED) {
            color = COLOR_BG_EXCELLENT;
        }
        return color;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.mode_menu_iv) {
            HSAnalytics.logEvent("Battery_HomePage_Mode_Clicked");
            NavUtils.startActivitySafely(this, new Intent(BatteryActivity.this, BatteryModeActivity.class));

        } else if (i == R.id.rank_menu_iv) {
            HSAnalytics.logEvent("Battery_HomePage_Rank_Clicked");
            NavUtils.startActivitySafely(this, new Intent(BatteryActivity.this, BatteryRankingActivity.class));

        } else if (i == R.id.optimize_btn) {
            if (Utils.checkDoubleClickGlobal()) {
                return;
            }

            final ServiceConnection connection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    ISubAdService adService = ISubAdService.Stub.asInterface(service);
                    if (adService != null) {
                        try {
                            adService.requestBatteryAd();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            HSLog.e(TAG, "battery request ad failed, remote exception");
                        } finally {
                            unbindService(this);
                        }
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };
            Intent intent = new Intent(this, SubAdService.class);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);

            onOptimizeButtonClicked();

        } else if (i == R.id.battery_menu) {
            NavUtils.startActivitySafely(this, new Intent(BatteryActivity.this, BatterySettingsActivity.class));
            mMenuIv.setImageResource(R.drawable.ic_battery_menu);
            ChargingPrefsUtil.setBatteryTipShown();

        } else {
        }
    }

    private void onOptimizeButtonClicked() {
        if (mBatteryChargingLl.getVisibility() == View.VISIBLE) {
            mBatteryChargingLl.setVisibility(View.GONE);
        }
        startCleaningAnimation();
        startBoost();
        if (BatteryUtils.isCleanTimeFrozen()) {
            HSAnalytics.logEvent("Battery_HomePage_Optimize_Clicked", "Type", "Optimized");
        } else {
            switch (mCurrentViewType) {
                case LOW_BATTERY:
                    HSAnalytics.logEvent("Battery_HomePage_Optimize_Clicked", "Type", "Low battery");
                    break;
                case CAREFUL:
                    HSAnalytics.logEvent("Battery_HomePage_Optimize_Clicked", "Type", "Careful");
                    break;
                case EXCELLENT:
                    HSAnalytics.logEvent("Battery_HomePage_Optimize_Clicked", "Type", "Excellent");
                    break;
                case CHARGING:
                case CHARGING_COMPLETED:
                    HSAnalytics.logEvent("Battery_HomePage_Optimize_Clicked", "Type", "Charging");
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        long backTimeFromCleaning = System.currentTimeMillis() - mStartCleaningTime;
        HSLog.d(TAG, "onBackPressed mIsCleaned = " + mIsCleaned + " backTimeFromCleaning = " + backTimeFromCleaning + " mIsCleanAnimating = " + mIsCleanAnimating);
        if (mIsCleaned || backTimeFromCleaning < BACK_FROZEN_TIME) {
            return;
        }

        boolean resumeManualToolBar = resumeManualToolBar();
        HSLog.d(TAG, "onBackPressed resumeManualToolBar = " + resumeManualToolBar);
        if (resumeManualToolBar) {
            stopAndUpdateView(0);
            return;
        }

        if (mIsCleanAnimating) {
            stopAndUpdateView(0);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelBoost();
        HSChargingManager.getInstance().removeChargingListener(mIChargingListener);
        BatteryUtils.setBatteryExitSecondTime(System.currentTimeMillis() / 1000);
        BatteryUtils.setBatteryExitLevel(mBatteryPercentNumber);
        BatteryUtils.setBatteryLastViewType(mCurrentViewType.getValue());
        stopChargingAnimation();
        stopStatusAppearAnimation();
        stopBgColorAnimation();
        stopCleaningAnimation();
        stopCleanFinishAnimation();
        mHandler.removeCallbacksAndMessages(null);
        mBatteryDataManager = null;
    }

    private void startBoost() {
        if (BatteryUtils.isCleanTimeFrozen()) {
            return;
        }
        HSAppMemoryManager.getInstance().setGlobalScanIncludeSysAppList(BoostTipUtils.getSystemApps());
        HSAppMemoryManager.getInstance().setGlobalScanExcludeList(BoostTipUtils.getExcludeSystemApps());
        HSAppMemoryManager.getInstance().startFullClean(null);
    }

    private void cancelBoost() {
        HSAppMemoryManager.getInstance().stopClean();
    }

    public static void initBattery() {
        final IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenFilter.setPriority(SYSTEM_HIGH_PRIORITY);
        HSApplication.getContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    ScreenStatusReceiver.onScreenOff();
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    ScreenStatusReceiver.onScreenOn();
                } else if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                    HSLog.d("Screen Receiver onReceiver screen present");
//                    HSGlobalNotificationCenter.sendNotification(NotificationCondition.EVENT_UNLOCK);
                }
            }
        }, screenFilter);

        Intent addShortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        addShortcutIntent.putExtra("duplicate", false);
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "battery");
        Context context = HSApplication.getContext();
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(context, R.drawable.boost_icon));
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.setClass(context, BatteryActivity.class);
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);
        context.sendBroadcast(addShortcutIntent);
    }
}
