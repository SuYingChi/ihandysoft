package com.artw.lockscreen;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;

import com.artw.lockscreen.common.LockerChargingScreenUtils;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.view.SearchEditTextView;

/**
 * Created by yanxia on 2017/12/12.
 */

public class PremiumLockerSearchActivity extends AppCompatActivity {

    private SearchEditTextView searchEditTextView;
    private boolean animationPlayed;
    private AlphaAnimation alphaAnimation;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        if (!LockerChargingScreenUtils.isNativeLollipop()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        if (!LockerUtils.isKeyguardSecure(this)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
        setContentView(R.layout.activity_premium_locker_search);
        View rootView = findViewById(R.id.search_root_view);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doHideAnimation();
            }
        });
        searchEditTextView = findViewById(R.id.search_view);
        searchEditTextView.setSearchButtonClickListener(new SearchEditTextView.OnSearchButtonClickListener() {
            @Override
            public void onSearchButtonClick(String searchText) {
                HSLog.d("searchText: " + searchText);
                if (TextUtils.isEmpty(searchText)) {
                    return;
                }
                String url = WebContentSearchManager.getInstance().queryText(searchText);
                SearchIntentReceiver.sendSearchIntent(url);
            }
        });
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (!animationPlayed) {
            doAnimation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alphaAnimation != null) {
            alphaAnimation.cancel();
        }
    }

    private void doAnimation() {
        if (alphaAnimation == null) {
            alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
            alphaAnimation.setDuration(500);
            alphaAnimation.setFillAfter(true);
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    searchEditTextView.setVisibility(View.VISIBLE);
                    animationPlayed = true;
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
        searchEditTextView.startAnimation(alphaAnimation);
    }

    private void doHideAnimation() {
        AlphaAnimation hideAnimation = new AlphaAnimation(1.0f, 0.0f);
        hideAnimation.setDuration(500);
        hideAnimation.setFillAfter(true);
        hideAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        searchEditTextView.startAnimation(hideAnimation);

    }
}
