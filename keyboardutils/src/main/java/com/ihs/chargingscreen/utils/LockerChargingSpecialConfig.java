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
import com.ihs.commons.utils.HSPreferenceHelper;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by Arthur on 17/12/7.
 * 为了判断特殊版本的Charging和Locker配置
 */

public class LockerChargingSpecialConfig {

    public static final int NOT_SPECIAL_USER = 0;
    public static final int CLASSIC_LOCKER_TYPE = 1;
    public static final int PREMIUM_LOCKER_TYPE = 2;

    private static final String LOCKER_TYPE_PREF = "locker_type_pref";

    private static LockerChargingSpecialConfig instance;

    private LockerConnection lockerConnection;
    private boolean showAd = true;

    public boolean shouldShowAd() {
        return showAd || HSConfig.optBoolean(true, "Application", "Locker", "Ads", "NewUserShowAd");
    }

    private static final String ACTION_BIND_SERVICE = "action.customize.service";

    /**
     * 用做判断当前版本是否为特殊用户版本
     * 此功能只针对tiger和Master：
     * 当前版本locker和chargingLocker的state如果是0，则
     * 1、老用户升级至此版本时，保留其之前的state状态。
     * 2、新用户，如果用户通过一些途径(比如set as lockscreen等)开启了锁屏，不要显示广告。
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
        if (!HSPreferenceHelper.getDefault().contains(LOCKER_TYPE_PREF)) {
            this.lockerType = lockerType;
            HSPreferenceHelper.getDefault().putInt(LOCKER_TYPE_PREF, lockerType);
        } else {
            this.lockerType = HSPreferenceHelper.getDefault().getInt(LOCKER_TYPE_PREF, PREMIUM_LOCKER_TYPE);
        }
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

    public boolean isClassicType() { return lockerType == CLASSIC_LOCKER_TYPE; }

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