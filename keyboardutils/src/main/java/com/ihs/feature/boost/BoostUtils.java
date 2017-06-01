package com.ihs.feature.boost;

import android.app.ActivityManager;
import android.content.Context;

import com.ihs.feature.common.CompatUtils;
import com.ihs.feature.common.LauncherConfig;

import java.util.Random;

public class BoostUtils {

    private static final String PREF_KEY_BOOST_PLUS_ONCE_FOREVER = "boost_plus_last_once_forever";

    public static boolean shouldEnableBoostPlusConfig() {
        return LauncherConfig.getVariantBoolean("Application", "BoostPlus", "AutoCleanFeatureEnabled");
    }

    public static boolean shouldEnableBoostPlusFeature() {
        return shouldEnableBoostPlusConfig() && isDeviceSupportAutoClean();
    }

    private static boolean isDeviceSupportAutoClean() {
        return !CompatUtils.IS_XIAOMI_DEVICE;
    }

    public static void updateConfigOnFirstLaunch() {
        //AB TEST CODE for auto clean.
//        boolean isFirstLaunch = HSVersionControlUtils.isFirstLaunchSinceInstallation();
//        if (isFirstLaunch) {
//            HSAnalytics.logEvent("App_FirstStart_AutoClean_" + (shouldEnableBoostPlusConfig() ? "Open" : "Closed"));
//        }
//        if (isFirstLaunch && shouldEnableBoostPlusConfig()) {
//            HSLog.d("BoostUtil", "Enable boost plus forever(Except clear data, upgrade)");
//            HSAnalytics.logEvent("Boost_Plus_Auto_Boost_Switch_On");
//            PreferenceHelper.get(LauncherFiles.BOOST_PREFS).putBoolean(PREF_KEY_BOOST_PLUS_ONCE_FOREVER, true);
//        }
    }

    private static final Random mRand = new Random();

    public static int getBoostedMemSizeBytes(Context context, int boostedPercentage) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        int boostedSizeBytes = Math.round(memoryInfo.totalMem *
                // Dither the percentage to make it looks real
                (boostedPercentage + mRand.nextFloat() - 0.5f) / 100f);
        if (boostedSizeBytes <= 0) {
            boostedSizeBytes = mRand.nextInt(3) + 1;
        }
        return boostedSizeBytes;
    }

    public static int getExtendedBatteryLife(int boostedPercentage) {
        // Current battery level in percentage
        int batteryLevel = DeviceManager.getInstance().getBatteryLevel();

        // We shamelessly assume that any full battery can be used for exactly 24 hours
        float estimatedRemainingTimeMinutes = 24 * 60 * (batteryLevel / 100f);

        // And we further assume that if we boost a 100% RAM usage down to 0%,
        // the remaining battery life is extended by 30%
        float estimatedExtendedTime = estimatedRemainingTimeMinutes * (boostedPercentage / 100f) * 0.3f;

        // How dare we don't add some randomness
        return Math.round(estimatedExtendedTime * (0.8f + mRand.nextFloat() * 0.4f));
    }

    public static float getCooledCpuTemperature(int boostedPercentage) {
        // On our planet, boosting a 100% RAM usage down to 0% means the CPU temperature dropping by 12 degrees. Period.
        float estimatedTemperatureDrop = 12f * (boostedPercentage / 100f);
        return estimatedTemperatureDrop * (0.8f + mRand.nextFloat() * 0.4f);
    }
}
