package com.ihs.feature.tip;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.honeycomb.launcher.R;
import com.honeycomb.launcher.chargingscreen.ChargingScreenSettings;
import com.honeycomb.launcher.chargingscreen.view.BatteryAnimatorHelper;
import com.honeycomb.launcher.chargingscreen.view.FlashButton;
import com.honeycomb.launcher.desktop.Launcher;
import com.honeycomb.launcher.desktop.state.LauncherView;
import com.honeycomb.launcher.locker.LockerSettings;
import com.honeycomb.launcher.model.LauncherFiles;
import com.honeycomb.launcher.util.CommonUtils;
import com.honeycomb.launcher.util.PreferenceHelper;
import com.honeycomb.launcher.util.ViewUtils;
import com.ihs.app.analytics.HSAnalytics;

import java.util.Map;

/**
 * Created by lz on 4/6/17.
 */

public class ChargingScreenGuideView extends LinearLayout implements LauncherView {

    private Animator mTransAnimator;
    private BatteryAnimatorHelper batteryAnimatorHelper;
    private FlashButton flashButton;

    public ChargingScreenGuideView(Context context) {
        this(context, null);
    }

    public ChargingScreenGuideView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChargingScreenGuideView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();

        setPadding(0, 0, 0, CommonUtils.getNavigationBarHeight(getContext()));

        ViewUtils.findViewById(this, R.id.ic_close).setOnClickListener(v -> dismissSelf());

        View batteryLayout = findViewById(R.id.charging_alert_battery_layout);
        final int densityDpi = getResources().getDisplayMetrics().densityDpi;
        if (densityDpi <= DisplayMetrics.DENSITY_HIGH) {
            batteryLayout.setScaleX(0.9f);
            batteryLayout.setScaleY(0.9f);
        }

        final View chargingAlertContent = findViewById(R.id.charging_alert_content);
        batteryAnimatorHelper = new BatteryAnimatorHelper(chargingAlertContent);
        chargingAlertContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                chargingAlertContent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                batteryAnimatorHelper.displayAnimator();
            }
        });

        flashButton = (FlashButton) findViewById(R.id.charging_alert_button);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            flashButton.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        } else {
            flashButton.setTypeface(Typeface.SANS_SERIF);
        }
        flashButton.setRepeatCount(10);
        flashButton.startFlash();

        flashButton.setOnClickListener(v -> {
            ChargingScreenSettings.setChargingScreenEnabled(true);
            if (!LockerSettings.isLockerEverEnabled()) {
                LockerSettings.setLockerEnabled(true);
            }
            Toast.makeText(getContext(), R.string.charging_screen_guide_turn_on, Toast.LENGTH_SHORT).show();
            HSAnalytics.logEvent("Alert_ChargingScreen_TurnOn_Clicked", "type", "Turn on");
            dismissSelf();
        });
    }

    private void dismissSelf() {
        ObjectAnimator slideOut = ObjectAnimator.ofFloat(ChargingScreenGuideView.this, View.TRANSLATION_Y, getMeasuredHeight());
        slideOut.setDuration(300);
        slideOut.setInterpolator(new AccelerateInterpolator());
        slideOut.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                ((Launcher) getContext()).getLauncherViewManager().finish(false, ChargingScreenGuideView.this);
            }
        });
        slideOut.start();
    }

    @Override public void onAdd(Map<String, Object> args) {

    }

    @Override public void transitionIn(boolean animated) {
        setTranslationY(getMeasuredHeight());
        setVisibility(View.VISIBLE);
        mTransAnimator = ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, 0);
        mTransAnimator.setDuration(300);
        mTransAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mTransAnimator.start();

        ChargingScreenSettings.increaseChargingScreenGuideShowCount();
        PreferenceHelper.get(LauncherFiles.LOCKER_PREFS).putInt(
                ChargingScreenSettings.PREF_KEY_CHARGING_SCREEN_GUIDE_LAST_SHOW_TIME,
                ChargingScreenSettings.getChargingCount());
    }

    @Override public void onStart() {

    }

    @Override public void onStop(boolean isFinishing) {

    }

    @Override public void transitionOut(boolean animated) {
        if (getParent() == null) {
            return;
        }
        flashButton.stopFlash();
        batteryAnimatorHelper.release();
        ((ViewGroup) getParent()).removeView(this);
    }

    @Override public void onRemove() {

    }

    @Override public boolean acceptBottomLauncherView(LauncherView launcherView) {
        return true;
    }

    @Override public boolean onBackPressed() {
        dismissSelf();
        return true;
    }

    @Override public String getDescription() {
        return null;
    }

    @Override public void finish(boolean animated) {

    }
}
