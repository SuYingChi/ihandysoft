package com.ihs.feature.junkclean;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.device.clean.accessibility.HSAccTaskManager;
import com.ihs.feature.common.FormatSizeBuilder;
import com.ihs.feature.junkclean.data.JunkManager;
import com.ihs.feature.junkclean.list.JunkCleanCategoryItem;
import com.ihs.feature.junkclean.model.ApkJunkWrapper;
import com.ihs.feature.junkclean.model.AppJunkWrapper;
import com.ihs.feature.junkclean.model.JunkWrapper;
import com.ihs.feature.junkclean.model.MemoryJunkWrapper;
import com.ihs.feature.junkclean.model.PathRuleJunkWrapper;
import com.ihs.feature.junkclean.model.SystemJunkWrapper;
import com.ihs.feature.junkclean.util.BaseItemAnimator;
import com.ihs.feature.junkclean.util.JunkCleanConstant;
import com.ihs.feature.junkclean.util.JunkCleanUtils;
import com.ihs.feature.resultpage.ResultPageActivity;
import com.ihs.feature.ui.TouchableRecycleView;
import com.ihs.keyboardutils.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;


public class JunkCleanWindowController {

    private class SlideOutLeftAnimator extends BaseItemAnimator {

        SlideOutLeftAnimator() {
        }

        @Override
        protected void animateRemoveImpl(RecyclerView.ViewHolder holder, int index) {
            ViewCompat.animate(holder.itemView)
                    .translationX(-holder.itemView.getRootView().getWidth())
                    .setDuration(getRemoveDuration())
                    .setInterpolator(mInterpolator)
                    .setListener(new DefaultRemoveVpaListener(holder))
                    .start();
        }
    }

    private static final long DURATION_ITEM_REMOVE_ANIMATION = 1000;
    private static final long DURATION_COLOR_CHANGE_ANIMATION = 1200;

    private static final long ITEM_REMOVE_TIME_INTERVAL = 700;

    private static final long ITEM_MOVE_LEFT_DURATION = 180;
    private static final long ITEM_MOVE_UP_DURATION = 90;

    private static final long ITEM_CLEAN_DELAY_DURATION = 1000;

    private static final int STATE_WINDOW_SHOWING = 1;
    private static final int STATE_WINDOW_DISMISS = 2;

    private int state = STATE_WINDOW_DISMISS;

    private JunkCleanActivity mJunkCleanActivity;
    private WindowManager mWindowManager;

    private View mRootView;
    private Toolbar mToolbar;
    private TouchableRecycleView mRecyclerView;
    private RelativeLayout mJunkSizeLayout;
    private TextView mScanOverJunkSizeTv;
    private TextView mScanOverJunkUnitTv;
    private TextView mScanOverJunkLabelTv;
    private View mStopDialogV;

    private FlexibleAdapter mFlexibleAdapter;

    private ValueAnimator mJunkSizeReduceAnimator;
    private ValueAnimator mJunkColorAnimator;
    private ValueAnimator mJunkCleanCountAnimator;

    private ArrayList<String> mForceCleanPackageNameList = new ArrayList<>();

    private boolean mIsCleanSucceed;
    public boolean mIsBackButtonClicked;
    private boolean mIsTimeoutCanceled;

    private long mMarkedJunkSize;
    private int mItemCount;

    private Handler mHandler = new Handler();
    private JunkManager mJunkManager = JunkManager.getInstance();

    JunkCleanWindowController(JunkCleanActivity junkCleanActivity) {
        this.mJunkCleanActivity = junkCleanActivity;
        mWindowManager = (WindowManager) HSApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        mRootView = LayoutInflater.from(this.mJunkCleanActivity).inflate(R.layout.activity_junk_clean_animation, null);
        JunkCleanConstant.sIsJunkCleaned = false;
        initView();
    }

