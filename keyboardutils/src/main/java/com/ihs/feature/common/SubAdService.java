package com.ihs.feature.common;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.ihs.aidl.ISubAdService;
import com.ihs.feature.resultpage.ResultPageAdsManager;


public class SubAdService extends Service {

    private final ISubAdService.Stub mBinder = new ISubAdService.Stub() {
        @Override
        public void requestBatteryAd() throws RemoteException {
            ConcurrentUtils.postOnMainThread(new Runnable() {
                @Override
                public void run() {
                    ResultPageAdsManager.getInstance().preloadAd();
                }
            });
        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
