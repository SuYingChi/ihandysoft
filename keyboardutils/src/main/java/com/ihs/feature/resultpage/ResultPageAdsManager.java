package com.ihs.feature.resultpage;


import android.text.TextUtils;

import com.artw.lockscreen.ScreenLockerManager;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.feature.common.AdAnalytics;
import com.ihs.feature.common.AdPlacements;
import com.ihs.keyboardutils.BuildConfig;
import com.ihs.keyboardutils.utils.ToastUtils;

import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.nativeads.AcbNativeAdLoader;
import net.appcloudbox.common.utils.AcbError;

import java.util.List;


public class ResultPageAdsManager {

    public interface OnAdListener {
        void onAdReceive(AcbNativeAd mAd);
    }

    private static final String TAG = ResultPageAdsManager.class.getSimpleName();

    private AcbNativeAd mAd = null;
    private static ResultPageAdsManager sInstance;

    private ResultPageAdsManager() {
    }

    private OnAdListener listener;


    public static ResultPageAdsManager getInstance() {
        if (sInstance == null) {
            synchronized (ResultPageAdsManager.class) {
                if (sInstance == null) {
                    sInstance = new ResultPageAdsManager();
                }
            }
        }
        return sInstance;
    }

    public void setOnAdListner(OnAdListener adListner) {
        this.listener = adListner;
    }

    public void preloadAd() {
        final String adPlacement = ScreenLockerManager.getResultPageAdPlacement();
        if (TextUtils.isEmpty(adPlacement)) {
            return;
        }

        AcbNativeAdLoader loader = new AcbNativeAdLoader(HSApplication.getContext(), adPlacement);
        loader.load(1, new AcbNativeAdLoader.AcbNativeAdLoadListener() {
            @Override
            public void onAdReceived(AcbNativeAdLoader acbNativeAdLoader, List<AcbNativeAd> list) {
                mAd = list.isEmpty() ? null : list.get(0);
                if (mAd != null) {
                    AdAnalytics.logAppViewEvent(AdPlacements.SHARED_POOL_NATIVE_AD_FLURRY_KEY_VIEW_IN_APP_RESULT_PAGE, true);
                    if (BuildConfig.DEBUG) {
                        ToastUtils.showToast("ad received");
                    }
                    if (listener != null) {
                        listener.onAdReceive(mAd);
                    }
                }
            }

            @Override
            public void onAdFinished(AcbNativeAdLoader acbNativeAdLoader, AcbError hsError) {
                if (hsError != null) {
                    AdAnalytics.logAppViewEvent(AdPlacements.SHARED_POOL_NATIVE_AD_FLURRY_KEY_VIEW_IN_APP_RESULT_PAGE, false);
                    HSLog.d(TAG, "result page load ad failed, error = " + hsError);
                }
            }
        });
        if (BuildConfig.DEBUG) {
            ToastUtils.showToast("LOAD AD");
        }
    }

    public AcbNativeAd getAd() {
        return mAd;
    }

    public void releaseAd() {
        if (mAd != null) {
            mAd.release();
            mAd = null;
        }
    }
}
