package com.ihs.feature.boost;

import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.feature.common.ConcurrentUtils;
import com.ihs.feature.common.LauncherFiles;
import com.ihs.feature.common.LauncherTipManager;
import com.ihs.feature.common.PreferenceHelper;
import com.ihs.feature.common.Utils;
import com.ihs.keyboardutils.BuildConfig;

import java.util.ArrayList;
import java.util.List;

public class BoostConditionManager {

    private static final String TAG = "BoostNotification";

    public static final int EFFECTIVE_BOOST_PERCENTAGE_THRESHOLD = 1;

    private static final BoostType DEBUG_BOOST_TYPE = BuildConfig.DEBUG ? null : null;
    private static final int DEBUG_CRITICAL_BATTERY_LEVEL = BuildConfig.DEBUG ? 95 : 30;
    private static final int DEBUG_NORMAL_BATTERY_LEVEL = BuildConfig.DEBUG ? 96 : 40;

    private static final int DEBUG_CRITICAL_CPU_TEMPERATURE = BuildConfig.DEBUG ? 35 : 43;
    private static final int DEBUG_NORMAL_CPU_TEMPERATURE = BuildConfig.DEBUG ? 40 : 35;

    private static final long BOOST_NOTIFICATION_MIN_INTERVAL = BuildConfig.DEBUG ?
            (5 * DateUtils.MINUTE_IN_MILLIS) : (6 * DateUtils.HOUR_IN_MILLIS);

    private static final long BOOST_USER_FEATURE_USED_INTERVAL = 5 * DateUtils.MINUTE_IN_MILLIS;
    private static final long BOOST_SPECIAL_ICON_INTERVAL = BuildConfig.DEBUG ? DateUtils.MINUTE_IN_MILLIS : 2 * DateUtils.HOUR_IN_MILLIS;
    private static final int CONDITION_FULFILL_DURATION_MINUTES = 2;

    public static final String PREF_KEY_LAST_BOOST_NOTIFICATION_TIME = "last_boost_notification_time";
    private static final String PREF_KEY_SPECIAL_BOOST_ICON_RESUME_TIME = "special_boost_icon_resume_time";

    public interface ConditionChangeListener {
        void onBoostNeeded(BoostType type);

        void onBoostNotNeeded(BoostType previousType);
    }

    private List<ConditionChangeListener> mListeners = new ArrayList<>(2);

    private Integer mLowBatteryCount = 5;
    private Integer mHotCpuCount = 0;
    private Integer mLowRamCount = 0;

    /**
     * Type of boost needed. {@code null} when boost is not needed.
     */
    private BoostType mCurrentBoostType;

    private BoostType mPendingHighlightType;

    private volatile static BoostConditionManager sInstance;

    public static BoostConditionManager getInstance() {
        if (sInstance == null) {
            synchronized (BoostConditionManager.class) {
                if (sInstance == null) {
                    sInstance = new BoostConditionManager();
                }
            }
        }
        return sInstance;
    }

    private BoostConditionManager() {
    }

    public synchronized void addConditionChangeListener(ConditionChangeListener listener) {
        mListeners.add(listener);
    }

    public synchronized void removeConditionChangeListener(ConditionChangeListener listener) {
        mListeners.remove(listener);
    }

    public @NonNull BoostType getCurrentBoostType() {
        if (mCurrentBoostType == null) {
            // Defaults to RAM when non of the RAM / BATTERY / CPU_TEMPERATURE condition is satisfied
            return BoostType.RAM;
        }
        return mCurrentBoostType;
    }

    public BoostType pollPendingHighlightType() {
        BoostType pendingType = mPendingHighlightType;
        mPendingHighlightType = null;
        return pendingType;
    }

