package com.ihs.keyboardutilslib.nativeads;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutilslib.R;
import com.ihs.nativeads.base.api.HSNativeAd;
import com.ihs.nativeads.base.api.HSNativeAdContainerView;
import com.ihs.nativeads.base.api.HSNativeAdFactory;
import com.ihs.nativeads.base.api.HSNativeAdPrimaryView;
import com.ihs.nativeads.base.api.INativeAdListener;

/**
 * Created by ihandysoft on 16/10/19.
 * <p>
 * 负责广告View的生成、广告数据的展示、广告的响应事件(click)、广告的flurry事件
 */

public class RefreshNativeAdView extends FrameLayout {

    private INotificationObserver nativeAdObserver = new INotificationObserver() {

        @Override
        public void onReceive(String notificationName, HSBundle bundle) {
            HSGlobalNotificationCenter.removeObserver(nativeAdObserver);
            hasNativeAdObserver = false;
            pauseLoadAdDataFrequentlyFromNativeAdPool();
            isStopped = false;
            loadAdDataFrequently();
        }
    };

    public interface NativeAdListener {
        public void onNativeAdShowed(HSNativeAd hsNativeAd);
        public void onNativeAdClicked(HSNativeAd hsNativeAd);
    }

    private String poolName;
    private int primaryWidth;
    private float primaryHWRatio;

    private HSNativeAdContainerView nativeAdContainerView;

    private NativeAdListener nativeAdListener;

    private long fetchNativeAdInterval;
    private boolean isStopped = false;

    private boolean hasNativeAdObserver = false;

    private Handler handler = new Handler();

    public RefreshNativeAdView(Context context, String poolName, int layoutId, int fetchNativeAdInterval) {
        this(context, poolName, layoutId, 1, fetchNativeAdInterval);
    }

    public RefreshNativeAdView(Context context, String poolName, int layoutId, float primaryHWRatio, int fetchNativeAdInterval) {
        this(context, null, 0, poolName, layoutId, primaryHWRatio, fetchNativeAdInterval);
    }

    public RefreshNativeAdView(Context context, AttributeSet attrs, int defStyleAttr, String poolName, int layoutId, float primaryHWRatio, int fetchNativeAdInterval) {
        super(context, attrs, defStyleAttr);
        this.poolName = poolName;
        this.primaryWidth = -1;
        this.primaryHWRatio = primaryHWRatio;
        this.hasNativeAdObserver = false;
        this.fetchNativeAdInterval = fetchNativeAdInterval;
        View view = View.inflate(HSApplication.getContext(), layoutId, null);
        this.nativeAdContainerView = HSNativeAdFactory.getInstance().createNativeAdContainerView(view.getContext(), view);
        initNativeAdContainerView(view);

        addView(nativeAdContainerView);
        tryLoadNativeAd();
    }

    public RefreshNativeAdView(Context context, String poolName, int layoutId, int fetchNativeAdInterval, NativeAdListener nativeAdListener) {
        this(context, poolName, layoutId, 1, fetchNativeAdInterval, nativeAdListener);
    }

    public RefreshNativeAdView(Context context, String poolName, int layoutId, float primaryHWRatio, int fetchNativeAdInterval, NativeAdListener nativeAdListener) {
        this(context, null, 0, poolName, layoutId, primaryHWRatio, fetchNativeAdInterval, nativeAdListener);
    }

