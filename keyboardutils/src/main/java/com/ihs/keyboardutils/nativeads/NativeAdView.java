package com.ihs.keyboardutils.nativeads;

import android.app.Activity;
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

import com.acb.adadapter.AcbAd;
import com.acb.adadapter.AcbNativeAd;
import com.acb.adadapter.ContainerView.AcbNativeAdContainerView;
import com.acb.adadapter.ContainerView.AcbNativeAdIconView;
import com.acb.adadapter.ContainerView.AcbNativeAdPrimaryView;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.R;

import java.util.List;

public class NativeAdView extends FrameLayout {

    public interface NativeAdViewListener {
        public void onNativeAdLoaded(NativeAdView adView);
    }

    private View viewGroup;
    private View loadingView;

    private NativeAdParams nativeAdParams;

    private AcbNativeAdContainerView nativeAdContainerView;
    private ViewTreeObserver.OnScrollChangedListener onScrollChangedListener;

    private NativeAdViewListener nativeAdViewListener;

    private boolean isPaused = true;

    private int currentNativeAdHashCode;
    private static Handler handler = new Handler();

    private boolean adLoaded = false;
    private long firstAdLoadedTime = 0;
    private long resumeTime = 0;
    private long currentAdDisplayDurationBeforeResume = 0;
    private long totalAdDisplayDurationBeforeRefresh = 0;
    private long currentAdDisplayLimit = 0;

    private Runnable displayFinishRunnable = new Runnable() {
        @Override
        public void run() {
            NativeAdManager.getInstance().markAdAsFinished(nativeAdParams.getPlacementName());
            refresh();
        }
    };

    public NativeAdView(Context context, View viewGroup) {
        this(context, viewGroup, null);
    }

    public NativeAdView(Context context, View viewGroup, View loadingView) {
        super(context);
        this.viewGroup = viewGroup;
        this.loadingView = loadingView;
    }

    public void setNativeAdViewListener(NativeAdViewListener listener) {
        this.nativeAdViewListener = listener;
    }

    public NativeAdViewListener getNativeAdViewListener() {
        return this.nativeAdViewListener;
    }

    public void configParams(NativeAdParams nativeAdParams) {
        this.nativeAdParams = nativeAdParams;

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

        refresh();
    }

