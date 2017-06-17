package com.ihs.feature.cpucooler;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.honeycomb.launcher.R;
import com.honeycomb.launcher.cpucooler.util.CpuCoolerUtils;
import com.honeycomb.launcher.cpucooler.util.CpuPreferenceHelper;
import com.honeycomb.launcher.cpucooler.view.CpuScanTwinkleView;
import com.honeycomb.launcher.ihs.BaseCenterActivity;
import com.honeycomb.launcher.notification.NotificationManager;
import com.honeycomb.launcher.resultpage.ResultPageActivity;
import com.honeycomb.launcher.util.ActivityUtils;
import com.honeycomb.launcher.util.CommonUtils;
import com.honeycomb.launcher.util.LauncherPackageManager;
import com.honeycomb.launcher.util.ViewUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.device.clean.memory.HSAppMemory;
import com.ihs.device.clean.memory.HSAppMemoryManager;

import java.util.ArrayList;
import java.util.List;

public class CpuCoolerScanActivity extends BaseCenterActivity {

    public static final String TAG = "CpuCoolerLog";

    private static final long DURATION_CPU_SCAN_ONCE = 2000;
    private static final long DURATION_ALPHA_OUT = 400;
    private static final int SHADOW_ROTATION = 40;
    private static final float SHADOW_WIDTH_DIVIDE_HEIGHT = 0.5f;
    private static final long TIME_OUT = 10000;

    private ImageView mScanLineIv;
    private RelativeLayout mCpuWithFeetLayout;
    private ImageView mCpuShadowIv;
    private ImageView mShadowMaskBlueIv;
    private RelativeLayout mTopLayerRl;
    private RelativeLayout mAnimationRl;
    private ImageView mScanMaskWhiteIv;
    private ImageView mThermometerBodyIv;
    private ImageView mThermometerTopIv;
    private TextView mScanHintTv;
    private CpuScanTwinkleView mCpuScanTwinkleView;

    private ObjectAnimator mScanLineIvAnimator;

    private float mThermometerStretchHeight;
    private float mStretchScale;

    private boolean mIsScanFinished = false;
    private boolean mIsVisible = false;
    private boolean mIsLeaveAfterScan;

    private List<String> mAppPackageNameList = new ArrayList<>();
    private HSAppMemoryManager.MemoryTaskListener mScanListener;
    private Handler mHandler = new Handler();

    private final int mAvailableHeight = CommonUtils.getPhoneHeight(HSApplication.getContext())
            - CommonUtils.getStatusBarHeight(HSApplication.getContext())
            - CommonUtils.getNavigationBarHeight(HSApplication.getContext());
    private final int mScreenWidth = CommonUtils.getPhoneWidth(HSApplication.getContext());


