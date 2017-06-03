package com.ihs.feature.boost.notification;

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;

import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.feature.boost.BoostTipUtils;
import com.ihs.feature.boost.DeviceManager;
import com.ihs.feature.common.DeviceUtils;
import com.ihs.feature.common.LauncherFiles;
import com.ihs.feature.common.PreferenceHelper;
import com.ihs.feature.common.ResultConstants;
import com.ihs.keyboardutils.BuildConfig;
import com.ihs.keyboardutils.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.ihs.feature.common.ResultConstants.PREF_KEY_LAST_BOOST_PLUS_USED_TIME;

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
    private static final int NOTIFICATION_TYPE_JUNK_CLEANER = 0;


    private static final int CHECK_STATE_START = -1;
    private static final int NOTIFICATION_TYPE_BOOST_PLUS = NOTIFICATION_ID_BOOST_PLUS;
    private static final int CHECK_STATE_DONE = -2;

    private static final int EVENT_CHECK_NEXT_NOTIFICATION = 100;

    // 等待通知检查条件时间，超时认为失败检查下一条。
    // 比如 boost+ 或者 junk clean 扫描用时。这里目前没做处理，所以最好是时间足够长，保证能完成扫描。
    private static final long CHECK_NOTIFICATION_TIMEOUT = DateUtils.MINUTE_IN_MILLIS;
    // 没有打开相应功能模块的时间
    public static final long NOT_OPEN_FEATURE_INTERVAL = 30 * DateUtils.MINUTE_IN_MILLIS;
    // 两条消息之间的时间间隔
    private static final long CHECK_NOTIFICATION_INTERVAL = DateUtils.HOUR_IN_MILLIS;
    // 亮屏之后到检查通知的时间
    private static final long AFTER_SCREEN_ON_TIME = DateUtils.MINUTE_IN_MILLIS;
    // 同一类型的消息的时间间隔 (需求为 24 小时 1 条)
    private static final long SAME_NOTIFICATION_INTERVAL = DateUtils.DAY_IN_MILLIS;
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
        @Override
        public void handleMessage(Message msg) {
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
                    if (checkState == NOTIFICATION_TYPE_BOOST_PLUS) {
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

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        HSGlobalNotificationCenter.removeObserver(this);
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        HSLog.d(TAG, "onReceive s == " + s);
        if (TextUtils.equals(s, ScreenStatusReceiver.NOTIFICATION_SCREEN_OFF)) {
            isUnlock = false;
        } else if (TextUtils.equals(s, ScreenStatusReceiver.NOTIFICATION_SCREEN_ON)) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
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
        trySendNotification();
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
            case NOTIFICATION_TYPE_BOOST_PLUS:
                ret = sendBoostPlusNotificationIfNeeded();
                break;
            default:
                break;
        }
        return ret;
    }

    private void sendNotificationIfNeeded() {
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (!isUnlock && km.isKeyguardLocked()) {
                HSLog.d(TAG, "没有解锁。" + !isUnlock + "  km: " + km.isKeyguardLocked());
                return;
            }
        }

//        if (Utils.isNewUserInDNDStatus()) {
//            HSLog.d(TAG, "新用户 2 小时内不提示。");
//            return;
//        }

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

        @Override
        public String toString() {
            return "NotificationHolder{" +
                    "nId=" + nId +
                    ", nType=" + nType +
                    ", sendTime=" + sendTime +
                    '}';
        }
    }
}
