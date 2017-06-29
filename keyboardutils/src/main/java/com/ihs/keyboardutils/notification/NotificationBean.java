package com.ihs.keyboardutils.notification;

import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Arthur on 17/4/29.
 */

public class NotificationBean {


    @Override
    public String toString() {
        return "NotificationBean{" +
                ", message=" + message +
                ", title='" + title + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", bgUrl='" + bgUrl + '\'' +
                ", name='" + name + '\'' +
                ", actionType='" + actionType + '\'' +
                ", pushTime=" + pushTime +
                '}';
    }

    private List<String> message; //随机出其中一个描述
    private String title = "";
    private String iconUrl = "";
    private String bgUrl = "";
    private String name = ""; // 用于匹配是否已下载的对象
    private String actionType = ""; //跳转对象类型


    public void setMessage(List<String> message) {
        this.message = message;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getBgUrl() {
        return bgUrl;
    }

    public void setBgUrl(String bgUrl) {
        this.bgUrl = bgUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public int getPushTime() {
        return pushTime;
    }

    public void setPushTime(int pushTime) {
        this.pushTime = pushTime;
    }

    private int pushTime; //每天有不同的时间


    public NotificationBean(Map<String, Object> value) {
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


    public String getMessage() {
        return message.get(new Random().nextInt(message.size()));
    }

    public String getTitle() {
        return title;
    }

    public String getSPKey(){
        return actionType + "," + name;
    }
}
