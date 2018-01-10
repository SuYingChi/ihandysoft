package com.ihs.keyboardutils.nativeads;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.iap.RemoveAdsManager;

import net.appcloudbox.ads.base.AcbAd;
import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdContainerView;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdIconView;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdPrimaryView;
import net.appcloudbox.ads.nativeads.AcbNativeAdLoader;
import net.appcloudbox.common.utils.AcbError;

import java.util.ArrayList;
import java.util.List;

public class KCNativeAdView extends FrameLayout {

    public interface OnAdLoadedListener {
        void onAdLoaded(KCNativeAdView adView);
    }

    public interface OnAdClickedListener {
        void onAdClicked(KCNativeAdView adView);
    }

    public enum NativeAdType {
        ICON, NORMAL
    }

    private View adLayoutView;
    private View loadingView;

    private AcbNativeAdLoader adLoader;
    private AcbNativeAd nativeAd;
    private AcbNativeAdContainerView nativeAdContainerView;

    private String placement;

    private ImageView.ScaleType primaryViewScaleType = ImageView.ScaleType.FIT_XY;
    private Point primaryViewSize;

    private OnAdLoadedListener adLoadedListener;
    private OnAdClickedListener adClickedListener;

    private boolean isPaused = true;

    private static Handler handler = new Handler();

    private boolean adLoaded = false;

    private NativeAdType nativeAdType = NativeAdType.NORMAL;

    public KCNativeAdView(Context context) {
        this(context, null);
    }

    public KCNativeAdView(Context context, AttributeSet attrs) {
        super(context, attrs);
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

    private void initNativeAdContainerView(View groupView) {
        this.nativeAdContainerView = new AcbNativeAdContainerView(getContext());
        this.nativeAdContainerView.addContentView(groupView);

        this.nativeAdContainerView.hideAdCorner();

        int coverImgId = getResources().getIdentifier("ad_cover_img", "id", getContext().getPackageName());
        int choiceId = getResources().getIdentifier("ad_choice", "id", getContext().getPackageName());
        int actionId = getResources().getIdentifier("ad_call_to_action", "id", getContext().getPackageName());
        int titleId = getResources().getIdentifier("ad_title", "id", getContext().getPackageName());
        int iconId = getResources().getIdentifier("ad_icon", "id", getContext().getPackageName());
        int subtitleId = getResources().getIdentifier("ad_subtitle", "id", getContext().getPackageName());

        List<View> clickableViews = new ArrayList<>();
        View view = groupView.findViewById(actionId);
        if (view != null) {
            nativeAdContainerView.setAdActionView(view);
            clickableViews.add(view);
        }
        view = groupView.findViewById(titleId);
        if (view != null) {
            nativeAdContainerView.setAdTitleView((TextView) view);
            clickableViews.add(view);
        }
        view = groupView.findViewById(subtitleId);
        if (view != null) {
            nativeAdContainerView.setAdSubTitleView((TextView) view);
            clickableViews.add(view);
        }
        view = groupView.findViewById(iconId);
        if (view != null) {
            nativeAdContainerView.setAdIconView((AcbNativeAdIconView) view);
            clickableViews.add(view);
        }
        view = groupView.findViewById(coverImgId);
        if (view != null) {
            nativeAdContainerView.setAdPrimaryView((AcbNativeAdPrimaryView) view);
            clickableViews.add(view);
        }
        view = groupView.findViewById(choiceId);
        if (view != null) {
            nativeAdContainerView.setAdChoiceView((FrameLayout) view);
        }
        //TODO
//        nativeAdContainerView.setClickViewList(clickableViews);
        addView(nativeAdContainerView);
    }

    private void fillNativeAdContainerView(AcbNativeAd nativeAd) {
        ViewGroup.LayoutParams layoutParams = nativeAdContainerView.getContentView().getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        nativeAdContainerView.getContentView().setLayoutParams(layoutParams);

        if (nativeAdContainerView.getAdPrimaryView() != null) {
            nativeAdContainerView.getAdPrimaryView().setBitmapConfig(Bitmap.Config.RGB_565);
            nativeAdContainerView.getAdPrimaryView().getNormalImageView().setScaleType(primaryViewScaleType);
            ViewGroup.LayoutParams adPrimaryViewLayoutParams = nativeAdContainerView.getAdPrimaryView().getLayoutParams();
            if (primaryViewSize != null) {
                nativeAdContainerView.getAdPrimaryView().setTargetSizePX(primaryViewSize.x, primaryViewSize.y);
                adPrimaryViewLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                adPrimaryViewLayoutParams.height = primaryViewSize.y;
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

        if (nativeAdContainerView.getAdIconView() != null) {
            if (TextUtils.isEmpty(nativeAd.getIconUrl())) {
                nativeAdContainerView.getAdIconView().setVisibility(View.GONE);
            } else {
                nativeAdContainerView.getAdIconView().setBitmapConfig(Bitmap.Config.RGB_565);
            }
        }

        if (loadingView != null) {
            removeView(loadingView);
            loadingView = null;
        }
    }

    public void setPrimaryViewScaleType(ImageView.ScaleType primaryViewScaleType) {
        this.primaryViewScaleType = primaryViewScaleType;
    }

    public void setPrimaryViewSize(int x, int y) {
        if (x > 0 && y > 0) {
            this.primaryViewSize = new Point(x, y);
        } else {
            if (HSApplication.isDebugging) {
                throw new IllegalArgumentException("Invalid primary view size.");
            }
        }

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

    public void load(String placement) {
        if (TextUtils.isEmpty(placement)) {
            if (HSApplication.isDebugging) {
                throw new IllegalArgumentException("Ad placement should not be empty.");
            }
        } else {
            this.placement = placement;
            initNativeAdContainerView(adLayoutView);

            if (loadingView != null) {
                addView(this.loadingView);
            }
            refresh();
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

        adLoader = new AcbNativeAdLoader(getContext().getApplicationContext(), placement);

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
            public void onAdFinished(AcbNativeAdLoader acbNativeAdLoader, AcbError hsError) {
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
                    if(HSLog.isDebugging()) {
                        Toast.makeText(getContext(), "Ad(" + placement + ") Error: " + hsError.getMessage(), Toast.LENGTH_LONG).show();
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
                adLoadedListener.onAdLoaded(KCNativeAdView.this);
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
                        Toast.makeText(getContext(), placement + ":" + acbAd.getVendorConfig().name(), Toast.LENGTH_SHORT).show();
                    }

                    if (adClickedListener != null) {
                        adClickedListener.onAdClicked(KCNativeAdView.this);
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

    private void logAnalyticsEvent(String actionSuffix) {
        if (!TextUtils.isEmpty(placement)) {
            String eventName = "NativeAd_" + placement + "_" + actionSuffix;
            HSAnalytics.logEvent(eventName);
        }
    }

    public AcbNativeAdContainerView getNativeAdContainerView() {
        return nativeAdContainerView;
    }
}
