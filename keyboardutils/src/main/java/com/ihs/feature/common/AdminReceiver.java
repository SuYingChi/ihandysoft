package com.ihs.feature.common;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;

public class AdminReceiver extends DeviceAdminReceiver {

    private static final String TAG = "DeviceAdmin";

    private static final String PREF_KEY_IS_DEVICE_ADMIN = "is_device_admin";

    private static Runnable sEnabledCallback;

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        HSLog.i(TAG, "Device admin enabled");
        HSPreferenceHelper.getDefault().putBoolean(PREF_KEY_IS_DEVICE_ADMIN, true);
        if (sEnabledCallback != null) {
            sEnabledCallback.run();
            sEnabledCallback = null;
        }
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
        HSLog.i(TAG, "Device admin disabled");
        HSPreferenceHelper.getDefault().putBoolean(PREF_KEY_IS_DEVICE_ADMIN, false);
    }

    public static boolean isDeviceAdmin() {
        return HSPreferenceHelper.getDefault().getBoolean(PREF_KEY_IS_DEVICE_ADMIN, false);
    }

    public static boolean isActiveAdmin(Context context) {
        DevicePolicyManager dpm = getDevicePolicyManager(context);
        ComponentName admin = new ComponentName(context, AdminReceiver.class);
        boolean active = dpm.isAdminActive(admin);
        HSLog.i(TAG, "deviceAdminActive: " + active);
        return active;
    }

    public static boolean deactiveAdmin(Context context) {
        try {
            DevicePolicyManager dpm = getDevicePolicyManager(context);
            ComponentName admin = new ComponentName(context, AdminReceiver.class);
            dpm.removeActiveAdmin(admin);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static DevicePolicyManager getDevicePolicyManager(Context context) {
        return (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    public static boolean lockNow(Context context) {
        DevicePolicyManager dpm = getDevicePolicyManager(context);
        if (dpm.isAdminActive(new ComponentName(context, AdminReceiver.class))) {
            try {
                dpm.lockNow();
                return true;
            } catch (Exception e) {
                HSLog.e(e.getMessage());
            }
        }
        return false;
    }


    public static void startObservingDeviceAdminStatus(Runnable callback) {
        sEnabledCallback = callback;
    }
}
