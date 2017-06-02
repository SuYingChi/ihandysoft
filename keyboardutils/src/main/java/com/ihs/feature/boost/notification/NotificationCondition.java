package com.ihs.feature.boost.notification;

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.widget.RemoteViews;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.feature.boost.BoostTipUtils;
import com.ihs.feature.common.FormatSizeBuilder;
import com.ihs.feature.common.LauncherConstants;
import com.ihs.feature.common.LauncherFiles;
import com.ihs.feature.common.PreferenceHelper;
import com.ihs.feature.common.Utils;
import com.ihs.feature.common.WeatherSettings;
import com.ihs.keyboardutils.BuildConfig;
import com.ihs.keyboardutils.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhewang on 20/04/2017.
 */

public class NotificationCondition implements INotificationObserver {
    public static final String TAG = "NotificationCondition";
    private static final String NOTIFICATION_HISTORY = "NOTIFICATION_HISTORY";
    public static final String EVENT_UNLOCK = "locker_event_unlock";


    private static final String PREF_KEY_BOOST_PLUS_LAST_NOTIFICATION_A_TIME = "boost_plus_last_notification_time";
    private static final String PREF_KEY_BOOST_PLUS_LAST_NOTIFICATION_B_TIME = "boost_plus_last_notification_b_time";

    public static final String NOTIFICATION_CHECK_DONE = "notification_check_done";
    public static final String KEY_NOTIFICATION_TYPE = "key_notification_type";

    @SuppressWarnings("PointlessBooleanExpression")
    private static final boolean DEBUG_BOOST_PLUS_NOTIFICATION = false && BuildConfig.DEBUG;

    private static final int NOTIFICATION_ID_BOOST_PLUS = 10005;

    private static final int CHECK_STATE_START = -1;
    private static final int NOTIFICATION_TYPE_BOOST_PLUS = NOTIFICATION_ID_BOOST_PLUS;
    private static final int CHECK_STATE_DONE = -2;

    private static final int EVENT_CHECK_NEXT_NOTIFICATION = 100;

    // 等待通知检查条件时间，超时认为失败检查下一条。
    // 比如 boost+ 或者 junk clean 扫描用时。这里目前没做处理，所以最好是时间足够长，保证能完成扫描。
    private static final long CHECK_NOTIFICATION_TIMEOUT     = DateUtils.MINUTE_IN_MILLIS;
    // 没有打开相应功能模块的时间
    public static final long NOT_OPEN_FEATURE_INTERVAL       = 30 * DateUtils.MINUTE_IN_MILLIS;
    // 两条消息之间的时间间隔
    private static final long CHECK_NOTIFICATION_INTERVAL    = DateUtils.HOUR_IN_MILLIS;
    // 亮屏之后到检查通知的时间
    private static final long AFTER_SCREEN_ON_TIME           = DateUtils.MINUTE_IN_MILLIS;
    // 同一类型的消息的时间间隔 (需求为 24 小时 1 条)
    private static final long SAME_NOTIFICATION_INTERVAL     = DateUtils.DAY_IN_MILLIS;
    // 每天最多通知条数 (需求为 24 小时 1 条)
    private static final int NOTIFICATION_LIMIT_IN_DAY = HSConfig.optInteger(6, "Application", "NotificationPushedNumber");

    private static final int CPU_ALERT_TEMPERATURE = 40;
    private static final int LOW_BATTERY = 30;
    private static final int JUNK_CLEAN_NOTIFICATION_SIZE = 80 * 1024 * 1024;

