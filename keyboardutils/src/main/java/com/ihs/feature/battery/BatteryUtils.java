package com.ihs.feature.battery;

import android.view.View;

import com.honeycomb.launcher.BuildConfig;
import com.honeycomb.launcher.R;
import com.honeycomb.launcher.boost.DeviceManager;
import com.honeycomb.launcher.desktop.Launcher;
import com.honeycomb.launcher.desktop.util.IconAnimUtils;
import com.honeycomb.launcher.dialog.LauncherTipManager;
import com.honeycomb.launcher.model.LauncherFiles;
import com.honeycomb.launcher.resultpage.data.ResultConstants;
import com.honeycomb.launcher.util.NavUtils;
import com.honeycomb.launcher.util.PreferenceHelper;
import com.honeycomb.launcher.util.Utils;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.commons.utils.HSLog;

import java.util.Calendar;

public class BatteryUtils {

    private static final boolean ICON_SHAKE_ANIM_DISABLED = true;

    public static final String PREF_KEY_BATTERY_TOTAL_REMAINING_TIME = "PREF_KEY_BATTERY_TOTAL_REMAINING_TIME";
    public static final String PREF_KEY_BATTERY_LAST_CLEAN_TIME = "PREF_KEY_BATTERY_LAST_CLEAN_TIME";
    public static final String PREF_KEY_BATTERY_VIEW_TYPE = "PREF_KEY_BATTERY_VIEW_TYPE";
    public static final String PREF_KEY_BATTERY_LAST_OPTIMIZE_REMAINING_SECOND_TIME = "PREF_KEY_BATTERY_LAST_OPTIMIZE_REMAINING_SECOND_TIME";
    public static final String PREF_KEY_BATTERY_EXIT_TIME = "PREF_KEY_BATTERY_EXIT_TIME";
    public static final String PREF_KEY_BATTERY_EXIT_LEVEL = "PREF_KEY_BATTERY_EXIT_LEVEL";
    // User click battery icon and enter
    private static final String PREF_KEY_BATTERY_USER_VISIT_TIME = "PREF_KEY_BATTERY_USER_VISIT_TIME";
    private static final String PREF_KEY_BATTERY_ICON_ANIM_COUNT = "PREF_KEY_BATTERY_ICON_ANIM_COUNT";
    private static final String PREF_KEY_BATTERY_TIP_CANCEL_COUNT = "pref_battery_tip_cancel_count";
    private static final String PREF_KEY_BATTERY_TIP_SHOW_TIME = "pref_battery_tip_show_time";
    private static final String PREF_KEY_BATTERY_ICON_CHANGED_TIME = "battery_icon_changed_time";

    private static final int BATTERY_MAX_SECOND = 24 * 3600;
    private static final int BATTERY_MIN_SECOND = 20 * 3600;
    private static final float BATTERY_MIN_POWER_RED_LIMIT = 20;
    private static final float BATTERY_ICON_CHANGED_LIMIT = 2;
    private static final float BATTERY_MIN_FACTOR = 0.5f;
    private static final long ONE_DAY_MillS = 86400000;

    static final int BATTERY_STATUS_A_LEVEL = 20;
    static final int BATTERY_STATUS_B_LEVEL = 40;
    static final int BATTERY_STATUS_C_LEVEL = 70;
    static final int BATTERY_TOOLBAR_STATUS_A_LEVEL = 30;
    static final int BATTERY_TOOLBAR_STATUS_B_LEVEL = 60;
    static final int BATTERY_TOOLBAR_STATUS_C_LEVEL = 90;

    private static final int BATTERY_EXPEND_POWER_LIMIT = 30;
    private static final long BATTERY_INIT_ANIMATION_FREEZE_SECOND_TIME = 5 * 60;
    private static final long BATTERY_RESUME_TOTAL_SECOND_TIME = 5 * 60;
    private static final float BATTERY_CLEAN_FREEZE_SECOND_TIME = 5 * 60;
    private static final float BATTERY_ICON_ANIM_TIMES_LIMIT = 3;
    private static final long BATTERY_TIP_SHOW_INTERVAL_MILLS = 6 * 3600 * 1000;
    private static final int BATTER_LOW_LEVEL_FOR_REMIND = 20; // 20%

