package com.ihs.chargingscreen.utils;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.artw.lockscreen.common.LockerChargingScreenUtils;
import com.artw.lockscreen.lockerappguide.LockerAppGuideManager;
import com.fasttrack.lockscreen.ICustomizeInterface;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by Arthur on 17/12/7.
 * 为了判断特殊版本的Charging和Locker配置
 */

public class LockerChargingSpecialConfig {

    public static final int NOT_SPECIAL_USER = 0;
    public static final int CLASSIC_LOCKER_TYPE = 1;
    public static final int PREMIUM_LOCKER_TYPE = 2;

    private static LockerChargingSpecialConfig instance;

    private LockerConnection lockerConnection;
    private boolean showAd = true;

    public boolean shouldShowAd() {
        if (lockerType != PREMIUM_LOCKER_TYPE) {
            return showAd || HSConfig.optBoolean(true, "Application", "Locker", "Ads", "NewUserShowAd");
        }
        return showAd;
    }

    private static final String ACTION_BIND_SERVICE = "action.customize.service";

    /**
     * 用做判断当前锁屏样式 classic(旧) or premium(新)
     *
     * 对于style
     * 新用户有可能使用新版锁屏，新版锁屏没有广告
     * 老用户沿用之前配置，使用旧版锁屏
     *
     * 对于tiger master
     * 新老用户均使用旧锁屏
     * plist控制新用户的锁屏是否显示广告
     */
    private int lockerType = 0;


    public static LockerChargingSpecialConfig getInstance() {
        if (instance == null) {
            instance = new LockerChargingSpecialConfig();
        }
        return instance;
    }

    public void init(int lockerType) {
        boolean shouldShowAd = lockerType != PREMIUM_LOCKER_TYPE;
        init(lockerType, shouldShowAd);
        this.lockerType = lockerType;
    }

    public void init(int lockerType, boolean showAd) {
        this.lockerType = lockerType;
        initLockerType();
        Intent lockerIntent = new Intent(ACTION_BIND_SERVICE);
        lockerIntent.setPackage(LockerAppGuideManager.getLockerAppPkgName());
        lockerConnection = new LockerConnection();
        HSApplication.getContext().bindService(lockerIntent, lockerConnection, BIND_AUTO_CREATE);
        this.showAd = showAd;
    }

    private void initLockerType() {
        if (isPremiumType()) {
            LockerChargingScreenUtils.setLockerStyle(LockerChargingScreenUtils.LOCKER_STYLE_ACTIVITY_PREMIUM);
        } else if (isClassicType()) {
            LockerChargingScreenUtils.setLockerStyle(LockerChargingScreenUtils.LOCKER_STYLE_ACTIVITY_CLASSIC);
        } else {
            LockerChargingScreenUtils.setLockerStyle(LockerChargingScreenUtils.LOCKER_STYLE_WINDOW);
        }
    }

    public boolean canShowAd() {
        return lockerType != PREMIUM_LOCKER_TYPE || HSConfig.optBoolean(false, "Application", "Locker", "Ads", "NewUserShowAd");
    }

    public boolean isPremiumType() {
        return lockerType == PREMIUM_LOCKER_TYPE;
    }

    public boolean isClassicType() {
        return lockerType == CLASSIC_LOCKER_TYPE;
    }

    public boolean isLockerEnable() {
        return lockerConnection != null && lockerConnection.isLockerEnable();
    }

    private class LockerConnection implements ServiceConnection {
        private boolean isLockerEnable = false;
        private ICustomizeInterface iCustomizeInterface;

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            iCustomizeInterface = ICustomizeInterface.Stub.asInterface(iBinder);
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

        private boolean isLockerEnable() {
            try {
                isLockerEnable = iCustomizeInterface != null && iCustomizeInterface.isLockerEnable();
                HSLog.e("xunling", String.valueOf(isLockerEnable));
                return isLockerEnable;
            } catch (RemoteException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}