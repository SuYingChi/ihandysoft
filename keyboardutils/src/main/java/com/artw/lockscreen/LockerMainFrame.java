package com.artw.lockscreen;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.acb.expressads.AcbExpressAdView;
import com.artw.lockscreen.common.LockerChargingScreenUtils;
import com.ihs.feature.common.ScreenStatusReceiver;
import com.artw.lockscreen.shimmer.Shimmer;
import com.artw.lockscreen.shimmer.ShimmerTextView;
import com.artw.lockscreen.slidingdrawer.SlidingDrawer;
import com.artw.lockscreen.slidingdrawer.SlidingDrawerContent;
import com.artw.lockscreen.slidingup.SlidingUpCallback;
import com.artw.lockscreen.slidingup.SlidingUpTouchListener;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.feature.common.ViewUtils;
import com.ihs.flashlight.FlashlightManager;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.CommonUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.artw.lockscreen.common.TimeTickReceiver.NOTIFICATION_CLOCK_TIME_CHANGED;

public class LockerMainFrame extends RelativeLayout implements INotificationObserver, SlidingDrawer.SlidingDrawerListener {

    public static final String EVENT_SLIDING_DRAWER_OPENED = "EVENT_SLIDING_DRAWER_OPENED";
    public static final String EVENT_SLIDING_DRAWER_CLOSED = "EVENT_SLIDING_DRAWER_CLOSED";
    public static final String LOCKER_NATIVE_AD_PLACEMENT_NAME = "ColorCam_A(NativeAds)ScreenLocker";
    private AcbExpressAdView acbExpressAdView;

    private boolean mIsSlidingDrawerOpened = false;
    private boolean mIsBlackHoleShowing = false;

    private View mDimCover;
    private SlidingDrawer mSlidingDrawer;
    private SlidingDrawerContent mSlidingDrawerContent;
    private View mDrawerHandle;
    private View mDrawerHandleUp;
    private View mDrawerHandleDown;
    private Shimmer mShimmer;
    private ShimmerTextView mUnlockText;

    private View mBottomOperationArea;
    private View mCameraContainer;
    private View mWallpaperContainer;
    private FrameLayout mAdContainer;

    private View mMenuMore;

    private PopupWindow menuPopupWindow;
    private Dialog dialog;

    private TextView mTvTime;
    private TextView mTvDate;

    public LockerMainFrame(Context context) {
        this(context, null);
    }

