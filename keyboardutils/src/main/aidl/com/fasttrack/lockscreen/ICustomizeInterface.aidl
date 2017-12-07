// ICustomizeInterface.aidl
package com.fasttrack.lockscreen;

// Declare any non-default types here with import statements

interface ICustomizeInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
//    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
//            double aDouble, String aString);

    void setTheme(String themeInfo);

    void enableLocker();

    String getCurrentTheme();

    void updateWeatherIfNeed();

    String getWeatherTemp();

    String getWeatherText(int type);

    void addLocalTheme(String themeName);

    boolean isLockerEnable();
}
