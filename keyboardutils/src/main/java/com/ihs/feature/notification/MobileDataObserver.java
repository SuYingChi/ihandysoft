package com.ihs.feature.notification;

import android.content.Intent;
import android.database.ContentObserver;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

public class MobileDataObserver extends ContentObserver {

    public MobileDataObserver() {
        super(null);
    }

    @Override
    public void onChange(boolean selfChange) {
        HSLog.d("MobileDataObserver", "Mobile data changed");
        Intent intent = new Intent(NotificationManager.ACTION_MOBILE_DATA_CHANGE);
        NotificationManager.getInstance().handleEvent(HSApplication.getContext(), intent);
//        HSGlobalNotificationCenter.sendNotification(MobileDataSettingsItemView.NOTIFICATION_MOBILE_DATA_CHANGE);
    }
}
