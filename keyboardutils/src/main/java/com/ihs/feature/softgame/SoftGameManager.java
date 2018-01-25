package com.ihs.feature.softgame;

public class SoftGameManager {

    private static SoftGameManager instance;
    private String nativeAdPlacement;
    private String fullscreenAdPlacement;

    public static SoftGameManager getInstance() {
        if (instance == null) {
            instance = new SoftGameManager();
        }
        return instance;
    }

    public void init(String nativeAdPlacement, String fullscreenAdPlacement) {
        this.nativeAdPlacement = nativeAdPlacement;
        this.fullscreenAdPlacement = fullscreenAdPlacement;
    }

    public String getNativeAdPlacement() {
        return nativeAdPlacement;
    }

    public String getFullscreenAdPlacement() {
        return fullscreenAdPlacement;
    }
}
