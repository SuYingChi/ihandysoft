package com.ihs.feature.common;

/**
 * Constants for all ad placements.
 */
public class AdPlacements {

    public static final String AD_PLACEMENT_NAME_CHARGING_SCREEN = "500_A(NativeAds)Charging";
    public static final String LOCKER_NATIVE_AD_PLACEMENT_NAME = "500_A(NativeAds)LockScreen";

    public static final String RESULT_PAGE_AD_PLACEMENT_NAME = "500_A(NativeAds)SevenInOne";
    public static final String SHARED_POOL_NATIVE_AD_FLURRY_KEY_VIEW_IN_APP_RESULT_PAGE = "SevenInOneAds_Viewed_In_App";
    public static final String SHARED_POOL_NATIVE_AD_FLURRY_EVENT_CLICKED_NAME_RESULT_PAGE = "SevenInOneAds_Clicked_In_App";
    public static final String SHARED_POOL_NATIVE_AD_FLURRY_EVENT_SHOWN_NAME_RESULT_PAGE = "SevenInOneAds_Shown";

    public static final String ALL_APPS_GIFT_AD_PLACEMENT_NAME = "500_A(NativeAds)AppdrawerGift";
    public static final String SHARED_POOL_NATIVE_AD_FLURRY_EVENT_SHOWN_NAME_LUCKY = "LuckyAds_Shown";
    public static final String SHARED_POOL_NATIVE_AD_FLURRY_EVENT_CLICKED_NAME_LUCKY = "LuckyAds_Clicked_In_App";

    public static final String FOLDER_ALL_APPS_AD_PLACEMENT_NAME = "500_A(NativeAds)AppDrawerFolder";
    public static final String SHARED_POOL_NATIVE_AD_FLURRY_EVENT_SHOWN_NAME_THREE_IN_ONE = "ThreeInOneAds_Shown";
    public static final String SHARED_POOL_NATIVE_AD_FLURRY_EVENT_CLICKED_NAME_THREE_IN_ONE = "ThreeInOneAds_Clicked_In_App";

    public static final String NEARBY_NATIVE_AD_PLACEMENT_NAME = "500_A(NativeAds)Nearby";
    public static final String SHARED_POOL_NATIVE_AD_FLURRY_EVENT_SHOWN_NAME_NEARBY = "NearByAds_Shown";
    public static final String SHARED_POOL_NATIVE_AD_FLURRY_EVENT_CLICKED_NAME_NEARBY = "NearBtAds_Clicked_In_App";

    public static final String MOMENT_NATIVE_AD_PLACEMENT_HUB = "500_A(NativeAds)Hub";
    public static final String SHARED_POOL_NATIVE_AD_FLURRY_EVENT_SHOWN_NAME_SIX_IN_ONE = "SixInOneAds_Shown";
    public static final String SHARED_POOL_NATIVE_AD_FLURRY_EVENT_CLICKED_NAME_SIX_IN_ONE = "SixInOneAds_Clicked_In_App";

    public static final String WALLPAPER_NATIVE_AD_PLACEMENT_NAME = "500_A(NativeAds)Wallpaper";
    public static final String SHARED_POOL_NATIVE_AD_FLURRY_EVENT_SHOWN_NAME_WALLPAPER_THEME = "WallpaperThemeAds_Shown";
    public static final String SHARED_POOL_NATIVE_AD_FLURRY_EVENT_CLICKED_NAME_WALLPAPER_THEME = "WallpaperThemeAds_Clicked_In_App";

    public static final String WALLPAPER_THEME_EXIT_AD_PLACEMENT_NAME = "500_A(InterstitialAds)Weel";



    public static final String FLURRY_EVENT_ACB_AD_LOADER_DID_LOAD = "FLURRY_EVENT_ACB_AD_LOADER_LOAD";
    public static final String FLURRY_EVENT_ACB_AD_LOADER_DID_FINISH = "FLURRY_EVENT_ACB_AD_LOADER_DID_FINISH";


    public static final String[] MAIN_PROCESS_PLACEMENTS = {
            AD_PLACEMENT_NAME_CHARGING_SCREEN,
            RESULT_PAGE_AD_PLACEMENT_NAME,
            LOCKER_NATIVE_AD_PLACEMENT_NAME,
            ALL_APPS_GIFT_AD_PLACEMENT_NAME,
            MOMENT_NATIVE_AD_PLACEMENT_HUB,
            NEARBY_NATIVE_AD_PLACEMENT_NAME,
            FOLDER_ALL_APPS_AD_PLACEMENT_NAME,
    };

    public static final String[] CUSTOMIZE_PROCESS_PLACEMENTS = {
            WALLPAPER_NATIVE_AD_PLACEMENT_NAME,
            WALLPAPER_THEME_EXIT_AD_PLACEMENT_NAME,
    };
}
