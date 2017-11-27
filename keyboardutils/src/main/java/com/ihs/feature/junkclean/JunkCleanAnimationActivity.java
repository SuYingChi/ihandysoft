package com.ihs.feature.junkclean;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.DefaultItemAnimator;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.feature.common.ActivityUtils;
import com.ihs.feature.common.BasePermissionActivity;
import com.ihs.feature.common.FormatSizeBuilder;
import com.ihs.feature.common.PromotionTracker;
import com.ihs.feature.common.Utils;
import com.ihs.feature.common.VectorCompat;
import com.ihs.feature.common.ViewUtils;
import com.ihs.feature.junkclean.data.JunkManager;
import com.ihs.feature.junkclean.list.JunkDetailItem;
import com.ihs.feature.junkclean.list.JunkHeadCategoryItem;
import com.ihs.feature.junkclean.list.JunkSubCategoryItem;
import com.ihs.feature.junkclean.model.ApkJunkWrapper;
import com.ihs.feature.junkclean.model.AppJunkWrapper;
import com.ihs.feature.junkclean.model.JunkWrapper;
import com.ihs.feature.junkclean.model.MemoryJunkWrapper;
import com.ihs.feature.junkclean.model.PathRuleJunkWrapper;
import com.ihs.feature.junkclean.model.SystemJunkWrapper;
import com.ihs.feature.junkclean.util.JunkCleanConstant;
import com.ihs.feature.junkclean.util.JunkCleanUtils;
import com.ihs.feature.resultpage.ResultPageActivity;
import com.ihs.feature.ui.TouchableRecycleView;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.permission.PermissionUtils;
import com.ihs.keyboardutils.utils.CommonUtils;
import com.kc.commons.utils.KCCommonUtils;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;


public class JunkCleanAnimationActivity extends BasePermissionActivity {

    public static final String INTENT_KEY_FROM = "INTENT_KEY_FROM";
    public static final String INTENT_KEY_APP_NAME = "INTENT_KEY_APP_NAME";
    public static final String INTENT_KEY_JUNK_SIZE = "INTENT_KEY_JUNK_SIZE";
    public static final int INTENT_FROM_CLEAN = 0;
    public static final int INTENT_FROM_INSTALL_ALERT = 1;
    public static final int INTENT_FROM_UNINSTALL_ALERT = 2;

    private static final long DURATION_COLOR_CHANGE_ANIMATION = 1200;
    private static final long DURATION_ITEM_REMOVE_ANIMATION = 1000;

    private String mSecurityPackage;

    private TouchableRecycleView mRecyclerView;
    private View mJunkSizeLayout;

    private TextView mPopJunkSizeTv;
    private TextView mPopJunkUnitTv;
    private View mStopDialogV;
    private BottomSheetDialog mBottomSheetDialog;

    private JunkManager mJunkManager = JunkManager.getInstance();

    private boolean mIsPowerfulCleanBottomDialogShowed;
    private boolean mIsBottomSkipButtonClicked;
    private boolean mIsBottomActionButtonClicked;
    private boolean mIsPaused;
    private boolean mShouldSecurityItemVisible;
    private boolean mShouldPowerfulCleanItemVisible;

    private long mJunkSize;
    private long mStopLastJunkSize;
    private int mIntentFrom = INTENT_FROM_CLEAN;
    private String mAppName;
    private long mUninstallJunkSize;

    public static void startToCleanAnimationActivity(JunkCleanActivity junkCleanActivity, JunkManager junkManager) {
        JunkCleanConstant.sIsTotalSelected = junkManager.isTotalJunkSelected();
        Intent intent = new Intent(junkCleanActivity, JunkCleanAnimationActivity.class);
        intent.putExtra(JunkCleanAnimationActivity.INTENT_KEY_FROM, JunkCleanAnimationActivity.INTENT_FROM_CLEAN);
        junkCleanActivity.startActivity(intent);
        junkCleanActivity.overridePendingTransition(0, 0);
        junkCleanActivity.setActionButtonTranslation(false, false); // Disappear
    }

