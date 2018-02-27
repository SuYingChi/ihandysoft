package com.ihs.keyboardutils.notification;

import android.graphics.Color;
import android.text.TextUtils;

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
                '}';
    }

    private List<String> message; //随机出其中一个描述
    private String title = "";
    private String iconUrl = "";
    private String bgUrl = "";
    private String name = ""; // 用于匹配是否已下载的对象
    private String actionType = ""; //跳转对象类型
    private String messageColor = "#ffffff";
    private String titleColor = "#ffffff";
    private String buttonTextColor = "#000000";
    private String buttonText = "CHECK";
    private String bgColor = "#ffffff";
    private int maxRepeatCount = 0;
    private int style = 0;


    public void setMessage(List<String> message) {
        this.message = message;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getBgUrl() {
        return bgUrl;
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

    public NotificationBean(Map<String, Object> value) {
        message = readStringListConfig(value, "Message");
        title = readStringConfig(value, "Title", "");
        iconUrl = readStringConfig(value, "IconUrl", "");
        bgUrl = readStringConfig(value, "BgUrl", bgUrl);
        name = readStringConfig(value, "Name", "");
        actionType = readStringConfig(value, "ActionType", "");
        buttonText = readStringConfig(value, "ButtonText", buttonText);
        buttonTextColor = readStringConfig(value, "ButtonTextColor", buttonTextColor);
        titleColor = readStringConfig(value, "TitleColor", titleColor);
        messageColor = readStringConfig(value, "MessageColor", messageColor);
        bgColor = readStringConfig(value, "BgColor", bgColor);
        style = readIntConfig(value, "Style", 0);
        maxRepeatCount = readIntConfig(value, "MaxRepeatCount", 1);
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

    private boolean readBooleanConfig(Map<String, Object> configs, String key, boolean defaultValue) {
        boolean item = defaultValue;
        try {
            item = (boolean) configs.get(key);
        } catch (Exception e) {
            HSLog.e(key + " config reading error giving default value ==> " + defaultValue);
        }
        return item;
    }


    private String readStringConfig(Map<String, Object> configs, String key, String defaultString) {
        String item = "";
        try {
            item = (String) configs.get(key);
        } catch (Exception e) {
            HSLog.e(key + " config reading error giving default value ==> empty string");
        }
        if (TextUtils.isEmpty(item)) {
            item = defaultString;
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
        if (message.size() > 0) {
            return message.get(new Random().nextInt(message.size()));
        } else return "";
    }

    public String getTitle() {
        return title;
    }

    public String getSPKey() {
        return actionType + "|" + name;
    }

    public int getMessageColor() {
        return Color.parseColor(messageColor);
    }

    public int getTitleColor() {
        return Color.parseColor(titleColor);
    }

    public int getButtonTextColor() {
        return Color.parseColor(buttonTextColor);
    }

    public String getButtonText() {
        return buttonText;
    }

    public int getBgColor() {
        return Color.parseColor(bgColor);
    }

    public int getStyle() {
        return style;
    }

    public int getMaxRepeatCount() {
        return maxRepeatCount;
    }
}
