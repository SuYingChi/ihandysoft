package com.kc.utils.phantom;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Application;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.ihs.commons.config.HSConfig;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.nativeads.KCNativeAdView;
import com.kc.utils.Compats;
import com.kc.utils.KCFeatureControlUtils;


public class KCPhantomNotificationManager {
    private final String adPlacement;

    private final Application applicationContext;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private Runnable showNotificationRunnable = this::showNotification;

    private static final int DEFAULT_DISPLAY_SECONDS = 3;
    private static final int DEFAULT_INTERVAL_SECONDS = 3600;
    private static final int DEFAULT_USER_PRESENT_DELAY_SECONDS = 0;
    private static final int DEFAULT_RETRY_SECONDS = 600;
    private static final int DEFAULT_ANIMATION_DURATION = 300;

    private static final String FEATURE_NAME = "Phantom";

    @SuppressLint("StaticFieldLeak")
    private volatile static KCPhantomNotificationManager instance;

    private KCPhantomNotificationManager(Context context, String adPlacement) {
        this.applicationContext = (Application) context.getApplicationContext();
        this.adPlacement = adPlacement;

        registerBroadcastReceiver();
    }

    private boolean isFeatureEnabled() {
        boolean enabled = HSConfig.optBoolean(false, "Application", "Phantom", "Enabled");
        if (!enabled) {
            return false;
        }
        int limit = HSConfig.optInteger(Integer.MAX_VALUE, "Application", "Phantom", "MaxCountPerDay");
        boolean limitReached = KCFeatureControlUtils.isCountLimitReachedToday(applicationContext, FEATURE_NAME, limit);
        if (limitReached) {
            return false;
        }
        return true;
    }

    private void registerBroadcastReceiver() {
        this.applicationContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (TextUtils.equals(intent.getAction(), Intent.ACTION_USER_PRESENT)) {
                    if (isFeatureEnabled()) {
                        int delaySeconds = HSConfig.optInteger(DEFAULT_USER_PRESENT_DELAY_SECONDS, "Application", "Phantom", "UserPresentDelaySeconds");
                        handler.post(() -> {
                            scheduleNotifications(delaySeconds * 1000);
                        });
                    }
                }

            }
        }, new IntentFilter(Intent.ACTION_USER_PRESENT));
    }

    private void scheduleNotifications(long delayMillis) {
        handler.removeCallbacks(showNotificationRunnable);

        if (isFeatureEnabled()) {
            handler.postDelayed(showNotificationRunnable, delayMillis);
        }
    }

    public static KCPhantomNotificationManager with(Context context, String adPlacement) {
        if (instance == null) {
            synchronized (KCPhantomNotificationManager.class) {
                if (instance == null) {
                    instance = new KCPhantomNotificationManager(context, adPlacement);
                }
            }
        }
        return instance;
    }

    protected static KCPhantomNotificationManager instance() {
        if (instance == null)
            throw new IllegalStateException("Must Initialize Phantom before using singleton()");
        return instance;
    }

    private void showNotification() {
        View adLayoutView = View.inflate(applicationContext, R.layout.layout_ad_phantom, null);
        KCNativeAdView adView = new KCNativeAdView(applicationContext);
        adView.setAdLayoutView(adLayoutView);
        adView.load(adPlacement);
        adView.setOnAdLoadedListener((view) -> {
            if (!isKeyguardLocked(applicationContext)) {
                WindowManager windowManager = (WindowManager) applicationContext.getSystemService(Context.WINDOW_SERVICE);
                displayBanner(windowManager, view);
            }
        });
        adView.setOnAdFailListener((view) -> {
            int retrySeconds = HSConfig.optInteger(DEFAULT_RETRY_SECONDS, "Application", "Phantom", "RetrySeconds");
            scheduleNotifications(retrySeconds * 1000);
        });
        adView.setOnAdClickedListener((view) -> {
            WindowManager windowManager = (WindowManager) applicationContext.getSystemService(Context.WINDOW_SERVICE);
            dismissBanner(windowManager, view);
        });
    }

    private void displayBanner(WindowManager windowManager, View view) {
        if (windowManager == null || view == null || view.getParent() != null) {
            return;
        }
        int notificationHeight = applicationContext.getResources().getDimensionPixelSize(R.dimen.phantom_notification_height);
        int displaySeconds = HSConfig.optInteger(DEFAULT_DISPLAY_SECONDS, "Application", "Phantom", "DisplaySeconds");

        RelativeLayout adContainer = new RelativeLayout(view.getContext());

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = needsSystemErrorFloatWindow()?
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR:
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        params.format = PixelFormat.TRANSLUCENT;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        params.gravity = Gravity.TOP;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = notificationHeight;
        windowManager.addView(adContainer, params);

        RelativeLayout.LayoutParams viewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, notificationHeight);
        viewParams.setMargins(0, -notificationHeight, 0, 0);
        adContainer.addView(view, viewParams);

        view.animate().translationY(notificationHeight).setDuration(DEFAULT_ANIMATION_DURATION)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.postDelayed(() -> {
                            dismissBanner(windowManager, view);
                        }, displaySeconds * 1000);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        dismissBanner(windowManager, view);
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        playSounds(applicationContext);

        KCFeatureControlUtils.increaseCountToday(applicationContext, FEATURE_NAME);
    }

    private void playSounds(Context context) {
        // vibrate
        if (HSConfig.optBoolean(false, "Application", "Phantom", "VibrationEnabled")) {
            try {
                Vibrator vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
                if (vibrator != null) {
                    vibrator.vibrate(new long[]{100L, 200L, 100L, 200L}, -1);
                }
            } catch (Exception e) {
                // Ignore exception
            }
        }

        // sound
        if (HSConfig.optBoolean(false, "Application", "Phantom", "SoundEnabled")) {
            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(context, notification);
                r.play();
            } catch (Exception e) {
                // Ignore exception
            }
        }
    }

    private void dismissBanner(WindowManager windowManager, View view) {
        if (windowManager == null || view == null || view.getParent() == null) {
            return;
        }


        view.animate()
                .translationY(-view.getHeight())
                .setDuration(DEFAULT_ANIMATION_DURATION)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        doAnimationEnd();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        doAnimationEnd();
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }

                    private void doAnimationEnd() {
                        int intervalSeconds = HSConfig.optInteger(DEFAULT_INTERVAL_SECONDS, "Application", "Phantom", "IntervalSeconds");
                        windowManager.removeView((View) view.getParent());
                        scheduleNotifications(intervalSeconds * 1000);
                    }
                })
                .start();
    }

    private static boolean isKeyguardLocked(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean isLocked = false;
        if (keyguardManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            isLocked = keyguardManager.isKeyguardLocked();
        }
        return isLocked;
    }

    public static boolean needsSystemErrorFloatWindow() {
        int sdkLevel = Build.VERSION.SDK_INT;
        return sdkLevel >= Build.VERSION_CODES.LOLLIPOP
                || (sdkLevel == Build.VERSION_CODES.KITKAT && Compats.IS_HUAWEI_DEVICE)
                || (sdkLevel == Build.VERSION_CODES.JELLY_BEAN_MR2 && Compats.IS_HTC_DEVICE)
                || (sdkLevel == Build.VERSION_CODES.JELLY_BEAN_MR2 && Compats.IS_SONY_DEVICE)
                || (sdkLevel == Build.VERSION_CODES.KITKAT && Compats.IS_SAMSUNG_DEVICE);
    }
}
