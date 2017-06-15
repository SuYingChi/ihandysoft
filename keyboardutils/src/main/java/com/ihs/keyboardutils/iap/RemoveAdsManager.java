package com.ihs.keyboardutils.iap;

import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
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

    /**
     * 设置去广告购买是否需要服务器验证， 默认不需要
     * @param needsServerVerification bool
     */
    public void setNeedsServerVerification(boolean needsServerVerification) {
        this.needsServerVerification = needsServerVerification;
    }

    public boolean isRemoveAdsPurchased() {
        return HSIAPManager.getInstance().hasOwnedSku(getRemoveAdsIapId());
    }

    public void purchaseRemoveAds() {
        if (isRemoveAdsPurchased()) {
            return;
        }

        if (isPurchasingRemoveAds) {
            HSLog.e(TAG, "Purchasing RemoveAds now");
            return;
        }
        isPurchasingRemoveAds = true;

        HSIAPManager.getInstance().purchase(getRemoveAdsIapId(), new HSIAPManager.HSPurchaseListener() {
            @Override
            public void onPurchaseSucceeded(String s) {
                HSLog.d("onPurchaseSucceeded: " + s);

                if (!needsServerVerification) {
                    HSGlobalNotificationCenter.sendNotification(NOTIFICATION_REMOVEADS_PURCHASED);
                    isPurchasingRemoveAds = false;
                }
            }

            @Override
            public void onPurchaseFailed(String s, int i, String s1) {
                HSLog.d(TAG, "onPurchaseFailed: " + s + " Error: " + s1 + " (" + i + ")");
                isPurchasingRemoveAds = false;
            }

            @Override
            public void onVerifySucceeded(String s, JSONObject jsonObject) {
                HSLog.d(TAG, "onVerifySucceeded: " + s + "json: " + jsonObject.toString());

                if (needsServerVerification) {
                    HSGlobalNotificationCenter.sendNotification(NOTIFICATION_REMOVEADS_PURCHASED);
                    isPurchasingRemoveAds = false;
                }
            }

            @Override
            public void onVerifyFailed(String s, int i, String s1) {
                HSLog.d(TAG, "onVerifyFailed: " + s + " Error: " + s1 + " (" + i + ")");

                if (needsServerVerification) {
                    isPurchasingRemoveAds = false;
                }
            }
        });
    }

    public String getRemoveAdsIapId() {
         return HSConfig.optString("", "Application", "RemoveAds", "iapID");
    }
}
