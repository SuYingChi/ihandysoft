package com.ihs.keyboardutils.ads;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import com.acb.adadapter.AcbInterstitialAd;
import com.acb.interstitialads.AcbInterstitialAdLoader;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.ihs.keyboardutils.utils.KCAnalyticUtil;

import java.util.List;

public class KCInterstitialAd {

    public interface OnAdCloseListener {
        void onAdClose();
    }

    public interface OnAdShowListener {
        void onAdShow(boolean success);
    }

    public static void load(String placement) {
        if (RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            return;
        }
        logAnalyticsEvent(placement, "Load");
        new AcbInterstitialAdLoader(HSApplication.getContext(), placement).load(1, null);
    }

    public static AcbInterstitialAdLoader loadAndShow(final String placement, final OnAdShowListener onAdShowListener, final OnAdCloseListener onAdCloseListener) {
        return loadAndShow(placement, onAdShowListener, onAdCloseListener, false);
    }

    /**
     * 加载插页广告，并在加载完成后显示，使用这种方式时需要在合适的时机去 Cancel 返回的 Loader
     * @param placement 广告位
     * @param onAdCloseListener 广告关闭时的回调
     * @param showQuietly 是否使用安静模式
     * @return 广告的Loader，需要在合适的时机去 Cancel
     */
    public static AcbInterstitialAdLoader loadAndShow(final String placement, final OnAdShowListener onAdShowListener, final OnAdCloseListener onAdCloseListener, final boolean showQuietly) {
        if (RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            return null;
        }

        logAnalyticsEvent(placement, "Load");
        AcbInterstitialAdLoader loader = new AcbInterstitialAdLoader(HSApplication.getContext(), placement);
        loader.load(1, new AcbInterstitialAdLoader.AcbInterstitialAdLoadListener() {
            OnAdShowListener listener = onAdShowListener;

            @Override
            public void onAdReceived(AcbInterstitialAdLoader acbInterstitialAdLoader, List<AcbInterstitialAd> list) {
                boolean success = show(placement, list, onAdCloseListener, showQuietly, null, null);
                if (listener != null) {
                    listener.onAdShow(success);
                    listener = null;
                }
            }

            @Override
            public void onAdFinished(AcbInterstitialAdLoader acbInterstitialAdLoader, HSError hsError) {
                if (hsError != null) {
                    HSLog.e("Load interstitial ad failed: " + hsError);
                    try {
                        ConnectivityManager cm =
                                (ConnectivityManager) HSApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo netInfo = cm.getActiveNetworkInfo();
                        if (netInfo == null || !netInfo.isAvailable() || !netInfo.isConnected()) {
                            logAnalyticsEvent(placement, "NoNetwork");
                        }
                    } catch (Exception e) {
                        // 防止因为没有权限而Crash
                    }
                    if (listener != null) {
                        listener.onAdShow(false);
                        listener = null;
                    }
                }
            }
        });

        return loader;
    }

    public static boolean show(final String placement) {
        return show(placement, null, false);
    }

    public static boolean show(final String placement, final String title, final String subTitle) {
        return show(placement, null, false, title, subTitle);
    }

    public static boolean show(final String placement, final OnAdCloseListener onAdCloseListener) {
        return show(placement, onAdCloseListener, false);
    }

    public static boolean show(final String placement, final OnAdCloseListener onAdCloseListener, boolean showQuietly) {
        if (RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            return false;
        }

        List<AcbInterstitialAd> interstitialAds = AcbInterstitialAdLoader.fetch(HSApplication.getContext(), placement, 1);
        if (interstitialAds.size() <= 0) {
            logAnalyticsEvent(placement, "FetchNoAd");
            return false;
        }

        return show(placement, interstitialAds, onAdCloseListener, showQuietly, null, null);
    }

    public static boolean show(final String placement, final OnAdCloseListener onAdCloseListener, boolean showQuietly, final String title, final String subTitle) {
        if (RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            return false;
        }

        List<AcbInterstitialAd> interstitialAds = AcbInterstitialAdLoader.fetch(HSApplication.getContext(), placement, 1);
        if (interstitialAds.size() <= 0) {
            logAnalyticsEvent(placement, "FetchNoAd");
            return false;
        }

        return show(placement, interstitialAds, onAdCloseListener, showQuietly, title, subTitle);
    }

    private static boolean show(final String placement, List<AcbInterstitialAd> interstitialAds, final OnAdCloseListener onAdCloseListener, boolean showQuietly, final String title, final String subTitle) {
        if (RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            return false;
        }

        if (interstitialAds == null || interstitialAds.size() == 0) {
            return false;
        }
        final AcbInterstitialAd interstitialAd = interstitialAds.get(0);
        interstitialAd.setInterstitialAdListener(new AcbInterstitialAd.IAcbInterstitialAdListener() {
            @Override
            public void onAdDisplayed() {
                logAnalyticsEvent(placement, "Show");
            }

            @Override
            public void onAdClicked() {
                if (HSApplication.isDebugging) {
                    Toast.makeText(HSApplication.getContext(), placement + ":" + interstitialAd.getVendorConfig().name(), Toast.LENGTH_SHORT).show();
                }

                logAnalyticsEvent(placement, "Click");
                releaseInterstitialAd(interstitialAd);
            }

            @Override
            public void onAdClosed() {
                logAnalyticsEvent(placement, "Close");
                releaseInterstitialAd(interstitialAd);
                if (onAdCloseListener != null) {
                    onAdCloseListener.onAdClose();
                }
            }
        });

        if (!TextUtils.isEmpty(title)) {
            interstitialAd.setCustomTitle(title);
        }

        if (!TextUtils.isEmpty(subTitle)) {
            interstitialAd.setCustomSubtitle(subTitle);
        }

        if (showQuietly) {
            interstitialAd.showQuietly(HSApplication.getContext());
        } else {
            interstitialAd.show();
        }

        return true;
    }

    private static void releaseInterstitialAd(final AcbInterstitialAd interstitialAd) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (interstitialAd != null) {
                    interstitialAd.release();
                }
            }
        });
    }

    private static void logAnalyticsEvent(String placement, String actionSuffix) {
        String eventName = "InterstitialAd_" + placement + "_" + actionSuffix;
        KCAnalyticUtil.logEvent(eventName);
    }
}