    public RefreshNativeAdView(Context context, AttributeSet attrs, int defStyleAttr, String poolName, int layoutId, float primaryHWRatio, int fetchNativeAdInterval, NativeAdListener nativeAdListener) {
        super(context, attrs, defStyleAttr);
        this.poolName = poolName;
        this.primaryWidth = -1;
        this.primaryHWRatio = primaryHWRatio;
        this.hasNativeAdObserver = false;
        this.fetchNativeAdInterval = fetchNativeAdInterval;
        View view = View.inflate(HSApplication.getContext(), layoutId, null);
        this.nativeAdContainerView = HSNativeAdFactory.getInstance().createNativeAdContainerView(view.getContext(), view);
        initNativeAdContainerView(view);
        this.nativeAdListener = nativeAdListener;

        addView(nativeAdContainerView);
        tryLoadNativeAd();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == GONE || visibility == INVISIBLE) {
            if (hasNativeAdObserver) {
                HSGlobalNotificationCenter.removeObserver(nativeAdObserver);
            }
            pauseLoadAdDataFrequentlyFromNativeAdPool();
        } else if (visibility == VISIBLE) {
            if (hasNativeAdObserver) {
                HSGlobalNotificationCenter.addObserver(poolName, nativeAdObserver);
            }
            resumeLoadAdDataFrequentlyFromNativeAdPool();
        }
    }

    private void initNativeAdContainerView(View view) {
        View view1 = view.findViewById(R.id.ad_call_to_action);
        if (view1 != null) {
            nativeAdContainerView.setAdActionView(view1);
        }
        view1 = view.findViewById(R.id.ad_title);
        if (view1 != null) {
            nativeAdContainerView.setAdTitleView((TextView) view1);
        }
        view1 = view.findViewById(R.id.ad_subtitle);
        if (view1 != null) {
            nativeAdContainerView.setAdSubTitleView((TextView) view1);
        }
        view1 = view.findViewById(R.id.ad_icon);
        if (view1 != null) {
            nativeAdContainerView.setAdIconView((ImageView) view1);
        }
        view1 = view.findViewById(R.id.ad_cover_img);
        if (view1 != null) {
            nativeAdContainerView.setAdPrimaryView((HSNativeAdPrimaryView) view1);
        }
        view1 = view.findViewById(R.id.ad_choice);
        if (view1 != null) {
            nativeAdContainerView.setAdChoiceView((FrameLayout) view1);
        }
    }

    public void setNativeAdListener(NativeAdListener nativeAdListener) {
        this.nativeAdListener = nativeAdListener;
    }

    private void adjustNativeAdContainerView(HSNativeAd hsNativeAd) {
        ViewGroup.LayoutParams layoutParams = nativeAdContainerView.getContainerView().getLayoutParams();
        layoutParams.width = primaryWidth;
        nativeAdContainerView.getContainerView().setLayoutParams(layoutParams);

        if (nativeAdContainerView.getAdSubTitleView() != null && ((TextView) nativeAdContainerView.getAdSubTitleView()).getText().toString().trim().equals("")) {
            ((TextView) nativeAdContainerView.getAdSubTitleView()).setVisibility(View.GONE);
        }

        if (nativeAdContainerView.getAdPrimaryView() != null) {
            nativeAdContainerView.getAdPrimaryView().getNormalImageView().setScaleType(ImageView.ScaleType.FIT_XY);
            if (primaryHWRatio == 1) {
                nativeAdContainerView.getAdPrimaryView().setLayoutParams(new FrameLayout.LayoutParams(primaryWidth, (int) (primaryWidth * primaryHWRatio)));
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
    }

    private void resumeLoadAdDataFrequentlyFromNativeAdPool() {
        isStopped = false;
        loadAdDataFrequently();
    }

    private void pauseLoadAdDataFrequentlyFromNativeAdPool() {
        isStopped = true;
        NativeAdManager.getInstance().resetCurrentNativeAdFetchTime(poolName);
        handler.removeCallbacks(frequentRunnable);
    }

    private Runnable frequentRunnable = new Runnable() {
        @Override
        public void run() {
            loadAdDataFrequently();
        }
    };

    private void tryLoadNativeAd() {
        // 如果没有到达下一次取广告的间隔，即便有新广告，也不从新取
        HSNativeAd ad = null;
        if (System.currentTimeMillis() - NativeAdManager.getInstance().getCurrentNativeAdFetchTime(poolName) >= fetchNativeAdInterval) {
            ad = NativeAdManager.getInstance().getNativeAd(poolName);
        } else {
            ad = NativeAdManager.getInstance().getCurrentNativeAd(poolName);
        }
        if (ad != null) {
            bindDataToView(ad);
        } else {
            HSGlobalNotificationCenter.addObserver(poolName, nativeAdObserver);
            hasNativeAdObserver = true;
        }
    }

    /**
     * 从缓存池里面拿1条数据，循环读取
     *
     * @return
     */
    private void loadAdDataFrequently() {
        HSLog.e("NativePoolName ======= " + toString());
        // 如果没有到达下一次取广告的间隔，即便有新广告，也不从新取
        if (System.currentTimeMillis() - NativeAdManager.getInstance().getCurrentNativeAdFetchTime(poolName) >= fetchNativeAdInterval) {
            HSNativeAd ad = NativeAdManager.getInstance().getNativeAd(poolName);
            if (ad != null) {
                bindDataToView(ad);
                if (!isStopped) {
                    handler.postDelayed(frequentRunnable, fetchNativeAdInterval);
                }
                return;
            }
        }
        if (!isStopped) {
            handler.postDelayed(frequentRunnable, fetchNativeAdInterval);
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

            HSLog.e("HSNativeAd => " + poolName + " " + toString());
            hsNativeAd.registerView(nativeAdContainerView.getContext(), nativeAdContainerView);
            // 添加事件
            if (nativeAdListener != null) {
                nativeAdListener.onNativeAdShowed(hsNativeAd);
            }
            // 调整布局
            adjustNativeAdContainerView(hsNativeAd);
            // 设置监听
            hsNativeAd.setNativeAdListener(new INativeAdListener() {
                @Override
                public void onNativeAdLoadSucceed(final HSNativeAd hsNativeAd) {
                }

                @Override
                public void onNativeAdLoadFailed(HSNativeAd hsNativeAd, int i, String s) {
                }

                @Override
                public void onNativeAdClicked(HSNativeAd hsNativeAd) {
                    if (nativeAdListener != null) {
                        nativeAdListener.onNativeAdClicked(hsNativeAd);
                    }
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
}
