package com.ihs.keyboardutils.appsuggestion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;

import static com.ihs.keyboardutils.appsuggestion.AppSuggestionActivity.showAppSuggestion;
import static com.ihs.keyboardutils.appsuggestion.AppSuggestionSetting.initEnableState;

/**
 * Created by Arthur on 17/12/8.
 */

public class AppSuggestionManager {
    private static AppSuggestionManager ourInstance;

    public static AppSuggestionManager getInstance() {
        if(ourInstance == null){
             ourInstance = new AppSuggestionManager();
        }
        return ourInstance;
    }

    private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                if(Intent.ACTION_USER_PRESENT.equals(intent.getAction())){
                    if(AppSuggestionSetting.getInstance().canShowAppSuggestion()){
                        showAppSuggestion();
                    }
                }
        }
    };


    public AppSuggestionManager() {
        initEnableState();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        HSApplication.getContext().registerReceiver(intentReceiver,intentFilter);

        INotificationObserver observer = new INotificationObserver() {
            @Override
            public void onReceive(String s, HSBundle hsBundle) {
                if (s.equals(HSNotificationConstant.HS_CONFIG_CHANGED)) {
                    AppSuggestionSetting.getInstance().updateAppSuggestionSetting();
                }
            }
        };
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_CONFIG_CHANGED, observer);
    }
}
