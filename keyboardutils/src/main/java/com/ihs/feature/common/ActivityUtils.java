package com.ihs.feature.common;

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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.artw.lockscreen.common.ViewStyleUtils;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.CommonUtils;


public class ActivityUtils {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setBlueStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(activity, R.color.blue_action_bar_bg));
        }
    }

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

    public static void configSimpleAppBar(AppCompatActivity activity, String title, int bgColor) {
        configSimpleAppBar(activity, title, bgColor, true);
    }

    public static void configSimpleAppBar(AppCompatActivity activity, String title, int bgColor,
                                          boolean containsBackButton) {
        configSimpleAppBar(activity, title, Color.WHITE, bgColor, containsBackButton);
    }

    public static void configSimpleAppBar(AppCompatActivity activity, String title, int titleColor, int bgColor,
                                          boolean containsBackButton) {
        View container = activity.findViewById(R.id.action_bar);

        assert container != null;
        if (container == null) {
            return;
        }
        Toolbar toolbar = null;
        if (container instanceof LinearLayout) {
            toolbar = (Toolbar) container.findViewById(R.id.inner_tool_bar);
        } else {
            toolbar = (Toolbar) container;
        }
        assert toolbar != null;
        if (toolbar == null) {
            return;
        }

        toolbar.setTitle("");
        TextView titleTextView = new TextView(activity);
        ViewStyleUtils.setToolBarTitle(titleTextView, !containsBackButton);
        titleTextView.setTextColor(titleColor);
        titleTextView.setText(title);
        toolbar.addView(titleTextView);

        toolbar.setBackgroundColor(bgColor);
        activity.setSupportActionBar(toolbar);
        if (CommonUtils.ATLEAST_LOLLIPOP) {
            //noinspection ConstantConditions
            container.setElevation(
                    activity.getResources().getDimensionPixelSize(R.dimen.app_bar_elevation));
        } else {
            View line = activity.findViewById(R.id.toolbar_separate_line);
            if (line != null) {
                line.setVisibility(View.VISIBLE);
            }
        }
        if (containsBackButton) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    public static void configSimpleAppBar(AppCompatActivity activity, String title, int bgColor, int logoResource) {
        View container = activity.findViewById(R.id.action_bar);
        assert container != null;
        if (container == null) {
            return;
        }

        Toolbar toolbar = null;
        if (container instanceof LinearLayout) {
            toolbar = (Toolbar) container.findViewById(R.id.inner_tool_bar);
        } else {
            toolbar = (Toolbar) container;
        }
        assert toolbar != null;
        if (toolbar == null) {
            return;
        }

        // Title
        assert toolbar != null;
        toolbar.setTitle("");
        TextView titleTextView = new TextView(activity);
        ViewStyleUtils.setToolBarTitle(titleTextView);
        titleTextView.setText(title);
        toolbar.addView(titleTextView);
        if (logoResource > 0) {
            toolbar.setLogo(logoResource);
        }
        toolbar.setBackgroundColor(bgColor);
        activity.setSupportActionBar(toolbar);
        if (CommonUtils.ATLEAST_LOLLIPOP) {
            //noinspection ConstantConditions
            activity.getSupportActionBar().setElevation(
                    activity.getResources().getDimensionPixelSize(R.dimen.app_bar_elevation));
        }
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
    }



    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setWhiteStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(activity, android.R.color.white));
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setCustomColorStatusBar(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(color);
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

    public static void hideSystemUi(Activity activity) {
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = activity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
}
