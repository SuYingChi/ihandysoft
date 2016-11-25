package com.ihs.keyboardutils.nativeads;

/**
 * Created by ihandysoft on 16/11/25.
 */

public class NativeAdParams {

    private String poolName;
    private int primaryWidth;
    private float primaryHWRatio;
    private int fetchNativeAdInterval;

    public NativeAdParams(String poolName, int fetchNativeAdInterval) {
        this(poolName, fetchNativeAdInterval, 0, 0);
    }

    public NativeAdParams(String poolName, int fetchNativeAdInterval, int primaryWidth, float primaryHWRatio) {
        this.poolName = poolName;
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

    String getPoolName() {
        return poolName;
    }

    int getPrimaryWidth() {
        return primaryWidth;
    }
}
