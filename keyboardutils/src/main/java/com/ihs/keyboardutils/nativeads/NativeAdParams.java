package com.ihs.keyboardutils.nativeads;

public class NativeAdParams {
    private String placementName;
    private int primaryWidth;
    private float primaryHWRatio;

    public NativeAdParams(String placementName) {
        this(placementName, 0, 0);
    }

    public NativeAdParams(String placementName, int primaryWidth, float primaryHWRatio) {
        this.placementName = placementName;
        this.primaryWidth = primaryWidth;
        this.primaryHWRatio = primaryHWRatio;
    }

    float getPrimaryHWRatio() {
        return primaryHWRatio;
    }

    String getPlacementName() {
        return placementName;
    }

    int getPrimaryWidth() {
        return primaryWidth;
    }
}
