package com.ihs.feature.resultpage;


import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.feature.common.AdAnalytics;
import com.ihs.feature.common.AdPlacements;
import com.ihs.keyboardutils.R;

import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.nativeads.AcbNativeAdLoader;
import net.appcloudbox.common.utils.AcbError;

import java.util.List;


public class ResultPageAdsManager {

    private static final String TAG = ResultPageAdsManager.class.getSimpleName();

    private AcbNativeAd mAd = null;

    private static ResultPageAdsManager sInstance;

    private ResultPageAdsManager() {
    }

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

    public void preloadAd() {
        AcbNativeAdLoader loader = new AcbNativeAdLoader(HSApplication.getContext(), HSApplication.getContext().getString(R.string.ad_placement_result_page));
        loader.load(1, new AcbNativeAdLoader.AcbNativeAdLoadListener() {
            @Override
            public void onAdReceived(AcbNativeAdLoader acbNativeAdLoader, List<AcbNativeAd> list) {
                mAd = list.isEmpty() ? null : list.get(0);
                if (mAd != null) {
                    AdAnalytics.logAppViewEvent(AdPlacements.SHARED_POOL_NATIVE_AD_FLURRY_KEY_VIEW_IN_APP_RESULT_PAGE, true);
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
