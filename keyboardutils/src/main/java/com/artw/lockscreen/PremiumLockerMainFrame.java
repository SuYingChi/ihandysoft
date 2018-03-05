package com.artw.lockscreen;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.acb.weather.plugin.AcbWeatherManager;
import com.artw.lockscreen.common.LockerChargingScreenUtils;
import com.artw.lockscreen.common.NavUtils;
import com.artw.lockscreen.lockerappguide.LockerAppGuideManager;
import com.artw.lockscreen.shimmer.Shimmer;
import com.artw.lockscreen.shimmer.ShimmerTextView;
import com.artw.lockscreen.slidingdrawer.SlidingDrawer;
import com.artw.lockscreen.slidingdrawer.SlidingDrawerContent;
import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.chargingscreen.utils.LockerChargingSpecialConfig;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSJsonUtil;
import com.ihs.commons.utils.HSLog;
import com.ihs.feature.battery.BatteryActivity;
import com.ihs.feature.battery.BatteryAppInfo;
import com.ihs.feature.battery.BatteryDataManager;
import com.ihs.feature.boost.plus.BoostPlusActivity;
import com.ihs.feature.common.DeviceManager;
import com.ihs.feature.common.ScreenStatusReceiver;
import com.ihs.feature.cpucooler.CpuCoolDownActivity;
import com.ihs.feature.cpucooler.CpuCoolerManager;
import com.ihs.feature.junkclean.JunkCleanActivity;
import com.ihs.feature.junkclean.data.JunkManager;
import com.ihs.feature.junkclean.model.JunkInfo;
import com.ihs.feature.softgame.GameStarterActivity;
import com.ihs.feature.softgame.SoftGameDisplayActivity;
import com.ihs.feature.weather.WeatherManager;
import com.ihs.feature.zodiac.ZodiacUtils;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.alerts.LockerUpgradeAlert;
import com.ihs.keyboardutils.appsuggestion.AppSuggestionManager;
import com.ihs.keyboardutils.notification.KCNotificationManager;
import com.ihs.keyboardutils.notification.NotificationBean;
import com.ihs.keyboardutils.utils.CommonUtils;
import com.ihs.keyboardutils.utils.RippleDrawableUtils;
import com.ihs.keyboardutils.view.HSGifImageView;
import com.kc.commons.utils.KCCommonUtils;
import com.kc.utils.KCAnalytics;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import net.appcloudbox.internal.service.utils.AcbError;
import net.appcloudbox.service.AcbHoroscopeData;
import net.appcloudbox.service.AcbHoroscopeRequest;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.artw.lockscreen.LockerSettings.recordLockerDisableOnce;
import static com.artw.lockscreen.common.TimeTickReceiver.NOTIFICATION_CLOCK_TIME_CHANGED;
import static com.ihs.feature.weather.WeatherManager.BUNDLE_KEY_WEATHER_DESCRIPTION;
import static com.ihs.feature.weather.WeatherManager.BUNDLE_KEY_WEATHER_ICON_ID;
import static com.ihs.feature.weather.WeatherManager.BUNDLE_KEY_WEATHER_TEMPERATURE_FORMAT;
import static com.ihs.feature.weather.WeatherManager.BUNDLE_KEY_WEATHER_TEMPERATURE_INT;

public class PremiumLockerMainFrame extends PercentRelativeLayout implements INotificationObserver, SlidingDrawer.SlidingDrawerListener {

    private static final String TAG = "PremiumLockerMainFrame";
    public static final String EVENT_SLIDING_DRAWER_OPENED = "EVENT_SLIDING_DRAWER_OPENED";
    public static final String EVENT_SLIDING_DRAWER_CLOSED = "EVENT_SLIDING_DRAWER_CLOSED";

    private static final int MODE_JUNK = 0;
    private static final int MODE_GAME = 1;
    private static final int MODE_CAMERA = 2;
    private static final int MODE_BATTERY = 3;
    private static final int MODE_CPU = 4;
    private static final int MODE_STORAGE = 5;
    private static final int MODE_ZODIAC = 6;
    private static final int GAME_INFO_COUNT = 10;
    private static final int MODE_COUNT = 7;

    private static final String PUSH_FRAME_PREFERENCE = "push_frame";
    private static final String GAME_INFO_PREFERENCE = "gameInfo";
    private static final String PUSH_FRAME_INDEX = "index";
    private static final String PUSH_FRAME_TIME = "time";
    private static final String PUSH_GAME_POSITION = "position";
    private static final String PUSH_GAME_THUMB = "thumb";
    private static final String PUSH_GAME_NAME = "name";
    private static final String PUSH_GAME_DESCRIPTION = "description";
    private static final String PUSH_GAME_URL = "link";
    private static final String PUSH_CAM_FINISHED_EVENT = "prefs_finished_event";
    private static final String PUSH_CAM_NOTIFICATION_INDEX = "next_notification_index";
    private static final String PUSH_ZODIAC_ADD_TO_FIRST_TIME = "pref_push_zodiac_add_to_first_date_key";

    private boolean mIsSlidingDrawerOpened = false;
    private boolean mIsBlackHoleShowing = false;

    private AcbHoroscopeRequest horoscopeRequest;

    private DeviceManager deviceManager = DeviceManager.getInstance();
    private BatteryDataManager batteryDataManager = new BatteryDataManager(getContext());
    private SharedPreferences pushFramePreferences = getContext().getSharedPreferences(PUSH_FRAME_PREFERENCE, Context.MODE_PRIVATE);
    private SharedPreferences pushGamePreferences = getContext().getSharedPreferences(GAME_INFO_PREFERENCE, Context.MODE_PRIVATE);

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

