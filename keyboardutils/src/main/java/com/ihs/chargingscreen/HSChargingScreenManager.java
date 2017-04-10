package com.ihs.chargingscreen;

import android.content.Intent;
import android.os.Build;

import com.acb.nativeads.AcbNativeAdManager;
import com.ihs.app.framework.HSApplication;
import com.ihs.charging.HSChargingManager;
import com.ihs.charging.HSChargingManager.HSChargingState;
import com.ihs.charging.HSChargingManager.IChargingListener;
import com.ihs.chargingscreen.activity.ChargingScreenActivity;
import com.ihs.chargingscreen.notification.ChargeNotifyManager;
import com.ihs.chargingscreen.utils.ChargingAnalytics;
import com.ihs.chargingscreen.utils.ChargingPrefsUtil;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;

import static com.ihs.charging.HSChargingManager.HSChargingState.STATE_CHARGING_FULL;
import static com.ihs.charging.HSChargingManager.HSChargingState.STATE_DISCHARGING;
import static com.ihs.chargingscreen.utils.ChargingPrefsUtil.CHARGING_DEFAULT_ACTIVE;
import static com.ihs.chargingscreen.utils.ChargingPrefsUtil.CHARGING_DEFAULT_DISABLED;
import static com.ihs.chargingscreen.utils.ChargingPrefsUtil.CHARGING_MUTED;
import static com.ihs.chargingscreen.utils.ChargingPrefsUtil.FULL_CHARGED_MAX_TIME;
import static com.ihs.chargingscreen.utils.ChargingPrefsUtil.PLUG_MAX_TIME;
import static com.ihs.chargingscreen.utils.ChargingPrefsUtil.UNPLUG_MAX_TIME;
import static com.ihs.chargingscreen.utils.ChargingPrefsUtil.USER_ENABLED_CHARGING;

/**
 * Created by zhixiangxiao on 6/2/16.
 */
public class HSChargingScreenManager {

    public String getNaitveAdsPlacementName() {
        return naitveAdsPlacementName;
    }

    public interface IChargingScreenListener {
        void onClosedByChargingPage();
    }

    private static HSChargingScreenManager instance;

    private boolean isChargingModuleOpened;
    private boolean showNativeAd;

    private String moduleName = "";
    private String naitveAdsPlacementName;

    private IChargingScreenListener iChargingScreenListener;

    public static HSChargingScreenManager getInstance() {
        return instance;
    }

    public synchronized static void init(boolean showNativeAd, String moduleName, String naitveAdsPlacementName, IChargingScreenListener chargingScreenListener) {
        if (instance == null) {
            instance = new HSChargingScreenManager(showNativeAd, moduleName, naitveAdsPlacementName, chargingScreenListener);

            registerChargingService();

            ChargeNotifyManager.getInstance().refreshChargingNotification();

        }
    }


