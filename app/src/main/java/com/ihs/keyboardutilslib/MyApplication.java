package com.ihs.keyboardutilslib;

import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;
import android.text.TextUtils;

import com.artw.lockscreen.ScreenLockerManager;
import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.chargingscreen.HSChargingScreenManager;
import com.ihs.commons.diversesession.HSDiverseSession;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.feature.boost.plus.BoostPlusActivity;
import com.ihs.keyboardutils.notification.KCNotificationManager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.squareup.leakcanary.LeakCanary;

import java.util.ArrayList;


/**
 * Created by ihandysoft on 16/10/24.
 */

public class MyApplication extends HSApplication {
    public static final String REMOTE_PROCESS_CLEAN = "clean";

    @Override
    public void onCreate() {
        super.onCreate();


        String packageName = getPackageName();
        String processName = getProcessName();
        if (TextUtils.equals(processName, packageName)) {
            onMainProcessApplicationCreate();
        } else {
            String processSuffix = processName.replace(packageName + ":", "");
            onRemoteProcessApplicationCreate(processSuffix);
        }

    }

    private void onRemoteProcessApplicationCreate(String processSuffix) {
    }

    private void onMainProcessApplicationCreate() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        HSChargingScreenManager.init(true, "", "Master_A(NativeAds)Charging", new HSChargingScreenManager.IChargingScreenListener() {
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
        BoostPlusActivity.initBoost();
        ScreenLockerManager.init();
        initImageLoader();
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

    private void initImageLoader() {
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(this);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.tasksProcessingOrder(QueueProcessingType.LIFO);

        ImageLoader.getInstance().init(config.build());
    }

    @Override
    protected void attachBaseContext(Context base) {
        MultiDex.install(base);
        super.attachBaseContext(base);
    }
}
