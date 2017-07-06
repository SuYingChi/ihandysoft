package com.ihs.chargingscreen.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.IntRange;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.acb.expressads.AcbExpressAdView;
import com.artw.lockscreen.LockerUtils;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.charging.HSChargingManager;
import com.ihs.charging.HSChargingManager.HSChargingState;
import com.ihs.chargingscreen.Constants;
import com.ihs.chargingscreen.HSChargingScreenManager;
import com.ihs.chargingscreen.notification.ChargeNotifyManager;
import com.ihs.chargingscreen.ui.BubbleView;
import com.ihs.chargingscreen.utils.ChargingAnalytics;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.chargingscreen.utils.ChargingPrefsUtil;
import com.ihs.chargingscreen.utils.ClickUtils;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.alerts.KCAlert;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.ihs.keyboardutils.utils.KCAnalyticUtil;
import com.ihs.keyboardutils.utils.RippleDrawableUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;
import static com.ihs.chargingscreen.HSChargingScreenManager.getChargingState;
import static com.ihs.keyboardutils.iap.RemoveAdsManager.NOTIFICATION_REMOVEADS_PURCHASED;

/**
 * Created by zhixiangxiao on 5/4/16.
 */
public class ChargingScreenActivity extends Activity {

    public static final String NOTIFICATION_CHARGING_ACTIVITY_STARTED = "notification_charging_activity_started";

    private static final int EVENT_CIRCULAR = 100;
    private static final int EVENT_START_SCROLL_UP_ANIMATOR = 101;

    private static final float AUTO_SCROLL_UP_HEIGHT_PERCENT = 0.2f;

    private View rootView;
    private TextView txtCurrentHour;
    private TextView txtCurrentMinute;
    private TextView txtWeek;
    private TextView txtMonth;
    private TextView txtDay;
    private TextView txtBatteryLevelPercent;
    private ImageView imgSetting;

    private TextView txtLeftTime;
    private TextView txtLeftTimeIndicator;
    private TextView txtChargingIndicator;

    private Dialog closeDialog;
    private PopupWindow popupWindow;

    private ImageView[] imgChargingStateList;

    private String[] txtChargingStateStrings;
    private String[] txtLeftTimeIndicatorStrings;
    private String[] txtChargingIndicatorStrings;

    private List<Drawable> imgChargingStateGreenDrawables = new ArrayList<>();
    private List<Drawable> imgChargingStateDarkDrawables = new ArrayList<>();

    private int firstTouchOnX;
    private int timeMinute;
    private boolean isAnimatorQuit;

    private long startDisplayTime;

    //    private AnimatorSet imgScrollUpAnimatorSet;
    private ValueAnimator rootViewTransXAnimator;

    private TelephonyManager telephonyManager;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case EVENT_CIRCULAR:

                    Calendar calendar = Calendar.getInstance();
                    if (calendar.get(Calendar.MINUTE) != timeMinute) {
                        timeMinute = calendar.get(Calendar.MINUTE);
                        updateTime(calendar);
                    }

                    handler.sendEmptyMessageDelayed(EVENT_CIRCULAR, 1000);
                    break;

                case EVENT_START_SCROLL_UP_ANIMATOR:
//                    imgScrollUpAnimatorSet.start();

                    handler.removeMessages(EVENT_START_SCROLL_UP_ANIMATOR);
                    handler.sendEmptyMessageDelayed(EVENT_START_SCROLL_UP_ANIMATOR, 5000);
                    break;

