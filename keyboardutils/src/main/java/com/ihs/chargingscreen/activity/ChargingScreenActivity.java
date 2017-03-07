package com.ihs.chargingscreen.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
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
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.acb.adadapter.AcbAd;
import com.acb.adadapter.AcbNativeAd;
import com.acb.adadapter.ContainerView.AcbNativeAdContainerView;
import com.acb.adadapter.ContainerView.AcbNativeAdIconView;
import com.acb.adadapter.ContainerView.AcbNativeAdPrimaryView;
import com.acb.nativeads.AcbNativeAdLoader;
import com.acb.nativeads.AcbNativeAdManager;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSActivity;
import com.ihs.charging.HSChargingManager;
import com.ihs.charging.HSChargingManager.HSChargingState;
import com.ihs.chargingscreen.Constants;
import com.ihs.chargingscreen.HSChargingScreenManager;
import com.ihs.chargingscreen.notification.ChargeNotifyManager;
import com.ihs.chargingscreen.ui.BubbleView;
import com.ihs.chargingscreen.utils.ChargingGARecorder;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.chargingscreen.utils.ChargingPrefsUtil;
import com.ihs.chargingscreen.utils.CommonUtils;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.ihs.chargingscreen.HSChargingScreenManager.getChargingState;

/**
 * Created by zhixiangxiao on 5/4/16.
 */
public class ChargingScreenActivity extends HSActivity {

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

    private FrameLayout adContainer;
    private FrameLayout adView;
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


    private AcbNativeAd nativeAd;
    private AcbNativeAdLoader nativeAdLoader;

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
    private TextView[] txtChargingStateList;
    private BubbleView bubbleView;
    private PowerManager powerManager = (PowerManager) HSApplication.getContext().getSystemService(Context.POWER_SERVICE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);

        ChargingGARecorder.getInstance().chargingScreenShowed();

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