    private Context context;
    private List<NotificationHolder> notificationHolderList;
    private int checkState = CHECK_STATE_DONE;
    private int runningApps = -1;
    private long lastScreenOnTime;
    private NotificationHolder lastHolder;
    private boolean isUnlock = false;

    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case EVENT_CHECK_NEXT_NOTIFICATION:
                    checkNextNotification();
                    break;
                default:
                    break;

            }
        }
    };

    NotificationCondition(Context context) {
        this.context = context;
        notificationHolderList = new ArrayList<>(6);
        readFromPref();
        HSGlobalNotificationCenter.addObserver(ScreenStatusReceiver.NOTIFICATION_SCREEN_ON, this);
        HSGlobalNotificationCenter.addObserver(ScreenStatusReceiver.NOTIFICATION_SCREEN_OFF, this);
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_CHECK_DONE, this);
        HSGlobalNotificationCenter.addObserver(EVENT_UNLOCK, this);
    }

    private void trySendNotificationInOrder() {
        if (checkState != CHECK_STATE_DONE) {
            return;
        }
        runningApps = -1;
        DeviceUtils.getRunningPackageListFromMemory(false, new DeviceUtils.RunningAppsListener() {
            @Override
            public void onScanFinished(int appSize) {
                HSLog.d(TAG, "onScanFinished appSize == " + appSize);
                runningApps = appSize;
                if (mHandler.hasMessages(EVENT_CHECK_NEXT_NOTIFICATION)) {
                    if (checkState == NOTIFICATION_TYPE_BATTERY || checkState == NOTIFICATION_TYPE_BOOST_PLUS) {
                        mHandler.removeMessages(EVENT_CHECK_NEXT_NOTIFICATION);
                        trySendNotification();
                    }
                }
            }
        });

        checkState = CHECK_STATE_START;
        checkNextNotification();
    }

    void sendNotification(int type) {
        trySendNotification(type);
    }

    @Override protected void finalize() throws Throwable {
        super.finalize();
        HSGlobalNotificationCenter.removeObserver(this);
    }

    @Override public void onReceive(String s, HSBundle hsBundle) {
        HSLog.d(TAG, "onReceive s == " + s);
        if (TextUtils.equals(s, ScreenStatusReceiver.NOTIFICATION_SCREEN_OFF)) {
            isUnlock = false;
        } else if (TextUtils.equals(s, ScreenStatusReceiver.NOTIFICATION_SCREEN_ON)) {
            mHandler.postDelayed(new Runnable() {
                @Override public void run() {
                    sendNotificationIfNeeded();
                }
            }, AFTER_SCREEN_ON_TIME);
        } else if (TextUtils.equals(s, NOTIFICATION_CHECK_DONE)) {
            if (hsBundle != null) {
                int type = hsBundle.getInt(KEY_NOTIFICATION_TYPE);
                if (checkState != CHECK_STATE_DONE && checkState == type) {
                    mHandler.removeMessages(EVENT_CHECK_NEXT_NOTIFICATION);
                    checkNextNotification();
                }
            }
        } else if (TextUtils.equals(s, EVENT_UNLOCK)) {
            isUnlock = true;
        }
    }

    private void checkNextNotification() {
        switch (checkState) {
            case CHECK_STATE_START:
                checkState = NOTIFICATION_TYPE_THEME;
                break;
            case NOTIFICATION_TYPE_THEME:
                checkState = NOTIFICATION_TYPE_WEATHER;
                break;
            case NOTIFICATION_TYPE_WEATHER:
                checkState = NOTIFICATION_TYPE_BATTERY;
                break;
            case NOTIFICATION_TYPE_BATTERY:
                checkState = NOTIFICATION_TYPE_CPU_COOLER;
                break;
            case NOTIFICATION_TYPE_CPU_COOLER:
                checkState = NOTIFICATION_TYPE_BOOST_PLUS;
                break;
            case NOTIFICATION_TYPE_BOOST_PLUS:
                checkState = NOTIFICATION_TYPE_JUNK_CLEANER;
                break;
            case NOTIFICATION_TYPE_JUNK_CLEANER:
            default:
                HSLog.d(TAG, "checkNextNotification Done");
                checkState = CHECK_STATE_DONE;
                return;
        }
        HSLog.d(TAG, "checkNextNotification checkState == " + checkState);

        if (lastHolder != null && lastHolder.nType == checkState) {
            HSLog.d(TAG, "checkNextNotification 跟上一条重复 ");
            checkNextNotification();
        } else {
            trySendNotification();
        }
    }

    private void trySendNotification() {
        if (trySendNotification(checkState)) {
            if (checkState != CHECK_STATE_DONE) {
                mHandler.sendEmptyMessageDelayed(EVENT_CHECK_NEXT_NOTIFICATION, CHECK_NOTIFICATION_TIMEOUT);
            }
        } else {
            checkNextNotification();
        }
    }

    private boolean trySendNotification(int type) {
        boolean ret = true;
        HSLog.d(TAG, "trySendNotification type == " + type);
        switch (type) {
            case NOTIFICATION_TYPE_THEME:
                ret = ThemeNotifier.getInstance().sendThemeNotificationIfNeeded();
                break;
            case NOTIFICATION_TYPE_WEATHER:
                ret = sendWeatherNotificationIfNeeded();
                break;
            case NOTIFICATION_TYPE_BATTERY:
                ret = sendBatteryNotificationIfNeeded(HSApplication.getContext(),
                        DeviceManager.getInstance().getBatteryLevel(), runningApps);
                break;
            case NOTIFICATION_TYPE_CPU_COOLER:
                ret = sendCpuCoolerNotificationIfNeeded();
                break;
            case NOTIFICATION_TYPE_BOOST_PLUS:
                ret = sendBoostPlusNotificationIfNeeded();
                break;
            case NOTIFICATION_TYPE_JUNK_CLEANER:
                sendJunkCleanNotificationIfNeeded();
                break;
            default:
                break;
        }
        return ret;
    }

    private void sendNotificationIfNeeded() {
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if (!isUnlock && km.isKeyguardLocked()) {
            HSLog.d(TAG, "没有解锁。" + !isUnlock + "  km: " + km.isKeyguardLocked());
            return;
        }
        
        if (Utils.isNewUserInDNDStatus()) {
            HSLog.d(TAG, "新用户 2 小时内不提示。");
            return;
        }

        long now = System.currentTimeMillis();
        long screenOnTime = ScreenStatusReceiver.getScreenOnTime();
        long keepOnTime = now - screenOnTime;
        HSLog.d(TAG, "sendNotificationIfNeeded keepOnTime == " + keepOnTime);
        if (keepOnTime > AFTER_SCREEN_ON_TIME && lastScreenOnTime != screenOnTime) {
            lastScreenOnTime = screenOnTime;
            checkHolders();
            if (notificationHolderList.size() < NOTIFICATION_LIMIT_IN_DAY) {
                if (lastHolder == null || now - lastHolder.sendTime > CHECK_NOTIFICATION_INTERVAL) {
                    trySendNotificationInOrder();
                } else {
                    HSLog.d(TAG, "1 小时内发送过消息");
                }
            } else {
                HSLog.d(TAG, "24 小时，超过 6 个");
            }
        } else {
            HSLog.d(TAG, "亮屏不超过 1 分钟 或者 本次亮屏已经判断过通知");
        }
    }

    void recordNotification(int notifyId, int type, long time) {
        HSLog.d(TAG, "recordNotification  id == " + notifyId + "  curType == " + checkState);
        checkState = CHECK_STATE_DONE;
        mHandler.removeMessages(EVENT_CHECK_NEXT_NOTIFICATION);

        lastHolder = new NotificationHolder();
        lastHolder.nId = notifyId;
        lastHolder.nType = type;
        lastHolder.sendTime = System.currentTimeMillis();

        notificationHolderList.add(lastHolder);

        saveToPref();
    }

    private void checkHolders() {
        int size = notificationHolderList.size();
        if (size > 0) {
            NotificationHolder holder;
            for (int i = size - 1; i > 0; i--) {
                holder = notificationHolderList.get(i);
                if (!holder.isValid()) {
                    notificationHolderList.remove(holder);
                }
            }
            saveToPref();
        }
        HSLog.d(TAG, "checkHolders size == " + notificationHolderList.size());
    }

    private void readFromPref() {
//        PreferenceHelper.get(LauncherFiles.NOTIFICATION_PREFS).putString(NOTIFICATION_HISTORY, "");
        String history = PreferenceHelper.get(LauncherFiles.NOTIFICATION_PREFS).getString(NOTIFICATION_HISTORY, "");
        HSLog.d(TAG, "readFromPref history == " + history);
        if (!TextUtils.isEmpty(history)) {
            notificationHolderList.clear();
            try {
                JSONArray jArray = new JSONArray(history);
                HSLog.d(TAG, "readFromPref jArray == " + jArray);
                if (jArray.length() > 0) {
                    for (int i = 0; i < jArray.length(); i++) {
                        HSLog.d(TAG, "readFromPref nStr == " + jArray.get(i));
                        lastHolder = new NotificationHolder();
                        lastHolder.fromJSON((JSONObject) jArray.get(i));
                        HSLog.d(TAG, "readFromPref holder == " + lastHolder);
                        if (lastHolder.isValid()) {
                            notificationHolderList.add(lastHolder);
                        } else {
                            lastHolder = null;
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            HSLog.d(TAG, "readFromPref size == " + notificationHolderList.size());
        }
    }

    private void saveToPref() {
        if (notificationHolderList.size() > 0) {
//            List<String> saveStr = new ArrayList<>(notificationHolderList.size());
            JSONArray jArray = new JSONArray();
            for (NotificationHolder holder : notificationHolderList) {
                jArray.put(holder.toJSON());
            }
            PreferenceHelper.get(LauncherFiles.NOTIFICATION_PREFS).putString(NOTIFICATION_HISTORY, jArray.toString());
        } else {
            PreferenceHelper.get(LauncherFiles.NOTIFICATION_PREFS).putString(NOTIFICATION_HISTORY, "");
        }
    }

    private boolean sendWeatherNotificationIfNeeded() {
        boolean isDailyForecastEnabled = WeatherSettings.isDailyForecastEnabled();
        boolean shouldNotify = shouldNotifyWeather();
        if (isDailyForecastEnabled && shouldNotify) {
            HSLog.d(WEATHER_TAG, "Weather daily forecast notification enabled, updating...");
            WeatherClockManager.getInstance().updateWeather(new WeatherClockManager.WeatherUpdateListener() {
                @Override
                public void onWeatherUpdateFinished() {
                    HSLog.d(WEATHER_TAG, "Weather update done, send notification");
                    sendWeatherNotification(context);
                }
            });
            return true;
        } else {
            if (!shouldNotify) {
                HSLog.d(TAG, "Weather  每天 1 条");
            }

            if (!isDailyForecastEnabled) {
                HSLog.d(TAG, "Weather  功能关闭");
            }
        }
        return false;
    }

    private static boolean shouldNotifyWeather() {
        if (DEBUG_WEATHER_NOTIFICATION) {
            return true;
        }

        if (!Utils.isNetworkAvailable(-1)) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        PreferenceHelper prefs = PreferenceHelper.get(LauncherFiles.DESKTOP_PREFS);

        long lastOpenWeatherTime = prefs.getLong(WeatherActivity.PREF_KEY_WEATHER_LAST_OPEN_TIME, 0);
        long lastNotificationTime = prefs.getLong(PREF_KEY_WEATHER_NOTIFICATION_TIME, 0);
        if ((currentTime - lastOpenWeatherTime) > NOT_OPEN_FEATURE_INTERVAL) {
            if (lastNotificationTime <= 0) {
                return Utils.getDayDifference(currentTime, Utils.getAppInstallTimeMillis(), 6) > 0;
            } else {
                return Utils.getDayDifference(currentTime, lastNotificationTime, 6) > 0;
            }
        } else {
            HSLog.d(TAG, "Weather  最近打开过应用");
        }
        return false;
    }

    private static void sendWeatherNotification(Context context) {
        try {
            sendWeatherNotificationWithCatch(context);
        } catch (Exception e) {
        }
    }

    private static void sendWeatherNotificationWithCatch(Context context) {
        WeatherClockManager manager = WeatherClockManager.getInstance();

        if (manager.getWeatherStatus() == WeatherClockManager.UpdateStatus.SUCCEEDED) {
            String low = manager.getTodayLowTemperatureDescription();
            String high = manager.getTodayHighTemperatureDescription();
            if (TextUtils.isEmpty(low) || TextUtils.isEmpty(high)) {
                HSLog.d(WEATHER_TAG, "Temperature not valid, skip weather notification: " + low + ", " + high);
                return;
            }
            String temperature = low + " ~ " + high;
            String weather = manager.getTodaySimpleConditionDescription();
            String description;
            if (!TextUtils.isEmpty(weather) && !TextUtils.isEmpty(temperature)) {
                description = weather + " " + temperature;
            } else if (!TextUtils.isEmpty(weather)) {
                description = weather;
            } else {
                description = temperature;
            }
            String cityName = manager.getDefaultCityName();
            int iconRes = manager.getTodayIconResourceId();

            PreferenceHelper.get(LauncherFiles.DESKTOP_PREFS).putLong(PREF_KEY_WEATHER_NOTIFICATION_TIME, System.currentTimeMillis());
            sendNotification(context, description, cityName,
                    context.getString(R.string.weather_notification_btn), R.drawable.notification_icon_weather, iconRes);
        }
    }

    private static void sendNotification(Context context,
                                         String title, String description, String buttonText,
                                         int smallIconId, int largeIconId) {
        RemoteViews notification = new RemoteViews(LauncherConstants.LAUNCHER_PACKAGE_NAME, R.layout.notification_weather);
        notification.setTextViewText(R.id.notification_title, title);
        notification.setTextViewText(R.id.notification_description, description);
        notification.setTextViewText(R.id.notification_btn_text, buttonText);
        notification.setImageViewResource(R.id.notification_icon, largeIconId);

        PendingIntent contentIntent = NotificationManager.getInstance().getPendingIntent(
                NotificationManager.ACTION_WEATHER, true);
        PendingIntent deleteIntent = NotificationManager.getInstance().getPendingIntent(
                NotificationManager.ACTION_WEATHER_DELETE, true);

        android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(smallIconId)
                .setContent(notification)
                .setTicker(title)
                .setContentIntent(contentIntent)
                .setDeleteIntent(deleteIntent)
                .setAutoCancel(true);

        NotificationManager.logNotificationPushed(NotificationManager.WEATHER);
        NotificationManager.getInstance().notify(WeatherClockManager.DAILY_WEATHER_NOTIFICATION_ID, builder.build());
    }


    private static long getLastBatteryNotificationTime(String prefKey) {
        return PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).getLong(prefKey, 0);
    }

    private static void setLastBatteryNotificationTime(String prefKey) {
        PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).putLong(prefKey, System.currentTimeMillis());
    }


    private static boolean sendBatteryNotificationIfNeeded(final Context context, final int batteryLevel, final int runningApps) {
        if (!DEBUG_BATTERY_NOTIFICATION) {
            if (BatteryUtils.hasUserUsedBatteryRecently(NOT_OPEN_FEATURE_INTERVAL)) {
                HSLog.d(TAG, "BatteryManager  最近打开过应用");
                return false;
            }
        }

        if (checkLastNotificationInterval(getLastBatteryNotificationTime(PREF_KEY_BATTERY_NOTIFICATION_B_SHOW_TIME))) {
            if (batteryLevel < LOW_BATTERY) {
                sendBatteryNotification(context, -1);
                setLastBatteryNotificationTime(PREF_KEY_BATTERY_NOTIFICATION_B_SHOW_TIME);
                return true;
            } else {
                HSLog.d(TAG, "Battery_B  点亮多于 30%");
            }
        } else {
            HSLog.d(TAG, "Battery_B  每天 1 条");
        }

        if (checkLastNotificationInterval(getLastBatteryNotificationTime(PREF_KEY_BATTERY_NOTIFICATION_A_SHOW_TIME))) {
            if (runningApps == -1) {
                return true;
            }

           if (runningApps >= 3 && batteryLevel >= LOW_BATTERY) {
                sendBatteryNotification(context, runningApps);
                setLastBatteryNotificationTime(PREF_KEY_BATTERY_NOTIFICATION_A_SHOW_TIME);
                return true;
            } else {
                HSLog.d(TAG, "BatteryManager  可清理应用数太少：" + runningApps);
            }
        } else {
            HSLog.d(TAG, "Battery_A  每天 1 条");
        }

        return false;
    }

    private static void sendBatteryNotification(Context context, int appSize) {
        LocalNotification localNotification = new LocalNotification();
        final int notificationId = NOTIFICATION_ID_BATTERY;
        localNotification.notificationId = notificationId;

        localNotification.buttonText = context.getString(R.string.battery_optimize);

        String title;
        String number;
        final String type;
        if (appSize > 0) {
            number = String.valueOf(appSize);
            title = context.getString(R.string.notification_battery_consuming_title, number);
            type = ResultConstants.BATTERY + "_B";
        } else {
            number = String.valueOf(DeviceManager.getInstance().getBatteryLevel());
            title = context.getString(R.string.notification_battery_consuming_title_level, number);
            type = ResultConstants.BATTERY +  "_A";
        }
        SpannableString titleSpannableString = new SpannableString(title);
        titleSpannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.notification_red)), 0, title.indexOf(" "), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        localNotification.title = titleSpannableString;
        localNotification.description = context.getString(R.string.notification_battery_consuming_description);
        localNotification.iconDrawableId = R.drawable.vector_notification_power_consuming;
        localNotification.smallIconDrawableId = R.drawable.notification_icon_battery_consuming;

        PendingIntent pendingIntent = NotificationManager.getInstance().getPendingIntent(
                NotificationManager.ACTION_BATTERY_OPTIMIZE, true, new NotificationManager.ExtraProvider() {
                    @Override
                    public void onAddExtras(Intent intent) {
                        intent.putExtra(NotificationManager.EXTRA_NOTIFICATION_ID, notificationId);
                        intent.putExtra(NotificationManager.EXTRA_NOTIFICATION_TYPE, type);
                    }
                });

        localNotification.pendingIntent = pendingIntent;
        localNotification.deletePendingIntent = NotificationManager.getInstance().getPendingIntent(NotificationManager.ACTION_BATTERY_OPTIMIZE_DELETE,
                false);
        localNotification.autoCleanTimeMills = 2 * 60 * 60 * 1000;

        NotificationManager.logNotificationPushed(type);

        NotificationManager.getInstance().sendDefaultStyleNotification(localNotification);
    }

    private static void setLastNotifyCpuCoolerTime() {
        PreferenceHelper.get(LauncherFiles.CPU_COOLER_PREFS).putLong(PREF_KEY_CPU_COOLER_NOTIFICATION_SHOW_TIME, System.currentTimeMillis());
    }

    private static long getLastNotifyCpuCoolerTime() {
        return PreferenceHelper.get(LauncherFiles.CPU_COOLER_PREFS).getLong(PREF_KEY_CPU_COOLER_NOTIFICATION_SHOW_TIME, 0);
    }

    private static boolean shouldNotifyCpuCooler() {
        if (DEBUG_BOOST_PLUS_NOTIFICATION) {
            HSLog.d(TAG, "shouldNotifyCpuCooler  无视自身条件发送");
            return true;
        }

        long lastOpenCpuCoolerTime = PreferenceHelper.get(LauncherFiles.CPU_COOLER_PREFS)
                .getLong(ResultConstants.PREF_KEY_LAST_CPU_COOLER_USED_TIME, -1);
        long lastNotifyCpuCoolerTime = getLastNotifyCpuCoolerTime();
        long secondTimeFromLastOpen = (System.currentTimeMillis() - lastOpenCpuCoolerTime);
        long secondTimeFromLastNotify = (System.currentTimeMillis() - lastNotifyCpuCoolerTime);
        HSLog.d(TAG, "shouldNotifyCpuCooler lastOpenBoostPlusTime = " + lastOpenCpuCoolerTime
                + " secondTimeFromLastOpen = " + secondTimeFromLastOpen
                + " lastNotifyCpuCoolerTime = " + lastNotifyCpuCoolerTime
                + " secondTimeFromLastNotify = " + secondTimeFromLastNotify); // 86400

        if (secondTimeFromLastOpen <= NOT_OPEN_FEATURE_INTERVAL) {
            HSLog.d(TAG, "shouldNotifyCpuCooler  最近打开过应用");
        }

        if (secondTimeFromLastNotify <= SAME_NOTIFICATION_INTERVAL) {
            HSLog.d(TAG, "shouldNotifyCpuCooler  每天 1 条");
        }

        return secondTimeFromLastOpen > NOT_OPEN_FEATURE_INTERVAL && secondTimeFromLastNotify > SAME_NOTIFICATION_INTERVAL;
    }

    private boolean sendCpuCoolerNotificationIfNeeded() {
        // Cpu Cooler notification
        if (shouldNotifyCpuCooler()) {
            int temp = (int) (CpuCoolerManager.getInstance().fetchCpuTemperature() + 0.5f);
            if (temp >= CPU_ALERT_TEMPERATURE) {
                HSLog.d(TAG, "Show boost+ notification when screen on");
                LocalNotification localNotification = new LocalNotification();
                localNotification.autoCleanTimeMills = 2 * 60 * 60 * 1000; // 2h
                localNotification.notificationId = NOTIFICATION_ID_CPU_COOLER;

                String title = context.getString(R.string.notification_cpu_cooler_title, String.valueOf(temp));
                localNotification.description = context.getString(R.string.notification_cpu_cooler_content);
                SpannableString titleSpannableString = new SpannableString(title);
                titleSpannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.notification_red)), 0, title.indexOf(" "), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                localNotification.title = titleSpannableString;

                localNotification.buttonText = context.getString(R.string.cool_capital);
                localNotification.iconDrawableId = R.drawable.notification_cpucooler;
                localNotification.smallIconDrawableId = R.drawable.notification_cpucooler_small;
                final int notificationId = localNotification.notificationId;
                localNotification.pendingIntent = NotificationManager.getInstance().getPendingIntent(
                        NotificationManager.ACTION_CPU_COOLER, true,
                        new NotificationManager.ExtraProvider() {
                            @Override
                            public void onAddExtras(Intent intent) {
                                intent.putExtra(NotificationManager.EXTRA_NOTIFICATION_ID, notificationId);
                            }
                        });
                NotificationManager.getInstance().sendDefaultStyleNotification(localNotification);
                setLastNotifyCpuCoolerTime();
//                HSAnalytics.logEvent("CpuCooler_Notification_Pushed");
                NotificationManager.logNotificationPushed(ResultConstants.CPU_COOLER);
                return true;
            } else {
                HSLog.d(TAG, "sendCpuCoolerNotificationIfNeeded 温度太低：" + temp);
            }
        }
        return false;
    }

    private static void setLastNotifyBoostPlusTime(String prefKey) {
        PreferenceHelper.get(LauncherFiles.BOOST_PREFS).putLong(prefKey, System.currentTimeMillis());
    }

    private static long getLastNotifyBoostPlusTime(String prefKey) {
        return PreferenceHelper.get(LauncherFiles.BOOST_PREFS).getLong(prefKey, 0);
    }

    private static boolean shouldNotifyBoostPlus() {
        if (DEBUG_BOOST_PLUS_NOTIFICATION) {
            HSLog.d(TAG, "shouldNotifyBoostPlus  无视自身条件发送");
            return true;
        }

        long lastOpenBoostPlusTime = PreferenceHelper.get(LauncherFiles.BOOST_PREFS)
                .getLong(ResultConstants.PREF_KEY_LAST_BOOST_PLUS_USED_TIME, -1);
        long secondTimeFromLastOpen = (System.currentTimeMillis() - lastOpenBoostPlusTime);
        HSLog.d(TAG, "shouldNotifyBoostPlus lastOpenBoostPlusTime = " + lastOpenBoostPlusTime
                + " secondTimeFromLastOpen = " + secondTimeFromLastOpen); // 86400

        if (secondTimeFromLastOpen <= NOT_OPEN_FEATURE_INTERVAL) {
            HSLog.d(TAG, "shouldNotifyBoostPlus  最近打开过应用");
        }

        return secondTimeFromLastOpen > NOT_OPEN_FEATURE_INTERVAL;
    }

    private boolean sendBoostPlusNotificationIfNeeded() {
        // Boost+ notification
        if (shouldNotifyBoostPlus()) {

            if (runningApps == -1) {
                return true;
            }

            if (runningApps >= BoostTipUtils.BOOST_PLUS_NOTIFICATION_RUNNING_APPS_LIMIT) {
                int ram = DeviceManager.getInstance().getRamUsage();
                if (ram > 70 && checkLastNotificationInterval(getLastNotifyBoostPlusTime(PREF_KEY_BOOST_PLUS_LAST_NOTIFICATION_A_TIME))) {
                    sendBoostPlusNotification(true);
                    return true;
                } else {
                    if (ram > 70) {
                        HSLog.d(TAG, "BoostPlus_A 间隔时间少于 1 天");
                    } else {
                        HSLog.d(TAG, "BoostPlus_A RAM 少于 70%");
                    }
                }

                if (checkLastNotificationInterval(getLastNotifyBoostPlusTime(PREF_KEY_BOOST_PLUS_LAST_NOTIFICATION_B_TIME))) {
                    sendBoostPlusNotification(false);
                } else {
                    HSLog.d(TAG, "BoostPlus_B 间隔时间少于 1 天");
                }

            } else {
                HSLog.d(TAG, "sendBoostPlusNotificationIfNeeded 可清理应用数太少：" + runningApps);
            }
        }
        return false;
    }

    private void sendBoostPlusNotification(boolean typeA) {
        HSLog.d(TAG, "Show boost+ notification when screen on");
        LocalNotification localNotification = new LocalNotification();
        localNotification.autoCleanTimeMills = 2 * 60 * 60 * 1000; // 2h
        localNotification.notificationId = NOTIFICATION_ID_BOOST_PLUS;
        String title;
        final String type;
        String number;
        if (typeA) {
            number = String.valueOf(DeviceManager.getInstance().getRamUsage());
            title = context.getString(R.string.notification_boost_plus_title_ram, number);
            localNotification.description = context.getString(R.string.notification_boost_plus_content_ram);
            type = ResultConstants.BOOST_PLUS + "_A";
            setLastNotifyBoostPlusTime(PREF_KEY_BOOST_PLUS_LAST_NOTIFICATION_A_TIME);
        } else {
            number = String.valueOf(runningApps);
            title = context.getString(R.string.notification_boost_plus_title, number);
            localNotification.description = context.getString(R.string.notification_boost_plus_content);
            type = ResultConstants.BOOST_PLUS + "_B";
            setLastNotifyBoostPlusTime(PREF_KEY_BOOST_PLUS_LAST_NOTIFICATION_B_TIME);
        }
        SpannableString titleSpannableString = new SpannableString(title);
        titleSpannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.notification_red)), 0, title.indexOf(" "), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        localNotification.title = titleSpannableString;

        localNotification.buttonText = context.getString(R.string.boost_notification_low_ram_btn);
        localNotification.iconDrawableId = R.drawable.notification_boost_plus_svg;
        localNotification.smallIconDrawableId = R.drawable.notification_boost_plus_small_icon;
        final int notificationId = localNotification.notificationId;
        localNotification.pendingIntent = NotificationManager.getInstance().getPendingIntent(
                NotificationManager.ACTION_BOOST_PLUS, true,
                new NotificationManager.ExtraProvider() {
                    @Override
                    public void onAddExtras(Intent intent) {
                        intent.putExtra(NotificationManager.EXTRA_NOTIFICATION_ID, notificationId);
                        intent.putExtra(NotificationManager.EXTRA_NOTIFICATION_TYPE, type);
                    }
                });
        NotificationManager.getInstance().sendDefaultStyleNotification(localNotification);