    @Override
    public boolean isEnableNotificationActivityFinish() {
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_junk_clean_animation);
        initData();
        initView();

        FlexibleAdapter flexibleAdapter = new FlexibleAdapter(getListItems());
        flexibleAdapter.expandItemsAtStartUp()
                .setAnimationOnScrolling(true)
                .setAnimationDuration(175)
                .setAnimationInterpolator(new FastOutSlowInInterpolator());

        mRecyclerView.setLayoutManager(new SmoothScrollLinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(flexibleAdapter);
        mRecyclerView.setTouchable(false);

        mRecyclerView.setAlpha(0);

        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.animate().alpha(1).setDuration(375).start();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        HSLog.d(JunkCleanActivity.TAG, "RecyclerView mIsPaused = " + mIsPaused);
                        if (mIsPaused) {
                            finish();
                            return;
                        }
                        startClean();
                    }
                }, 500);
            }
        });
    }

    private void initData() {
        JunkCleanConstant.sIsJunkCleaned = false;
        Intent intent = getIntent();
        if (null != intent) {
            mIntentFrom = intent.getIntExtra(INTENT_KEY_FROM, INTENT_FROM_CLEAN);
            mAppName = intent.getStringExtra(INTENT_KEY_APP_NAME);
            mUninstallJunkSize = intent.getLongExtra(INTENT_KEY_JUNK_SIZE, 0);
        }
        mSecurityPackage = HSConfig.optString("", "Application", "Promotions", "SecurityPackage");
        boolean isNetworkAvailable = Utils.isNetworkAvailable(-1);
        boolean isSecurityInstalled = CommonUtils.isPackageInstalled(mSecurityPackage);
        boolean hasSecurityAlerted = JunkCleanUtils.hasSecurityAlerted();
        boolean hasPowerFulCleanAlerted = JunkCleanUtils.hasPowerFulCleanAlerted();
        boolean isCleanClickCountLimit = JunkCleanUtils.isCleanClickCountLimit();
        mShouldSecurityItemVisible = isNetworkAvailable && !isSecurityInstalled && !hasSecurityAlerted && !isCleanClickCountLimit
                && mIntentFrom != INTENT_FROM_INSTALL_ALERT && mIntentFrom != INTENT_FROM_UNINSTALL_ALERT;
        mShouldPowerfulCleanItemVisible = !isCleanClickCountLimit && JunkCleanUtils.shouldForceCleanSystemAppCache() && !PermissionUtils.isAccessibilityGranted()
                && !hasPowerFulCleanAlerted && mIntentFrom != INTENT_FROM_INSTALL_ALERT && mIntentFrom != INTENT_FROM_UNINSTALL_ALERT;

        HSLog.d(JunkCleanActivity.TAG, "onAnimationEnd *** isNetworkAvailable = " + isNetworkAvailable + " isSecurityInstalled = " + isSecurityInstalled + " hasSecurityAlerted = " + hasSecurityAlerted
                + " hasPowerFulCleanAlerted = " + hasPowerFulCleanAlerted + " isCleanClickCountLimit = " + isCleanClickCountLimit
                + " ||| mShouldSecurityItemVisible = " + mShouldSecurityItemVisible
                + " ||| mShouldPowerfulCleanItemVisible = " +mShouldPowerfulCleanItemVisible);
    }

    @SuppressLint("NewApi")
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        CommonUtils.setupTransparentSystemBarsForLmp(this);
        ActivityUtils.setNavigationBarColor(this, ContextCompat.getColor(this, android.R.color.black));
        ActivityUtils.configSimpleAppBar(this, getString(R.string.clean_title), Color.TRANSPARENT, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsPaused = false;
        if (mIsPowerfulCleanBottomDialogShowed && null != mBottomSheetDialog && !PermissionUtils.isAccessibilityGranted()) {
            onSkipButtonClick();
            mBottomSheetDialog = null;
        } else if (!mIsPowerfulCleanBottomDialogShowed && null != mBottomSheetDialog && mIsBottomActionButtonClicked) {
            onSkipButtonClick();
            mBottomSheetDialog = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsPaused = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBottomSheetDialog = null;
    }

    @Override
    public void onBackPressed() {
        JunkCleanUtils.showStopDialog(mStopDialogV);
    }

    public void startClean() {
        HSLog.d(JunkCleanActivity.TAG, "startClean ***");
        mRecyclerView.setTouchable(false);
        mJunkManager.startJunkClean();

        long itemAnimDuration = mRecyclerView.getChildCount() == 0 ? 0 : DURATION_ITEM_REMOVE_ANIMATION / mRecyclerView.getChildCount();

        int i = mShouldPowerfulCleanItemVisible || mShouldSecurityItemVisible ? 1 : 0;
        for (; i < mRecyclerView.getChildCount(); i++) {
            View childView = mRecyclerView.getChildAt(i);
            childView.animate().translationX(-childView.getWidth()).setDuration(DURATION_ITEM_REMOVE_ANIMATION / 2).setStartDelay(itemAnimDuration * i).start();
            childView.animate().alpha(0).setDuration(DURATION_ITEM_REMOVE_ANIMATION / 2).setStartDelay(itemAnimDuration * i).start();
        }

        ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(),
                (Object[])(mIntentFrom == INTENT_FROM_INSTALL_ALERT || mIntentFrom != INTENT_FROM_UNINSTALL_ALERT ? new Integer[] {JunkCleanConstant.PRIMARY_YELLOW, JunkCleanConstant.PRIMARY_BLUE} : mJunkManager.getColors(mJunkManager.getTotalJunkSize(), mStopLastJunkSize)));
        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int color = (int) animation.getAnimatedValue();
                mJunkSizeLayout.setBackgroundColor(color);
            }
        });

        long endSize = 0;
        if (mShouldSecurityItemVisible || mShouldPowerfulCleanItemVisible) {
            endSize = mStopLastJunkSize;
        }
        HSLog.d(JunkCleanActivity.TAG_TEST, "startClean *** startSize = " + mJunkSize + " endSize = " + endSize);

        ValueAnimator mJunkSizeAnimator = ValueAnimator.ofFloat(mJunkSize, endSize);
        mJunkSizeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float junkSize = (float) animation.getAnimatedValue();
                FormatSizeBuilder mJunkSizeBuilder = new FormatSizeBuilder((long) junkSize);
                mPopJunkSizeTv.setText(mJunkSizeBuilder.size);
                mPopJunkUnitTv.setText(mJunkSizeBuilder.unit);
            }
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(DURATION_COLOR_CHANGE_ANIMATION).addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                HSLog.d(JunkCleanActivity.TAG, "onAnimationEnd mIsPaused = " + mIsPaused + " mShouldSecurityItemVisible = " + mShouldSecurityItemVisible + " mShouldPowerfulCleanItemVisible = " + mShouldPowerfulCleanItemVisible);
                if (mIsPaused) {
                    finish();
                    return;
                }

