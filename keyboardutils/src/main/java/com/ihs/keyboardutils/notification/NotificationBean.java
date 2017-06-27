package com.ihs.keyboardutils.notification;

import android.support.annotation.NonNull;

import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Arthur on 17/4/29.
 */

public class NotificationBean implements Comparable<NotificationBean> {


    @Override
    public String toString() {
        return "NotificationBean{" +
                "priority=" + priority +
                ", maxShowCount=" + maxShowCount +
                ", interval=" + interval +
                ", message=" + message +
                ", title='" + title + '\'' +
                ", event='" + event + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", bgUrl='" + bgUrl + '\'' +
                ", name='" + name + '\'' +
                ", actionType='" + actionType + '\'' +
                ", pushTime=" + pushTime +
                '}';
    }

    private int priority = 0;
    private int maxShowCount = Integer.MAX_VALUE;
    private int interval = 0;
    private List<String> message; //随机出其中一个描述
    private String title = "";
    private String event = "";
    private String iconUrl = "";
    private String bgUrl = "";
    private String name = ""; // 用于匹配是否已下载的对象
    private String actionType = ""; //跳转对象类型
    private int pushTime; //每天有不同的时间


    public NotificationBean(Map<String, Object> value) {
        priority = readIntConfig(value, "Priority", 0);
        maxShowCount = readIntConfig(value, "MaxShowCount", 0);
        interval = readIntConfig(value, "Interval", 0);
        message = readStringListConfig(value, "Message");
        title = readStringConfig(value, "Title");
        pushTime = readIntConfig(value, "PushTime", 0);
        iconUrl = readStringConfig(value, "IconUrl");
        bgUrl = readStringConfig(value, "BgUrl");
        name = readStringConfig(value, "Name");
        actionType = readStringConfig(value, "ActionType");
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


    private List<String> readStringListConfig(Map<String, Object> configs, String key) {
        List<String> list = null;
        try {
            list = (List<String>) configs.get(key);
        } catch (Exception e) {
            HSLog.e(key + " config reading error giving default value ==> empty List");
        }
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    private List<Float> readFloatListConfig(Map<String, Object> configs, String key) {
        List<Float> list = null;
        try {
            list = (List<Float>) configs.get(key);
        } catch (Exception e) {
            HSLog.e(key + " config reading error giving default value ==> empty List");
        }
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
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
        return message.get(new Random().nextInt(message.size()));
    }

    public String getTitle() {
        return title;
    }

    @Override
    public int compareTo(@NonNull NotificationBean o) {
        return o.getPriority() - priority;
    }


    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}
