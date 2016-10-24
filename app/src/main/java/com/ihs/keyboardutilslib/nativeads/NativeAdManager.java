package com.ihs.keyboardutilslib.nativeads;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.util.ArrayMap;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.ihs.nativeads.base.api.AdVendorOption;
import com.ihs.nativeads.base.api.HSNativeAd;
import com.ihs.nativeads.base.api.PreCacheOption;
import com.ihs.nativeads.pool.api.HSNativeAdPool;
import com.ihs.nativeads.pool.api.INativeAdPoolListener;

import java.util.List;
import java.util.Map;

/**
 * Created by ihandysoft on 16/10/20.
 */

public class NativeAdManager {

    private static NativeAdManager nativeAdManager;
    private ArrayMap<String, HSNativeAdPool> nativeAdsPools;
    private ArrayMap<String, HSNativeAd> currentNativeAds;
    private ArrayMap<String, Long> currentNativeAdFetchTimes;

    private NativeAdManager() {
        initSupportNativeAdPools();
        currentNativeAds = new ArrayMap<>();
        currentNativeAdFetchTimes = new ArrayMap<>();
    }

    public static void init() {
        if (nativeAdManager == null) {
            synchronized (NativeAdManager.class) {
                if (nativeAdManager == null) {
                    nativeAdManager = new NativeAdManager();
                }
            }
        }
    }

    public static NativeAdManager getInstance() {
        if (nativeAdManager == null) {
            init();
        }
        return nativeAdManager;
    }

    private void initSupportNativeAdPools() {
        nativeAdsPools = new ArrayMap<>();
        for (Map.Entry entry : HSConfig.getMap("nativeAdsPool").entrySet()) {
            if (isNetworkAvailable(HSApplication.getContext()) && (entry.getValue() instanceof Map) && HSConfig.getBoolean("Application", "NativeAds", "isShow" + entry.getKey().toString())) {
                nativeAdsPools.put(entry.getKey().toString(), createNativeAdPool(entry.getKey().toString()));
            }
        }
    }

    private HSNativeAdPool createNativeAdPool(final String poolName) {
        AdVendorOption vendorOption = new AdVendorOption(new PreCacheOption(true, true));
        vendorOption.setMediaType(AdVendorOption.MediaType.IMAGE);
        HSNativeAdPool hsNativeAdPool = new HSNativeAdPool(poolName, poolName, HSNativeAdPool.AdStrategy.APP_POOL, vendorOption);
        hsNativeAdPool.addListener(new INativeAdPoolListener() {

            @Override
            public void onAdWillExpire(HSNativeAd hsNativeAd) {

            }

            @Override
            public void onAvailableAdCountChanged(int i) {
                HSLog.e("onAvailableAdCountChanged - count : " + i);
                HSGlobalNotificationCenter.sendNotification(poolName);
            }
        });

        return hsNativeAdPool;
    }

    HSNativeAd getNativeAd(String poolName) {
        HSNativeAdPool hsNativeAdPool = nativeAdsPools.get(poolName);
        if (hsNativeAdPool != null) {
            List<HSNativeAd> ads = hsNativeAdPool.getAds(1);
            if (ads.size() > 0) {
                HSNativeAd nativeAd = ads.get(0);
                HSNativeAd oldNativeAd = currentNativeAds.get(poolName);
                if (oldNativeAd != null) {
                    oldNativeAd.release();
                }
                currentNativeAds.put(poolName, nativeAd);
                currentNativeAdFetchTimes.put(poolName, System.currentTimeMillis());
                return nativeAd;
            }
            return getCurrentNativeAd(poolName);
        }
        return null;
    }

    HSNativeAd getCurrentNativeAd(String poolName) {
        HSNativeAd oldNativeAd = currentNativeAds.get(poolName);
        if (oldNativeAd != null) {
            return oldNativeAd;
        }
        return null;
    }

    long getCurrentNativeAdFetchTime(String poolName) {
        return currentNativeAdFetchTimes.get(poolName) == null ? 0 : currentNativeAdFetchTimes.get(poolName);
    }

    public void releaseAllNativeAdPools() {
        for (Map.Entry<String, HSNativeAd> entry : currentNativeAds.entrySet()) {
            entry.getValue().release();
        }
        currentNativeAds.clear();
        for (Map.Entry<String, HSNativeAdPool> entry : nativeAdsPools.entrySet()) {
            entry.getValue().release();
        }
        nativeAdsPools.clear();
    }


    /**
     * 检查当前网络是否可用
     *
     * @param context
     * @return
     */

    private boolean isNetworkAvailable(Context context) {
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        } else {
            // 获取NetworkInfo对象
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

            if (networkInfo != null && networkInfo.length > 0) {
                for (int i = 0; i < networkInfo.length; i++) {
                    // 判断当前网络状态是否为连接状态
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
