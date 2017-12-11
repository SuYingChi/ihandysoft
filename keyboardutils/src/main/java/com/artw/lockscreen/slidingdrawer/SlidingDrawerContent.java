package com.artw.lockscreen.slidingdrawer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.artw.lockscreen.LockerActivity;
import com.artw.lockscreen.PremiumLockerActivity;
import com.artw.lockscreen.common.ConfigConstants;
import com.artw.lockscreen.common.NavUtils;
import com.artw.lockscreen.common.SystemSettingsManager;
import com.artw.lockscreen.slidingdrawer.wallpaper.WallpaperBlurUtils;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.feature.common.ConcurrentUtils;
import com.ihs.feature.common.Utils;
import com.ihs.flashlight.FlashlightManager;
import com.ihs.keyboardutils.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.artw.lockscreen.common.SystemSettingsManager.SettingsItem.BRIGHTNESS;
import static com.artw.lockscreen.common.SystemSettingsManager.SettingsItem.RINGMODE;
import static com.artw.lockscreen.common.SystemSettingsManager.SettingsItem.WIFI;


public class SlidingDrawerContent extends FrameLayout
        implements View.OnClickListener,
        SystemSettingsManager.ISystemSettingsListener, SeekBar.OnSeekBarChangeListener,
        INotificationObserver {

    public final static String EVENT_SHOW_BLACK_HOLE = "EVENT_SHOW_BLACK_HOLE";
    public static final String EVENT_BLACK_HOLE_ANIMATION_END = "EVENT_BLACK_HOLE_ANIMATION_END";
    public static final String EVENT_REFRESH_BLUR_WALLPAPER = "EVENT_REFRESH_BLUR_WALLPAPER";
    public final static int DURATION_BALL_DISAPPEAR = 400;
    public final static int DURATION_BALL_APPEAR = 300;
    private static final int WALLPAPER_BLUR_RADIUS = 8;

    private static final int EVENT_SYSTEM_SETTING_WIFI = 100;
    private static final int EVENT_SYSTEM_SETTING_BLUETOOTH = 101;
    private static final int EVENT_SYSTEM_SETTING_SOUND_PROFILE = 102;
    private static final int EVENT_SYSTEM_SETTING_MOBILE_DATA = 103;
    private static final long SYSTEM_SETTING_CHECK_DELAY = 1500;
    private static final long SYSTEM_SETTING_CHECK_DELAY_LONG = 2000;

    List<String> mCalculatorApps;
    private ImageView flashlight;
    private ImageView wifiState;
    private ImageView bluetoothState;
    private ImageView soundProfileState;
    private ImageView mobileDataState;
    private SeekBar brightnessBar;

    private int[] bluetoothStateRes;
    private int[] mobileDataStateRes;
    private int[] soundStateRes;
    private int[] wifiStateRes;
    private int brightnessValue;
    private boolean isCameraUsageAccess = true;
    private boolean isDrawerBgInitial = false;
    private Timer mMobileDataPollingTimer;
    private int mobileDataLastState = 0;

    private ImageView ivDrawerBg;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_SYSTEM_SETTING_WIFI:
                    Map<String, String> params = new HashMap<>();
                    params.put("type", "fail");
                    HSAnalytics.logEvent("Locker_Toggle_Wifi_Clicked", params);
                    wifiState.setEnabled(true);
                    break;
                case EVENT_SYSTEM_SETTING_BLUETOOTH:
                    params = new HashMap<>();
                    params.put("type", "fail");
                    HSAnalytics.logEvent("Locker_Toggle_Bluetooth_Clicked", params);
                    bluetoothState.setEnabled(true);
                    break;
                case EVENT_SYSTEM_SETTING_MOBILE_DATA:
                    mobileDataState.setEnabled(true);
                    break;
                case EVENT_SYSTEM_SETTING_SOUND_PROFILE:
                    soundProfileState.setEnabled(true);
                default:
                    break;
            }
        }
    };

    private SystemSettingsManager mSystemSettingsManager;

    public SlidingDrawerContent(Context context) {
        this(context, null);
    }

    public SlidingDrawerContent(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingDrawerContent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mSystemSettingsManager = new SystemSettingsManager(getContext());
    }

    public void setDrawerBg(final Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        ConcurrentUtils.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                final Bitmap bluredBitmap = WallpaperBlurUtils.blurBitmap(getContext(), bitmap, WALLPAPER_BLUR_RADIUS);
                ConcurrentUtils.postOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        ObjectAnimator wallpaperOut = ObjectAnimator.ofFloat(ivDrawerBg, "alpha", 1f, 0.5f);
                        wallpaperOut.setDuration(400);
                        wallpaperOut.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                ivDrawerBg.setImageBitmap(bluredBitmap);
                            }
                        });

                        ObjectAnimator wallpaperIn = ObjectAnimator.ofFloat(ivDrawerBg, "alpha", 0.5f, 1f);
                        wallpaperIn.setDuration(400);

                        AnimatorSet change = new AnimatorSet();
                        change.playSequentially(wallpaperOut, wallpaperIn);
                        change.start();
                    }
                });
            }
        });
    }

    public void onScroll(float cur, float total) {
        if (ivDrawerBg != null) {
            ivDrawerBg.setTranslationY(-1 * cur / total * getMeasuredHeight());
        }
    }

    public void clearBlurredBackground() {
        if (ivDrawerBg != null) {
            ivDrawerBg.setImageResource(android.R.color.transparent);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                requestDisallowInterceptTouchEvent(false);
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ivDrawerBg = (ImageView) findViewById(R.id.iv_slide_bg);
        flashlight = (ImageView) findViewById(R.id.flashlight);
        ImageView calculator = (ImageView) findViewById(R.id.calculator);
        wifiState = (ImageView) findViewById(R.id.wifi);
        bluetoothState = (ImageView) findViewById(R.id.bluetooth);
        soundProfileState = (ImageView) findViewById(R.id.sound_profile);
        mobileDataState = (ImageView) findViewById(R.id.data);
        brightnessBar = (SeekBar) findViewById(R.id.brightness_seekbar);

        flashlight.setOnClickListener(this);
        flashlight.setImageResource(FlashlightManager.getInstance().isOn() ?
                R.drawable.locker_flashlight_on : R.drawable.locker_flashlight_off);
        calculator.setOnClickListener(this);
        wifiState.setOnClickListener(this);
        bluetoothState.setOnClickListener(this);
        soundProfileState.setOnClickListener(this);
        mobileDataState.setOnClickListener(this);
        brightnessBar.setProgress(mSystemSettingsManager.getSystemSettingsItemState(BRIGHTNESS));
        brightnessBar.setOnSeekBarChangeListener(this);

        createToggleStateRes();

        try {
            mCalculatorApps = (List<String>) HSConfig.getList("Application", "Locker",ConfigConstants.APP_LIST_NAME_CALCULATOR);
        }catch (Exception e){

        }
        if (mCalculatorApps == null) mCalculatorApps = new ArrayList<>();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        HSGlobalNotificationCenter.addObserver(EVENT_BLACK_HOLE_ANIMATION_END, this);
        HSGlobalNotificationCenter.addObserver(EVENT_REFRESH_BLUR_WALLPAPER, this);

        mSystemSettingsManager.register(this);

        updateSystemToggles();

        postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshDrawerBg();
            }
        }, 300);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        HSGlobalNotificationCenter.removeObserver(this);
        mSystemSettingsManager.unRegister();

        if (mMobileDataPollingTimer != null) {
            mMobileDataPollingTimer.cancel();
        }
        mMobileDataPollingTimer = null;

        if (isCameraUsageAccess && !FlashlightManager.getInstance().isOn()) {
            FlashlightManager.getInstance().release();
        }
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case EVENT_REFRESH_BLUR_WALLPAPER:
                refreshDrawerBg();
                break;
            default:
                break;
        }
    }

    private void refreshDrawerBg() {
        if (isDrawerBgInitial) return;
        isDrawerBgInitial = true;
        Drawable currentWallpaper;
        if (getContext() instanceof LockerActivity) {
            currentWallpaper = ((LockerActivity) getContext()).getIvLockerWallpaper().getDrawable();
        } else if (getContext() instanceof PremiumLockerActivity) {
            currentWallpaper = ((PremiumLockerActivity) getContext()).getIvLockerWallpaper().getDrawable();
        } else {
            currentWallpaper = getContext().getResources().getDrawable(R.drawable.wallpaper_locker);
        }
        if (currentWallpaper != null && currentWallpaper instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) currentWallpaper).getBitmap();
            if (bitmap != null) {
                setDrawerBg(bitmap);
            } else {
                setDrawerBg(BitmapFactory.decodeResource(getResources(), R.drawable.wallpaper_locker));
            }
        } else {
            setDrawerBg(BitmapFactory.decodeResource(getResources(), R.drawable.wallpaper_locker));
        }
    }

    @Override
    public void onClick(View v) {
        Message msg = Message.obtain();
        int i = v.getId();
        if (i == R.id.wifi) {
            msg.arg1 = mSystemSettingsManager.getSystemSettingsItemState(WIFI);
            msg.what = EVENT_SYSTEM_SETTING_WIFI;
            handler.removeMessages(EVENT_SYSTEM_SETTING_WIFI);
            handler.sendMessageDelayed(msg, SYSTEM_SETTING_CHECK_DELAY);
            mSystemSettingsManager.toggleWifi();
            wifiState.setEnabled(false);

        } else if (i == R.id.bluetooth) {
            msg.arg1 = mSystemSettingsManager.getSystemSettingsItemState(SystemSettingsManager.SettingsItem.BLUETOOTH);
            msg.what = EVENT_SYSTEM_SETTING_BLUETOOTH;
            handler.removeMessages(EVENT_SYSTEM_SETTING_BLUETOOTH);
            handler.sendMessageDelayed(msg, SYSTEM_SETTING_CHECK_DELAY);
            mSystemSettingsManager.toggleBluetooth();
            bluetoothState.setEnabled(false);

        } else if (i == R.id.sound_profile) {
            msg.arg1 = mSystemSettingsManager.getSystemSettingsItemState(RINGMODE);
            msg.what = EVENT_SYSTEM_SETTING_SOUND_PROFILE;
            handler.removeMessages(EVENT_SYSTEM_SETTING_SOUND_PROFILE);
            handler.sendMessageDelayed(msg, SYSTEM_SETTING_CHECK_DELAY);
            try {
                mSystemSettingsManager.toggleRingMode();
            } catch (SecurityException e) {
            }

        } else if (i == R.id.data) {
            msg.arg1 = mSystemSettingsManager.getSystemSettingsItemState(SystemSettingsManager.SettingsItem.MOBILE_DATA);
            msg.what = EVENT_SYSTEM_SETTING_MOBILE_DATA;
            handler.removeMessages(EVENT_SYSTEM_SETTING_MOBILE_DATA);
            handler.sendMessageDelayed(msg, SYSTEM_SETTING_CHECK_DELAY_LONG); // 比较耗时
            if (!Utils.setMobileDataStatus(getContext(),
                    mSystemSettingsManager.getSystemSettingsItemState(SystemSettingsManager.SettingsItem.MOBILE_DATA) != 1)) {
                NavUtils.startSystemDataUsageSetting(getContext());
                HSGlobalNotificationCenter.sendNotification(LockerActivity.EVENT_FINISH_SELF);
            } else {
                startMobileDataChecker();
            }

        } else if (i == R.id.flashlight) {
            if (FlashlightManager.getInstance().isOn()) {
                FlashlightManager.getInstance().turnOff();
                FlashlightManager.getInstance().release();
                HSAnalytics.logEvent("Locker_Toggle_Flashlight_Clicked", "type", "off");
            } else {
                try {
                    FlashlightManager.getInstance().init();
                    FlashlightManager.getInstance().turnOn();
                } catch (Exception e) {
                    isCameraUsageAccess = false;
                }
                HSAnalytics.logEvent("Locker_Toggle_Flashlight_Clicked", "type", "on");
            }
            flashlight.setImageResource(FlashlightManager.getInstance().isOn() ?
                    R.drawable.locker_flashlight_on : R.drawable.locker_flashlight_off);

        } else if (i == R.id.calculator) {
            if (startCalculator()) {
                HSGlobalNotificationCenter.sendNotification(LockerActivity.EVENT_FINISH_SELF);
                HSAnalytics.logEvent("Locker_Toggle_Calculator_Clicked");
            }
        } else {
        }
    }

    private void startMobileDataChecker() {
        if (mMobileDataPollingTimer != null)
            return;
        mMobileDataPollingTimer = new Timer();
        mMobileDataPollingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        int currentState = mSystemSettingsManager.getSystemSettingsItemState(SystemSettingsManager.SettingsItem.MOBILE_DATA);
                        if (mobileDataLastState != currentState) {
                            Map<String, String> params = new HashMap<>();
                            params.put("type", "success");
                            if (currentState == 1) {
                                params.put("state", "on");
                                HSAnalytics.logEvent("Locker_Toggle_MobileData_Clicked", params);
                            } else {
                                params.put("state", "off");
                                HSAnalytics.logEvent("Locker_Toggle_MobileData_Clicked", params);
                            }
                            mobileDataLastState = currentState;
                        }
                        mobileDataState.setImageResource(mobileDataStateRes[mSystemSettingsManager.getSystemSettingsItemState(SystemSettingsManager.SettingsItem.MOBILE_DATA)]);
                    }
                });
            }
        }, 0, 1000);
    }

    @Override
    public void onSystemSettingsStateChanged(SystemSettingsManager.SettingsItem toggle, int state) {
        HSLog.i("MainFrame", "onSystemSettingStateChanged(), toggle = " + toggle + ", state = " + state);
        switch (toggle) {
            case BLUETOOTH:
                if (handler.hasMessages(EVENT_SYSTEM_SETTING_BLUETOOTH)) {
                    Map<String, String> params = new HashMap<>();
                    params.put("type", "success");
                    if (state == 1) {
                        params.put("state", "on");
                        HSAnalytics.logEvent("Locker_Toggle_Bluetooth_Clicked", params);
                        handler.removeMessages(EVENT_SYSTEM_SETTING_BLUETOOTH);
                    } else if (state == 0) {
                        params.put("state", "off");
                        HSAnalytics.logEvent("Locker_Toggle_Bluetooth_Clicked", params);
                        handler.removeMessages(EVENT_SYSTEM_SETTING_BLUETOOTH);
                    }
                }
                bluetoothState.setImageResource(bluetoothStateRes[state]);
                bluetoothState.setEnabled(true);
                break;
            case WIFI:
                if (handler.hasMessages(EVENT_SYSTEM_SETTING_WIFI)) {
                    Map<String, String> params = new HashMap<>();
                    params.put("type", "success");
                    if (state == 1) {
                        params.put("state", "on");
                        HSAnalytics.logEvent("Locker_Toggle_Wifi_Clicked", params);
                        handler.removeMessages(EVENT_SYSTEM_SETTING_WIFI);
                    } else if (state == 0) {
                        params.put("state", "off");
                        HSAnalytics.logEvent("Locker_Toggle_Wifi_Clicked", params);
                        handler.removeMessages(EVENT_SYSTEM_SETTING_WIFI);
                    }
                }
                wifiState.setImageResource(wifiStateRes[state]);
                wifiState.setEnabled(true);
                break;
            case BRIGHTNESS:
                int bright = mSystemSettingsManager.getSystemSettingsItemState(BRIGHTNESS);
                if (bright != brightnessValue) {
                    brightnessBar.setProgress(bright);
                }
                break;
            case RINGMODE:
                if (handler.hasMessages(EVENT_SYSTEM_SETTING_SOUND_PROFILE)) {
                    if (state == 2) {
                        HSAnalytics.logEvent("Locker_Toggle_Sound_Clicked", "type", "sound");
                    } else if (state == 1) {
                        HSAnalytics.logEvent("Locker_Toggle_Sound_Clicked", "type", "vibrate");
                    } else {
                        HSAnalytics.logEvent("Locker_Toggle_Sound_Clicked", "type", "silence");
                    }
                    handler.removeMessages(EVENT_SYSTEM_SETTING_SOUND_PROFILE);
                }
                soundProfileState.setImageResource(soundStateRes[state]);
                soundProfileState.setEnabled(true);
                break;
            default:
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mSystemSettingsManager.setSystemSettingsItemState(BRIGHTNESS, progress);
        }
        brightnessValue = progress;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        brightnessValue = seekBar.getProgress();
        HSAnalytics.logEvent("Locker_Toggle_Brightness_Draged");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }


    private void createToggleStateRes() {
        TypedArray iconResArray = getContext().getResources().obtainTypedArray(R.array.bluetooth_state_drawable);
        bluetoothStateRes = new int[iconResArray.length()];
        for (int j = 0; j < iconResArray.length(); ++j) {
            bluetoothStateRes[j] = iconResArray.getResourceId(j, 0);
        }
        iconResArray.recycle();

        iconResArray = getContext().getResources().obtainTypedArray(R.array.mobile_data_state_drawable);
        mobileDataStateRes = new int[iconResArray.length()];
        for (int j = 0; j < iconResArray.length(); ++j) {
            mobileDataStateRes[j] = iconResArray.getResourceId(j, 0);
        }
        iconResArray.recycle();

        iconResArray = getContext().getResources().obtainTypedArray(R.array.sound_state_drawable);
        soundStateRes = new int[iconResArray.length()];
        for (int j = 0; j < iconResArray.length(); ++j) {
            soundStateRes[j] = iconResArray.getResourceId(j, 0);
        }
        iconResArray.recycle();

        iconResArray = getContext().getResources().obtainTypedArray(R.array.wifi_state_drawable);
        wifiStateRes = new int[iconResArray.length()];
        for (int j = 0; j < iconResArray.length(); ++j) {
            wifiStateRes[j] = iconResArray.getResourceId(j, 0);
        }
        iconResArray.recycle();
    }

    private void updateSystemToggles() {
        wifiState.setImageResource(wifiStateRes[mSystemSettingsManager.getSystemSettingsItemState(WIFI)]);
        bluetoothState.setImageResource(bluetoothStateRes[mSystemSettingsManager.getSystemSettingsItemState(SystemSettingsManager.SettingsItem.BLUETOOTH)]);
        soundProfileState.setImageResource(soundStateRes[mSystemSettingsManager.getSystemSettingsItemState(RINGMODE)]);
        mobileDataState.setImageResource(mobileDataStateRes[mSystemSettingsManager.getSystemSettingsItemState(SystemSettingsManager.SettingsItem.MOBILE_DATA)]);
        mobileDataLastState = mSystemSettingsManager.getSystemSettingsItemState(SystemSettingsManager.SettingsItem.MOBILE_DATA);
    }

    private boolean startCalculator() {
        boolean result = true;
        try {
            openApp(mCalculatorApps);
        } catch (ActivityNotFoundException exception) {
            result = false;
        } catch (SecurityException e) {
            result = false;
        }
        return result;
    }

    private void openApp(List<String> candidateApps) {
        PackageManager packageManager = HSApplication.getContext().getPackageManager();
        // Code snippet from http://stackoverflow.com/questions/3590955/intent-to-launch-the-clock-application-on-android
        Intent openIntent = null;

        boolean foundImpl = false;

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < candidateApps.size(); i++) {
            String vendor = "Vendor " + i;
            String implName = candidateApps.get(i);
            String packageName;
            String className = "";

            if (implName.contains("/")) {
                packageName = implName.substring(0, implName.indexOf("/"));
                className = implName.substring(implName.indexOf("/") + 1);
                if (className.startsWith(".")) {
                    className = packageName + className;
                }
            } else {
                packageName = implName;
            }

            try {
                if (className.isEmpty()) {
                    packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
                    openIntent = packageManager.getLaunchIntentForPackage(packageName);
                } else {
                    ComponentName cn = new ComponentName(packageName, className);
                    packageManager.getActivityInfo(cn, PackageManager.GET_META_DATA); // Throws when not found
                    openIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
                    openIntent.setComponent(cn);
                }
                foundImpl = true;
                break;
            } catch (PackageManager.NameNotFoundException e) {
            }
        }

        if (foundImpl && openIntent != null) {
            getContext().startActivity(openIntent);
        }
    }
}
