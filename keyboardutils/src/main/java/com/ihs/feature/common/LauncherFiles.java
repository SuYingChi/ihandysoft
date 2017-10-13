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
    public static final String BOOST_PREFS = "com.honeycomb.launcher_boost"; // Main process
    public static final String BATTERY_PREFS = "com.honeycomb.launcher.battery.prefs"; // Main process
    public static final String LOCKER_PREFS = "com.honeycomb.launcher.locker.prefs"; // Main process
    public static final String JUNK_CLEAN_PREFS = "com.honeycomb.launcher.junk.clean.prefs"; // Process ":clean"
    public static final String CPU_COOLER_PREFS = "com.honeycomb.launcher.cpu.cooler.prefs"; // Main process
    public static final String NOTIFICATION_PREFS = "com.honeycomb.launcher.notification.prefs"; // Main process
    public static final String NOTIFICATION_CLEANER_PREFS = "com.honeycomb.launcher.notification.cleaner.prefs"; // Main process
    public static final String WEATHER_PREFS = "com.honeycomb.launcher_weather"; // Main process
}