    static final int BATTERY_LOW_LIMIT = 30;
    static final int BATTERY_RAM_USAGE_LIMIT = 50;

    private static boolean mIsBatteryOpened;
    private static int sAnimPlayCount = 0;
    private static long sBatteryLowTipShowTime = 0;


    /**
     * return int[0]: hour int[1]: minute
     */
    static int[] calculateBatteryRemainingTime(int batteryLevel) {
        long batteryTotalSecondTime = calculateBatteryRemainingSecondTime(batteryLevel);
        long batteryLevelMinute = (long) Math.floor((double)batteryTotalSecondTime / 60);
        int hour = (int) (Math.floor((double) batteryLevelMinute / 60));
        int minute = (int) (batteryLevelMinute < 60 ? batteryLevelMinute : batteryLevelMinute % 60);
        HSLog.d(BatteryActivity.TAG, "BatteryUtils calculateBatteryRemainingTime batteryLevel = " + batteryLevel + " batteryTotalSecondTime = " + batteryTotalSecondTime + " batteryLevelMinute = " + batteryLevelMinute + " hour = " + hour + " minute = " + minute);
        int[] times = new int[2];
        times[0] = hour;
        times[1] = minute;
        return times;
    }

    private static long calculateBatteryRemainingSecondTime(int batteryLevel) {
        long batteryTotalSecondTime = calculateBatteryTotalSecondTime(false);
        long batteryLevelMinute = (long) Math.floor((double) batteryTotalSecondTime / 100 * batteryLevel / 60);
        if (batteryLevel <= BATTERY_MIN_POWER_RED_LIMIT) {
            batteryLevelMinute = (long) Math.floor(batteryLevelMinute * BATTERY_MIN_FACTOR);
        }
        HSLog.d(BatteryActivity.TAG, "BatteryUtils calculateBatteryRemainingSecondTime batteryLevel = " + batteryLevel + " batteryLevelMinute = " + batteryLevelMinute);
        return batteryLevelMinute * 60;
    }

    private static long calculateBatteryTotalSecondTime(boolean isRecalculateTotalTime) {
        long batteryTotalSecondTime = getBatteryTotalRemainingSecondTime(); // 72000~86400
        if (isRecalculateTotalTime) {
            batteryTotalSecondTime = BATTERY_MIN_SECOND + (long)(Math.random() * (BATTERY_MAX_SECOND - BATTERY_MIN_SECOND));
            setBatteryTotalRemainingSecondTime(batteryTotalSecondTime);
        }
        HSLog.d(BatteryActivity.TAG, "BatteryUtils calculateBatteryTotalSecondTime isRecalculateTotalTime  = " + isRecalculateTotalTime + " batteryTotalSecondTime = " + batteryTotalSecondTime);
        return batteryTotalSecondTime;
    }

    static int[] calculateBatteryExtendTime(int batteryLevel) {
        long remainingSecondTime = calculateBatteryRemainingSecondTime(batteryLevel);
        long lastRemainingSecondTime = getBatteryLastOptimizeRemainingSecondTime();
        setBatteryLastOptimizeRemainingSecondTime(remainingSecondTime);

        long extendSecondTimeMin = (long)(0.1f * remainingSecondTime);
        long extendSecondTimeMax = (long)(0.2f * remainingSecondTime);
        long extendSecondTime;
        if (batteryLevel < BATTERY_EXPEND_POWER_LIMIT) {
            extendSecondTime = extendSecondTimeMin + (long)(Math.random() * (extendSecondTimeMax - extendSecondTimeMin));
        } else {
            extendSecondTime = 3600 + (long)(Math.random() * 3600); // 1h^2h
        }

        if ((lastRemainingSecondTime - remainingSecondTime) > 0 && extendSecondTime > (lastRemainingSecondTime - remainingSecondTime)) {
            extendSecondTime = 0;
        }

        if (extendSecondTime > 0) {
            long totalRemainingSecondTime = getBatteryTotalRemainingSecondTime();
            long extendTotalRemainingSecondTime = (long) Math.floor(100 * extendSecondTime / batteryLevel + totalRemainingSecondTime);

            HSLog.d(BatteryActivity.TAG, "calculateBatteryExtendTime extendTotalRemainingSecondTime = " + extendTotalRemainingSecondTime);
            setBatteryTotalRemainingSecondTime(extendTotalRemainingSecondTime);
        }

        long totalMinute = (long) Math.floor((double) extendSecondTime / 60);
        int hour = (int) (Math.floor((double) totalMinute / 60));
        int minute = (int) (totalMinute < 60 ? totalMinute : totalMinute % 60);
        int[] times = new int[2];
        times[0] = hour;
        times[1] = minute;
        HSLog.d(BatteryActivity.TAG, "calculateBatteryExtendTime extendTotalRemainingSecondTime extend level = " + batteryLevel + " hour = " + hour + " minute = " + minute + " extendSecondTime = " + extendSecondTime);
        return times;
    }

