package com.ihs.feature.common;

/**
 * Central list of files the Launcher writes to the application data directory.
 *
 * To add a new Launcher file, create a String constant referring to the filename, and add it to
 * ALL_FILES, as shown below.
 */
public class LauncherFiles {

    /**
     * File name format for new files: "com.honeycomb.launcher_module".
     */
    public static final String DEFAULT_PREFS = "com.honeycomb.launcher_preferences"; // Multi-process
    public static final String DESKTOP_PREFS = "com.honeycomb.launcher_desktop"; // Main process
    public static final String NEWS_PREFS = "com.honeycomb.launcher_news"; // Main process
    public static final String SEARCH_PREFS = "com.honeycomb.launcher.search.prefs"; // Main process
    public static final String LUCKY_PREFS = "com.honeycomb.launcher.lucky.prefs"; // Main process
    public static final String NEARBY_PREFS = "com.honeycomb.launcher.nearby.prefs"; // Main process
    public static final String CUSTOMIZE_PREFS = "com.honeycomb.launcher.customize.prefs"; // Process ":customize"
    public static final String BOOST_PREFS = "com.honeycomb.launcher_boost"; // Main process
    public static final String MOMENT_PREFS = "com.honeycomb.launcher_moment"; // Main process
    public static final String BATTERY_PREFS = "com.honeycomb.launcher.battery.prefs"; // Main process
    public static final String LOCKER_PREFS = "com.honeycomb.launcher.locker.prefs"; // Main process
    public static final String JUNK_CLEAN_PREFS = "com.honeycomb.launcher.junk.clean.prefs"; // Process ":clean"
    public static final String CPU_COOLER_PREFS = "com.honeycomb.launcher.cpu.cooler.prefs"; // Main process
    public static final String NOTIFICATION_PREFS = "com.honeycomb.launcher.notification.prefs"; // Main process
    public static final String NOTIFICATION_CLEANER_PREFS = "com.honeycomb.launcher.notification.cleaner.prefs"; // Main process
    public static final String WEATHER_PREFS = "com.honeycomb.launcher_weather"; // Main process

    /**
     * File name format for new files: "module_name.db".
     */
    public static final String LAUNCHER_DB = "launcher.db";
    public static final String APP_ICONS_DB = "app_icons.db";
    public static final String WALLPAPER_DB = "wallpaper.db";
    public static final String WIDGET_PREVIEWS_DB = "widget_previews.db";
    public static final String WEATHER_DB = "weather.db";
    public static final String NEWS_DB = "news.db";
    public static final String SEARCH_DB = "search.db";

}
