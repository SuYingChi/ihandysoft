package com.artw.lockscreen;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;

import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.view.SearchEditTextView;

/**
 * Created by yanxia on 2017/12/15.
 */

public class PremiumSearchDialog extends Dialog {

    private View rootView;
    private SearchEditTextView searchEditTextView;
    private AlphaAnimation alphaAnimation;

    public PremiumSearchDialog(@NonNull Context context) {
        this(context, R.style.SearchDialogTheme);
    }

    public PremiumSearchDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    private void init() {
        rootView = View.inflate(getContext(), R.layout.activity_premium_locker_search, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(rootView);
        searchEditTextView = findViewById(R.id.search_view);
        searchEditTextView.setSearchButtonClickListener(new SearchEditTextView.OnSearchButtonClickListener() {
            @Override
            public void onSearchButtonClick(String searchText) {
                HSLog.d("searchText: " + searchText);
                if (TextUtils.isEmpty(searchText)) {
                    return;
                }
                String url = WebContentSearchManager.getInstance().queryText(searchText);
                Intent intent = new Intent(getContext(), BrowserActivity.class);
                intent.putExtra(BrowserActivity.SEARCH_URL_EXTRA, url);
                getContext().startActivity(intent);
            }
        });
        View rootView = findViewById(R.id.search_root_view);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doHideAnimation();
            }
        });
    }

    @Override
    public void show() {
        super.show();
        Window window = getWindow();
        if (window != null) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }
        doShowAnimation();
    }

    private void doShowAnimation() {
        if (alphaAnimation == null) {
            alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
            alphaAnimation.setDuration(500);
            alphaAnimation.setFillAfter(true);
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    searchEditTextView.setVisibility(View.VISIBLE);
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
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dismiss();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        searchEditTextView.startAnimation(hideAnimation);

    }
}
