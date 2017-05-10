package com.ihs.keyboardutils.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Arthur on 17/5/9.
 */

public class NotificationExecutionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
       KCNotificationManager.getInstance().scheduleNotify();
    }
}
