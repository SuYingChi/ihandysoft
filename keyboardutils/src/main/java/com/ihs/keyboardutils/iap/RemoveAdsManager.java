package com.ihs.keyboardutils.iap;

import android.os.Handler;

import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.iap.HSIAPManager;

import org.json.JSONObject;

/**
 * Created by liuzhongtao on 17/6/6.
 *
 */

public class RemoveAdsManager {
    public static final String NOTIFICATION_REMOVEADS_PURCHASED = "NOTIFICATION_REMOVEADS_PURCHASED";

    private static final String TAG = "RemoveAdsManager";

    private static volatile RemoveAdsManager instance;

    private boolean isPurchasingRemoveAds = false;
    private boolean needsServerVerification = false;

    private String removeAdsIapId;

    public static RemoveAdsManager getInstance() {
		if(instance == null) {
			synchronized(RemoveAdsManager.class) {
				if (instance == null) {
					instance = new RemoveAdsManager();
				}
			}
		}
		return instance;
    }

    private RemoveAdsManager() {
        removeAdsIapId = HSConfig.optString("", "Application", "RemoveAds", "iapID");
        HSGlobalNotificationCenter.addObserver(HSConfig.HS_NOTIFICATION_CONFIG_CHANGED, new INotificationObserver() {
            @Override
            public void onReceive(String s, HSBundle hsBundle) {
                removeAdsIapId = HSConfig.optString("", "Application", "RemoveAds", "iapID");
            }
        });
    }

    /**
     * 设置去广告购买是否需要服务器验证， 默认不需要
     * @param needsServerVerification bool
     */
    public void setNeedsServerVerification(boolean needsServerVerification) {
        this.needsServerVerification = needsServerVerification;
    }

    public boolean isRemoveAdsPurchased() {
        // TODO: 删除测试代码及配置
        if (HSLog.isDebugging() && HSConfig.optBoolean(false, "libIAP", "Purchased")) {
            return true;
        }

        return HSIAPManager.getInstance().hasOwnedSku(removeAdsIapId);
    }

    public void purchaseRemoveAds() {
        if (isRemoveAdsPurchased()) {
            return;
        }

        // TODO: 删除测试代码
        if (HSLog.isDebugging() && HSConfig.optBoolean(false, "libIAP", "PurchasedAction")) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    HSGlobalNotificationCenter.sendNotification(NOTIFICATION_REMOVEADS_PURCHASED);
                }
            }, 5000);
        }

        if (isPurchasingRemoveAds) {
            HSLog.e(TAG, "Purchasing RemoveAds now");
            return;
        }
        isPurchasingRemoveAds = true;

        HSIAPManager.getInstance().purchase(needsServerVerification ? HSIAPManager.VERIFICATION_ON_SERVER : HSIAPManager.VERIFICATION_ON_DEVICE, removeAdsIapId, new HSIAPManager.HSPurchaseListener() {
            @Override
            public void onPurchaseSucceeded(String s) {
                HSLog.d("onPurchaseSucceeded: " + s);
            }

            @Override
            public void onPurchaseFailed(String s, int i, String s1) {
                HSLog.d(TAG, "onPurchaseFailed: " + s + " Error: " + s1 + " (" + i + ")");
                isPurchasingRemoveAds = false;
            }

            @Override
            public void onVerifySucceeded(String s, JSONObject jsonObject) {
                HSLog.d(TAG, "onVerifySucceeded: " + s + "json: " + jsonObject.toString());

                HSGlobalNotificationCenter.sendNotification(NOTIFICATION_REMOVEADS_PURCHASED);
                isPurchasingRemoveAds = false;
            }

            @Override
            public void onVerifyFailed(String s, int i, String s1) {
                HSLog.d(TAG, "onVerifyFailed: " + s + " Error: " + s1 + " (" + i + ")");

                isPurchasingRemoveAds = false;
            }
        });
    }
}
