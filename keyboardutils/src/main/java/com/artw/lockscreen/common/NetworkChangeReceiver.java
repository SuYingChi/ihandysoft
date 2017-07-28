package com.artw.lockscreen.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.feature.common.Utils;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final String TAG = NetworkChangeReceiver.class.getSimpleName();

    public static final String NOTIFICATION_CONNECTIVITY_CHANGED = "connectivity_changed";
    public static final String BUNDLE_KEY_IS_NETWORK_AVAILABLE = "is_network_available";

    private boolean mNetworkAvailable;

    @Override
    public void onReceive(Context context, Intent intent) {
        HSLog.i(TAG, "NetworkChangeReceiver invoked");

        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            boolean isNetworkAvailable = Utils.isNetworkAvailable(-1);
            boolean changed = (isNetworkAvailable != mNetworkAvailable);
            mNetworkAvailable = isNetworkAvailable;
            if (changed) {
                HSBundle data = new HSBundle();
                data.putBoolean(BUNDLE_KEY_IS_NETWORK_AVAILABLE, isNetworkAvailable);
                HSGlobalNotificationCenter.sendNotification(NOTIFICATION_CONNECTIVITY_CHANGED, data);
            }
        }
    }
}
