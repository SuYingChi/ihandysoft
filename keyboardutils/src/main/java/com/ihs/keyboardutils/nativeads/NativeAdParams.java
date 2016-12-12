package com.ihs.keyboardutils.nativeads;

public class NativeAdParams {
    private String placementName;
    private int primaryWidth;
    private float primaryHWRatio;
    private int fetchNativeAdInterval;

    public NativeAdParams(String placementName, int fetchNativeAdInterval) {
        this(placementName, fetchNativeAdInterval, 0, 0);
    }

    public NativeAdParams(String placementName, int fetchNativeAdInterval, int primaryWidth, float primaryHWRatio) {
        this.placementName = placementName;
        this.primaryWidth = primaryWidth;
        this.primaryHWRatio = primaryHWRatio;
        this.fetchNativeAdInterval = fetchNativeAdInterval;
    }

    float getPrimaryHWRatio() {
        return primaryHWRatio;
    }

    int getFetchNativeAdInterval() {
        return fetchNativeAdInterval;
    }

    String getPlacementName() {
        return placementName;
    }

    int getPrimaryWidth() {
        return primaryWidth;
    }
}
