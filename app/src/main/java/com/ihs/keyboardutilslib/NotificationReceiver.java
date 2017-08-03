package com.ihs.keyboardutilslib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ihs.keyboardutils.utils.ToastUtils;

/**
 * Created by Arthur on 17/6/30.
 */

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ToastUtils.showToast(intent.getStringExtra("actionType") + "," + intent.getStringExtra("name"));
    }
}
