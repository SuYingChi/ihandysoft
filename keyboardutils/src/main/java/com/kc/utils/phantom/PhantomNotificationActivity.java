package com.kc.utils.phantom;

import android.animation.Animator;
import android.app.Service;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.ihs.commons.config.HSConfig;
import com.ihs.keyboardutils.R;

public class PhantomNotificationActivity extends AppCompatActivity {

    private static final long ENTER_ANIMATION_DURATION = 500;
    private static final long EXIT_ANIMATION_DURATION = 500;
    private static final int DEFAULT_DISPLAY_SECONDS = 2;

    private boolean animationStarted = false;
    private FrameLayout adBannerContainer;

    private View adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phantom);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        adBannerContainer = findViewById(R.id.ad_banner_container);
        adView = KCPhantomNotificationManager.instance().obtainAdView();

        if (adView == null) {
            finish();
        } else {
            adView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            adBannerContainer.addView(adView);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        KCPhantomNotificationManager.instance().dropAdView(adView);
        adView = null;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus && !animationStarted) {
            animationStarted = true;

            // vibrate
            if (HSConfig.optBoolean(false, "Application", "Phantom", "VibrationEnabled")) {
                try {
                    Vibrator vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
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
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();
                } catch (Exception e) {
                    // Ignore exception
                }
            }

            long displayDuration = HSConfig.optInteger(DEFAULT_DISPLAY_SECONDS, "Application", "Phantom", "DisplaySeconds") * 1000L;

            // banner
            adBannerContainer.animate().
                    translationY(adBannerContainer.getHeight()).
                    setDuration(ENTER_ANIMATION_DURATION).
                    start();
            adBannerContainer.postDelayed(()-> {
                adBannerContainer.animate().
                        translationY(-adBannerContainer.getHeight()).
                        setDuration(EXIT_ANIMATION_DURATION).setListener(exitAnimatorListener).
                        start();
            }, ENTER_ANIMATION_DURATION + displayDuration);
        }
    }

    private Animator.AnimatorListener exitAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            finish();
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };
}