    private HSChargingScreenManager(boolean showNativeAd, String moduleName, String placementName, IChargingScreenListener iChargingScreenListener) {

        AcbNativeAdManager.sharedInstance();
        this.naitveAdsPlacementName = placementName;
        this.showNativeAd = showNativeAd;
        this.moduleName = moduleName;
        this.iChargingScreenListener = iChargingScreenListener;

        HSChargingManager.getInstance().addChargingListener(new IChargingListener() {
            @Override
            public void onBatteryLevelChanged(int preBatteryLevel, int curBatteryLevel) {
                if (!HSChargingScreenManager.getInstance().isChargingModuleOpened()) {
                    return;
                }
                if (!HSChargingManager.getInstance().isCharging()) {
                    if (((curBatteryLevel == 21 && preBatteryLevel == 22) || (curBatteryLevel == 11 && preBatteryLevel == 12))) {
                        /**低电量提醒*/
                        ChargeNotifyManager.getInstance().pendingToShow(ChargeNotifyManager.PUSH_BATTERY_LOW_PRIORITY);
                        HSGlobalNotificationCenter.sendNotificationOnMainThread(Constants.EVENT_CHARGING_SHOW_PUSH);
                    }
                }
            }

            @Override
            public void onChargingStateChanged(HSChargingState preChargingState, HSChargingState curChargingState) {
                HSLog.e(preChargingState.toString() + " -- " + curChargingState.toString() + " -- " + HSChargingScreenManager.getInstance().isChargingModuleOpened());

                if (!HSChargingScreenManager.getInstance().isChargingModuleOpened() && !ChargingPrefsUtil.getInstance().getSpHelper().contains(USER_ENABLED_CHARGING)) {
                    //功能未开启时插电 并且5.0以下
                    if (canShowPlugNotification(preChargingState)) {
                        showChargingStateChangedNotification(ChargeNotifyManager.PUSH_ENABLE_WHEN_PLUG);
                    }

                    if (canShowUnplugNotification(preChargingState, curChargingState)) {
                        showChargingStateChangedNotification(ChargeNotifyManager.PUSH_ENABLE_WHEN_PLUG);
                    }

                    if (canShowFullChargNotification(preChargingState, curChargingState)) {
                        showChargingStateChangedNotification(ChargeNotifyManager.PUSH_ENABLE_WHEN_FULL_CHARGE);
                    }

                    return;
                }

                if (!isChargingModuleOpened) {
                    return;
                }
//
//                if (preChargingState == HSChargingState.STATE_DISCHARGING &&
//                        HSChargingManager.getInstance().getBatteryPluggedSource() == BatteryPluggedSource.USB) {
//                    /**低电压充电提醒*/
//                    if (HSChargingScreenManager.getInstance().isChargingModuleOpened()) {
//                        ChargeNotifyManager.getInstance().pendingToShow(ChargeNotifyManager.PUSH_LOW_VOLTAGE_PRIORITY);
//                    }
//                }
//
//
//                if (preChargingState != HSChargingState.STATE_DISCHARGING && preChargingState != HSChargingState.STATE_UNKNOWN
//                        && curChargingState == HSChargingState.STATE_DISCHARGING) {
//                    //拔电源
//                    ChargeNotifyManager.getInstance().cancelLowVoltagePush();
//
//                    if (HSChargingManager.getInstance().getBatteryRemainingPercent() <= 80) {
//                        ChargeNotifyManager.getInstance().pendingToShow(ChargeNotifyManager.PUSH_CUT_OFF_CHARGE_PRIORITY);
//                    }
//                }
//
                if (preChargingState == HSChargingState.STATE_DISCHARGING && curChargingState != HSChargingState.STATE_DISCHARGING) {
                    //插电
                    Intent intent1 = new Intent(HSApplication.getContext(), ChargingScreenActivity.class);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    HSApplication.getContext().startActivity(intent1);
                }

//                    ChargeNotifyManager.getInstance().pendingToShow(ChargeNotifyManager.PUSH_CHARGING_PRIORITY);
//                }
//
//                if (preChargingState == HSChargingState.STATE_CHARGING_CONTINUOUS && curChargingState == HSChargingState.STATE_CHARGING_TRICKLE) {
//                    //电量充至100，提示继续充10min
//                    ChargeNotifyManager.getInstance().pendingToShow(ChargeNotifyManager.PUSH_BATTERY_DOCTOR_PRIORITY);
//                }
//
//                if (preChargingState == HSChargingState.STATE_CHARGING_TRICKLE && curChargingState == HSChargingState.STATE_CHARGING_FULL) {
//                    //完全充满
//                    ChargeNotifyManager.getInstance().pendingToShow(ChargeNotifyManager.PUSH_FULL_CHARGE_PRIORITY);
//
//                }

//                HSGlobalNotificationCenter.sendNotificationOnMainThread(Constants.EVENT_CHARGING_SHOW_PUSH);
//                HSGlobalNotificationCenter.sendNotificationOnMainThread(Constants.EVENT_SYSTEM_BATTERY_CHARGING_STATE_CHANGED);
            }

            private boolean canShowFullChargNotification(HSChargingState preChargingState, HSChargingState curChargingState) {
                return preChargingState != STATE_CHARGING_FULL && curChargingState == STATE_CHARGING_FULL
                        && ChargingPrefsUtil.getInstance().isChagringNotifyMaxAppearTimesAcheived(FULL_CHARGED_MAX_TIME);
            }

            private void showChargingStateChangedNotification(int pushEnableWhenPlug) {
                ChargingAnalytics.getInstance().chargingEnableNotificationShowed();
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    ChargeNotifyManager.getInstance().pendingToShow(pushEnableWhenPlug);
                    HSGlobalNotificationCenter.sendNotificationOnMainThread(Constants.EVENT_CHARGING_SHOW_PUSH);
                }
                HSGlobalNotificationCenter.sendNotificationOnMainThread(Constants.EVENT_SYSTEM_BATTERY_CHARGING_STATE_CHANGED);
            }

            private boolean canShowUnplugNotification(HSChargingState preChargingState, HSChargingState curChargingState) {
                return getPreChargingState(preChargingState) > 0 && curChargingState == STATE_DISCHARGING
                        && ChargingPrefsUtil.getInstance().isChagringNotifyMaxAppearTimesAcheived(UNPLUG_MAX_TIME);
            }

            private boolean canShowPlugNotification(HSChargingState preChargingState) {
                return preChargingState == STATE_DISCHARGING && getChargingState() > 0
                        && ChargingPrefsUtil.getInstance().isChagringNotifyMaxAppearTimesAcheived(PLUG_MAX_TIME);

            }

            @Override
            public void onChargingRemainingTimeChanged(int chargingRemainingMinutes) {
//                ChargeNotifyManager.getInstance().refreshChargingNotification();
            }

            @Override
            public void onBatteryTemperatureChanged(float preBatteryTemperature, float curBatteryTemperature) {

            }
        });

    }


    public synchronized void start() {
        if (isChargingModuleOpened) {
            return;
        }

        ChargeNotifyManager.getInstance().registerScreenOffReceiver();
        ChargeNotifyManager.getInstance().removeNotification();

        isChargingModuleOpened = true;
    }

    public void stop() {
        stop(true);
    }

    public synchronized void stop(boolean isClosedByModule) {

        if (!isChargingModuleOpened) {
            return;
        }

        ChargeNotifyManager.getInstance().refreshChargingNotification();
        ChargeNotifyManager.getInstance().unregisterScreenOffReceiver();
        isChargingModuleOpened = false;

        if (iChargingScreenListener != null && isClosedByModule) {
            iChargingScreenListener.onClosedByChargingPage();
        }

    }

    public String getModuleName() {
        return moduleName;
    }

    public boolean isChargingModuleOpened() {
        return isChargingModuleOpened;
    }

    public boolean isShowNativeAd() {
        return showNativeAd;
    }


    public static void registerChargingService() {
        HSApplication.getContext().startService(new Intent(HSApplication.getContext(), KeepAliveService.class));
        int chargingEnabled = ChargingPrefsUtil.getInstance().getChargingEnableStates();
        switch (chargingEnabled) {
            case CHARGING_MUTED:
            default:
                return;
            case CHARGING_DEFAULT_DISABLED:
                HSChargingScreenManager.getInstance().stop(false);
                break;
            case CHARGING_DEFAULT_ACTIVE:
                HSChargingScreenManager.getInstance().start();
                break;
        }
        HSChargingManager.getInstance().start();
    }


    private int getPreChargingState(HSChargingState preState) {
        int chargingState = 0;
        switch (preState) {
            case STATE_DISCHARGING:

                chargingState = -1;
                break;
            case STATE_CHARGING_SPEED:
            case STATE_CHARGING_CONTINUOUS:
            case STATE_CHARGING_TRICKLE:
                chargingState = 1;

                break;
            case STATE_CHARGING_FULL:
                chargingState = 2;

                break;
            default:
                chargingState = 0;


                break;
        }
        return chargingState;
    }

    public static int getChargingState() {
        int chargingState = 0;
        switch (HSChargingManager.getInstance().getChargingState()) {
            case STATE_DISCHARGING:

                chargingState = -1;
                break;
            case STATE_CHARGING_SPEED:
            case STATE_CHARGING_CONTINUOUS:
            case STATE_CHARGING_TRICKLE:
                chargingState = 1;

                break;
            case STATE_CHARGING_FULL:
                chargingState = 2;

                break;
            default:
                chargingState = 0;


                break;
        }
        return chargingState;
    }
}
