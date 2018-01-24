package com.ihs.feature.softgame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;

import com.ihs.app.framework.HSApplication;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.ads.KCInterstitialAd;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.kc.utils.KCAnalytics;

import net.appcloudbox.ads.interstitialads.AcbInterstitialAdLoader;

/**
 * Created by Arthur on 17/12/6.
 */

public class GameStarterActivity extends Activity {
    public static final String SHOW_WHEN_LOCKED = "show_when_locked";
    public static final String FULL_SCREEN_AD_PLACEMENT = "full_screen_ad_placement";
    private static final int TIME_OUT_LIMIT = 5000;
    private Handler handler = new Handler();
    private boolean adShowed = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        boolean showWhenLocked = getIntent().getBooleanExtra(SHOW_WHEN_LOCKED, false);
        if (showWhenLocked) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }

        final String fullscreenPlacement = getIntent().getStringExtra(FULL_SCREEN_AD_PLACEMENT);
        if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased() && !TextUtils.isEmpty(fullscreenPlacement)) {
            KCInterstitialAd.load(fullscreenPlacement);
            AcbInterstitialAdLoader loader = KCInterstitialAd.loadAndShow(fullscreenPlacement, "", "", new KCInterstitialAd.OnAdShowListener() {
                @Override
                public void onAdShow(boolean b) {
                    adShowed = b;
                }
            }, null);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!adShowed) {
                        loader.cancel();
                    }
                    KCInterstitialAd.load(fullscreenPlacement);
                }
            }, TIME_OUT_LIMIT);

        }

        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Intent intent = getIntent();//getIntent将该项目中包含的原始intent检索出来，将检索出来的intent赋值给一个Intent类型的变量intent
        String url = intent.getStringExtra("url");
        if (android.text.TextUtils.isEmpty(url)) {
            finish();
            return;
        }

        Intent gameIntent = new Intent(this, GameActivity.class);
        gameIntent.putExtra("url", url);
        startActivityForResult(gameIntent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        handler.removeCallbacksAndMessages(null);
        KCInterstitialAd.show(getResources().getString(R.string.placement_full_screen_game), "", "");
        finish();
    }

    public static void startGame(String gameUrl, String callFrom, String placement) {
        startGame(gameUrl, callFrom, placement, "", false);
    }

    public static void startGame(String gameUrl, String callFrom, String placement, String gameName, boolean showWhenLocked) {
        Intent intent = new Intent(HSApplication.getContext(), GameStarterActivity.class);
        intent.putExtra("url", gameUrl);
        intent.putExtra(SHOW_WHEN_LOCKED, showWhenLocked);
        intent.putExtra(FULL_SCREEN_AD_PLACEMENT, placement);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        HSApplication.getContext().startActivity(intent);
        if (TextUtils.isEmpty(gameName)) {
            KCAnalytics.logEvent(callFrom);
        } else {
            KCAnalytics.logEvent(callFrom, callFrom, gameName);
        }
    }
}
