package com.ihs.chargingscreen.utils;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.fasttrack.lockscreen.ICustomizeInterface;
import com.ihs.commons.config.HSConfig;

/**
 * Created by Arthur on 17/12/7.
 * 为了判断特殊版本的Charging和Locker配置
 */

public class LockerChargingSpecialConfig {

    public static final int NOT_SPECIAL_USER = 0;
    public static final int SPECIAL_USER_OLD = 1;
    public static final int SPECIAL_USER_NEW = 2;

    private static LockerChargingSpecialConfig instance;

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
//        Intent lockerIntent = new Intent(ACTION_BIND_SERVICE);
//        lockerIntent.setPackage(BOUND_SERVICE_PACKAGE);
//        LockerConnection lockerConnection = new LockerConnection();
//        HSApplication.getContext().bindService(lockerIntent, lockerConnection, BIND_AUTO_CREATE);
        return instance;
    }

    public void init(int noAdsVersionUserType) {
        this.noAdsVersionUserType = noAdsVersionUserType;

    }

    public boolean canShowAd() {
        return noAdsVersionUserType != SPECIAL_USER_NEW || HSConfig.optBoolean(false, "Application", "Locker", "Ads", "NewUserShowAd");
    }


    private static class LockerConnection implements ServiceConnection {
        private boolean isLockerEnable = false;

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ICustomizeInterface iCustomizeInterface = ICustomizeInterface.Stub.asInterface(iBinder);
            try {
                isLockerEnable = iCustomizeInterface.isLockerEnable();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isLockerEnable = false;
        }

        public boolean isLockerEnable() {
            return isLockerEnable;
        }
    }
}