package com.ihs.keyboardutils.nativeads;

import android.content.Context;
import android.support.v4.util.ArrayMap;

import com.acb.adadapter.AcbNativeAd;
import com.acb.nativeads.AcbNativeAdLoader;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;

import java.util.List;

public class NativeAdManager {
    private static NativeAdManager nativeAdManager;
    private ArrayMap<String, NativeAdProxy> nativeAdProxies;

    public interface AdLoadListener {
        public void onAdLoaded(AcbNativeAd ad, long remainingDisplayDuration);
    }

    private NativeAdManager() {
        nativeAdProxies = new ArrayMap<>();
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

    void markAdAsFinished(String placementName) {
        getNativeAdProxy(placementName).markAsFinished();
    }

    public static void preloadAd(String placementName) {
        getInstance().loadNativeAd(HSApplication.getContext(), placementName, null);
    }

    void loadNativeAd(Context context, String placementName, AdLoadListener listener) {
        getNativeAdProxy(placementName).loadNativeAd(context, listener);
    }

    AcbNativeAd loadLocalNativeAd(Context context, String placementName) {
        return getNativeAdProxy(placementName).loadLocalNativeAd(context);
    }

    NativeAdProxy getNativeAdProxy(String placementName) {
        if (nativeAdProxies.containsKey(placementName)) {
            return nativeAdProxies.get(placementName);
        } else {
            NativeAdProxy proxy = new NativeAdProxy(placementName);
            nativeAdProxies.put(placementName, proxy);
            return proxy;
        }
    }

    public static final class NativeAdProxy {
        /**
         * 广告位名字
         **/
        private String placementName;
        /**
         * 广告位对应的Loader
         **/
        private AcbNativeAdLoader loader;
        /**
         * 广告位对应的广告
         **/
        private AcbNativeAd cachedNativeAd;
        /**
         * 当前广告实际展示的时间
         **/
        private long cachedNativeAdShowedTime;
        /**
         * 显示足够长时间或者已经被点击
         **/
        private boolean displayFinished;

        NativeAdProxy(String placementName) {
            this.placementName = placementName;
        }

        long getCachedNativeAdShowedTime() {
            return cachedNativeAdShowedTime;
        }

        void setCachedNativeAdShowedTime(long cachedNativeAdShowedTime) {
            this.cachedNativeAdShowedTime = cachedNativeAdShowedTime;
            if (cachedNativeAdShowedTime > NativeAdConfig.getNativeAdFrequency()) {
                markAsFinished();
            }
        }

        private void log(String functionName, String key, String value) {
            HSLog.e(placementName + " - " + functionName + " : " + key + " - " + value);
        }

        void markAsFinished() {
            displayFinished = true;
        }

        AcbNativeAd loadLocalNativeAd(Context context) {
            if (cachedNativeAd != null && !cachedNativeAd.isExpired() && !displayFinished) {
                return cachedNativeAd;
            }

            if (cachedNativeAd != null) {
                clearCacheNativeAd();
            }

            List<AcbNativeAd> ads = AcbNativeAdLoader.fetch(context, placementName, 1);

            if (ads.size() > 0) {
                cachedNativeAd = ads.get(0);
                /** 更新广告信息 **/
                NativeAdProfile nativeAdProfile = NativeAdProfile.get(placementName);
                nativeAdProfile.incHasShowedCount();
                nativeAdProfile.setVendorName(cachedNativeAd.getVendor().name());
                nativeAdProfile.setCachedNativeAdTime(System.currentTimeMillis());

                return cachedNativeAd;
            } else {
                return null;
            }
        }

        void loadNativeAd(Context context, AdLoadListener listener) {
            final AdLoadListener adLoadListener = listener;
            if (cachedNativeAd != null && !cachedNativeAd.isExpired() && !displayFinished) {
                if (adLoadListener != null) {
                    adLoadListener.onAdLoaded(cachedNativeAd, NativeAdConfig.getNativeAdFrequency() - getCachedNativeAdShowedTime());
                }
                return;
            }

            if (cachedNativeAd != null) {
                clearCacheNativeAd();
            }

            loader = new AcbNativeAdLoader(context.getApplicationContext(), placementName);

            loader.load(1, new AcbNativeAdLoader.AcbNativeAdLoadListener() {

                @Override
                public void onAdReceived(AcbNativeAdLoader acbNativeAdLoader, List<AcbNativeAd> list) {
                    clearCacheNativeAd();
                    cachedNativeAd = list.get(0);
                    /** 更新广告信息 **/
                    NativeAdProfile nativeAdProfile = NativeAdProfile.get(placementName);
                    nativeAdProfile.incHasShowedCount();
                    nativeAdProfile.setVendorName(cachedNativeAd.getVendor().name());
                    nativeAdProfile.setCachedNativeAdTime(System.currentTimeMillis());

                    loader = null;

                    if (adLoadListener != null) {
                        adLoadListener.onAdLoaded(cachedNativeAd, NativeAdConfig.getNativeAdFrequency());
                    }
                }

                @Override
                public void onAdFinished(AcbNativeAdLoader acbNativeAdLoader, HSError hsError) {

                }
            });
        }

        void release() {
            if (loader != null) {
                loader.cancel();
            }
            clearCacheNativeAd();
            NativeAdProfile.get(placementName).release();
        }

        private void clearCacheNativeAd() {
            if (cachedNativeAd != null) {
                cachedNativeAd.release();
                cachedNativeAd = null;
                cachedNativeAdShowedTime = 0;
                displayFinished = false;
            }
        }

        @Override
        public int hashCode() {
            return placementName.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof NativeAdProxy) {
                NativeAdProxy nativeAdProxy = (NativeAdProxy) obj;
                return this.placementName.equals(nativeAdProxy.placementName);
            } else {
                return false;
            }

        }
    }
}
