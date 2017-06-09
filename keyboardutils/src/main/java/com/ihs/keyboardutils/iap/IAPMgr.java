package com.ihs.keyboardutils.iap;

import android.text.TextUtils;

import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.ihs.iap.HSIAPManager;

import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by liuzhongtao on 17/6/6.
 *
 */

public class IAPMgr {
    public static final String NOTIFICATION_REMOVEADS_PURCHASED = "NOTIFICATION_REMOVEADS_PURCHASED";

    private static final String TAG = "Cam_IAP_Tag";

    private static volatile IAPMgr instance;

    private boolean isPurchasingRemoveAds = false;

    public static IAPMgr getInstance() {
		if(instance == null) {
			synchronized(IAPMgr.class) {
				if (instance == null) {
					instance = new IAPMgr();
				}
			}
		}
		return instance;
    }

	public void init() {
        List<String>  inAppNonConsumableSkuList = null;
        String removeAdsId = getRemoveAdsIapId();
        if (!TextUtils.isEmpty(removeAdsId)) {
            inAppNonConsumableSkuList = Collections.singletonList(removeAdsId);
        }
        HSIAPManager.getInstance().init(null, inAppNonConsumableSkuList);
    }

    public boolean needShowAds() {
        return !HSIAPManager.getInstance().hasOwnedSku(getRemoveAdsIapId());
    }

    public void purchaseRemoveAds() {
        if (!needShowAds()) {
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

    private String getRemoveAdsIapId() {
         return HSConfig.optString("", "Application", "RemoveAds", "iapID");
    }
}
