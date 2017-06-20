package com.ihs.feature.cpucooler;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.commons.utils.HSLog;
import com.ihs.feature.common.ActivityUtils;
import com.ihs.feature.common.LauncherPackageManager;
import com.ihs.feature.common.Utils;
import com.ihs.feature.cpucooler.recycleitem.CpuListHeadItem;
import com.ihs.feature.cpucooler.recycleitem.CpuListSubItem;
import com.ihs.feature.cpucooler.util.CpuCoolerConstant;
import com.ihs.feature.cpucooler.util.CpuCoolerUtils;
import com.ihs.keyboardutils.R;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

public class CpuCoolerCleanActivity extends HSAppCompatActivity {

    public static final String EXTRA_KEY_APP_LIST = "EXTRA_KEY_APP_LIST";

    private static final long DURATION_APPBAR_TRANSLATE = 300;
    private static final long DURATION_FADE_OUT = 225;

    private AppBarLayout mAppBarLayout;
    private RecyclerView mRecyclerView;
    private View mRootLayout;
    private Toolbar mToolbar;
    private LinearLayout mAppBarLayoutContentLayout;
    private RelativeLayout mTemperatureRl;
    private TextView mTemperatureTv;
    private TextView mTemperatureQuantifierTv;
    private TextView mCpuStateTv;
    private TextView mOptimizeButtonTv;

    private CpuListHeadItem mCpuListHeadItem;
    private FlexibleAdapter mFlexibleAdapter;

