package com.ihs.chargingscreen.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import com.ihs.app.framework.HSApplication;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

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

    public static Point getNavigationBarSize(Context context) {
        Point appUsableSize = getAppUsableScreenSize(context);
        Point realScreenSize = getRealScreenSize(context);

        // navigation bar on the right
        if (appUsableSize.x < realScreenSize.x) {
            return new Point(realScreenSize.x - appUsableSize.x, appUsableSize.y);
        }

        // navigation bar at the bottom
        if (appUsableSize.y < realScreenSize.y) {
            return new Point(appUsableSize.x, realScreenSize.y - appUsableSize.y);
        }

        // navigation bar is not present
        return new Point();
    }

    public static Point getAppUsableScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static Point getRealScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();

        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealSize(size);
        } else if (Build.VERSION.SDK_INT >= 14) {
            try {
                size.x = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                size.y = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            } catch (IllegalAccessException e) {} catch (InvocationTargetException e) {} catch (NoSuchMethodException e) {}
        }

        return size;
    }

    public static int getScreenWidthForContent() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }
}
