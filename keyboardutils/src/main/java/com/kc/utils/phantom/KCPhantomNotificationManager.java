package com.kc.utils.phantom;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.nativeads.KCNativeAdView;

import net.appcloudbox.ads.expressads.AcbExpressAdView;


public class KCPhantomNotificationManager {
    private final String adPlacement;

    private final Application applicationContext;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private Runnable showNotificationRunnable = this::showNotification;

    @SuppressLint("StaticFieldLeak")
    private volatile static KCPhantomNotificationManager instance;

    private KCNativeAdView adView;

    private KCPhantomNotificationManager(Context context, String adPlacement) {
        this.applicationContext = (Application) context.getApplicationContext();
        this.adPlacement = adPlacement;

        registerBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        this.applicationContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handler.post(KCPhantomNotificationManager.this::scheduleNotifications);
            }
        }, new IntentFilter(Intent.ACTION_USER_PRESENT));
    }

    private void scheduleNotifications() {
        handler.removeCallbacks(showNotificationRunnable);

        handler.postDelayed(showNotificationRunnable, 2000);
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
        if (adView != null) {
            return;
        }

        View adLayoutView = View.inflate(applicationContext, R.layout.layout_ad_phantom, null);
        adView = new KCNativeAdView(applicationContext);
        adView.setAdLayoutView(adLayoutView);
        adView.load(adPlacement);
        adView.setOnAdLoadedListener((adView)->{
            if (!isKeyguardLocked()) {
                Intent intent = new Intent(applicationContext, PhantomNotificationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                applicationContext.startActivity(intent);
            }
        });
        adView.setOnAdFailListener((adView)->{
            scheduleNotifications();
        });
    }

    private boolean isKeyguardLocked() {
        KeyguardManager keyguardManager = (KeyguardManager) applicationContext.getSystemService(Context.KEYGUARD_SERVICE);
        boolean isLocked = false;
        if (keyguardManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            isLocked = keyguardManager.isKeyguardLocked();
        }
        return isLocked;
    }

    protected View obtainAdView() {
        View view = adView;
        adView = null;
        return view;
    }

    protected void dropAdView(View view) {
        if (view != null && view instanceof AcbExpressAdView) {
            ((AcbExpressAdView)view).destroy();
        }

    }
}
