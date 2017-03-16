package com.ihs.keyboardutils.nativeads;

import android.widget.ImageView;

public class NativeAdParams {
    private String placementName;
    private int primaryWidth;
    private float primaryHWRatio;
    private ImageView.ScaleType scaleType = ImageView.ScaleType.FIT_XY;

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

    public void setScaleType(ImageView.ScaleType scaleType) {
        this.scaleType = scaleType;
    }

    public ImageView.ScaleType getScaleType() {
        return scaleType;
    }
}
