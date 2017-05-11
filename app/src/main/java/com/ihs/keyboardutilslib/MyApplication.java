package com.ihs.keyboardutilslib;

import android.content.Intent;

import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.chargingscreen.HSChargingScreenManager;
import com.ihs.commons.diversesession.HSDiverseSession;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.notification.KCNotificationManager;
import com.squareup.leakcanary.LeakCanary;

import java.util.ArrayList;


/**
 * Created by ihandysoft on 16/10/24.
 */

public class MyApplication extends HSApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        HSChargingScreenManager.init(true, "", "Master_A((NativeAds)BoostDone", new HSChargingScreenManager.IChargingScreenListener() {
            @Override
            public void onClosedByChargingPage() {
            }
        });

        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_START, sessionEventObserver);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_END, sessionEventObserver);


        KCNotificationManager.getInstance().setNotificationResponserType(KCNotificationManager.TYPE_ACTIVITY);
        ArrayList<String> eventList = new ArrayList<>();
        eventList.add("ScreenLocker");
        eventList.add("Charging");
        eventList.add("AddNewPhotoToPrivate");
        for (String event : eventList) {
            int reqCode = 0;
            switch (event) {
                case "ScreenLocker":
                    reqCode = 1;
                    break;
                case "Charging":
                    reqCode = 2;
                    break;
                case "AddNewPhotoToPrivate":
                    reqCode = 3;
                    break;
            }

            Intent resultIntent = new Intent(getContext(), MainActivity.class);
            resultIntent.putExtra("reqCode", reqCode);
            KCNotificationManager.getInstance().addNotificationEvent(event, resultIntent);
        }
    }

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
        HSLog.e("onSessionStart");
    }
}