    @Override
    public boolean isEnableNotificationActivityFinish() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpu_scan);
        initView();
        boolean isFrozen = CpuCoolerUtils.isCpuCoolerScanFrozen();
        if (isFrozen) {
            startDoneActivity();
            return;
        }
        initShadowParams();
        new Handler().postDelayed(this::startScanLineAnimation, 200);
        startScanApp();
        LauncherPackageManager.getInstance();
    }

    private void initView() {
        mScanLineIv = (ImageView) findViewById(R.id.iv_scan_line);
        mCpuWithFeetLayout = (RelativeLayout) findViewById(R.id.layout_cpu_with_feet);
        mCpuShadowIv = (ImageView) findViewById(R.id.iv_cpu_shadow);
        mShadowMaskBlueIv = (ImageView) findViewById(R.id.iv_shadow_mask_blue);
        mTopLayerRl = (RelativeLayout) findViewById(R.id.layout_top_layer);
        mAnimationRl = (RelativeLayout) findViewById(R.id.scan_animation_layout);
        mScanMaskWhiteIv = (ImageView) findViewById(R.id.iv_scan_mask_white);
        mThermometerBodyIv = (ImageView) findViewById(R.id.iv_thermometer_inner_body);
        mThermometerTopIv = (ImageView) findViewById(R.id.iv_thermometer_inner_top);
        mScanHintTv = (TextView) findViewById(R.id.scanning_hint_tv);
        mCpuScanTwinkleView = (CpuScanTwinkleView) findViewById(R.id.cpu_scan_view);

        float cpuWidth = getResources().getFraction(R.fraction.cpu_width, mScreenWidth, 1);
        float cpuHeight = cpuWidth / getResources().getFraction(R.fraction.cpu_without_feet_width_divide_height, 1, 1);
        mThermometerStretchHeight = getResources().getFraction(R.fraction.thermometer_stretch_height, (int) cpuHeight, 1);
        mStretchScale = mThermometerStretchHeight / getResources().getFraction(R.fraction.thermometer_max_height, (int) cpuHeight, 1);
        mThermometerTopIv.setTranslationY(mThermometerStretchHeight);
        mThermometerBodyIv.setScaleY(1f - mStretchScale);
        mThermometerBodyIv.setTranslationY(mThermometerStretchHeight / 2);
    }

    @SuppressLint("NewApi")
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        CommonUtils.setupTransparentSystemBarsForLmp(this);
        View viewContainer = ViewUtils.findViewById(this, R.id.view_container);
        viewContainer.setPadding(0, CommonUtils.getStatusBarHeight(this), 0, 0);
        ActivityUtils.setNavigationBarColor(this, ContextCompat.getColor(this, android.R.color.black));
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

    @Override
    protected void onStart() {
        super.onStart();
        mIsVisible = true;
        if (mIsScanFinished) {
            onScanFinished();
        }
    }

    @Override
    protected void onStop() {
        mIsVisible = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (null != mScanLineIvAnimator) {
            mScanLineIvAnimator.cancel();
        }
        mCpuScanTwinkleView.stopTwinkle();
        HSAppMemoryManager.getInstance().stopScan(mScanListener);
        CpuPreferenceHelper.setIsScanCanceled(!mIsScanFinished);
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void initShadowParams() {
        float cpuWidth = getResources().getFraction(R.fraction.cpu_width, mScreenWidth, 1);
        float cpuHeight = cpuWidth / getResources().getFraction(R.fraction.cpu_without_feet_width_divide_height, 1, 1);
        float feetHeight = (cpuHeight - cpuWidth) / 2;

        double angle = Math.PI * SHADOW_ROTATION / 180;
        double topExtraHeight = cpuWidth / Math.tan(angle) - cpuWidth;
        double shadowHeight = cpuWidth / Math.sin(angle) - topExtraHeight * Math.cos(angle);

        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) mCpuShadowIv.getLayoutParams();
        rlp.height = (int) shadowHeight;
        rlp.width = (int) (shadowHeight * SHADOW_WIDTH_DIVIDE_HEIGHT);
        mCpuShadowIv.setLayoutParams(rlp);

        mShadowMaskBlueIv.setPivotY(cpuHeight);
        mShadowMaskBlueIv.setScaleY((float) (cpuHeight + topExtraHeight) / cpuHeight);
        mShadowMaskBlueIv.setTranslationY(-feetHeight);

        mCpuShadowIv.setPivotX(0);
        mCpuShadowIv.setPivotY(rlp.height);
        mCpuShadowIv.setRotation(SHADOW_ROTATION);
        mCpuShadowIv.setTranslationY(-feetHeight);
    }

    private void startScanLineAnimation() {
        final float scanAreaHeight = getResources().getFraction(R.fraction.cpu_scan_area_height, mAvailableHeight, 1);
        final int maskRiseHeight = getResources().getDimensionPixelOffset(R.dimen.cpu_mask_rise_height);
        final int lineAlphaHeight = getResources().getDimensionPixelOffset(R.dimen.cpu_line_alpha_height);
        mScanLineIvAnimator = ObjectAnimator.ofFloat(mScanLineIv, "translationY", scanAreaHeight, 0);
        mScanLineIvAnimator.setInterpolator(PathInterpolatorCompat.create(0.5f, 0, 0.19f, 1));
        mScanLineIvAnimator.setDuration(DURATION_CPU_SCAN_ONCE);
        mScanLineIvAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mScanLineIvAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mScanLineIvAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mTopLayerRl.setTranslationY(scanAreaHeight);
                mCpuWithFeetLayout.setTranslationY(-scanAreaHeight);
                mScanMaskWhiteIv.setTranslationY(-scanAreaHeight);
                mTopLayerRl.setVisibility(View.VISIBLE);
                mScanLineIv.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mIsVisible) {
                    mScanLineIv.setVisibility(View.INVISIBLE);
                    mTopLayerRl.setVisibility(View.INVISIBLE);
                    animToDetailActivity();
                }
            }
        });

        mScanLineIvAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            private boolean isLineDownward = false;
            private boolean hasReversed = false;
            private int animationCount = 0;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                float fraction = animation.getAnimatedFraction();
                mTopLayerRl.setTranslationY(value);
                mCpuWithFeetLayout.setTranslationY(-value);

                float mScanLineIvHeight = scanAreaHeight - value;
                mScanLineIv.setAlpha(mScanLineIvHeight < lineAlphaHeight ? mScanLineIvHeight / lineAlphaHeight : 1);

                if (!hasReversed && ((fraction < 0.01 && isLineDownward) || (fraction > 0.99 && !isLineDownward))) {
                    isLineDownward = !isLineDownward;
                    hasReversed = true;
                    if (!isLineDownward) {
                        animationCount++;
                    }
                    if (animationCount >= 1 && mIsScanFinished) {
                        mScanLineIvAnimator.end();
                    }
                }
                if (0.01 < fraction && fraction < 0.99) {
                    hasReversed = false;
                }

                mScanMaskWhiteIv.setTranslationY(-value - (isLineDownward ? maskRiseHeight : fraction * maskRiseHeight));

                if (animationCount < 1) {
                    if (isLineDownward) {
                        mThermometerTopIv.setTranslationY(fraction * mThermometerStretchHeight / 2);
                        mThermometerBodyIv.setScaleY(1f - fraction * mStretchScale / 2);
                        mThermometerBodyIv.setTranslationY(fraction * mThermometerStretchHeight / 4);
                    } else {
                        mThermometerTopIv.setTranslationY((1 - fraction / 2) * mThermometerStretchHeight);
                        mThermometerBodyIv.setScaleY(1f - mStretchScale + fraction * mStretchScale / 2);
                        mThermometerBodyIv.setTranslationY((1 - fraction / 2) * mThermometerStretchHeight / 2);
                    }
                } else {
                    mThermometerTopIv.setTranslationY(0);
                    mThermometerBodyIv.setScaleY(1);
                    mThermometerBodyIv.setTranslationY(0);
                }
            }
        });
        mScanLineIvAnimator.start();
        mCpuScanTwinkleView.startTwinkle();

        mHandler.postDelayed(() -> {
            if (!mIsLeaveAfterScan) {
                onScanFinished();
            }
        }, TIME_OUT);
    }

    private void startScanApp() {
        CpuPreferenceHelper.setIsScanCanceled(false);
        ArrayList<String> excludeList = new ArrayList<>();
        excludeList.addAll(CpuCoolerUtils.getLauncherAppList());
        excludeList.addAll(CpuCoolerUtils.getKeyBoardAppList());

        HSAppMemoryManager.getInstance().setGlobalScanExcludeList(excludeList);
        mScanListener = new HSAppMemoryManager.MemoryTaskListener() {
            @Override
            public void onStarted() {
            }

            @Override
            public void onProgressUpdated(int processCount, int total, HSAppMemory hsAppMemory) {
            }

            @Override
            public void onSucceeded(List<HSAppMemory> list, long totalScannedSize) {
                mIsScanFinished = true;
                mAppPackageNameList.clear();
                if (null != list) {
                    HSLog.d(TAG, "Cpu scan list size = " + list.size());
                    for (HSAppMemory app : list) {
                        String packageName = app.getPackageName();
                        mAppPackageNameList.add(packageName);
                    }
                }
                NotificationManager.getInstance().autoUpdateCpuCoolerTemperature();
            }

            @Override
            public void onFailed(int i, String s) {
                mIsScanFinished = true;
            }
        };

        HSAppMemoryManager.getInstance().startScanWithoutProgress(mScanListener);
    }

    private void animToDetailActivity() {
        ValueAnimator alphaAnimator = ValueAnimator.ofFloat(1, 0);
        alphaAnimator.addUpdateListener(animation -> {
            mAnimationRl.setAlpha((float) animation.getAnimatedValue());
            mScanHintTv.setAlpha((float) animation.getAnimatedValue());
        });
        alphaAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mIsVisible) {
                    onScanFinished();
                }
            }
        });
        alphaAnimator.setDuration(DURATION_ALPHA_OUT).start();
    }

    private void onScanFinished() {
        int appSize = mAppPackageNameList.size();
        mIsLeaveAfterScan = true;
        HSLog.d(TAG, "Cpu scan startDetailActivity list appSize = " + appSize);
        if (appSize == 0) {
            startDoneActivity();
        } else {
            startDetailActivity();
        }
    }

    private void startDetailActivity() {
        HSLog.d(TAG, "Cpu scan startDetailActivity");
        Intent intent = new Intent(CpuCoolerScanActivity.this, CpuCoolerCleanActivity.class);
        intent.putStringArrayListExtra(CpuCoolerCleanActivity.EXTRA_KEY_APP_LIST, (ArrayList<String>) mAppPackageNameList);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_null, R.anim.anim_null);
        finish();
    }

    private void startDoneActivity() {
        HSLog.d(TAG, "Cpu scan startDoneActivity");
        ResultPageActivity.startForCpuCooler(CpuCoolerScanActivity.this);
        finish();
    }

}
