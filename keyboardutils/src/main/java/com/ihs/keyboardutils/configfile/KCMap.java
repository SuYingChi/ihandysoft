package com.ihs.keyboardutils.configfile;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class KCMap implements Map<String, Object> {
    private Map<String, Object> map;

    public KCMap(Map<String, Object> map) {
        this.map = Collections.unmodifiableMap(map);
    }

    @NonNull
    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @NonNull
    @Override
    public Collection<Object> values() {
        return map.values();
    }

    @NonNull
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return map.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    public int getInt(Object key) {
        return KCUtils.toInt(get(key));
    }

    public double getDouble(Object key) {
        return KCUtils.toDouble(get(key));
    }

    public boolean getBoolean(Object key) {
        return KCUtils.toBoolean(get(key));
    }

    public long getLong(Object key) {
        return KCUtils.toLong(get(key));
    }

    public String getString(Object key) {
        return KCUtils.toString(get(key));
    }

    public KCList getList(Object key) {
        Object object = get(key);
        if (object instanceof KCList) {
            return (KCList) object;
        } else {
            return null;
        }
    }

    public KCMap getMap(Object key) {
        Object object = get(key);
        if (object instanceof KCMap) {
            return (KCMap) object;
        } else {
            return null;
        }
    }
}
