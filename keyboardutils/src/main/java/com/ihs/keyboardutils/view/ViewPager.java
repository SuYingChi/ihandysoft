package com.ihs.keyboardutils.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.crashlytics.android.Crashlytics;

/**
 * Created by guanche on 19/09/2017.
 */

public class ViewPager extends android.support.v4.view.ViewPager {
    public ViewPager(Context context) {
        super(context);
    }

    public ViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean result = false;
        try {
            result = super.onInterceptTouchEvent(ev);
        } catch (Exception e) {
            Crashlytics.setString("context", getContext().toString());
            Crashlytics.setString("parent", getParent().toString());
            Crashlytics.setString("event", ev.toString());
            Crashlytics.logException(e);
        }
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean result = false;
        try {
            result = super.onTouchEvent(ev);
        } catch (Exception e) {
            Crashlytics.setString("context", getContext().toString());
            Crashlytics.setString("parent", getParent().toString());
            Crashlytics.setString("event", ev.toString());
            Crashlytics.logException(e);
        }
        return result;
    }
}
