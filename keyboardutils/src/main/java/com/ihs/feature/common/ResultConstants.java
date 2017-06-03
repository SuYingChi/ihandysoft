package com.ihs.feature.common;

public class ResultConstants {

    public static final int RESULT_TYPE_BOOST_PLUS = 0;
    public static final int RESULT_TYPE_BATTERY = 1;
    public static final int RESULT_TYPE_JUNK_CLEAN = 2;
    public static final int RESULT_TYPE_CPU_COOLER = 3;
    public static final int RESULT_TYPE_NOTIFICATION_CLEANER = 4;

    public static final int CARD_VIEW_TYPE_INVALID = 0;

    // Feature promotions
    public static final int CARD_VIEW_TYPE_BATTERY = 1;
    public static final int CARD_VIEW_TYPE_BOOST_PLUS = 2;
    public static final int CARD_VIEW_TYPE_JUNK_CLEANER = 3;
    public static final int CARD_VIEW_TYPE_CPU_COOLER = 4;

    // Cross-app promotions
    public static final int CARD_VIEW_TYPE_SECURITY = 5;
    public static final int CARD_VIEW_TYPE_MAX_GAME_BOOSTER = 6;
    public static final int CARD_VIEW_TYPE_MAX_APP_LOCKER = 7;
    public static final int CARD_VIEW_TYPE_MAX_DATA_THIEVES = 8;

    public static final int CARD_VIEW_TYPE_ACCESSIBILITY = 9;

    // Fallbacks when no promotion card applies
    public static final int CARD_VIEW_TYPE_DEFAULT = 10;

    public static final String CHARGING_SCREEN_FULL = "ChargingScreen_Full";
    public static final String NOTIFICATION_CLEANER = "NotificationCleaner";
    public static final String NOTIFICATION_CLEANER_FULL = "NotificationCleaner_Full";
    public static final String AD = "AD";
    public static final String BATTERY = "Battery";
    public static final String BOOST_PLUS = "BoostPlus";
    public static final String JUNK_CLEANER = "JunkCleaner";
    public static final String CPU_COOLER = "CPUCooler";
    public static final String ACCESSIBILITY = "Accessibility";
    public static final String DEFAULT = "DefaultCard";

    public static final String PREF_KEY_LAST_BATTERY_USED_TIME = "last_battery_used_time";
    public static final String PREF_KEY_LAST_BOOST_PLUS_USED_TIME = "last_boost_plus_used_time";
    public static final String PREF_KEY_LAST_JUNK_CLEAN_USED_TIME = "last_junk_clean_used_time";
    public static final String PREF_KEY_LAST_CPU_COOLER_USED_TIME = "last_cpu_cooler_used_time";
}