//                if (mShouldSecurityItemVisible) {
//                    showSecurityBottomDialog();
//                } else if (mShouldPowerfulCleanItemVisible) {
//                    showPowerfulCleanBottomDialog();
//                } else {
                    startToResultPageActivity();
//                }
            }
        });
        animatorSet.playTogether(colorAnimator, mJunkSizeAnimator);
        animatorSet.start();
    }

    private void showPowerfulCleanBottomDialog() {
        mBottomSheetDialog = new BottomSheetDialog(JunkCleanAnimationActivity.this);
        mBottomSheetDialog.setCanceledOnTouchOutside(false);
        mIsPowerfulCleanBottomDialogShowed = true;

        mIsBottomSkipButtonClicked = false;
        mIsBottomActionButtonClicked = false;

        View dialogRootView = LayoutInflater.from(JunkCleanAnimationActivity.this).inflate(R.layout.clean_bottom_sheet_dialog_layout, null);
        TextView contentTv = (TextView) dialogRootView.findViewById(R.id.dialog_content_tv);
        TextView titleTv = (TextView) dialogRootView.findViewById(R.id.dialog_title_tv);
        Button actionBtn = ViewUtils.findViewById(dialogRootView, R.id.action_btn);
        Button skipBtn = ViewUtils.findViewById(dialogRootView, R.id.skip_btn);

        titleTv.setText(getString(R.string.clean_powerful_clean));

        String appSizeText = String.valueOf((int) mJunkManager.getHiddenSystemJunkSize() / 1024 / 1024) + "MB";
        String contentText = getString(R.string.clean_hidden_junk_dialog_message, appSizeText);
        SpannableString contentSpannableString = new SpannableString(contentText);
        contentSpannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.notification_red)), 0, appSizeText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        int start = contentText.indexOf(appSizeText);
        if (start != -1) {
            contentSpannableString.setSpan(new StyleSpan(Typeface.BOLD), start, start + appSizeText.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        contentTv.setText(contentSpannableString);
        actionBtn.setText(getString(R.string.advanced_boost_authorize_btn));

        skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSkipButtonClick();
            }
        });

        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                HSLog.e("这里请求Accessibility");

                mIsBottomActionButtonClicked = true;
