package com.ihs.chargingscreen;

import android.content.Intent;
import android.os.Build;

import com.acb.nativeads.AcbNativeAdManager;
import com.ihs.app.framework.HSApplication;
import com.ihs.charging.HSChargingManager;
import com.ihs.charging.HSChargingManager.HSChargingState;
import com.ihs.charging.HSChargingManager.IChargingListener;
import com.ihs.chargingscreen.notification.ChargeNotifyManager;
import com.ihs.chargingscreen.utils.ChargingAnalytics;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.chargingscreen.utils.ChargingPrefsUtil;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
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

    private static HSChargingScreenManager instance;

    private boolean isChargingModuleOpened;
    private boolean showNativeAd;

    private String naitveAdsPlacementName;

    public static HSChargingScreenManager getInstance() {
        return instance;
    }

    public synchronized static void init(boolean showNativeAd, String naitveAdsPlacementName) {
        if (instance == null) {
            instance = new HSChargingScreenManager(showNativeAd, naitveAdsPlacementName);

            registerChargingService();

            ChargeNotifyManager.getInstance().refreshChargingNotification();

            HSGlobalNotificationCenter.addObserver(HSConfig.HS_NOTIFICATION_CONFIG_CHANGED, new INotificationObserver() {
                @Override
                public void onReceive(String s, HSBundle hsBundle) {
                    registerChargingService();
                }
            });

        }
    }


    private HSChargingScreenManager(boolean showNativeAd, String placementName) {

        AcbNativeAdManager.sharedInstance();
        this.naitveAdsPlacementName = placementName;
        this.showNativeAd = showNativeAd;

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

                if (preChargingState == HSChargingState.STATE_DISCHARGING && curChargingState != HSChargingState.STATE_DISCHARGING) {
                    //插电
                    ChargingManagerUtil.startChargingActivity();
                }

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

    public synchronized void stop() {

        if (!isChargingModuleOpened) {
            return;
        }

        ChargeNotifyManager.getInstance().refreshChargingNotification();
        ChargeNotifyManager.getInstance().unregisterScreenOffReceiver();
        isChargingModuleOpened = false;
    }

    public boolean isChargingModuleOpened() {
        return isChargingModuleOpened;
    }

    public boolean isShowNativeAd() {
        return showNativeAd;
    }


    public static void registerChargingService() {
        HSApplication.getContext().startService(new Intent(HSApplication.getContext(), AgentService.class));
        int chargingEnabled = ChargingPrefsUtil.getInstance().getChargingEnableStates();
        switch (chargingEnabled) {
            case CHARGING_MUTED:
            case CHARGING_DEFAULT_DISABLED:
            default:
                HSChargingScreenManager.getInstance().stop();
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
