package com.ihs.chargingscreen.utils;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import com.ihs.app.framework.HSApplication;

import java.lang.reflect.Field;

/**
 * Created by sharp on 16/4/7.
 */
public class DisplayUtils {

    private static int statusBarHeight = 0;
    private static int screenHeightPixels = 0;
    private static int screenWidthPixels = 0;

    public static DisplayMetrics getDisplayMetrics() {
        DisplayMetrics dm = new DisplayMetrics();
        getDisplay().getMetrics(dm);
        return dm;
    }

    public static Display getDisplay() {
        WindowManager mWindowManager = (WindowManager) HSApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        return mWindowManager.getDefaultDisplay();
    }

    public static int dip2px(int value) {
        DisplayMetrics displayMetrics = HSApplication.getContext().getResources().getDisplayMetrics();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, displayMetrics);
        return Math.round(px);
    }

    public static int getStatusBarHeight() {
        if (statusBarHeight == 0) {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                statusBarHeight = HSApplication.getContext().getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }

    public static int getScreenHeightPixels() {
        if (0 == screenHeightPixels) {
            Display display = DisplayUtils.getDisplay();
            DisplayMetrics displayMetrics = DisplayUtils.getDisplayMetrics();
            // since SDK_INT = 1;
            screenWidthPixels = displayMetrics.widthPixels;
            screenHeightPixels = displayMetrics.heightPixels;
            // includes window decorations (statusbar bar/menu bar)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                try {
                    screenWidthPixels = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                    screenHeightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
                } catch (Exception ignored) {
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                try {
                    Point realSize = new Point();
                    Display.class.getMethod("getRealSize", Point.class).invoke(display, realSize);
                    screenWidthPixels = realSize.x;
                    screenHeightPixels = realSize.y;
                } catch (Exception ignored) {
                }
            }
        }
        return screenHeightPixels;
    }

    public static int getScreenWidthPixels() {
        if (0 == screenWidthPixels) {
            Display display = DisplayUtils.getDisplay();
            DisplayMetrics displayMetrics = DisplayUtils.getDisplayMetrics();
            // since SDK_INT = 1;
            screenWidthPixels = displayMetrics.widthPixels;
            screenHeightPixels = displayMetrics.heightPixels;
            // includes window decorations (statusbar bar/menu bar)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                try {
                    screenWidthPixels = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                    screenHeightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
                } catch (Exception ignored) {
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                try {
                    Point realSize = new Point();
                    Display.class.getMethod("getRealSize", Point.class).invoke(display, realSize);
                    screenWidthPixels = realSize.x;
                    screenHeightPixels = realSize.y;
                } catch (Exception ignored) {
                }
            }
        }
        return screenWidthPixels;
    }

}
