package com.ihs.keyboardutils.nativeads;

import android.support.v4.util.ArrayMap;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.nativeads.base.api.AdVendorOption;
import com.ihs.nativeads.base.api.HSNativeAd;
import com.ihs.nativeads.base.api.PreCacheOption;
import com.ihs.nativeads.pool.api.HSNativeAdPool;
import com.ihs.nativeads.pool.api.INativeAdPoolListener;

import java.util.List;

/**
 * Created by dongdong.han on 16/10/20.
 */

public class NativeAdManager {

    public static String NOTIFICATION_NEW_AD = "NEW_NATIVE_AD_NOTIFICATION";
    public static String NATIVE_AD_POOL_NAME = "NATIVE_AD_POOL_NAME";

    private static NativeAdManager nativeAdManager;
    private ArrayMap<String, NativeAdProxy> nativeAdProxies;

    private NativeAdManager() {
        nativeAdProxies = new ArrayMap<>();
        for (String poolName : NativeAdConfig.getAvailablePoolNames()) {
            nativeAdProxies.put(poolName, new NativeAdProxy(poolName));
        }
    }

    public static NativeAdManager getInstance() {
        if (nativeAdManager == null) {
            synchronized (NativeAdManager.class) {
                if (nativeAdManager == null) {
                    nativeAdManager = new NativeAdManager();
                }
            }
        }
        return nativeAdManager;
    }

    public boolean existNativeAd(String poolName) {
        if (nativeAdProxies != null && nativeAdProxies.containsKey(poolName)) {
            return nativeAdProxies.get(poolName).existNativeAd();
        }
        return false;
    }

    NativeAdProxy getNativeAdProxy(String poolName) {
        if (nativeAdProxies != null && nativeAdProxies.containsKey(poolName)) {
            return nativeAdProxies.get(poolName);
        }
        return null;
    }

    public static final class NativeAdProxy {
        /** 广告池名字 **/
        private String poolName;
        /** 广告池 **/
        private HSNativeAdPool hsNativeAdPool;
        /** 广告池最近提供的广告 **/
        private HSNativeAd cachedNativeAd;
        /** 当前广告实际展示的时间 **/
        private long cachedNativeAdShowedTime;
        /** 是否停止发送广告池数量改变通知 **/
        private boolean stopAvailableAdCountChangedNotification = false;

        NativeAdProxy(String poolName) {
            this.poolName = poolName;
            createNativeAdPool();
        }

        private void createNativeAdPool() {
            AdVendorOption vendorOption = new AdVendorOption(new PreCacheOption(true, true));
            vendorOption.setMediaType(AdVendorOption.MediaType.IMAGE);
            hsNativeAdPool = new HSNativeAdPool(poolName, poolName, HSNativeAdPool.AdStrategy.SESSION_POOL, vendorOption);
            hsNativeAdPool.addListener(new INativeAdPoolListener() {

                @Override
                public void onAdWillExpire(HSNativeAd hsNativeAd) {
                    hsNativeAd.release();
                    hsNativeAd = null;
                }

                @Override
                public void onAvailableAdCountChanged(int i) {
                    log("onAvailableAdCountChanged", "count", i + "");
                    NativeAdProfile.get(poolName).setAvailableCount(i);
                    if (!stopAvailableAdCountChangedNotification) {
                        if (i > 0) {
                            HSBundle hsBundle = new HSBundle();
                            hsBundle.putString(NATIVE_AD_POOL_NAME, poolName);
                            HSGlobalNotificationCenter.sendNotification(NOTIFICATION_NEW_AD, hsBundle);
                        }
                    }
                }
            });

        }

        public long getCachedNativeAdShowedTime() {
            return cachedNativeAdShowedTime;
        }

        public void setCachedNativeAdShowedTime(long cachedNativeAdShowedTime) {
            this.cachedNativeAdShowedTime = cachedNativeAdShowedTime;
        }

        private void log(String functionName, String key, String value) {
            HSLog.e(poolName + " - " + functionName + " : " + key + " - " + value);
        }

        HSNativeAd getNativeAd() {
            if (hsNativeAdPool != null && existNativeAd()) {
                clearCacheNativeAd();
                List<HSNativeAd> ads = hsNativeAdPool.getAds(1);
                cachedNativeAd = ads.get(0);
                cachedNativeAdShowedTime = 0;
                /** 更新广告信息 **/
                NativeAdProfile nativeAdProfile = NativeAdProfile.get(poolName);
                nativeAdProfile.incHasShowedCount();
                nativeAdProfile.setVendorName(cachedNativeAd.getVendor().name());
                nativeAdProfile.setCachedNativeAdTime(System.currentTimeMillis());
                return cachedNativeAd;
            }
            return null;
        }

        HSNativeAd getCachedNativeAd(){
            if (cachedNativeAd != null) {
                if (cachedNativeAd.isExpired()) {
                    clearCacheNativeAd();
                }
            }
            return cachedNativeAd;
        }


        boolean existNativeAd() {
            return hsNativeAdPool.getAvailableNativeAdCount() > 0;
        }

        void release() {
            stopAvailableAdCountChangedNotification = true;
            clearCacheNativeAd();
            NativeAdProfile.get(poolName).release();
        }

        private void clearCacheNativeAd() {
            if (cachedNativeAd != null) {
                cachedNativeAd.release();
                cachedNativeAd = null;
                cachedNativeAdShowedTime = 0;
            }
        }

        void startAvailableAdCountChangedNotifaction() {
            stopAvailableAdCountChangedNotification = false;
        }

        @Override
        public int hashCode() {
            return poolName.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            NativeAdProxy nativeAdProxy = (NativeAdProxy) obj;
            return this.poolName.equals(nativeAdProxy.poolName);
        }
    }
}