    private void initView() {
        mStopDialogV = JunkCleanUtils.initStopDialog(this, mRootView);
        mToolbar = (Toolbar) mRootView.findViewById(R.id.action_bar);
        mToolbar.setTitle(HSApplication.getContext().getString(R.string.clean_title));
        mToolbar.setTitleTextColor(HSApplication.getContext().getResources().getColor(android.R.color.white));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JunkCleanUtils.showStopDialog(mStopDialogV);
            }
        });


        mRecyclerView = (TouchableRecycleView) mRootView.findViewById(R.id.recycler_view);
        mScanOverJunkLabelTv = (TextView) mRootView.findViewById(R.id.pop_junk_label);
        mScanOverJunkSizeTv = (TextView) mRootView.findViewById(R.id.pop_junk_size);
        mScanOverJunkUnitTv = (TextView) mRootView.findViewById(R.id.pop_junk_unit);

        FormatSizeBuilder junkSizeBuilder = new FormatSizeBuilder(JunkManager.getInstance().getJunkSelectedSize());
        mScanOverJunkSizeTv.setText(junkSizeBuilder.size);
        mScanOverJunkUnitTv.setText(junkSizeBuilder.unit);

        mJunkSizeLayout = (RelativeLayout) mRootView.findViewById(R.id.junk_size_layout);
        mJunkSizeLayout.setBackgroundColor(JunkManager.getInstance().getColor());
        mToolbar.setBackgroundColor(JunkManager.getInstance().getColor());

        mRecyclerView.setTouchable(false);

        mRecyclerView.setAlpha(0);
        mJunkSizeLayout.setScaleY(0);
        mJunkSizeLayout.setPivotY(0);
        mScanOverJunkSizeTv.setPivotX(0);
        mScanOverJunkSizeTv.setPivotY(0);
        mScanOverJunkUnitTv.setPivotX(0);
        mScanOverJunkUnitTv.setPivotY(0);
        mScanOverJunkLabelTv.setPivotX(0);
        mScanOverJunkLabelTv.setPivotY(0);
    }

    void showCleanWindow() {
        mIsBackButtonClicked = false;
        mFlexibleAdapter = new FlexibleAdapter(getListItems());
        mFlexibleAdapter.expandItemsAtStartUp()
                .setAnimationOnScrolling(true)
                .setAnimationDuration(175)
                .setAnimationInterpolator(new FastOutSlowInInterpolator());

        mRecyclerView.setLayoutManager(new SmoothScrollLinearLayoutManager(mJunkCleanActivity));
        SlideOutLeftAnimator slideOutLeftAnimator = new SlideOutLeftAnimator();
        slideOutLeftAnimator.setMoveDuration(ITEM_MOVE_UP_DURATION);
        slideOutLeftAnimator.setRemoveDuration(ITEM_MOVE_LEFT_DURATION);
        mRecyclerView.setItemAnimator(slideOutLeftAnimator);
        mRecyclerView.setAdapter(mFlexibleAdapter);

        mRecyclerView.setAlpha(0);
        mRecyclerView.animate().alpha(1).setDuration(375).start();

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, 1f);
        valueAnimator.setDuration(375);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mJunkSizeLayout.setScaleY(value);
                mScanOverJunkSizeTv.setScaleX(value);
                mScanOverJunkSizeTv.setScaleY(value);
                mScanOverJunkUnitTv.setScaleX(value);
                mScanOverJunkUnitTv.setScaleY(value);
                mScanOverJunkLabelTv.setScaleX(value);
                mScanOverJunkLabelTv.setScaleX(value);
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startForceClearCacheAnimator();

                startOneTapClearCache();
            }
        });
        valueAnimator.start();

        WindowManager.LayoutParams cleanWindowParams = new WindowManager.LayoutParams();
        cleanWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        cleanWindowParams.format = PixelFormat.RGBA_8888;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            cleanWindowParams.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        }
        cleanWindowParams.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        cleanWindowParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        cleanWindowParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        try {
            if (HSLog.isDebugging()) {
                mRootView.setAlpha(0.9f);
            }
            mWindowManager.addView(mRootView, cleanWindowParams);

            state = STATE_WINDOW_SHOWING;
        } catch (SecurityException e) {
            HSLog.e(e.toString());
        }
        JunkCleanConstant.sIsTotalSelected = mJunkManager.isTotalJunkSelected();
    }

    private void startForceClearCacheAnimator() {
        mIsTimeoutCanceled = false;

        int stopIndex = (int)Math.ceil(mForceCleanPackageNameList.size() / 3.0f);

        final long averageJunkSize = mMarkedJunkSize / (mItemCount + 1);

        mJunkColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), (Object[]) mJunkManager.getColors(mMarkedJunkSize,
                mMarkedJunkSize - averageJunkSize * mForceCleanPackageNameList.size()));
        mJunkColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int color = (int) animation.getAnimatedValue();
                mJunkSizeLayout.setBackgroundColor(color);
                mToolbar.setBackgroundColor(color);
            }
        });
        mJunkColorAnimator.setDuration(stopIndex * ITEM_REMOVE_TIME_INTERVAL + ITEM_CLEAN_DELAY_DURATION * 3);
        mJunkColorAnimator.start();

        mJunkSizeReduceAnimator = ValueAnimator.ofFloat(mMarkedJunkSize,
                mMarkedJunkSize = mMarkedJunkSize - averageJunkSize * mForceCleanPackageNameList.size());
        mJunkSizeReduceAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float junkSize = (float) animation.getAnimatedValue();
                FormatSizeBuilder junkSizeBuilder = new FormatSizeBuilder((long) junkSize);
                mScanOverJunkSizeTv.setText(junkSizeBuilder.size);
                mScanOverJunkUnitTv.setText(junkSizeBuilder.unit);
            }
        });
        mJunkSizeReduceAnimator.setDuration(stopIndex * ITEM_REMOVE_TIME_INTERVAL + ITEM_CLEAN_DELAY_DURATION * 3);
        mJunkSizeReduceAnimator.start();

        mJunkCleanCountAnimator = ValueAnimator.ofInt(1 , stopIndex);
        mJunkCleanCountAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            int count = 0;
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                if (value > count) {
                    count++;
                    mFlexibleAdapter.removeItem(0);
                }
            }
        });
        mJunkCleanCountAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mIsCleanSucceed) {

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            normalClean();
                        }
                    }, ITEM_REMOVE_TIME_INTERVAL * 2);

                } else {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mIsCleanSucceed) {
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        normalClean();
                                    }
                                }, ITEM_CLEAN_DELAY_DURATION * 2);
                            } else {
                                mIsTimeoutCanceled = true;
                                HSAccTaskManager.getInstance().cancel();
                            }

                        }
                    }, ITEM_CLEAN_DELAY_DURATION);
                }
            }
        });
        mJunkCleanCountAnimator.setStartDelay(375);
        mJunkCleanCountAnimator.setDuration(stopIndex * ITEM_REMOVE_TIME_INTERVAL);
        mJunkCleanCountAnimator.start();

    }

    private void startOneTapClearCache() {
        mIsCleanSucceed = false;

        HSAccTaskManager.getInstance().startOneTapClearCache(new HSAccTaskManager.AccTaskListener() {
            @Override
            public void onStarted() {
                HSLog.d("one tap clear cache start");
            }

            @Override
            public void onProgressUpdated(int index, int total, String packageName) {
            }

            @Override
            public void onSucceeded() {
                HSLog.d("one tap clear cache onSucceeded");

                mIsCleanSucceed = true;

                List<JunkWrapper> junkWrappers = JunkManager.getInstance().getJunkWrappers();
                Iterator<JunkWrapper> iterator = junkWrappers.iterator();

                List<JunkWrapper> removed = new ArrayList<>();
                while (iterator.hasNext()) {
                    JunkWrapper junkWrapper = iterator.next();
                    if (junkWrapper.getCategory().equals(SystemJunkWrapper.SYSTEM_JUNK)) {
                        removed.add(junkWrapper);
                    }
                }
                junkWrappers.removeAll(removed);
            }

            @Override
            public void onFailed(int failCode, String string) {
                HSLog.d("one tap clear cache onFailed : " + string);
                HSLog.d("one tap clear cache onFailed : mIsBackButtonClicked = " + mIsBackButtonClicked);
                HSLog.d("one tap clear cache onFailed : mIsTimeoutCanceled = " + mIsTimeoutCanceled);

                if (failCode == HSAccTaskManager.FAIL_CANCEL && mIsBackButtonClicked) {

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dismissCleanWindow();
                        }
                    }, 300);

                    return;
                }

                if (failCode == HSAccTaskManager.FAIL_CANCEL && mIsTimeoutCanceled){

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            normalClean();
                        }
                    }, ITEM_CLEAN_DELAY_DURATION * 2);

                    return;
                }

                startForceClear();
            }
        });

    }

    private void startForceClear() {
        HSAccTaskManager.getInstance().startClearCache(mForceCleanPackageNameList, new HSAccTaskManager.AccTaskListener() {
            @Override
            public void onStarted() {
                HSLog.d("force Clean  onStarted");
            }

            @Override
            public void onProgressUpdated(int index, int total, String packageName) {
                HSLog.d("Force clean onProgressUpdated packageName = " + packageName);

                List<JunkWrapper> junkWrappers = JunkManager.getInstance().getJunkWrappers();
                for (JunkWrapper junkWrapper : junkWrappers) {
                    if (junkWrapper.getCategory().equals(SystemJunkWrapper.SYSTEM_JUNK)
                            && junkWrapper.getPackageName().equals(packageName)) {
                        junkWrappers.remove(junkWrapper);
                        break;
                    }
                }
            }

            @Override
            public void onSucceeded() {
                HSLog.d("Force clean succeed");
                mIsCleanSucceed = true;
            }

            @Override
            public void onFailed(int failCode, String string) {
                HSLog.d("Force clean onFailed");
                if (failCode == HSAccTaskManager.FAIL_CANCEL && mIsTimeoutCanceled) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            normalClean();
                        }
                    }, ITEM_CLEAN_DELAY_DURATION * 2);

                    return;
                }

                if ((failCode == HSAccTaskManager.FAIL_CANCEL && mIsBackButtonClicked)
                        || failCode != HSAccTaskManager.FAIL_CANCEL) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dismissCleanWindow();
                        }
                    }, 300);
                }
            }
        });
    }

    private void normalClean() {
        if (mJunkSizeReduceAnimator != null && mJunkSizeReduceAnimator.isRunning()) {
            mJunkSizeReduceAnimator.cancel();
        }
        if (mJunkColorAnimator != null && mJunkColorAnimator.isRunning()) {
            mJunkColorAnimator.cancel();
        }

        long itemAnimDuration = mRecyclerView.getChildCount() == 0 ? 0 : DURATION_ITEM_REMOVE_ANIMATION / mRecyclerView.getChildCount();

        for (int i = 0; i < mRecyclerView.getChildCount(); i++) {
            View childView = mRecyclerView.getChildAt(i);
            childView.animate().translationX(-childView.getWidth()).setDuration(DURATION_ITEM_REMOVE_ANIMATION / 2).setStartDelay(itemAnimDuration * i).start();
            childView.animate().alpha(0).setDuration(DURATION_ITEM_REMOVE_ANIMATION / 2).setStartDelay(itemAnimDuration * i).start();
        }

        ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), (Object[]) mJunkManager.getColors(mMarkedJunkSize, 0));
        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int color = (int) animation.getAnimatedValue();
                mJunkSizeLayout.setBackgroundColor(color);
                mToolbar.setBackgroundColor(color);
            }
        });

        ValueAnimator junkSizeAnimator = ValueAnimator.ofFloat(mMarkedJunkSize, 0);
        junkSizeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float junkSize = (float) animation.getAnimatedValue();
                FormatSizeBuilder junkSizeBuilder = new FormatSizeBuilder((long) junkSize);
                mScanOverJunkSizeTv.setText(junkSizeBuilder.size);
                mScanOverJunkUnitTv.setText(junkSizeBuilder.unit);
            }
        });
        junkSizeAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startToResultPageActivity();
            }
        });

        mJunkManager.startJunkClean();

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(DURATION_COLOR_CHANGE_ANIMATION);
        animatorSet.playTogether(colorAnimator, junkSizeAnimator);
        animatorSet.start();
    }

    private void startToResultPageActivity() {
        HSLog.d(JunkCleanActivity.TAG, "JunkCleanWindowController startToResultPageActivity");
        ResultPageActivity.startForJunkClean(mJunkCleanActivity);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissCleanWindow();
            }
        }, 50);
    }

    private List<AbstractFlexibleItem> getListItems() {
        List<JunkWrapper> junkWrappers = JunkManager.getInstance().getJunkWrappers();
        List<AbstractFlexibleItem> flexibleItems = new ArrayList<>();

        List<JunkWrapper> unInstallAppJunkWrapperList = new ArrayList<>();
        List<JunkWrapper> installAppJunkWrapperList = new ArrayList<>();
        List<JunkWrapper> apkJunkWrapperList = new ArrayList<>();
        List<JunkWrapper> pathRuleWrapperList = new ArrayList<>();
        List<JunkWrapper> memoryJunkWrapperList = new ArrayList<>();

        mMarkedJunkSize = 0;
        mItemCount = 0;

        for (JunkWrapper junkWrapper : junkWrappers) {

            if (!junkWrapper.isMarked()) {
                continue;
            }

            mMarkedJunkSize += junkWrapper.getSize();

            switch (junkWrapper.getCategory()) {
                case SystemJunkWrapper.SYSTEM_JUNK:
                    JunkCleanCategoryItem systemJunkItem = new JunkCleanCategoryItem(JunkCleanCategoryItem.SYSTEM_JUNK);
                    systemJunkItem.setJunkWrapper(junkWrapper);
                    flexibleItems.add(systemJunkItem);
                    mItemCount ++;
                    mForceCleanPackageNameList.add(junkWrapper.getPackageName());
                    break;

                case AppJunkWrapper.APP_JUNK:

                    if (((AppJunkWrapper) junkWrapper).isInstall()) {
                        installAppJunkWrapperList.add(junkWrapper);
                    } else {
                        unInstallAppJunkWrapperList.add(junkWrapper);
                    }
                    break;

                case ApkJunkWrapper.APK_JUNK:
                    apkJunkWrapperList.add(junkWrapper);
                    break;

                case PathRuleJunkWrapper.PATH_RULE_JUNK:
                    pathRuleWrapperList.add(junkWrapper);
                    break;

                case MemoryJunkWrapper.MEMORY_JUNK:
                    memoryJunkWrapperList.add(junkWrapper);
                    break;
            }
        }

        if (unInstallAppJunkWrapperList.size() > 0) {
            JunkCleanCategoryItem unInstallItem = new JunkCleanCategoryItem(JunkCleanCategoryItem.UNINSTALL_APP_JUNK);
            for (JunkWrapper junkWrapper : unInstallAppJunkWrapperList) {
                unInstallItem.setJunkSize(unInstallItem.getJunkSize() + junkWrapper.getSize());
            }
            flexibleItems.add(unInstallItem);
            mItemCount++;
        }

        if (installAppJunkWrapperList.size() > 0) {
            JunkCleanCategoryItem installItem = new JunkCleanCategoryItem(JunkCleanCategoryItem.INSTALL_APP_JUNK);
            for (JunkWrapper junkWrapper : installAppJunkWrapperList) {
                installItem.setJunkSize(installItem.getJunkSize() + junkWrapper.getSize());
            }
            flexibleItems.add(installItem);
            mItemCount++;
        }

        if (apkJunkWrapperList.size() > 0) {
            JunkCleanCategoryItem apkJunkItem = new JunkCleanCategoryItem(JunkCleanCategoryItem.APK_JUNK);
            for (JunkWrapper junkWrapper : apkJunkWrapperList) {
                apkJunkItem.setJunkSize(apkJunkItem.getJunkSize() + junkWrapper.getSize());
            }
            flexibleItems.add(apkJunkItem);
        }
        if (pathRuleWrapperList.size() > 0) {
            JunkCleanCategoryItem pathRuleItem = new JunkCleanCategoryItem(JunkCleanCategoryItem.PATH_RULE_JUNK);
            for (JunkWrapper junkWrapper : pathRuleWrapperList) {
                pathRuleItem.setJunkSize(pathRuleItem.getJunkSize() + junkWrapper.getSize());
            }
            flexibleItems.add(pathRuleItem);
            mItemCount++;
        }

        if (memoryJunkWrapperList.size() > 0) {
            JunkCleanCategoryItem memoryItem = new JunkCleanCategoryItem(JunkCleanCategoryItem.MEMORY_JUNK);
            for (JunkWrapper junkWrapper : memoryJunkWrapperList) {
                memoryItem.setJunkSize(memoryItem.getJunkSize() + junkWrapper.getSize());
            }
            flexibleItems.add(memoryItem);
            mItemCount++;
        }

        return flexibleItems;
    }

    public void dismissCleanWindow() {
        if (state == STATE_WINDOW_DISMISS) {
            return;
        }

        if (mJunkSizeReduceAnimator != null && mJunkSizeReduceAnimator.isRunning()) {
            mJunkSizeReduceAnimator.cancel();
        }
        if (mJunkColorAnimator != null && mJunkColorAnimator.isRunning()) {
            mJunkColorAnimator.cancel();
        }
        if (mJunkCleanCountAnimator != null && mJunkCleanCountAnimator.isRunning()) {
            mJunkCleanCountAnimator.cancel();
        }

        mHandler.removeCallbacksAndMessages(null);

        mWindowManager.removeView(mRootView);

        state = STATE_WINDOW_DISMISS;
        mJunkCleanActivity.refreshView();
        JunkCleanUtils.dismissStopDialog(mStopDialogV);
    }

}
