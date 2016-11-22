package com.ihs.keyboardutils.nativeads;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.R;
import com.ihs.nativeads.base.api.HSNativeAd;
import com.ihs.nativeads.base.api.HSNativeAdContainerView;
import com.ihs.nativeads.base.api.HSNativeAdFactory;
import com.ihs.nativeads.base.api.HSNativeAdPrimaryView;
import com.ihs.nativeads.base.api.INativeAdListener;

/**
 * Created by ihandysoft on 16/10/19.
 */

public class NativeAdView extends FrameLayout {

    private INotificationObserver nativeAdObserver = new INotificationObserver() {

        @Override
        public void onReceive(String notificationName, HSBundle bundle) {
            if (notificationName.equals(NativeAdManager.NOTIFICATION_NEW_AD)) {
                if (poolName.equals(bundle.getString(NativeAdManager.NATIVE_AD_POOL_NAME))) {
                    log("onReceive", "New NativeAd Notification", "");
                    startRefreshing();
                }
            }
        }
    };

    private String poolName;
    private int primaryWidth;
    private float primaryHWRatio;
    private NativeAdTimer nativeAdTimer;

    private View loadingView;
    private HSNativeAdContainerView nativeAdContainerView;
    private ViewTreeObserver.OnScrollChangedListener onScrollChangedListener;

    private boolean isPaused = true;
    private boolean hasObserver = false;
    private boolean isCurrentNativeAdClicked = false;

    private int currentNativeAdHashCode;

    private static Handler handler = new Handler();

    public NativeAdView(Context context, View groupView) {
        super(context);
        initNativeAdContainerView(groupView);
    }

    public NativeAdView(Context context, AttributeSet attrs, View groupView) {
        super(context, attrs);
        initNativeAdContainerView(groupView);
    }

    public NativeAdView(Context context, AttributeSet attrs, int defStyleAttr, View groupView) {
        super(context, attrs, defStyleAttr);
        initNativeAdContainerView(groupView);
    }

    public void showNativeAd(String poolName) {
        showNativeAd(poolName, null);
    }

    public void showNativeAd(String poolName, View loadingView) {
        showNativeAd(poolName, 0, loadingView);
    }

    public void showNativeAd(String poolName, int fetchNativeAdInterval) {
        showNativeAd(poolName, fetchNativeAdInterval, null);
    }

    public void showNativeAd(String poolName, int fetchNativeAdInterval, View loadingView) {
        showNativeAd(poolName, 0, fetchNativeAdInterval, loadingView);
    }

    public void showNativeAd(String poolName, float primaryHWRatio, int fetchNativeAdInterval) {
        showNativeAd(poolName, primaryHWRatio, fetchNativeAdInterval, null);
    }

