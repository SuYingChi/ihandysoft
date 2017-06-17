package com.ihs.feature.common;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.ihs.keyboardutils.utils.CommonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Profile utility. For API 17 or above.
 *
 * Note that for convenience of test this tool does NOT shortcut its operation on release builds,
 * and uses {@link Log} instead of {@link com.ihs.commons.utils.HSLog} as logging facility.
 * Be sure to shortcut, if not remove, any usage of this class upon shipping the application.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class Profiler {

    private static final String TAG = "Launcher.Profiler";

    private static final String DEFAULT_PROFILE_NAME = "default_profile";

    private static Map<String, ProfileInfo> sProfileMap = new HashMap<>(1);

    public static void start() {
        start(DEFAULT_PROFILE_NAME);
    }

    public static void start(String profileName) {
        if (CommonUtils.ATLEAST_JB_MR1) {
            sProfileMap.put(profileName, new ProfileInfo(profileName));
        }
    }

    public static void end() {
        end(DEFAULT_PROFILE_NAME);
    }

    public static void end(String profileName) {
        if (CommonUtils.ATLEAST_JB_MR1) {
            ProfileInfo profile = sProfileMap.get(profileName);
            if (profile != null) {
                profile.endProfile();
                profile.dump();
                sProfileMap.remove(profileName);
            } else {
                Log.w(TAG, "Profile " + profileName + " is not started");
            }
        }
    }

    private static class ProfileInfo {
        String name;

        long startTimeNanos;
        long endTimeNanos;
        long startThreadTimeMillis;
        long endThreadTimeMillis;

        ProfileInfo(String profileName) {
            name = profileName;
            startTimeNanos = SystemClock.elapsedRealtimeNanos();
            startThreadTimeMillis = SystemClock.currentThreadTimeMillis();
        }

        void endProfile() {
            endTimeNanos = SystemClock.elapsedRealtimeNanos();
            endThreadTimeMillis = SystemClock.currentThreadTimeMillis();
        }

        void dump() {
            Log.i(TAG, "Profiling result: " + name +
                    " costs " + (endTimeNanos - startTimeNanos) / 1000 + " us in real time, " +
                    (endThreadTimeMillis - startThreadTimeMillis) * 1000 + " us in current thread");
        }
    }
}
