package com.ihs.keyboardutils.interstitial;

import android.content.Context;
import android.support.annotation.NonNull;

import com.acb.adadapter.AcbInterstitialAd;
import com.acb.interstitialads.AcbInterstitialAdLoader;
import com.acb.interstitialads.AcbInterstitialAdLoader.AcbInterstitialAdLoadListener;
import com.acb.interstitialads.AcbInterstitialAdManager;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSError;

import java.util.List;
import java.util.Map;

/**
 * Created by chenyuanming on 17/02/2017.
 */

public class KBInterstitialAdManager {

    public static void init(Context context) {
        AcbInterstitialAdManager.getInstance(context);
    }

    public static void stoploadAllAds(Context context) {
        Map adPlacementMap = HSConfig.getMap(new String[]{"interstitialAds"});

    }

    public static void startManualPreLoad(@NonNull Context context, String... adPlacementNames) {
        if (adPlacementNames != null) {
            for (String adPlacementName : adPlacementNames) {
                AcbInterstitialAdManager.startManualPreLoad(adPlacementName, context);
            }
        }
    }

    public static void stopManualPreLoad(@NonNull Context context, String... adPlacementNames) {
        if (adPlacementNames != null) {
            for (String adPlacementName : adPlacementNames) {
                AcbInterstitialAdManager.stopManualPreLoad(adPlacementName, context);
            }
        }
    }

    public static void preloadForPlacement(Context context, String placementName, int loadCount) {
        AcbInterstitialAdLoader loader = new AcbInterstitialAdLoader(context, placementName);
        // 注意listener需要传入null
        loader.load(loadCount, null);
    }


    public static void loadForPlacement(Context context, String placementName, int loadCount, AcbInterstitialAdLoadListener listener) {
        //appOpenPlacementName: 广告位名称，就是plist里的广告名
        AcbInterstitialAdLoader loader = new AcbInterstitialAdLoader(context, placementName);

        //loadCount：请求广告的个数，如果广告池里没足够数目，会从第三方获取。优先从本地广告池获取
        loader.load(loadCount, new AcbInterstitialAdLoader.AcbInterstitialAdLoadListener() {
            @Override
            //每次获取广告都会回调，广告分批回调
            public void onAdReceived(AcbInterstitialAdLoader loader, List<AcbInterstitialAd> ads) {
                //已经取到广告，APP要负责ads的生命周期。
            }

            @Override
            //广告获取结束的回调
            public void onAdFinished(AcbInterstitialAdLoader loader, HSError error) {
            }
        });
    }

    public static void loadForPlacement(Context context, String placementName, AcbInterstitialAdLoadListener listener) {
        //appOpenPlacementName: 广告位名称，就是plist里的广告名
        AcbInterstitialAdLoader loader = new AcbInterstitialAdLoader(context, placementName);

        //loadCount：请求广告的个数，如果广告池里没足够数目，会从第三方获取。优先从本地广告池获取
        loader.load(1, new AcbInterstitialAdLoader.AcbInterstitialAdLoadListener() {
            @Override
            //每次获取广告都会回调，广告分批回调
            public void onAdReceived(AcbInterstitialAdLoader loader, List<AcbInterstitialAd> ads) {
                //已经取到广告，APP要负责ads的生命周期。
            }

            @Override
            //广告获取结束的回调
            public void onAdFinished(AcbInterstitialAdLoader loader, HSError error) {
            }
        });
    }

    public static int getAvailableAdCount(Context context, String placementName) {
        return fetch(context, placementName, 100).size();
    }

    public static List<AcbInterstitialAd> fetch(Context context, String placementName, int count) {
        return AcbInterstitialAdLoader.fetch(context, placementName, count);
    }

    public static boolean showInterstitialAd(String applyPlacementName, final AcbInterstitialAd.IAcbInterstitialAdListener listener) {
        List<AcbInterstitialAd> interstitialAds = KBInterstitialAdManager.fetch(HSApplication.getContext(), applyPlacementName, 1);
        if (interstitialAds.size() > 0) {
            final AcbInterstitialAd interstitialAd = interstitialAds.get(0);
            interstitialAd.setInterstitialAdListener(new AcbInterstitialAd.IAcbInterstitialAdListener() {
                @Override
                public void onAdDisplayed() {
                    if(listener!=null){
                        listener.onAdDisplayed();
                    }
                }

                @Override
                public void onAdClicked() {
                    if(listener!=null){
                        listener.onAdClicked();
                    }
                }

                @Override
                public void onAdClosed() {
                    if(listener!=null){
                        listener.onAdClosed();
                    }
                    //在使用完广告后一定要调用release接口避免内存泄漏；
                    interstitialAd.release();
                }
            });
            interstitialAd.show();
            return true;
        } else {
            return false;
        }
    }
}
