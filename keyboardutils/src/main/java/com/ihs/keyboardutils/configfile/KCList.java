package com.ihs.keyboardutils.configfile;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class KCList implements List<Object> {
    private List<Object> list;

    public KCList(List<Object> list) {
        this.list = Collections.unmodifiableList(list);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator<Object> iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <Object> Object[] toArray(Object[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object set(int index, Object element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, Object element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<Object> listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator<Object> listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public List<Object> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    @Override
    public Object get(int index) {
        return list.get(index);
    }

    public int getInt(int index) {
        return KCUtils.toInt(get(index));
    }

    public double getDouble(int index) {
        return KCUtils.toDouble(get(index));
    }

    public boolean getBoolean(int index) {
        return KCUtils.toBoolean(get(index));
    }

    public long getLong(int index) {
        return KCUtils.toLong(get(index));
    }

    public String getString(int index) {
        return KCUtils.toString(get(index));
    }

    public KCList getList(int index) {
        Object object = get(index);
        if (object instanceof KCList) {
            return (KCList) object;
        } else {
            return null;
        }
    }

    public KCMap getMap(int index) {
        Object object = get(index);
        if (object instanceof KCMap) {
            return (KCMap) object;
        } else {
            return null;
        }
    }
}
