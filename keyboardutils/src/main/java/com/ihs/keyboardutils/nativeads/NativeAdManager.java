package com.ihs.keyboardutils.nativeads;

import android.support.v4.util.ArrayMap;

import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.nativeads.base.api.AdVendorOption;
import com.ihs.nativeads.base.api.HSNativeAd;
import com.ihs.nativeads.base.api.PreCacheOption;
import com.ihs.nativeads.pool.api.HSNativeAdPool;
import com.ihs.nativeads.pool.api.INativeAdPoolListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by dongdong.han on 16/10/20.
 */

public class NativeAdManager {

    public static String NOTIFICATION_NEW_AD = "NEW_NATIVE_AD_NOTIFICATION";
    public static String NATIVE_AD_POOL_NAME = "NATIVE_AD_POOL_NAME";


    private static NativeAdManager nativeAdManager;

    private ArrayMap<String, NativeAdProxy> nativeAdProxies;

    private synchronized ArrayList<NativeAdProxy> getAllPoolState() {
        if (nativeAdProxies != null) {
            ArrayList<NativeAdProxy> result = new ArrayList<>();
            Iterator<NativeAdProxy> it = nativeAdProxies.values().iterator();
            while (it.hasNext()) {
                result.add(it.next());
            }
            return result;
        }
        return null;
    }

    private NativeAdManager() {
        nativeAdProxies = new ArrayMap<>();
        List<?> disabledPools = HSConfig.getList("Application", "NativeAds", "DisabledPools");
        for (Map.Entry entry : HSConfig.getMap("nativeAdsPool").entrySet()) {
            if ((entry.getValue() instanceof Map) && !disabledPools.contains(entry.getKey().toString())) {
                nativeAdProxies.put(entry.getKey().toString(), new NativeAdProxy(entry.getKey().toString()));
            }
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

    /**
     * first: notify observer {@link INativeAdPoolListener#onAvailableAdCountChanged(int)}
     * second: provide the native ad {@link #getNativeAd()}
     * third: release cachedNativeAd {@link #release()}
     */
    public static final class NativeAdProxy {
        private String poolName;
        private HSNativeAdPool hsNativeAdPool;
        private HSNativeAd cachedNativeAd;
        private long cachedNativeAdTime;
        private boolean stopNotifyAvailableAdCountChanged = false;

        private int hasShowedCount;

        NativeAdProxy(String poolName) {
            this.poolName = poolName;
            createNativeAdPool();
        }

        private void createNativeAdPool() {
            AdVendorOption vendorOption = new AdVendorOption(new PreCacheOption(true, true));
            vendorOption.setMediaType(AdVendorOption.MediaType.IMAGE);
            hsNativeAdPool = new HSNativeAdPool(poolName, poolName, HSNativeAdPool.AdStrategy.APP_POOL, vendorOption);
            hsNativeAdPool.addListener(new INativeAdPoolListener() {

                @Override
                public void onAdWillExpire(HSNativeAd hsNativeAd) {
                    hsNativeAd.release();
                    hsNativeAd = null;
                }

                @Override
                public void onAvailableAdCountChanged(int i) {
                    if (!stopNotifyAvailableAdCountChanged) {
                        log("onAvailableAdCountChanged", "count", i + "");
                        if (i > 0) {
                            HSBundle hsBundle = new HSBundle();
                            hsBundle.putString(NATIVE_AD_POOL_NAME, poolName);
                            HSGlobalNotificationCenter.sendNotification(NOTIFICATION_NEW_AD, hsBundle);
                        }
                    }
                }
            });

        }

        private void log(String functionName, String key, String value) {
            HSLog.e(poolName + " - " + functionName + " : " + key + " - " + value);
        }

        HSNativeAd getNativeAd() {
            if (hsNativeAdPool != null) {
                List<HSNativeAd> ads = hsNativeAdPool.getAds(1);
                if (ads.size() > 0) {
                    clearCacheNativeAd();
                    HSNativeAd nativeAd = ads.get(0);

                    hasShowedCount++;
                    cachedNativeAd = nativeAd;
                    cachedNativeAdTime = System.currentTimeMillis();
                    log("getNativeAd", "New NativeAd", nativeAd.hashCode() + "");
                    return cachedNativeAd;
                }
            }
            return null;
        }

        HSNativeAd getCachedNativeAd() {
            if(cachedNativeAd != null) {
                log("getCachedNativeAd", "Cached NativeAd", cachedNativeAd.hashCode() + "");
                if (cachedNativeAd.isExpired()) {
                    clearCacheNativeAd();
                }
            }
            return cachedNativeAd;
        }

        long getCachedNativeAdTime() {
            return cachedNativeAdTime;
        }

        boolean existNativeAd() {
            return hsNativeAdPool.getAvailableNativeAdCount() > 0;
        }

        void release() {
            stopNotifyAvailableAdCountChanged = true;
            clearCacheNativeAd();
        }


        void clearCacheNativeAd(){
            if (cachedNativeAd != null) {
                cachedNativeAd.release();
                cachedNativeAdTime = 0;
                cachedNativeAd = null;
            }
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

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder(poolName);
            stringBuilder.append("(CurrentCount:");
            stringBuilder.append(hsNativeAdPool.getAvailableNativeAdCount());
            stringBuilder.append(";HasShowedCount:" + hasShowedCount);
            if (cachedNativeAd != null) {
                stringBuilder.append(";VendorName:" + cachedNativeAd.getVendor().name());
            }
            stringBuilder.append(")");
            return stringBuilder.toString();
        }
    }
}