    private static void calculateBatteryRemainingRealSecondTime() {
        long exitDurationSecondTime = System.currentTimeMillis() - getBatteryExitSecondTime();
        if (exitDurationSecondTime > BATTERY_RESUME_TOTAL_SECOND_TIME) {
            return;
        }
        int batteryLevel = DeviceManager.getInstance().getBatteryLevel();
        int batteryExitLevel = getBatteryExitLevel();
        HSLog.d(BatteryActivity.TAG, "BatteryUtils isBatteryResumeFrozen exitDurationSecondTime = " + exitDurationSecondTime + " batteryLevel = " + batteryLevel + " batteryExitLevel = " + batteryExitLevel);
        if (batteryLevel < batteryExitLevel) {
            long exitRemainingSecondTime = calculateBatteryRemainingSecondTime(batteryExitLevel);
            long currentBatteryLevelSecondTime = exitRemainingSecondTime - exitDurationSecondTime;
            if (currentBatteryLevelSecondTime > 0) {
                long realRemainingTotalSecondTime = (long) Math.floor((double) currentBatteryLevelSecondTime / batteryLevel * 100) ;
                HSLog.d(BatteryActivity.TAG, "BatteryUtils isBatteryResumeFrozen realRemainingTotalSecondTime = " + realRemainingTotalSecondTime);
                if (realRemainingTotalSecondTime >= BATTERY_MIN_SECOND - BATTERY_RESUME_TOTAL_SECOND_TIME) {
                    setBatteryTotalRemainingSecondTime(realRemainingTotalSecondTime);
                }
            }
        }
    }

    static void resumeBatteryTime() {
        if (isBatteryResumeFrozen()) {
            calculateBatteryRemainingRealSecondTime();
        } else {
            calculateBatteryTotalSecondTime(true);
        }
    }

    private static boolean isBatteryResumeFrozen() {
        long batteryExitSecondTime = getBatteryExitSecondTime();
        long freezeSecondTimeFromLastExit = System.currentTimeMillis() / 1000 - batteryExitSecondTime;
        HSLog.d(BatteryActivity.TAG, "BatteryUtils isBatteryResumeFrozen freezeSecondTimeFromLastExit = " + freezeSecondTimeFromLastExit);
        return batteryExitSecondTime != 0 && freezeSecondTimeFromLastExit < BATTERY_RESUME_TOTAL_SECOND_TIME;
    }

    static boolean isInitAnimationFrozen() {
        long batteryExitSecondTime = getBatteryExitSecondTime();
        long freezeSecondTimeFromLastExit = System.currentTimeMillis() / 1000 - batteryExitSecondTime;
        HSLog.d(BatteryActivity.TAG, "BatteryUtils isInitAnimationFrozen ****************** freezeSecondTimeFromLastExit = " + freezeSecondTimeFromLastExit);
        return batteryExitSecondTime != 0 && freezeSecondTimeFromLastExit < BATTERY_INIT_ANIMATION_FREEZE_SECOND_TIME;
    }

