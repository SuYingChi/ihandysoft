package com.ihs.keyboardutils.nativeads;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.acb.adadapter.AcbAd;
import com.acb.adadapter.AcbNativeAd;
import com.acb.adadapter.ContainerView.AcbNativeAdContainerView;
import com.acb.adadapter.ContainerView.AcbNativeAdIconView;
import com.acb.adadapter.ContainerView.AcbNativeAdPrimaryView;
import com.acb.nativeads.AcbNativeAdLoader;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.iap.RemoveAdsManager;

import java.util.List;

public class NativeAdView extends FrameLayout {

    public interface OnAdLoadedListener {
        void onAdLoaded(NativeAdView adView);
    }

    public interface OnAdClickedListener {
        void onAdClicked(NativeAdView adView);
    }

    public enum NativeAdType {
        ICON, NORMAL
    }

    private View adLayoutView;
    private View loadingView;

    private AcbNativeAdLoader adLoader;
    private AcbNativeAd nativeAd;
    private AcbNativeAdContainerView nativeAdContainerView;

    private NativeAdParams nativeAdParams;

    private OnAdLoadedListener adLoadedListener;
    private OnAdClickedListener adClickedListener;

    private boolean isPaused = true;

    private static Handler handler = new Handler();

    private boolean adLoaded = false;

    private NativeAdType nativeAdType = NativeAdType.NORMAL;

    public NativeAdView(Context context) {
        super(context);
    }

    @Deprecated
    public NativeAdView(Context context, View adLayoutView, View loadingView) {
        this(context);
        setAdLayoutView(adLayoutView);
        setLoadingView(loadingView);
    }

    @Deprecated
    public NativeAdView(Context context, View adLayoutView) {
        this(context);
        setAdLayoutView(adLayoutView);
    }

    public void setAdLayoutView(View adLayoutView) {
        this.adLayoutView = adLayoutView;
    }

    public void setLoadingView(View loadingView) {
        this.loadingView = loadingView;
    }

    public boolean isAdLoaded() {
        return adLoaded;
    }

    public void setOnAdLoadedListener(OnAdLoadedListener listener) {
        this.adLoadedListener = listener;
    }

    public void setOnAdClickedListener(OnAdClickedListener listener) {
        this.adClickedListener = listener;
    }

    public OnAdLoadedListener getOnAdLoadedListener() {
        return adLoadedListener;
    }

    public OnAdClickedListener getOnAdClickedListener() {
        return this.adClickedListener;
    }

    public void setNativeAdType(NativeAdType nativeAdType) {
        this.nativeAdType = nativeAdType;
    }

    public void configParams(NativeAdParams nativeAdParams) {
        this.nativeAdParams = nativeAdParams;

        initNativeAdContainerView(adLayoutView);

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

        View view = groupView.findViewById(actionId);
        if (view != null) {
            nativeAdContainerView.setAdActionView(view);
        }
        view = groupView.findViewById(titleId);
        if (view != null) {
            nativeAdContainerView.setAdTitleView((TextView) view);
        }
        view = groupView.findViewById(subtitleId);
        if (view != null) {
            nativeAdContainerView.setAdSubTitleView((TextView) view);
        }
        view = groupView.findViewById(iconId);
        if (view != null) {
            nativeAdContainerView.setAdIconView((AcbNativeAdIconView) view);
        }
        view = groupView.findViewById(coverImgId);
        if (view != null) {
            nativeAdContainerView.setAdPrimaryView((AcbNativeAdPrimaryView) view);
        }
        view = groupView.findViewById(choiceId);
        if (view != null) {
            nativeAdContainerView.setAdChoiceView((FrameLayout) view);
        }
        addView(nativeAdContainerView);
    }

