package com.ihs.feature.resultpage;


import android.text.TextUtils;

import com.artw.lockscreen.ScreenLockerManager;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.BuildConfig;
import com.ihs.keyboardutils.utils.ToastUtils;

import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.common.utils.AcbError;
import net.appcloudbox.ads.nativead.AcbNativeAdLoader;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;

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

    public void setOnAdListener(OnAdListener adListener) {
        this.listener = adListener;
    }

    public void preloadAd() {
        final String adPlacement = ScreenLockerManager.getResultPageAdPlacement();
        if (TextUtils.isEmpty(adPlacement)) {
            return;
        }

        AcbNativeAdLoader loader = AcbNativeAdManager.createLoaderWithPlacement(adPlacement);
        loader.load(1, new AcbNativeAdLoader.AcbNativeAdLoadListener() {
            @Override
            public void onAdReceived(AcbNativeAdLoader acbNativeAdLoader, List<AcbNativeAd> list) {
                mAd = list.isEmpty() ? null : list.get(0);
                if (mAd != null) {
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
