package com.ihs.feature.resultpage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.acb.adadapter.AcbNativeAd;
import com.acb.adadapter.ContainerView.AcbNativeAdContainerView;
import com.acb.adadapter.ContainerView.AcbNativeAdIconView;
import com.acb.adadapter.ContainerView.AcbNativeAdPrimaryView;
import com.artw.lockscreen.LockerSettings;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.chargingscreen.utils.ChargingPrefsUtil;
import com.ihs.commons.utils.HSLog;
import com.ihs.feature.common.LauncherAnimUtils;
import com.ihs.feature.common.Thunk;
import com.ihs.feature.common.ViewUtils;
import com.ihs.feature.resultpage.data.CardData;
import com.ihs.feature.resultpage.data.ResultConstants;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.giftad.RevealFlashButton;
import com.ihs.keyboardutils.utils.CommonUtils;
import com.ihs.keyboardutils.utils.LauncherAnimationUtils;
import com.ihs.keyboardutils.utils.ToastUtils;

import java.util.List;

import static com.flurry.sdk.bb.D;
import static com.flurry.sdk.bb.P;
import static com.ihs.feature.resultpage.ResultController.Type.AD;

@SuppressWarnings("WeakerAccess")
abstract class ResultController implements View.OnClickListener {

    protected static final String TAG = "ResultController";
    static final long FRAME = 100;
    static final long FRAME_HALF = 50;

    static final long DURATION_BALL_TRANSLATE = 7 * FRAME_HALF;
    private static final long START_DELAY_CARDS = FRAME / 10 * 86;
    @Thunk
    static final long DURATION_CARD_TRANSLATE = 8 * FRAME;
    @Thunk
    static final long DURATION_BG_TRANSLATE = 7 * FRAME;

    // Ad / charging screen
    private static final long START_DELAY_AD_OR_CHARGING_SCREEN = 16 * FRAME;
    @Thunk
    static final long START_DELAY_BUTTON_REVEAL = 3 * FRAME;
    @Thunk
    static final long START_DELAY_BUTTON_FLASH = 23 * FRAME;
    @Thunk
    static final float TRANSLATION_MULTIPLIER_SHADOW_1 = 1.4f;
    @Thunk
    static final float TRANSLATION_MULTIPLIER_SHADOW_2 = 1.2f;
    @Thunk
    static final float TRANSLATION_MULTIPLIER_DESCRIPTION = 1.25f;
    @Thunk
    static final long DURATION_SLIDE_IN = 800;

    protected ResultPageActivity mActivity;
    int mScreenHeight;
    int mResultType;
    Type mType = AD;

    private FrameLayout mTransitionView;
    private FrameLayout mAdOrChargingScreenContainerView;
    private RecyclerView mCardRecyclerView;
    @Thunk
    View mResultView;
    private View mBgView;
    private View mHeaderTagView;

    // Ad or charging screen
    private View mImageFrameShadow1;
    private View mImageFrameShadow2;
    private AcbNativeAdContainerView mAdContainer;
    private AcbNativeAdPrimaryView mAdImageContainer;
    private FrameLayout mChargingScreenImageContainer;
    private ImageView mImageIv;
    private ViewGroup mAdChoice;
    private AcbNativeAdIconView mAdIconView;
    private TextView mTitleTv;
    private TextView mDescriptionTv;
    private RevealFlashButton mActionBtn;

    private AcbNativeAd mAd;
    private List<CardData> mCardDataList;

    enum Type {
        AD,
        CHARGE_SCREEN,
        NOTIFICATION_CLEANER,
        CARD_VIEW,
    }

    ResultController() {
    }

    ResultController(ResultPageActivity activity, int resultType, Type type, @Nullable AcbNativeAd ad, List<CardData> cardDataList) {
        init(activity, resultType, type, ad, cardDataList);
    }