//                PermissionUtils.requestAccessibilityPermission(JunkCleanAnimationActivity.this, new Runnable() {
//                    @Override
//                    public void run() {
//                        onBottomDialogCallBack(mBottomSheetDialog);
//                    }
//                });
            }
        });

        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                onBottomDialogDismiss();
            }
        });

        mBottomSheetDialog.setContentView(dialogRootView);
        showDialog(mBottomSheetDialog);

        JunkCleanUtils.setHasPowerFulCleanAlerted(true);
        JunkCleanUtils.setHasShowCleanNotClick(true);
    }

    private void showSecurityBottomDialog() {
        HSAnalytics.logEvent("Promotion_Viewed", "Type", "SecurityAlert");
        mBottomSheetDialog = new BottomSheetDialog(JunkCleanAnimationActivity.this);
        mBottomSheetDialog.setCanceledOnTouchOutside(false);
        mIsPowerfulCleanBottomDialogShowed = false;


        View dialogRootView = LayoutInflater.from(JunkCleanAnimationActivity.this).inflate(R.layout.clean_bottom_sheet_dialog_layout, null);
        AppCompatImageView iconIv = ViewUtils.findViewById(dialogRootView, R.id.bottom_dialog_icon_iv);
        TextView titleTv = (TextView) dialogRootView.findViewById(R.id.dialog_title_tv);
        TextView contentTv = (TextView) dialogRootView.findViewById(R.id.dialog_content_tv);
        Button actionBtn = ViewUtils.findViewById(dialogRootView, R.id.action_btn);
        Button skipBtn = ViewUtils.findViewById(dialogRootView, R.id.skip_btn);

        VectorCompat.setImageViewVectorResource(this, iconIv, R.drawable.clean_security_promotion_icon_svg);
        titleTv.setText(getString(R.string.promotion_security_alert_title));
        contentTv.setText(getString(R.string.promotion_security_alert_description));
        skipBtn.setText(" " + getString(R.string.promotion_ignore_btn) + " ");
        actionBtn.setText(getString(R.string.locker_install_btn));

        actionBtn.setOnClickListener(v -> {
            HSAnalytics.logEvent("Promotion_Clicked", "Type", "SecurityAlert");
            mIsBottomActionButtonClicked = true;
            PromotionTracker.startTracking(mSecurityPackage, PromotionTracker.EVENT_LOG_APP_NAME_SECURITY, true);
        });

        skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSkipButtonClick();
            }
        });

        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                onBottomDialogDismiss();
            }
        });

        mBottomSheetDialog.setContentView(dialogRootView);
        showDialog(mBottomSheetDialog);
        JunkCleanUtils.setHasSecurityAlerted(true);
        JunkCleanUtils.setHasShowCleanNotClick(true);
    }

    private void onSkipButtonClick() {
        mIsBottomSkipButtonClicked = true;
        KCCommonUtils.dismissDialog(mBottomSheetDialog);
        startToResultPageActivity();
    }

    private void onBottomDialogDismiss() {
        HSLog.d(JunkCleanActivity.TAG, "onBottomDialogDismiss mIsBottomSkipButtonClicked = " + mIsBottomSkipButtonClicked + " mIsBottomActionButtonClicked = " + mIsBottomActionButtonClicked);
        if (mIsBottomSkipButtonClicked || mIsBottomActionButtonClicked) {
            return;
        }

        startToResultPageActivity();
    }

