package com.artw.lockscreen;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.acb.weather.plugin.AcbWeatherManager;
import com.artw.lockscreen.common.LockerChargingScreenUtils;
import com.artw.lockscreen.shimmer.Shimmer;
import com.artw.lockscreen.shimmer.ShimmerTextView;
import com.artw.lockscreen.slidingdrawer.SlidingDrawer;
import com.artw.lockscreen.slidingdrawer.SlidingDrawerContent;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.feature.common.ScreenStatusReceiver;
import com.ihs.feature.weather.WeatherManager;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.CommonUtils;
import com.ihs.keyboardutils.utils.RippleDrawableUtils;
import com.kc.commons.utils.KCCommonUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.artw.lockscreen.LockerSettings.recordLockerDisableOnce;
import static com.artw.lockscreen.common.TimeTickReceiver.NOTIFICATION_CLOCK_TIME_CHANGED;
import static com.ihs.feature.weather.WeatherManager.BUNDLE_KEY_WEATHER_ICON_ID;
import static com.ihs.feature.weather.WeatherManager.BUNDLE_KEY_WEATHER_TEMPERATURE_FORMAT;
import static com.ihs.feature.weather.WeatherManager.BUNDLE_KEY_WEATHER_TEMPERATURE_INT;


public class PremiumLockerMainFrame extends RelativeLayout implements INotificationObserver,
        SlidingDrawer.SlidingDrawerListener, View.OnClickListener {

    public static final String EVENT_SLIDING_DRAWER_OPENED = "EVENT_SLIDING_DRAWER_OPENED";
    public static final String EVENT_SLIDING_DRAWER_CLOSED = "EVENT_SLIDING_DRAWER_CLOSED";

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

    private View mMenuMore;

    private PopupWindow menuPopupWindow;
    private Dialog dialog;

    private TextView mTvTime;
    private TextView mTvDate;
    private Context activity;

    private View buttonUpgrade;
    private View buttonSearch;
    private View buttonBoost;
    private View buttonGame;
    private View buttonCamera;
    private View buttonWeather;

    private boolean shouldShowButtonUpgrade;
    private boolean shouldShowButtonSearch;
    private boolean shouldShowButtons; //Boost, Game, Camera, Weather

    private BroadcastReceiver weatherReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            HSLog.d("weather intent == " + intent);
            if (intent != null && WeatherManager.ACTION_WEATHER_CHANGE.equals(intent.getAction())) {
                updateWeatherView(intent);
            }
        }
    };

    private boolean isReceiverRegistered = false;

    public PremiumLockerMainFrame(Context context) {
        this(context, null);
    }


    public PremiumLockerMainFrame(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PremiumLockerMainFrame(Context context, AttributeSet attrs, int defStyle) {
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

    private View.OnClickListener clickListener = new View.OnClickListener() {
        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.search_button) {
                Intent intent = new Intent(getContext(), PremiumLockerSearchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                ContextCompat.startActivity(getContext(), intent, null);
            } else if (v.getId() == R.id.button_boost) {
                HSLog.d("");
            } else if (v.getId() == R.id.button_game) {
                HSLog.d("");
            } else if (v.getId() == R.id.button_camera) {
                HSLog.d("");
            } else if (v.getId() == R.id.button_weather) {
                HSLog.d("");
            } else if (v.getId() == R.id.icon_locker_upgrade) {
                HSLog.d("");
            }
        }
    };

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mDimCover = findViewById(R.id.dim_cover);

        shouldShowButtonUpgrade = HSConfig.optBoolean(true, "");
        shouldShowButtonSearch = HSConfig.optBoolean(true, "");
        shouldShowButtons = HSConfig.optBoolean(true, "");

        int backgroundColor = ContextCompat.getColor(getContext(), R.color.locker_button_bg);

        buttonUpgrade = findViewById(R.id.icon_locker_upgrade);
        buttonUpgrade.setOnClickListener(clickListener);
        buttonSearch = findViewById(R.id.search_button);
        buttonSearch.setOnClickListener(clickListener);
        buttonSearch.setBackgroundDrawable(RippleDrawableUtils.getContainDisableStatusCompatRippleDrawable(backgroundColor, backgroundColor, DisplayUtils.dip2px(2)));
        buttonBoost = findViewById(R.id.button_boost);
        buttonBoost.setOnClickListener(clickListener);
        buttonBoost.setBackgroundDrawable(RippleDrawableUtils.getContainDisableStatusCompatRippleDrawable(backgroundColor, backgroundColor, DisplayUtils.dip2px(4)));
        buttonGame = findViewById(R.id.button_game);
        buttonGame.setOnClickListener(clickListener);
        buttonGame.setBackgroundDrawable(RippleDrawableUtils.getContainDisableStatusCompatRippleDrawable(backgroundColor, backgroundColor, DisplayUtils.dip2px(4)));
        buttonCamera = findViewById(R.id.button_camera);
        buttonCamera.setOnClickListener(clickListener);
        buttonCamera.setBackgroundDrawable(RippleDrawableUtils.getContainDisableStatusCompatRippleDrawable(backgroundColor, backgroundColor, DisplayUtils.dip2px(4)));
        buttonWeather = findViewById(R.id.button_weather);
        buttonWeather.setOnClickListener(clickListener);
        initButtons();
        buttonWeather.setBackgroundDrawable(RippleDrawableUtils.getContainDisableStatusCompatRippleDrawable(backgroundColor, backgroundColor, DisplayUtils.dip2px(4)));

        if (!shouldShowButtonUpgrade) {
            buttonUpgrade.setVisibility(View.INVISIBLE);
        }
        if (!shouldShowButtonSearch) {
            buttonSearch.setVisibility(View.INVISIBLE);
        }
        if (!shouldShowButtons) {
            buttonBoost.setVisibility(View.INVISIBLE);
            buttonGame.setVisibility(View.INVISIBLE);
            buttonCamera.setVisibility(View.INVISIBLE);
            buttonWeather.setVisibility(View.INVISIBLE);
        }

        mSlidingDrawerContent = (SlidingDrawerContent) findViewById(R.id.sliding_drawer_content);
        mDrawerHandle = findViewById(R.id.blank_handle);
        mDrawerHandleUp = findViewById(R.id.handle_action_up);
        mDrawerHandleDown = findViewById(R.id.handle_action_down);
        mBottomOperationArea = findViewById(R.id.bottom_operation_area);
        mSlidingDrawer = (SlidingDrawer) findViewById(R.id.operation_area);
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

    private void initButtons() {
        initWeather();
    }

    private void initWeather() {
        registerDataReceiver();
    }

    private void requestWeather() {
        Intent intent = new Intent(WeatherManager.ACTION_WEATHER_REQUEST);
        getContext().sendBroadcast(intent);
    }

    public void registerDataReceiver() {
        if (!isReceiverRegistered) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WeatherManager.ACTION_WEATHER_CHANGE);
            getContext().registerReceiver(weatherReceiver, intentFilter);
            isReceiverRegistered = true;

            WeatherManager.init(getContext());
            requestWeather();
        }
    }

    public void unregisterDataReceiver() {
        if (isReceiverRegistered) {
            getContext().unregisterReceiver(weatherReceiver);
            isReceiverRegistered = false;
        }
    }

    private void updateWeatherView(Intent intent) {
        int temp = intent.getIntExtra(BUNDLE_KEY_WEATHER_TEMPERATURE_INT, 0);
        String tempFormat = intent.getStringExtra(BUNDLE_KEY_WEATHER_TEMPERATURE_FORMAT);
        String tempStr;
        if (!TextUtils.isEmpty(tempFormat)) {
            tempStr = String.format(tempFormat, temp);
        } else {
            tempStr = String.valueOf(temp);
        }
        int weatherResId = intent.getIntExtra(BUNDLE_KEY_WEATHER_ICON_ID, R.drawable.battery_rank_svg);
        Drawable weatherDrawable = ContextCompat.getDrawable(getContext(), weatherResId);
        weatherDrawable.setBounds(0, 0, weatherDrawable.getMinimumWidth(), weatherDrawable.getMinimumHeight());
        buttonWeather.setText(tempStr);
        buttonWeather.setCompoundDrawablesWithIntrinsicBounds(null, weatherDrawable, null, null);
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
                ObjectAnimator alphaInAnim = ObjectAnimator.ofFloat(PremiumLockerMainFrame.this, View.ALPHA, 1);
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
        HSGlobalNotificationCenter.removeObserver(this);
        if (mShimmer != null) {
            mShimmer.cancel();
        }
        unregisterDataReceiver();
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
        if (shouldShowButtons) {
            buttonBoost.setVisibility(View.VISIBLE);
            buttonGame.setVisibility(View.VISIBLE);
            buttonCamera.setVisibility(View.VISIBLE);
            buttonWeather.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onScrollEnded(boolean expanded) {
        mIsSlidingDrawerOpened = expanded;

        if (mIsSlidingDrawerOpened) {
            mBottomOperationArea.setVisibility(View.INVISIBLE);
            buttonBoost.setVisibility(View.INVISIBLE);
            buttonGame.setVisibility(View.INVISIBLE);
            buttonCamera.setVisibility(View.INVISIBLE);
            buttonWeather.setVisibility(View.INVISIBLE);
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

    public void setActivityContext(Context activity) {
        this.activity = activity;
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
                LockerSettings.setLockerEnabled(false);
                ((PremiumLockerActivity) getContext()).finishSelf();
                Toast.makeText(getContext(), R.string.locker_diabled_success, Toast.LENGTH_SHORT).show();
                HSAnalytics.logEvent("Locker_DisableLocker_Alert_TurnOff_Clicked");
                recordLockerDisableOnce();
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
        KCCommonUtils.dismissDialog(dialog);
        this.dialog = dialog;
        KCCommonUtils.showDialog(dialog);
        return true;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_weather) {
            AcbWeatherManager.showWeatherInfo(getContext());
        }

    }
}
