package com.ihs.feature.common;

import android.support.annotation.NonNull;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSPreferenceHelper;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This helper class wraps {@link HSPreferenceHelper} and:
 * <p/>
 * (1) Provides access to multiple preferences files with cached helper instances.
 * (2) Is thread safe.
 * (3) Provides convenience methods {@link #doOnce(Runnable, String)}, {@link #incrementAndGetInt(String)},
 *     {@link #putStringList(String, List)} and {@link #getStringList(String)}.
 *
 * This class is only for (a) preferences files used in single process, (b) the default preferences file. Don't use this
 * for preferences file used in multi-process.
 */
public class PreferenceHelper {

    private static Map<String, PreferenceHelper> sHelpersCache = new HashMap<>();

    private HSPreferenceHelper mFrameworkHelper;

    /**
     * {@link Integer} has an internal cache to support the object identity semantics of autoboxing for values between
     * -128 and 127 (inclusive) as required by JLS. We use the non-negative half of these cached objects as segmented
     * locks by mapping pref key strings to them. This is to avoid applying a global lock for all (unrelated) keys.
     */
    private static final int INTEGER_CACHE_UPPER_BOUND = 128;

    /**
     * Get default PreferenceHelper object.
     */
    public static PreferenceHelper getDefault() {
        return get(LauncherFiles.DEFAULT_PREFS);
    }

    /**
     * Get PreferenceHelper by filename.
     */
    public synchronized static PreferenceHelper get(String filename) {
        // Elements in HashMap's backing array do not have a volatile semantic.
        // So double checked locking is not safe here.
        PreferenceHelper prefs = sHelpersCache.get(filename);
        if (prefs == null) {
            if (LauncherFiles.DEFAULT_PREFS.equals(filename)) {
                prefs = new PreferenceHelper(HSPreferenceHelper.getDefault());
            } else {
                prefs = new PreferenceHelper(HSPreferenceHelper.create(HSApplication.getContext(), filename));
            }
            sHelpersCache.put(filename, prefs);
        }
        return prefs;
    }

    private PreferenceHelper(HSPreferenceHelper preferenceHelper) {
        mFrameworkHelper = preferenceHelper;
    }

    /**
     * Execute the given action only once for the given token.
     *
     * Note that this method is thread safe only with token consumption.
     * Action execution should be synchronized by caller if necessary.
     *
     * @param action The action to perform.
     * @param token  The identifier on which the action can be performed only once.
     * @return {@code true} if the action is performed. {@code false} if the action has already been done before and
     * not performed this time.
     */
    public boolean doOnce(@NonNull Runnable action, String token) {
        boolean run = false;
        synchronized (getLock(token)) {
            if (!mFrameworkHelper.getBoolean(token, false)) {
                mFrameworkHelper.putBoolean(token, true);
                run = true;
            }
        }
        if (run) {
            action.run();
        }
        return run;
    }

    /**
     * Note that this method is thread safe only with token consumption.
     * Action execution should be synchronized by caller if necessary.
     */
    public boolean doLimitedTimes(@NonNull Runnable action, String token, int limitedTimes) {
        int tokenConsumedTimesAfterThis = incrementAndGetInt(token);
        if (tokenConsumedTimesAfterThis <= limitedTimes) {
            action.run();
            return true;
        }
        return false;
    }

    /**
     * Increment an integer on the given key by 1.
     *
     * @return The value of the integer *AFTER* incrementation.
     */
    public int incrementAndGetInt(String key) {
        int incremented;
        synchronized (getLock(key)) {
            incremented = mFrameworkHelper.getInt(key, 0) + 1;
            mFrameworkHelper.putInt(key, incremented);
        }
        return incremented;
    }

    public boolean contains(String key) {
        synchronized (getLock(key)) {
            return mFrameworkHelper.contains(key);
        }
    }

    public void remove(String key) {
        synchronized (getLock(key)) {
            mFrameworkHelper.remove(key);
        }
    }

    public int getInt(String key, int defaultValue) {
        synchronized (getLock(key)) {
            return mFrameworkHelper.getInt(key, defaultValue);
        }
    }

    public void putInt(String key, int value) {
        synchronized (getLock(key)) {
            mFrameworkHelper.putInt(key, value);
        }
    }

    public long getLong(String key, long defaultValue) {
        synchronized (getLock(key)) {
            return mFrameworkHelper.getLong(key, defaultValue);
        }
    }

    public void putLong(String key, long value) {
        synchronized (getLock(key)) {
            mFrameworkHelper.putLong(key, value);
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        synchronized (getLock(key)) {
            return mFrameworkHelper.getBoolean(key, defaultValue);
        }
    }

    public void putBoolean(String key, boolean value) {
        synchronized (getLock(key)) {
            mFrameworkHelper.putBoolean(key, value);
        }
    }

    public String getString(String key, String defaultValue) {
        synchronized (getLock(key)) {
            return mFrameworkHelper.getString(key, defaultValue);
        }
    }

    public void putString(String key, String value) {
        synchronized (getLock(key)) {
            mFrameworkHelper.putString(key, value);
        }
    }

    public List<String> getStringList(String key) {
        String listCsv;
        synchronized (getLock(key)) {
            listCsv = mFrameworkHelper.getString(key, "");
        }
        return Utils.getStringList(listCsv);
    }

    public void putStringList(String key, List<String> stringList) {
        if (stringList != null) {
            String listCsv = Utils.getStringListCsv(stringList);
            synchronized (getLock(key)) {
                mFrameworkHelper.putString(key, listCsv);
            }
        }
    }

    public void addStringToList(String key, String value) {
        if (value != null) {
            synchronized (getLock(key)) {
                List<String> stringList = getStringList(key);
                if (!stringList.contains(value)) {
                    stringList.add(value);
                    putStringList(key, stringList);
                }
            }
        }
    }

    public boolean removeStringFromList(String key, String value) {
        if (value != null) {
            boolean contains;
            synchronized (getLock(key)) {
                List<String> stringList = getStringList(key);
                contains = stringList.remove(value);
                putStringList(key, stringList);
            }
            return contains;
        }
        return false;
    }

    public void addMap(String key, Map<String,String> inputMap){
        synchronized (getLock(key)) {
            Map<String,String> savedMap = getMap(key);
            for (Map.Entry<String, String> entry : inputMap.entrySet()) {
                savedMap.put(entry.getKey(), entry.getValue());
            }

            JSONObject jsonObject = new JSONObject(savedMap);
            String jsonString = jsonObject.toString();
            mFrameworkHelper.putString(key, jsonString);
        }
    }

    public void putMap(String key, Map<String,String> inputMap){
        synchronized (getLock(key)) {
            JSONObject jsonObject = new JSONObject(inputMap);
            String jsonString = jsonObject.toString();
            mFrameworkHelper.putString(key, jsonString);
        }
    }

    public Map<String,String> getMap(String key){
        synchronized (getLock(key)) {
            Map<String,String> outputMap = new HashMap<>();
            try{
                String jsonString = mFrameworkHelper.getString(key, (new JSONObject()).toString());
                JSONObject jsonObject = new JSONObject(jsonString);
                Iterator<String> keysItr = jsonObject.keys();
                while(keysItr.hasNext()) {
                    String k = keysItr.next();
                    String v = (String) jsonObject.get(k);
                    outputMap.put(k,v);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            return outputMap;
        }
    }

    private Object /* Integer */ getLock(String key) {
        return key.hashCode() % INTEGER_CACHE_UPPER_BOUND;
    }

}
