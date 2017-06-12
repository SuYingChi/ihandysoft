package com.ihs.feature.lucky;

import java.io.File;

/**
 * Created by Arthur on 17/5/27.
 */

public class ThemeBean {

    public static final String THEME_DIRECTORY = "preload" + File.separator + "theme";
    public static final String ICON = "Icon";
    public static final String BANNER = "Banner";

    private String themeName;
    private String themeBannerUrl;
    private String themeIconUrl;

    public String getThemeName() {
        return themeName;
    }

    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }

    public String getThemeBannerUrl() {
        return themeBannerUrl;
    }

    public void setThemeBannerUrl(String themeBannerUrl) {
        this.themeBannerUrl = themeBannerUrl;
    }

    public String getThemeIconUrl() {
        return themeIconUrl;
    }

    public void setThemeIconUrl(String themeIconUrl) {
        this.themeIconUrl = themeIconUrl;
    }

    public ThemeBean(String themeName, String themeBannerUrl, String themeIconUrl) {
        this.themeName = themeName;
        this.themeBannerUrl = themeBannerUrl;
        this.themeIconUrl = themeIconUrl;
    }
}