    private void initNativeAdContainerView(View groupView) {
        this.nativeAdContainerView = new AcbNativeAdContainerView(getContext());
        this.nativeAdContainerView.addContentView(groupView);

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
            if (disabledIcons.contains(nativeAdParams.getPlacementName())) {
                view1.setVisibility(GONE);
            } else {
                nativeAdContainerView.setAdIconView((AcbNativeAdIconView) view1);
            }
        }
        view1 = groupView.findViewById(coverImgId);
        if (view1 != null) {
            nativeAdContainerView.setAdPrimaryView((AcbNativeAdPrimaryView) view1);
        }
        view1 = groupView.findViewById(choiceId);
        if (view1 != null) {
            nativeAdContainerView.setAdChoiceView((FrameLayout) view1);
        }
        addView(nativeAdContainerView);
    }

    private void fillNativeAdContainerView(AcbNativeAd hsNativeAd) {
        ViewGroup.LayoutParams layoutParams = nativeAdContainerView.getContentView().getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        nativeAdContainerView.getContentView().setLayoutParams(layoutParams);

        if (nativeAdContainerView.getAdPrimaryView() != null) {
            nativeAdContainerView.getAdPrimaryView().getNormalImageView().setScaleType(ImageView.ScaleType.FIT_XY);
            if (nativeAdParams.getPrimaryHWRatio() == 0) {
                nativeAdContainerView.getAdPrimaryView().setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            } else {
                nativeAdContainerView.getAdPrimaryView().setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (nativeAdParams.getPrimaryWidth() / nativeAdParams.getPrimaryHWRatio())));
            }
        }

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

    private boolean isViewEnviromentReady() {
        boolean ready = true;
        ready = ready && getVisibility() == VISIBLE;
        ready = ready && getWindowVisibility() == VISIBLE;

        // 如果 View 显示在键盘中，windowFocus 会一直是 false
        if (getContext() instanceof Activity) {
            ready = ready && hasWindowFocus();
        }

        ready = ready && !getScreenVisibleRect().isEmpty() && screenRect.contains(getScreenVisibleRect());

        return ready;
    }

    protected void onViewEnviromentChanged() {
        boolean ready = isViewEnviromentReady();

        if (adLoaded) {
            if (ready) {
                resumeAutoRefreshing();
            } else {
                pauseAutoRefreshing();
            }
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        onViewEnviromentChanged();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        onViewEnviromentChanged();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        onViewEnviromentChanged();
    }


    protected void onViewPositionChanged(Rect rectInScreen) {
        onViewEnviromentChanged();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnScrollChangedListener(onScrollChangedListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        getViewTreeObserver().removeOnScrollChangedListener(onScrollChangedListener);
        super.onDetachedFromWindow();
    }

    private void startAutoRefreshingIfNeeded() {
        log("resumeAutoRefreshing", "", "");

        totalAdDisplayDurationBeforeRefresh += getCurrentAdDisplayDuration();
        currentAdDisplayDurationBeforeResume = 0;

        if (isViewEnviromentReady()) {
            isPaused = false;
            resumeTime = System.currentTimeMillis();
            scheduleRefreshing();
        } else {
            isPaused = true;
            handler.removeCallbacks(displayFinishRunnable);
        }
    }

    private void resumeAutoRefreshing() {
        if (!isPaused) {
            return;
        }
        isPaused = false;
        resumeTime = System.currentTimeMillis();

        scheduleRefreshing();
    }

    private void pauseAutoRefreshing() {
        if (isPaused) {
            return;
        }
        log("pauseAutoRefreshing", "", "");

        isPaused = true;
        handler.removeCallbacks(displayFinishRunnable);

        currentAdDisplayDurationBeforeResume += System.currentTimeMillis() - resumeTime;
    }

    private long getCurrentAdDisplayDuration() {
        if (isPaused) {
            return currentAdDisplayDurationBeforeResume;
        } else {
            return currentAdDisplayDurationBeforeResume + System.currentTimeMillis() - resumeTime;
        }
    }

    private long getTotalAdDisplayDuration() {
        return totalAdDisplayDurationBeforeRefresh + getCurrentAdDisplayDuration();
    }

    public void release() {
        logGoogleAnalyticsEvent("DisplayTime", getTotalAdDisplayDuration());
        if (NativeAdManager.getInstance().getNativeAdProxy(nativeAdParams.getPlacementName()) != null) {
            NativeAdManager.getInstance().getNativeAdProxy(nativeAdParams.getPlacementName()).setCachedNativeAdShowedTime(getCurrentAdDisplayDuration());
            log("release", "", "");
        }
    }

    private void scheduleRefreshing() {
        if (!isPaused && currentAdDisplayLimit > 0) {
            handler.removeCallbacks(displayFinishRunnable);
            handler.postDelayed(displayFinishRunnable, currentAdDisplayLimit - getCurrentAdDisplayDuration());
        }
    }

    private boolean isRefreshing = false;
    private void refresh() {
        if (isRefreshing) {
            return;
        }
        logGoogleAnalyticsEvent("Load");

        isRefreshing = true;
        NativeAdManager.getInstance().markAdAsFinished(nativeAdParams.getPlacementName());
        NativeAdManager.getInstance().loadNativeAd(getContext(), nativeAdParams.getPlacementName(), new NativeAdManager.AdLoadListener() {
            @Override
            public void onAdLoaded(AcbNativeAd ad, long remainingAdDisplayDuration) {
                if (firstAdLoadedTime == 0) {
                    firstAdLoadedTime = System.currentTimeMillis();
                }
                currentAdDisplayLimit = remainingAdDisplayDuration;

                bindDataToView(ad);

                isRefreshing = false;

                if (!adLoaded) {
                    adLoaded = true;

                    if (nativeAdViewListener != null) {
                        nativeAdViewListener.onNativeAdLoaded(NativeAdView.this);
                    }
                }

                startAutoRefreshingIfNeeded();
            }
        });
    }

    /**
     * 绑定获取的广告数据
     *
     * @param hsNativeAd
     */
    private void bindDataToView(AcbNativeAd hsNativeAd) {
        if (nativeAdContainerView != null && hsNativeAd != null) {
            log("bindDataToView", "NativeAd", hsNativeAd.hashCode() + "");
            if (currentNativeAdHashCode == hsNativeAd.hashCode()) {
                return;
            }
            logGoogleAnalyticsEvent("Show");
            currentNativeAdHashCode = hsNativeAd.hashCode();
            nativeAdContainerView.fillNativeAd(hsNativeAd);

            // 调整布局
            fillNativeAdContainerView(hsNativeAd);
            hsNativeAd.setNativeClickListener(new AcbNativeAd.AcbNativeClickListener() {
                @Override
                public void onAdClick(AcbAd acbAd) {
                    NativeAdManager.getInstance().markAdAsFinished(nativeAdParams.getPlacementName());
                    logGoogleAnalyticsEvent("Click");
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
        HSLog.e(hashCode() + " - " + nativeAdParams.getPlacementName() + " - " + functionName + " : " + key + " - " + value + ": ");
    }

    private void logGoogleAnalyticsEvent(String actionSuffix) {
        logGoogleAnalyticsEvent(actionSuffix, null);
    }

    private void logGoogleAnalyticsEvent(String actionSuffix, Long value) {
        String screenName = HSApplication.getContext().getResources().getString(R.string.english_ime_name);
        HSAnalytics.logGoogleAnalyticsEvent(screenName, "APP", "NativeAd_" + nativeAdParams.getPlacementName() + "_" + actionSuffix, "", value, null, null);
    }
}
