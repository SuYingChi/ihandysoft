package com.ihs.feature.boost;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.acb.adadapter.AcbAd;
import com.acb.adadapter.AcbNativeAd;
import com.acb.adadapter.ContainerView.AcbNativeAdContainerView;
import com.acb.adadapter.ContainerView.AcbNativeAdIconView;
import com.acb.adadapter.ContainerView.AcbNativeAdPrimaryView;
import com.acb.nativeads.AcbNativeAdLoader;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.feature.common.FormatSizeBuilder;
import com.ihs.feature.common.LauncherFiles;
import com.ihs.feature.common.PreferenceHelper;
import com.ihs.feature.common.Thunk;
import com.ihs.feature.common.Utils;
import com.ihs.feature.common.VectorCompat;
import com.ihs.feature.common.ViewUtils;
import com.ihs.feature.common.WeatherSettings;
import com.ihs.feature.ui.FloatWindowListener;
import com.ihs.feature.ui.LauncherFloatWindowManager;
import com.ihs.feature.ui.SafeWindowManager;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.CommonUtils;
import com.ihs.keyboardutils.utils.LauncherAnimationUtils;

import java.util.List;

/**
 * After boost, this tip view is shown as floating window.
 */
@SuppressLint("ViewConstructor")
public class BoostTip extends FrameLayout implements View.OnClickListener, FloatWindowListener,
        AcbNativeAd.AcbNativeClickListener, AcbNativeAd.AcbAdListener {

    public static final String TAG = BoostTip.class.getSimpleName();

    public static final String NOTIFICATION_LAUNCH_BOOST_PLUS = "launch_boost_plus";
    public static final String NOTIFICATION_BOOST_END = "notification_boost_end";
    public static final String BUNDLE_KEY_SRC_BUTTON = "src_button";

    private static final String PREF_KEY_BOOST_TIP_SHOW_COUNT = "boost_tip_show_count";
    public static final String PREF_KEY_BOOST_PLUS_SHOW_TIME_OPTIMAL = "boost_plus_show_time_optimal";
    public static final String PREF_KEY_BOOST_PLUS_SHOW_TIME_MEMORY = "boost_plus_show_time_memory";
    public static final String PREF_KEY_BOOST_PLUS_SHOW_TIME_BATTERY = "boost_plus_show_time_battery";

    private static final int FADE_IN_NORMAL_ANIM_DURATION = 250;
    private static final int FADE_OUT_ANIM_DURATION_LONG = 800;
    private static final int FADE_OUT_ANIM_DURATION_SHORT = 400;
    private static final int AD_SHOW_ANIM_DURATION = 800;
    private static final int AUTO_DISMISS_DURATION = 3000;

    private enum BoostPlusViewType {
        OPTIMAL,
        MEMORY,
        BATTERY,
    }

    private BoostPlusViewType mBoostPlusViewType = BoostPlusViewType.MEMORY;

    private Context mContext;
    @Thunk
    PreferenceHelper mPrefs;

    private BoostType mType;
    private int mBoostedPercentage;
    private BoostSource mBoostSource;

    private View mBackground;
    private View mContentWrapper;
    private View mContentWrapperPopup;
    private ImageView mTypeIcon;
    private TextView mTitleText;
    private LinearLayout mDismissButton;

    private AcbNativeAd mAd;
    private boolean mIsAutoDismissState = true;

    private final Runnable mDismissAction = new Runnable() {
        @Override
        public void run() {
            dismiss(true);
        }
    };

    private RelativeLayout mAdContainerLayout;
    private RelativeLayout mAdWindowLayout;

    public BoostTip(Context context, BoostType type, int boostedPercentage, BoostSource source) {
        super(context);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.boost_tip, this);

        mBackground = findViewById(R.id.boost_bg);
        mContentWrapper = findViewById(R.id.content_wrapper);
        mContentWrapperPopup = findViewById(R.id.content_wrapper_popup);
        mTypeIcon = ViewUtils.findViewById(mContentWrapper, R.id.boost_type_icon);
        mTitleText = (TextView) mContentWrapper.findViewById(R.id.boost_title_text);
        mDismissButton = (LinearLayout) findViewById(R.id.boost_dismiss_btn);
        mAdContainerLayout = (RelativeLayout) mContentWrapper.findViewById(R.id.boost_ad_container);
        mAdContainerLayout.setVisibility(View.GONE);
        mAdWindowLayout = (RelativeLayout) mContentWrapper.findViewById(R.id.boost_ad_window);
        mAdWindowLayout.setVisibility(View.GONE);

        mContentWrapperPopup.setBackgroundResource(R.drawable.dialog_normal_bg);

        mType = type;
        mBoostedPercentage = boostedPercentage;
        mBoostSource = source;
        bindViews();

        mContentWrapper.setOnClickListener(this);
        mBackground.setOnClickListener(this);
        mDismissButton.setOnClickListener(this);
        mPrefs = PreferenceHelper.get(LauncherFiles.BOOST_PREFS);
    }

    private void bindViews() {
        if (isEffectiveBoost()) {
            @StringRes int titleFixedId = R.string.boost_tip_title_memory;
            @ColorRes int resultColorId = R.color.boost_result_text_memory;
            String resultString = "";
            switch (mType) {
                case RAM:
                    mTypeIcon.setImageResource(R.drawable.boost_result_memory);
                    titleFixedId = R.string.boost_tip_title_memory;
                    resultColorId = R.color.boost_result_text_memory;
                    FormatSizeBuilder resultStringBuilder = new FormatSizeBuilder(
                            BoostUtils.getBoostedMemSizeBytes(mContext, mBoostedPercentage));
                    resultString = resultStringBuilder.sizeUnit;
                    break;
                case BATTERY:
                    mTypeIcon.setImageResource(R.drawable.boost_result_battery);
                    titleFixedId = R.string.boost_tip_title_battery;
                    resultColorId = R.color.boost_result_text_battery;
                    int extendedTimeMinutes = BoostUtils.getExtendedBatteryLife(mBoostedPercentage);
                    resultString = getContext().getString(R.string.set_as_default_boost_battery_figure, extendedTimeMinutes);
                    break;
                case CPU_TEMPERATURE:
                    mTypeIcon.setImageResource(R.drawable.boost_result_temperature);
                    titleFixedId = R.string.boost_tip_title_cpu_temperature;
                    resultColorId = R.color.boost_result_text_cpu_temperature;
                    float cooledCpuTemperatureCelsius = BoostUtils.getCooledCpuTemperature(mBoostedPercentage);
                    if (WeatherSettings.shouldDisplayFahrenheit()) {
                        float cooledCpuTemperatureFahrenheit = Utils.celsiusToFahrenheit(cooledCpuTemperatureCelsius);
                        resultString = getContext().getString(R.string.set_as_default_boost_cool_figure_fahrenheit,
                                Math.round(cooledCpuTemperatureFahrenheit));
                    } else {
                        resultString = getContext().getString(R.string.set_as_default_boost_cool_figure_celsius,
                                Math.round(cooledCpuTemperatureCelsius));
                    }
                    break;
            }

            String titleFixed = getContext().getString(titleFixedId);


            String fullString = titleFixed + " " + resultString;
            Spannable spannable = new SpannableString(fullString);
            spannable.setSpan(new ForegroundColorSpan(
                            ContextCompat.getColor(getContext(), resultColorId)),
                    titleFixed.length(), fullString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(Typeface.NORMAL),
                    titleFixed.length(), fullString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new RelativeSizeSpan(50f / 48f),
                    titleFixed.length(), fullString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mTitleText.setText(spannable, TextView.BufferType.SPANNABLE);
        } else {
            mTypeIcon.setImageResource(R.drawable.boost_result_memory);
            mTitleText.setText(R.string.boost_tip_optimal);
        }
    }

    @Override
    public void onAddedToWindow(SafeWindowManager windowManager) {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("NewApi")
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Removed from default preferences file since v1.3.4 (44) and moved to separate file
                HSPreferenceHelper.getDefault().remove(PREF_KEY_BOOST_TIP_SHOW_COUNT);
                show();
                mDismissButton.setVisibility(View.VISIBLE);
                if (mIsAutoDismissState) {
                    mDismissButton.setVisibility(View.INVISIBLE);
                    postDelayed(mDismissAction, AUTO_DISMISS_DURATION);
                }
            }
        });
    }

    private boolean isEffectiveBoost() {
        return mBoostedPercentage >= BoostConditionManager.EFFECTIVE_BOOST_PERCENTAGE_THRESHOLD;
    }

    @Override
    public void onClick(final View v) {
        if (v == mContentWrapper) {

        } else if (v == mDismissButton) {
            HSLog.d("Boost.Alert", "boost toast closed");
            HSAnalytics.logEvent("Boost_Toast_Closed");
            dismiss(true);
            mBackground.setClickable(false);
        } else if (v == mBackground) {
            HSLog.d("Boost.Alert", "click else");
            dismiss(true);
            mBackground.setClickable(false);
        } else if (v.getId() == R.id.boost_tip_boost_plus_btn
                || v.getId() == R.id.boost_tip_boost_plus_container) {
            postDelayed(new Runnable() {
                @Override public void run() {
                    HSBundle data = new HSBundle();
                    data.putObject(BUNDLE_KEY_SRC_BUTTON, v);
                    HSGlobalNotificationCenter.sendNotification(NOTIFICATION_LAUNCH_BOOST_PLUS, data);
                }
            }, FADE_OUT_ANIM_DURATION_SHORT);

            switch (mBoostPlusViewType) {
                case OPTIMAL:
                    HSAnalytics.logEvent("BoostPlus_Open", "Type", "Toast Optimal");
                    break;
                case MEMORY:
                    HSAnalytics.logEvent("BoostPlus_Open", "Type", "Toast Memory");
                    break;
                case BATTERY:
                    HSAnalytics.logEvent("BoostPlus_Open", "Type", "Toast Battery");
                    break;
            }
            dismiss(false);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_OUTSIDE:
                dismiss(true);
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void show() {
        mContentWrapper.setVisibility(VISIBLE);
        mContentWrapper.setAlpha(0f);
        mContentWrapper.animate().alpha(1f)
                .setDuration(FADE_IN_NORMAL_ANIM_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
        HSAnalytics.logEvent("Boost_Toast_Shown");

        if (mBoostSource == BoostSource.LOCKER_TOGGLE) {
            return;
        }

        if (BoostTipUtils.mayShowChargingScreen(mType)) {
            mContentWrapperPopup.setBackgroundResource(R.drawable.dialog_boost_with_ad_above_bg);
            mIsAutoDismissState = false;
            setupChargingScreenView();
            showSlideDownView();
            BoostTipUtils.incrementAdShowIntervalCount();
            return;
        }

        boolean willShowBoostPlusView = false;
        boolean boostPlusShown = false;
        boolean boostAdShown = false;

        if (BoostTipUtils.getIfMayShowBoostPlus()) {
            HSLog.d(TAG, "May show boost plus");
            willShowBoostPlusView = setupBoostPlusView();
            if (willShowBoostPlusView) {
                mContentWrapperPopup.setBackgroundResource(R.drawable.dialog_boost_with_ad_above_bg);
                HSLog.d(TAG, "Will show boost plus");
                showSlideDownView();
                boostPlusShown = true;
                mIsAutoDismissState = false;
            }
        }

        boolean shouldShowBoostAd = BoostTipUtils.getShouldShowBoostAd();
        HSLog.d(TAG, "willShowBoostPlusView: " + willShowBoostPlusView
                + ", shouldShowBoostAd: " + shouldShowBoostAd);
        if (!willShowBoostPlusView && shouldShowBoostAd) {
            boostAdShown = getAndShowAd();
        }

        if (boostAdShown) {
            BoostTipUtils.resetAdShowIntervalCount();
        } else if (!boostPlusShown) {
            BoostTipUtils.incrementAdShowIntervalCount();
        }
    }

    private boolean getAndShowAd() {
        List<AcbNativeAd> ads = AcbNativeAdLoader.fetch(HSApplication.getContext(), HSApplication.getContext().getString(R.string.ad_placement_boost_tip), 1);
        mAd = ads.isEmpty() ? null : ads.get(0);
        if (mAd != null) {
            mContentWrapperPopup.setBackgroundResource(R.drawable.dialog_boost_with_ad_above_bg);
            setupAdsView(mAd);
            HSLog.d(TAG, "Actually show ad");
            showSlideDownView();
            mIsAutoDismissState = false;
            mAd.setNativeClickListener(new AcbNativeAd.AcbNativeClickListener() {
                @Override
                public void onAdClick(AcbAd acbAd) {
                    dismiss(false);
                }
            });
            return true;
        } else {
            HSLog.d(TAG, "No ad to show");
            return false;
        }
    }

    public void dismiss(boolean isLongDuration) {
        mContentWrapper.animate().alpha(0f)
                .setDuration(FADE_OUT_ANIM_DURATION_LONG)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        removeCallbacks(mDismissAction);
                        LauncherFloatWindowManager.getInstance().removeBoostTip();
//                        showFiveStarRateIfNeeded();
                        releaseAds();
                        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_BOOST_END);
                    }
                });
        if (isLongDuration) {
            mContentWrapper.animate().setDuration(FADE_OUT_ANIM_DURATION_LONG);
        } else {
            mContentWrapper.animate().setDuration(FADE_OUT_ANIM_DURATION_SHORT);
        }
        mContentWrapper.animate().start();
    }

    private void releaseAds() {
        if (mAd != null) {
            mAd.release();
            mAd = null;
        }
    }

    private void showSlideDownView() {
        mAdContainerLayout.setVisibility(View.VISIBLE);
        mAdWindowLayout.setVisibility(View.VISIBLE);
        mAdContainerLayout.setAlpha(0f);
        mAdContainerLayout.setTranslationY(-(1100));
        mAdContainerLayout.animate().alpha(1f)
                .translationY(0)
                .setDuration(AD_SHOW_ANIM_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void setupAdsView(AcbNativeAd nativeAd) {
        if (nativeAd != null) {
            HSLog.d("Boost.Alert", "loadAds success");
            final View containerView = View.inflate(getContext(), R.layout.boost_tip_ad_hs, null);
            AcbNativeAdContainerView nativeAdContainerView = new AcbNativeAdContainerView(mContext);
            nativeAdContainerView.addContentView(containerView);

            nativeAdContainerView.setAdPrimaryView((AcbNativeAdPrimaryView) containerView.findViewById(R.id.boost_ad_image_container));
            nativeAdContainerView.setAdChoiceView((ViewGroup) containerView.findViewById(R.id.boost_ad_choice));
            nativeAdContainerView.setAdIconView((AcbNativeAdIconView) containerView.findViewById(R.id.boost_ad_icon));
            nativeAdContainerView.setAdTitleView((TextView) containerView.findViewById(R.id.boost_ad_title));
            nativeAdContainerView.setAdBodyView((TextView) containerView.findViewById(R.id.boost_ad_description));
            nativeAdContainerView.setAdActionView(containerView.findViewById(R.id.boost_ad_action));

            final ImageView flash = (ImageView) containerView.findViewById(R.id.boost_ad_action_flash);
            Runnable flashAction = new Runnable() {
                @Override
                public void run() {
                    flash.setVisibility(View.VISIBLE);
                    flash.setTranslationX(-60f);
                    flash.animate()
                            .setDuration(getContext().getResources().getInteger(R.integer.config_resultPageActionButtonFlashDuration))
                            .setInterpolator(LauncherAnimationUtils.linearInterpolator)
                            .translationX(CommonUtils.getPhoneWidth(getContext()))
                            .start();
                }
            };
            flash.postDelayed(flashAction, 1500);
            flash.postDelayed(flashAction, 3000);
            flash.postDelayed(flashAction, 4500);

            mAdContainerLayout.addView(nativeAdContainerView);
            nativeAdContainerView.fillNativeAd(nativeAd);
        } else {
            HSLog.d("Boost.Alert", "loadAds fail");
        }
    }

    private boolean setupBoostPlusView() {
        View boostPlusView = LayoutInflater.from(getContext()).inflate(R.layout.boost_tip_boost_plus, mAdContainerLayout, false);
        ImageView icon = (ImageView) boostPlusView.findViewById(R.id.boost_tip_boost_plus_icon);
        TextView title = (TextView) boostPlusView.findViewById(R.id.boost_tip_boost_plus_title);
        TextView description = (TextView) boostPlusView.findViewById(R.id.boost_tip_boost_plus_desc);
        TextView button = (TextView) boostPlusView.findViewById(R.id.boost_tip_boost_plus_btn);

        boolean optimal = !isEffectiveBoost();
        if (optimal) {
            // Optimal
            mBoostPlusViewType = BoostPlusViewType.OPTIMAL;
            if (BoostTipUtils.getAlreadyShownOptimalToday()) {
                HSLog.d(TAG, "Already shown Boost+ optimal view today");
                return false;
            }
            icon.setImageDrawable(VectorCompat.createVectorDrawable(getContext(), R.drawable.boost_plus_tip_optimal));
            title.setText(getContext().getText(R.string.boost_plus_tip_optimal_title));
            description.setText(getContext().getText(R.string.boost_plus_tip_optimal_desc));
            button.setText(getContext().getText(R.string.boost_plus_tip_memory_btn));
            button.setBackgroundResource(R.drawable.material_compat_button_bg_blue);

            mPrefs.putLong(PREF_KEY_BOOST_PLUS_SHOW_TIME_OPTIMAL, System.currentTimeMillis());
            HSAnalytics.logEvent("BoostPlus_Show", "Type", "Toast Optimal");
        } else if (BoostTipUtils.getLowBattery() && !BoostTipUtils.getAlreadyShownBatteryToday()) {
            // Battery
            mBoostPlusViewType = BoostPlusViewType.BATTERY;
            icon.setImageDrawable(VectorCompat.createVectorDrawable(getContext(), R.drawable.boost_plus_tip_battery));
            title.setText(getContext().getText(R.string.boost_plus_tip_battery_title));
            description.setText(getContext().getText(R.string.boost_plus_tip_battery_desc));
            button.setText(getContext().getText(R.string.boost_plus_tip_battery_btn));
            button.setBackgroundResource(R.drawable.boost_tip_boost_plus_green_bg);

            mPrefs.putLong(PREF_KEY_BOOST_PLUS_SHOW_TIME_BATTERY, System.currentTimeMillis());
            HSAnalytics.logEvent("BoostPlus_Show", "Type", "Toast Battery");
        } else {
            // Memory
            mBoostPlusViewType = BoostPlusViewType.MEMORY;
            if (BoostTipUtils.getAlreadyShownMemoryToday()) {
                HSLog.d(TAG, "Already shown Boost+ memory view today");
                return false;
            }
            icon.setImageDrawable(VectorCompat.createVectorDrawable(getContext(), R.drawable.boost_plus_tip_memory));
            title.setText(getContext().getText(R.string.boost_plus_tip_memory_title));
            description.setText(getContext().getText(R.string.boost_plus_tip_memory_desc));
            button.setText(getContext().getText(R.string.boost_plus_tip_memory_btn));
            button.setBackgroundResource(R.drawable.material_compat_button_bg_blue);

            mPrefs.putLong(PREF_KEY_BOOST_PLUS_SHOW_TIME_MEMORY, System.currentTimeMillis());
            HSAnalytics.logEvent("BoostPlus_Show", "Type", "Toast Memory");
        }
        boostPlusView.setOnClickListener(this);
        mAdContainerLayout.addView(boostPlusView);
        return true;
    }

    private void setupChargingScreenView() {
        View boostPlusView = View.inflate(getContext(), R.layout.boost_tip_charging_screen, null);
        final TextView title = (TextView) boostPlusView.findViewById(R.id.boost_tip_charging_screen_title);
        TextView button = (TextView) boostPlusView.findViewById(R.id.boost_tip_charging_screen_btn);

        if (mType == BoostType.BATTERY) {
            title.setText(getContext().getText(R.string.charging_screen_guide_battery_title));
            HSAnalytics.logEvent("ChargingScreen_Toast_Shown", "type", "Battery life");
        } else if (mType == BoostType.CPU_TEMPERATURE) {
            title.setText(getContext().getText(R.string.charging_screen_guide_temperature_title));
            HSAnalytics.logEvent("ChargingScreen_Toast_Shown", "type", "CPU");
        }
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss(false);
                ChargingManagerUtil.enableCharging(false,"BoostTip");
                HSAnalytics.logEvent("ChargingScreen_Toast_TurnOn_Clicked", "type",
                        mType == BoostType.BATTERY ? "Battery life" : "CPU");
                Toast.makeText(getContext(), R.string.charging_screen_guide_turn_on, Toast.LENGTH_SHORT).show();
            }
        });
        mAdContainerLayout.addView(boostPlusView);
    }

    @Override
    public void onAdExpired(AcbAd acbAd) {
    }

    @Override
    public void onAdWillExpired(AcbAd acbAd) {
    }

    @Override
    public void onAdClick(AcbAd acbAd) {
    }
}