    public void reportMinuteData(int batteryLevel, boolean isCharging, float cpuTemperature, int ramUsage) {
        HSLog.i(TAG, "Time tick, battery " + batteryLevel + "%, CPU temp " + cpuTemperature
                + ", RAM " + ramUsage + "%, isCharging " + isCharging);
        if (DEBUG_BOOST_TYPE != null) {
            setCurrentBoostType(DEBUG_BOOST_TYPE);
            notify(DEBUG_BOOST_TYPE, true);
            return;
        }

        if (mCurrentBoostType == null || mCurrentBoostType == BoostType.RAM) {
            // RAM
            if (ramUsage >= 80) {
                mLowRamCount++;
                mLowRamCount = 0;
                if (mCurrentBoostType == null) {
                    notify(BoostType.RAM, true);
                }
            } else {
                mLowRamCount = 0;
                if (mCurrentBoostType == BoostType.RAM) {
                    notify(BoostType.RAM, false);
                }
            }
        }
    }

    public void reportBoostDone(int percentageBoosted) {
        if (percentageBoosted >= EFFECTIVE_BOOST_PERCENTAGE_THRESHOLD) {
            //// TODO: 17/6/1 zhelishisha
//            PreferenceHelper.get(LauncherFiles.DESKTOP_PREFS)
//                    .incrementAndGetInt(SetAsDefaultBoostedTipInfo.PREF_KEY_EFFECTIVE_BOOST_TIMES);
        }
        if (mCurrentBoostType != null) {
            BoostType type = mCurrentBoostType;
            setCurrentBoostType(null);
            notify(type, false);
        }
    }

    public void setCurrentBoostType(BoostType type) {
        mCurrentBoostType = type;
        mPendingHighlightType = type;
    }

    private synchronized boolean notify(final BoostType type, boolean deviceNeedBoost) {
        // Not always notify user, DND!
        if (deviceNeedBoost && (Utils.inSleepTime())) {
            if (BuildConfig.DEBUG) {
                HSLog.i(TAG, "休息时间，不变化图标：" + Utils.inSleepTime());
            }
            return false;
        }
        setCurrentBoostType(deviceNeedBoost ? type : null);

        LauncherTipManager.getInstance().showTip(HSApplication.getContext(),
                LauncherTipManager.TipType.NEED_BOOST_TIP, type, deviceNeedBoost);
        return true;
    }

    public synchronized void notifyNeedBoost(final BoostType type, boolean isNeeded) {
        for (ConditionChangeListener listener : mListeners) {
            final ConditionChangeListener listenerFinal = listener;
            if (isNeeded) {
                HSLog.d(TAG, "Boost condition " + type + " fulfilled");
                HSPreferenceHelper.getDefault().putLong(PREF_KEY_LAST_BOOST_NOTIFICATION_TIME, System.currentTimeMillis());
                ConcurrentUtils.postOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        listenerFinal.onBoostNeeded(type);
                    }
                });
            } else {
                HSLog.d(TAG, "Boost condition " + type + " off");
                ConcurrentUtils.postOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        listenerFinal.onBoostNotNeeded(type);
                    }
                });
            }
        }
    }

    private static boolean hasRemindUserShortlyBefore() {
        long lastNotificationTime = HSPreferenceHelper.getDefault().getLong(PREF_KEY_LAST_BOOST_NOTIFICATION_TIME, -1);
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - lastNotificationTime;
        HSLog.d(TAG, timeDifference + " ms since last shown boost notification");
        if (timeDifference < BOOST_NOTIFICATION_MIN_INTERVAL) {
            return true;
        }
       return false;
    }

    private static boolean canChangeSpecialBoostIcon() {
        long time = PreferenceHelper.get(LauncherFiles.DESKTOP_PREFS).getLong(PREF_KEY_SPECIAL_BOOST_ICON_RESUME_TIME, -1);
        return (System.currentTimeMillis() - time) > BOOST_SPECIAL_ICON_INTERVAL;
    }

    private static void setChangeSpecialBoostIcon() {
        PreferenceHelper.get(LauncherFiles.DESKTOP_PREFS).putLong(PREF_KEY_SPECIAL_BOOST_ICON_RESUME_TIME, System.currentTimeMillis());
    }
}
