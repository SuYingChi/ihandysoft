package com.ihs.keyboardutils.nativeads;

import android.content.Context;
import android.view.View;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;

/**
 * Created by ihandysoft on 16/11/24.
 */

public class NativeAdProvider {

    public interface NativeAdViewListener {
        public void NativeAdViewPrepared(NativeAdView nativeAdView);
    }

    private INotificationObserver nativeAdObserver = new INotificationObserver() {

        @Override
        public void onReceive(String notificationName, HSBundle bundle) {
            if (notificationName.equals(NativeAdManager.NOTIFICATION_NEW_AD)) {
                if (poolName.equals(bundle.getString(NativeAdManager.NATIVE_AD_POOL_NAME))) {
                    log("onReceive", "New NativeAd Notification", "");
                    HSGlobalNotificationCenter.removeObserver(nativeAdObserver);
                    nativeAdViewListener.NativeAdViewPrepared(new NativeAdView(context, groupView, poolName, primaryHWRatio, fetchNativeAdInterval));
                }
            }
        }
    };

    public NativeAdProvider(NativeAdViewListener nativeAdViewListener) {
        this.nativeAdViewListener = nativeAdViewListener;

    }

    private NativeAdViewListener nativeAdViewListener;
    private String poolName;
    private Context context;
    private View groupView;
    private float primaryHWRatio;
    private int fetchNativeAdInterval;

    public void createNativeAdView(Context context, View groupView, String poolName) {
        createNativeAdView(context, groupView, poolName, 0);
    }

    public void createNativeAdView(Context context, View groupView, String poolName, int fetchNativeAdInterval) {
        createNativeAdView(context, groupView, poolName, 0, fetchNativeAdInterval);
    }

    public void createNativeAdView(Context context, View groupView, String poolName, float primaryHWRatio, int fetchNativeAdInterval) {
        this.poolName = poolName;
        this.context = context;
        this.groupView = groupView;
        this.primaryHWRatio = primaryHWRatio;
        this.fetchNativeAdInterval = fetchNativeAdInterval;
        HSGlobalNotificationCenter.addObserver(NativeAdManager.NOTIFICATION_NEW_AD, nativeAdObserver);
        if(NativeAdManager.getInstance().existNativeAd(poolName)) {
            HSGlobalNotificationCenter.sendNotification(NativeAdManager.NOTIFICATION_NEW_AD);
        }
    }

    private void log(String functionName, String key, String value) {
        HSLog.e(poolName + " - " + functionName + " : " + key + " - " + value);
    }
}