//    private void onBottomDialogCallBack(BottomSheetDialog bottomSheetDialog) {
//        mJunkManager.selectSystemJunk();
//        mJunkManager.startJunkClean();
//
//        LauncherFloatWindowManager.getInstance().removeFloatButton();
//        SettingLauncherPadActivity.closeSettingsActivity(JunkCleanAnimationActivity.this);
//
//        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
//            bottomSheetDialog.dismiss();
//        }
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                startToResultPageActivity();
//            }
//        }, 300);
//    }

    private void startToResultPageActivity() {
        HSLog.d(JunkCleanActivity.TAG, "JunkCleanAnimationActivity startToResultPageActivity mIsBottomSkipButtonClicked = " + mIsBottomSkipButtonClicked);
        Runnable runnable = () -> {
            Activity activity = JunkCleanActivity.getInstance() == null ? JunkCleanAnimationActivity.this : JunkCleanActivity.getInstance();
            ResultPageActivity.startForJunkClean(activity);
            finish();
        };

        if (mIsBottomSkipButtonClicked) {
            runnable.run();
            return;
        }

        if (mShouldPowerfulCleanItemVisible || mShouldSecurityItemVisible) {
            HSLog.d(JunkCleanActivity.TAG, "onBottomDialogCallBack *** startSize = " + mStopLastJunkSize + " endSize = " + 0);

            if (mRecyclerView.getChildCount() > 0) {
                final View childView = mRecyclerView.getChildAt(0);
                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, childView.getWidth());
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (float) animation.getAnimatedValue();
                        childView.setTranslationX(-value);
                        childView.setAlpha(1 - value / childView.getWidth());
                    }
                });
                valueAnimator.setDuration(DURATION_ITEM_REMOVE_ANIMATION / 2);
                valueAnimator.start();
            }

            ValueAnimator mJunkSizeAnimator = ValueAnimator.ofFloat(mStopLastJunkSize, 0);
            mJunkSizeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float junkSize = (float) animation.getAnimatedValue();
                    FormatSizeBuilder mJunkSizeBuilder = new FormatSizeBuilder((long) junkSize);
                    mPopJunkSizeTv.setText(mJunkSizeBuilder.size);
                    mPopJunkUnitTv.setText(mJunkSizeBuilder.unit);
                }
            });
            mJunkSizeAnimator.setDuration(DURATION_ITEM_REMOVE_ANIMATION / 2);
            mJunkSizeAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    runnable.run();
                }
            });
            mJunkSizeAnimator.start();
        } else {
            runnable.run();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        mRecyclerView = (TouchableRecycleView) findViewById(R.id.recycler_view);

        TextView popJunkLabelTv = (TextView) findViewById(R.id.pop_junk_label);
        mPopJunkSizeTv = (TextView) findViewById(R.id.pop_junk_size);
        mPopJunkUnitTv = (TextView) findViewById(R.id.pop_junk_unit);

        if (mIntentFrom == INTENT_FROM_UNINSTALL_ALERT) {
            mJunkSize = mUninstallJunkSize;
        } else {
            mJunkSize = mJunkManager.getJunkSelectedSize();
        }

        mStopLastJunkSize = 0;
        if (mShouldPowerfulCleanItemVisible || mShouldSecurityItemVisible) {
            List<JunkWrapper> junkWrapperList = mJunkManager.getJunkWrappers();
            if (null != junkWrapperList && junkWrapperList.size() > 0) {
                JunkWrapper firstJunkWrapper = junkWrapperList.get(0);
                mStopLastJunkSize = firstJunkWrapper.getSize();
            }
        }

        FormatSizeBuilder junkSizeBuilder = new FormatSizeBuilder(mJunkSize);
        mPopJunkSizeTv.setText(junkSizeBuilder.size);
        mPopJunkUnitTv.setText(junkSizeBuilder.unit);
        popJunkLabelTv.setText(getString(R.string.clean_junk_selected));

        mJunkSizeLayout = findViewById(R.id.junk_size_layout);
        mJunkSizeLayout.setBackgroundColor(mIntentFrom == INTENT_FROM_INSTALL_ALERT || mIntentFrom == INTENT_FROM_UNINSTALL_ALERT ? JunkCleanConstant.PRIMARY_YELLOW : mJunkManager.getColor());
        mStopDialogV = JunkCleanUtils.initStopDialog(this);
    }

    private List<AbstractFlexibleItem> getListItems() {
        List<JunkWrapper> junkWrappers = mJunkManager.getJunkWrappers();
        List<AbstractFlexibleItem> flexibleItems = new ArrayList<>();

        JunkSubCategoryItem systemSubCategory = new JunkSubCategoryItem(JunkCleanConstant.CATEGORY_SYSTEM_JUNK);
        JunkSubCategoryItem uninstallAppSubCategory = new JunkSubCategoryItem(JunkCleanConstant.CATEGORY_UNINSTALL_APP_JUNK);
        JunkSubCategoryItem apkSubCategory = new JunkSubCategoryItem(JunkCleanConstant.CATEGORY_APK_JUNK);

        JunkHeadCategoryItem appHeadCategory = new JunkHeadCategoryItem(JunkCleanConstant.CATEGORY_APP_JUNK);
        List<JunkSubCategoryItem> installAppSubItems = new ArrayList<>();

        JunkHeadCategoryItem pathRuleHeadCategory = new JunkHeadCategoryItem(JunkCleanConstant.CATEGORY_PATH_RULE_JUNK);

        JunkHeadCategoryItem memoryHeadCategory = new JunkHeadCategoryItem(JunkCleanConstant.CATEGORY_MEMORY_JUNK);

        for (JunkWrapper junkWrapper : junkWrappers) {
            if (null == junkWrapper) {
                continue;
            }
            if (mIntentFrom == INTENT_FROM_INSTALL_ALERT && junkWrapper.getType() != JunkWrapper.TYPE_JUNK_INSTALL) {
                continue;
            } else if (mIntentFrom == INTENT_FROM_UNINSTALL_ALERT && junkWrapper.getType() != JunkWrapper.TYPE_JUNK_UNINSTALL) {
                continue;
            }
            if (mShouldPowerfulCleanItemVisible && !mShouldSecurityItemVisible) {
                if (!junkWrapper.getCategory().equals(SystemJunkWrapper.SYSTEM_JUNK)
                        && !junkWrapper.isMarked()) {
                        continue;
                }
            } else if (!junkWrapper.isMarked()) {
                continue;
            }

            switch (junkWrapper.getCategory()) {
                case SystemJunkWrapper.SYSTEM_JUNK:
                    JunkDetailItem systemJunkItem = new JunkDetailItem(junkWrapper);
                    systemSubCategory.addSubItem(systemJunkItem);
                    systemJunkItem.setParentCategory(systemSubCategory);
                    break;

                case AppJunkWrapper.APP_JUNK:
                    JunkDetailItem appJunkItem = new JunkDetailItem(junkWrapper);

                    if (((AppJunkWrapper) junkWrapper).isInstall()) {
                        boolean sameTypeItem = false;

                        for (JunkSubCategoryItem subItem : installAppSubItems) {
                            if (TextUtils.equals(subItem.getJunkWrapper().getPackageName(), junkWrapper.getPackageName())) {
                                subItem.addSubItem(appJunkItem);

                                appJunkItem.setParentCategory(subItem);
                                sameTypeItem = true;
                            }
                        }

                        if (!sameTypeItem) {
                            JunkSubCategoryItem subItem = new JunkSubCategoryItem(JunkCleanConstant.CATEGORY_INSTALL_APP_JUNK);
                            subItem.setJunkWrapper(junkWrapper);
                            subItem.addSubItem(appJunkItem);
                            appJunkItem.setParentCategory(subItem);

                            installAppSubItems.add(subItem);
                        }
                    } else {
                        uninstallAppSubCategory.addSubItem(appJunkItem);
                        appJunkItem.setParentCategory(uninstallAppSubCategory);
                    }
                    break;

                case ApkJunkWrapper.APK_JUNK:
                    JunkDetailItem apkJunkItem = new JunkDetailItem(junkWrapper);
                    apkSubCategory.addSubItem(apkJunkItem);
                    apkJunkItem.setParentCategory(apkSubCategory);
                    break;

                case PathRuleJunkWrapper.PATH_RULE_JUNK:
                    JunkSubCategoryItem pathRuleJunkSubCategory = new JunkSubCategoryItem(JunkCleanConstant.CATEGORY_PATH_RULE_JUNK);
                    pathRuleJunkSubCategory.setJunkWrapper(junkWrapper);
                    pathRuleHeadCategory.addSubItem(pathRuleJunkSubCategory);
                    pathRuleJunkSubCategory.setParentCategory(pathRuleHeadCategory);
                    break;

                case MemoryJunkWrapper.MEMORY_JUNK:
                    JunkSubCategoryItem memorySubCategory = new JunkSubCategoryItem(JunkCleanConstant.CATEGORY_MEMORY_JUNK);
                    memorySubCategory.setJunkWrapper(junkWrapper);
                    memoryHeadCategory.addSubItem(memorySubCategory);
                    memorySubCategory.setParentCategory(memoryHeadCategory);
                    break;
            }
        }

        if (systemSubCategory.getSize() > 0) {
            systemSubCategory.setCheckBoxVisible(false);
            flexibleItems.add(systemSubCategory);
        }

        if (uninstallAppSubCategory.getSize() > 0) {
            uninstallAppSubCategory.setCheckBoxVisible(false);
            flexibleItems.add(uninstallAppSubCategory);
        }

        if (apkSubCategory.getSize() > 0) {
            apkSubCategory.setCheckBoxVisible(false);
            flexibleItems.add(apkSubCategory);
        }

        for (JunkSubCategoryItem item : installAppSubItems) {
            appHeadCategory.addSubItem(item);
            item.setParentCategory(appHeadCategory);
        }

        // 一屏显示不下，所以除system cache 之外只加载十个
        int count = 0;
        for (JunkSubCategoryItem item : appHeadCategory.getSubItems()) {
            if (count >= 10) {
                break;
            }
            count++;
            item.setCheckBoxVisible(false);
            flexibleItems.add(item);
        }

        for (JunkSubCategoryItem item : pathRuleHeadCategory.getSubItems()) {
            if (count >= 10) {
                break;
            }
            count++;
            item.setCheckBoxVisible(false);
            flexibleItems.add(item);
        }

        for (JunkSubCategoryItem item : memoryHeadCategory.getSubItems()) {
            if (count >= 10) {
                break;
            }
            count++;
            item.setCheckBoxVisible(false);
            flexibleItems.add(item);
        }


        if (mIntentFrom == INTENT_FROM_UNINSTALL_ALERT) {
            JunkSubCategoryItem adRandomCacheCategory = new JunkSubCategoryItem(JunkCleanConstant.CATEGORY_UNINSTALL_AD_RANDOM_JUNK);
            adRandomCacheCategory.setCheckBoxVisible(false);
            JunkSubCategoryItem appRandomCacheCategory = new JunkSubCategoryItem(JunkCleanConstant.CATEGORY_UNINSTALL_APP_RANDOM_JUNK);
            appRandomCacheCategory.setCheckBoxVisible(false);
            appRandomCacheCategory.setAppName(TextUtils.isEmpty(mAppName) ? "" : mAppName);
            flexibleItems.add(adRandomCacheCategory);
            flexibleItems.add(appRandomCacheCategory);
        }

        return flexibleItems;
    }

}
