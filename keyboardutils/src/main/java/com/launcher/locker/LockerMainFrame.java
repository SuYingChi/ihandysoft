package com.launcher.locker;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.PowerManager;
import android.support.v7.widget.AppCompatButton;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.artw.lockscreen.shimmer.Shimmer;
import com.artw.lockscreen.shimmer.ShimmerTextView;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.ClickUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.feature.boost.BoostSource;
import com.ihs.feature.boost.animation.BlackHoleLayout;
import com.ihs.feature.common.ScreenStatusReceiver;
import com.ihs.feature.common.ViewUtils;
import com.ihs.flashlight.FlashlightManager;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.CommonUtils;
import com.launcher.FloatWindowCompat;
import com.launcher.FloatWindowController;
import com.launcher.LockScreen;
import com.launcher.chargingscreen.view.PopupView;
import com.launcher.chargingscreen.view.RipplePopupView;
import com.launcher.locker.slidingdrawer.SlidingDrawer;
import com.launcher.locker.slidingdrawer.SlidingDrawerContent;
import com.launcher.locker.slidingup.SlidingUpCallback;
import com.launcher.locker.slidingup.SlidingUpTouchListener;

import net.appcloudbox.ads.expressads.AcbExpressAdView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.artw.lockscreen.common.TimeTickReceiver.NOTIFICATION_CLOCK_TIME_CHANGED;

public class LockerMainFrame extends RelativeLayout implements INotificationObserver, SlidingDrawer.SlidingDrawerListener {

    public static final String EVENT_SLIDING_DRAWER_OPENED = "EVENT_SLIDING_DRAWER_OPENED";
    public static final String EVENT_SLIDING_DRAWER_CLOSED = "EVENT_SLIDING_DRAWER_CLOSED";

    private boolean mIsSlidingDrawerOpened = false;
    private boolean mIsBlackHoleShowing = false;

    private LockScreen mLockScreen;

    private View mDimCover;
    private SlidingDrawer mSlidingDrawer;
    private SlidingDrawerContent mSlidingDrawerContent;
    private View mDrawerHandleUp;
    private View mDrawerHandleDown;
    private Shimmer mShimmer;
    private ShimmerTextView mUnlockText;

    private View mBottomOperationArea;
    private View mCameraContainer;
    private View mWallpaperContainer;
    private RelativeLayout mAdContainer;

    private BlackHoleLayout mBlackHole;

    private View mMenuMore;
    private RipplePopupView menuPopupView;
    private PopupView mCloseLockerPopupView;

    private TextView mTvTime;
    private TextView mTvDate;
    private AcbExpressAdView expressAdView;
    private boolean mAdShown;
    private long mOnStartTime;

    public LockerMainFrame(Context context) {
        this(context, null);
    }