                default:
                    break;
            }

        }
    };

    private HSChargingManager.IChargingListener chargingListener = new HSChargingManager.IChargingListener() {
        @Override
        public void onBatteryLevelChanged(int preBatteryLevel, int curBatteryLevel) {

        }

        @Override
        public void onChargingStateChanged(HSChargingState preChargingState, HSChargingState curChargingState) {
            updateInfo();
        }

        @Override
        public void onChargingRemainingTimeChanged(int chargingRemainingMinutes) {
            txtLeftTime.setText(ChargingManagerUtil.getChargingLeftTimeString(chargingRemainingMinutes));
        }

        @Override
        public void onBatteryTemperatureChanged(float preBatteryTemperature, float curBatteryTemperature) {
        }
    };

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {

        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {

                case TelephonyManager.CALL_STATE_IDLE:
                    break;

                case TelephonyManager.CALL_STATE_RINGING:
                    ChargingScreenActivity.this.finish();
                    return;

                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;

                default:
                    break;
            }
        }
    };
    private BubbleView bubbleView;
    private PowerManager powerManager = (PowerManager) HSApplication.getContext().getSystemService(Context.POWER_SERVICE);
    private AcbExpressAdView acbExpressAdView;
    private FrameLayout adContainer;
    private ImageView removeAds;


    private long createTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        createTime = System.currentTimeMillis();
        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_CHARGING_ACTIVITY_STARTED);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        ChargingManagerUtil.enableCharging(false, "plist");

        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(FLAG_TRANSLUCENT_STATUS);
            window.addFlags(FLAG_TRANSLUCENT_NAVIGATION);
        }

        //原生系统会闪烁？
        if (!(Build.VERSION_CODES.LOLLIPOP == Build.VERSION.SDK_INT
                && ("Google".equals(Build.BRAND) || "google".equals(Build.BRAND)))) {
            window.addFlags(FLAG_FULLSCREEN);
        }

        window.addFlags(FLAG_SHOW_WHEN_LOCKED);
        window.setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        boolean keyguardSecure = LockerUtils.isKeyguardSecure(this);

        if (!keyguardSecure) {
            window.addFlags(FLAG_DISMISS_KEYGUARD);
        }

        ChargingAnalytics.getInstance().chargingScreenShowed();

        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();

        String language = Locale.getDefault().getDisplayLanguage(Locale.ENGLISH);
        if (HSConfig.optBoolean(true, "libChargingScreen", "MultiLanguage", language)) {
            config.locale = Locale.getDefault();
        } else {
            String defaultLanguage = HSConfig.optString("", "libChargingScreen", "MultiLanguage", "DefaultLanguage");
            if (TextUtils.isEmpty(defaultLanguage) || defaultLanguage.equalsIgnoreCase("English")) {
                config.locale = Locale.ENGLISH;
            } else if (defaultLanguage.equalsIgnoreCase("Dutch")) {
                config.locale = new Locale("nl");
            } else if (defaultLanguage.equalsIgnoreCase("Chinese")) {
                config.locale = Locale.CHINESE;
            } else if (defaultLanguage.equalsIgnoreCase("French")) {
                config.locale = Locale.FRENCH;
            } else if (defaultLanguage.equalsIgnoreCase("German")) {
                config.locale = Locale.GERMAN;
            } else if (defaultLanguage.equalsIgnoreCase("Italian")) {
                config.locale = Locale.ITALIAN;
            } else if (defaultLanguage.equalsIgnoreCase("Japanese")) {
                config.locale = Locale.JAPANESE;
            } else if (defaultLanguage.equalsIgnoreCase("Korean")) {
                config.locale = Locale.KOREA;
            } else if (defaultLanguage.equalsIgnoreCase("Spanish")) {
                config.locale = new Locale("es");
            } else if (defaultLanguage.equalsIgnoreCase("Portuguese")) {
                config.locale = new Locale("pt");
            }
        }
        resources.updateConfiguration(config, dm);

        HSChargingManager.getInstance().addChargingListener(chargingListener);

        if (ChargingManagerUtil.hasPermission("android.permission.READ_PHONE_STATE")) {

            telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            }

        }

        setContentView(R.layout.charging_module_activity_charging_screen);

        bubbleView = ((BubbleView) findViewById(R.id.bubbleView));

        rootView = findViewById(R.id.root_view);
        txtCurrentHour = (TextView) findViewById(R.id.txt_current_hour);
        txtCurrentMinute = (TextView) findViewById(R.id.txt_current_minute);
        txtWeek = (TextView) findViewById(R.id.txt_week);
        txtMonth = (TextView) findViewById(R.id.txt_month);
        txtDay = (TextView) findViewById(R.id.txt_day);

        Calendar calendar = Calendar.getInstance();
        timeMinute = calendar.get(Calendar.MINUTE);
        updateTime(calendar);

        imgSetting = (ImageView) findViewById(R.id.img_setting);
        imgSetting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ClickUtils.isFastDoubleClick()) {
                    return;
                }
                showPopupWindow(ChargingScreenActivity.this, imgSetting);
                KCAnalyticUtil.logEvent("HSLib_chargingscreen_settings_clicked");

            }
        });

        txtLeftTime = (TextView) findViewById(R.id.txt_left_time);
        txtLeftTimeIndicator = (TextView) findViewById(R.id.txt_left_time_indicator);
        txtChargingIndicator = (TextView) findViewById(R.id.txt_charging_indicator);

        ImageView appIcon = (ImageView) findViewById(R.id.app_icon);
        TextView appName = (TextView) findViewById(R.id.app_name);
        try {
            appName.setText(getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA)));
            appIcon.setImageDrawable(getPackageManager().getApplicationIcon(getPackageName()));
        } catch (Exception e) {
        }

        adContainer = (FrameLayout) findViewById(R.id.ad_container);
        txtBatteryLevelPercent = (TextView) findViewById(R.id.txt_battery_level);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/dincond_medium.otf");
        txtBatteryLevelPercent.setTypeface(tf);

        imgChargingStateList = new ImageView[]{
                (ImageView) findViewById(R.id.img_charging_state1),
                (ImageView) findViewById(R.id.img_charging_state2),
                (ImageView) findViewById(R.id.img_charging_state3),
        };


        handler.sendEmptyMessageDelayed(EVENT_START_SCROLL_UP_ANIMATOR, 3000);

        handler.sendEmptyMessageDelayed(EVENT_CIRCULAR, 1000);

        initStringListAndDrawableList();
        updateInfo();


        if (!HSConfig.optBoolean(true, "Application", "ChargeLocker", "ShowAppInfo")) {
            findViewById(R.id.ll_info).setVisibility(View.INVISIBLE);
        }

        if (!HSConfig.optBoolean(true, "Application", "ChargeLocker", "ShowSettingIcon")) {
            findViewById(R.id.img_setting).setVisibility(View.INVISIBLE);
        }

        if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            acbExpressAdView = new AcbExpressAdView(HSApplication.getContext(), HSChargingScreenManager.getInstance().getNaitveAdsPlacementName());
            adContainer.addView(acbExpressAdView);

            // 单次关闭广告或永久删除广告
            removeAds = (ImageView) findViewById(R.id.remove_ads);
            removeAds.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    showRemoveAdsDialog();
                }
            });

            acbExpressAdView.setExpressAdViewListener(new AcbExpressAdView.AcbExpressAdViewListener() {
                @Override
                public void onAdClicked(AcbExpressAdView acbExpressAdView) {
                    finish();
                }

                @Override
                public void onAdShown(AcbExpressAdView acbExpressAdView) {
                    removeAds.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void showChargingIndicatorText() {
        final HSChargingState chargingState = HSChargingManager.getInstance().getChargingState();
        if (chargingState == HSChargingState.STATE_CHARGING_SPEED || chargingState == HSChargingState.STATE_CHARGING_CONTINUOUS
                || chargingState == HSChargingState.STATE_CHARGING_TRICKLE) {
            txtChargingIndicator.setVisibility(View.VISIBLE);
        } else {
            txtChargingIndicator.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        HSAnalytics.startFlurry();

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager.isScreenOn()) {
            ChargeNotifyManager.getInstance().setIsChargingActivityAlive(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getChargingState() > 0) {
            bubbleView.start();
        }
        if(System.currentTimeMillis() - startDisplayTime >1000 ){
            startDisplayTime = System.currentTimeMillis();
        }else{
            startDisplayTime = -1;
        }
        HSLog.d("chargingtest onResume");

        long duration = System.currentTimeMillis() - createTime;

        HSLog.d("Charging activity display duration: " + duration + "ms");

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!HSSessionMgr.isSessionStarted()) {
            HSAnalytics.stopFlurry();
        }
        HSLog.d("chargingtest onStop");

        bubbleView.stop();
        showChargingIndicatorText();
    }

    @Override
    public void onPause() {
        super.onPause();
        HSLog.d("chargingtest onPause");

        firstTouchOnX = 0;

        if (!isAnimatorQuit) {
            rootView.setTranslationY(0);
        }

        ChargeNotifyManager.getInstance().setIsChargingActivityAlive(false);

        if(startDisplayTime!=-1){
            logDisplayTime("app_chargingLocker_displaytime", startDisplayTime);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }


    @Override
    public void onDestroy() {
        if (acbExpressAdView != null) {
            acbExpressAdView.destroy();
        }
        acbExpressAdView = null;
        if (telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
            telephonyManager = null;
        }
        HSChargingManager.getInstance().removeChargingListener(chargingListener);
        cancelAllAnimators();
        handler.removeCallbacksAndMessages(null);
        if (null != closeDialog) {
            closeDialog.dismiss();
        }
        super.onDestroy();
        ChargingPrefsUtil.getInstance().setChargingForFirstSession();




    }

    private void updateTime(Calendar calendar) {
        timeMinute = calendar.get(Calendar.MINUTE);

        ContentResolver contentResolver = this.getContentResolver();
        String strTimeFormat = Settings.System.getString(contentResolver, Settings.System.TIME_12_24);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        Date date = new Date();
        if ("24".equals(strTimeFormat)) {
            simpleDateFormat.applyPattern("HH");
            txtCurrentHour.setText(simpleDateFormat.format(date));
        } else {
            simpleDateFormat.applyPattern("hh");
            txtCurrentHour.setText(simpleDateFormat.format(date));
        }

        simpleDateFormat.applyPattern("mm");
        txtCurrentMinute.setText(simpleDateFormat.format(date));

        txtDay.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
        txtWeek.setText(calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH));
        txtMonth.setText(calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        if (isAnimatorQuit) {
            return true;
        }

        if (firstTouchOnX == 0) {
            firstTouchOnX = (int) (event.getX());
        }

        VelocityTracker velocityTracker = VelocityTracker.obtain();
        velocityTracker.addMovement(event);
        velocityTracker.computeCurrentVelocity(1000);

        int currentEventX;

        final int windowWidth = DisplayUtils.getScreenWidthPixels();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            firstTouchOnX = (int) (event.getX());
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {

            currentEventX = (int) (event.getX());

            if (velocityTracker.getXVelocity() > 5000) {
                startRootViewTranXAnimator(windowWidth, windowWidth);
            } else if (currentEventX - firstTouchOnX > 0) {
                rootView.setTranslationX(currentEventX - firstTouchOnX);
            }
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {

            if (rootView.getTranslationX() > 0) {

                if (Math.abs(rootView.getTranslationX()) >= windowWidth * AUTO_SCROLL_UP_HEIGHT_PERCENT) {

                    startRootViewTranXAnimator(windowWidth, windowWidth);
                } else {
                    startRootViewTranXAnimator(0, windowWidth);
                }

            }

            velocityTracker.recycle();
        }

        return super.dispatchTouchEvent(event);
    }

    private void startRootViewTranXAnimator(float endTransX, final float windowWidth) {

        if (endTransX == windowWidth) {
            isAnimatorQuit = true;
        }

        rootViewTransXAnimator = ObjectAnimator.ofFloat(rootView, "translationX", rootView.getTranslationX(), endTransX);
        rootViewTransXAnimator.setDuration((int) (Math.abs((rootView.getTranslationX()) - endTransX) * 400 / windowWidth));
        rootViewTransXAnimator.start();
        rootViewTransXAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                rootView.setTranslationX((float) (animation.getAnimatedValue()));
            }
        });

        rootViewTransXAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (rootView.getTranslationX() >= windowWidth) {
                    ChargeNotifyManager.getInstance().setIsChargingActivityAlive(false);
                    if (HSChargingManager.getInstance().isCharging()) {
                        ChargeNotifyManager.getInstance().pendingToShow(ChargeNotifyManager.PUSH_CHARGING_PRIORITY);
                        HSGlobalNotificationCenter.sendNotificationOnMainThread(Constants.EVENT_CHARGING_SHOW_PUSH);

                    }
                    finish();

                }
            }
        });
    }

    private void showPopupWindow(Context context, View parentView) {
        if (popupWindow == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View view = layoutInflater.inflate(R.layout.charging_module_popup_window, null);
            TextView txtCloseChargingBoost = (TextView) view.findViewById(R.id.txt_close_charging_boost);
            txtCloseChargingBoost.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ClickUtils.isFastDoubleClick()) {
                        return;
                    }

                    if (popupWindow != null) {
                        popupWindow.dismiss();
                    }

                    showAlert();
                    ChargingAnalytics.getInstance().chargingDisableTouchedOnce("activity");
                    KCAnalyticUtil.logEvent("HSLib_chargingscreen_Charge_TurnOff_Clicked");
                }
            });

            popupWindow = new PopupWindow(view);
            popupWindow.setWidth(LayoutParams.WRAP_CONTENT);
            popupWindow.setHeight(LayoutParams.WRAP_CONTENT);
            popupWindow.setFocusable(true);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.charging_module_popup_window_bg));
            popupWindow.update();
        }

        popupWindow.showAsDropDown(parentView, -30, -20);
    }

    private void showAlert() {
        if (closeDialog == null) {
            closeDialog = new Dialog(this, R.style.dialog);
            closeDialog.setContentView(R.layout.charging_module_alert_close_charge_screen);

            TextView closeAlertTitle = (TextView) closeDialog.findViewById(R.id.close_alert_title);
            String moduleName = HSChargingScreenManager.getInstance().getModuleName();
            if (moduleName == null || "".equals(moduleName)) {
                moduleName = getResources().getString(R.string.charging_module_default_module_name);
            }
//            closeAlertTitle.setText(getResources().getString(R.string.charging_module_close_charging_boost) + moduleName + "?");

            closeAlertTitle.setText(R.string.disable_battery_master);
            View btnCancel = closeDialog.findViewById(R.id.alert_cancel);
            View btnClose = closeDialog.findViewById(R.id.alert_close);

            btnCancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (closeDialog == null) {
                        return;
                    }
                    closeDialog.dismiss();
                    closeDialog = null;
                }
            });

            btnClose.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (closeDialog == null) {
                        return;
                    }
                    closeDialog.dismiss();
                    closeDialog = null;

                    ChargingAnalytics.getInstance().chargingDisableConfirmedOnce("activity");


                    HSChargingScreenManager.getInstance().stop(true);
                    ChargingPrefsUtil.getInstance().setChargingEnableByUser(false);

                    finish();

                    KCAnalyticUtil.logEvent("HSLib_chargingscreen_Charge_Alert_Disable_Clicked");
                }
            });
            btnCancel.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(Color.WHITE, 0, 0, 0, DisplayUtils.dip2px(8)));
            btnClose.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(Color.WHITE, 0, 0, DisplayUtils.dip2px(8), 0));

        }
        closeDialog.show();
    }

    private void initStringListAndDrawableList() {

        Resources resources = getResources();

        txtChargingStateStrings = new String[]{
                resources.getString(R.string.charging_module_charging_state_speed),
                resources.getString(R.string.charging_module_charging_state_continuous),
                resources.getString(R.string.charging_module_charging_state_trickle),
                resources.getString(R.string.charging_module_charging_state_finish), //3
                resources.getString(R.string.charging_module_charging_state_unknown) //4
        };

        txtLeftTimeIndicatorStrings = new String[]{
                resources.getString(R.string.charging_module_speed_charging_left_time_indicator),
                resources.getString(R.string.charging_module_continuous_charging_left_time_indicator),
                resources.getString(R.string.charging_module_trickle_charging_left_time_indicator),
                resources.getString(R.string.charging_module_finish_charging_left_time_indicator),
                resources.getString(R.string.charging_module_charging_state_unknown),
        };

        txtChargingIndicatorStrings = new String[]{
                resources.getString(R.string.charging_module_charging_state_speed_charging_indicator),
                resources.getString(R.string.charging_module_charging_state_continuous_charging_indicator),
                resources.getString(R.string.charging_module_charging_state_trickle_charging_indicator),
        };

        imgChargingStateGreenDrawables.add(resources.getDrawable(R.drawable.ic_charging_speed));
        imgChargingStateGreenDrawables.add(resources.getDrawable(R.drawable.ic_charging_continue));
        imgChargingStateGreenDrawables.add(resources.getDrawable(R.drawable.ic_charging_trickle));

        imgChargingStateDarkDrawables.add(resources.getDrawable(R.drawable.ic_charging_speed_dark));
        imgChargingStateDarkDrawables.add(resources.getDrawable(R.drawable.ic_charging_continue_dark));
        imgChargingStateDarkDrawables.add(resources.getDrawable(R.drawable.ic_charging_trickle_dark));
    }

    private void cancelAllAnimators() {

        cancelAnimator(flashAnimatorSet);
        cancelAnimator(rootViewTransXAnimator);

        flashAnimatorSet = null;
        rootViewTransXAnimator = null;
    }

    private void cancelAnimator(Animator animator) {
        if (animator == null) {
            return;
        }
        animator.removeAllListeners();
        animator.cancel();
    }

    private boolean isImgChargingStateFlash() {

        HSChargingState chargingState = HSChargingManager.getInstance().getChargingState();

        return chargingState != HSChargingState.STATE_DISCHARGING
                && chargingState != HSChargingState.STATE_CHARGING_FULL;
    }

    private AnimatorSet flashAnimatorSet;

    private void startFlashAnimation(final int imgChargingStateGreenDrawableCount) {

        cancelAnimator(flashAnimatorSet);
        flashAnimatorSet = null;

        if (!isImgChargingStateFlash()) {
            return;
        }

        if (imgChargingStateGreenDrawableCount <= 0) {
            return;
        }

        final int ANIMATION_DURATION = 700;
        final int ANIMATION_START_DELAY = 150;

        ValueAnimator imgChargingStateDisAppearAnimator = ObjectAnimator.ofInt(imgChargingStateList[imgChargingStateGreenDrawableCount - 1],
                "alpha", 255, 125);
        imgChargingStateDisAppearAnimator.setDuration(ANIMATION_DURATION);
        imgChargingStateDisAppearAnimator.setStartDelay(ANIMATION_START_DELAY);


        ValueAnimator imgChargingStateAppearAnimator = ObjectAnimator.ofInt(imgChargingStateList[imgChargingStateGreenDrawableCount - 1],
                "alpha", 125, 255);
        imgChargingStateAppearAnimator.setDuration(500);
        imgChargingStateAppearAnimator.setStartDelay(ANIMATION_START_DELAY);

        flashAnimatorSet = new AnimatorSet();
        flashAnimatorSet.play(imgChargingStateAppearAnimator).after(imgChargingStateDisAppearAnimator);

        flashAnimatorSet.start();
        flashAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (flashAnimatorSet != null) {
                    flashAnimatorSet.start();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                imgChargingStateList[imgChargingStateGreenDrawableCount - 1].setAlpha(255);
            }
        });

    }

    private void updateInfo() {
        txtBatteryLevelPercent.setText(new StringBuilder(String.valueOf((HSChargingManager.getInstance().getBatteryRemainingPercent())))
                .toString());


        for (ImageView imgChargingState : imgChargingStateList) {
            imgChargingState.clearAnimation();
            imgChargingState.setAlpha(255);
        }


        switch (HSChargingManager.getInstance().getChargingState()) {

            case STATE_CHARGING_SPEED:

                updateTxtAndImgAndStartFlashAnim(0, 1);
                break;
            case STATE_CHARGING_CONTINUOUS:

                updateTxtAndImgAndStartFlashAnim(1, 2);
                break;
            case STATE_CHARGING_TRICKLE:

                updateTxtAndImgAndStartFlashAnim(2, 3);
                break;
            case STATE_CHARGING_FULL:

                updateTxtAndImgAndStartFlashAnim(3, 3);
                break;
            case STATE_DISCHARGING:

                unplugged();
                break;
            default:
                break;
        }
    }

    private void unplugged() {
        startFlashAnimation(0);

        txtLeftTimeIndicator.setText(txtLeftTimeIndicatorStrings[4]);
        txtLeftTime.setVisibility(View.GONE);
        bubbleView.stop();
        for (int i = 0; i < imgChargingStateList.length; i++) {
            imgChargingStateList[i].setImageDrawable(imgChargingStateDarkDrawables.get(i));
        }
    }

    private void updateTxtAndImgAndStartFlashAnim(@IntRange(from = 0, to = 4) int txtChargingStateStringIndex,
                                                  @IntRange(from = 0, to = 3) int imgChargingStateGreenDrawableCount) {

        if (powerManager.isScreenOn() && txtChargingStateStringIndex <= 4) {
            bubbleView.start();
        } else {
            bubbleView.stop();
        }


        if (txtChargingStateStringIndex != 4 && txtChargingStateStringIndex != 3) {
            txtChargingIndicator.setVisibility(View.VISIBLE);
            txtChargingIndicator.setText(txtChargingIndicatorStrings[txtChargingStateStringIndex]);
        }
        txtLeftTimeIndicator.setVisibility(View.VISIBLE);
        txtLeftTimeIndicator.setText(txtLeftTimeIndicatorStrings[txtChargingStateStringIndex]);

        txtLeftTime.setVisibility(View.VISIBLE);
        txtLeftTime.setText(ChargingManagerUtil.getChargingLeftTimeString(HSChargingManager.getInstance().getChargingLeftMinutes()));
//        } else {
//            txtChargingIndicator.setVisibility(View.INVISIBLE);
//            txtLeftTimeIndicator.setVisibility(View.INVISIBLE);
//            txtLeftTime.setVisibility(View.INVISIBLE);
//        }

        for (int i = 0; i < imgChargingStateList.length; i++) {
            ImageView imgChargingState = imgChargingStateList[i];

            if (i < imgChargingStateGreenDrawableCount) {
                imgChargingState.setImageDrawable(imgChargingStateGreenDrawables.get(i));
            } else {
                imgChargingState.setImageDrawable(imgChargingStateDarkDrawables.get(i));
            }


        }

        if (txtChargingStateStringIndex == 4) {
            imgChargingStateList[0].setImageDrawable(imgChargingStateDarkDrawables.get(0));
        }

        startFlashAnimation(imgChargingStateGreenDrawableCount);
    }


    public static void logDisplayTime(String key, long startDisplayTime) {
        long totalTime = (System.currentTimeMillis() - startDisplayTime) / 1000;

        if (totalTime < 1) {
            KCAnalyticUtil.logEvent(key, "0~1s");
        } else if (totalTime < 2) {
            KCAnalyticUtil.logEvent(key, "1~2s");
        } else if (totalTime < 3) {
            KCAnalyticUtil.logEvent(key, "2~3s");
        } else if (totalTime < 4) {
            KCAnalyticUtil.logEvent(key, "3~4s");
        } else if (totalTime < 5) {
            KCAnalyticUtil.logEvent(key, "4~5s");
        } else if (totalTime < 6) {
            KCAnalyticUtil.logEvent(key, "5~6s");
        } else if (totalTime < 7) {
            KCAnalyticUtil.logEvent(key, "6~7s");
        } else if (totalTime < 8) {
            KCAnalyticUtil.logEvent(key, "7~8s");
        } else {
            KCAnalyticUtil.logEvent(key, "8s+");
        }
    }

    private void showRemoveAdsDialog() {
        final Dialog removeAdsDialog = new Dialog(this, R.style.dialog);
        removeAdsDialog.setContentView(R.layout.remove_ads_dialog);

        View btnJustOnce = removeAdsDialog.findViewById(R.id.btn_just_once);
        View btnForever = removeAdsDialog.findViewById(R.id.btn_forever);

        btnJustOnce.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAdsDialog.dismiss();

                adContainer.removeView(acbExpressAdView);
                removeAds.setVisibility(View.GONE);
            }
        });

        btnForever.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAdsDialog.dismiss();

                RemoveAdsManager.getInstance().purchaseRemoveAds();

                HSGlobalNotificationCenter.addObserver(NOTIFICATION_REMOVEADS_PURCHASED, new INotificationObserver() {
                    @Override
                    public void onReceive(String s, HSBundle hsBundle) {
                        HSGlobalNotificationCenter.removeObserver(this);
                        if (removeAds != null) {
                            removeAds.setVisibility(View.GONE);
                        }
                        if (acbExpressAdView != null) {
                            adContainer.removeView(acbExpressAdView);
                            acbExpressAdView.destroy();
                            acbExpressAdView = null;
                        }
                    }
                });
            }
        });
        btnForever.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(Color.WHITE, 0, 0, 0, DisplayUtils.dip2px(8)));
        btnJustOnce.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(Color.WHITE, 0, 0, DisplayUtils.dip2px(8), 0));

        removeAdsDialog.show();
    }
}
