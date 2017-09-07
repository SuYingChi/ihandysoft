package com.launcher.locker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.artw.lockscreen.LockerGuideView;
import com.artw.lockscreen.LockerSettings;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.feature.common.Thunk;
import com.ihs.feature.notification.NotificationCondition;
import com.ihs.keyboardutils.BuildConfig;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.CommonUtils;
import com.launcher.HomeKeyWatcher;
import com.launcher.LockScreen;
import com.launcher.LockScreensLifeCycleRegistry;
import com.launcher.locker.slidingdrawer.SlidingDrawerContent;
import com.launcher.locker.slidingup.LockerSlidingUpCallback;
import com.launcher.locker.statusbar.StatusBar;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class Locker extends LockScreen implements INotificationObserver {

    private static final String TAG = "Locker";

    public static DisplayImageOptions lockerBgOption = new DisplayImageOptions.Builder()
            .cacheOnDisk(true)
            .considerExifParams(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    public static final String EVENT_FINISH_SELF = "locker_event_finish_self";
    public static final String EXTRA_SHOULD_DISMISS_KEYGUARD = "extra_should_dismiss_keyguard";
    public static final String PREF_KEY_CURRENT_WALLPAPER_HD_URL = "current_hd_wallpaper_url";

    @Thunk
    ViewPager mViewPager;
    private LockerAdapter mLockerAdapter;
    private ImageView mLockerWallpaper;

    private boolean mIsDestroyed;

    private HomeKeyWatcher mHomeKeyWatcher;
    private boolean mHomeKeyClicked;
    private boolean mIsSetup;

    @Override
    public void setup(ViewGroup root, Bundle extra) {
        super.setup(root, extra);

        mIsSetup = true;
        // ======== onCreate ========
        mHomeKeyWatcher = new HomeKeyWatcher(root.getContext());
        mHomeKeyWatcher.setOnHomePressedListener(new HomeKeyWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                mHomeKeyClicked = true;
            }

            @Override
            public void onRecentsPressed() {
            }
        });
        mHomeKeyWatcher.startWatch();

        mLockerWallpaper = (ImageView) mRootView.findViewById(R.id.locker_wallpaper_view);

        try {
            initLockerWallpaper();
        } catch (Exception e) {
            // LauncherGlideModule is not GlideModule
            // only happened on SamSung, android OS 5.0
            if (BuildConfig.DEBUG) {
                throw e;
            }
            dismiss(root.getContext(), false);
        }
        configLockViewPager();

        ViewGroup container = (ViewGroup) mRootView.findViewById(R.id.activity_locker);
        StatusBar statusBar = (StatusBar) LayoutInflater.from(mRootView.getContext())
                .inflate(R.layout.locker_status_bar_new, container, false);
        container.addView(statusBar);

        HSGlobalNotificationCenter.addObserver(EVENT_FINISH_SELF, this);

        // Add guide view
        boolean isLockerGuideEnabled = HSApplication.getContext().getResources().getBoolean(R.bool.locker_guide_enabled);
        if (isLockerGuideEnabled && LockerSettings.getLockerShowCount() == 0) {
            final LockerGuideView guideView = (LockerGuideView) LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.locker_guide_view, container, false);
            guideView.setPadding(0, 0, 0, CommonUtils.getNavigationBarHeightUnconcerned(HSApplication.getContext()));
            guideView.setOnFinishListener(new LockerGuideView.OnFinishListener() {
                @Override
                public void onFinish() {
                    container.removeView(guideView);
                }
            });
            container.addView(guideView);
        }

        LockerSettings.increaseLockerShowCount();

        // ======== onStart ========
        if (mLockerAdapter.lockerMainFrame != null) {
            mLockerAdapter.lockerMainFrame.onStart();
        }

        // ======== onResume ========
        if (mHomeKeyClicked && mLockerAdapter != null && mLockerAdapter.lockerMainFrame != null) {
            mHomeKeyClicked = false;
            mLockerAdapter.lockerMainFrame.closeDrawer();
        }

        // Life cycle
        LockScreensLifeCycleRegistry.setLockerActive(true);
        HSGlobalNotificationCenter.sendNotification(NotificationCondition.EVENT_LOCK);
    }

    private void initLockerWallpaper() {
        String wallpaperUrl = LockerSettings.getLockerBgUrl();
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
        Context context = mRootView.getContext();
        mViewPager = (ViewPager) mRootView.findViewById(R.id.locker_pager);
        mLockerAdapter = new LockerAdapter(context, this, new LockerSlidingUpCallback(this));
        mViewPager.setAdapter(mLockerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.requestFocus();
        mViewPager.setCurrentItem(LockerAdapter.PAGE_INDEX_MAINFRAME);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (LockerAdapter.PAGE_INDEX_UNLOCK == position) {
                    dismiss(getContext(), true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void dismiss(Context context, boolean dismissKeyguard) {
        if (!mIsSetup) {
            return;
        }
        mIsSetup = false;

        mRootView.findViewById(R.id.bottom_layer).setVisibility(View.GONE);
        ObjectAnimator fadeOutAnim = ObjectAnimator.ofFloat(mLockerWallpaper, View.ALPHA, 0f);
        fadeOutAnim.setDuration(300);
        fadeOutAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLockerWallpaper.setImageResource(android.R.color.transparent);
                if (mLockerAdapter != null && mLockerAdapter.lockerMainFrame != null) {
                    mLockerAdapter.lockerMainFrame.clearDrawerBackground();
                }

                // Life cycle
                LockScreensLifeCycleRegistry.setLockerActive(false);
                HSGlobalNotificationCenter.sendNotification(NotificationCondition.EVENT_UNLOCK);

                cleanup();

                Locker.super.dismiss(context, dismissKeyguard);
            }
        });
        fadeOutAnim.start();
    }

    private void cleanup() {
        // ======== onPause ========
        if (mLockerAdapter.lockerMainFrame != null) {
            mLockerAdapter.lockerMainFrame.onPause();
        }

        // ======== onStop ========
        if (mLockerAdapter.lockerMainFrame != null) {
            mLockerAdapter.lockerMainFrame.onStop();
        }

        // ======== onDestroy ========
        mHomeKeyWatcher.destroy();
        HSGlobalNotificationCenter.removeObserver(this);
        mIsDestroyed = true;
    }

    public void onBackPressed() {
        if (mLockerAdapter != null && mLockerAdapter.lockerMainFrame != null) {
            mLockerAdapter.lockerMainFrame.onBackPressed();
        }
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case EVENT_FINISH_SELF:
                boolean shouldDismissKeyguard = true;
                if (hsBundle != null) {
                    shouldDismissKeyguard = hsBundle.getBoolean(EXTRA_SHOULD_DISMISS_KEYGUARD, true);
                }
                dismiss(getContext(), shouldDismissKeyguard);
                break;
            default:
                break;
        }
    }

    public ImageView getIvLockerWallpaper() {
        return mLockerWallpaper;
    }

    public boolean isDestroyed() {
        return mIsDestroyed;
    }
}
