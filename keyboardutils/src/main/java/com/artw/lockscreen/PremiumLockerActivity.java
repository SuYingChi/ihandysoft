package com.artw.lockscreen;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.artw.lockscreen.common.LockerChargingScreenUtils;
import com.artw.lockscreen.slidingdrawer.SlidingDrawerContent;
import com.artw.lockscreen.statusbar.StatusBar;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.chargingscreen.activity.ChargingScreenActivity;
import com.ihs.chargingscreen.utils.ChargingAnalytics;
import com.ihs.chargingscreen.utils.ClickUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.feature.common.ScreenStatusReceiver;
import com.ihs.feature.common.Thunk;
import com.ihs.feature.weather.WeatherManager;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.CommonUtils;
import com.kc.commons.utils.KCCommonUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import static android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;

public class PremiumLockerActivity extends AppCompatActivity implements INotificationObserver {
    public static final String EVENT_FINISH_SELF = "locker_event_finish_self";
    public static final String PREF_KEY_CURRENT_WALLPAPER_HD_URL = "current_hd_wallpaper_url";
    private long startDisplayTime;

    @Thunk
    ViewPager mViewPager;
    private PremiumLockerAdapter mLockerAdapter;
    private ImageView mLockerWallpaper;
    public static DisplayImageOptions lockerBgOption = new DisplayImageOptions.Builder()
            .cacheOnDisk(true)
            .considerExifParams(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        LockerSettings.recordLockerEnableOnce();

        // set translucent status bar & navigation bar
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(FLAG_TRANSLUCENT_STATUS);
            window.addFlags(FLAG_TRANSLUCENT_NAVIGATION);
        }
        if (!LockerChargingScreenUtils.isNativeLollipop()) {
            window.addFlags(FLAG_FULLSCREEN);
        }
        window.addFlags(FLAG_SHOW_WHEN_LOCKED);
        window.setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (!LockerUtils.isKeyguardSecure(this)) {
            window.addFlags(FLAG_DISMISS_KEYGUARD);
        }

        setContentView(R.layout.activity_locker);

        mLockerWallpaper = (ImageView) findViewById(R.id.locker_wallpaper_view);

        initLockerWallpaper();
        configLockViewPager();

        final ViewGroup container = (ViewGroup) findViewById(R.id.activity_locker);
        StatusBar statusBar = (StatusBar) getLayoutInflater().inflate(R.layout.locker_status_bar, container, false);
        container.addView(statusBar);
        if (LockerChargingScreenUtils.isNativeLollipop()) {
            statusBar.setVisibility(View.GONE);
        }

        HSGlobalNotificationCenter.addObserver(EVENT_FINISH_SELF, this);
        LockerSettings.increaseLockerShowCount();
        HSGlobalNotificationCenter.addObserver(ScreenStatusReceiver.NOTIFICATION_SCREEN_ON, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WeatherManager.getInstance().requestWeather();
        long current = System.currentTimeMillis();
        if (current - startDisplayTime > 1000) {
            startDisplayTime = current;
        } else {
            startDisplayTime = -1;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (startDisplayTime != -1) {
            ChargingScreenActivity.logDisplayTime("app_screenLocker_displaytime", startDisplayTime);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        HSAnalytics.startFlurry();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!HSSessionMgr.isSessionStarted()) {
            HSAnalytics.stopFlurry();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HSGlobalNotificationCenter.removeObserver(this);

        KCCommonUtils.fixInputMethodManagerLeak(this);
    }


    private void initLockerWallpaper() {
        String wallpaperUrl = LockerSettings.getPref().getString(PREF_KEY_CURRENT_WALLPAPER_HD_URL, "");
        if (!TextUtils.isEmpty(wallpaperUrl)) {
            ImageLoader.getInstance().loadImage(wallpaperUrl, lockerBgOption, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {

                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {
                    mLockerWallpaper.setImageResource(R.drawable.wallpaper_locker);
                    HSGlobalNotificationCenter.sendNotification(SlidingDrawerContent.EVENT_REFRESH_BLUR_WALLPAPER);
                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    mLockerWallpaper.setImageBitmap(bitmap);
                    HSGlobalNotificationCenter.sendNotification(SlidingDrawerContent.EVENT_REFRESH_BLUR_WALLPAPER);
                }

                @Override
                public void onLoadingCancelled(String s, View view) {
                    mLockerWallpaper.setImageResource(R.drawable.wallpaper_locker);
                    HSGlobalNotificationCenter.sendNotification(SlidingDrawerContent.EVENT_REFRESH_BLUR_WALLPAPER);
                }
            });
        } else {
            mLockerWallpaper.setImageResource(R.drawable.wallpaper_locker);
        }
    }

    private void configLockViewPager() {
        WeatherManager.init(this);
        mViewPager = (ViewPager) findViewById(R.id.locker_pager);
        mLockerAdapter = new PremiumLockerAdapter(this);
        mViewPager.setAdapter(mLockerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setPadding(0, 0, 0, CommonUtils.getNavigationBarHeightUnconcerned(this));
        mViewPager.requestFocus();
        mViewPager.setCurrentItem(LockerAdapter.PAGE_INDEX_MAINFRAME);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (LockerAdapter.PAGE_INDEX_UNLOCK == position) {
                    finishSelf();
                    HSAnalytics.logEvent("Locker_Unlocked");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void startActivity(Intent intent) {
        // 从锁屏界面启动其它Activity时，应该先解锁再显示相应的界面

        super.startActivity(intent);
    }

    public void finishSelf() {
        findViewById(R.id.bottom_layer).setVisibility(View.GONE);
        ObjectAnimator fadeOutAnim = ObjectAnimator.ofFloat(mLockerWallpaper, View.ALPHA, 0f);
        fadeOutAnim.setDuration(300);
        fadeOutAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                DismissKeyguradActivity.startSelfIfKeyguardSecure(getApplicationContext());
                finish();
                overridePendingTransition(0, 0);

                mLockerWallpaper.setImageResource(android.R.color.transparent);
                if (mLockerAdapter != null && mLockerAdapter.lockerMainFrame != null) {
                    mLockerAdapter.lockerMainFrame.clearDrawerBackground();
                }
            }
        });
        fadeOutAnim.start();
    }

    @Override
    public void onBackPressed() {
        if (mLockerAdapter != null && mLockerAdapter.lockerMainFrame != null) {
            mLockerAdapter.lockerMainFrame.onBackPressed();
        }
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case EVENT_FINISH_SELF:
                finishSelf();
                break;
            case ScreenStatusReceiver.NOTIFICATION_SCREEN_ON:
                if (!ClickUtils.isFastDoubleClick()) {
                    ChargingAnalytics.logLockScreenShow();
                    ChargingAnalytics.logLockeScreenOrChargingScreenShow();
                }
                break;
            default:
                break;
        }
    }

    public ImageView getIvLockerWallpaper() {
        return mLockerWallpaper;
    }

}
