package com.artw.lockscreen.lockerappguide;

/**
 * Created by Arthur on 17/11/22.
 */

public class LockerGuideAlertBean {
    private String title;
    private String body;
    private String button;

    public LockerGuideAlertBean(String title, String body, String button) {
        this.title = title;
        this.body = body.replace("\\n","\n");
        this.button = button;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getButton() {
        return button;
    }
}
