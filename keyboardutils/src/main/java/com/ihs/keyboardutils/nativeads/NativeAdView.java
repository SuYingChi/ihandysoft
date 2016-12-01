package com.ihs.keyboardutils.nativeads;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.text.TextUtils;
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

import java.util.List;

/**
 * Created by ihandysoft on 16/10/19.
 */

public class NativeAdView extends FrameLayout {

    public interface NativeAdViewListener {
        public void onNativeAdLoaded();
    }

    private INotificationObserver nativeAdObserver = new INotificationObserver() {

        @Override
        public void onReceive(String notificationName, HSBundle bundle) {
            if (notificationName.equals(NativeAdManager.NOTIFICATION_NEW_AD)) {
                if (nativeAdParams.getPoolName().equals(bundle.getString(NativeAdManager.NATIVE_AD_POOL_NAME))) {
                    log("onReceive", "NativeAd Notification", "");
                    startRefreshing();
                }
            }
        }
    };

    private View viewGroup;
    private View loadingView;

    private NativeAdParams nativeAdParams;
    private NativeAdTimer nativeAdTimer;

    private HSNativeAdContainerView nativeAdContainerView;
    private ViewTreeObserver.OnScrollChangedListener onScrollChangedListener;

    private NativeAdViewListener nativeAdViewListener;

    private boolean isPaused = true;
    private boolean hasObserver = false;
    private boolean isCurrentNativeAdClicked = false;

    private int currentNativeAdHashCode;
    private static Handler handler = new Handler();

    private Runnable frequentRunnable = new Runnable() {
        @Override
        public void run() {
            startRefreshing();
        }
    };

    public NativeAdView(Context context, View viewGroup) {
        super(context);
        this.viewGroup = viewGroup;
    }

    public NativeAdView(Context context, View viewGroup, View loadingView) {
        super(context);
        this.viewGroup = viewGroup;
        this.loadingView = loadingView;
    }

    public NativeAdView(Context context, View viewGroup, NativeAdViewListener nativeAdViewListener) {
        super(context);
        this.viewGroup = viewGroup;
        this.nativeAdViewListener = nativeAdViewListener;
    }

    public void configParams(NativeAdParams nativeAdParams) {
        this.nativeAdParams = nativeAdParams;
        NativeAdManager.getInstance().getNativeAdProxy(this.nativeAdParams.getPoolName()).startAvailableAdCountChangedNotifaction();
        NativeAdManager.getInstance().getNativeAdProxy(nativeAdParams.getPoolName()).setCachedNativeAdShowedTime(0);
        this.nativeAdTimer = new NativeAdTimer(this.nativeAdParams.getFetchNativeAdInterval());
        initNativeAdContainerView(viewGroup);

        onScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {

            @Override
            public void onScrollChanged() {
                onViewPositionChanged(getScreenVisibleRect());
            }
        };

        if (loadingView != null) {
            addView(this.loadingView);
        }
        if (nativeAdViewListener != null) {
            resumeRefreshing();
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

        List disabledIcons = NativeAdConfig.getDisabledIconPools();

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
            if (disabledIcons.contains(nativeAdParams.getPoolName())) {
                view1.setVisibility(GONE);
            } else {
                nativeAdContainerView.setAdIconView((ImageView) view1);
            }
        }
        view1 = groupView.findViewById(coverImgId);
        if (view1 != null) {
            nativeAdContainerView.setAdPrimaryView((HSNativeAdPrimaryView) view1);
        }
        view1 = groupView.findViewById(choiceId);
        if (view1 != null) {
            nativeAdContainerView.setAdChoiceView((FrameLayout) view1);
        }
        addView(nativeAdContainerView);
    }