    private TextView mTvTime;
    private TextView mTvDate;

    private HSGifImageView buttonUpgrade;
    private View buttonSearch;
    private View buttonBoost;
    private View buttonGame;
    private View buttonCamera;
    private View buttonWeather;

    private boolean shouldShowButtonUpgrade;
    private boolean shouldShowButtonSearch;
    private boolean shouldShowCommonUseButtons; //Boost, Game, Camera, Weather

    private PremiumSearchDialog searchDialog;

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
            AppSuggestionManager.getInstance().disableAppSuggestionForOneTime();
            Context context = getContext().getApplicationContext();
            boolean quickLaunch = HSConfig.optBoolean(false, "Application", "Locker", "QuickLaunch");
            if (v.getId() == R.id.search_button) {
                KCAnalytics.logEvent("new_screenLocker_feature_clicked", "entry", "search");
                searchDialog = new PremiumSearchDialog(getContext());
                searchDialog.setOnSearchListerner((searchDialog, searchText) -> {
                    String url = WebContentSearchManager.getInstance().queryText(searchText);
                    SearchIntentReceiver.sendSearchIntent(url);
                    if (!quickLaunch) {
                        HSGlobalNotificationCenter.sendNotification(PremiumLockerActivity.EVENT_FINISH_SELF);
                    }
                    searchDialog.dismiss();
                });
                searchDialog.setOnDismissListener(dialog -> {
                    searchDialog = null;
                });
                searchDialog.show();
            } else if (v.getId() == R.id.button_boost) {
                KCAnalytics.logEvent("new_screenLocker_feature_clicked", "entry", "boost");
                Intent intent = new Intent(context, BoostPlusActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                HSGlobalNotificationCenter.sendNotification(PremiumLockerActivity.EVENT_FINISH_SELF);
            } else if (v.getId() == R.id.button_game) {
                KCAnalytics.logEvent("new_screenLocker_feature_clicked", "entry", "game");
                Intent intent = new Intent(context, SoftGameDisplayActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                HSGlobalNotificationCenter.sendNotification(PremiumLockerActivity.EVENT_FINISH_SELF);
            } else if (v.getId() == R.id.button_camera) {
                KCAnalytics.logEvent("new_screenLocker_feature_clicked", "entry", "camera");
                NavUtils.startCameraFromLockerScreen(context);
                HSBundle bundle = new HSBundle();
                bundle.putBoolean(PremiumLockerActivity.FINISH_WITHOUT_UNLOCK, true);
                HSGlobalNotificationCenter.sendNotification(PremiumLockerActivity.EVENT_FINISH_SELF, bundle);
            } else if (v.getId() == R.id.button_weather) {
                KCAnalytics.logEvent("new_screenLocker_feature_clicked", "entry", "weather");
                AcbWeatherManager.showWeatherInfo(context, false);
                HSGlobalNotificationCenter.sendNotification(PremiumLockerActivity.EVENT_FINISH_SELF);
            } else if (v.getId() == R.id.icon_locker_upgrade) {
                KCAnalytics.logEvent("new_screenLocker_feature_clicked", "entry", "upgrade");
                if (LockerAppGuideManager.getInstance().isLockerInstall() && !LockerChargingSpecialConfig.getInstance().isLockerEnable()) {
                    LockerAppGuideManager.openApp(LockerAppGuideManager.getLockerAppPkgName());
                    HSGlobalNotificationCenter.sendNotification(PremiumLockerActivity.EVENT_FINISH_SELF);
                } else {
                    final LockerUpgradeAlert alert = new LockerUpgradeAlert(getContext());
                    alert.show();
                    //noinspection ConstantConditions
                    alert.getWindow().setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.locker_upgrade_alert_background));
                }
            }
        }
    };

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mDimCover = findViewById(R.id.dim_cover);

        shouldShowButtonUpgrade = HSConfig.optBoolean(true, "Application", "Locker", "ShowUpgradeButton");
        shouldShowButtonSearch = HSConfig.optBoolean(true, "Application", "Locker", "ShowSearchButton");
        shouldShowCommonUseButtons = HSConfig.optBoolean(true, "Application", "Locker", "ShowCommonUseButton");

        int backgroundColor = ContextCompat.getColor(getContext(), R.color.locker_button_bg);
        int backgroundPressColor = ContextCompat.getColor(getContext(), R.color.locker_button_press_bg);

        buttonUpgrade = findViewById(R.id.icon_locker_upgrade);
        buttonUpgrade.setImageResource(R.raw.upgrade_icon);
        buttonUpgrade.setOnClickListener(clickListener);
        buttonSearch = findViewById(R.id.search_button);
        buttonSearch.setOnClickListener(clickListener);
        buttonSearch.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(backgroundColor, backgroundPressColor, DisplayUtils.dip2px(2)));
        buttonBoost = findViewById(R.id.button_boost);
        buttonBoost.setOnClickListener(clickListener);
        buttonBoost.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(backgroundColor, backgroundPressColor, DisplayUtils.dip2px(4)));
        buttonGame = findViewById(R.id.button_game);
        buttonGame.setOnClickListener(clickListener);
        buttonGame.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(backgroundColor, backgroundPressColor, DisplayUtils.dip2px(4)));
        buttonCamera = findViewById(R.id.button_camera);
        buttonCamera.setOnClickListener(clickListener);
        buttonCamera.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(backgroundColor, backgroundPressColor, DisplayUtils.dip2px(4)));
        buttonWeather = findViewById(R.id.button_weather);
        buttonWeather.setOnClickListener(clickListener);
        buttonWeather.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(backgroundColor, backgroundPressColor, DisplayUtils.dip2px(4)));
        findViewById(R.id.push_close_icon).setOnClickListener(view -> findViewById(R.id.push_frame).setVisibility(INVISIBLE));

        if (!shouldShowButtonUpgrade) {
            buttonUpgrade.setVisibility(View.INVISIBLE);
        }
        if (!shouldShowButtonSearch) {
            buttonSearch.setVisibility(View.INVISIBLE);
        }
        if (!shouldShowCommonUseButtons) {

            buttonBoost.setVisibility(View.INVISIBLE);
            buttonGame.setVisibility(View.INVISIBLE);
            buttonCamera.setVisibility(View.INVISIBLE);
            buttonWeather.setVisibility(View.INVISIBLE);
        }

        mSlidingDrawerContent = findViewById(R.id.sliding_drawer_content);
        mDrawerHandle = findViewById(R.id.blank_handle);
        mDrawerHandleUp = findViewById(R.id.handle_action_up);
        mDrawerHandleDown = findViewById(R.id.handle_action_down);
        mBottomOperationArea = findViewById(R.id.bottom_operation_area);
        mSlidingDrawer = findViewById(R.id.operation_area);
        mMenuMore = findViewById(R.id.ic_menu);
        mMenuMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenuPopupWindow(getContext(), mMenuMore);
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

        mUnlockText = findViewById(R.id.unlock_text);
        mShimmer = new Shimmer();
        mShimmer.setDuration(2000);

        mTvTime = findViewById(R.id.tv_time);
        mTvDate = findViewById(R.id.tv_date);
        refreshClock();
    }

    private void initButtons() {
        initWeather();
    }

    private void initWeather() {
        if (WeatherManager.getInstance().getCurrentWeatherCondition() != null) {
            ImageView weatherImageView = buttonWeather.findViewById(R.id.weather_image);
            TextView weatherTextView = buttonWeather.findViewById(R.id.weather_desc);
            weatherImageView.setImageResource(WeatherManager.getInstance().getWeatherConditionIconResourceID());
            weatherTextView.setText(WeatherManager.getInstance().getTemperatureDescription());
        }
        registerDataReceiver();
    }

    public void registerDataReceiver() {
        if (!isReceiverRegistered) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WeatherManager.ACTION_WEATHER_CHANGE);
            getContext().registerReceiver(weatherReceiver, intentFilter);
            isReceiverRegistered = true;
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
        String tempDesc = intent.getStringExtra(BUNDLE_KEY_WEATHER_DESCRIPTION);
        int weatherResId = intent.getIntExtra(BUNDLE_KEY_WEATHER_ICON_ID, R.drawable.weather_unknown_s);
        ImageView weatherImageView = buttonWeather.findViewById(R.id.weather_image);
        TextView weatherTextView = buttonWeather.findViewById(R.id.weather_desc);
        AlphaAnimation mShowAction = new AlphaAnimation(0, 1);
        mShowAction.setDuration(1000);
        AlphaAnimation mHiddenAction = new AlphaAnimation(1, 0);
        mHiddenAction.setDuration(1000);
        mHiddenAction.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                weatherImageView.setVisibility(GONE);
                weatherImageView.setImageResource(weatherResId);
                weatherTextView.setText(tempStr);
                weatherImageView.startAnimation(mShowAction);
                weatherImageView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        weatherImageView.startAnimation(mHiddenAction);
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

        initButtons();
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
        if (horoscopeRequest != null) {
            horoscopeRequest.cancel();
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
                findViewById(R.id.push_frame).setVisibility(INVISIBLE);
                if (mShimmer.isAnimating()) {
                    mShimmer.cancel();
                }
                buttonUpgrade.clear();
                break;
            case ScreenStatusReceiver.NOTIFICATION_SCREEN_ON:
                findViewById(R.id.push_frame).setVisibility(VISIBLE);
                if (pushGamePreferences.getAll().size() <= 0) {
                    int gameInfoPosition = pushGamePreferences.getInt(PUSH_GAME_POSITION, 0);
                    gameInfoPosition++;
                    gameInfoPosition = gameInfoPosition % GAME_INFO_COUNT;
                    askForGameInfo(gameInfoPosition);
                }

                if (!mShimmer.isAnimating()) {
                    mShimmer.start(mUnlockText);
                }
                buttonUpgrade.setImageResource(R.raw.upgrade_icon);
                if (!addZodiacToFirst()) {
                    int timeForUpdate = (int) (System.currentTimeMillis() - pushFramePreferences.getLong(PUSH_FRAME_TIME, 0)) / (60 * 1000);
                    if (timeForUpdate >= HSConfig.optInteger(5, "Application", "LockerPush", "IntervalTimeInMin")) {
                        findViewById(R.id.push_frame).setVisibility(VISIBLE);
                        increasePushFrameItemIndex();
                    }
                }
                while (!showPushFrameItem(getPushFrameItemIndex())) {
                    increasePushFrameItemIndex();
                }
                break;
            default:
                break;
        }
    }

    private int getPushFrameItemIndex() {
        return pushFramePreferences.getInt(PUSH_FRAME_INDEX, 0);
    }

    private void increasePushFrameItemIndex() {
        int pushFrameItemIndex = getPushFrameItemIndex();
        if (pushFrameItemIndex == MODE_GAME) {
            int gameInfoPosition = pushGamePreferences.getInt(PUSH_GAME_POSITION, 0);
            gameInfoPosition++;
            gameInfoPosition = gameInfoPosition % GAME_INFO_COUNT;
            askForGameInfo(gameInfoPosition);
        }
        if (pushFrameItemIndex == MODE_CAMERA) {
            int notificationBeanIndex = pushFramePreferences.getInt(PUSH_CAM_NOTIFICATION_INDEX, 0);
            pushFramePreferences.edit().putInt(PUSH_CAM_NOTIFICATION_INDEX, ++notificationBeanIndex).apply();
        }
        pushFrameItemIndex++;
        pushFrameItemIndex = pushFrameItemIndex % MODE_COUNT;
        pushFramePreferences.edit().putInt(PUSH_FRAME_INDEX, pushFrameItemIndex).putLong(PUSH_FRAME_TIME, System.currentTimeMillis()).apply();
    }

    private boolean addZodiacToFirst() {
        //时间大于3点并且今天星座item未加塞到第一项并且当天未点击过星座
        if (getHourOfDay() > 3 && !hasZodiacAddToFirstToday() && !ZodiacUtils.hasZodiacPageShowedToday()) {
            SharedPreferences.Editor editor = pushFramePreferences.edit();
            editor.putLong(PUSH_ZODIAC_ADD_TO_FIRST_TIME, System.currentTimeMillis());
            editor.putInt(PUSH_FRAME_INDEX, MODE_ZODIAC);
            editor.apply();
            return true;
        } else {
            return false;
        }
    }

    private boolean hasZodiacAddToFirstToday() {
        long lastZodiacAddedToFirstTime = pushFramePreferences.getLong(PUSH_ZODIAC_ADD_TO_FIRST_TIME, 0);
        return DateUtils.isToday(lastZodiacAddedToFirstTime);
    }

    private int getHourOfDay() {
        Date date = new Date(System.currentTimeMillis());
        Calendar.getInstance().setTime(date);
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    private void askForGameInfo(int position) {
        String urlOfGame = "http://api.famobi.com/feed?a=A-KCVWU&n=10&sort=top_games";
        HSHttpConnection hsHttpConnection = new HSHttpConnection(urlOfGame);
        hsHttpConnection.startAsync();
        hsHttpConnection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override
            public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                JSONObject bodyJSON = hsHttpConnection.getBodyJSON();
                try {
                    List<Object> jsonMap = HSJsonUtil.toList(bodyJSON.getJSONArray("games"));
                    Map<String, String> object = (Map<String, String>) jsonMap.get(position);
                    SharedPreferences.Editor editorOfGame = pushGamePreferences.edit();
                    editorOfGame.putString(PUSH_GAME_NAME, object.get(PUSH_GAME_NAME));
                    editorOfGame.putString(PUSH_GAME_DESCRIPTION, object.get(PUSH_GAME_DESCRIPTION));
                    editorOfGame.putString(PUSH_GAME_THUMB, object.get(PUSH_GAME_THUMB));
                    editorOfGame.putString(PUSH_GAME_URL, object.get(PUSH_GAME_URL));
                    editorOfGame.putInt(PUSH_GAME_POSITION, position);
                    editorOfGame.apply();

                    DisplayImageOptions options = new DisplayImageOptions.Builder()
                            .cacheOnDisk(true)
                            .build();
                    ImageLoader.getInstance().loadImage(object.get(PUSH_GAME_URL), options, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                hsError.getMessage();
            }
        });
    }

    private boolean showPushFrameItem(int pushFrameItemIndex) {
        HSLog.d(TAG, "pushFrameItemIndex: " + pushFrameItemIndex);
        findViewById(R.id.push_camera).setVisibility(GONE);
        findViewById(R.id.push_game).setVisibility(GONE);
        findViewById(R.id.push_boost_two).setVisibility(GONE);
        findViewById(R.id.push_boost_scan).setVisibility(GONE);
        findViewById(R.id.push_boost_one).setVisibility(GONE);
        findViewById(R.id.push_zodiac).setVisibility(GONE);

        switch (pushFrameItemIndex) {
            case MODE_JUNK:
                findViewById(R.id.push_camera).setVisibility(GONE);
                findViewById(R.id.push_boost_two).setVisibility(GONE);
                findViewById(R.id.push_game).setVisibility(GONE);

                View junkRootView = findViewById(R.id.push_boost_one);
                junkRootView.setVisibility(GONE);
                ((ContentLoadingProgressBar) findViewById(R.id.spin_circle)).getIndeterminateDrawable().setColorFilter(Color.parseColor("#7f000000"), PorterDuff.Mode.MULTIPLY);
                findViewById(R.id.push_boost_scan).setVisibility(VISIBLE);
                ((TextView) findViewById(R.id.push_boost_scan_button)).setText(getResources().getString(R.string.push_junk_button));
                findViewById(R.id.push_boost_scan_button).setOnClickListener(view -> {
                    increasePushFrameItemIndex();
                    Intent junkCleanIntent = new Intent(getContext(), JunkCleanActivity.class);
                    getContext().startActivity(junkCleanIntent);
                    HSGlobalNotificationCenter.sendNotification(PremiumLockerActivity.EVENT_FINISH_SELF);
                });
                JunkManager junkManager = JunkManager.getInstance();
                junkManager.startJunkScan(new JunkManager.ScanJunkListener() {
                    @Override
                    public void onScanNameChanged(String name) {

                    }

                    @Override
                    public void onScanSizeChanged(String categoryType, JunkInfo junkInfo, boolean isEnd) {

                    }

                    @Override
                    public void onScanFinished(long junkSize) {
                        if (getPushFrameItemIndex() == MODE_JUNK) {
                            findViewById(R.id.push_boost_scan).setVisibility(GONE);
                            junkRootView.setVisibility(VISIBLE);

                            int junkSizeInMB = (int) (junkSize / (1024L * 1024L));
                            if (junkSize < 0) {
                                ((TextView) junkRootView.findViewById(R.id.boost_result)).setText("50+MB");
                                ((TextView) junkRootView.findViewById(R.id.boost_title)).setText(getResources().getString(R.string.push_junk_title));
                                ((TextView) junkRootView.findViewById(R.id.boost_subtitle)).setText(getResources().getString(R.string.push_junk_subtitle));
                                ((Button) junkRootView.findViewById(R.id.push_boost_button)).setText(getResources().getString(R.string.push_junk_button));
                            } else if (junkSizeInMB < 1024) {
                                ((TextView) junkRootView.findViewById(R.id.boost_result)).setText(junkSizeInMB + "MB");
                                ((TextView) junkRootView.findViewById(R.id.boost_title)).setText(getResources().getString(R.string.push_junk_title));
                                ((TextView) junkRootView.findViewById(R.id.boost_subtitle)).setText(getResources().getString(R.string.push_junk_subtitle));
                                ((Button) junkRootView.findViewById(R.id.push_boost_button)).setText(getResources().getString(R.string.push_junk_button));
                            } else {
                                float junkSizeInGB = (float) junkSizeInMB / 1024f;
                                String junkSizeInGBFloat = String.format("%.2f", junkSizeInGB);
                                ((TextView) junkRootView.findViewById(R.id.boost_result)).setText(junkSizeInGBFloat + "GB");
                                ((TextView) junkRootView.findViewById(R.id.boost_title)).setText(getResources().getString(R.string.push_junk_title));
                                ((TextView) junkRootView.findViewById(R.id.boost_subtitle)).setText(getResources().getString(R.string.push_junk_subtitle));
                                ((Button) junkRootView.findViewById(R.id.push_boost_button)).setText(getResources().getString(R.string.push_junk_button));
                            }
                            ((ImageView) junkRootView.findViewById(R.id.icon)).setImageDrawable(getResources().getDrawable(R.drawable.new_locker_junk));
                            junkRootView.findViewById(R.id.push_boost_button).setOnClickListener(view -> {
                                KCAnalytics.logEvent("Screenlocker_push_clicked", "type", "junk");
                                increasePushFrameItemIndex();
                                Intent junkCleanIntent = new Intent(getContext(), JunkCleanActivity.class);
                                getContext().startActivity(junkCleanIntent);
                                HSGlobalNotificationCenter.sendNotification(PremiumLockerActivity.EVENT_FINISH_SELF);
                            });
                        }
                    }
                });
                break;
            case MODE_GAME:
                if (pushGamePreferences.getAll().size() <= 0) {
                    return false;
                }
                findViewById(R.id.push_boost_one).setVisibility(GONE);
                findViewById(R.id.push_boost_two).setVisibility(GONE);
                findViewById(R.id.push_camera).setVisibility(GONE);
                findViewById(R.id.push_boost_scan).setVisibility(GONE);

                View gameRootView = findViewById(R.id.push_game);
                gameRootView.setVisibility(VISIBLE);

                String iconUrl = pushGamePreferences.getString(PUSH_GAME_THUMB, "");
                DisplayImageOptions gameIconOptions = new DisplayImageOptions.Builder()
                        .cacheOnDisk(true)
                        .bitmapConfig(Bitmap.Config.ARGB_8888)
                        .build();
                ImageLoader.getInstance().displayImage(iconUrl, (ImageView) gameRootView.findViewById(R.id.icon), gameIconOptions);
                ((TextView) gameRootView.findViewById(R.id.push_game_title)).setText(pushGamePreferences.getString(PUSH_GAME_NAME, ""));
                ((TextView) gameRootView.findViewById(R.id.push_game_subtitle)).setText(pushGamePreferences.getString(PUSH_GAME_DESCRIPTION, ""));
                ((Button) gameRootView.findViewById(R.id.push_game_button)).setText(getResources().getString(R.string.push_game_button));
                gameRootView.findViewById(R.id.push_game_button).setOnClickListener(view -> {
                    KCAnalytics.logEvent("Screenlocker_push_clicked", "type", "game");
                    Intent intent = new Intent(getContext(), SoftGameDisplayActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(intent);
                    increasePushFrameItemIndex();
                    String url = pushGamePreferences.getString(PUSH_GAME_URL, null);
                    GameStarterActivity.startGame(url, "push_game_clicked");
                    HSGlobalNotificationCenter.sendNotification(PremiumLockerActivity.EVENT_FINISH_SELF);
                });
                break;
            case MODE_CAMERA:
                List<Map<String, ?>> configs = null;
                try {
                    configs = (List<Map<String, ?>>) HSConfig.getList("Application", "LocalNotifications", "Content");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (configs == null || configs.size() <= 0) {
                    return false;
                }

                int startNotificationBeanIndex = pushFramePreferences.getInt(PUSH_CAM_NOTIFICATION_INDEX, 0);

                NotificationBean bean = null;
                for (int notificationBeanIndex = startNotificationBeanIndex; notificationBeanIndex < startNotificationBeanIndex + configs.size(); notificationBeanIndex++) {
                    bean = KCNotificationManager.getInstance().getAvailableBean(configs, notificationBeanIndex % configs.size());
                    if (bean != null) {
                        pushFramePreferences.edit().putInt(PUSH_CAM_NOTIFICATION_INDEX, notificationBeanIndex % configs.size()).apply();
                        break;
                    }
                }

                if (bean == null) {
                    return false;
                }

                findViewById(R.id.push_boost_one).setVisibility(GONE);
                findViewById(R.id.push_boost_two).setVisibility(GONE);
                findViewById(R.id.push_boost_scan).setVisibility(GONE);
                findViewById(R.id.push_game).setVisibility(GONE);

                View cameraRootView = findViewById(R.id.push_camera);
                cameraRootView.setVisibility(VISIBLE);

                String pushCameraActionType = bean.getActionType();

                ((TextView) cameraRootView.findViewById(R.id.push_camera_title)).setText(bean.getTitle());
                ((TextView) cameraRootView.findViewById(R.id.push_camera_subtitle)).setText(bean.getMessage());
                ((Button) findViewById(R.id.push_camera_button)).setText(bean.getButtonText());
                findViewById(R.id.push_camera_button).setOnClickListener(view -> {
                    KCAnalytics.logEvent("Screenlocker_push_clicked", "type", "camera");
                    increasePushFrameItemIndex();
                    Intent cameraIntent = new Intent("push.camera.store");
                    switch (pushCameraActionType) {
                        case "Filter":
                            cameraIntent.putExtra("intent_key_default_tab", "tab_filter");
                            break;
                        case "Sticker":
                            cameraIntent.putExtra("intent_key_default_tab", "tab_sticker");
                            break;
                        case "LiveSticker":
                            cameraIntent.putExtra("intent_key_default_tab", "tab_live_sticker");
                            break;
                        default:
                            break;
                    }

                    getContext().startActivity(cameraIntent);
                    HSGlobalNotificationCenter.sendNotification(PremiumLockerActivity.EVENT_FINISH_SELF);
                });

                String imageUrl = bean.getIconUrl();
                ((ImageView) findViewById(R.id.icon)).setImageResource(R.drawable.push_camera_icon);
                DisplayImageOptions cameraIconOptions = new DisplayImageOptions.Builder()
                        .showImageOnLoading(R.drawable.push_camera_icon)
                        .showImageOnFail(R.drawable.push_camera_icon)
                        .cacheInMemory(true)
                        .bitmapConfig(Bitmap.Config.ARGB_8888)
                        .build();
                ImageLoader.getInstance().displayImage(imageUrl, (ImageView) cameraRootView.findViewById(R.id.icon), cameraIconOptions);
                break;
            case MODE_BATTERY:
                int batteryLevel = deviceManager.getBatteryLevel();
                int appsCount = 0;
                if (batteryDataManager.getCleanAnimationBatteryApps() != null) {
                    appsCount = batteryDataManager.getCleanAnimationBatteryApps().size();
                }
                if (batteryLevel < 30) {
                    findViewById(R.id.push_camera).setVisibility(GONE);
                    findViewById(R.id.push_game).setVisibility(GONE);
                    findViewById(R.id.push_boost_two).setVisibility(GONE);
                    findViewById(R.id.push_boost_scan).setVisibility(GONE);

                    View batteryOneRootView = findViewById(R.id.push_boost_one);
                    batteryOneRootView.setVisibility(VISIBLE);

                    ((ImageView) batteryOneRootView.findViewById(R.id.icon)).setImageDrawable(getResources().getDrawable(R.drawable.new_locker_battery));
                    ((TextView) batteryOneRootView.findViewById(R.id.boost_result)).setText(batteryLevel + "%");
                    ((TextView) batteryOneRootView.findViewById(R.id.boost_title)).setText(getResources().getString(R.string.push_battery_title));
                    ((TextView) batteryOneRootView.findViewById(R.id.boost_subtitle)).setText(getResources().getString(R.string.push_battery_subtitle));
                    ((Button) batteryOneRootView.findViewById(R.id.push_boost_button)).setText(getResources().getString(R.string.push_battery_button));
                    batteryOneRootView.findViewById(R.id.push_boost_button).setOnClickListener(view -> {
                        KCAnalytics.logEvent("Screenlocker_push_clicked", "type", "battery");
                        increasePushFrameItemIndex();
                        Intent batteryIntent = new Intent(getContext(), BatteryActivity.class);
                        getContext().startActivity(batteryIntent);
                        HSGlobalNotificationCenter.sendNotification(PremiumLockerActivity.EVENT_FINISH_SELF);
                    });
                } else if (batteryLevel <= 80 && appsCount >= 8) {
                    findViewById(R.id.push_camera).setVisibility(GONE);
                    findViewById(R.id.push_game).setVisibility(GONE);
                    findViewById(R.id.push_boost_one).setVisibility(GONE);
                    findViewById(R.id.push_boost_scan).setVisibility(GONE);

                    View batteryTwoRootView = findViewById(R.id.push_boost_two);
                    batteryTwoRootView.setVisibility(VISIBLE);

                    ((ImageView) batteryTwoRootView.findViewById(R.id.icon)).setImageDrawable(getResources().getDrawable(R.drawable.new_locker_battery));
                    ((TextView) batteryTwoRootView.findViewById(R.id.boost_result)).setText(appsCount + "");
                    ((TextView) batteryTwoRootView.findViewById(R.id.boost_title)).setText(getResources().getString(R.string.push_battery_title_plus));
                    ((Button) batteryTwoRootView.findViewById(R.id.push_boost_button)).setText(getResources().getString(R.string.push_battery_button));
                    batteryTwoRootView.findViewById(R.id.push_boost_button).setOnClickListener(view -> {
                        KCAnalytics.logEvent("Screenlocker_push_clicked", "type", "battery");
                        increasePushFrameItemIndex();
                        Intent batteryIntent = new Intent(getContext(), BatteryActivity.class);
                        getContext().startActivity(batteryIntent);
                        HSGlobalNotificationCenter.sendNotification(PremiumLockerActivity.EVENT_FINISH_SELF);
                    });
                    List<BatteryAppInfo> appInfos = batteryDataManager.getCleanAnimationBatteryApps();
                    ((ImageView) batteryTwoRootView.findViewById(R.id.icon_first)).setImageDrawable(appInfos.get(0).getAppDrawable());
                    ((ImageView) batteryTwoRootView.findViewById(R.id.icon_second)).setImageDrawable(appInfos.get(1).getAppDrawable());
                    ((ImageView) batteryTwoRootView.findViewById(R.id.icon_third)).setImageDrawable(appInfos.get(2).getAppDrawable());
                } else {
                    return false;
                }
                break;
            case MODE_CPU:
                int cpuTemperature = CpuCoolerManager.getInstance().fetchCpuTemperature();
                if (cpuTemperature <= 40) {
                    return false;
                }
                findViewById(R.id.push_camera).setVisibility(GONE);
                findViewById(R.id.push_game).setVisibility(GONE);
                findViewById(R.id.push_boost_two).setVisibility(GONE);
                findViewById(R.id.push_boost_scan).setVisibility(GONE);

                View cpuRootView = findViewById(R.id.push_boost_one);
                cpuRootView.setVisibility(VISIBLE);

                ((ImageView) cpuRootView.findViewById(R.id.icon)).setImageDrawable(getResources().getDrawable(R.drawable.new_locker_thermometer));
                ((TextView) cpuRootView.findViewById(R.id.boost_result)).setText(cpuTemperature + "℃");
                ((TextView) cpuRootView.findViewById(R.id.boost_title)).setText(getResources().getString(R.string.push_cpu_title));
                ((TextView) cpuRootView.findViewById(R.id.boost_subtitle)).setText(getResources().getString(R.string.push_cpu_subtitle));
                ((Button) cpuRootView.findViewById(R.id.push_boost_button)).setText(getResources().getString(R.string.push_cpu_button));
                cpuRootView.findViewById(R.id.push_boost_button).setOnClickListener(view -> {
                    KCAnalytics.logEvent("Screenlocker_push_clicked", "type", "CPU");
                    increasePushFrameItemIndex();
                    Intent cpuCoolerCleanIntent = new Intent(getContext(), CpuCoolDownActivity.class);
                    getContext().startActivity(cpuCoolerCleanIntent);
                    HSGlobalNotificationCenter.sendNotification(PremiumLockerActivity.EVENT_FINISH_SELF);
                });
                break;
            case MODE_ZODIAC:
                if (ZodiacUtils.hasZodiacPageShowedToday()) {
                    return false;
                }
                View zodiacRootView = findViewById(R.id.push_zodiac);
                zodiacRootView.setVisibility(VISIBLE);
                View zodiacSetView = zodiacRootView.findViewById(R.id.push_zodiac_set);
                View zodiacShowView = zodiacRootView.findViewById(R.id.push_zodiac_show);
                if (ZodiacUtils.hasSelectedZodiac()) {
                    zodiacSetView.setVisibility(GONE);
                    zodiacShowView.setVisibility(VISIBLE);
                    TextView zodiacTodayTipTitle = findViewById(R.id.zodiac_today_zodiac_name_title);
                    String str = String.format(getContext().getResources().getString(R.string.zodiac_today_title), ZodiacUtils.getZodiacName(ZodiacUtils.getSelectZodiac()));
                    zodiacTodayTipTitle.setText(str);
                    loadZodiacDataAndShow(zodiacShowView);
                } else {
                    zodiacSetView.setVisibility(VISIBLE);
                    zodiacShowView.setVisibility(GONE);
                    TextView setZodiacButton = zodiacSetView.findViewById(R.id.zodiac_set_zodiac_button);
                    setZodiacButton.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(getContext().getResources().getColor(R.color.zodiac_set_button_bg), DisplayUtils.dip2px(20)));
                    setZodiacButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            increasePushFrameItemIndex();
                            NavUtils.startCameraFromLockerScreenWithZodiacInfo(getContext().getApplicationContext(), null);
                            HSGlobalNotificationCenter.sendNotification(PremiumLockerActivity.EVENT_FINISH_SELF);
                        }
                    });
                }
                break;
            case MODE_STORAGE:
                int usage = deviceManager.getRamUsage();
                if (usage < 75) {
                    return false;
                }
                findViewById(R.id.push_camera).setVisibility(GONE);
                findViewById(R.id.push_game).setVisibility(GONE);
                findViewById(R.id.push_boost_two).setVisibility(GONE);
                findViewById(R.id.push_boost_scan).setVisibility(GONE);

                View storageRootView = findViewById(R.id.push_boost_one);
                storageRootView.setVisibility(VISIBLE);

                ((ImageView) storageRootView.findViewById(R.id.icon)).setImageDrawable(getResources().getDrawable(R.drawable.new_locker_boost));
                ((TextView) storageRootView.findViewById(R.id.boost_result)).setText(deviceManager.getRamUsage() + "%");
                ((TextView) storageRootView.findViewById(R.id.boost_title)).setText(getResources().getString(R.string.push_memory_title));
                ((TextView) storageRootView.findViewById(R.id.boost_subtitle)).setText(getResources().getString(R.string.push_memory_subtitle));
                ((Button) findViewById(R.id.push_boost_button)).setText(getResources().getString(R.string.push_memory_button));
                findViewById(R.id.push_boost_button).setOnClickListener(view -> {
                    KCAnalytics.logEvent("Screenlocker_push_clicked", "type", "memory");
                    increasePushFrameItemIndex();
                    Intent boostIntent = new Intent(getContext(), BoostPlusActivity.class);
                    getContext().startActivity(boostIntent);
                    HSGlobalNotificationCenter.sendNotification(PremiumLockerActivity.EVENT_FINISH_SELF);
                });
                break;
            default:
                break;
        }
        return true;
    }

    private void refreshClock() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        if (!android.text.format.DateFormat.is24HourFormat(HSApplication.getContext()) && hour != 12) {
            hour = hour % 12;
        }
        mTvTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
        DateFormat format = new SimpleDateFormat("EEE\nMMMM, dd", Locale.getDefault());
        mTvDate.setText(format.format(new Date()));
    }

    private void loadZodiacDataAndShow(View zodiacShowView) {
        ProgressBar progressBar = zodiacShowView.findViewById(R.id.zodiac_waiting_progress_bar);
        progressBar.setVisibility(VISIBLE);
        View zodiacDetailView = zodiacShowView.findViewById(R.id.zodiac_detail_view);
        zodiacDetailView.setVisibility(GONE);
        View zodiacRefreshView = zodiacShowView.findViewById(R.id.zodiac_refresh_view);
        zodiacRefreshView.setVisibility(GONE);
        AcbHoroscopeData.HoroscopeType horoscopeType = ZodiacUtils.getSelectZodiac();
        if (horoscopeRequest != null) {
            horoscopeRequest.cancel();
        }
        horoscopeRequest = new AcbHoroscopeRequest(getContext(), horoscopeType, new Date(System.currentTimeMillis()), new AcbHoroscopeRequest.AcbHoroscopeListener() {
            @Override
            public void onSuccess(AcbHoroscopeData acbHoroscopeData) {
                HSLog.d(TAG, String.valueOf(acbHoroscopeData));
                progressBar.setVisibility(GONE);
                zodiacDetailView.setVisibility(VISIBLE);
                zodiacRefreshView.setVisibility(GONE);
                RatingBar ratingBarLove = zodiacDetailView.findViewById(R.id.ratingBar_love);
                ratingBarLove.setRating(acbHoroscopeData.getLoveRatings());
                RatingBar ratingBarMoney = zodiacDetailView.findViewById(R.id.ratingBar_money);
                ratingBarMoney.setRating(acbHoroscopeData.getMoneyRatings());
                RatingBar ratingBarCareer = zodiacDetailView.findViewById(R.id.ratingBar_career);
                ratingBarCareer.setRating(acbHoroscopeData.getCareerRatings());
                RatingBar ratingBarHealth = zodiacDetailView.findViewById(R.id.ratingBar_health);
                ratingBarHealth.setRating(acbHoroscopeData.getHealthRatings());
                TextView textViewTipContent = zodiacDetailView.findViewById(R.id.zodiac_tip_content);
                textViewTipContent.setText(acbHoroscopeData.getTip());
                TextView readMore = zodiacDetailView.findViewById(R.id.zodiac_read_more);
                readMore.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(Color.YELLOW, DisplayUtils.dip2px(20)));
                zodiacDetailView.setOnClickListener(v -> {
                    increasePushFrameItemIndex();
                    NavUtils.startCameraFromLockerScreenWithZodiacInfo(getContext().getApplicationContext(), horoscopeType);
                    HSGlobalNotificationCenter.sendNotification(PremiumLockerActivity.EVENT_FINISH_SELF);
                });
            }

            @Override
            public void onFailure(AcbError acbError) {
                HSLog.e(TAG, acbError.getMessage());
                progressBar.setVisibility(GONE);
                zodiacDetailView.setVisibility(GONE);
                zodiacRefreshView.setVisibility(VISIBLE);
                zodiacRefreshView.setOnClickListener(v -> loadZodiacDataAndShow(zodiacShowView));
            }
        });
        horoscopeRequest.start();
    }

    @Override
    public void onScrollStarted() {
        mBottomOperationArea.setVisibility(View.VISIBLE);
        mDimCover.setVisibility(View.VISIBLE);
        if (shouldShowCommonUseButtons) {
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

    private void showMenuPopupWindow(Context context, View parentView) {
        if (menuPopupWindow == null) {
            View view = LayoutInflater.from(context).inflate(R.layout.charging_screen_popup_window, null);
            TextView txtCloseChargingBoost = view.findViewById(R.id.txt_close_charging_boost);
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
        KCCommonUtils.showDialog(dialog);
        return true;
    }

    void onActivityStop() {
        if (searchDialog != null) {
            searchDialog.dismiss();
            searchDialog = null;
        }
    }
}
