package com.ihs.feature.notification;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.AppCompatDrawableManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.widget.RemoteViews;

import com.artw.lockscreen.DismissKeyguradActivity;
import com.artw.lockscreen.LockerUtils;
import com.artw.lockscreen.common.NavUtils;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.feature.battery.BatteryActivity;
import com.ihs.feature.battery.BatteryUtils;
import com.ihs.feature.boost.BoostActivity;
import com.ihs.feature.boost.BoostIcon;
import com.ihs.feature.boost.RamUsageDisplayUpdater;
import com.ihs.feature.boost.plus.BoostPlusActivity;
import com.ihs.feature.common.ConcurrentUtils;
import com.ihs.feature.common.DeviceManager;
import com.ihs.feature.common.LauncherConstants;
import com.ihs.feature.common.Thunk;
import com.ihs.feature.cpucooler.CpuCoolDownActivity;
import com.ihs.feature.cpucooler.CpuCoolerManager;
import com.ihs.feature.cpucooler.util.CpuCoolerConstant;
import com.ihs.feature.cpucooler.util.CpuCoolerUtils;
import com.ihs.feature.junkclean.JunkCleanActivity;
import com.ihs.feature.junkclean.util.JunkCleanConstant;
import com.ihs.feature.junkclean.util.JunkCleanUtils;
import com.ihs.feature.resultpage.data.ResultConstants;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.CommonUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NotificationManager {

    //
    // IMPORTANT notice for maintainers
    //
    // Notification Toolbar could cause the following crash
    //
    //     Fatal Exception: android.app.RemoteServiceException:
    //     Bad notification posted from package com.honeycomb.launcher: Couldn't expand RemoteViews for:
    //     StatusBarNotification(pkg=com.honeycomb.launcher user=UserHandle{0} id=0 tag=null score=20:
    //     Notification(pri=2 contentView=com.honeycomb.launcher/0x7f040130 vibrate=null sound=null defaults=0x0 flags=0x2 kind=[null]))
    //
    // If any associated ID (view IDs, drawables, colors, strings, styles, etc.) changes when launcher is upgraded.
    //
    // If you were to add any new IDs to the notification toolbar layout or set any new drawables with
    // setVectorDrawableForImageView(), be sure to add these IDs to
    //
    // ids.xml
    // public.xml
    //
    // in order to pin their values across different release builds and thus to prevent the crash.
    //
    // See: http://stackoverflow.com/a/40170940/5113273
    //

    public static final String NOTIFICATION_PERFORM_BOOST = "should_start_boost";
    public static final String NOTIFICATION_PERFORM_SEARCH = "should_show_search";
    public static final String BUNDLE_KEY_BOOST_TYPE = "boost_type";

    public static final String TAG = NotificationManager.class.getSimpleName();

    private static final int TOOLBAR_NOTIFICATION_ID = 0;

    private static final int CLICK_DEBOUNCE_INTERVAL = 400;

    @ColorInt
    private static final int TRACK_COLOR = 0xff3f4043;

    private static final String ACTION_CONTENT = "action_content";
    private static final String ACTION_BOOST_TOOLBAR = "action_boost_toolbar";
    private static final String ACTION_BOOST_NOTIFICATION = "action_boost_notification";
    private static final String ACTION_BOOST_NOTIFICATION_DELETE = "action_boost_notification_delete";
    public static final String ACTION_WIFI_STATE_CHANGE = "action_wifi_state_change";
    private static final String ACTION_WIFI_CLICK = "action_wifi_click";
    private static final String ACTION_MOBILE_DATA = "action_data";
    static final String ACTION_MOBILE_DATA_CHANGE = "action_data_change";
    private static final String ACTION_FLASH_LIGHT = "action_flash_light";
    static final String ACTION_CPU_COOLER_TOOLBAR = "action_cpu_cooler_toolbar";
    static final String ACTION_CPU_COOLER = "action_cpu_cooler";
    private static final String ACTION_ALL_APPS = "action_all_apps";
    private static final String ACTION_TOOLBAR_SETTINGS = "action_toolbar_settings";
    static final String ACTION_WEATHER = "action_weather";
    static final String ACTION_WEATHER_DELETE = "action_weather_delete";
    public static final String ACTION_THEME = "action_theme";
    public static final String ACTION_THEME_DELETE = "action_theme_delete";
    public static final String ACTION_SET_AS_DEFAULT = "action_set_as_default";
    public static final String ACTION_SET_AS_HOME_DELETE = "action_set_as_default_delete";
    public static final String ACTION_CLEAR_DEFAULT_DELETE = "action_clear_default_delete";
    private static final String ACTION_LONGTIME_NO_CLEAR = "action_boost_plus_longtime_no_clear";
    static final String ACTION_BOOST_PLUS = "action_boost_plus";
    static final String ACTION_BATTERY_OPTIMIZE = "action_battery_optimize";
    static final String ACTION_BATTERY_OPTIMIZE_DELETE = "action_battery_optimize_delete";
    private static final String ACTION_BATTERY_TOOLBAR = "action_battery_toolbar";
    static final String ACTION_JUNK_CLEAN = "action_junk_clean";
    public static final String ACTION_JUNK_CLEAN_BADGE = "action_junk_clean_badge";
    public static final String ACTION_NOTIFICATION_CLEANER = "action_notification_cleaner";

    static final String WEATHER = "Weather";
    static final String CPU_COOLER = "cpu_cooler";
    public static final String SET_AS_HOME = "SetAsHome";
    public static final String CLEAR_DEFAULT = "ClearDefault";
    public static final String THEME = "Theme";

    public static final long JUNK_CLEAN_BADGE_ALARM_INTERVAL = 24 * 60 * 60 * 1000; // 24h

    public static final String EXTRA_SET_AS_DEFAULT_TYPE = "set_as_default_type";
    public static final int EXTRA_VALUE_SET_AS_DEFAULT_SET = 0;
    public static final int EXTRA_VALUE_SET_AS_DEFAULT_CLEAR = 1;

    private static final String EXTRA_BOOST_TYPE = "boost_type";
    private static final String EXTRA_BOOST_SOURCE = "boost_source";
    static final String EXTRA_NOTIFICATION_ID = "notification_id";
    static final String EXTRA_NOTIFICATION_TYPE = "notification_type";
    private static final String EXTRA_NOTIFICATION_TIME_OUT_CLEAN = "notification_time_out_clean";

    private static final int EXTRA_VALUE_BOOST_SOURCE_TOOLBAR = 0;
    public static final int EXTRA_VALUE_BOOST_SOURCE_NOTIFICATION = 1;

    public static final String EXTRA_THEME_PACKAGE = "theme_package";

    public static final String EXTRA_AUTO_COLLAPSE = "auto_collapse";

    private static final int NOTIFICATION_TOOLBAR_ICON_BITMAP_SIZE = CommonUtils.pxFromDp(30);

    private volatile static NotificationManager sInstance;

    private Locale mNotificationToolbarLocale;

    private android.app.NotificationManager mNotificationManager;
    private RamUsageDisplayUpdater mRamUsageDisplayUpdater;
    private RemoteViews mRemoteViews;
    private Notification mNotificationToolbar;
    private Runnable mTurnOnRunnable;
    private Runnable mTurnOffRunnable;
    private boolean mRepeatCycle;
    private WifiDisplayState mDisplayState = WifiDisplayState.DISABLED;
    private String mWifiString = "Wifi";

    private SparseArray<Bitmap> mPreLollipopIconHolder;
    private Bitmap mBoostIcon;
    private Canvas mBoostIconCanvas;
    private Paint mClearPaint;

    private Map<String, Long> mLastClickMap = new HashMap<>(6);
    private int lastClearDefaultNotificationId;
    private volatile int mCpuTemperature;
    private int mCpuCoolerDrawableId;
    private volatile int mBatteryLevel;
    private int mBatteryDrawableId;
    private Handler mHandler = new Handler();
    private NotificationCondition mNotificationCondition;

    @SuppressWarnings("WeakerAccess")
    @Thunk
    enum WifiDisplayState {
        ENABLED,
        ENABLING,
        DISABLED,
    }

    /**
     * {@link NotificationManager} can only be instantiated on the UI thread as it needs to interact with
     * flashlight manager, who may creates surfaces views on instantiation.
     */
    public static NotificationManager getInstance() {
        if (sInstance == null) {
            synchronized (NotificationManager.class) {
                if (sInstance == null) {
                    sInstance = new NotificationManager();
                }
            }
        }
        return sInstance;
    }

    private NotificationManager() {
        if (!CommonUtils.ATLEAST_LOLLIPOP) {
            mPreLollipopIconHolder = new SparseArray<>(16);
        }
        mNotificationCondition = new NotificationCondition(HSApplication.getContext());
    }

    private RamUsageDisplayUpdater.RamUsageChangeListener mRamUsageChangeListener = new RamUsageDisplayUpdater.RamUsageChangeListener() {
        @Override
        public void onDisplayedRamUsageChange(int displayedRamUsage, boolean isImmediatelyUpdate) {
            HSLog.d("ToolBar.Boost", "onDisplayedRamUsageChange usage = " + displayedRamUsage + " isImmediatelyUpdate = " + isImmediatelyUpdate);
            updateBoostIcon(displayedRamUsage);
        }

        @Override
        public void onBoostComplete(int afterBoostRamUsage) {
            HSLog.d("ToolBar.Boost", "onBoostComplete usage = " + afterBoostRamUsage);
            updateBoostIcon(afterBoostRamUsage);
        }
    };

    public void hideNotificationToolbar() {
        if (mNotificationManager == null) {
            Context context = HSApplication.getContext();
            mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        mNotificationManager.cancel(TOOLBAR_NOTIFICATION_ID);

        // Flush the bitmap cache when we hide notification toolbar as it is the primary user of the cache and we want
        // to keep memory usage low. But note that other notifications (see sendDefaultStyleNotification()) use the
        // cache as well.
        if (mPreLollipopIconHolder != null) {
            mPreLollipopIconHolder.clear();
        }
    }

    private void initIfNeeded(Context context) {
        Locale newLocale = CommonUtils.getLocale(context);
        if (mNotificationToolbarLocale != null && !mNotificationToolbarLocale.equals(newLocale)) {
            // Re-initialize for locale change
            HSLog.i(TAG + ".Locale", "Detected locale change from " + mNotificationToolbarLocale + " to " + newLocale);
            mRemoteViews = null;
            mNotificationToolbar = null;
            hideNotificationToolbar(); // Notification must be cancelled and re-sent to ensure its UI update
        }
        mNotificationToolbarLocale = newLocale;
        initIfNeededLocaleChecked(context);
    }

    private void initIfNeededLocaleChecked(Context context) {
        if (mNotificationManager == null) {
            mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }


        if (mRamUsageDisplayUpdater == null) {
            mRamUsageDisplayUpdater = RamUsageDisplayUpdater.getInstance();
            mRamUsageDisplayUpdater.addRamUsageChangeListener(mRamUsageChangeListener);
        }

        boolean isForceUpdate = (mRemoteViews == null);

        if (mRemoteViews == null) {
            mRemoteViews = new RemoteViews(LauncherConstants.LAUNCHER_PACKAGE_NAME, R.layout.notification_toolbar);
            mRemoteViews.setOnClickPendingIntent(R.id.boost_layout, getPendingIntent(ACTION_BOOST_TOOLBAR, true));
            mRemoteViews.setOnClickPendingIntent(R.id.wifi_layout, getPendingIntent(ACTION_WIFI_CLICK, false));
            mRemoteViews.setOnClickPendingIntent(R.id.data_layout,
                    getPendingIntent(ACTION_MOBILE_DATA, CommonUtils.ATLEAST_LOLLIPOP));
            mRemoteViews.setOnClickPendingIntent(R.id.flashlight_layout, getPendingIntent(ACTION_FLASH_LIGHT, false));
            mRemoteViews.setOnClickPendingIntent(R.id.cpu_cooler_layout, getPendingIntent(ACTION_CPU_COOLER_TOOLBAR, true));
//            mRemoteViews.setOnClickPendingIntent(R.id.all_apps_layout, getPendingIntent(ACTION_ALL_APPS, true));
            mRemoteViews.setOnClickPendingIntent(R.id.battery_layout, getPendingIntent(ACTION_BATTERY_TOOLBAR, true));
            mRemoteViews.setOnClickPendingIntent(R.id.toolbar_settings_layout, getPendingIntent(ACTION_TOOLBAR_SETTINGS, true));
            HSLog.d("Wifi.Alert ", "init");
            mWifiString = context.getResources().getString(R.string.notification_toolbar_wifi);

            // cpu cooler
            initCpuCoolerView(true, isForceUpdate);

//            setVectorDrawableForImageView(mRemoteViews, R.id.iv_all_apps, R.drawable.notification_toolbar_allapps_svg);
            mBoostIcon = Bitmap.createBitmap(CommonUtils.pxFromDp(NOTIFICATION_TOOLBAR_ICON_BITMAP_SIZE),
                    CommonUtils.pxFromDp(NOTIFICATION_TOOLBAR_ICON_BITMAP_SIZE), Bitmap.Config.ARGB_4444);
            mBoostIconCanvas = new Canvas(mBoostIcon);
            mClearPaint = new Paint();
            mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        } else {
            initCpuCoolerView(false, isForceUpdate);
        }
        mRemoteViews.setImageViewBitmap(R.id.iv_boost, getBoostIcon(mRamUsageDisplayUpdater.getDisplayedRamUsage() / 100f));
//        mRemoteViews.setImageViewBitmap(R.id.iv_battery, BatteryIconDrawable.getToolbarBatteryBitmap(isUserEnterBatteryManagerRecent()));
        initBatteryView(isForceUpdate);

        if (mNotificationToolbar == null) {
            mNotificationToolbar = new NotificationCompat.Builder(context)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_MAX - 1)
                    .setSmallIcon(R.drawable.notification_toolbar_small_icon)
                    .setContent(mRemoteViews)
                    .setContentIntent(getPendingIntent(ACTION_CONTENT, true))
                    .build();
        }
//
//        if (mTurnOnRunnable == null) {
//            mTurnOnRunnable = new Runnable() {
//                @Override
//                public void run() {
//                    setVectorDrawableForImageView(mRemoteViews, R.id.iv_flash_light, R.drawable.notification_toolbar_flashlight_on_svg);
////                    showNotificationToolbarIfEnabled();
//                }
//            };
//        }
//
//        if (mTurnOffRunnable == null) {
//            mTurnOffRunnable = new Runnable() {
//                @Override
//                public void run() {
//                    setVectorDrawableForImageView(mRemoteViews, R.id.iv_flash_light, R.drawable.notification_toolbar_flashlight_off_svg);
////                    showNotificationToolbarIfEnabled();
//                }
//            };
//        }

//        if (mWifiEnableAnim == null) {
//            mWifiEnableAnim = new WifiEnableAnim();
//        }
    }

    private void initCpuCoolerView(boolean isFetchCpuTemperature, boolean isForceUpdateIcon) {
        if (null == mRemoteViews) {
            return;
        }
        if (isFetchCpuTemperature) {
            mCpuTemperature = CpuCoolerManager.getInstance().fetchCpuTemperature();
        }
        int cpuResId = CpuCoolerManager.getInstance().getDrawableId(mCpuTemperature);
        HSLog.d(TAG, "Notification temperature = " + mCpuTemperature + " isFetchCpuTemperature = " + isFetchCpuTemperature + " mCpuCoolerDrawableId = " + mCpuCoolerDrawableId + " cpuResId = " + cpuResId + " isForceUpdateIcon = " + isForceUpdateIcon);
        if (mCpuCoolerDrawableId != cpuResId || isForceUpdateIcon) {
            mCpuCoolerDrawableId = cpuResId;
            setVectorDrawableForImageView(mRemoteViews, R.id.iv_cpu_cooler, mCpuCoolerDrawableId);
        }

        String contentText = mCpuTemperature + HSApplication.getContext().getString(R.string.cpu_cooler_temperature_quantifier_celsius);
        SpannableString contentSpannable = new SpannableString(contentText);
        contentSpannable.setSpan(new ForegroundColorSpan(CpuCoolerManager.getInstance().getColor(mCpuTemperature)), 0, contentText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        mRemoteViews.setTextViewText(R.id.tv_cpu_temperature, contentSpannable);
    }

    private void initBatteryView(boolean isForceUpdateIcon) {
        if (null == mRemoteViews) {
            return;
        }
        int batteryLv = DeviceManager.getInstance().getBatteryLevel();
        if (mBatteryLevel != batteryLv || isForceUpdateIcon) {
            mBatteryLevel = batteryLv;
            mRemoteViews.setTextViewText(R.id.tv_battery_level, String.valueOf(batteryLv + "%"));
        }
        int resId = BatteryUtils.getToolbarBatteryResId(batteryLv, BatteryUtils.hasUserUsedBatteryRecently(5 * DateUtils.MINUTE_IN_MILLIS));
        HSLog.d(TAG, "Notification initBatteryView mBatteryLevel = " + mBatteryLevel + " batteryLv = " + batteryLv + " mBatteryDrawableId = " + mBatteryDrawableId + " resId = " + resId + " isForceUpdateIcon = " + isForceUpdateIcon);
        if (mBatteryDrawableId != resId || isForceUpdateIcon) {
            mBatteryDrawableId = resId;
            mRemoteViews.setImageViewResource(R.id.iv_battery, resId);
        }
    }

    public boolean isWifiDisplayEnabled() {
        return mDisplayState == WifiDisplayState.ENABLED;
    }

    private void handleLockerToolbarClick(final Context context, @NonNull final Runnable task) {
        if (LockerUtils.isKeyguardLocked(context, true)) {
            DismissKeyguradActivity.startSelfIfKeyguardSecure(context);
            mHandler.postDelayed(task, 2000);
        } else {
            task.run();
        }
    }

    void handleEventAndCollapse(final Context context, final Intent intent) {
        // Collapse notification bar
        Intent collapseIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(collapseIntent);

        // Notification set alarm to remove self when user not handle it long time.
        boolean timeoutSelfClean = intent.getBooleanExtra(EXTRA_NOTIFICATION_TIME_OUT_CLEAN, false);
        if (timeoutSelfClean) {
            cancelTimeoutCleanAlarm(intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0));
        }

        HSLog.d("ToolBar.Click", "Click action = " + intent.getAction());
        switch (intent.getAction()) {
            case ACTION_BOOST_TOOLBAR:
            case ACTION_BOOST_NOTIFICATION:
                final Runnable boost = new Runnable() {
                    @Override
                    public void run() {
                        int typeOrdinal = intent.getIntExtra(NotificationManager.EXTRA_BOOST_TYPE, 0);
                        int source = intent.getIntExtra(NotificationManager.EXTRA_BOOST_SOURCE,
                                NotificationManager.EXTRA_VALUE_BOOST_SOURCE_TOOLBAR);
                        if (source == NotificationManager.EXTRA_VALUE_BOOST_SOURCE_TOOLBAR) {
                            HSAnalytics.logEvent("Notification_Toolbar_Icon_Clicked", "type", "Boost");
                            HSAnalytics.logEvent("Notification_Toolbar_Boost_Clicked");
                        }
                        Intent boostIntent = new Intent(context, BoostActivity.class);
                        intent.putExtra(BoostActivity.INTENT_KEY_BOOST_TYPE, typeOrdinal);
                        NavUtils.startActivitySafely(context, boostIntent);
                    }
                };
                handleLockerToolbarClick(context, boost);
                break;
            case ACTION_BOOST_NOTIFICATION_DELETE:
                int typeOrdinal = intent.getIntExtra(NotificationManager.EXTRA_BOOST_TYPE, 0);
                break;
            case ACTION_WEATHER:
                HSAnalytics.logEvent("Notification_Clicked", "Type", WEATHER, "time",
                        String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
                break;
            case ACTION_WEATHER_DELETE:
                break;
            case ACTION_MOBILE_DATA:
                handleLockerToolbarClick(context, new Runnable() {
                    @Override
                    public void run() {
                        NavUtils.startSystemDataUsageSetting(context, true);
                    }
                });
                break;
            case ACTION_CPU_COOLER:
                logNotificationClicked(CPU_COOLER);
                HSAnalytics.logEvent("CPUCooler_Open", "Type", "Notification");
                mHandler.postDelayed(() -> {
                    Intent cpuCoolerIntent = new Intent(context, CpuCoolDownActivity.class);
                    cpuCoolerIntent.putExtra(CpuCoolDownActivity.EXTRA_KEY_NEED_SCAN, true);
                    cpuCoolerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    NavUtils.startActivitySafely(context, cpuCoolerIntent);
                }, 500);
                break;
            case ACTION_CPU_COOLER_TOOLBAR:
                final Runnable cpu = new Runnable() {
                    @Override
                    public void run() {
                        HSAnalytics.logEvent("Notification_Toolbar_CPU_Clicked", "Type", CpuCoolerUtils.getTemperatureColorText(mCpuTemperature));
                        HSAnalytics.logEvent("CPUCooler_Open", "Type", "Toolbar");
                        Intent cpuCoolerIntent = new Intent(context, CpuCoolDownActivity.class);
                        cpuCoolerIntent.putExtra(CpuCoolDownActivity.EXTRA_KEY_NEED_SCAN, true);
                        cpuCoolerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        NavUtils.startActivitySafely(context, cpuCoolerIntent);
                    }
                };

                handleLockerToolbarClick(context, cpu);
                break;
            case ACTION_BATTERY_TOOLBAR:
                final Runnable battery = new Runnable() {
                    @Override
                    public void run() {
                        HSAnalytics.logEvent("Notification_Toolbar_Battery_Clicked");
                        HSAnalytics.logEvent("Battery_OpenFrom", "type", "From Toolbar");
                        Intent intentBattery = new Intent(context, BatteryActivity.class);
                        NavUtils.startActivitySafely(context, intentBattery);
                    }
                };
                handleLockerToolbarClick(context, battery);
                break;
            case ACTION_BOOST_PLUS:
                Intent boostPlusIntent = new Intent(context, BoostPlusActivity.class);
                boostPlusIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                NavUtils.startActivitySafely(context, boostPlusIntent);
                String boostType = intent.getStringExtra(EXTRA_NOTIFICATION_TYPE);
                logNotificationClicked(boostType);
                HSAnalytics.logEvent("BoostPlus_Open", "Type", "Notification");
                break;
            case ACTION_BATTERY_OPTIMIZE:
                Intent batteryIntent = new Intent(context, BatteryActivity.class);
                NavUtils.startActivitySafely(context, batteryIntent);
                String batteryType = intent.getStringExtra(EXTRA_NOTIFICATION_TYPE);
                logNotificationClicked(batteryType);
                HSAnalytics.logEvent("Battery_OpenFrom", "type", "From Notifications");
                break;
            case ACTION_CONTENT:
                // Used for content intent. Do nothing
                HSLog.d(TAG, "Click content");
                break;
            case ACTION_JUNK_CLEAN:
                logNotificationClicked(ResultConstants.JUNK_CLEANER);
                JunkCleanUtils.FlurryLogger.logOpen(JunkCleanConstant.NOTIFICATION);
                Intent junkCleanAnimationIntent = new Intent(context, JunkCleanActivity.class);
                junkCleanAnimationIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                NavUtils.startActivitySafely(context, junkCleanAnimationIntent);
                break;
            default:
                HSLog.w(TAG, "Unsupported action");
        }
    }

    public void handleEvent(Context context, Intent intent) {
        String action = intent.getAction();
        HSLog.d("ToolBar.Click", "Click action = " + action);
        long currentTimeMillis = System.currentTimeMillis();
        Long lastClickTime = mLastClickMap.get(action);
        mLastClickMap.put(action, currentTimeMillis);
        if (lastClickTime == null) {
            lastClickTime = 0L;
        }
        if (currentTimeMillis - lastClickTime < CLICK_DEBOUNCE_INTERVAL && !TextUtils.equals(action,
                ACTION_WIFI_STATE_CHANGE) && !TextUtils.equals(action, ACTION_MOBILE_DATA_CHANGE)) {
            // In case of fast double click
            HSLog.d("ToolBar.Click", "fast double click, action = " + action);
            return;
        }
        switch (action) {
            case NotificationManager.ACTION_SET_AS_HOME_DELETE:
                break;
            case NotificationManager.ACTION_CLEAR_DEFAULT_DELETE:
                lastClearDefaultNotificationId = 0;
                break;
            case NotificationManager.ACTION_BATTERY_OPTIMIZE_DELETE:
                break;
            case NotificationManager.ACTION_SET_AS_DEFAULT:
                lastClearDefaultNotificationId = 0;
                break;
            case ACTION_LONGTIME_NO_CLEAR:
                int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0);
                HSLog.d(TAG, "Action boost plus long time no clear nId = " + notificationId);
                if (notificationId != 0) {
                    cancelSafely(notificationId);
                    cancelTimeoutCleanAlarm(notificationId);
                }
                break;
            default:
                break;
        }
    }


    @SuppressWarnings({"WeakerAccess", "UnusedParameters"})
    @Thunk
    void updateBoostIcon(int ramUsage) {
        if (mNotificationManager != null && mRemoteViews != null && mNotificationToolbar != null) {
//            showNotificationToolbarIfEnabled();
        }
    }

    public void updateCpuCooler() {
        if (mNotificationManager != null && mRemoteViews != null && mNotificationToolbar != null) {
            mCpuTemperature = CpuCoolerManager.getInstance().fetchCpuTemperature();
            HSLog.d(TAG, "updateCpuCooler mCpuTemperature = " + mCpuTemperature);
            initCpuCoolerView(false, false);
        }
    }

    public void updateCpuCooler(int cpuTemperature) {
        if (mNotificationManager != null && mRemoteViews != null && mNotificationToolbar != null) {
            mCpuTemperature = cpuTemperature;
            HSLog.d(TAG, "updateCpuCooler cpuTemperature = " + cpuTemperature);
            initCpuCoolerView(false, false);
        }
    }

    public void updateCpuCoolerCoolDown(int coolDownCpuTemperature) {
        if (mNotificationManager != null && mRemoteViews != null && mNotificationToolbar != null) {
            if (0 != mCpuTemperature && mCpuTemperature > coolDownCpuTemperature) {
                mCpuTemperature -= coolDownCpuTemperature;
            }
            HSLog.d(TAG, "updateCpuCoolerCoolDown mCpuTemperature = " + mCpuTemperature);
//            showNotificationToolbarIfEnabled();
        }
    }

    public void updateBattery() {
        if (mNotificationManager != null && mRemoteViews != null && mNotificationToolbar != null) {
            HSLog.d(TAG, "updateBattery");
//            showNotificationToolbarIfEnabled();
        }
    }

    public void autoUpdateCpuCoolerTemperature() {
        HSLog.d(TAG, "Notification autoUpdateCpuCoolerTemperature");
        mHandler.postDelayed(this::updateCpuCooler, CpuCoolerConstant.FROZEN_CPU_COOLER_SECOND_TIME * 500); // 30s, 1/2 FROZEN_CPU_COOLER_SECOND_TIME
    }

    public PendingIntent getPendingIntent(String action, boolean autoCollapse) {
        return getPendingIntent(action, autoCollapse, null);
    }

    public interface ExtraProvider {
        void onAddExtras(Intent intent);
    }

    public PendingIntent getPendingIntent(String action, boolean autoCollapse, ExtraProvider extras) {
        Context context = HSApplication.getContext();
        int requestCode = (int) System.currentTimeMillis();
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(EXTRA_AUTO_COLLAPSE, autoCollapse);
        if (extras != null) {
            extras.onAddExtras(intent);
        }
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Bitmap getBoostIcon(float percentage) {
        mBoostIconCanvas.drawPaint(mClearPaint);
        float centX = mBoostIcon.getWidth() / 2.0f;
        float centY = mBoostIcon.getHeight() / 2.0f;
        float radius = centX > centY ? centY : centX;
        radius -= CommonUtils.pxFromDp(4f);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        mBoostIconCanvas.drawCircle(centX, centY, radius, paint);
        paint.setColor(TRACK_COLOR);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(radius * 0.3f);
        float sweepAngle = 360 * percentage;
        RectF rectF = new RectF(centX - radius * 0.7f, centY - radius * 0.7f, centX + radius * 0.7f, centY + radius * 0.7f);
        mBoostIconCanvas.drawArc(rectF, 0, 360, false, paint);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(BoostIcon.getProgressColor((int) (percentage * 100)));
        mBoostIconCanvas.drawArc(rectF, -90, sweepAngle, false, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        mBoostIconCanvas.drawCircle(centX, centY - radius * 0.7f, radius * 0.13f, paint);
        return mBoostIcon;
    }


    @SuppressWarnings({"WeakerAccess", "RestrictedApi"})
    @Thunk
    void setVectorDrawableForImageView(RemoteViews remoteViews, int viewId, int drawableId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            remoteViews.setImageViewResource(viewId, drawableId);
        } else {
            Drawable drawable;
            try {
                drawable = AppCompatDrawableManager.get().getDrawable(HSApplication.getContext(), drawableId);
            } catch (Exception e) {
                drawable = ContextCompat.getDrawable(HSApplication.getContext(), R.drawable.empty);
            }
            Bitmap bitmap = getBitmapForIcon(viewId);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            remoteViews.setImageViewBitmap(viewId, bitmap);
        }
    }

    private Bitmap getBitmapForIcon(int viewId) {
        Bitmap bitmap = mPreLollipopIconHolder.get(viewId);
        if (bitmap == null) {
            // Generate one and only one bitmap per view ID
            bitmap = Bitmap.createBitmap(NOTIFICATION_TOOLBAR_ICON_BITMAP_SIZE,
                    NOTIFICATION_TOOLBAR_ICON_BITMAP_SIZE, Bitmap.Config.ARGB_8888);
            mPreLollipopIconHolder.put(viewId, bitmap);
        }
        return bitmap;
    }

    public void showNotificationIfNeeded(int type) {
        mNotificationCondition.sendNotification(type);
    }

    /**
     * 所有的 notification 通知都要调用这个方法来发送
     *
     * @param id
     * @param notification
     */
    public void notify(final int id, final Notification notification) {
        notify(id, id, notification);
    }

    public void notify(final int id, final int type, final Notification notification) {
        if (id == NotificationCondition.NOTIFICATION_ID_CLEAR_DEFAULT) {
            lastClearDefaultNotificationId = NotificationCondition.NOTIFICATION_ID_CLEAR_DEFAULT;
        }

        if (id != TOOLBAR_NOTIFICATION_ID) {
            mNotificationCondition.recordNotification(id, type, notification.when);
        }

        Runnable notifyRunnable = new Runnable() {
            @Override
            public void run() {
                android.app.NotificationManager notifyMgr = (android.app.NotificationManager)
                        HSApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                try {
                    HSLog.d(TAG, "notify()");
                    notifyMgr.notify(id, notification);
                } catch (Exception e) {
                }
            }
        };

        ConcurrentUtils.postOnSingleThreadExecutor(notifyRunnable); // Keep notifications in original order
    }

    public boolean cancelSafely(int id) {
        android.app.NotificationManager notifyMgr = (android.app.NotificationManager)
                HSApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        try {
            notifyMgr.cancel(id);
            if (id == NotificationCondition.NOTIFICATION_ID_CLEAR_DEFAULT) {
                lastClearDefaultNotificationId = 0;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean hasDefaultClearedNotification() {
        boolean ret = lastClearDefaultNotificationId == NotificationCondition.NOTIFICATION_ID_CLEAR_DEFAULT;
        HSLog.e("hasClearNotification ret == " + ret);
        return ret;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    void setTimeoutCleanAlarm(int notificationId, long intervalMilliSecond) {
        HSLog.d(TAG, "*** setLongTimeNoScanAlarm *** intervalDay = " + intervalMilliSecond);
        Context context = HSApplication.getContext();

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(ACTION_LONGTIME_NO_CLEAR);
        intent.putExtra(EXTRA_AUTO_COLLAPSE, false);
        intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        // For identity purpose
        intent.setData(createUriForNotification(notificationId));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        long currentTime = System.currentTimeMillis() + intervalMilliSecond;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
        am.setRepeating(AlarmManager.RTC_WAKEUP, currentTime, intervalMilliSecond, pendingIntent);
    }

    private Uri createUriForNotification(int notificationId) {
        return Uri.fromParts("launcher_notification", String.valueOf(notificationId), null);
    }

    private void cancelTimeoutCleanAlarm(int notificationId) {
        HSLog.d(TAG, "cancelLongTimeNoScanAlarm");
        Context context = HSApplication.getContext();
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(ACTION_LONGTIME_NO_CLEAR);
        intent.setData(createUriForNotification(notificationId));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }

    public void setTimeAlarm(String action, long intervalMilliSecond) {
        HSLog.d(TAG, "*** setTimeAlarm *** action = " + action + " intervalDay = " + intervalMilliSecond);
        Context context = HSApplication.getContext();

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        long currentTime = System.currentTimeMillis() + intervalMilliSecond;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
        am.setRepeating(AlarmManager.RTC_WAKEUP, currentTime, intervalMilliSecond, pendingIntent);
    }

    private void cancelTimeAlarm(String action) {
        HSLog.d(TAG, "*** cancelTimeAlarm ***");
        Context context = HSApplication.getContext();
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }


    public static void logNotificationPushed(String type) {
        HSAnalytics.logEvent("Notification_Pushed", "Type", type);
    }

    public static void logNotificationClicked(String type) {
        HSAnalytics.logEvent("Notification_Clicked", "Type", type);
    }

    public void checkNextNotification() {
        mNotificationCondition.trySendNotificationInOrder();
    }
}