        boolean keyguardFlag;
        if (VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN) {
            keyguardFlag = false;
        } else {
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager == null) {
                keyguardFlag = false;
            } else {
                keyguardFlag = keyguardManager.isKeyguardSecure();
                HSLog.i("isKeyguardSecure: " + keyguardManager.isKeyguardSecure()
                        + " isKeyguardLocked: " + keyguardManager.isKeyguardLocked());
            }
        }

        Window window = getWindow();
        if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
            window.addFlags(LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        window.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        if (!keyguardFlag) {
            window.addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);
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
                if (CommonUtils.isFastDoubleClick()) {
                    return;
                }
                showPopupWindow(ChargingScreenActivity.this, imgSetting);
                HSAnalytics.logEvent("HSLib_chargingscreen_settings_clicked");

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
        txtChargingStateList = new TextView[]{
                (TextView) findViewById(R.id.tv_regular),
                (TextView) findViewById(R.id.tv_continuous),
                (TextView) findViewById(R.id.tv_trickle),
        };

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

    }


    private void removeAdView() {
        if (adView != null) {
            adContainer.removeView(adView);
            adView = null;
        }
        if (nativeAd != null) {
            nativeAd.release();
            nativeAd = null;
        }

        if (nativeAdLoader != null) {
            nativeAdLoader.cancel();
            nativeAdLoader = null;
        }

        final HSChargingState chargingState = HSChargingManager.getInstance().getChargingState();
        if (chargingState == HSChargingState.STATE_CHARGING_SPEED || chargingState == HSChargingState.STATE_CHARGING_CONTINUOUS
                || chargingState == HSChargingState.STATE_CHARGING_TRICKLE) {
            txtChargingIndicator.setVisibility(View.VISIBLE);
        } else {
            txtChargingIndicator.setVisibility(View.INVISIBLE);
        }
    }

    private void showAd(AcbNativeAd nativeAd) {
        if (adView != null) {
            return;
        }

        if (!HSChargingScreenManager.getInstance().isShowNativeAd()) {
            return;
        }

        ChargingGARecorder.getInstance().nativeAdShow();
        adView = getAdView(this, nativeAd);
        adContainer.addView(adView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        txtChargingIndicator.setVisibility(View.INVISIBLE);
        nativeAd.setNativeClickListener(new AcbNativeAd.AcbNativeClickListener() {
            @Override
            public void onAdClick(AcbAd acbAd) {
                HSAnalytics.logEvent("HSLib_chargingscreen_Charge_Ad_Clicked");
                ChargingGARecorder.getInstance().nativeAdClick();
                finish();
            }
        });
        HSAnalytics.logEvent("HSLib_chargingscreen_Charge_Ad_Viewed");
    }

    @Override
    public void onStart() {
        super.onStart();

        HSLog.d("chargingtest onStart");

        startDisplayTime = System.currentTimeMillis();
        ChargeNotifyManager.getInstance().setIsChargingActivityAlive(true);
        AcbNativeAdManager.startManualPreLoad(HSChargingScreenManager.getInstance().getNaitveAdsPlacementName(), this);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager.isScreenOn()) {
            if (this.nativeAdLoader != null) {
                this.nativeAdLoader.cancel();
                this.nativeAdLoader = null;
            }
            this.nativeAdLoader = new AcbNativeAdLoader(this, HSChargingScreenManager.getInstance().getNaitveAdsPlacementName());
            ChargingGARecorder.getInstance().nativeAdLoad();
            this.nativeAdLoader.load(1, new AcbNativeAdLoader.AcbNativeAdLoadListener() {
                @Override
                public void onAdReceived(AcbNativeAdLoader loader, List<AcbNativeAd> list) {
                    if (ChargingScreenActivity.this.nativeAd == null) {
                        AcbNativeAd nativeAd = (list != null && list.size() > 0) ? list.get(0) : null;
                        if (nativeAd != null) {
                            ChargingScreenActivity.this.nativeAd = nativeAd;
                            ChargingScreenActivity.this.showAd(ChargingScreenActivity.this.nativeAd);
                        }
                    }
                }

                @Override
                public void onAdFinished(AcbNativeAdLoader loader, HSError hsError) {
                    ChargingScreenActivity.this.nativeAdLoader = null;
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getChargingState() > 0) {
            bubbleView.start();
        }

        HSLog.d("chargingtest onResume");

    }

    @Override
    protected void onStop() {
        super.onStop();
        HSLog.d("chargingtest onStop");

        bubbleView.stop();

        AcbNativeAdManager.stopManualPreLoad(HSChargingScreenManager.getInstance().getNaitveAdsPlacementName(), this);

        removeAdView();
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

        long endDisplayTime = System.currentTimeMillis();

        if (endDisplayTime - startDisplayTime >= 1000) {
            HSAnalytics.logEvent("HSLib_chargingscreen_Charge_FullPage_Viewed", "Stay_Length", (endDisplayTime - startDisplayTime) / 1000 + "s");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        ChargingPrefsUtil.getInstance().setChargingForFirstSession();

        HSChargingManager.getInstance().removeChargingListener(chargingListener);

        cancelAllAnimators();

        if (telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        handler.removeCallbacksAndMessages(null);

        if (this.nativeAdLoader != null) {
            this.nativeAdLoader.cancel();
            this.nativeAdLoader = null;
        }

        if (this.nativeAd != null) {
            this.nativeAd.release();
            this.nativeAd = null;
        }

        if (null != closeDialog) {
            closeDialog.dismiss();
        }

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

    private FrameLayout getAdView(Context context, final AcbNativeAd nativeAd) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.charging_module_ad_card, null);

        final AcbNativeAdContainerView containerView = new AcbNativeAdContainerView(context);
        containerView.addContentView(contentView);

        containerView.setAdTitleView((TextView) contentView.findViewById(R.id.ad_title));
        containerView.setAdSubTitleView((TextView) contentView.findViewById(R.id.ad_subtitle));
        containerView.setAdActionView(contentView.findViewById(R.id.ad_call_to_action));
        containerView.setAdIconView((AcbNativeAdIconView) contentView.findViewById(R.id.ad_icon));
        containerView.setAdPrimaryView((AcbNativeAdPrimaryView) contentView.findViewById(R.id.ad_cover_img));
        containerView.setAdChoiceView((ViewGroup) contentView.findViewById(R.id.ad_conner));

        containerView.fillNativeAd(nativeAd);

        ((TextView) containerView.getAdTitleView()).setText(nativeAd.getTitle().replace("&nbsp;", ""));
        if (!TextUtils.isEmpty(nativeAd.getSubtitle()) && !TextUtils.isEmpty(nativeAd.getSubtitle().trim())) {
            ((TextView) containerView.getAdSubTitleView()).setText(String.format("(%s)", nativeAd.getSubtitle().trim()));
        } else {
            containerView.getAdSubTitleView().setVisibility(View.GONE);
        }


        final int tempWidth = DisplayUtils.getDisplayMetrics().widthPixels - DisplayUtils.dip2px(50);
        String imageFilePath = nativeAd.getResourceFilePath(AcbNativeAd.LOAD_RESOURCE_TYPE_IMAGE);
        if (!TextUtils.isEmpty(imageFilePath) && new File(imageFilePath).exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imageFilePath, options);
            if (options.outWidth > options.outHeight) {
                HSLog.i("onSizeReady:" + options.outWidth + "x" + options.outHeight);
                containerView.getAdPrimaryView().getLayoutParams().width = tempWidth;
                containerView.getAdPrimaryView().getLayoutParams().height = options.outHeight * tempWidth / options.outWidth;
            } else {
                HSLog.i("onSizeReady:" + options.outWidth + "x" + options.outHeight);
                containerView.getAdPrimaryView().getLayoutParams().height = tempWidth / 2;
                containerView.getAdPrimaryView().getLayoutParams().width = tempWidth;
            }
        } else {
            containerView.getAdPrimaryView().getLayoutParams().height = tempWidth / 2;
            containerView.getAdPrimaryView().getLayoutParams().width = tempWidth;

        }

        return containerView;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

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

        return true;
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
                    if (CommonUtils.isFastDoubleClick()) {
                        return;
                    }

                    if (popupWindow != null) {
                        popupWindow.dismiss();
                    }

                    showAlert();
                    ChargingGARecorder.getInstance().chargingDisableTouchedOnce();
                    HSAnalytics.logEvent("HSLib_chargingscreen_Charge_TurnOff_Clicked");
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
            closeAlertTitle.setText(getResources().getString(R.string.charging_module_close_charging_boost) + moduleName + "?");

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

                    ChargingGARecorder.getInstance().chargingDisableConfirmedOnce();


                    HSChargingScreenManager.getInstance().stop(true);
                    ChargingPrefsUtil.getInstance().setChargingEnableByUser(false);

                    finish();

                    HSAnalytics.logEvent("HSLib_chargingscreen_Charge_Alert_Disable_Clicked");
                }
            });
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

        cancelAnimator(rootViewTransXAnimator);

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

    private void startFlashAnimation(int imgChargingStateGreenDrawableCount) {

        if (!isImgChargingStateFlash()) {

            return;
        }
        if (imgChargingStateGreenDrawableCount <= 0) {
            return;
        }

        final int ANIMATION_DURATION = 700;
        final int ANIMATION_START_DELAY = 150;

        ValueAnimator imgChargingStateDisAppearAnimator = ObjectAnimator.ofInt(imgChargingStateList[imgChargingStateGreenDrawableCount - 1],
                "alpha", 255, 160);
        imgChargingStateDisAppearAnimator.setDuration(ANIMATION_DURATION);
        imgChargingStateDisAppearAnimator.setStartDelay(ANIMATION_START_DELAY);

        ValueAnimator imgChargingStateAppearAnimator = ObjectAnimator.ofInt(imgChargingStateList[imgChargingStateGreenDrawableCount - 1],
                "alpha", 160, 255);
        imgChargingStateAppearAnimator.setDuration(500);
        imgChargingStateAppearAnimator.setStartDelay(ANIMATION_START_DELAY);

    }

    private void updateInfo() {
        txtBatteryLevelPercent.setText(new StringBuilder(String.valueOf((HSChargingManager.getInstance().getBatteryRemainingPercent())))
                .toString());

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

            imgChargingState.clearAnimation();
            imgChargingState.setAlpha(255);

            if (i < imgChargingStateGreenDrawableCount) {
                imgChargingState.setImageDrawable(imgChargingStateGreenDrawables.get(i));
            } else {
                imgChargingState.setImageDrawable(imgChargingStateDarkDrawables.get(i));
            }

            if (i < imgChargingStateGreenDrawableCount) {
                txtChargingStateList[i].setTextColor(Color.WHITE);
            } else {
                txtChargingStateList[i].setTextColor(getResources().getColor(R.color.charging_grey));
            }

        }

        if (txtChargingStateStringIndex == 4) {
            txtChargingStateList[0].setTextColor(getResources().getColor(R.color.charging_grey));
            imgChargingStateList[0].setImageDrawable(imgChargingStateDarkDrawables.get(0));
        }

        startFlashAnimation(imgChargingStateGreenDrawableCount);
    }


}