    public LockerMainFrame(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LockerMainFrame(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    void setLockScreen(LockScreen lockScreen) {
        mLockScreen = lockScreen;
        mSlidingDrawerContent.setLockScreen((Locker) mLockScreen);
    }

    public void onBackPressed() {
        if (!mIsBlackHoleShowing && mIsSlidingDrawerOpened && mSlidingDrawer != null) {
            mSlidingDrawer.closeDrawer(true);
        }
    }

    public void clearDrawerBackground() {
        if (mSlidingDrawerContent != null) {
            mSlidingDrawerContent.clearBlurredBackground();
        }
    }

    public void closeDrawer() {
        if (mSlidingDrawer != null) {
            mSlidingDrawer.closeDrawer(false);
            onScrollStarted();
            onScrollEnded(false);
            mBottomOperationArea.setAlpha(1);
            mDrawerHandleUp.setAlpha(1);
            mDrawerHandleDown.setAlpha(0);
            mDimCover.setAlpha(0);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (!FloatWindowCompat.needsSystemErrorFloatWindow()) {
            setPadding(0, 0, 0, CommonUtils.getNavigationBarHeight(HSApplication.getContext()));
        }

        mDimCover = findViewById(R.id.dim_cover);
        mSlidingDrawerContent = (SlidingDrawerContent) findViewById(R.id.sliding_drawer_content);
        mDrawerHandleUp = findViewById(R.id.handle_action_up);
        mDrawerHandleDown = findViewById(R.id.handle_action_down);
        mBottomOperationArea = findViewById(R.id.bottom_operation_area);
        mSlidingDrawer = (SlidingDrawer) findViewById(R.id.operation_area);
        mCameraContainer = findViewById(R.id.camera_container);
        mWallpaperContainer = findViewById(R.id.wallpaper_container);
        mAdContainer = ViewUtils.findViewById(this, R.id.rl_ad_container);
        mMenuMore = findViewById(R.id.ic_menu);
        mMenuMore.setOnClickListener(v -> {
            showMenuPopupWindow(getContext(), mMenuMore);
            HSAnalytics.logEvent("Locker_Menu_Clicked");
        });

        mSlidingDrawer.setListener(this);
        mSlidingDrawer.setHandle(R.id.blank_handle, 0);
        mDrawerHandleDown.setOnClickListener(v -> {
            mSlidingDrawer.closeDrawer(true);
        });
        mDrawerHandleUp.setOnClickListener(v -> {
            if (!mIsSlidingDrawerOpened) {
                mSlidingDrawer.doBounceUpAnimation();
            }
        });

        mUnlockText = (ShimmerTextView) findViewById(R.id.unlock_text);
        mShimmer = new Shimmer();
        mShimmer.setDuration(1200);

        mTvTime = (TextView) findViewById(R.id.tv_time);
        mTvDate = (TextView) findViewById(R.id.tv_date);
        refreshClock();
        mAdShown = false;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                mSlidingDrawer.setTranslationY(mSlidingDrawer.getHeight() - CommonUtils.pxFromDp(48));
                setAlpha(0f);
                ObjectAnimator alphaInAnim = ObjectAnimator.ofFloat(LockerMainFrame.this, View.ALPHA, 1);
                alphaInAnim.setDuration(960);
                alphaInAnim.start();
            }
        });

        requestFocus();

        HSGlobalNotificationCenter.addObserver(ScreenStatusReceiver.NOTIFICATION_SCREEN_OFF, this);
        HSGlobalNotificationCenter.addObserver(ScreenStatusReceiver.NOTIFICATION_SCREEN_ON, this);
        HSGlobalNotificationCenter.addObserver(SlidingDrawerContent.EVENT_SHOW_BLACK_HOLE, this);
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_CLOCK_TIME_CHANGED, this);

        requestAds();

        PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        if (pm.isScreenOn()) {
            mShimmer.start(mUnlockText);
        }
    }

    private void requestAds() {
        if (FloatWindowController.getInstance().isLockScreenShown()) {
            expressAdView = new AcbExpressAdView(getContext(), getContext().getString(R.string.ad_placement_locker));
            expressAdView.setAutoSwitchAd(false);
            expressAdView.setExpressAdViewListener(new AcbExpressAdView.AcbExpressAdViewListener() {
                @Override
                public void onAdShown(AcbExpressAdView acbExpressAdView) {
                    mAdShown = true;
                }

                @Override
                public void onAdClicked(AcbExpressAdView acbExpressAdView) {
                    HSGlobalNotificationCenter.sendNotification(Locker.EVENT_FINISH_SELF);
                }
            });
        }
    }

    private void showExpressAd() {
        if (expressAdView != null && expressAdView.getParent() == null) {
            mAdContainer.addView(expressAdView, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        }
    }

    public void onStart() {
        mOnStartTime = System.currentTimeMillis();
    }

    public void onResume() {
        if (expressAdView != null && HSConfig.optBoolean(false, "Application", "Locker", "LockerAutoRefreshAdsEnable")) {
            HSLog.d("LockerMainFrame expressAdView resumeDisplayNewAd");
            expressAdView.switchAd();
        }
    }

    public void onPause() {
    }

    public void onStop() {
        if (System.currentTimeMillis() - mOnStartTime > DateUtils.SECOND_IN_MILLIS) {
            mAdShown = false;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (expressAdView != null) {
            expressAdView.destroy();
        }

        HSGlobalNotificationCenter.removeObserver(this);
        mShimmer.cancel();

        super.onDetachedFromWindow();
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case SlidingDrawerContent.EVENT_SHOW_BLACK_HOLE:
                if (mIsBlackHoleShowing) {
                    break;
                }
                mBlackHole = new BlackHoleLayout(getContext());
                mBlackHole.setBoostSource(BoostSource.LOCKER_TOGGLE);
                mBlackHole.setBlackHoleAnimationListener(() -> {
                    if (mBlackHole != null) {
                        removeView(mBlackHole);
                        mBlackHole = null;
                        HSGlobalNotificationCenter.sendNotification(SlidingDrawerContent.EVENT_BLACK_HOLE_ANIMATION_END);
                    }
                    mIsBlackHoleShowing = false;
                });
                LayoutParams params = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
                addView(mBlackHole, params);
                mIsBlackHoleShowing = true;
                postDelayed(() -> {
                    if (mBlackHole != null) {
                        mBlackHole.startAnimation();
                    }
                }, SlidingDrawerContent.DURATION_BALL_DISAPPEAR);
                break;
//            case WeatherClockManager.NOTIFICATION_CLOCK_TIME_CHANGED:
//                refreshClock();
//                break;
            case ScreenStatusReceiver.NOTIFICATION_SCREEN_OFF:
                onPause();
                onStop();

                if (mShimmer.isAnimating()) {
                    mShimmer.cancel();
                }
                break;
            case ScreenStatusReceiver.NOTIFICATION_SCREEN_ON:
                if (expressAdView == null) {
                    requestAds();
                    showExpressAd();
                } else if (expressAdView.getParent() == null) {
                    showExpressAd();
                } else {
                    onResume();
                }

                if (!mShimmer.isAnimating()) {
                    mShimmer.start(mUnlockText);
                }

                // toggle guide
                if (!LockerSettings.isLockerToggleGuideShown()) {
                    if (mDrawerHandleUp == null) {
                        return;
                    }

                    postDelayed(() -> {
                        int bounceTranslationY = -CommonUtils.pxFromDp(13);
                        ObjectAnimator bounceAnimator = ObjectAnimator.ofFloat(mDrawerHandleUp,
                                View.TRANSLATION_Y,
                                0, bounceTranslationY, 0, bounceTranslationY, 0, bounceTranslationY, 0, bounceTranslationY, 0);
                        bounceAnimator.setDuration(3500);
                        bounceAnimator.setInterpolator(new LinearInterpolator());
                        bounceAnimator.start();
                    }, 300);
                }
                break;
            default:
                break;
        }
    }

    private void refreshClock() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        if (!android.text.format.DateFormat.is24HourFormat(HSApplication.getContext()) && hour != 12) {
            hour = hour % 12;
        }
        mTvTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
        DateFormat format = new SimpleDateFormat("MMMM, dd\nEEE", Locale.getDefault());
        mTvDate.setText(format.format(new Date()));
    }

    @Override
    public void onScrollStarted() {
        mBottomOperationArea.setVisibility(View.VISIBLE);
        mDimCover.setVisibility(View.VISIBLE);
        mAdContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onScrollEnded(boolean expanded) {
        LockerSettings.setLockerToggleGuideShown();
        mIsSlidingDrawerOpened = expanded;

        if (mIsSlidingDrawerOpened) {
            mBottomOperationArea.setVisibility(View.INVISIBLE);
            mAdContainer.setVisibility(View.INVISIBLE);
            HSGlobalNotificationCenter.sendNotification(EVENT_SLIDING_DRAWER_OPENED);
        } else {
            mDimCover.setVisibility(View.INVISIBLE);
            HSGlobalNotificationCenter.sendNotification(EVENT_SLIDING_DRAWER_CLOSED);
        }
    }

    @Override
    public void onScroll(float cur, float total) {
        float heightToDisappear = CommonUtils.pxFromDp(24);
        float alpha = (heightToDisappear + cur - total) / heightToDisappear;
        alpha = alpha < 0 ? 0 : (alpha > 1 ? 1 : alpha);
        mBottomOperationArea.setAlpha(alpha);
        mDrawerHandleUp.setAlpha(cur / total);
        mDrawerHandleDown.setAlpha(1f - cur / total);
        mDimCover.setAlpha(1f - cur / total);
        mSlidingDrawerContent.onScroll(cur, total);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mIsSlidingDrawerOpened) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN
                    && !LockerUtils.isTouchInView(mSlidingDrawer, ev)
                    && !mIsBlackHoleShowing) {
                mSlidingDrawer.closeDrawer(true);
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setSlidingUpCallback(SlidingUpCallback callback) {
        final SlidingUpTouchListener rightListener = new SlidingUpTouchListener(SlidingUpTouchListener.TYPE_RIGHT, callback);
        mCameraContainer.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!FlashlightManager.getInstance().isOn() && !mIsSlidingDrawerOpened) {
                    rightListener.onTouch(v, event);
                }
                return true;
            }
        });

        final SlidingUpTouchListener leftListener = new SlidingUpTouchListener(SlidingUpTouchListener.TYPE_LEFT, callback);
        mWallpaperContainer.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                leftListener.onTouch(v, event);
                return true;
            }
        });
    }

    private void showMenuPopupWindow(Context context, View anchorView) {
        if (menuPopupView == null) {
            menuPopupView = new RipplePopupView(context, mLockScreen.getRootView());
            View view = LayoutInflater.from(context).inflate(R.layout.charging_screen_popup_window_new,
                    mLockScreen.getRootView(), false);
            TextView txtCloseChargingBoost = (TextView) view.findViewById(R.id.tv_close);
            txtCloseChargingBoost.setText(getResources().getString(R.string.locker_menu_disable));
            txtCloseChargingBoost.requestLayout();
            txtCloseChargingBoost.setOnClickListener(v -> {
                if (ClickUtils.isFastDoubleClick()) {
                    return;
                }
                menuPopupView.dismiss();
                showLockerCloseDialog();
            });

            menuPopupView.setOutSideBackgroundColor(Color.TRANSPARENT);
            menuPopupView.setContentView(view);
            menuPopupView.setOutSideClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    menuPopupView.dismiss();
                }
            });
        }

        menuPopupView.showAsDropDown(anchorView,
                -(getResources().getDimensionPixelOffset(R.dimen.lock_screen_pop_menu_offset_x) - anchorView.getWidth()),
                -(getResources().getDimensionPixelOffset(R.dimen.charging_screen_menu_to_top_height)
                        + anchorView.getHeight()) / 2);
    }

    private void showLockerCloseDialog() {
        if (mCloseLockerPopupView == null) {
            mCloseLockerPopupView = new PopupView(getContext(), mLockScreen.getRootView());
            View content = LayoutInflater.from(getContext()).inflate(R.layout.locker_popup_dialog, null);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams((int) (CommonUtils
                    .getPhoneWidth(getContext()) * 0.872f), WRAP_CONTENT);
            content.setLayoutParams(layoutParams);
            TextView title = ViewUtils.findViewById(content, R.id.title);
            TextView hintContent = ViewUtils.findViewById(content, R.id.hint_content);
            AppCompatButton buttonYes = ViewUtils.findViewById(content, R.id.button_yes);
            AppCompatButton buttonNo = ViewUtils.findViewById(content, R.id.button_no);
            title.setText(R.string.locker_disable_confirm);
            hintContent.setText(R.string.locker_disable_confirm_detail);
            buttonNo.setText(R.string.charging_screen_close_dialog_positive_action);
            buttonNo.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mCloseLockerPopupView.dismiss();
                }
            });
            buttonYes.setText(R.string.charging_screen_close_dialog_negative_action);
            buttonYes.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    com.artw.lockscreen.LockerSettings.setLockerEnabled(false);
                    mLockScreen.dismiss(getContext(), false);
                    Toast.makeText(getContext(), R.string.locker_diabled_success, Toast.LENGTH_SHORT).show();
                    mCloseLockerPopupView.dismiss();
                }
            });
            mCloseLockerPopupView.setOutSideBackgroundColor(0xB3000000);
            mCloseLockerPopupView.setContentView(content);
            mCloseLockerPopupView.setOutSideClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mCloseLockerPopupView.dismiss();
                }
            });
        }
        mCloseLockerPopupView.showInCenter();
    }
}