    public void showNativeAd(String poolName, float primaryHWRatio, int fetchNativeAdInterval, View loadingView) {
        if(this.poolName != null && this.poolName.equals(poolName)){
            return;
        }
        this.poolName = poolName;
        this.primaryWidth = -1;
        this.primaryHWRatio = primaryHWRatio;
        this.nativeAdTimer = new NativeAdTimer(fetchNativeAdInterval);
        this.loadingView = loadingView;
        NativeAdManager.getInstance().getNativeAdProxy(poolName).startNotifyAvailableAdCountChanged();
        onScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {

            @Override
            public void onScrollChanged() {
                onViewStateChanged();
            }
        };
        getViewTreeObserver().addOnScrollChangedListener(onScrollChangedListener);
        addView(nativeAdContainerView);
        if (this.loadingView != null) {
            addView(this.loadingView);
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        onViewStateChanged();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        onViewStateChanged();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        onViewStateChanged();
    }


    private Rect screenRect = new Rect();

    {
        WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        Point p = new Point();
        wm.getDefaultDisplay().getSize(p);
        screenRect = new Rect(0, 0, p.x, p.y);
    }

    private void onViewStateChanged() {
        Rect rectInScreen = getScreenVisibleRect();
        if (getWindowVisibility() == VISIBLE && getVisibility() == VISIBLE && hasWindowFocus() && !rectInScreen.isEmpty() && screenRect.contains(rectInScreen)) {
            resumeRefreshing();
        } else {
            pauseRefreshing();
        }
    }

    private Rect getScreenVisibleRect() {
        int[] out = new int[2];
        getLocationOnScreen(out);
        int left = out[0] > 0 ? out[0] : 0;
        int top = out[1] > 0 ? out[1] : 0;
        int right = getMeasuredWidth() + out[0];
        right = right >= screenRect.right ? screenRect.right : right;
        int bottom = getMeasuredHeight() + out[1];
        bottom = bottom >= screenRect.bottom ? screenRect.bottom : bottom;
        return new Rect(left, top, right, bottom);
    }

    public void release() {
        if (NativeAdManager.getInstance().getNativeAdProxy(poolName) != null) {
            NativeAdManager.getInstance().getNativeAdProxy(poolName).release();
            log("release", "", "");
        }
    }

    private void initNativeAdContainerView(View groupView) {
        this.nativeAdContainerView = HSNativeAdFactory.getInstance().createNativeAdContainerView(getContext(), groupView);

        int coverImgId = getResources().getIdentifier("ad_cover_img", "id", getContext().getPackageName());
        int choiceId = getResources().getIdentifier("ad_choice", "id", getContext().getPackageName());
        int actionId = getResources().getIdentifier("ad_call_to_action", "id", getContext().getPackageName());
        int titleId = getResources().getIdentifier("ad_title", "id", getContext().getPackageName());
        int iconId = getResources().getIdentifier("ad_icon", "id", getContext().getPackageName());
        int subtitleId = getResources().getIdentifier("ad_subtitle", "id", getContext().getPackageName());

        View view1 = groupView.findViewById(actionId);
        if (view1 != null) {
            nativeAdContainerView.setAdActionView(view1);
        }
        view1 = groupView.findViewById(titleId);
        if (view1 != null) {
            nativeAdContainerView.setAdTitleView((TextView) view1);
        }
        view1 = groupView.findViewById(subtitleId);
        if (view1 != null) {
            nativeAdContainerView.setAdSubTitleView((TextView) view1);
        }
        view1 = groupView.findViewById(iconId);
        if (view1 != null) {
            nativeAdContainerView.setAdIconView((ImageView) view1);
        }
        view1 = groupView.findViewById(coverImgId);
        if (view1 != null) {
            nativeAdContainerView.setAdPrimaryView((HSNativeAdPrimaryView) view1);
        }
        view1 = groupView.findViewById(choiceId);
        if (view1 != null) {
            nativeAdContainerView.setAdChoiceView((FrameLayout) view1);
        }
    }

    private void adjustNativeAdContainerView(HSNativeAd hsNativeAd) {
        ViewGroup.LayoutParams layoutParams = nativeAdContainerView.getContainerView().getLayoutParams();
        layoutParams.width = primaryWidth;
        nativeAdContainerView.getContainerView().setLayoutParams(layoutParams);

        if (nativeAdContainerView.getAdSubTitleView() != null && ((TextView) nativeAdContainerView.getAdSubTitleView()).getText().toString().trim().equals("")) {
            nativeAdContainerView.getAdSubTitleView().setVisibility(View.GONE);
        }

        if (nativeAdContainerView.getAdPrimaryView() != null) {
            nativeAdContainerView.getAdPrimaryView().getNormalImageView().setScaleType(ImageView.ScaleType.FIT_XY);
            if (primaryHWRatio == 0) {
                nativeAdContainerView.getAdPrimaryView().setLayoutParams(new FrameLayout.LayoutParams(primaryWidth, primaryWidth));
            } else {
                nativeAdContainerView.getAdPrimaryView().setLayoutParams(new FrameLayout.LayoutParams(primaryWidth, (int) (nativeAdContainerView.getWidth() * primaryHWRatio)));
            }
        }

        if (TextUtils.isEmpty(hsNativeAd.getIconUrl())) {
            if (nativeAdContainerView.getAdIconView() == null) {
                return;
            }
            nativeAdContainerView.getAdIconView().setVisibility(View.GONE);
        }

        if (loadingView != null) {
            removeView(loadingView);
            loadingView = null;
        }
    }

    public void resumeRefreshing() {
        if (!isPaused) {
            return;
        }
        log("resumeRefreshing", "", "");
        nativeAdTimer.resumeTimer(NativeAdManager.getInstance().getNativeAdProxy(poolName).getCachedNativeAdTime());
        isPaused = false;
        startRefreshing();

    }

    public void pauseRefreshing() {
        if (isPaused) {
            return;
        }
        log("pauseRefreshing", "", "");
        nativeAdTimer.pauseTimer();
        isPaused = true;
        removeObserver();
        handler.removeCallbacks(frequentRunnable);
    }

    private Runnable frequentRunnable = new Runnable() {
        @Override
        public void run() {
            startRefreshing();
        }
    };


    private void removeObserver() {
        if (hasObserver) {
            HSGlobalNotificationCenter.removeObserver(nativeAdObserver);
            hasObserver = false;
        }
    }

    private void addObserver() {
        if (!hasObserver) {
            hasObserver = true;
            HSGlobalNotificationCenter.addObserver(NativeAdManager.NOTIFICATION_NEW_AD, nativeAdObserver);
        }
    }

    /**
     * 从缓存池里面拿1条数据，循环读取
     *
     * @return
     */
    private void startRefreshing() {
        if (nativeAdTimer.refreshInterval > 0) {
            if (nativeAdTimer.isExpired() || isCurrentNativeAdClicked) {
                // fetch new NativeAd
                HSNativeAd ad = NativeAdManager.getInstance().getNativeAdProxy(poolName).getNativeAd();
                if (ad != null) {
                    log("startRefreshing", "New NativeAd", ad.hashCode() + "");
                    nativeAdTimer.resetTimer(NativeAdManager.getInstance().getNativeAdProxy(poolName).getCachedNativeAdTime());
                    isCurrentNativeAdClicked = false;
                    removeObserver();
                    handler.removeCallbacks(frequentRunnable);
                    bindDataToView(ad);
                    if (!isPaused) {
                        handler.postDelayed(frequentRunnable, nativeAdTimer.refreshInterval);
                    }
                    return;
                }
                // fetch cached NativeAd
                ad = NativeAdManager.getInstance().getNativeAdProxy(poolName).getCachedNativeAd();
                if (ad != null) {
                    log("startRefreshing", "Cached NativeAd", ad.hashCode() + "");
                    bindDataToView(ad);
                }

                if (!isPaused) {
                    addObserver();
                    handler.removeCallbacks(frequentRunnable);
                }
            } else if (!isPaused) {
                handler.postDelayed(frequentRunnable, nativeAdTimer.nextFetchNativeAdInterval());
            }
        } else {
            if (NativeAdManager.getInstance().getNativeAdProxy(poolName).getCachedNativeAd() == null || isCurrentNativeAdClicked) {
                // fetch new NativeAd
                HSNativeAd ad = NativeAdManager.getInstance().getNativeAdProxy(poolName).getNativeAd();
                if (ad != null) {
                    bindDataToView(ad);
                    pauseRefreshing();
                    return;
                }
                if (!isPaused) {
                    addObserver();
                    handler.removeCallbacks(frequentRunnable);
                }
            }
        }
    }

    /**
     * 绑定获取的广告数据
     *
     * @param hsNativeAd
     */
    private void bindDataToView(HSNativeAd hsNativeAd) {
        if (nativeAdContainerView != null) {
            if (hsNativeAd == null) {
                return;
            }

            boolean flag = currentNativeAdHashCode == hsNativeAd.hashCode();
            logGoogleAnalyticsEvent("Show");
            if (flag) {
                return;
            }
            currentNativeAdHashCode = hsNativeAd.hashCode();
            hsNativeAd.registerView(nativeAdContainerView.getContext(), nativeAdContainerView);

            // 调整布局
            adjustNativeAdContainerView(hsNativeAd);
            hsNativeAd.setNativeAdListener(new INativeAdListener() {
                @Override
                public void onNativeAdLoadSucceed(HSNativeAd hsNativeAd) {
                }

                @Override
                public void onNativeAdLoadFailed(HSNativeAd hsNativeAd, int i, String s) {
                }

                @Override
                public void onNativeAdClicked(HSNativeAd hsNativeAd) {
                    isCurrentNativeAdClicked = true;
                    logGoogleAnalyticsEvent("Click");
                }

                @Override
                public void onNativeAdExpired(HSNativeAd hsNativeAd) {
                }

                @Override
                public void onNativeAdWillExpire(HSNativeAd hsNativeAd) {
                }
            });
        }
    }

    private void log(String functionName, String key, String value) {
        HSLog.e(poolName + " - " + functionName + " : " + key + " - " + value);
    }


    private void logGoogleAnalyticsEvent(String actionSuffix) {
        String screenName = HSApplication.getContext().getResources().getString(R.string.english_ime_name);
        HSAnalytics.logGoogleAnalyticsEvent(screenName, "APP", "NativeAd_" + poolName + "_" + actionSuffix, "", null, null, null);
    }

    class NativeAdTimer {

        private long currentResumeTime;
        private long totalDisplayDuration;
        private int refreshInterval;

        NativeAdTimer(int refreshInterval) {
            this.refreshInterval = refreshInterval;
        }

        void resetTimer(long currentTime) {
            this.currentResumeTime = currentTime;
            this.totalDisplayDuration = 0;
        }

        void resumeTimer(long cachedNativeAdTime) {
            if (totalDisplayDuration == 0) {
                this.currentResumeTime = cachedNativeAdTime;
            } else {
                this.currentResumeTime = System.currentTimeMillis();
            }
        }

        void pauseTimer() {
            if (currentResumeTime != 0) {
                long showedTime = System.currentTimeMillis() - currentResumeTime;
                totalDisplayDuration += showedTime;
            }
        }

        boolean isExpired() {
            totalDisplayDuration += System.currentTimeMillis() - currentResumeTime;
            if (totalDisplayDuration < refreshInterval) {
                log("isExpired", "HasShowedTime", totalDisplayDuration + "");
                return false;
            }
            log("isExpired", "HasShowedTime", totalDisplayDuration + "");
            return true;
        }

        long nextFetchNativeAdInterval() {
            return refreshInterval - totalDisplayDuration;
        }
    }
}