    private void adjustNativeAdContainerView(HSNativeAd hsNativeAd) {
        ViewGroup.LayoutParams layoutParams = nativeAdContainerView.getContainerView().getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        nativeAdContainerView.getContainerView().setLayoutParams(layoutParams);

        if (nativeAdContainerView.getAdPrimaryView() != null) {
            nativeAdContainerView.getAdPrimaryView().getNormalImageView().setScaleType(ImageView.ScaleType.FIT_XY);
            if (nativeAdParams.getPrimaryHWRatio() == 0) {
                nativeAdContainerView.getAdPrimaryView().setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            } else {
                nativeAdContainerView.getAdPrimaryView().setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (nativeAdParams.getPrimaryWidth() / nativeAdParams.getPrimaryHWRatio())));
            }
        }
        // 设置subtitle值
        if (nativeAdContainerView.getAdSubTitleView() != null) {
            if (hsNativeAd.getSubtitle() != null && !hsNativeAd.getSubtitle().equals("")) {
                ((TextView) nativeAdContainerView.getAdSubTitleView()).setText(hsNativeAd.getSubtitle());
            } else if (hsNativeAd.getBody() != null && !hsNativeAd.getBody().equals("")) {
                ((TextView) nativeAdContainerView.getAdSubTitleView()).setText(hsNativeAd.getBody());
            } else {
                nativeAdContainerView.getAdSubTitleView().setVisibility(GONE);
            }
        }

        if (TextUtils.isEmpty(hsNativeAd.getIconUrl())) {
            if (nativeAdContainerView.getAdIconView() != null) {
                nativeAdContainerView.getAdIconView().setVisibility(View.GONE);
            }
        }

        if (loadingView != null) {
            removeView(loadingView);
            loadingView = null;
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        log("onWindowVisibilityChanged", "visibility", visibility + ": " + hashCode());
        if (visibility == VISIBLE && isPaused) {
            resumeRefreshing();
        } else if (visibility != VISIBLE && !isPaused) {
            pauseRefreshing();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        log("onWindowFocusChanged", "visibility", hasWindowFocus + ": " + hashCode());
        if (hasWindowFocus && isPaused) {
            resumeRefreshing();
        } else if (!hasWindowFocus && !isPaused) {
            pauseRefreshing();
        }
    }


    protected void onViewPositionChanged(Rect rectInScreen) {
        if (!rectInScreen.isEmpty() && screenRect.contains(rectInScreen)) {
            resumeRefreshing();
        } else {
            pauseRefreshing();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnScrollChangedListener(onScrollChangedListener);
        if (NativeAdManager.getInstance().getNativeAdProxy(nativeAdParams.getPoolName()).getCachedNativeAd() != null) {
            nativeAdTimer.setCurrentTotalDisplayDuration(NativeAdManager.getInstance().getNativeAdProxy(nativeAdParams.getPoolName()).getCachedNativeAdShowedTime());
            log("onAttachedToWindow", "currentTotalDisplayDuration", "" + nativeAdTimer.getCurrentTotalDisplayDuration());
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        getViewTreeObserver().removeOnScrollChangedListener(onScrollChangedListener);
        if (NativeAdManager.getInstance().getNativeAdProxy(nativeAdParams.getPoolName()).getCachedNativeAd() != null) {
            NativeAdManager.getInstance().getNativeAdProxy(nativeAdParams.getPoolName()).setCachedNativeAdShowedTime(nativeAdTimer.currentTotalDisplayDuration);
            log("onDetachedFromWindow", "currentTotalDisplayDuration", "" + nativeAdTimer.getCurrentTotalDisplayDuration());
        }
        super.onDetachedFromWindow();
    }

    public void resumeRefreshing() {
        if (!isPaused) {
            return;
        }
        log("resumeRefreshing", "", "");
        nativeAdTimer.resumeTimer();
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
        handler.removeCallbacks(frequentRunnable);
    }

    private void removeObserver() {
        if (hasObserver) {
            HSGlobalNotificationCenter.removeObserver(nativeAdObserver);
            hasObserver = false;
            log("removeObserver", "", "");
        }
    }

    private void addObserver() {
        if (!hasObserver) {
            hasObserver = true;
            HSGlobalNotificationCenter.addObserver(NativeAdManager.NOTIFICATION_NEW_AD, nativeAdObserver);
            log("addObserver", "", "");
        }
    }

    public void release() {
        removeObserver();
        logGoogleAnalyticsEvent("DisplayTime", nativeAdTimer.getTotalDisplayDuration());
        if (NativeAdManager.getInstance().getNativeAdProxy(nativeAdParams.getPoolName()) != null) {
            NativeAdManager.getInstance().getNativeAdProxy(nativeAdParams.getPoolName()).release();
            log("release", "", "");
        }
    }

    /**
     * 从缓存池里面拿1条数据，循环读取
     *
     * @return
     */
    private void startRefreshing() {
        if (nativeAdTimer.refreshInterval > 0) {
            if (nativeAdTimer.isCurrentExpired() || isCurrentNativeAdClicked) {
                logGoogleAnalyticsEvent("Load");

                // fetch new NativeAd
                HSNativeAd ad = NativeAdManager.getInstance().getNativeAdProxy(nativeAdParams.getPoolName()).getNativeAd();
                if (ad != null) {
                    nativeAdTimer.resetTimer();
                    isCurrentNativeAdClicked = false;
                    removeObserver();
                    handler.removeCallbacks(frequentRunnable);
                    bindDataToView(ad);
                    if (nativeAdViewListener != null) {
                        nativeAdViewListener.onNativeAdLoaded();
                        nativeAdViewListener = null;
                    }

                    if (!isPaused) {
                        handler.postDelayed(frequentRunnable, nativeAdTimer.refreshInterval);
                    }
                    return;
                }
                // fetch cached NativeAd
                ad = NativeAdManager.getInstance().getNativeAdProxy(nativeAdParams.getPoolName()).getCachedNativeAd();
                if (ad != null) {
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
            if (NativeAdManager.getInstance().getNativeAdProxy(nativeAdParams.getPoolName()).getCachedNativeAd() == null || isCurrentNativeAdClicked) {
                // fetch new NativeAd
                HSNativeAd ad = NativeAdManager.getInstance().getNativeAdProxy(nativeAdParams.getPoolName()).getNativeAd();
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
            log("bindDataToView", "NativeAd", hsNativeAd.hashCode() + "");
            if (currentNativeAdHashCode == hsNativeAd.hashCode()) {
                return;
            }
            logGoogleAnalyticsEvent("Show");
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

    private Rect screenRect = new Rect();

    {
        WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        Point p = new Point();
        wm.getDefaultDisplay().getSize(p);
        screenRect = new Rect(0, 0, p.x, p.y);
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


    private void log(String functionName, String key, String value) {
        HSLog.e(hashCode() + " - " + nativeAdParams.getPoolName() + " - " + functionName + " : " + key + " - " + value + ": ");
    }

    private void logGoogleAnalyticsEvent(String actionSuffix) {
        logGoogleAnalyticsEvent(actionSuffix, null);
    }

    private void logGoogleAnalyticsEvent(String actionSuffix, Long value) {
        String screenName = HSApplication.getContext().getResources().getString(R.string.english_ime_name);
        HSAnalytics.logGoogleAnalyticsEvent(screenName, "APP", "NativeAd_" + nativeAdParams.getPoolName() + "_" + actionSuffix, "", value, null, null);
    }

    private static class NativeAdTimer {
        private long currentResumeTime;
        private long currentTotalDisplayDuration;
        private int refreshInterval;
        private long totalDisplayDuration;

        private NativeAdTimer(int refreshInterval) {
            this.refreshInterval = refreshInterval;
        }

        private void resetTimer() {
            this.currentResumeTime = System.currentTimeMillis();
            this.totalDisplayDuration += currentTotalDisplayDuration + (System.currentTimeMillis() - currentResumeTime);
            this.currentTotalDisplayDuration = 0;
        }

        private void resumeTimer() {
            if (currentTotalDisplayDuration != 0) {
                this.currentResumeTime = System.currentTimeMillis();
            }
        }

        private void pauseTimer() {
            if (currentResumeTime != 0) {
                currentTotalDisplayDuration += System.currentTimeMillis() - currentResumeTime;
            }
        }

        private long getTotalDisplayDuration() {
            totalDisplayDuration += currentTotalDisplayDuration;
            return totalDisplayDuration;
        }

        private boolean isCurrentExpired() {
            long displayDuration = 0;
            if (currentResumeTime != 0) {
                displayDuration = currentTotalDisplayDuration + (System.currentTimeMillis() - currentResumeTime);
            }
            if (displayDuration < refreshInterval && displayDuration > 0) {
                return false;
            }
            return true;
        }

        private long nextFetchNativeAdInterval() {
            return refreshInterval - currentTotalDisplayDuration;
        }

        private void setCurrentTotalDisplayDuration(long currentTotalDisplayDuration) {
            this.currentTotalDisplayDuration = currentTotalDisplayDuration;
        }

        private long getCurrentTotalDisplayDuration() {
            return currentTotalDisplayDuration;
        }
    }
}
