package com.ihs.chargingscreen.utils;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.artw.lockscreen.LockerSettings;
import com.fasttrack.lockscreen.ICustomizeInterface;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by Arthur on 17/12/7.
 * 为了判断特殊版本的Charging和Locker配置
 */

public class LockerChargingSpecialConfig {

    public static final int NOT_SPECIAL_USER = 0;
    public static final int SPECIAL_USER_OLD = 1;
    public static final int SPECIAL_USER_NEW = 2;

    private static LockerChargingSpecialConfig instance;

    private LockerConnection lockerConnection;
    private Intent lockerIntent;
    
    static final String BOUND_SERVICE_PACKAGE = HSConfig.getString("Application", "Locker", "AppName");
    private static final String ACTION_BIND_SERVICE = "action.customize.service";

    /**
     * 用做判断当前版本是否为特殊用户版本
     * 此功能只针对tiger和Master：
     * 当前版本locker和chargingLocker的state如果是0，则
     * 1、老用户升级至此版本时，保留其之前的state状态。
     * 2、新用户，如果用户通过一些途径(比如set as lockscreen等)开启了锁屏，不要显示广告。
     */
    private int noAdsVersionUserType = 0;


    public static LockerChargingSpecialConfig getInstance() {
        if (instance == null) {
            instance = new LockerChargingSpecialConfig();
        }
        return instance;
    }

    public void init(int noAdsVersionUserType) {
        this.noAdsVersionUserType = noAdsVersionUserType;
        lockerIntent = new Intent(ACTION_BIND_SERVICE);
        lockerIntent.setPackage(BOUND_SERVICE_PACKAGE);
        lockerConnection = new LockerConnection();
        boolean success = HSApplication.getContext().bindService(lockerIntent, lockerConnection, BIND_AUTO_CREATE);
        if (!success) {
            enableLockerForSpecialUser();
        }
    }

    public void rebindService() {
        if (!isSpecialNewUser()) {
            return;
        }
        HSApplication.getContext().unbindService(lockerConnection);
        lockerConnection = new LockerConnection();
        boolean success = HSApplication.getContext().bindService(lockerIntent, lockerConnection, BIND_AUTO_CREATE);
        if (!success) {
            enableLockerForSpecialUser();
        }
    }

    public boolean canShowAd() {
        return noAdsVersionUserType != SPECIAL_USER_NEW || HSConfig.optBoolean(false, "Application", "Locker", "Ads", "NewUserShowAd");
    }

    public void enableLockerForSpecialUser() {
        if (LockerSettings.isSpecialUserEnableLockerBefore()) { //仅special user 才有可能返回true
            LockerSettings.setLockerEnabled(true);
        }
        if (ChargingPrefsUtil.getInstance().isChargingEnableBySpecialUSer()) { //同上
            ChargingManagerUtil.enableCharging(false);
        }
    }

    private void disableLockerForSpecialUser() {
        if (!canShowAd()) {
            LockerSettings.setLockerEnabled(false);
            ChargingManagerUtil.disableCharging();
        }
    }

    public boolean isSpecialNewUser() {
        return noAdsVersionUserType == SPECIAL_USER_NEW;
    }

    public boolean isLockerEnable() {
        return lockerConnection != null && lockerConnection.isLockerEnable();
    }

    private class LockerConnection implements ServiceConnection {
        private boolean isLockerEnable = false;

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ICustomizeInterface iCustomizeInterface = ICustomizeInterface.Stub.asInterface(iBinder);
            try {
                isLockerEnable = iCustomizeInterface.isLockerEnable();
                if (!isLockerEnable) {
                    enableLockerForSpecialUser();
                } else {
                    disableLockerForSpecialUser();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isLockerEnable = false;
        }

        private boolean isLockerEnable() {
            return isLockerEnable;
        }
    }
}