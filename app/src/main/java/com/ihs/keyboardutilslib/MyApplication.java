package com.ihs.keyboardutilslib;

import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.commons.diversesession.HSDiverseSession;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.keyboardutilslib.nativeads.NativeAdManager;

/**
 * Created by ihandysoft on 16/10/24.
 */

public class MyApplication extends HSApplication {

    private INotificationObserver sessionEventObserver = new INotificationObserver() {

        @Override
        public void onReceive(String notificationName, HSBundle bundle) {
            if (HSNotificationConstant.HS_SESSION_START.equals(notificationName)) {
//                int currentapiVersion = android.os.Build.VERSION.SDK_INT;
//                if (currentapiVersion <= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                    HSLog.d("should delay rate alert for sdk version between 4.0 and 4.2");
//                }
                HSAlertMgr.delayRateAlert();
                onSessionStart();
            }

            if (HSNotificationConstant.HS_SESSION_END.equals(notificationName)) {
                HSDiverseSession.end();
            }
        }
    };


    private void onSessionStart() {
        NativeAdManager.init();
    }
}
