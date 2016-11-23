package com.ihs.keyboardutils.nativeads;

import android.support.v4.util.ArrayMap;

import java.util.ArrayList;

/**
 * Created by ihandysoft on 16/11/23.
 */

public class NativeAdProfile {

    private static ArrayMap<String, NativeAdProfile> nativeAdProfileArrayMap = new ArrayMap<>();

    private String poolName;
    private int hasShowedCount;
    private int availableCount;
    private String vendorName;
    private long cachedNativeAdTime;

    private NativeAdProfile(String poolName) {
        this.poolName = poolName;
    }

    public static NativeAdProfile get(String poolName) {
        if (nativeAdProfileArrayMap.containsKey(poolName)) {
            return nativeAdProfileArrayMap.get(poolName);
        } else {
            NativeAdProfile nativeAdProfile = new NativeAdProfile(poolName);
            nativeAdProfileArrayMap.put(poolName, nativeAdProfile);
            return nativeAdProfile;
        }
    }

    public int getHasShowedCount() {
        return hasShowedCount;
    }

    void incHasShowedCount() {
        this.hasShowedCount += 1;
    }

    public int getAvailableCount() {
        return availableCount;
    }

    void setAvailableCount(int availableCount) {
        this.availableCount = availableCount;
    }

    public String getVendorName() {
        return vendorName;
    }

    void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public long getCachedNativeAdTime() {
        return cachedNativeAdTime;
    }

    void setCachedNativeAdTime(long cachedNativeAdTime) {
        this.cachedNativeAdTime = cachedNativeAdTime;
    }

    void release(){
        nativeAdProfileArrayMap.remove(poolName);
    }


    public static ArrayList<String> getAllNativeAdPoolState() {
        ArrayList<String> result = new ArrayList<>();
        for (NativeAdProfile nativeAdProfile : nativeAdProfileArrayMap.values()) {
            StringBuilder stringBuilder = new StringBuilder(nativeAdProfile.poolName);
            stringBuilder.append("(CC:");
            stringBuilder.append(nativeAdProfile.availableCount);
            stringBuilder.append(";SC:" + nativeAdProfile.hasShowedCount + ")");
            result.add(stringBuilder.toString());
        }
        return result;
    }
}
