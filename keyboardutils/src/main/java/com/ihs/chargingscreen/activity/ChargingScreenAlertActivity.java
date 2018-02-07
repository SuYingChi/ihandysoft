package com.ihs.chargingscreen.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IntRange;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.artw.lockscreen.LockerUtils;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.charging.HSChargingManager;
import com.ihs.charging.HSChargingManager.HSChargingState;
import com.ihs.chargingscreen.HSChargingScreenManager;
import com.ihs.chargingscreen.notification.ChargeNotifyManager;
import com.ihs.chargingscreen.ui.BubbleView;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.ihs.keyboardutils.nativeads.KCNativeAdView;
import com.ihs.keyboardutils.utils.RippleDrawableUtils;
import com.kc.commons.utils.KCCommonUtils;
import com.kc.utils.KCAnalytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
import static com.ihs.chargingscreen.HSChargingScreenManager.getChargingState;
import static com.ihs.keyboardutils.iap.RemoveAdsManager.NOTIFICATION_REMOVEADS_PURCHASED;

/**
 * Created by zhixiangxiao on 5/4/16.
 */
public class ChargingScreenAlertActivity extends Activity {

    private static final int EVENT_START_SCROLL_UP_ANIMATOR = 101;

    private TextView txtBatteryLevelPercent;

    private TextView txtLeftTime;
    private TextView txtLeftTimeIndicator;
    private TextView txtChargingIndicator;

    private ImageView[] imgChargingStateList;

    private String[] txtLeftTimeIndicatorStrings;
    private String[] txtChargingIndicatorStrings;

    private List<Drawable> imgChargingStateGreenDrawables = new ArrayList<>();
    private List<Drawable> imgChargingStateDarkDrawables = new ArrayList<>();

    private TelephonyManager telephonyManager;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case EVENT_START_SCROLL_UP_ANIMATOR:
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
                    ChargingScreenAlertActivity.this.finish();
                    return;

                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;

