package com.ihs.feature.boost.notification;

import android.app.PendingIntent;

import java.io.Serializable;

public class LocalNotification implements Serializable {

    public int notificationId = hashCode();
    // Notification remove itself when timeout.
    public long autoCleanTimeMills;
    public CharSequence title;
    public CharSequence description;
    public CharSequence buttonText;
    public int iconDrawableId;
    public int smallIconDrawableId;
    public PendingIntent pendingIntent;
    public PendingIntent deletePendingIntent;

}
