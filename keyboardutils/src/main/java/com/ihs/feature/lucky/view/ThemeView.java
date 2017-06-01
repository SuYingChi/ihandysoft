package com.ihs.feature.lucky.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.feature.common.AnimatorListenerAdapter;
import com.ihs.feature.common.ViewUtils;
import com.ihs.feature.lucky.LuckyPreloadManager;
import com.ihs.feature.lucky.ThemeBean;
import com.ihs.keyboardutils.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;


public class ThemeView extends FlyAwardBaseView implements View.OnClickListener {

    private View mContainer;
    private ImageView mBanner;
    private TextView mTitle;
    private TextView mBody;
    private ThemeBean themeItem;

    public ThemeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mContainer = ViewUtils.findViewById(this, R.id.lucky_game_theme_container);
        mIcon = ViewUtils.findViewById(this, R.id.lucky_game_theme_icon);
        mBanner = ViewUtils.findViewById(this, R.id.lucky_game_theme_image_container);
        mTitle = ViewUtils.findViewById(this, R.id.lucky_game_theme_title);
        mBody = ViewUtils.findViewById(this, R.id.lucky_game_theme_body);
        mBody.setAlpha(0.5f);
        mDragIcon = ViewUtils.findViewById(this, R.id.lucky_game_drag_theme_icon);
        View install = ViewUtils.findViewById(this, R.id.lucky_game_theme_action);
        install.setOnClickListener(this);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0, getContext().getResources().getDimensionPixelSize(R.dimen.lucky_award_ad_view_container_top_margin), 0, 0);
        setLayoutParams(layoutParams);

        calculateAnimationDistance(ThemeView.this);
    }

    public AnimatorSet getThemeAnimation() {
        ObjectAnimator holdOn = ObjectAnimator.ofFloat(mDragIcon, "alpha", 1.0f, 1.0f);
        holdOn.setDuration(QUESTION_MARK_HOLD_ON_DURATION);
        holdOn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                File themeIcon = new File(LuckyPreloadManager.getDirectory(themeItem.getThemeName()),  ThemeBean.ICON);
                ImageLoader.getInstance().displayImage(
                        Uri.fromFile(themeIcon).toString(),
                        mDragIcon);
            }
        });

        ObjectAnimator fadeIn = fadeIn(this);
        fadeIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mContainer.setVisibility(VISIBLE);
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(dragUp(), holdOn, flipToIcon(), fadeIn);

        return set;
    }

    public boolean fetchTheme() {
        themeItem = LuckyPreloadManager.getInstance().getThemeInfo();
        if (themeItem == null) {
            return false;
        }

        File theme = new File(LuckyPreloadManager.getDirectory(themeItem.getThemeName()), ThemeBean.BANNER);
        ImageLoader.getInstance().displayImage(
                Uri.fromFile(theme).toString(),
                mBanner);
        mTitle.setText(themeItem.getThemeName());
        return true;
    }

    public void resetVisible() {
        mContainer.setVisibility(INVISIBLE);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.lucky_game_theme_action) {
            //// TODO: 17/6/1 获取theme
//            ThemeDownloadManager.getInstance().downloadTheme(themeItem);
            HSAnalytics.logEvent("Lucky_Award_Theme_Install_Clicked");
        }
    }
}
