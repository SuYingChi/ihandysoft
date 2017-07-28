package com.ihs.feature.junkclean.util;

public class JunkCleanConstant {

    public static final boolean JUNK_CLEAN_BADGE_DISABLED = true;

    public static final String CATEGORY_SYSTEM_JUNK = "CATEGORY_SYSTEM_JUNK";
    public static final String CATEGORY_APP_JUNK = "CATEGORY_APP_JUNK";
    public static final String CATEGORY_AD_JUNK = "CATEGORY_AD_JUNK";
    public static final String CATEGORY_MEMORY_JUNK = "CATEGORY_MEMORY_JUNK";
    public static final String CATEGORY_SPECIAL_TOTAL_JUNK = "CATEGORY_SPECIAL_TOTAL_JUNK";

    public static final String CATEGORY_UNINSTALL_AD_RANDOM_JUNK = "CATEGORY_UNINSTALL_AD_RANDOM_JUNK";
    public static final String CATEGORY_UNINSTALL_APP_RANDOM_JUNK = "CATEGORY_UNINSTALL_APP_RANDOM_JUNK";

    public static final String CATEGORY_UNINSTALL_APP_JUNK = "CATEGORY_UNINSTALL_APP_JUNK";
    public static final String CATEGORY_APK_JUNK = "CATEGORY_APK_JUNK";
    public static final String CATEGORY_INSTALL_APP_JUNK = "CATEGORY_INSTALL_APP_JUNK";
    public static final String CATEGORY_PATH_RULE_JUNK = "CATEGORY_PATH_RULE_JUNK";

    public static final String JUNK_TYPE_APK = "apk";

    public static final String ICON = "Icon";
    public static final String NOTIFICATION = "Notification";
    public static final String RESIDUAL_FILES = "Residual Files";
    public static final String OBSOLETE_APK = "Obsolete apk";
    public static final String APP_DRAWER = "App Drawer";
    public static final String RESULTPAGE = "Resultpage";

    public static final String BANNER = "Banner";
    public static final String BOTTOM_ALERT = "BottomAlert";
    public static final String DIALOG = "Dialog";

    public static final long JUNK_CLEAN_NOTIFICATION_SECOND_TIME_LIMIT = 24 * 60 * 60;
    public static final long FROZEN_JUNK_SCAN_SECOND_TIME = 3 * 60;

    public static final long NORMAL_JUNK_SIZE = 10 * 1024 * 1024; // 10M
    public static final long DANGER_JUNK_SIZE = 70 * 1024 * 1024; // 70M

    public static final int PRIMARY_YELLOW = 0xFFFFBE00;
    public static final int PRIMARY_BLUE = 0xFF00ADFF;
    public static final int PRIMARY_RED = 0xFFF44336;

    public static boolean sIsTotalSelected;
    public static boolean sIsTotalCleaned;
    public static boolean sIsJunkCleaned;

}
