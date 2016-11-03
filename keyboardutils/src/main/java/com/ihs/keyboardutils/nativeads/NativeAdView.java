package com.ihs.keyboardutils.nativeads;

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
            if (notificationName.equals(NativeAdManager.NEW_AD_NOTIFICATION)) {
                if (poolName.equals(bundle.getString(NativeAdManager.NATIVE_AD_POOL_NAME))) {
                    startRefreshing();
                }
            }
        }
    };


    public interface NativeAdListener {
        void onNativeAdShowed();

        void onNativeAdClicked();
    }

    private String poolName;
    private int primaryWidth;
    private float primaryHWRatio;
    private long fetchNativeAdInterval;
    private NativeAdListener nativeAdListener;

    private HSNativeAdContainerView nativeAdContainerView;

    private boolean isPaused = true;
    private boolean hasObserver = false;

    private static Handler handler = new Handler();

    public NativeAdView(Context context) {
        super(context);
    }

    public NativeAdView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NativeAdView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setConfigParams(String poolName, int layoutId) {
        setConfigParams(poolName, layoutId, -1);
    }

    public void setConfigParams(String poolName, int layoutId, int fetchNativeAdInterval) {
        setConfigParams(poolName, layoutId, fetchNativeAdInterval, null);
    }

    public void setConfigParams(String poolName, int layoutId, int fetchNativeAdInterval, NativeAdListener nativeAdListener) {
        setConfigParams(poolName, layoutId, 0, fetchNativeAdInterval, nativeAdListener);
    }

    public void setConfigParams(String poolName, int layoutId, float primaryHWRatio, int fetchNativeAdInterval) {
        setConfigParams(poolName, layoutId, primaryHWRatio, fetchNativeAdInterval, null);
    }

    public void setConfigParams(String poolName, int layoutId, float primaryHWRatio, int fetchNativeAdInterval, NativeAdListener nativeAdListener) {
        pauseRefreshing();
        this.poolName = poolName;
        this.primaryWidth = -1;
        this.primaryHWRatio = primaryHWRatio;
        this.fetchNativeAdInterval = fetchNativeAdInterval;
        this.nativeAdListener = nativeAdListener;
        if (nativeAdContainerView == null) {
            initNativeAdContainerView(layoutId);
            addView(nativeAdContainerView);
        } else {
            resumeRefreshing();
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

    private void onViewStateChanged() {
        if (getWindowVisibility() == VISIBLE && getVisibility() == VISIBLE && hasWindowFocus()) {
            resumeRefreshing();
        } else {
            pauseRefreshing();
        }
    }

    private void initNativeAdContainerView(int layoutId) {
        View view = View.inflate(HSApplication.getContext(), layoutId, null);
        this.nativeAdContainerView = HSNativeAdFactory.getInstance().createNativeAdContainerView(view.getContext(), view);
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
    }

    private void resumeRefreshing() {
        if (!isPaused) {
            return;
        }
        isPaused = false;
        startRefreshing();
    }

    private void pauseRefreshing() {
        if (isPaused) {
            return;
        }
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
            HSGlobalNotificationCenter.addObserver(NativeAdManager.NEW_AD_NOTIFICATION, nativeAdObserver);
        }
    }

    /**
     * 从缓存池里面拿1条数据，循环读取
     *
     * @return
     */
    private void startRefreshing() {
        long interval = System.currentTimeMillis() - NativeAdManager.getInstance().getNativeAdProxy(poolName).getCachedNativeAdTime();
        if (interval >= fetchNativeAdInterval) {
            HSNativeAd ad = NativeAdManager.getInstance().getNativeAdProxy(poolName).getNativeAd();
            if (ad != null) {
                removeObserver();
                handler.removeCallbacks(frequentRunnable);
                bindDataToView(ad);
                if (!isPaused && fetchNativeAdInterval > 0) {
                    handler.postDelayed(frequentRunnable, fetchNativeAdInterval);
                }
                return;
            }
            ad = NativeAdManager.getInstance().getNativeAdProxy(poolName).getCachedNativeAd();
            if (ad != null) {
                bindDataToView(ad);
            }
            if (!isPaused && fetchNativeAdInterval > 0) {
                addObserver();
                handler.removeCallbacks(frequentRunnable);
                handler.postDelayed(frequentRunnable, fetchNativeAdInterval);
            }
        } else if (!isPaused && fetchNativeAdInterval > 0) {
            handler.postDelayed(frequentRunnable, fetchNativeAdInterval - interval);
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
                nativeAdContainerView.setVisibility(INVISIBLE);
                return;
            }

            HSLog.e("HSNativeAd => " + poolName + ":" + hsNativeAd.hashCode());
            hsNativeAd.registerView(nativeAdContainerView.getContext(), nativeAdContainerView);
            // 添加事件
            if (nativeAdListener != null) {
                nativeAdListener.onNativeAdShowed();
            }
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
                    if (nativeAdListener != null) {
                        nativeAdListener.onNativeAdClicked();
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