//                HSAnalytics.logEvent("BoostPlus_Notification_Pushed", "type", type);
        NotificationManager.logNotificationPushed(type);
    }

    private static void setLastNotifyJunkCleanTime() {
        PreferenceHelper.get(LauncherFiles.JUNK_CLEAN_PREFS).putLong(PREF_KEY_JUNK_CLEAN_LAST_NOTIFICATION_TIME, System.currentTimeMillis());
    }

    private static long getLastNotifyJunkCleanTime() {
        return PreferenceHelper.get(LauncherFiles.JUNK_CLEAN_PREFS).getLong(PREF_KEY_JUNK_CLEAN_LAST_NOTIFICATION_TIME, 0);
    }

    private static boolean shouldNotifyJunkClean() {
        if (DEBUG_JUNK_CLEAN_NOTIFICATION) {
            HSLog.d(TAG, "shouldNotifyJunkClean  无视自身条件发送");
            return true;
        }

        long lastOpenJunkCleanTime = PreferenceHelper.get(LauncherFiles.JUNK_CLEAN_PREFS)
                .getLong(ResultConstants.PREF_KEY_LAST_JUNK_CLEAN_USED_TIME, -1);
        long lastNotifyJunkCleanTime = getLastNotifyJunkCleanTime();
        long secondTimeFromLastOpen = (System.currentTimeMillis() - lastOpenJunkCleanTime);
        long secondTimeFromLastNotify = (System.currentTimeMillis() - lastNotifyJunkCleanTime);
        HSLog.d(TAG, "shouldNotifyJunkClean *** lastOpenJunkCleanTime = " + lastOpenJunkCleanTime
                + " secondTimeFromLastOpen = " + secondTimeFromLastOpen
                + " lastNotifyJunkCleanTime = " + lastNotifyJunkCleanTime
                + " secondTimeFromLastNotify = " + secondTimeFromLastNotify); // 86400
        if (secondTimeFromLastOpen <= NOT_OPEN_FEATURE_INTERVAL) {
            HSLog.d(TAG, "shouldNotifyJunkClean  最近打开过应用");
        }

        if (checkLastNotificationInterval(lastNotifyJunkCleanTime)) {
            HSLog.d(TAG, "shouldNotifyJunkClean  每天 1 条");
        }
        return secondTimeFromLastOpen > NOT_OPEN_FEATURE_INTERVAL && checkLastNotificationInterval(lastNotifyJunkCleanTime);
    }

    private boolean sendJunkCleanNotificationIfNeeded() {
        // JunkClean notification
        if (shouldNotifyJunkClean()) {
            if (!JunkCleanActivity.sIsJunkCleanRunning) {
                HSLog.d(TAG, "Show junk clean notification startJunkScan");
                long junkSize = JunkManager.getInstance().getTotalJunkSize();
                if (junkSize > JUNK_CLEAN_NOTIFICATION_SIZE) {
                    sendJunkCleanNotification(junkSize);
                } else {
                    JunkManager.getInstance().startJunkScan(new JunkManager.ScanJunkListenerAdapter() {
                        @Override
                        public void onScanFinished(long junkSize) {
                            float junkSizeMB = junkSize / 1024 / 1024;
                            HSLog.d(TAG, "Show junk clean *** notification when screen on junkSizeMB = " + junkSizeMB);
                            if (junkSize > JUNK_CLEAN_NOTIFICATION_SIZE) {
                                sendJunkCleanNotification(junkSize);
                            } else {
                                HSBundle hsBundle = new HSBundle();
                                hsBundle.putInt(KEY_NOTIFICATION_TYPE, NOTIFICATION_TYPE_JUNK_CLEANER);
                                HSGlobalNotificationCenter.sendNotification(NOTIFICATION_CHECK_DONE);
                            }
                        }
                    });
                }
                return true;
            } else {
                HSLog.d(TAG, "JunkCleanActivity.sIsJunkCleanRunning 正在运行");
            }
        }
        return false;
    }

    private void sendJunkCleanNotification(long junkSize) {
        FormatSizeBuilder junkSizeBuilder = new FormatSizeBuilder(junkSize);
        String appSizeText = junkSizeBuilder.size + junkSizeBuilder.unit;
        HSLog.d(NotificationManager.TAG, "Show junk clean *** notification when screen on");
        LocalNotification localNotification = new LocalNotification();
        localNotification.autoCleanTimeMills = 2 * 60 * 60 * 1000; // 2h
        localNotification.notificationId = NOTIFICATION_ID_JUNK_CLEAN;
        String title = context.getString(R.string.notification_junk_clean_title, appSizeText);
        SpannableString titleSpannableString = new SpannableString(title);
        titleSpannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.notification_red)), 0, appSizeText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        localNotification.title = titleSpannableString;
        localNotification.description = context.getString(R.string.notification_junk_clean_content);
        localNotification.buttonText = context.getString(R.string.clean_capital);
        localNotification.iconDrawableId = R.drawable.ic_junk_cleaner;
        localNotification.smallIconDrawableId = R.drawable.notification_clean_small_icon;
        final int notificationId = localNotification.notificationId;
        localNotification.pendingIntent = NotificationManager.getInstance().getPendingIntent(
                NotificationManager.ACTION_JUNK_CLEAN, true,
                new NotificationManager.ExtraProvider() {
                    @Override
                    public void onAddExtras(Intent intent) {
                        intent.putExtra(NotificationManager.EXTRA_NOTIFICATION_ID, notificationId);
                    }
                });
        NotificationManager.getInstance().sendDefaultStyleNotification(localNotification);
        NotificationManager.logNotificationPushed(ResultConstants.JUNK_CLEANER);

        setLastNotifyJunkCleanTime();
    }

    private static boolean checkLastNotificationInterval(long last) {
        return (System.currentTimeMillis() - last) > SAME_NOTIFICATION_INTERVAL;
    }

    class NotificationHolder {
        private static final String ID = "id";
        private static final String TYPE = "type";
        private static final String TIME = "time";

        int nId;
        int nType;
        long sendTime;

        JSONObject toJSON() {
            JSONObject jObj = new JSONObject();
            try {
                jObj.put(ID, nId);
                jObj.put(TYPE, nType);
                jObj.put(TIME, sendTime);
            } catch (JSONException e) {
            }
            HSLog.d(TAG, "toJSON == " + jObj.toString());
            return jObj;
        }

        NotificationHolder fromJSON(JSONObject jObj) {
            if (jObj == null) {
                return this;
            }

            try {
                nId = jObj.getInt(ID);
                nType = jObj.getInt(TYPE);
                sendTime = jObj.getLong(TIME);
            } catch (JSONException e) {
            }
            HSLog.d(TAG, "fromJSON == " + toString());
            return this;
        }

        public boolean isValid() {
            long time = System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS;
            return nType >= 0 && sendTime > time;
        }

        @Override public String toString() {
            return "NotificationHolder{" +
                    "nId=" + nId +
                    ", nType=" + nType +
                    ", sendTime=" + sendTime +
                    '}';
        }
    }
}