    public LockerMainFrame(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LockerMainFrame(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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

        mDimCover = findViewById(R.id.dim_cover);
        mSlidingDrawerContent = (SlidingDrawerContent) findViewById(R.id.sliding_drawer_content);
        mDrawerHandle = findViewById(R.id.blank_handle);
        mDrawerHandleUp = findViewById(R.id.handle_action_up);
        mDrawerHandleDown = findViewById(R.id.handle_action_down);
        mBottomOperationArea = findViewById(R.id.bottom_operation_area);
        mSlidingDrawer = (SlidingDrawer) findViewById(R.id.operation_area);
        mCameraContainer = findViewById(R.id.camera_container);
        mWallpaperContainer = findViewById(R.id.wallpaper_container);
        mAdContainer = ViewUtils.findViewById(this, R.id.rl_ad_container);
        mMenuMore = findViewById(R.id.ic_menu);
        mMenuMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenuPopupWindow(getContext(), mMenuMore);
                HSAnalytics.logEvent("Locker_Menu_Clicked");
            }
        });

        mSlidingDrawer.setListener(this);
        mSlidingDrawer.setHandle(R.id.blank_handle, 0);
        mDrawerHandleDown.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSlidingDrawer.closeDrawer(true);
            }
        });
        mDrawerHandleUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsSlidingDrawerOpened) {
                    mSlidingDrawer.doBounceUpAnimation();
                }
            }
        });

        mUnlockText = (ShimmerTextView) findViewById(R.id.unlock_text);
        mShimmer = new Shimmer();
        mShimmer.setDuration(2000);

        mTvTime = (TextView) findViewById(R.id.tv_time);
        mTvDate = (TextView) findViewById(R.id.tv_date);
        refreshClock();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        acbExpressAdView = new AcbExpressAdView(HSApplication.getContext(),LOCKER_NATIVE_AD_PLACEMENT_NAME);
        acbExpressAdView.setExpressAdViewListener(new AcbExpressAdView.AcbExpressAdViewListener() {
            @Override
            public void onAdClicked(AcbExpressAdView acbExpressAdView) {
                HSAnalytics.logEvent("NativeAd_ColorCam_A(NativeAds)ScreenLocker_Click");
                HSGlobalNotificationCenter.sendNotification(LockerActivity.EVENT_FINISH_SELF);
            }
        });
        mAdContainer.addView(acbExpressAdView);
        mAdContainer.setVisibility(VISIBLE);

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
        mShimmer.start(mUnlockText);
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(acbExpressAdView!=null){
            acbExpressAdView.destroy();
        }


        HSGlobalNotificationCenter.removeObserver(this);
        if(mShimmer!=null){
            mShimmer.cancel();
        }
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case NOTIFICATION_CLOCK_TIME_CHANGED:
                refreshClock();
                break;
            case ScreenStatusReceiver.NOTIFICATION_SCREEN_OFF:

                if (mShimmer.isAnimating()) {
                    mShimmer.cancel();
                }
                break;
            case ScreenStatusReceiver.NOTIFICATION_SCREEN_ON:

                if (!mShimmer.isAnimating()) {
                    mShimmer.start(mUnlockText);
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
        mIsSlidingDrawerOpened = expanded;

        if (mIsSlidingDrawerOpened) {
            mBottomOperationArea.setVisibility(View.INVISIBLE);
            mAdContainer.setVisibility(View.INVISIBLE);
            HSGlobalNotificationCenter.sendNotification(EVENT_SLIDING_DRAWER_OPENED);
            HSAnalytics.logEvent("Locker_Toggle_Slided");
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

    private void showMenuPopupWindow(Context context, View parentView) {
        if (menuPopupWindow == null) {
            View view = LayoutInflater.from(context).inflate(R.layout.charging_screen_popup_window, null);
            TextView txtCloseChargingBoost = (TextView) view.findViewById(R.id.txt_close_charging_boost);
            txtCloseChargingBoost.setText(getResources().getString(R.string.locker_menu_disable));
            txtCloseChargingBoost.requestLayout();
            txtCloseChargingBoost.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (LockerChargingScreenUtils.isFastDoubleClick()) {
                        return;
                    }
                    if (menuPopupWindow != null) {
                        menuPopupWindow.dismiss();
                    }
                    HSAnalytics.logEvent("Locker_DisableLocker_Clicked");
                    showLockerCloseDialog();
                }
            });

            menuPopupWindow = new PopupWindow(view);
            menuPopupWindow.setWidth(Toolbar.LayoutParams.WRAP_CONTENT);
            menuPopupWindow.setHeight(Toolbar.LayoutParams.WRAP_CONTENT);
            menuPopupWindow.setFocusable(true);
            menuPopupWindow.setOutsideTouchable(true);
            menuPopupWindow.setBackgroundDrawable(new BitmapDrawable());
            menuPopupWindow.update();
        }

        if (menuPopupWindow.isShowing()) {
            return;
        }
        menuPopupWindow.showAsDropDown(parentView, -getResources().getDimensionPixelSize(R.dimen.charging_popmenu_margin_right),
                -(getResources().getDimensionPixelOffset(R.dimen.charging_screen_menu_to_top_height) + parentView.getHeight()) >> 1);
    }


    private void showLockerCloseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        String title = getContext().getString(R.string.locker_disable_confirm);
        SpannableString spannableStringTitle = new SpannableString(title);
        spannableStringTitle.setSpan(
                new ForegroundColorSpan(0xDF000000),
                0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setTitle(spannableStringTitle);

        String message = getContext().getString(R.string.locker_disable_confirm_detail);
        SpannableString spannableStringMessage = new SpannableString(message);
        spannableStringMessage.setSpan(
                new ForegroundColorSpan(0x8A000000),
                0, message.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setMessage(spannableStringMessage);

        builder.setPositiveButton(getContext().getString(R.string.charging_screen_close_dialog_positive_action), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        builder.setNegativeButton(getContext().getString(R.string.charging_screen_close_dialog_negative_action), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                LockerSettings.setLockerEnabled(false, "activity");
                ((LockerActivity) getContext()).finishSelf(false);
                Toast.makeText(getContext(), R.string.locker_diabled_success, Toast.LENGTH_SHORT).show();
                HSAnalytics.logEvent("Locker_DisableLocker_Alert_TurnOff_Clicked");
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                negativeButton.setTextColor(ContextCompat.getColor(HSApplication.getContext(), R.color.charging_screen_alert_negative_action));

                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setTextColor(ContextCompat.getColor(HSApplication.getContext(), R.color.charging_screen_alert_positive_action));
            }
        });

        showDialog(alertDialog);
    }

    private boolean showDialog(Dialog dialog) {
        hideDialog();
        this.dialog = dialog;
        this.dialog.show();
        return true;
    }

    private void hideDialog() {
        if (this.dialog != null) {
            this.dialog.dismiss();
            this.dialog = null;
        }
    }
}
