package com.ihs.feature.common;

/**
 * Constants for all ad placements.
 */
public class AdPlacements {

    public static final String AD_PLACEMENT_NAME_CHARGING_SCREEN = "500_A(NativeAds)Charging";
    public static final String LOCKER_NATIVE_AD_PLACEMENT_NAME = "500_A(NativeAds)LockScreen";

    public static final String SHARED_POOL_NATIVE_AD_PLACEMENT_NAME = "500_A(NativeAds)SevenInOne";
    public static final String SHARED_POOL_NATIVE_AD_FLURRY_KEY_VIEW_IN_APP_SEVEN_IN_ONE = "SevenInOneAds_Viewed_In_App";
    public static final String SHARED_POOL_NATIVE_AD_FLURRY_EVENT_CLICKED_NAME_SEVEN_IN_ONE = "SevenInOneAds_Clicked_In_App";
    public static final String SHARED_POOL_NATIVE_AD_FLURRY_EVENT_SHOWN_NAME_SEVEN_IN_ONE = "SevenInOneAds_Shown";

    public static final String LUCKY_NATIVE_AD_PLACEMENT_NAME = "500_A(NativeAds)Lucky";
    public static final String ALL_APPS_FULL_INTERSTITIAL_AD_PLACEMENT_NAME = "500_A(NativeAds)AppdrawerGift";
    public static final String MOMENT_NATIVE_AD_PLACEMENT_HUB = "500_A(NativeAds)Hub";
    public static final String NEARBY_NATIVE_AD_PLACEMENT_NAME = "500_A(NativeAds)Nearby";
    public static final String MOMENT_NEARBY_NATIVE_AD_PLACEMENT_NAME = "500_A(NativeAds)HubNearby";
    public static final String FOLDER_CLOSE_AD_PLACEMENT_NAME = "500_A(NativeAds)FolderQuit";
    public static final String NEWS_NATIVE_AD_PLACEMENT_NAME = "500_A(NativeAds)News";
    public static final String FOLDER_ALL_APPS_AD_PLACEMENT_NAME = "500_A(NativeAds)AppDrawerFolder";
    public static final String DESKTOP_WIDGET_AD_PLACEMENT_NAME = "500_A(NativeAds)Widget";
    public static final String SEARCH_BAR_NATIVE_AD_PLACEMENT_NAME = "500_A(NativeAds)Search1";
    public static final String SEARCH_NEWS_NATIVE_AD_PLACEMENT_NAME = "500_A(NativeAds)Search2";

    public static final String THEME_NATIVE_AD_PLACEMENT_NAME = "500_A(NativeAds)Theme";
    public static final String WALLPAPER_NATIVE_AD_PLACEMENT_NAME = "500_A(NativeAds)Wallpaper";
    public static final String WALLPAPER_PREVIEW_NATIVE_AD_PLACEMENT_NAME = "500_A(NativeAds)WallpaperDetails";
    public static final String WALLPAPER_THEME_EXIT_AD_PLACEMENT_NAME = "500_A(InterstitialAds)Weel";



    public static final String FLURRY_EVENT_ACB_AD_LOADER_DID_LOAD = "FLURRY_EVENT_ACB_AD_LOADER_LOAD";
    public static final String FLURRY_EVENT_ACB_AD_LOADER_DID_FINISH = "FLURRY_EVENT_ACB_AD_LOADER_DID_FINISH";


    public static final String[] MAIN_PROCESS_PLACEMENTS = {
            AD_PLACEMENT_NAME_CHARGING_SCREEN,
            SHARED_POOL_NATIVE_AD_PLACEMENT_NAME,
            LOCKER_NATIVE_AD_PLACEMENT_NAME,
            LUCKY_NATIVE_AD_PLACEMENT_NAME,
            ALL_APPS_FULL_INTERSTITIAL_AD_PLACEMENT_NAME,
            MOMENT_NATIVE_AD_PLACEMENT_HUB,
            NEARBY_NATIVE_AD_PLACEMENT_NAME,
            MOMENT_NEARBY_NATIVE_AD_PLACEMENT_NAME,
            FOLDER_CLOSE_AD_PLACEMENT_NAME,
            NEWS_NATIVE_AD_PLACEMENT_NAME,
            FOLDER_ALL_APPS_AD_PLACEMENT_NAME,
            DESKTOP_WIDGET_AD_PLACEMENT_NAME,
            SEARCH_BAR_NATIVE_AD_PLACEMENT_NAME,
            SEARCH_NEWS_NATIVE_AD_PLACEMENT_NAME,
    };

    public static final String[] CUSTOMIZE_PROCESS_PLACEMENTS = {
            THEME_NATIVE_AD_PLACEMENT_NAME,
            WALLPAPER_NATIVE_AD_PLACEMENT_NAME,
            WALLPAPER_PREVIEW_NATIVE_AD_PLACEMENT_NAME,
    };
}