    protected void init(ResultPageActivity activity, int resultType, Type type, @Nullable AcbNativeAd ad, List<CardData> cardDataList) {
        HSLog.d(TAG, "ResultController init *** resultType = " + resultType + " type = " + type);
        mActivity = activity;
        mType = type;
        logViewEvent(type);

        mCardDataList = cardDataList;
        mResultType = resultType;
        mAd = ad;
        mScreenHeight = CommonUtils.getPhoneHeight(activity);

        LayoutInflater layoutInflater = LayoutInflater.from(activity);

        mBgView = ViewUtils.findViewById(activity, R.id.bg_view);

        mHeaderTagView = ViewUtils.findViewById(activity, R.id.result_header_tag_view);

        mTransitionView = ViewUtils.findViewById(activity, R.id.transition_view_container);
        if (null != mTransitionView) {
            mTransitionView.removeAllViews();
            layoutInflater.inflate(getLayoutId(), mTransitionView, true);
            onFinishInflateTransitionView(mTransitionView);
        }

        switch (type) {
            case AD:
            case CHARGE_SCREEN:
            case NOTIFICATION_CLEANER:
            case CARD_VIEW:
                initAdOrChargingScreenView(activity, layoutInflater);
                break;
//            case CARD_VIEW:
//
//                initCardView(activity, layoutInflater, type);
//                break;
        }
        mResultView = ViewUtils.findViewById(activity, R.id.result_view);
    }

    private void initAdOrChargingScreenView(Activity activity, LayoutInflater layoutInflater) {
        HSLog.d(TAG, "initAdOrChargingScreenView");
        FrameLayout container = ViewUtils.findViewById(activity, R.id.ad_or_charging_screen_view_container);
        if (null != container) {
            int layoutId = R.layout.result_page_card_ad_or_charging_screen;
            View resultContentView = layoutInflater.inflate(layoutId, mAdOrChargingScreenContainerView, false);
            mAdOrChargingScreenContainerView = container;
            onFinishInflateResultView(resultContentView);
            initActionButton(activity);
            mAdOrChargingScreenContainerView.setVisibility(View.VISIBLE);
            if (null != mCardRecyclerView) {
                mCardRecyclerView.setVisibility(View.GONE);
            }
        }
    }

    private void initCardView(Activity activity, LayoutInflater layoutInflater, Type type) {
        HSLog.d(TAG, "initCardView type = " + type);
        mCardRecyclerView = ViewUtils.findViewById(activity, R.id.result_card_recycler_view);
        mCardRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mCardRecyclerView.setHasFixedSize(true);
        ResultListAdapter resultListAdapter = new ResultListAdapter(mActivity, mResultType, mCardDataList);
        mCardRecyclerView.setAdapter(resultListAdapter);
        initActionButton(activity);
        mCardRecyclerView.setVisibility(View.VISIBLE);
        if (null != mAdOrChargingScreenContainerView) {
            mAdOrChargingScreenContainerView.setVisibility(View.GONE);
        }
    }

    private void initActionButton(Context context) {
        if (null != mActionBtn) {
            if (this instanceof BoostPlusResultController) {
                mActionBtn.setTextColor(ContextCompat.getColor(context, R.color.boost_plus_clean_bg));
            } else if (this instanceof BatteryResultController) {
                mActionBtn.setTextColor(ContextCompat.getColor(context, R.color.battery_green));
            } else if (this instanceof CpuCoolerResultController) {
                mActionBtn.setTextColor(ContextCompat.getColor(context, R.color.cpu_cooler_primary_blue));
            } else if (this instanceof JunkCleanResultController) {
                mActionBtn.setTextColor(ContextCompat.getColor(context, R.color.clean_primary_blue));
            }
        }
    }

    protected abstract int getLayoutId();

    protected abstract void onFinishInflateTransitionView(View transitionView);