                default:
                    break;
            }
        }
    };

    private BubbleView bubbleView;
    private RelativeLayout adContainer;
    private ImageView removeAds;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), Intent.ACTION_SCREEN_OFF)) {
                finish();
            }
        }
    };
    private KCNativeAdView nativeAdView;
    private Dialog closeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        KCAnalytics.logEvent("chargeAlert_show");
        KCAnalytics.logEvent("Cable_Report_Show");

        HSChargingScreenManager.getInstance().start();

        Window window = getWindow();

        boolean keyguardSecure = LockerUtils.isKeyguardSecure(this);

        if (!keyguardSecure) {
            window.addFlags(FLAG_DISMISS_KEYGUARD);
        }

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

        setContentView(R.layout.charging_module_alert_activity);

        View contentView = findViewById(R.id.content_layout);


        findViewById(R.id.view_spac1).setBackgroundDrawable(VectorDrawableCompat.create(getResources(), R.drawable.shape_wihte_dot, null));
        findViewById(R.id.view_spac2).setBackgroundDrawable(VectorDrawableCompat.create(getResources(), R.drawable.shape_wihte_dot, null));

        bubbleView = ((BubbleView) findViewById(R.id.bubbleView));
        bubbleView.post(new Runnable() {
            @Override
            public void run() {
                if (contentView.getWidth() > 0 && contentView.getHeight() > 0) {
                    bubbleView.setLayoutParams(new RelativeLayout.LayoutParams(contentView.getWidth() - contentView.getPaddingLeft() - contentView.getPaddingRight(), contentView.getHeight() - contentView.getPaddingTop() - contentView.getPaddingBottom()));
                }
            }
        });

        ImageView closeBtn = findViewById(R.id.close_btn);
        closeBtn.setBackgroundDrawable(RippleDrawableUtils.getTransparentRippleBackground());
        closeBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
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

        adContainer = (RelativeLayout) findViewById(R.id.ad_container);
        txtBatteryLevelPercent = (TextView) findViewById(R.id.txt_battery_level);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/dincond_medium.otf");
        txtBatteryLevelPercent.setTypeface(tf);

        imgChargingStateList = new ImageView[]{
                (ImageView) findViewById(R.id.img_charging_state1),
                (ImageView) findViewById(R.id.img_charging_state2),
                (ImageView) findViewById(R.id.img_charging_state3),
        };


        handler.sendEmptyMessageDelayed(EVENT_START_SCROLL_UP_ANIMATOR, 3000);

        initStringListAndDrawableList();
        updateInfo();


        if (!HSConfig.optBoolean(true, "Application", "ChargeLocker", "ShowAppInfo")) {
            findViewById(R.id.ll_info).setVisibility(View.INVISIBLE);
        }

        if (!HSConfig.optBoolean(true, "Application", "ChargeLocker", "ShowSettingIcon")) {
            findViewById(R.id.img_setting).setVisibility(View.INVISIBLE);
        }

        if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            View view = LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_style_charging_alert, null);
            int width = (int) (DisplayUtils.getScreenWidthPixels() * 0.9);
            View loadingView = new View(HSApplication.getContext());
            LinearLayout.LayoutParams loadingLP = new LinearLayout.LayoutParams(width, (int) (width / 1.9f + HSApplication.getContext().getResources().getDimensionPixelOffset(R.dimen.ad_style_charging_alert_bottom_container_height)));
            loadingView.setLayoutParams(loadingLP);
            nativeAdView = new KCNativeAdView(HSApplication.getContext());
            nativeAdView.setLoadingView(loadingView);
            nativeAdView.setAdLayoutView(view);
            nativeAdView.setPrimaryViewSize(width, (int) (width / 1.9f));
            nativeAdView.load(HSChargingScreenManager.getInstance().getChargingAlertAdsPlacementName());
            adContainer.addView(nativeAdView);


            // 单次关闭广告或永久删除广告
            removeAds = (ImageView) findViewById(R.id.remove_ads);
            removeAds.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    showRemoveAdsDialog();
                }
            });
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(broadcastReceiver, filter);

        ImageView ivSetting = findViewById(R.id.iv_setting);
        ivSetting.setBackgroundDrawable(RippleDrawableUtils.getTransparentRippleBackground());
        ivSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlert();
            }
        });
    }

    private void showAlert() {
        if (closeDialog == null) {
            closeDialog = new Dialog(this, R.style.dialog);
            closeDialog.setContentView(R.layout.charging_module_alert_close_charge_screen);

            TextView closeAlertTitle = (TextView) closeDialog.findViewById(R.id.close_alert_title);
            closeAlertTitle.setText(R.string.disable_battery_master);

            View btnCancel = closeDialog.findViewById(R.id.alert_cancel);
            View btnClose = closeDialog.findViewById(R.id.alert_close);

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (closeDialog == null) {
                        return;
                    }
                    KCCommonUtils.dismissDialog(closeDialog);
                    closeDialog = null;
                }
            });

            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (closeDialog == null) {
                        return;
                    }
                    KCCommonUtils.dismissDialog(closeDialog);
                    closeDialog = null;

                    ChargingManagerUtil.disableCharging();
                    finish();
                }
            });
            btnCancel.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(Color.WHITE, 0, 0, 0, DisplayUtils.dip2px(8)));
            btnClose.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(Color.WHITE, 0, 0, DisplayUtils.dip2px(8), 0));

        }
        KCCommonUtils.showDialog(closeDialog);
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

        ChargeNotifyManager.getInstance().setIsChargingActivityAlive(true);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (getChargingState() > 0) {
            bubbleView.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!HSSessionMgr.isSessionStarted()) {
            HSAnalytics.stopFlurry();
        }
        bubbleView.stop();
        showChargingIndicatorText();
    }

    @Override
    public void onPause() {
        super.onPause();

        ChargeNotifyManager.getInstance().setIsChargingActivityAlive(false);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);

        if (telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
            telephonyManager = null;
        }
        HSChargingManager.getInstance().removeChargingListener(chargingListener);
        cancelAllAnimators();
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
        KCCommonUtils.fixInputMethodManagerLeak(this);
    }

    private void initStringListAndDrawableList() {

        Resources resources = getResources();

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

        imgChargingStateGreenDrawables.add(getCompatDrawable(R.drawable.ic_charging_speed));
        imgChargingStateGreenDrawables.add(getCompatDrawable(R.drawable.ic_charging_continue));
        imgChargingStateGreenDrawables.add(getCompatDrawable(R.drawable.ic_charging_trickle));

        imgChargingStateDarkDrawables.add(getCompatDrawable(R.drawable.ic_charging_speed_dark));
        imgChargingStateDarkDrawables.add(getCompatDrawable(R.drawable.ic_charging_continue_dark));
        imgChargingStateDarkDrawables.add(getCompatDrawable(R.drawable.ic_charging_trickle_dark));
    }

    private Drawable getCompatDrawable(int drawableRes) {
        return VectorDrawableCompat.create(getResources(), drawableRes, null);
    }

    private void cancelAllAnimators() {
        cancelAnimator(flashAnimatorSet);
        flashAnimatorSet = null;
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

        if (txtChargingStateStringIndex <= 4) {
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

    private void showRemoveAdsDialog() {
        final Dialog removeAdsDialog = new Dialog(this, R.style.dialog);
        removeAdsDialog.setContentView(R.layout.remove_ads_dialog);

        View btnJustOnce = removeAdsDialog.findViewById(R.id.btn_just_once);
        View btnForever = removeAdsDialog.findViewById(R.id.btn_forever);

        btnJustOnce.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                KCCommonUtils.dismissDialog(removeAdsDialog);
                adContainer.removeView(nativeAdView);
                removeAds.setVisibility(View.GONE);
            }
        });

        btnForever.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                KCCommonUtils.dismissDialog(removeAdsDialog);

                RemoveAdsManager.getInstance().purchaseRemoveAds();

                HSGlobalNotificationCenter.addObserver(NOTIFICATION_REMOVEADS_PURCHASED, new INotificationObserver() {
                    @Override
                    public void onReceive(String s, HSBundle hsBundle) {
                        HSGlobalNotificationCenter.removeObserver(this);
                        if (removeAds != null) {
                            removeAds.setVisibility(View.GONE);
                        }
                        if (nativeAdView != null) {
                            adContainer.removeView(nativeAdView);
                            nativeAdView.release();
                            nativeAdView = null;
                        }
                    }
                });
            }
        });
        btnForever.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(Color.WHITE, 0, 0, 0, DisplayUtils.dip2px(8)));
        btnJustOnce.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(Color.WHITE, 0, 0, DisplayUtils.dip2px(8), 0));
        KCCommonUtils.showDialog(removeAdsDialog);
    }
}
