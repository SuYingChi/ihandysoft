package com.ihs.keyboardutils.utils;

public class AlertShowingUtils {
    private static boolean isShowingAlert;

    public static void startShowingAlert() {
        isShowingAlert = true;
    }

    public static void stopShowingAlert() {
        isShowingAlert = false;
    }

    public static boolean isShowingAlert() {
        return isShowingAlert;
    }
}
