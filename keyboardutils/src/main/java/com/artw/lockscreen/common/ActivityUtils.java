package com.artw.lockscreen.common;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.ihs.keyboardutils.R;


public class ActivityUtils {

    public static Toolbar configSimpleAppBar(AppCompatActivity activity, @StringRes int titleResId) {
        return configSimpleAppBar(activity, activity.getString(titleResId));
    }

    public static Toolbar configSimpleAppBar(AppCompatActivity activity, String title) {
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.action_bar);

        // Title
        assert toolbar != null;
        toolbar.setTitle("");
        TextView titleTextView = new TextView(activity);
        ViewStyleUtils.setToolBarTitle(titleTextView);
        titleTextView.setText(title);
        toolbar.addView(titleTextView);

        toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.blue_action_bar_bg));
        activity.setSupportActionBar(toolbar);
        if (CommonUtils.ATLEAST_LOLLIPOP) {
            //noinspection ConstantConditions
            activity.getSupportActionBar().setElevation(
                    activity.getResources().getDimensionPixelSize(R.dimen.app_bar_elevation));
        }
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);

        return toolbar;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setBlueStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(activity, R.color.blue_action_bar_bg));
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarColor(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(color);
        }
    }

    public static void hideStatusBar(Activity activity) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public static void showStatusBar(Activity activity) {
        final Window window = activity.getWindow();
        if ((window.getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) ==
                WindowManager.LayoutParams.FLAG_FULLSCREEN) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setNavigationBarAlpha(Activity activity, float alpha) {
        if (CommonUtils.ATLEAST_LOLLIPOP) {
            int alphaInt = (int) (0xff * alpha);
            activity.getWindow().setNavigationBarColor(Color.argb(alphaInt, 0x00, 0x00, 0x00));
        }
    }

    public static void setNavigationBarColor(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setNavigationBarColor(color);
        } else {
            setNavigationBarColorNative(activity, color);
        }
    }

    public static void setNavigationBarColorNative(Activity activity, int color) {
        View navigationBarView = ViewUtils.findViewById(activity, R.id.navigation_bar_bg_v);
        if (null != navigationBarView) {
            if (color == Color.TRANSPARENT) {
                navigationBarView.setVisibility(View.GONE);
            } else {
                int navigationBarHeight = CommonUtils.getNavigationBarHeight(activity);
                if (navigationBarHeight == 0) {
                    navigationBarView.setVisibility(View.GONE);
                } else {
                    InsettableFrameLayout.LayoutParams layoutParams = new InsettableFrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.ignoreInsets = true;
                    layoutParams.height = navigationBarHeight;
                    navigationBarView.setLayoutParams(layoutParams);
                    layoutParams.gravity = Gravity.BOTTOM;
                    navigationBarView.setBackgroundColor(color);
                    navigationBarView.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
