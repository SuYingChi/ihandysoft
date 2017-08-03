package com.ihs.keyboardutils.giftad;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.acb.adadapter.AcbAd;
import com.acb.adadapter.AcbNativeAd;
import com.acb.adadapter.ContainerView.AcbNativeAdContainerView;
import com.acb.adadapter.ContainerView.AcbNativeAdIconView;
import com.acb.adadapter.ContainerView.AcbNativeAdPrimaryView;
import com.acb.nativeads.AcbNativeAdLoader;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.OnCompositionLoadedListener;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.CommonUtils;
import com.ihs.keyboardutils.utils.InterstitialGiftUtils;
import com.ihs.keyboardutils.utils.LauncherAnimationUtils;
import com.ihs.keyboardutils.utils.ToastUtils;

import java.util.List;

public class InterstitialGiftActivity extends Activity {
    public final static String PLACEMENT_MESSAGE = "PLACEMENT.MESSAGE";

    // Interstitial ad.
    private final static int TYPE_ANIMATION = 0;
    private final static int TYPE_DISPLAY_AD = 1;
    private final static int TYPE_REFRESH_AD = 2;

    private final static float INTERSTITIAL_AD_ANIMATION_NODE_ONE = 0f;
    private final static float INTERSTITIAL_AD_ANIMATION_NODE_TWO = 0.1750f;
    private final static float INTERSTITIAL_AD_ANIMATION_NODE_THREE = 0.7000f;
    private final static float INTERSTITIAL_AD_ANIMATION_NODE_FOUR = 1f;

    private final static int MAX_INTERSTITIAL_AD_ANIM_COUNT = 2;
    private int mInterstitialAdAnimDisplayedCount = 1;

    private boolean mIsInterstitialAdRefreshClicked = false;
    private boolean isGiftAnimationPlayed = false;
    private float mInterstitialAdTransDistance;

