package com.ihs.feature.junkclean;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.artw.lockscreen.common.NavUtils;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.feature.boost.plus.BannerBackground;
import com.ihs.feature.common.ActivityUtils;
import com.ihs.feature.common.AnimatorListenerAdapter;
import com.ihs.feature.common.BasePermissionActivity;
import com.ihs.feature.common.FormatSizeBuilder;
import com.ihs.feature.common.LauncherAnimUtils;
import com.ihs.feature.common.PromotionTracker;
import com.ihs.feature.common.SpringInterpolator;
import com.ihs.feature.common.Thunk;
import com.ihs.feature.common.Utils;
import com.ihs.feature.common.ViewUtils;
import com.ihs.feature.junkclean.data.JunkManager;
import com.ihs.feature.junkclean.list.JunkDetailItem;
import com.ihs.feature.junkclean.list.JunkHeadCategoryItem;
import com.ihs.feature.junkclean.list.JunkSubCategoryItem;
import com.ihs.feature.junkclean.list.PowerfulCleanItem;
import com.ihs.feature.junkclean.list.SecurityItem;
import com.ihs.feature.junkclean.list.TopBannerBaseItem;
import com.ihs.feature.junkclean.model.ApkJunkWrapper;
import com.ihs.feature.junkclean.model.AppJunkWrapper;
import com.ihs.feature.junkclean.model.JunkInfo;
import com.ihs.feature.junkclean.model.JunkWrapper;
import com.ihs.feature.junkclean.model.MemoryJunkWrapper;
import com.ihs.feature.junkclean.model.PathRuleJunkWrapper;
import com.ihs.feature.junkclean.model.SystemJunkWrapper;
import com.ihs.feature.junkclean.util.JunkCleanConstant;
import com.ihs.feature.junkclean.util.JunkCleanUtils;
import com.ihs.feature.junkclean.view.PermissionDialog;
import com.ihs.feature.notification.NotificationManager;
import com.ihs.feature.resultpage.ResultEmptyView;
import com.ihs.feature.resultpage.ResultPageAdsManager;
import com.ihs.feature.ui.HeuristicAnimator;
import com.ihs.feature.ui.ProgressWheel;
import com.ihs.feature.ui.TouchableRecycleView;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.permission.PermissionUtils;
import com.ihs.keyboardutils.utils.CommonUtils;
import com.ihs.keyboardutils.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

public class JunkCleanActivity extends BasePermissionActivity {

    public static final String TAG = "JunkCleanActivity";
    public static final String TAG_TEST = "CleanTest";

    private static final long DURATION_JUNK_SIZE_SCAN_ANIM = 5000;
    private static final long DELAY_SCAN_FINISHED = 300;
    public static boolean sIsJunkCleanRunning;

    private String mSecurityPackage;

    private RecyclerView.AdapterDataObserver mAdapterDataObserver;
    @Thunk
    TouchableRecycleView mRecyclerView;
    @Thunk FlexibleAdapter mFlexibleAdapter;
    @Thunk AppBarLayout mAppBarLayout;
    @Thunk CollapsingToolbarLayout mCollapsingToolbarLayout;

    @Thunk TextView mScanJunkLabelTv;
    @Thunk TextView mScanJunkSizeTv;
    @Thunk TextView mScanJunkUnitTv;

    @Thunk TextView mSystemJunkSizeTv;
    @Thunk TextView mAdJunkSizeTv;
    @Thunk TextView mAppJunkSizeTv;
    @Thunk TextView mMemoryJunkSizeTv;
    @Thunk TextView mScanningSelectedTextTv;

    @Thunk
    ProgressWheel mSystemProgressWheel;
    @Thunk ProgressWheel mAdProgressWheel;
    @Thunk ProgressWheel mAppProgressWheel;
    @Thunk ProgressWheel mMemoryProgressWheel;

    @Thunk AppCompatImageView mSystemTickIv;
    @Thunk AppCompatImageView mAdTickIv;
    @Thunk AppCompatImageView mAppTickIv;
    @Thunk AppCompatImageView mMemoryTickIv;
    @Thunk AppCompatImageView mLoadingProgressIv;
    @Thunk Button mCleanButtonTv;

    private ViewPropertyAnimator mActionBtnAnimator;
    @Thunk
    HeuristicAnimator mProgressAnimator;

    @Thunk
    JunkManager mJunkManager = JunkManager.getInstance();

    @Thunk ValueAnimator mTotalJunkSizeAnimator;
    @Thunk ValueAnimator mSystemJunkSizeAnimator;
    @Thunk ValueAnimator mAppJunkSizeAnimator;
    @Thunk ValueAnimator mAdJunkSizeAnimator;
    @Thunk ValueAnimator mMemoryJunkSizeAnimator;
    private BannerBackground mBannerBg;

    @Thunk long mCurrentTotalJunkSize;
    @Thunk long mCurrentSystemJunkSize;
    @Thunk long mCurrentAppJunkSize;
    @Thunk long mCurrentAdJunkSize;
    @Thunk long mCurrentMemoryJunkSize;

    private float mTotalTranslation;
    private int mPhoneWidth;

    @Thunk boolean mIsTotalAnimationScanFinished;
    @Thunk boolean mIsSystemScanAnimationFinished;
    @Thunk boolean mIsAppScanAnimationFinished;
    @Thunk boolean mIsAdScanAnimationFinished;
    @Thunk boolean mIsMemoryScanAnimationFinished;

    @Thunk boolean mIsOnScanFinished;
    @Thunk boolean mIsJunkScanFrozen;
    @Thunk boolean mHasTouched;
    @Thunk boolean mIsActionBtnAnimating;
    @Thunk boolean mIsSecurityBannerShowed;
    @Thunk boolean mIsJunkScanning;
    @Thunk boolean mIsCleanButtonAppearStatus;

    @SuppressLint("StaticFieldLeak")
    private static JunkCleanActivity sJunkCleanActivity;

    private class LongEvaluator implements TypeEvaluator {
        @Override
        public Object evaluate(float fraction, Object startValue, Object endValue) {
            long startJunkSize = (long) startValue;
            long endJunkSize = (long) endValue;
            return (long) (startJunkSize + (endJunkSize - startJunkSize) * fraction);
        }
    }

    public static JunkCleanActivity getInstance() {
        return sJunkCleanActivity;
    }

    @Override
    public boolean isEnableNotificationActivityFinish() {
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_junk_clean);
        sJunkCleanActivity = this;

        mSecurityPackage = HSConfig.optString("", "Application", "Promotions", "SecurityPackage");

