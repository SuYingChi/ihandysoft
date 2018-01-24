package com.kc.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.ihs.app.framework.HSApplication;
import com.ihs.keyboardutils.alerts.HeadSetActivity;

/**
 * Created by yingchi.su on 2018/1/17.
 */

public class HeadSetManager {


    private static HeadSetManager mHeadSetAdvManager;
    private  HeadReceiver headSetReceiver;
    private  String placeMent;
    private  boolean isEnable = false;
    private HeadSetManager(){
        headSetReceiver = new HeadReceiver();
    }

    public static synchronized HeadSetManager getInstance() {
        if (mHeadSetAdvManager == null) {
            mHeadSetAdvManager = new HeadSetManager();
        }
        return mHeadSetAdvManager;
    }
    public  void setHeadSetAdPlaceMent(String placeMent){

        this.placeMent=placeMent;
    }
    public  String  getHeadSetAdPlaceMent(){
        return placeMent;
    }
//get value from setting,remaining
    public boolean getEnable() {
        return isEnable;
    }
//set value of setting,ramaining
    public void setEnable(boolean enable) {
        if(!isEnable&&enable){
            registerHeadSetReceiver();
        }else if(isEnable&&!enable){
            unRegisterHeadSetReceiver();
        }
        isEnable = enable;
    }

    private class HeadReceiver extends BroadcastReceiver {

        public String TAG = "HeadSetReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                if (intent.getIntExtra("state", 0) == 1) {
                    Log.d(TAG, " onReceive  headset==========" + Intent.ACTION_HEADSET_PLUG + "      state================" + intent.getIntExtra("state", 0));
                    if (getEnable()) {
                        Intent mIntent = new Intent(context, HeadSetActivity.class);
                        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(mIntent);
                    }
                }
            }
        }

    }
    public  void registerHeadSetReceiver(){
        HSApplication.getContext().registerReceiver(headSetReceiver,new IntentFilter(Intent.ACTION_HEADSET_PLUG));
    }
    public  void unRegisterHeadSetReceiver(){
        HSApplication.getContext().unregisterReceiver(headSetReceiver);
    }

    public  void init(String string){
        mHeadSetAdvManager.setHeadSetAdPlaceMent(string);
        if(isEnable) {
            mHeadSetAdvManager.registerHeadSetReceiver();
        }

    }
}
