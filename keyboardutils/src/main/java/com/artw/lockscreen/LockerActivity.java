package com.artw.lockscreen;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
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
import com.artw.lockscreen.slidingup.LockerSlidingUpCallback;
import com.artw.lockscreen.statusbar.StatusBar;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.chargingscreen.activity.ChargingScreenActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.feature.common.Thunk;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.CommonUtils;
import com.ihs.keyboardutils.utils.PublisherUtils;
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

public class LockerActivity extends AppCompatActivity implements INotificationObserver {

    private static final String TAG = "LOCKER_ACTIVITY";

    public static final String EVENT_FINISH_SELF = "locker_event_finish_self";
    public static final String EXTRA_SHOULD_DISMISS_KEYGUARD = "extra_should_dismiss_keyguard";
    public static final String PREF_KEY_CURRENT_WALLPAPER_HD_URL = "current_hd_wallpaper_url";
    private long startDisplayTime;


    @Thunk
    ViewPager mViewPager;
    private LockerAdapter mLockerAdapter;
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

        // Add guide view
        boolean isLockerGuideEnabled = getResources().getBoolean(R.bool.locker_guide_enabled);
        if (isLockerGuideEnabled && LockerSettings.getLockerShowCount() == 0) {
            final LockerGuideView guideView = (LockerGuideView) getLayoutInflater().inflate(R.layout.locker_guide_view, container, false);
            guideView.setPadding(0, 0, 0, CommonUtils.getNavigationBarHeightUnconcerned(this));
            guideView.setOnFinishListener(new LockerGuideView.OnFinishListener() {
                @Override
                public void onFinish() {
                    container.removeView(guideView);
                }
            });
            container.addView(guideView);
        }

        LockerSettings.increaseLockerShowCount();

        HSAnalytics.logEvent("app_screen_locker_show", "install_type", PublisherUtils.getInstallType());
    }

    @Override
    protected void onResume() {
        super.onResume();
        //// TODO: 17/3/31 增加home键事件处理
//        if (mIsHomeKeyClicked && mLockerAdapter != null && mLockerAdapter.lockerMainFrame != null) {
//            mIsHomeKeyClicked = false;
//            mLockerAdapter.lockerMainFrame.closeDrawer();
//        }
        long current = System.currentTimeMillis();
        if(current  - startDisplayTime >1000){
            startDisplayTime = current;
        }else {
            startDisplayTime = -1;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(startDisplayTime!=-1){
            ChargingScreenActivity.logDisplayTime("app_screenLocker_displaytime",startDisplayTime);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        HSAnalytics.startFlurry();

        // ExpressAd灭屏亮屏刷新补丁
        if (mLockerAdapter != null && mLockerAdapter.lockerMainFrame != null) {
            View adView = mLockerAdapter.lockerMainFrame.getAdView();
            if (adView != null) {
                adView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!HSSessionMgr.isSessionStarted()) {
            HSAnalytics.stopFlurry();
        }

        // ExpressAd灭屏亮屏刷新补丁
        if (mLockerAdapter != null && mLockerAdapter.lockerMainFrame != null) {
            View adView = mLockerAdapter.lockerMainFrame.getAdView();
            if (adView != null) {
                adView.setVisibility(View.INVISIBLE);
            }
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
        mViewPager = (ViewPager) findViewById(R.id.locker_pager);
        mLockerAdapter = new LockerAdapter(this, new LockerSlidingUpCallback(LockerActivity.this));
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

    public void finishSelf() {
        findViewById(R.id.bottom_layer).setVisibility(View.GONE);
        ObjectAnimator fadeOutAnim = ObjectAnimator.ofFloat(mLockerWallpaper, View.ALPHA, 0f);
        fadeOutAnim.setDuration(300);
        fadeOutAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
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
            default:
                break;
        }
    }

    public ImageView getIvLockerWallpaper() {
        return mLockerWallpaper;
    }

}
