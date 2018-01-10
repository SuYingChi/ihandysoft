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
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.percent.PercentRelativeLayout;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
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
import com.ihs.app.analytics.HSAnalytics;
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
import com.ihs.feature.boost.plus.BoostPlusActivity;
import com.ihs.feature.common.ScreenStatusReceiver;
import com.ihs.feature.softgame.SoftGameDisplayActivity;
import com.ihs.feature.weather.WeatherManager;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.alerts.LockerUpgradeAlert;
import com.ihs.keyboardutils.appsuggestion.AppSuggestionManager;
import com.ihs.keyboardutils.notification.NotificationBean;
import com.ihs.keyboardutils.utils.CommonUtils;
import com.ihs.keyboardutils.utils.RippleDrawableUtils;
import com.ihs.keyboardutils.view.HSGifImageView;
import com.kc.commons.utils.KCCommonUtils;

import org.json.JSONObject;

import java.net.URL;
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


    public static final String EVENT_SLIDING_DRAWER_OPENED = "EVENT_SLIDING_DRAWER_OPENED";
    public static final String EVENT_SLIDING_DRAWER_CLOSED = "EVENT_SLIDING_DRAWER_CLOSED";

    private static final int MODE_JUNK = 0;
    private static final int MODE_GAME = 1;
    private static final int MODE_CAMERA = 2;
    private static final int MODE_QUIZ = 3;
    private static final int MODE_BATTERY = 4;
    private static final int MODE_CPU = 5;
    private static final int MODE_STORAGE = 6;
    private static final int GAME_INFO_COUNT = 10;

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

    private TextView mTvTime;
    private TextView mTvDate;

    private HSGifImageView buttonUpgrade;
    private View buttonSearch;
    private View buttonBoost;
    private View buttonGame;
    private View buttonCamera;
    private View buttonWeather;
    private TextView pushDialogButton;
    private ImageView pushDialogIconImageView;

    private boolean shouldShowButtonUpgrade;
    private boolean shouldShowButtonSearch;
    private boolean shouldShowCommonUseButtons; //Boost, Game, Camera, Weather

    private PremiumSearchDialog searchDialog;

    private int pushDialogIndex = 1;
    private int gameInfoPosition = 0;

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
                HSAnalytics.logEvent("new_screenLocker_feature_clicked", "entry", "search");
                searchDialog = new PremiumSearchDialog(getContext());
                searchDialog.setOnSearchListerner((searchDialog, searchText) -> {
                    String url = WebContentSearchManager.getInstance().queryText(searchText);
                    Intent intent = new Intent(getContext(), BrowserActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(BrowserActivity.SEARCH_URL_EXTRA, url);
                    intent.putExtra(BrowserActivity.SHOW_WHEN_LOCKED, quickLaunch);
                    context.startActivity(intent);
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
                HSAnalytics.logEvent("new_screenLocker_feature_clicked", "entry", "boost");
                Intent intent = new Intent(context, BoostPlusActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                HSGlobalNotificationCenter.sendNotification(PremiumLockerActivity.EVENT_FINISH_SELF);
            } else if (v.getId() == R.id.button_game) {
                HSAnalytics.logEvent("new_screenLocker_feature_clicked", "entry", "game");
                Intent intent = new Intent(context, SoftGameDisplayActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                HSGlobalNotificationCenter.sendNotification(PremiumLockerActivity.EVENT_FINISH_SELF);
            } else if (v.getId() == R.id.button_camera) {
                HSAnalytics.logEvent("new_screenLocker_feature_clicked", "entry", "camera");
                NavUtils.startCameraFromLockerScreen(context);
                HSBundle bundle = new HSBundle();
                bundle.putBoolean(PremiumLockerActivity.FINISH_WITHOUT_UNLOCK, true);
                HSGlobalNotificationCenter.sendNotification(PremiumLockerActivity.EVENT_FINISH_SELF, bundle);
            } else if (v.getId() == R.id.button_weather) {
                HSAnalytics.logEvent("new_screenLocker_feature_clicked", "entry", "weather");
                AcbWeatherManager.showWeatherInfo(context, false);
                HSGlobalNotificationCenter.sendNotification(PremiumLockerActivity.EVENT_FINISH_SELF);
            } else if (v.getId() == R.id.icon_locker_upgrade) {
                HSAnalytics.logEvent("new_screenLocker_feature_clicked", "entry", "upgrade");
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
        pushDialogButton = findViewById(R.id.push_dialog_button);
        pushDialogIconImageView = findViewById(R.id.icon);

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
                buttonUpgrade.clear();
                SharedPreferences.Editor editorOfDialog = getContext().getSharedPreferences("pushDialog", Context.MODE_PRIVATE).edit();
                editorOfDialog.putLong("time", System.currentTimeMillis());
                editorOfDialog.putInt("index", pushDialogIndex);
                editorOfDialog.apply();
                break;
            case ScreenStatusReceiver.NOTIFICATION_SCREEN_ON:
                // TODO: 2018/1/10 update list of games every week
                SharedPreferences preferencesOfGame = getContext().getSharedPreferences("gameInfo", Context.MODE_PRIVATE);
                if (preferencesOfGame.getAll() == null) {
                    gameInfoPosition++;
                    askForGameInfo(gameInfoPosition%GAME_INFO_COUNT);
                }

                if (!mShimmer.isAnimating()) {
                    mShimmer.start(mUnlockText);
                }
                buttonUpgrade.setImageResource(R.raw.upgrade_icon);
                int timeForUpdate = (int) (System.currentTimeMillis() - getContext().getSharedPreferences("pushDialog", Context.MODE_PRIVATE).getLong("time", 0)) / (60 * 1000);
                pushDialogIndex = getContext().getSharedPreferences("pushDialog", Context.MODE_PRIVATE).getInt("index", pushDialogIndex);
                if (timeForUpdate > 5) {
                    pushDialogIndex++;
                    int pushDialogCount = 7;
                    pushDialogIndex = pushDialogIndex% pushDialogCount;
                }
                switchPushDialog(pushDialogIndex);
                break;
            default:
                break;
        }
    }

    private void askForGameInfo(int position){
        SharedPreferences.Editor editorOfGame = getContext().getSharedPreferences("gameInfo", Context.MODE_PRIVATE).edit();
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
                    editorOfGame.putString("name", object.get("name"));
                    editorOfGame.putString("description", object.get("description"));
                    editorOfGame.putString("thumb", object.get("thumb"));
                    editorOfGame.putString("link", object.get("link"));
                    editorOfGame.apply();
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

    private void switchPushDialog(int pushDialogIndex) {
        switch (pushDialogIndex) {
            case MODE_JUNK:
                findViewById(R.id.title_for_boost).setVisibility(VISIBLE);
                findViewById(R.id.push_dialog_subtitle).setVisibility(VISIBLE);
                findViewById(R.id.game_and_cam_title).setVisibility(GONE);
                findViewById(R.id.quiz_head).setVisibility(GONE);
                findViewById(R.id.quiz_title).setVisibility(GONE);
                //垃圾清理
                break;
            case MODE_GAME:
                SharedPreferences preferencesOfGame = getContext().getSharedPreferences("gameInfo", Context.MODE_PRIVATE);
                if (preferencesOfGame.getAll().size() <= 0) {
                    gameInfoPosition++;
                    askForGameInfo(gameInfoPosition%GAME_INFO_COUNT);
                    this.pushDialogIndex++;
                    switchPushDialog(this.pushDialogIndex);
                } else {
                    findViewById(R.id.game_and_cam_title).setVisibility(VISIBLE);
                    findViewById(R.id.push_dialog_subtitle).setVisibility(VISIBLE);
                    findViewById(R.id.title_for_boost).setVisibility(GONE);
                    findViewById(R.id.quiz_head).setVisibility(GONE);
                    findViewById(R.id.quiz_title).setVisibility(GONE);

                    String iconUrl = preferencesOfGame.getString("thumb", "");
                    new Thread(() -> {
                        try {
                            URL urlOfImage = new URL(iconUrl);
                            Bitmap bitmap = BitmapFactory.decodeStream(urlOfImage.openConnection().getInputStream());
                            pushDialogIconImageView.setImageBitmap(bitmap);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }).start();
                    ((TextView)findViewById(R.id.game_and_cam_title)).setText(preferencesOfGame.getString("name", ""));
                    ((TextView)findViewById(R.id.push_dialog_subtitle)).setText(preferencesOfGame.getString("description", ""));
                    //todo：游戏的string
                    pushDialogButton.setText("youxi");
                }
                //游戏
                break;
            case MODE_CAMERA:
                List<Map<String, Object>> configs;
                NotificationBean bean = null;
                try {
                    configs = (List<Map<String, Object>>) HSConfig.getList("Application", "LocalNotifications", "Content");
                    bean = new NotificationBean(configs.get(0));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (bean == null) {
                    this.pushDialogIndex++;
                    switchPushDialog(this.pushDialogIndex);
                } else {
                    findViewById(R.id.game_and_cam_title).setVisibility(VISIBLE);
                    findViewById(R.id.push_dialog_subtitle).setVisibility(VISIBLE);
                    findViewById(R.id.title_for_boost).setVisibility(GONE);
                    findViewById(R.id.quiz_head).setVisibility(GONE);
                    findViewById(R.id.quiz_title).setVisibility(GONE);

                    ((TextView)findViewById(R.id.game_and_cam_title)).setText(bean.getName());
                    ((TextView)findViewById(R.id.push_dialog_subtitle)).setText(bean.getMessage());
                    pushDialogButton.setText(bean.getButtonText());

                    String imageUrl = bean.getIconUrl();
                    if (imageUrl == null) {
                        ((ImageView)findViewById(R.id.icon)).setImageResource(R.drawable.ic_locker_camera);
                    } else {
                        new Thread(() -> {
                            try {
                                URL urlOfImage = new URL(imageUrl);
                                Bitmap bitmap = BitmapFactory.decodeStream(urlOfImage.openConnection().getInputStream());
                                pushDialogIconImageView.setImageBitmap(bitmap);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }).start();
                    }
                }
                break;
            case MODE_QUIZ:
                findViewById(R.id.quiz_head).setVisibility(VISIBLE);
                findViewById(R.id.quiz_title).setVisibility(VISIBLE);
                findViewById(R.id.game_and_cam_title).setVisibility(GONE);
                findViewById(R.id.push_dialog_subtitle).setVisibility(GONE);
                findViewById(R.id.title_for_boost).setVisibility(GONE);
                //quiz
                break;
            case MODE_BATTERY:
                findViewById(R.id.title_for_boost).setVisibility(VISIBLE);
                findViewById(R.id.push_dialog_subtitle).setVisibility(VISIBLE);
                findViewById(R.id.game_and_cam_title).setVisibility(GONE);
                findViewById(R.id.quiz_head).setVisibility(GONE);
                findViewById(R.id.quiz_title).setVisibility(GONE);
                //电量
                break;
            case MODE_CPU:
                findViewById(R.id.title_for_boost).setVisibility(VISIBLE);
                findViewById(R.id.push_dialog_subtitle).setVisibility(VISIBLE);
                findViewById(R.id.game_and_cam_title).setVisibility(GONE);
                findViewById(R.id.quiz_head).setVisibility(GONE);
                findViewById(R.id.quiz_title).setVisibility(GONE);
                //CPU
                break;
            case MODE_STORAGE:
                findViewById(R.id.title_for_boost).setVisibility(VISIBLE);
                findViewById(R.id.push_dialog_subtitle).setVisibility(VISIBLE);
                findViewById(R.id.game_and_cam_title).setVisibility(GONE);
                findViewById(R.id.quiz_head).setVisibility(GONE);
                findViewById(R.id.quiz_title).setVisibility(GONE);
                //boost
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