    @SuppressLint("NewApi")
    protected void onFinishInflateResultView(View resultView) {
        HSLog.d(TAG, "onFinishInflateResultView mType = " + mType);
        Context context = getContext();
        VectorDrawableCompat imageFrame = null;

        if (mType == AD || mType == Type.CHARGE_SCREEN || mType == Type.NOTIFICATION_CLEANER) {
            mImageFrameShadow1 = ViewUtils.findViewById(resultView, R.id.result_image_iv_shadow_1);
            mImageFrameShadow2 = ViewUtils.findViewById(resultView, R.id.result_image_iv_shadow_2);
            mAdImageContainer = ViewUtils.findViewById(resultView, R.id.result_image_container_ad);
            mChargingScreenImageContainer = ViewUtils.findViewById(resultView, R.id.result_image_container_charging_screen);
            mImageIv = ViewUtils.findViewById(resultView, R.id.result_image_iv);
            mAdChoice = ViewUtils.findViewById(resultView, R.id.result_ad_choice);
            mAdIconView = ViewUtils.findViewById(resultView, R.id.result_ad_icon);
            mTitleTv = ViewUtils.findViewById(resultView, R.id.description_title_tv);
            mDescriptionTv = ViewUtils.findViewById(resultView, R.id.description_content_tv);
            mActionBtn = ViewUtils.findViewById(resultView, R.id.result_action_btn);

            imageFrame = VectorDrawableCompat.create(context.getResources(),
                    R.drawable.result_page_image_frame, context.getTheme());
            mImageFrameShadow1.setBackground(imageFrame);
            mImageFrameShadow2.setBackground(imageFrame);
        }

        if (ChargingPrefsUtil.getChargingEnableStates() == ChargingPrefsUtil.CHARGING_DEFAULT_ACTIVE) {
            mType = AD;
        }

        switch (mType) {
            case AD:
            case CARD_VIEW:
                mAdImageContainer.setBackground(imageFrame);
                AcbNativeAdContainerView adContainer = new AcbNativeAdContainerView(getContext());
                adContainer.addContentView(resultView);
                adContainer.setAdPrimaryView(mAdImageContainer);
                mChargingScreenImageContainer.setVisibility(View.INVISIBLE);
                adContainer.setAdChoiceView(mAdChoice);
                adContainer.setAdIconView(mAdIconView);
                adContainer.setAdTitleView(mTitleTv);
                adContainer.setAdBodyView(mDescriptionTv);
                adContainer.setAdActionView(mActionBtn);
                mAdOrChargingScreenContainerView.addView(adContainer);
                mAdContainer = adContainer;
                fillNativeAd(mAd);
                break;
            case CHARGE_SCREEN:
                mChargingScreenImageContainer.setBackground(imageFrame);
                mAdOrChargingScreenContainerView.addView(resultView);
                mAdImageContainer.setVisibility(View.INVISIBLE);
                mAdIconView.setVisibility(View.INVISIBLE);
                mImageIv.setImageResource(R.drawable.charging_screen_guide);
                mTitleTv.setText(R.string.result_page_card_battery_protection_title);

                String title = context.getString(R.string.result_page_card_battery_protection_description);
                int startIndex = 0;
                if (!TextUtils.isEmpty(title) && title.contains("(")) {
                    startIndex = title.indexOf("(");
                }
                if (0 == startIndex) {
                    mDescriptionTv.setText(title);
                } else {
                    SpannableString titleSpannableString = new SpannableString(title);
                    titleSpannableString.setSpan(new TextAppearanceSpan(mActivity, R.style.result_page_charging_content),
                            startIndex, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mDescriptionTv.setText(titleSpannableString);
                }
                mActionBtn.setText(R.string.result_page_card_battery_protection_btn);
                mActionBtn.setOnClickListener(this);
                break;
            case NOTIFICATION_CLEANER:
                mChargingScreenImageContainer.setBackground(imageFrame);
                mAdOrChargingScreenContainerView.addView(resultView);
                mAdImageContainer.setVisibility(View.INVISIBLE);
                mAdIconView.setVisibility(View.INVISIBLE);
                mImageIv.setImageResource(R.drawable.notification_cleaner_result_page_guide);
                mTitleTv.setText(R.string.notification_cleaner_title);
                mDescriptionTv.setText(R.string.notification_cleaner_clean_now_tip);
                mActionBtn.setText(R.string.result_page_card_notification_cleaner_open_now);
                mActionBtn.setOnClickListener(this);
                break;
        }
    }

    void fillNativeAd(AcbNativeAd ad) {
        if (mAdContainer != null) {
            mAdContainer.fillNativeAd(ad);
        }
    }

    protected abstract void onStartTransitionAnimation(View transitionView);

    protected void onFunctionCardViewShown() {
    }

    void startTransitionAnimation() {
        HSLog.d(TAG, "startTransitionAnimation mTransitionView = " + mTransitionView);
        if (null != mTransitionView) {
            onStartTransitionAnimation(mTransitionView);
            if (mType == AD || mType == Type.CHARGE_SCREEN || mType == Type.NOTIFICATION_CLEANER) {
                if (mResultType != ResultConstants.RESULT_TYPE_JUNK_CLEAN && mResultType != ResultConstants.RESULT_TYPE_CPU_COOLER
                        && mResultType != ResultConstants.RESULT_TYPE_NOTIFICATION_CLEANER) {
                    // animation self
                    startAdOrChargingScreenResultAnimation(START_DELAY_AD_OR_CHARGING_SCREEN);
                }
            } else {
                if (mResultType != ResultConstants.RESULT_TYPE_BOOST_PLUS && mResultType != ResultConstants.RESULT_TYPE_JUNK_CLEAN
                        && mResultType != ResultConstants.RESULT_TYPE_CPU_COOLER && mResultType != ResultConstants.RESULT_TYPE_NOTIFICATION_CLEANER) {
                    // animation self
                    startCardResultAnimation(START_DELAY_CARDS);
                }
            }
        }
    }

    public void startAdOrChargingScreenResultAnimation(long startDelay) {
        HSLog.d(TAG, "startAdOrChargingScreenResultAnimation startDelay = " + startDelay + " mResultView = " + mResultView);
        if (null != mResultView) {
            mResultView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    HSLog.d(TAG, "startAdOrChargingScreenResultAnimation run");
                    View imageContainer = (mType == AD) ? mAdImageContainer : mChargingScreenImageContainer;

                    int[] location = new int[2];
                    imageContainer.getLocationInWindow(location);
                    float baseTranslationY = mScreenHeight - location[1];

                    imageContainer.setTranslationY(baseTranslationY);
                    mAdIconView.setTranslationY(baseTranslationY);
                    mImageFrameShadow1.setTranslationY(baseTranslationY * TRANSLATION_MULTIPLIER_SHADOW_1);
                    mImageFrameShadow2.setTranslationY(baseTranslationY * TRANSLATION_MULTIPLIER_SHADOW_2);
                    mTitleTv.setTranslationY(baseTranslationY);
                    mDescriptionTv.setTranslationY(baseTranslationY * TRANSLATION_MULTIPLIER_DESCRIPTION);
                    mAdChoice.setAlpha(0f);

                    mResultView.setVisibility(View.VISIBLE);

                    View[] translatedViews = new View[]{
                            imageContainer, mAdIconView, mImageFrameShadow1,
                            mImageFrameShadow2, mTitleTv, mDescriptionTv
                    };
                    for (View v : translatedViews) {
                        v.animate()
                                .translationY(0f)
                                .setDuration(DURATION_SLIDE_IN)
                                .setInterpolator(LauncherAnimUtils.DECELERATE_QUINT)
                                .start();
                    }
                    if (mType == AD) {
                        // Choice view only applies to ad, no need to animate when charging screen is shown
                        mAdChoice.animate()
                                .alpha(1f)
                                .setDuration(DURATION_SLIDE_IN)
                                .setInterpolator(LauncherAnimUtils.DECELERATE_QUAD)
                                .start();
                    }

                    mActionBtn.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mActionBtn.reveal();
                        }
                    }, START_DELAY_BUTTON_REVEAL);
                    mActionBtn.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mActionBtn.flash();
                        }
                    }, START_DELAY_BUTTON_FLASH);
                }
            }, startDelay);
        }
    }

    public void startCardResultAnimation(long startDelay) {
        if (true) {
            mType = AD;
            startAdOrChargingScreenResultAnimation(startDelay);
        }

        if (mType == Type.CARD_VIEW) {
            if (null != mResultView) {
                mResultView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mResultView.setVisibility(View.VISIBLE);
                        startBgTranslateAnimation();
                        float slideUpTranslation = mScreenHeight - mActivity.getResources().getDimensionPixelSize(R.dimen.result_page_header_height) - CommonUtils.getStatusBarHeight(mActivity) - CommonUtils.pxFromDp(15);
                        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, slideUpTranslation, 0);
                        translateAnimation.setDuration(DURATION_CARD_TRANSLATE);
                        translateAnimation.setInterpolator(LauncherAnimationUtils.accelerateDecelerateInterpolator);
                        translateAnimation.setAnimationListener(new LauncherAnimationUtils.AnimationListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                super.onAnimationEnd(animation);
                                onFunctionCardViewShown();
                            }
                        });
                        mCardRecyclerView.startAnimation(translateAnimation);
                    }
                }, startDelay);
            }
        }
    }

    @Thunk
    void startBgTranslateAnimation() {
        if (null == mBgView) {
            return;
        }

        if (mHeaderTagView.getWidth() > 0) {
            startRealBgTranslateAnimation();
        } else {
            mHeaderTagView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mHeaderTagView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    startRealBgTranslateAnimation();
                }
            });
        }
    }

    private void startRealBgTranslateAnimation() {
        int bottom = ViewUtils.getLocationRect(mHeaderTagView).bottom;
        float translateDistance = mScreenHeight - bottom;
        mBgView.animate()
                .translationYBy(-translateDistance)
                .setDuration(DURATION_BG_TRANSLATE)
                .setInterpolator(LauncherAnimUtils.ACCELERATE_DECELERATE)
                .start();
    }

    protected Context getContext() {
        return mActivity;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.result_action_btn) {
            switch (mType) {
                case CHARGE_SCREEN:
                    ChargingManagerUtil.enableCharging(false);
                    if (!LockerSettings.isLockerEnabledBefore()) {
                        LockerSettings.setLockerEnabled(true);
                    }
                    ToastUtils.showToast(R.string.result_page_card_battery_protection_toast);
                    mActivity.finishAndNotify();
                    break;
            }

        } else if (i == R.id.cleaning_ball_iv) {
            mActivity.finish();


        } else {
        }
    }

    private void logViewEvent(Type type) {
        if (type == AD) {
            HSAnalytics.logEvent("ResultPage_Cards_Show", "type", ResultConstants.AD);
        } else if (type == Type.CHARGE_SCREEN) {
            HSAnalytics.logEvent("ResultPage_Cards_Show", "type", ResultConstants.CHARGING_SCREEN_FULL);
        } else if (type == Type.NOTIFICATION_CLEANER) {
            HSAnalytics.logEvent("ResultPage_Cards_Show", "type", ResultConstants.NOTIFICATION_CLEANER_FULL);
        }
    }

    protected void logClickEvent(Type type) {
        if (type == AD) {
            // No log here, logged in onAdClick()
        } else if (type == Type.CHARGE_SCREEN) {
            HSAnalytics.logEvent("ResultPage_Cards_Click", "Type", ResultConstants.CHARGING_SCREEN_FULL);
        } else if (type == Type.NOTIFICATION_CLEANER) {
            HSAnalytics.logEvent("ResultPage_Cards_Click", "Type", ResultConstants.NOTIFICATION_CLEANER_FULL);
        }
    }
}
