package com.ihs.keyboardutils.notification;

import android.support.annotation.NonNull;

import com.ihs.commons.utils.HSLog;

import java.util.Map;

/**
 * Created by Arthur on 17/4/29.
 */

public class NotificationBean implements Comparable<NotificationBean> {


    private int priority = 0;
    private int maxShowCount = Integer.MAX_VALUE;
    private int interval = 0;
    private String message = "";
    private String title = "";
    private String event;

    public NotificationBean(int priority, int maxShowCount, int interval, String message, String title) {
        this.priority = priority;
        this.maxShowCount = maxShowCount;
        this.interval = interval;
        this.message = message;
        this.title = title;

    }


    public NotificationBean(Map<String, Object> value) {
        priority = readIntConfig(value, "Priority", 0);
        maxShowCount = readIntConfig(value, "MaxShowCount", 0);
        interval = readIntConfig(value, "Interval", 0);
        message = readStringConfig(value, "Message");
        title = readStringConfig(value, "Title");
    }


    private int readIntConfig(Map<String, Object> configs, String key, int defaultValue) {
        int item = defaultValue;
        try {
            item = (int) configs.get(key);
        } catch (Exception e) {
            HSLog.e(key + " config reading error giving default value ==> " + defaultValue);
        }
        return item;
    }

    private String readStringConfig(Map<String, Object> configs, String key) {
        String item = "";
        try {
            item = (String) configs.get(key);
        } catch (Exception e) {
            HSLog.e(key + " config reading error giving default value ==> empty string");
        }
        return item;
    }

    public int getPriority() {
        return priority;
    }

    public int getMaxShowCount() {
        return maxShowCount;
    }

    public int getInterval() {
        return interval;
    }

    public String getMessage() {
        return message;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public int compareTo(@NonNull NotificationBean o) {
        return o.getPriority() - priority;
    }


    @Override
    public String toString() {
        return "NotificationBean{" +
                "priority=" + priority +
                ", maxShowCount=" + maxShowCount +
                ", interval=" + interval +
                ", message='" + message + '\'' +
                ", title='" + title + '\'' +
                '}';
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}