    private static void setBatteryTotalRemainingSecondTime(long secondTime) {
        PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).putLong(PREF_KEY_BATTERY_TOTAL_REMAINING_TIME, secondTime);
    }

    private static long getBatteryTotalRemainingSecondTime() {
        return PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).getLong(PREF_KEY_BATTERY_TOTAL_REMAINING_TIME, 0);
    }

    static void setBatteryLastCleanSecondTime(long lastCleanTime) {
        PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).putLong(PREF_KEY_BATTERY_LAST_CLEAN_TIME, lastCleanTime);
    }

    private static long getBatteryLastCleanSecondTime() {
        return PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).getLong(PREF_KEY_BATTERY_LAST_CLEAN_TIME, 0);
    }

    static void setBatteryLastViewType(int lastViewType) {
        PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).putInt(PREF_KEY_BATTERY_VIEW_TYPE, lastViewType);
    }

    static int getBatteryLastViewType() {
        return PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).getInt(PREF_KEY_BATTERY_VIEW_TYPE, BatteryActivity.ViewType.CAREFUL.getValue());
    }

    private static void setBatteryLastOptimizeRemainingSecondTime(long secondTime) {
        PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).putLong(PREF_KEY_BATTERY_LAST_OPTIMIZE_REMAINING_SECOND_TIME, secondTime);
    }

    private static long getBatteryLastOptimizeRemainingSecondTime() {
        return PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).getLong(PREF_KEY_BATTERY_LAST_OPTIMIZE_REMAINING_SECOND_TIME, 0);
    }

    static void setBatteryExitSecondTime(long exitTime) {
        PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).putLong(PREF_KEY_BATTERY_EXIT_TIME, exitTime);
    }

    private static long getBatteryExitSecondTime() {
        return PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).getLong(PREF_KEY_BATTERY_EXIT_TIME, 0);
    }

    static void setBatteryExitLevel(int batteryLevel) {
        PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).putInt(PREF_KEY_BATTERY_EXIT_LEVEL, batteryLevel);
    }

    private static int getBatteryExitLevel() {
        return PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).getInt(PREF_KEY_BATTERY_EXIT_LEVEL, 0);
    }

    static void setBatteryOpenFlag(boolean isOpened) {
        mIsBatteryOpened = isOpened;
    }

    public static boolean getBatteryOpenFlag() {
        return mIsBatteryOpened;
    }

    static boolean isCleanTimeFrozen() {
        long lastCleanSecondTime = getBatteryLastCleanSecondTime();
        long cleanFreezeSecondTime = System.currentTimeMillis() / 1000 - lastCleanSecondTime;
        HSLog.d(BatteryActivity.TAG, "BatteryUtils isCleanFreeze cleanFreezeSecondTime = " + cleanFreezeSecondTime);
        return (0 != lastCleanSecondTime && cleanFreezeSecondTime < BATTERY_CLEAN_FREEZE_SECOND_TIME);
    }

    public static void flurryBatteryIconClicked(int batteryLevel, boolean isCharging) {
        if (batteryLevel <= 0) {
            batteryLevel = DeviceManager.getInstance().getBatteryLevel();
        }
        if (batteryLevel <= BATTERY_STATUS_A_LEVEL) {
            HSAnalytics.logEvent("Battery_IconClicked", "type", isCharging ? "Charging-a" : "Not Charging-a");
        } else if (batteryLevel <= BATTERY_STATUS_B_LEVEL) {
            HSAnalytics.logEvent("Battery_IconClicked", "type", isCharging ? "Charging-b" : "Not Charging-b");
        } else if (batteryLevel <= BATTERY_STATUS_C_LEVEL) {
            HSAnalytics.logEvent("Battery_IconClicked", "type", isCharging ? "Charging-c" : "Not Charging-c");
        } else {
            HSAnalytics.logEvent("Battery_IconClicked", "type", isCharging ? "Charging-d" : "Not Charging-d");
        }
        PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).putLong(PREF_KEY_BATTERY_USER_VISIT_TIME, System.currentTimeMillis());
    }

    public static boolean isUserEnterBatteryManagerRecent(long interval) {
        long lastTime = PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).getLong(PREF_KEY_BATTERY_USER_VISIT_TIME, 0);
        return (System.currentTimeMillis() - lastTime) < interval;
    }

    // Battery Icon Anim
    private static boolean needShowIconAnim() {
        if (ICON_SHAKE_ANIM_DISABLED) {
            return false;
        }
        if (sAnimPlayCount == 0) {
            sAnimPlayCount = PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).getInt(PREF_KEY_BATTERY_ICON_ANIM_COUNT, 0);
        }
        // 1 User enter battery manager out one day
        if (sAnimPlayCount >= BATTERY_ICON_ANIM_TIMES_LIMIT) {
            return false;
        }
        // 2 Anim has reach limit
        if (isUserEnterBatteryManagerRecent(ONE_DAY_MillS)) {
            return false;
        }

        sAnimPlayCount++;
        PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).putInt(PREF_KEY_BATTERY_ICON_ANIM_COUNT, sAnimPlayCount);
        return true;
    }

    public static void startIconAnimIfNeeded(View batteryIcon) {
        if (!needShowIconAnim()) {
            return;
        }
        IconAnimUtils.startHintAnimation(batteryIcon);
    }

    public static void showBatteryLowDialogIfNeeded(final Launcher launcher, int batteryLevel, boolean isBatteryCharging) {

        // Charging
        if (isBatteryCharging) {
            return;
        }
        // Low battery
        if (batteryLevel > BATTER_LOW_LEVEL_FOR_REMIND) {
            return;
        }
        // Already in 'Save' mode
        if (BatteryModeManager.getLastMode() != BatteryModeActivity.ModeType.CURRENT_SAVER.ordinal()) {
            return;
        }
        // Show interval time should less than 6 hour.
        if (sBatteryLowTipShowTime == 0) {
            sBatteryLowTipShowTime = PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).getLong(PREF_KEY_BATTERY_TIP_SHOW_TIME, 0);
        }
        if ((System.currentTimeMillis() - sBatteryLowTipShowTime) < BATTERY_TIP_SHOW_INTERVAL_MILLS) {
            return;
        }

        if (Utils.isNewUserInDNDStatus()) {
            return;
        }

        // User cancel it more than 2 time
        int showCount= PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).
                    getInt(PREF_KEY_BATTERY_TIP_CANCEL_COUNT, 0);

        if (showCount > 2) {
            return;
        }

        LauncherTipManager.getInstance().showTip(launcher, LauncherTipManager.TipType.BATTERY_LOW, new Runnable() {
            @Override
            public void run() {
                // Show
                sBatteryLowTipShowTime = System.currentTimeMillis();
                PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).putLong(PREF_KEY_BATTERY_TIP_SHOW_TIME, sBatteryLowTipShowTime);
            }
        }, new Runnable() {
            @Override
            public void run() {
                // Confirm
                NavUtils.startActivity(launcher, BatteryActivity.class);
                HSAnalytics.logEvent("Battery_OpenFrom", "type", "From Dialog");
                HSAnalytics.logEvent("Battery_Dialog_LowPower_Show", "type", "Try");
            }
        }, new Runnable() {
            @Override
            public void run() {
                // Cancel
                PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).incrementAndGetInt(PREF_KEY_BATTERY_TIP_CANCEL_COUNT);
                HSAnalytics.logEvent("Battery_Dialog_LowPower_Show", "type", "Cancel");
            }
        });
    }

    public static void dismissBatteryLowDialog() {
        LauncherTipManager.getInstance().dismiss(LauncherTipManager.TipType.BATTERY_LOW);
    }

    public static boolean hasUserUsedBatteryRecently(long timeInMs) {
        long lastTime = PreferenceHelper.get(LauncherFiles.BATTERY_PREFS)
                .getLong(ResultConstants.PREF_KEY_LAST_BATTERY_USED_TIME, -1);
        return (System.currentTimeMillis() - lastTime) < timeInMs;
    }

    public static int getToolbarBatteryResId(int batteryNumber, boolean afterOptimize) {
        int resID;
        if (batteryNumber <= BatteryUtils.BATTERY_TOOLBAR_STATUS_A_LEVEL) {
            if (afterOptimize) {
                resID = R.drawable.battery_toolbar_first_optimized;
            } else {
                resID = R.drawable.battery_toolbar_first;
            }
        } else if (batteryNumber <= BatteryUtils.BATTERY_TOOLBAR_STATUS_B_LEVEL) {
            if (afterOptimize) {
                resID = R.drawable.battery_toolbar_second_optimized;
            } else {
                resID = R.drawable.battery_toolbar_second;
            }
        } else if (batteryNumber <= BatteryUtils.BATTERY_TOOLBAR_STATUS_C_LEVEL) {
            resID = R.drawable.battery_toolbar_third;
        } else {
            resID = R.drawable.battery_toolbar_forth;
        }
        return resID;
    }

    public static boolean canChangeToBoostBatteryIcon() {
        // use {date * 100 + count (2017050101)} record date and count
        int data = PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).getInt(PREF_KEY_BATTERY_ICON_CHANGED_TIME, 0);
        int date = data / 100;
        int count = data % 100;
        Calendar cal = Calendar.getInstance();
        int today = cal.get(Calendar.YEAR) * 10000 + cal.get(Calendar.MONTH) * 100 + cal.get(Calendar.DAY_OF_MONTH);
        if (BuildConfig.DEBUG) {
            HSLog.i("BoostNotification", "get Battery 日期：" + date + "  today：" + today + "  变换次数：" + count);
        }
        if (today == date) {
            return count < BATTERY_ICON_CHANGED_LIMIT;
        }
        return true;
    }

    public static void setChangeToBoostBatteryIcon() {
        // use {date * 100 + count (2017050101)} record date and count
        int data = PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).getInt(PREF_KEY_BATTERY_ICON_CHANGED_TIME, 0);
        int date = data / 100;
        int count = data % 100;
        Calendar cal = Calendar.getInstance();
        int today = cal.get(Calendar.YEAR) * 10000 + cal.get(Calendar.MONTH) * 100 + cal.get(Calendar.DAY_OF_MONTH);
        if (today == date) {
            count++;
        } else {
            count = 1;
        }
        data = today * 100 + count;
        if (BuildConfig.DEBUG) {
            HSLog.i("BoostNotification", "set Battery 日期：" + date + "  today：" + today + "  变换次数：" + count);
        }
        PreferenceHelper.get(LauncherFiles.BATTERY_PREFS).putInt(PREF_KEY_BATTERY_ICON_CHANGED_TIME, data);
    }

    public static boolean isBatteryNeedReloadIcon(int batteryLastNumber, int batteryNumber) {
        if (batteryLastNumber <= BatteryUtils.BATTERY_STATUS_A_LEVEL && batteryNumber <= BatteryUtils.BATTERY_STATUS_A_LEVEL) {
            return false;
        } else if (batteryLastNumber > BatteryUtils.BATTERY_STATUS_A_LEVEL && batteryLastNumber <= BatteryUtils.BATTERY_STATUS_B_LEVEL
            && batteryNumber > BatteryUtils.BATTERY_STATUS_A_LEVEL && batteryNumber <= BatteryUtils.BATTERY_STATUS_B_LEVEL) {
            return false;
        } else if (batteryLastNumber > BatteryUtils.BATTERY_STATUS_B_LEVEL && batteryLastNumber <= BatteryUtils.BATTERY_STATUS_C_LEVEL
            && batteryNumber > BatteryUtils.BATTERY_STATUS_B_LEVEL && batteryNumber <= BatteryUtils.BATTERY_STATUS_C_LEVEL) {
            return false;
        } else if (batteryLastNumber > BatteryUtils.BATTERY_STATUS_C_LEVEL && batteryLastNumber <= 100
                && batteryNumber > BatteryUtils.BATTERY_STATUS_C_LEVEL && batteryNumber <= 100) {
            return false;
        }
        return true;
    }

}