    private List<AbstractFlexibleItem> mCpuDetailItemList = new ArrayList<>();
    private int mCurrentColor;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpu_clean);

        initView();
        initRecycleView();

        mAppBarLayout.addOnOffsetChangedListener((mAppBarLayoutLayout, verticalOffset) -> {
            float delta = 1 - (float) Math.abs(verticalOffset) / mAppBarLayoutLayout.getTotalScrollRange() * 2;
            mTemperatureRl.setScaleX(delta);
            mTemperatureRl.setScaleY(delta);
            mTemperatureRl.setAlpha(delta);
            mCpuStateTv.setAlpha(delta);
        });

        mOptimizeButtonTv.setOnClickListener(v -> startFadeOutAnimation());
    }

    @SuppressLint("NewApi")
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        startFadeInAnimation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        CpuCoolerManager.getInstance().setScannedApp(getIntent().getStringArrayListExtra(EXTRA_KEY_APP_LIST));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startFadeInAnimation() {
        mAppBarLayoutContentLayout.postDelayed(() -> {
            final int height = DisplayUtils.getScreenHeightPixels() - getResources().getDimensionPixelSize(R.dimen.cpu_appbar_height);
            int temperature = CpuCoolerManager.getInstance().fetchCpuTemperature();
            mCurrentColor = CpuCoolerUtils.getCpuTemperatureColor();

            ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), ContextCompat.getColor(CpuCoolerCleanActivity.this, R.color.cpu_cooler_primary_blue), mCurrentColor);
            colorAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mAppBarLayoutContentLayout.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mTemperatureTv.setText(String.valueOf(temperature));
                    mTemperatureQuantifierTv.setText(getString(R.string.cpu_cooler_temperature_quantifier_celsius));
                    mCpuStateTv.setText(CpuCoolerUtils.getCpuTemperatureStateText(CpuCoolerCleanActivity.this));
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mOptimizeButtonTv.setVisibility(View.VISIBLE);
                    mAppBarLayout.setBackgroundColor(mCurrentColor);
                    mRootLayout.setBackgroundColor(Color.WHITE);
                    HSAnalytics.logEvent("CPUCooler_ScanResult_Show", "Type", CpuCoolerUtils.getTemperatureColorText(temperature));
                }
            });

            colorAnimator.addUpdateListener(animation -> {
                float fraction = animation.getAnimatedFraction();
                mRecyclerView.setTranslationY((1 - fraction) * height);
                mAppBarLayoutContentLayout.setTranslationY((1 - fraction) * height);
                mRootLayout.setBackgroundColor((int) animation.getAnimatedValue());
            });

            colorAnimator.setDuration(DURATION_APPBAR_TRANSLATE).start();
        }, CpuCoolerConstant.DELAY_ANIMATION_FADE_IN);
    }

    private void startFadeOutAnimation() {
        HSAnalytics.logEvent("CPUCooler_ScanResult_BtnClicked");
        final int height = getResources().getDimensionPixelSize(R.dimen.cpu_appbar_height);
        ValueAnimator fadeOutAnimator = ValueAnimator.ofFloat(0, 1);
        fadeOutAnimator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            mAppBarLayout.setTranslationY(-fraction * height);
            mAppBarLayout.setAlpha(1 - fraction);
            mRecyclerView.setTranslationY(-fraction * 50);
            mRecyclerView.setAlpha(1 - fraction);
            mOptimizeButtonTv.setAlpha(1 - fraction);
            mToolbar.setAlpha(1 - fraction);
        });
        fadeOutAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                List<String> selectedApps = new ArrayList<>();
                for (CpuListSubItem item : mCpuListHeadItem.getSubItems()) {
                    if (item.getCheckStatus()) {
                        selectedApps.add(item.packageName);
                    }
                }
                Intent intent = new Intent(CpuCoolerCleanActivity.this, CpuCoolDownActivity.class);
                intent.putStringArrayListExtra(CpuCoolDownActivity.EXTRA_KEY_SELECTED_APP_LIST, (ArrayList<String>) selectedApps);
                startActivity(intent);
                overridePendingTransition(R.anim.anim_null, R.anim.anim_null);
                finish();
            }
        });
        fadeOutAnimator.setDuration(DURATION_FADE_OUT).start();
    }

    private void initView() {
        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        mRootLayout = findViewById(R.id.view_container);
        mAppBarLayoutContentLayout = (LinearLayout) findViewById(R.id.app_bar_content_layout);
        mTemperatureRl = (RelativeLayout) findViewById(R.id.temperature_layout);
        mTemperatureTv = (TextView) findViewById(R.id.tv_temperature);
        mTemperatureQuantifierTv = (TextView) findViewById(R.id.tv_temperature_quantifier);
        mCpuStateTv = (TextView) findViewById(R.id.tv_cpu_state_hint);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        mOptimizeButtonTv = (TextView) findViewById(R.id.optimize_button_tv);

        mToolbar = (Toolbar) findViewById(R.id.action_bar);
        ActivityUtils.configSimpleAppBar(this, getString(R.string.promotion_max_card_title_cpu_cooler), Color.TRANSPARENT);

        Utils.setupTransparentSystemBarsForLmpNoNavigation(this);
        ActivityUtils.setNavigationBarColor(this, ContextCompat.getColor(this, android.R.color.black));
    }

    private void initRecycleView() {
        mCpuListHeadItem = new CpuListHeadItem(isAllUnselected -> {
        });

        List<String> packageNameList = getIntent().getStringArrayListExtra(EXTRA_KEY_APP_LIST);
        if (null != packageNameList) {
            HSLog.d(CpuCoolerScanActivity.TAG, "Cpu detail packageNameList size = " + packageNameList.size());
            for (String packageName : packageNameList) {
                ApplicationInfo applicationInfo = LauncherPackageManager.getInstance().getApplicationInfo(packageName);
                if (applicationInfo == null) {
                    continue;
                }
                String appName = LauncherPackageManager.getInstance().getApplicationLabel(applicationInfo);
                Drawable icon = LauncherPackageManager.getInstance().getApplicationIcon(applicationInfo);
                CpuListSubItem cpuListSubItem = new CpuListSubItem(packageName, appName, icon);
                mCpuListHeadItem.addSubItem(cpuListSubItem);
                cpuListSubItem.setHeader(mCpuListHeadItem);
            }
        }
        mCpuDetailItemList.add(mCpuListHeadItem);

        mFlexibleAdapter = new FlexibleAdapter(mCpuDetailItemList);
        mFlexibleAdapter.setAnimationOnScrolling(false);
        mFlexibleAdapter.expandItemsAtStartUp().setAnimationDuration(375).setAnimationOnScrolling(true)
                .setAnimationInterpolator(new FastOutSlowInInterpolator());

        mRecyclerView.setLayoutManager(new SmoothScrollLinearLayoutManager(CpuCoolerCleanActivity.this));
        mRecyclerView.setAdapter(mFlexibleAdapter);

        mRecyclerView.setOnTouchListener((v, event) -> {
            mFlexibleAdapter.setAnimationOnScrolling(false);
            return false;
        });
    }

}
