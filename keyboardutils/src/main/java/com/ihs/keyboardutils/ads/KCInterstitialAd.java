package com.ihs.keyboardutils.ads;

import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.acb.adadapter.AcbInterstitialAd;
import com.acb.interstitialads.AcbInterstitialAdLoader;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.keyboardutils.R;

import java.util.List;

public class KCInterstitialAd {

    public interface OnAdCloseListener {
        void onAdClose();
    }

    public static void load(String placement) {
        logAnalyticsEvent(placement, "Load");
        new AcbInterstitialAdLoader(HSApplication.getContext(), placement).load(1, null);
    }

    public static boolean show(final String placement) {
        return show(placement, null);
    }

    public static boolean show(final String placement, final OnAdCloseListener onAdCloseListener) {
        List<AcbInterstitialAd> interstitialAds = AcbInterstitialAdLoader.fetch(HSApplication.getContext(), placement, 1);
        if (interstitialAds.size() <= 0) {
            logAnalyticsEvent(placement, "FetchNoAd");
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
                    Toast.makeText(HSApplication.getContext(), placement + ":" + interstitialAd.getVendor().name(), Toast.LENGTH_SHORT);
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

        if (interstitialAd != null) {
            interstitialAd.show();
            return true;
        } else {
            return false;
        }
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
        String screenName = HSApplication.getContext().getResources().getString(R.string.english_ime_name);
        String eventName = "InterstitialAd_" + placement + "_" + actionSuffix;

        HSAnalytics.logEvent(eventName);
        HSAnalytics.logGoogleAnalyticsEvent(screenName, "APP", eventName, null, null, null, null);
    }
}