    private void fillNativeAdContainerView(AcbNativeAd nativeAd) {
        ViewGroup.LayoutParams layoutParams = nativeAdContainerView.getContentView().getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        nativeAdContainerView.getContentView().setLayoutParams(layoutParams);

        if (nativeAdContainerView.getAdPrimaryView() != null) {
            nativeAdContainerView.getAdPrimaryView().getNormalImageView().setScaleType(nativeAdParams.getScaleType());
            ViewGroup.LayoutParams adPrimaryViewLayoutParams = nativeAdContainerView.getAdPrimaryView().getLayoutParams();
            if (nativeAdParams.getPrimaryHWRatio() == 0) {
                
            } else {
                adPrimaryViewLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                adPrimaryViewLayoutParams.height = (int) (nativeAdParams.getPrimaryWidth() / nativeAdParams.getPrimaryHWRatio());
            }
        }

        if (nativeAdContainerView.getAdSubTitleView() != null) {
            if (nativeAd.getSubtitle() != null && !nativeAd.getSubtitle().equals("")) {
                ((TextView) nativeAdContainerView.getAdSubTitleView()).setText(nativeAd.getSubtitle());
            } else if (nativeAd.getBody() != null && !nativeAd.getBody().equals("")) {
                ((TextView) nativeAdContainerView.getAdSubTitleView()).setText(nativeAd.getBody());
            } else {
                nativeAdContainerView.getAdSubTitleView().setVisibility(GONE);
            }
        }

        if (TextUtils.isEmpty(nativeAd.getIconUrl())) {
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

    public void release() {
        if (adLoader != null) {
            adLoader.cancel();
            adLoader = null;
        }
        if (nativeAd != null) {
            nativeAd.release();
            nativeAd = null;
        }
    }

    public void refresh() {
        if (RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            return;
        }

        if (adLoader != null) {
            return;
        }

        logAnalyticsEvent("Load");

        adLoader = new AcbNativeAdLoader(getContext().getApplicationContext(), nativeAdParams.getPlacementName());

        adLoader.load(1, new AcbNativeAdLoader.AcbNativeAdLoadListener() {

            @Override
            public void onAdReceived(AcbNativeAdLoader acbNativeAdLoader, List<AcbNativeAd> list) {
                if (RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
                    return;
                }

                AcbNativeAd nativeAd = list.get(0);
                adLoaded(nativeAd);
            }

            @Override
            public void onAdFinished(AcbNativeAdLoader acbNativeAdLoader, HSError hsError) {
                if (hsError != null) {
                    HSLog.e("Load native ad failed: " + hsError);
                    try {
                        ConnectivityManager cm =
                                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo netInfo = cm.getActiveNetworkInfo();
                        if (netInfo == null || !netInfo.isAvailable() || !netInfo.isConnected()) {
                            logAnalyticsEvent("NoNetwork");
                        }
                    } catch (Exception e) {
                        // 防止因为没有权限而Crash
                    }
                }

                adLoader = null;
            }
        });
    }

    private void adLoaded(final AcbNativeAd ad) {
        bindDataToView(ad);

        if (!adLoaded) {
            adLoaded = true;

            if (adLoadedListener != null) {
                adLoadedListener.onAdLoaded(NativeAdView.this);
            }
        }
    }

    /**
     * 绑定获取的广告数据
     *
     * @param nativeAd
     */
    private void bindDataToView(AcbNativeAd nativeAd) {
        if (nativeAdContainerView != null && nativeAd != null) {
            logAnalyticsEvent("Show");
            nativeAdContainerView.fillNativeAd(nativeAd);

            // 调整布局
            fillNativeAdContainerView(nativeAd);
            if (nativeAdType == NativeAdType.ICON) {
                addScaleAnimTo(nativeAdContainerView);
            }
            nativeAd.setNativeClickListener(new AcbNativeAd.AcbNativeClickListener() {
                @Override
                public void onAdClick(AcbAd acbAd) {
                    logAnalyticsEvent("Click");

                    if (HSApplication.isDebugging) {
                        Toast.makeText(getContext(), nativeAdParams.getPlacementName() + ":" + acbAd.getVendorConfig().name(), Toast.LENGTH_SHORT).show();
                    }

                    if (adClickedListener != null) {
                        adClickedListener.onAdClicked(NativeAdView.this);
                    }
                }
            });
        }
    }

    private void addScaleAnimTo(AcbNativeAdContainerView nativeAdContainerView) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(300);
        nativeAdContainerView.startAnimation(scaleAnimation);
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

    private void logAnalyticsEvent(String actionSuffix) {
        String eventName = "NativeAd_" + nativeAdParams.getPlacementName() + "_" + actionSuffix;
        HSAnalytics.logEvent(eventName);
    }

    public AcbNativeAdContainerView getNativeAdContainerView() {
        return nativeAdContainerView;
    }
}