    private String placementName;
    private AnimatorSet mInterstitialAdResultAnimator = null;
    private ViewGroup mInterstitialAdWindowTitle;
    private ViewGroup mInterstitialAdContainer;
    private ViewGroup mInterstitialAdAnimContent;
    private AcbNativeAdContainerView mInterstitialAdDisplayContent;
    private LinearLayout mInterstitialAdWindow;
    private LinearLayout mInterstitialAdRefreshContent;
    private LottieAnimationView mLottieAnimationView;
    private ValueAnimator mInterstitialAdAnimator = null;
    private AcbNativeAd mAd = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interstitial_gift);
        Intent intent = getIntent();
        placementName = intent.getStringExtra(PLACEMENT_MESSAGE);
        initInterstitialAd();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isGiftAnimationPlayed) {
            showInterstitialAd();
            isGiftAnimationPlayed = true;
        }
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key.  The default implementation simply finishes the current activity,
     * but you can override this to do whatever you want.
     */
    @Override
    public void onBackPressed() {
        hideInterstitialAdView();
    }

    private void showInterstitialAd() {
        // request ad
        AcbNativeAdLoader loader = new AcbNativeAdLoader(HSApplication.getContext(), placementName);
        loader.load(1, null);
        if (mInterstitialAdContainer == null) {
            initInterstitialAd();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (InterstitialGiftUtils.isNetworkAvailable(-1)) {
                    setInterstitialAdViewVisibility(TYPE_ANIMATION);
                    doInterstitialAdAnimation(INTERSTITIAL_AD_ANIMATION_NODE_ONE, INTERSTITIAL_AD_ANIMATION_NODE_TWO);
                } else {
                    setInterstitialAdViewVisibility(TYPE_REFRESH_AD);
                    doInterstitialAdDisplayAnimation(TYPE_REFRESH_AD, false);
                }
            }
        }, 200);
    }

    private void initInterstitialAd() {
        LayoutInflater inflater = LayoutInflater.from(HSApplication.getContext());
        // adWindow
        mInterstitialAdWindow = (LinearLayout) findViewById(R.id.all_apps_interstitial_ad_window);
        mInterstitialAdWindowTitle = (RelativeLayout) findViewById(R.id.all_apps_interstitial_ad_window_title);
        mInterstitialAdWindowTitle.setVisibility(View.GONE);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0, InterstitialGiftUtils.getStatusBarHeight(HSApplication.getContext()), 0, CommonUtils.getNavigationBarHeight(HSApplication.getContext()));
        mInterstitialAdWindow.setLayoutParams(layoutParams);

        RelativeLayout dismissButton = (RelativeLayout) findViewById(R.id.all_apps_interstitial_ad_dismiss_button);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideInterstitialAdView();
            }
        });

        int cameraDistance = CommonUtils.getPhoneHeight(HSApplication.getContext()) * 8;

        // display content.
        mInterstitialAdContainer = (FrameLayout) findViewById(R.id.all_apps_Interstitial_ad_container);
        View containerView = inflater.inflate(R.layout.all_apps_interstitial_ad_display_content, mInterstitialAdContainer, false);
        mInterstitialAdDisplayContent = new AcbNativeAdContainerView(HSApplication.getContext());
        mInterstitialAdDisplayContent.addContentView(containerView);

        mInterstitialAdDisplayContent.setAdPrimaryView((AcbNativeAdPrimaryView) containerView.findViewById(R.id.all_apps_interstitial_ad_primary_container));
        mInterstitialAdDisplayContent.setAdChoiceView((ViewGroup) containerView.findViewById(R.id.all_apps_interstitial_ad_choice));
        mInterstitialAdDisplayContent.setAdIconView((AcbNativeAdIconView) containerView.findViewById(R.id.all_apps_interstitial_ad_icon));
        mInterstitialAdDisplayContent.setAdTitleView((TextView) containerView.findViewById(R.id.all_apps_interstitial_ad_title));
        mInterstitialAdDisplayContent.setAdBodyView((TextView) containerView.findViewById(R.id.all_apps_interstitial_ad_description));
        mInterstitialAdDisplayContent.setAdActionView(containerView.findViewById(R.id.all_apps_interstitial_ad_action));
        mInterstitialAdDisplayContent.setCameraDistance(cameraDistance);

        // refresh content.
        mInterstitialAdRefreshContent = (LinearLayout) inflater.inflate(R.layout.all_apps_interstitial_ad_refresh_content, null);
        mInterstitialAdRefreshContent.setVisibility(View.GONE);
        mInterstitialAdRefreshContent.setCameraDistance(cameraDistance);

        ImageView refreshButton = (ImageView) mInterstitialAdRefreshContent.findViewById(R.id.all_apps_interstitial_ad_refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (InterstitialGiftUtils.isNetworkAvailable(-1)) {
                    mIsInterstitialAdRefreshClicked = true;
                    showInterstitialAd();
                } else {
                    HSAnalytics.logEvent("AppDrawer_Gift_AdsPage_Refresh_Clicked", "type", "nonetwork");
                    ToastUtils.showToast(R.string.locker_wallpaper_network_error, Toast.LENGTH_LONG);
                }
            }
        });

        // animation content.
        mInterstitialAdAnimContent = (FrameLayout) inflater.inflate(R.layout.all_apps_interstitial_ad_anim_content, null);
        mInterstitialAdAnimContent.setVisibility(View.GONE);
        mInterstitialAdAnimContent.setCameraDistance(cameraDistance);

        mLottieAnimationView = (LottieAnimationView) mInterstitialAdAnimContent.findViewById(R.id.all_apps_interstitial_ad_anim_view);
        LottieComposition.Factory.fromAssetFileName(HSApplication.getContext(), "all_apps_interstitial_ad.json", new OnCompositionLoadedListener() {
            @Override
            public void onCompositionLoaded(LottieComposition lottieComposition) {
                mLottieAnimationView.setComposition(lottieComposition);
                mLottieAnimationView.setProgress(0f);
            }
        });
        setInterstitialAdDisplayPosition();

        // add to window.
        mInterstitialAdContainer.addView(mInterstitialAdAnimContent);
        mInterstitialAdContainer.addView(mInterstitialAdRefreshContent);
        mInterstitialAdContainer.addView(mInterstitialAdDisplayContent);
        mInterstitialAdContainer.setVisibility(View.VISIBLE);
        mInterstitialAdWindow.setVisibility(View.GONE);
    }

    private void doInterstitialAdDisplayAnimation(int type, boolean animated) {
        switch (type) {
            case TYPE_DISPLAY_AD:
                showResultAnimation(animated, mInterstitialAdDisplayContent, mInterstitialAdWindowTitle,
                        mInterstitialAdDisplayContent.getAdIconView(), mInterstitialAdDisplayContent.getAdTitleView(),
                        mInterstitialAdDisplayContent.getAdBodyView(), mInterstitialAdDisplayContent.getAdActionView());
                break;
            case TYPE_REFRESH_AD:
                showResultAnimation(animated, mInterstitialAdRefreshContent, mInterstitialAdWindowTitle,
                        mInterstitialAdRefreshContent.findViewById(R.id.all_apps_interstitial_ad_refresh_button));
                break;
            default:
                break;
        }
    }

    private void showResultAnimation(boolean animated, @NonNull final View target, final View title, final View... views) {
        if (views != null && views.length != 0) {
            if (animated) {
                for (View view : views) {
                    view.setVisibility(View.INVISIBLE);
                    view.setAlpha(0f);
                }

                ObjectAnimator lottieRotation = ObjectAnimator.ofFloat(mInterstitialAdAnimContent, "rotationY", 0f, 90f);
                lottieRotation.addListener(new com.ihs.keyboardutils.giftad.AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!mCancelled) {
                            mInterstitialAdAnimContent.setVisibility(View.GONE);
                            mInterstitialAdAnimContent.setRotationY(0f);
                            title.setVisibility(View.INVISIBLE);
                            target.setRotationY(270f);
                            target.setVisibility(View.VISIBLE);
                        }
                    }
                });
                lottieRotation.setDuration(200);

                float endPosition;
                if (target instanceof AcbNativeAdContainerView) {
                    endPosition = mInterstitialAdDisplayContent.findViewById(R.id.all_apps_interstitial_ad_image_container)
                            .getTranslationY();
                    mInterstitialAdDisplayContent.findViewById(R.id.all_apps_interstitial_ad_image_container)
                            .setTranslationY(endPosition - mInterstitialAdTransDistance);
                } else {
                    endPosition = mInterstitialAdRefreshContent.findViewById(R.id.all_apps_interstitial_no_ad_view)
                            .getTranslationY();
                    mInterstitialAdRefreshContent.findViewById(R.id.all_apps_interstitial_no_ad_view)
                            .setTranslationY(endPosition - mInterstitialAdTransDistance);
                }
                final float startPosition = endPosition - mInterstitialAdTransDistance;

                ObjectAnimator adPrimaryRotation = ObjectAnimator.ofFloat(target, "rotationY", 270f, 360f);
                adPrimaryRotation.setDuration(200);

                ValueAnimator fadeIn = ValueAnimator.ofFloat(0f, 1f);
                fadeIn.addListener(new android.animation.AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        for (View view : views) {
                            view.setVisibility(View.VISIBLE);
                        }
                        title.setVisibility(View.VISIBLE);
                    }
                });
                fadeIn.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        for (View view : views) {
                            view.setAlpha(animation.getAnimatedFraction());
                        }
                        title.setAlpha(animation.getAnimatedFraction());
                    }
                });
                fadeIn.setDuration(400);

                ValueAnimator primaryTranslation = ValueAnimator.ofFloat(0f, 1f);

                if (target instanceof AcbNativeAdContainerView) {
                    primaryTranslation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            mInterstitialAdDisplayContent.findViewById(R.id.all_apps_interstitial_ad_image_container)
                                    .setTranslationY(startPosition + mInterstitialAdTransDistance * valueAnimator.getAnimatedFraction());
                        }
                    });

                    primaryTranslation.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ((RevealFlashButton) (mInterstitialAdDisplayContent).getAdActionView()).flash();
                        }
                    });
                } else {
                    primaryTranslation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            mInterstitialAdRefreshContent.findViewById(R.id.all_apps_interstitial_no_ad_view)
                                    .setTranslationY(startPosition + mInterstitialAdTransDistance * valueAnimator.getAnimatedFraction());

                        }
                    });
                }
                primaryTranslation.setDuration(400);

                AnimatorSet display = new AnimatorSet();
                display.setInterpolator(LauncherAnimationUtils.linearInterpolator);
                display.playTogether(fadeIn, primaryTranslation);

                mInterstitialAdResultAnimator = new AnimatorSet();
                mInterstitialAdResultAnimator.playSequentially(lottieRotation, adPrimaryRotation, display);
                mInterstitialAdResultAnimator.setInterpolator(LauncherAnimationUtils.linearInterpolator);
                mInterstitialAdResultAnimator.start();

            } else {
                mInterstitialAdAnimContent.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                if (target instanceof AcbNativeAdContainerView) {
                    mInterstitialAdDisplayContent.setVisibility(View.VISIBLE);
                } else {
                    mInterstitialAdRefreshContent.setVisibility(View.VISIBLE);
                }
                for (View view : views) {
                    view.setVisibility(View.VISIBLE);
                }
            }
        }

    }

    private void setInterstitialAdDisplayPosition() {
        mInterstitialAdContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int animHeight = mLottieAnimationView.getHeight();
                if (animHeight <= 0) {
                    return;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mInterstitialAdContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mInterstitialAdContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                mInterstitialAdTransDistance = -120f * animHeight / 1920f;
            }
        });
    }

    private void doInterstitialAdAnimation(final float from, final float to) {
        if (mLottieAnimationView == null || mLottieAnimationView.getVisibility() != View.VISIBLE) {
            return;
        }
        final float total = to - from;
        mInterstitialAdAnimator = ValueAnimator.ofFloat(from, to);
        mInterstitialAdAnimator.setDuration((int) (5000 * total * 1.0f));
        mInterstitialAdAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = from + total * animation.getAnimatedFraction();
                mLottieAnimationView.setProgress(progress);
            }
        });

        mInterstitialAdAnimator.addListener(new com.ihs.keyboardutils.giftad.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!mCancelled) {
                    if (floatEquals(to, INTERSTITIAL_AD_ANIMATION_NODE_TWO)
                            || floatEquals(to, INTERSTITIAL_AD_ANIMATION_NODE_THREE)) {
                        List<AcbNativeAd> ads = AcbNativeAdLoader.fetch(HSApplication.getContext(), placementName, 1);
                        mAd = ads.isEmpty() ? null : ads.get(0);
                        if (mAd == null && mInterstitialAdAnimDisplayedCount++ <= MAX_INTERSTITIAL_AD_ANIM_COUNT) {
                            doInterstitialAdAnimation(INTERSTITIAL_AD_ANIMATION_NODE_TWO, INTERSTITIAL_AD_ANIMATION_NODE_THREE);
                        } else {
                            doInterstitialAdAnimation(to, INTERSTITIAL_AD_ANIMATION_NODE_FOUR);
                        }
                    } else if (floatEquals(to, INTERSTITIAL_AD_ANIMATION_NODE_FOUR)) {
                        if (mAd != null) {
                            setInterstitialAdViewVisibility(TYPE_DISPLAY_AD);
                            doInterstitialAdDisplayAnimation(TYPE_DISPLAY_AD, true);
                        } else {
                            setInterstitialAdViewVisibility(TYPE_REFRESH_AD);
                            doInterstitialAdDisplayAnimation(TYPE_REFRESH_AD, true);
                        }
                        mInterstitialAdAnimDisplayedCount = 1;
                        if (mIsInterstitialAdRefreshClicked) {
                            HSAnalytics.logEvent("AppDrawer_Gift_AdsPage_Refresh_Clicked", "type", mAd != null ? "yes" : "no");
                            mIsInterstitialAdRefreshClicked = false;
                        }
                    }
                }
            }
        });
        mInterstitialAdAnimator.setInterpolator(LauncherAnimationUtils.linearInterpolator);
        mInterstitialAdAnimator.start();

    }

    private void setInterstitialAdViewVisibility(int type) {
        mInterstitialAdWindow.setVisibility(View.VISIBLE);

        switch (type) {
            case TYPE_ANIMATION:
                mInterstitialAdDisplayContent.setVisibility(View.GONE);
                mInterstitialAdRefreshContent.setVisibility(View.GONE);
                mInterstitialAdWindowTitle.setVisibility(View.GONE);
                mInterstitialAdAnimContent.setVisibility(View.VISIBLE);
                break;
            case TYPE_DISPLAY_AD:
                mInterstitialAdRefreshContent.setVisibility(View.GONE);
                setupInterstitialAd();
                break;
            case TYPE_REFRESH_AD:
                mInterstitialAdDisplayContent.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    private void hideInterstitialAdView() {
        mInterstitialAdWindow.setVisibility(View.GONE);
        mInterstitialAdDisplayContent.setVisibility(View.GONE);
        mInterstitialAdRefreshContent.setVisibility(View.GONE);
        mInterstitialAdWindowTitle.setVisibility(View.GONE);

        if (mAd != null) {
            mAd.release();
            mAd = null;
        }
        if (mInterstitialAdAnimator != null) {
            mInterstitialAdAnimator.cancel();
        }
        if (mInterstitialAdResultAnimator != null) {
            mInterstitialAdResultAnimator.cancel();
        }
        this.finish();
    }

    private void setupInterstitialAd() {
        if (mAd != null) {
            mAd.setNativeClickListener(new AcbNativeAd.AcbNativeClickListener() {
                @Override
                public void onAdClick(AcbAd acbAd) {
                    HSAnalytics.logEvent("AppDrawer_Gift_Ads_Clicked");
                }
            });
            mInterstitialAdDisplayContent.fillNativeAd(mAd);
        }
    }

    private boolean floatEquals(float f1, float f2) {
        return Math.abs(f1 - f2) < 0.00001f;
    }
}