        initData();
        initView();
        setListener();

        long totalJunkSize = mJunkManager.getTotalJunkSize();
        HSLog.d(TAG, "onCreate === mIsJunkScanFrozen = " + mIsJunkScanFrozen + " totalJunkSize = " + totalJunkSize);

        if (totalJunkSize == 0 && mIsJunkScanFrozen) {
            showEmptyView();
        }

        mFlexibleAdapter = new FlexibleAdapter(mIsJunkScanFrozen ? getListItems() : getScanningListItems());
        mFlexibleAdapter.expandItemsAtStartUp()
            .setAnimationOnScrolling(true)
            .setAnimationDuration(375)
            .setAnimationInterpolator(new FastOutSlowInInterpolator());

        mRecyclerView.setLayoutManager(new SmoothScrollLinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mFlexibleAdapter);
        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!mHasTouched) {
                    mHasTouched = true;
                    mFlexibleAdapter.setAnimationOnScrolling(false);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mAppBarLayout.setElevation(getResources().getDimension(R.dimen.clean_elevation_app_bar));
                    }
                }
                return false;
            }
        });

        if (mIsJunkScanFrozen) {
            setActionButtonTranslation(true, false);
        } else {
            mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mRecyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    initScanItemView();
                }
            });
        }

        ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(),
                ContextCompat.getColor(this, R.color.window_background), 0xf0f0f0);
        colorAnimator.setDuration(375).addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ((View) mRecyclerView.getParent()).setBackgroundColor((Integer) animation.getAnimatedValue());
            }
        });

        startJunkScan();

        JunkCleanUtils.setLastOpenJunkCleanTime();
        if (!JunkCleanConstant.JUNK_CLEAN_BADGE_DISABLED) {
            NotificationManager.getInstance().setTimeAlarm(NotificationManager.ACTION_JUNK_CLEAN_BADGE, NotificationManager.JUNK_CLEAN_BADGE_ALARM_INTERVAL);
        }
        refreshJunkSize(false);

        mAdapterDataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                HSLog.d(TAG, "AdapterDataObserver *** selectedSize = " + mJunkManager.getJunkSelectedSize()
                        + " mIsJunkScanFrozen = " + mIsJunkScanFrozen + " mIsJunkScanFrozen = " + mIsJunkScanFrozen + " mIsOnScanFinished = " + mIsOnScanFinished);
                refreshJunkSelectedSizeText();
                if (mIsOnScanFinished) {
                    refreshCleanButtonStatus(true);
                }
            }
        };
        mFlexibleAdapter.registerAdapterDataObserver(mAdapterDataObserver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
////            if (NotificationCleanerUtil.isJunkCleanerSettingsNotificationOpened()) {
////                getMenuInflater().inflate(R.menu.boost_plus, menu);
////            } else {
////                getMenuInflater().inflate(R.menu.boost_plus_new, menu);
////            }
//            return true;
//        } else {
//            return false;
//        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();

        } else if (i == R.id.action_bar_refresh) {
            NavUtils.startActivitySafely(this, new Intent(JunkCleanActivity.this, JunkCleanerSettingsActivity.class));
            invalidateOptionsMenu();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initScanItemView() {
        RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        if (layoutManager != null && layoutManager.getChildCount() >= 3) {
            View systemItemView = layoutManager.findViewByPosition(0);
            View appItemView = layoutManager.findViewByPosition(1);
            View adItemView = layoutManager.findViewByPosition(2);
            View memoryItemView = layoutManager.findViewByPosition(3);
            if (null != systemItemView) {
                mSystemJunkSizeTv = (TextView) systemItemView.findViewById(R.id.category_junk_size);
                mSystemProgressWheel = (ProgressWheel) systemItemView.findViewById(R.id.category_progress_wheel);
                mSystemTickIv= (AppCompatImageView) systemItemView.findViewById(R.id.category_load_tick_view);
            }
            if (null != appItemView) {
                mAppJunkSizeTv = (TextView) appItemView.findViewById(R.id.category_junk_size);
                mAppProgressWheel = (ProgressWheel) appItemView.findViewById(R.id.category_progress_wheel);
                mAppTickIv= (AppCompatImageView) appItemView.findViewById(R.id.category_load_tick_view);
            }
            if (null != adItemView) {
                mAdJunkSizeTv = (TextView) adItemView.findViewById(R.id.category_junk_size);
                mAdProgressWheel = (ProgressWheel) adItemView.findViewById(R.id.category_progress_wheel);
                mAdTickIv= (AppCompatImageView) adItemView.findViewById(R.id.category_load_tick_view);
            }
            if (null != memoryItemView) {
                mMemoryJunkSizeTv = (TextView) memoryItemView.findViewById(R.id.category_junk_size);
                mMemoryProgressWheel = (ProgressWheel) memoryItemView.findViewById(R.id.category_progress_wheel);
                mMemoryTickIv= (AppCompatImageView) memoryItemView.findViewById(R.id.category_load_tick_view);
            }
        }
    }

    private void refreshCleanButtonStatus(boolean isAnimation) {
        long markedJunkSize = mJunkManager.getJunkSelectedSize();
        if (markedJunkSize <= 0) {
            setActionButtonTranslation(false, isAnimation); // Disappear
        } else {
            setActionButtonTranslation(true, isAnimation); // Appear
        }
    }

    private void setListener() {
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float alpha = 1 - (float) Math.abs(verticalOffset) / (appBarLayout.getTotalScrollRange() - 100);
                if (alpha < 0) {
                    alpha = 0;
                }
                if (alpha > 1.0f) {
                    alpha = 1.0f;
                }
                mScanJunkLabelTv.setAlpha(alpha);
                mScanJunkSizeTv.setAlpha(alpha);
                mScanJunkUnitTv.setAlpha(alpha);
                mScanningSelectedTextTv.setAlpha(alpha);
            }
        });

        mCleanButtonTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ResultPageAdsManager.getInstance().preloadAd();
                JunkCleanUtils.setCleanClickCount(JunkCleanUtils.hasShowCleanNotClick());
                JunkCleanUtils.setHasShowCleanNotClick(false);
                boolean systemCacheChecked = false;
                for (JunkWrapper junkWrapper : mJunkManager.getJunkWrappers()) {
                    if (junkWrapper.getCategory().equals(SystemJunkWrapper.SYSTEM_JUNK) && junkWrapper.isMarked()) {
                        systemCacheChecked = true;
                        break;
                    }
                }

                boolean shouldForceCleanSystemAppCache = JunkCleanUtils.shouldForceCleanSystemAppCache();
                boolean isAccessibilityGranted = PermissionUtils.isAccessibilityGranted();

                boolean isNetworkAvailable = Utils.isNetworkAvailable(-1);
                boolean isSecurityInstalled = CommonUtils.isPackageInstalled(mSecurityPackage);
                boolean hasSecurityAlerted = JunkCleanUtils.hasSecurityAlerted();
                boolean isCleanClickCountLimit = JunkCleanUtils.isCleanClickCountLimit();
                boolean shouldSecurityItemVisible = isNetworkAvailable
                        && !isSecurityInstalled
                        && !hasSecurityAlerted
                        && !isCleanClickCountLimit;

                boolean isDebug = HSLog.isDebugging();
                HSLog.d(TAG, "onClick *** systemCacheChecked = " + systemCacheChecked + " shouldForceCleanSystemAppCache = " + shouldForceCleanSystemAppCache
                        + " isAccessibilityGranted = " + isAccessibilityGranted + " shouldSecurityItemVisible " + shouldSecurityItemVisible + " debug = " + isDebug);
                if (!shouldSecurityItemVisible && systemCacheChecked && shouldForceCleanSystemAppCache && isAccessibilityGranted) {
                    if (isDebug) {
                        Toast.makeText(JunkCleanActivity.this, "Junk Clean Float Window", Toast.LENGTH_LONG).show();
                    }
                    new JunkCleanWindowController(JunkCleanActivity.this).showCleanWindow();
                } else {
                    JunkCleanAnimationActivity.startToCleanAnimationActivity(JunkCleanActivity.this, mJunkManager);
                }
                JunkCleanUtils.FlurryLogger.logHomepageButtonClicked();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshView();
    }

    public void refreshView() {
        HSLog.d(TAG, "refreshView *** sIsTotalCleaned = " + JunkCleanConstant.sIsTotalCleaned + " sIsJunkCleaned = " + JunkCleanConstant.sIsJunkCleaned + " visible = " + mRecyclerView.getVisibility());
        if (JunkCleanConstant.sIsTotalCleaned && mRecyclerView.getVisibility() != View.GONE) {
            showEmptyView();
        } else if (JunkCleanConstant.sIsJunkCleaned) {
            updateAdapterDataSet(getListItems());
            JunkCleanConstant.sIsJunkCleaned = false;
            refreshBannerColor(mJunkManager.getTotalJunkSize(), false);
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        CommonUtils.setupTransparentSystemBarsForLmp(this);
        ActivityUtils.setNavigationBarColor(this, ContextCompat.getColor(this, android.R.color.black));
        ActivityUtils.configSimpleAppBar(this, getString(R.string.clean_title), Color.TRANSPARENT, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sIsJunkCleanRunning = false;
        sJunkCleanActivity = null;
        JunkCleanConstant.sIsJunkCleaned = false;

        mFlexibleAdapter.unregisterAdapterDataObserver(mAdapterDataObserver);
        stopLoadingAnimation(0);

        if (null != mTotalJunkSizeAnimator) {
            mTotalJunkSizeAnimator.cancel();
            mTotalJunkSizeAnimator = null;
        }
        if (null != mSystemJunkSizeAnimator) {
            mSystemJunkSizeAnimator.cancel();
            mSystemJunkSizeAnimator = null;
        }
        if (null != mAppJunkSizeAnimator) {
            mAppJunkSizeAnimator.cancel();
            mAppJunkSizeAnimator = null;
        }
        if (null != mAdJunkSizeAnimator) {
            mAdJunkSizeAnimator.cancel();
            mAdJunkSizeAnimator = null;
        }
        if (null != mMemoryJunkSizeAnimator) {
            mMemoryJunkSizeAnimator.cancel();
            mMemoryJunkSizeAnimator = null;
        }

        if (null != mActionBtnAnimator) {
            mActionBtnAnimator.cancel();
            mActionBtnAnimator = null;
        }

        mJunkManager.stopJunkScan();
        HSLog.d(TAG, "onDestroy mIsOnScanFinished = " + mIsOnScanFinished);
        JunkCleanUtils.setIsScanCanceled(!mIsOnScanFinished);
        ResultPageAdsManager.getInstance().releaseAd();
    }

    private void initData() {
        sIsJunkCleanRunning = true;
        mBannerBg = new BannerBackground(this, R.id.app_bar);
        mIsJunkScanFrozen = mJunkManager.isJunkScanFrozen();

        if (!mIsJunkScanFrozen) {
            JunkCleanConstant.sIsTotalCleaned = false;
        }
        mTotalTranslation = getResources().getDimensionPixelOffset(R.dimen.boost_plus_action_btn_anim_translation);
        mPhoneWidth = CommonUtils.getPhoneWidth(JunkCleanActivity.this);
    }

    private void initView() {
        mRecyclerView = (TouchableRecycleView) findViewById(R.id.recycler_view);
        mScanJunkLabelTv = (TextView) findViewById(R.id.pop_junk_label);
        mScanJunkSizeTv = (TextView) findViewById(R.id.pop_junk_size);
        mScanJunkUnitTv = (TextView) findViewById(R.id.pop_junk_unit);
        mScanningSelectedTextTv = (TextView) findViewById(R.id.scanning_selected_text_tv);
        mLoadingProgressIv = ViewUtils.findViewById(this, R.id.loading_progress_bar_iv);

        mCleanButtonTv = (Button) findViewById(R.id.clean_button);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        mCollapsingToolbarLayout = ViewUtils.findViewById(this, R.id.collapsing_tool_bar);
    }

    private void refreshJunkSize(boolean isEmpty) {
        if (null != mScanJunkSizeTv) {
            if (isEmpty) {
                mScanJunkSizeTv.setText("0");
                mScanJunkUnitTv.setText(getString(R.string.megabyte_abbr));
                return;
            }
            FormatSizeBuilder junkSizeBuilder = new FormatSizeBuilder(mJunkManager.getTotalJunkSize());
            mScanJunkSizeTv.setText(junkSizeBuilder.size);
            mScanJunkUnitTv.setText(junkSizeBuilder.unit);
        }
    }

    private void refreshJunkSelectedSizeText() {
        long junkSelectedSize = mJunkManager.getJunkSelectedSize();
        final FormatSizeBuilder selectedSizeBuilder = new FormatSizeBuilder(junkSelectedSize);
        mCleanButtonTv.setText(getString(R.string.clean_action_button_text, selectedSizeBuilder.size + selectedSizeBuilder.unit));
        mScanningSelectedTextTv.setText(getString(R.string.boost_plus_selected_size_text, selectedSizeBuilder.sizeUnit));
        if (junkSelectedSize == 0) {
            setActionButtonTranslation(false, false);
        }
    }

    public void refreshBannerColor(final long totalSizeBytes, final boolean animated) {
        int sizeInMb = (int) (totalSizeBytes / (1024 * 1024));
        if (sizeInMb >= 100) {
            mBannerBg.setBannerColor(R.color.boost_plus_red, animated);
            return;
        }
        if (sizeInMb < 100 && sizeInMb >= 30) {
            mBannerBg.setBannerColor(R.color.boost_plus_yellow, animated);
            return;
        }
        mBannerBg.setBannerColor(R.color.clean_primary_blue, animated);
    }

    private void startLoadingAnimation() {
        if (mProgressAnimator != null) {
            mProgressAnimator.cancel();
        }
        final int translateX = -mPhoneWidth + CommonUtils.pxFromDp(10);
        mProgressAnimator = new HeuristicAnimator(0, 100, 5000);
        mProgressAnimator.setConstringencyRatio(0.9f);
        mProgressAnimator.setListener(new HeuristicAnimator.AnimatorListener(){
            @Override
            public void onAnimationStart(HeuristicAnimator animation) {
                mLoadingProgressIv.setVisibility(View.VISIBLE);
                mLoadingProgressIv.setTranslationX(translateX);
                mLoadingProgressIv.setScaleX(0);
            }

            @Override
            public void onAnimationEnd(HeuristicAnimator animation) {
                mLoadingProgressIv.setTranslationX(0f);
                mLoadingProgressIv.setScaleX(1.0f);
            }
        });
        mProgressAnimator.setUpdateListener(new HeuristicAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(HeuristicAnimator animation) {
                float progress = animation.getAnimatedValue();
                mLoadingProgressIv.setTranslationX((1 - progress / 100) * translateX);
                mLoadingProgressIv.setScaleX(progress / 100);
            }
        });
        mProgressAnimator.start();
    }

    private void stopLoadingAnimation(long delayTime) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mProgressAnimator != null) {
                    mProgressAnimator.cancel();
                }
                if (null != mLoadingProgressIv) {
                    mLoadingProgressIv.setVisibility(View.GONE);
                    mLoadingProgressIv.setTranslationX(0f);
                    mLoadingProgressIv.setScaleX(1.0f);
                }
            }
        };
        if (0 == delayTime) {
            runnable.run();
        } else {
            if (null != mLoadingProgressIv) {
                mLoadingProgressIv.postDelayed(runnable, delayTime);
            }
        }
    }

    private List<AbstractFlexibleItem> getScanningListItems() {
        JunkHeadCategoryItem.isScanStatus = true;
        List<AbstractFlexibleItem> flexibleItems = new ArrayList<>();

        JunkHeadCategoryItem systemHeadCategory = new JunkHeadCategoryItem(JunkCleanConstant.CATEGORY_SYSTEM_JUNK);
        JunkHeadCategoryItem appHeadCategory = new JunkHeadCategoryItem(JunkCleanConstant.CATEGORY_APP_JUNK);
        JunkHeadCategoryItem pathRuleHeadCategory = new JunkHeadCategoryItem(JunkCleanConstant.CATEGORY_AD_JUNK);
        JunkHeadCategoryItem memoryHeadCategory = new JunkHeadCategoryItem(JunkCleanConstant.CATEGORY_MEMORY_JUNK);

        flexibleItems.add(systemHeadCategory);
        flexibleItems.add(appHeadCategory);
        flexibleItems.add(pathRuleHeadCategory);
        flexibleItems.add(memoryHeadCategory);
        return flexibleItems;
    }

    private List<AbstractFlexibleItem> getListItems() {
        HSLog.d(TAG, "getListItems ****");
        JunkHeadCategoryItem.isScanStatus = false;

        List<AbstractFlexibleItem> flexibleItems = new ArrayList<>();
        if (mJunkManager.getTotalJunkSize() == 0) {
            return flexibleItems;
        }

        List<JunkWrapper> junkWrappers = mJunkManager.getJunkWrappers();
        JunkHeadCategoryItem systemHeadCategory = new JunkHeadCategoryItem(JunkCleanConstant.CATEGORY_SYSTEM_JUNK);
        JunkSubCategoryItem systemSubCategory = new JunkSubCategoryItem(JunkCleanConstant.CATEGORY_SYSTEM_JUNK);
        JunkSubCategoryItem uninstallAppSubCategory = new JunkSubCategoryItem(JunkCleanConstant.CATEGORY_UNINSTALL_APP_JUNK);
        JunkSubCategoryItem apkSubCategory = new JunkSubCategoryItem(JunkCleanConstant.CATEGORY_APK_JUNK);

        JunkHeadCategoryItem appHeadCategory = new JunkHeadCategoryItem(JunkCleanConstant.CATEGORY_APP_JUNK);
        List<JunkSubCategoryItem> installAppSubItems = new ArrayList<>();

        JunkHeadCategoryItem pathRuleHeadCategory = new JunkHeadCategoryItem(JunkCleanConstant.CATEGORY_AD_JUNK);

        JunkHeadCategoryItem memoryHeadCategory = new JunkHeadCategoryItem(JunkCleanConstant.CATEGORY_MEMORY_JUNK);

        for (JunkWrapper junkWrapper : junkWrappers) {
            switch (junkWrapper.getCategory()) {
                case SystemJunkWrapper.SYSTEM_JUNK:
                    if(true){
                        break;
                    }

                    JunkDetailItem systemJunkItem = new JunkDetailItem(junkWrapper);

                    if (JunkCleanUtils.shouldForceCleanSystemAppCache() && !PermissionUtils.isAccessibilityGranted()) {
                        systemJunkItem.setOnItemCheckBoxTouchedListener(new JunkDetailItem.OnItemCheckedListener() {
                            @Override
                            public boolean onItemChecked() {
                                if (!PermissionUtils.isAccessibilityGranted()) {
                                    showDialog(new PermissionDialog(JunkCleanActivity.this));
                                    return true;
                                }
                                return false;
                            }
                        });
                    }

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
                    JunkSubCategoryItem pathRuleJunkSubCategory = new JunkSubCategoryItem(JunkCleanConstant.CATEGORY_AD_JUNK);
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

        if (systemSubCategory.getSubItems().size() > 0) {
            systemHeadCategory.addSubItem(systemSubCategory);
            systemSubCategory.setParentCategory(systemHeadCategory);
        }

        if (uninstallAppSubCategory.getSubItems().size() > 0) {
            systemHeadCategory.addSubItem(uninstallAppSubCategory);
            uninstallAppSubCategory.setParentCategory(systemHeadCategory);
        }

        if (apkSubCategory.getSubItems().size() > 0) {
            systemHeadCategory.addSubItem(apkSubCategory);
            apkSubCategory.setParentCategory(systemHeadCategory);
        }

        boolean isNetworkAvailable = Utils.isNetworkAvailable(-1);
        boolean isApkInstalled = CommonUtils.isPackageInstalled(mSecurityPackage);
        boolean isSecurityBannerShowCountLimit = JunkCleanUtils.isSecurityBannerShowCountLimit();
        boolean shouldSecurityItemVisible = false;//isNetworkAvailable && !isApkInstalled && !isSecurityBannerShowCountLimit;
        HSLog.d(TAG, "getListItems **** isNetworkAvailable = " + isNetworkAvailable + " isApkInstalled = " + isApkInstalled + " isSecurityBannerShowCountLimit = " + isSecurityBannerShowCountLimit + " shouldSecurityItemVisible = " + shouldSecurityItemVisible);
        if (shouldSecurityItemVisible) {
            final String eventLogParam = "SecurityBanner";
            HSAnalytics.logEvent("Promotion_Viewed", "Type", eventLogParam);
            SecurityItem securityItem = new SecurityItem(JunkCleanActivity.this);
            securityItem.setOnClickListener(() -> {
                HSAnalytics.logEvent("Promotion_Clicked", "Type", eventLogParam);
                PromotionTracker.startTracking(mSecurityPackage, PromotionTracker.EVENT_LOG_APP_NAME_SECURITY, true);
            });
            flexibleItems.add(securityItem);
            if (!mIsSecurityBannerShowed) {
                JunkCleanUtils.setSecurityBannerShowCount();
                mIsSecurityBannerShowed = true;
            }
        }

        boolean shouldForceCleanSystemAppCache = false;// JunkCleanUtils.shouldForceCleanSystemAppCache();
        boolean isAccessibilityGranted = PermissionUtils.isAccessibilityGranted();
        HSLog.d(TAG, "getListItems ^^^ shouldForceCleanSystemAppCache = " + shouldForceCleanSystemAppCache + " isAccessibilityGranted = " + isAccessibilityGranted);
        if (false) {
            if (!shouldSecurityItemVisible) {
                long hiddenCacheCount = systemSubCategory.getSize();
                HSLog.d(TAG, "getListItems hiddenCacheCount = " + hiddenCacheCount);
                if (hiddenCacheCount > 0) {
                    PowerfulCleanItem powerfulCleanItem = new PowerfulCleanItem(JunkCleanActivity.this);
                    powerfulCleanItem.setOnClickListener(new TopBannerBaseItem.OnClickListener() {
                        @Override
                        public void onClick() {
                            ToastUtils.showToast("这里请求Accessibility");
//                            PermissionUtils.requestAccessibilityPermission(JunkCleanActivity.this, new Runnable() {
//                                @Override
//                                public void run() {
//                                    onAccessibilityPermissionOpenSuccess();
//                                }
//                            });
                        }
                    });

                    flexibleItems.add(powerfulCleanItem);
                }
            }

            systemHeadCategory.setOnItemCheckBoxTouchedListener(new JunkHeadCategoryItem.OnItemCheckedListener() {
                @Override
                public boolean onItemChecked() {
                    if (!PermissionUtils.isAccessibilityGranted()) {
                        showDialog(new PermissionDialog(JunkCleanActivity.this));
                        return true;
                    }
                    return false;
                }
            });
            systemSubCategory.setOnItemCheckBoxTouchedListener(new JunkSubCategoryItem.OnItemCheckedListener() {
                @Override
                public boolean onItemChecked() {
                    if (!PermissionUtils.isAccessibilityGranted()) {
                        showDialog(new PermissionDialog(JunkCleanActivity.this));
                        return true;
                    }
                    return false;
                }
            });
        }

        if (systemHeadCategory.getSubItems().size() > 0) {
            flexibleItems.add(systemHeadCategory);
        }

        for (JunkSubCategoryItem item : installAppSubItems) {
            appHeadCategory.addSubItem(item);
            item.setParentCategory(appHeadCategory);
        }

        if (appHeadCategory.getSubItems().size() > 0) {
            flexibleItems.add(appHeadCategory);
        }

        if (pathRuleHeadCategory.getSubItems().size() > 0) {
            flexibleItems.add(pathRuleHeadCategory);
        }

        if (memoryHeadCategory.getSubItems().size() > 0) {
            flexibleItems.add(memoryHeadCategory);
        }

        return flexibleItems;
    }

    public void onAccessibilityPermissionOpenSuccess() {
        mJunkManager.selectSystemJunk();
        updateAdapterDataSet(getListItems());

    }

    private void updateAdapterDataSet(List<AbstractFlexibleItem> dataList) {
        if (null == dataList || dataList.size() == 0) {
            showEmptyView();
            refreshJunkSize(true);
            refreshCleanButtonStatus(true);
            return;
        }

        if (null != mFlexibleAdapter) {
            mFlexibleAdapter.updateDataSet(dataList);
        }
        refreshJunkSize(false);
        if (null != mAppBarLayout) {
            mAppBarLayout.setExpanded(true);
        }

        refreshCleanButtonStatus(true);
    }

    private void startJunkScan() {
        if (mIsJunkScanFrozen) {
            if (JunkCleanConstant.sIsTotalCleaned) {
                showEmptyView();
            } else {
                refreshBannerColor(mJunkManager.getTotalJunkSize(), false);
            }
            mIsOnScanFinished = false;
            onJunkScanFinished(true);
            return;
        }

        ResultPageAdsManager.getInstance().preloadAd();
        mIsOnScanFinished = false;
        mIsSystemScanAnimationFinished = false;
        mIsAppScanAnimationFinished = false;
        mIsAdScanAnimationFinished = false;
        mIsMemoryScanAnimationFinished = false;
        mIsTotalAnimationScanFinished = false;
        startLoadingAnimation();
        JunkCleanUtils.setIsScanCanceled(false);

        initScanItemView();
        mJunkManager.clear();
        mIsJunkScanning = true;
        mJunkManager.startJunkScan(new JunkManager.ScanJunkListener() {
            @Override
            public void onScanNameChanged(String name) {
                mScanningSelectedTextTv.setText(getString(R.string.clean_scanning, name));
            }

            @Override
            public void onScanSizeChanged(String categoryType, JunkInfo junkInfo, boolean isEnd) {
                 if (JunkCleanConstant.CATEGORY_SYSTEM_JUNK.equals(categoryType)) {
                     updateSizeProgress(mSystemJunkSizeTv, null, mSystemProgressWheel, mSystemTickIv, junkInfo, JunkCleanConstant.CATEGORY_SYSTEM_JUNK, isEnd);
                 } else if (JunkCleanConstant.CATEGORY_APP_JUNK.equals(categoryType)) {
                     updateSizeProgress(mAppJunkSizeTv, null, mAppProgressWheel, mAppTickIv, junkInfo, JunkCleanConstant.CATEGORY_APP_JUNK, isEnd);
                 } else if (JunkCleanConstant.CATEGORY_AD_JUNK.equals(categoryType)) {
                     updateSizeProgress(mAdJunkSizeTv, null, mAdProgressWheel, mAdTickIv, junkInfo, JunkCleanConstant.CATEGORY_AD_JUNK, isEnd);
                 } else if (JunkCleanConstant.CATEGORY_MEMORY_JUNK.equals(categoryType)) {
                     updateSizeProgress(mMemoryJunkSizeTv, null, mMemoryProgressWheel, mMemoryTickIv, junkInfo, JunkCleanConstant.CATEGORY_MEMORY_JUNK, isEnd);
                 }
                 if (JunkCleanConstant.CATEGORY_SPECIAL_TOTAL_JUNK.equals(categoryType)) {
                     updateSizeProgress(mScanJunkSizeTv, mScanJunkUnitTv, null, null, junkInfo, JunkCleanConstant.CATEGORY_SPECIAL_TOTAL_JUNK, true);
                 } else {
                     updateSizeProgress(mScanJunkSizeTv, mScanJunkUnitTv, null, null, junkInfo, JunkCleanConstant.CATEGORY_SPECIAL_TOTAL_JUNK, false);
                 }
            }

            @Override
            public void onScanFinished(long junkSize) {
                HSLog.d(TAG, "startJunkScan ^^^ onScanFinished ^^^ junkSize = " + junkSize);
                JunkCleanUtils.setLastJunkScanFinishTime();
            }
        });
    }

    private void updateSizeProgress(final TextView sizeTv, final TextView unitTv, final ProgressWheel progressWheel,
                                    final ImageView tickIv, final JunkInfo junkInfo, final String categoryType, final boolean isEnd) {
        if (mIsOnScanFinished) {
            return;
        }

        long duration = isEnd ? 2000 : DURATION_JUNK_SIZE_SCAN_ANIM;
        HSLog.d(TAG, "updateSizeProgress category = " + categoryType + " isEnd = " + isEnd);

        switch (categoryType) {
            case JunkCleanConstant.CATEGORY_SPECIAL_TOTAL_JUNK:
                final long totalJunkSize = junkInfo.getTotalJunkSize();
                if (totalJunkSize > mCurrentTotalJunkSize) {
                    if (mTotalJunkSizeAnimator != null) {
                        mTotalJunkSizeAnimator.cancel();
                        mTotalJunkSizeAnimator.removeAllUpdateListeners();
                        mTotalJunkSizeAnimator.removeAllListeners();
                    }
                    mTotalJunkSizeAnimator = ValueAnimator.ofObject(new LongEvaluator(), mCurrentTotalJunkSize, totalJunkSize);
                    mTotalJunkSizeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            mCurrentTotalJunkSize = (long) animation.getAnimatedValue();
                            setProgressText(mCurrentTotalJunkSize, sizeTv, unitTv);
                            if (mCurrentTotalJunkSize == totalJunkSize) {
                                onSingleScanFinished(progressWheel, tickIv, junkInfo, categoryType, isEnd);
                            }
                        }
                    });
                    mTotalJunkSizeAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            onSingleScanFinished(progressWheel, tickIv, junkInfo, categoryType, isEnd);
                        }
                    });
                    mTotalJunkSizeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                    mTotalJunkSizeAnimator.setDuration(duration).start();
                }

                if ((null == mTotalJunkSizeAnimator || totalJunkSize == mCurrentTotalJunkSize)) {
                    onSingleScanFinished(progressWheel, tickIv, junkInfo, categoryType, true);
                }
                refreshBannerColor(totalJunkSize, true);
                break;
            case JunkCleanConstant.CATEGORY_SYSTEM_JUNK:
                final long totalSystemJunkSize = junkInfo.getSystemJunkSize() + junkInfo.getApkJunkSize();
                if (totalSystemJunkSize > mCurrentSystemJunkSize) {
                    if (mSystemJunkSizeAnimator != null) {
                        mSystemJunkSizeAnimator.cancel();
                        mSystemJunkSizeAnimator.removeAllUpdateListeners();
                        mSystemJunkSizeAnimator.removeAllListeners();
                    }
                    mSystemJunkSizeAnimator = ValueAnimator.ofObject(new LongEvaluator(), mCurrentSystemJunkSize, totalSystemJunkSize);
                    mSystemJunkSizeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            mCurrentSystemJunkSize = (long) animation.getAnimatedValue();
                            setProgressText(mCurrentSystemJunkSize, sizeTv, unitTv);
                            if (mCurrentSystemJunkSize == totalSystemJunkSize) {
                                onSingleScanFinished(progressWheel, tickIv, junkInfo, categoryType, isEnd);
                            }
                        }
                    });
                    mSystemJunkSizeAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            onSingleScanFinished(progressWheel, tickIv, junkInfo, categoryType, isEnd);
                        }
                    });
                    mSystemJunkSizeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                    mSystemJunkSizeAnimator.setDuration(duration).start();
                }

                if ((null == mSystemJunkSizeAnimator || totalSystemJunkSize == mCurrentSystemJunkSize) && isEnd) {
                    onSingleScanFinished(progressWheel, tickIv, junkInfo, categoryType, true);
                }
                break;
            case JunkCleanConstant.CATEGORY_APP_JUNK:
                final long totalAppJunkSize = junkInfo.getAppJunkSize();
                if (totalAppJunkSize > mCurrentAppJunkSize) {
                    if (mAppJunkSizeAnimator != null) {
                        mAppJunkSizeAnimator.cancel();
                        mAppJunkSizeAnimator.removeAllUpdateListeners();
                        mAppJunkSizeAnimator.removeAllListeners();
                    }
                    mAppJunkSizeAnimator = ValueAnimator.ofObject(new LongEvaluator(), mCurrentAppJunkSize, totalAppJunkSize);
                    mAppJunkSizeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            mCurrentAppJunkSize = (long) animation.getAnimatedValue();
                            setProgressText(mCurrentAppJunkSize, sizeTv, unitTv);
                            if (mCurrentAppJunkSize == totalAppJunkSize) {
                                onSingleScanFinished(progressWheel, tickIv, junkInfo, categoryType, isEnd);
                            }
                        }
                    });
                    mAppJunkSizeAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            onSingleScanFinished(progressWheel, tickIv, junkInfo, categoryType, isEnd);
                        }
                    });
                    mAppJunkSizeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                    mAppJunkSizeAnimator.setDuration(duration).start();
                }

                if ((null == mAppJunkSizeAnimator || totalAppJunkSize == mCurrentAppJunkSize) && isEnd) {
                    onSingleScanFinished(progressWheel, tickIv, junkInfo, categoryType, true);
                }
                break;
            case JunkCleanConstant.CATEGORY_AD_JUNK:
                final long totalAdJunkSize = junkInfo.getPathFileJunkSize();
                if (totalAdJunkSize > mCurrentAdJunkSize) {
                    if (mAdJunkSizeAnimator != null) {
                        mAdJunkSizeAnimator.cancel();
                        mAdJunkSizeAnimator.removeAllUpdateListeners();
                        mAdJunkSizeAnimator.removeAllListeners();
                    }
                    mAdJunkSizeAnimator = ValueAnimator.ofObject(new LongEvaluator(), mCurrentAdJunkSize, totalAdJunkSize);
                    mAdJunkSizeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            mCurrentAdJunkSize = (long) animation.getAnimatedValue();
                            setProgressText(mCurrentAdJunkSize, sizeTv, unitTv);
                            if (mCurrentAdJunkSize == totalAdJunkSize) {
                                onSingleScanFinished(progressWheel, tickIv, junkInfo, categoryType, isEnd);
                            }
                        }
                    });
                    mAdJunkSizeAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            onSingleScanFinished(progressWheel, tickIv, junkInfo, categoryType, isEnd);
                        }
                    });
                    mAdJunkSizeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                    mAdJunkSizeAnimator.setDuration(duration).start();
                }

                if ((null == mAdJunkSizeAnimator || totalAdJunkSize == mCurrentAdJunkSize) && isEnd) {
                    onSingleScanFinished(progressWheel, tickIv, junkInfo, categoryType, true);
                }
                break;
            case JunkCleanConstant.CATEGORY_MEMORY_JUNK:
                final long totalMemoryJunkSize = junkInfo.getMemoryJunkSize();
                if (totalMemoryJunkSize > mCurrentMemoryJunkSize) {
                    if (mMemoryJunkSizeAnimator != null) {
                        mMemoryJunkSizeAnimator.cancel();
                        mMemoryJunkSizeAnimator.removeAllUpdateListeners();
                        mMemoryJunkSizeAnimator.removeAllListeners();
                    }
                    mMemoryJunkSizeAnimator = ValueAnimator.ofObject(new LongEvaluator(), mCurrentMemoryJunkSize, totalMemoryJunkSize);
                    mMemoryJunkSizeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            mCurrentMemoryJunkSize = (long) animation.getAnimatedValue();
                            setProgressText(mCurrentMemoryJunkSize, sizeTv, unitTv);
                            if (mCurrentMemoryJunkSize == totalMemoryJunkSize) {
                                onSingleScanFinished(progressWheel, tickIv, junkInfo, categoryType, isEnd);
                            }
                        }
                    });
                    mMemoryJunkSizeAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            onSingleScanFinished(progressWheel, tickIv, junkInfo, categoryType, isEnd);
                        }
                    });
                    mMemoryJunkSizeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                    mMemoryJunkSizeAnimator.setDuration(duration).start();
                }

                if ((null == mMemoryJunkSizeAnimator || totalMemoryJunkSize == mCurrentMemoryJunkSize) && isEnd) {
                    onSingleScanFinished(progressWheel, tickIv, junkInfo, categoryType, true);
                }
                break;
        }
    }

    private void onSingleScanFinished(final ProgressWheel progressWheel, final ImageView tickIv, final JunkInfo junkInfo,
                                      final String categoryType, final boolean isAnimationEnd) {
        if (isAnimationEnd) {
            boolean isItemScanFinished = false;
            int finishCount = 0;

            switch (categoryType) {
                case JunkCleanConstant.CATEGORY_SPECIAL_TOTAL_JUNK:
                    if (mIsTotalAnimationScanFinished) {
                        return;
                    }
                    mIsTotalAnimationScanFinished = mIsSystemScanAnimationFinished && mIsAppScanAnimationFinished && mIsAdScanAnimationFinished && mIsMemoryScanAnimationFinished;
                    isItemScanFinished = true;
                    break;
                case JunkCleanConstant.CATEGORY_SYSTEM_JUNK:
                    if (mIsSystemScanAnimationFinished) {
                        return;
                    }
                    mIsSystemScanAnimationFinished = true;
                    isItemScanFinished = true;
                    break;
                case JunkCleanConstant.CATEGORY_APP_JUNK:
                    if (mIsAppScanAnimationFinished) {
                        return;
                    }
                    mIsAppScanAnimationFinished = true;
                    isItemScanFinished = true;
                    break;
                case JunkCleanConstant.CATEGORY_AD_JUNK:
                    if (mIsAdScanAnimationFinished) {
                        return;
                    }
                    mIsAdScanAnimationFinished = true;
                    isItemScanFinished = true;
                    break;
                case JunkCleanConstant.CATEGORY_MEMORY_JUNK:
                    if (mIsMemoryScanAnimationFinished) {
                        return;
                    }
                    mIsMemoryScanAnimationFinished = true;
                    isItemScanFinished = true;
                    break;
            }

            HSLog.d(TAG, "startJunkScan +++ category = " + categoryType + " mIsTotalAnimationScanFinished = " + mIsTotalAnimationScanFinished  + " mIsSystemScanAnimationFinished = " + mIsSystemScanAnimationFinished
                    + " mIsAppScanAnimationFinished = " + mIsAppScanAnimationFinished + " mIsAdScanAnimationFinished = " + mIsAdScanAnimationFinished + " mIsMemoryScanAnimationFinished = " + mIsMemoryScanAnimationFinished);

            if (mIsSystemScanAnimationFinished) {
                finishCount++;
            }
            if (mIsAppScanAnimationFinished) {
                finishCount++;
            }
            if (mIsAdScanAnimationFinished) {
                finishCount++;
            }
            if (mIsMemoryScanAnimationFinished) {
                finishCount++;
            }

            if (isItemScanFinished) {
                appearTickAndGoneProgressWheel(progressWheel, tickIv);
            }

            if (mIsTotalAnimationScanFinished && mIsSystemScanAnimationFinished && mIsAppScanAnimationFinished && mIsAdScanAnimationFinished && mIsMemoryScanAnimationFinished) {
                onJunkScanFinished(false);
            } else if (!mIsTotalAnimationScanFinished && mIsSystemScanAnimationFinished && mIsAppScanAnimationFinished && mIsAdScanAnimationFinished && mIsMemoryScanAnimationFinished) {
                if (mProgressAnimator != null) {
                    mProgressAnimator.postValue(95);
                }
                updateSizeProgress(mScanJunkSizeTv, mScanJunkUnitTv, null, null, junkInfo, JunkCleanConstant.CATEGORY_SPECIAL_TOTAL_JUNK, true);
            } else if (finishCount >= 3) {
                if (mProgressAnimator != null) {
                    mProgressAnimator.postValue(95);
                }
            }
        } else {
            if (JunkCleanConstant.CATEGORY_SPECIAL_TOTAL_JUNK.equals(categoryType) && mIsSystemScanAnimationFinished  && mIsAppScanAnimationFinished && mIsAdScanAnimationFinished && mIsMemoryScanAnimationFinished) {
                mIsTotalAnimationScanFinished = true;
                onJunkScanFinished(false);
            }
        }
    }

    private void appearTickAndGoneProgressWheel(final ProgressWheel progressWheel,
                                                final ImageView tickIv) {
        if (null != progressWheel) {
            progressWheel.stopSpinning();
            progressWheel.setVisibility(View.INVISIBLE);
        }
        if (null != tickIv) {
            tickIv.setVisibility(View.VISIBLE);
        }
    }

    private void setProgressText(long junkSize, TextView sizeTv, TextView unitTv) {
        FormatSizeBuilder totalSizeBuilder = new FormatSizeBuilder(junkSize);
        if (null != sizeTv) {
            sizeTv.setText(null == unitTv ? String.valueOf(totalSizeBuilder.size + totalSizeBuilder.unit) : String.valueOf(totalSizeBuilder.size));
        }
        if (null != unitTv) {
            unitTv.setText(String.valueOf(totalSizeBuilder.unit));
        }
    }

    private void onJunkScanFinished(boolean isJunkScanFrozen) {
        HSLog.d(TAG, "onScanFinished === isJunkScanFrozen = " + isJunkScanFrozen + " mIsOnScanFinished = " + mIsOnScanFinished);
        if (mIsOnScanFinished) {
            return;
        }

        if (isJunkScanFrozen) {
            refreshJunkSelectedSizeText();
        }

        mIsOnScanFinished = true;
        mIsJunkScanning = false;
        mScanningSelectedTextTv.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isJunkScanFrozen) {
                    refreshJunkSelectedSizeText();
                    setActionButtonTranslation(true, true);
                    if (mProgressAnimator != null) {
                        mProgressAnimator.postValue(100);
                        stopLoadingAnimation(200);
                    }
                    if (null != mFlexibleAdapter) {
                        updateAdapterDataSet(getListItems());
                    }
                }
            }
        }, DELAY_SCAN_FINISHED);
    }

    public void setActionButtonTranslation(final boolean isAppear, boolean animated) {
        if (mIsActionBtnAnimating) {
            if (isAppear == mIsCleanButtonAppearStatus) {
                return;
            } else {
                if (null != mActionBtnAnimator) {
                    mActionBtnAnimator.cancel();
                }
            }
        }

        mIsCleanButtonAppearStatus = isAppear;

        if (isAppear && mCleanButtonTv.getVisibility() == View.VISIBLE) {
            return;
        }

        final float startTranslation = isAppear ? mTotalTranslation : 0;
        final float toTranslation = isAppear ? 0 : mTotalTranslation;

        mCleanButtonTv.setTranslationY(startTranslation);
        boolean downward = (toTranslation > startTranslation);
        mCleanButtonTv.setVisibility(isAppear ? View.VISIBLE : View.GONE);

        if (null != mActionBtnAnimator) {
            mActionBtnAnimator.cancel();
        }

        if (animated) {
            mActionBtnAnimator = mCleanButtonTv.animate()
                    .translationY(toTranslation)
                    .setDuration(LauncherAnimUtils.getShortAnimDuration() * (downward ? 1 : 6))
                    .setInterpolator(downward ? LauncherAnimUtils.ACCELERATE_QUAD : new SpringInterpolator(0.3f))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            mIsActionBtnAnimating = true;
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mIsActionBtnAnimating = false;
                            mCleanButtonTv.setTranslationY(toTranslation);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            super.onAnimationCancel(animation);
                            mIsActionBtnAnimating = false;
                        }
                    });
            mActionBtnAnimator.start();
        } else {
            mCleanButtonTv.setTranslationY(toTranslation);
        }
    }

    private void showEmptyView() {
        View emptyView = findViewById(R.id.empty_view);
        HSLog.d(TAG, "showEmptyView mIsJunkScanning = " + mIsJunkScanning);
        if (mIsJunkScanning || (null != emptyView && emptyView.getVisibility() == View.VISIBLE)) {
            return;
        }
        mRecyclerView.setVisibility(View.GONE);
        if (emptyView == null) {
            ViewStub emptyViewStub = (ViewStub) findViewById(R.id.empty_view_stub);
            assert emptyViewStub != null;
            emptyView = emptyViewStub.inflate();
        }
        emptyView.setVisibility(View.VISIBLE);
        if(emptyView instanceof ResultEmptyView){
            ((ResultEmptyView) emptyView).setType(ResultEmptyView.TYPE_JUNK_CLEAN);
            ((ResultEmptyView) emptyView).startPromotionEvaluation();
        }

        setActionButtonTranslation(false, false);
        mScanningSelectedTextTv.setText(getString(R.string.boost_plus_selected_size_text, "0 B"));
        refreshBannerColor(0, false);
        refreshJunkSize(true);
    }

    @Override
    protected boolean registerCloseSystemDialogsReceiver() {
        return true;
    }

}
