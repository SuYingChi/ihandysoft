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

    public static final String NOTIFICATION_NATIVE_AD_SHOWED = "NOTIFICATION_NATIVE_AD_SHOWED";
    public static final String NOTIFICATION_NATIVE_AD_CLIKED = "NOTIFICATION_NATIVE_AD_CLIKED";


    private INotificationObserver nativeAdObserver = new INotificationObserver() {

        @Override
        public void onReceive(String notificationName, HSBundle bundle) {
            if (notificationName.equals(NativeAdManager.NOTIFICATION_NEW_AD)) {
                if (poolName.equals(bundle.getString(NativeAdManager.NATIVE_AD_POOL_NAME))) {
                    startRefreshing();
                }
            }
        }
    };

    private String poolName;
    private int primaryWidth;
    private float primaryHWRatio;
    private NativeAdTimer nativeAdTimer;

    private HSNativeAdContainerView nativeAdContainerView;
    private ViewTreeObserver.OnScrollChangedListener onScrollChangedListener;

    private boolean isPaused = true;
    private boolean hasObserver = false;
    private boolean isCurrentNativeAdClicked = false;

    private int currentNativeAdHashCode;

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
        setConfigParams(poolName, layoutId, 0, fetchNativeAdInterval);
    }

    public void setConfigParams(String poolName, int layoutId, float primaryHWRatio, int fetchNativeAdInterval) {
        if (poolName.equals(this.poolName)) {
            return;
        }
        pauseRefreshing();
        this.poolName = poolName;
        this.primaryWidth = -1;
        this.primaryHWRatio = primaryHWRatio;
        this.nativeAdTimer = new NativeAdTimer(fetchNativeAdInterval);
        if (nativeAdContainerView == null) {
            initNativeAdContainerView(layoutId);
            onScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {

                @Override
                public void onScrollChanged() {
                    onViewStateChanged();
                }
            };
            nativeAdContainerView.getViewTreeObserver().addOnScrollChangedListener(onScrollChangedListener);
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


    private Rect screenRect = new Rect();

    {
        WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        Point p = new Point();
        wm.getDefaultDisplay().getSize(p);
        screenRect = new Rect(0, 0, p.x, p.y);
    }

    private void onViewStateChanged() {

        Rect rect = new Rect();
        getLocalVisibleRect(rect);
        if (getWindowVisibility() == VISIBLE && getVisibility() == VISIBLE && hasWindowFocus() && screenRect.contains(rect)) {

            resumeRefreshing();
        } else {
            pauseRefreshing();
            if (getWindowVisibility() == GONE && !hasWindowFocus()) {
                NativeAdManager.getInstance().getNativeAdProxy(poolName).release();
            }
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

        nativeAdContainerView.setBackgroundColor(Color.BLUE);
        setBackgroundColor(Color.GREEN);
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

    public void resumeRefreshing() {
        if (!isPaused) {
            return;
        }
        HSLog.e("resume");
        nativeAdTimer.resumeTimer(NativeAdManager.getInstance().getNativeAdProxy(poolName).getCachedNativeAdTime());
        isPaused = false;
        startRefreshing();

    }

    public void pauseRefreshing() {
        if (isPaused) {
            return;
        }
        HSLog.e("pause");
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
        if(hasObserver) {
            HSGlobalNotificationCenter.removeObserver(nativeAdObserver);
            hasObserver = false;
        }
    }

    private void addObserver() {
        if(!hasObserver) {
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
        if (nativeAdTimer.isExpired() || isCurrentNativeAdClicked) {
            // fetch new NativeAd
            HSNativeAd ad = NativeAdManager.getInstance().getNativeAdProxy(poolName).getNativeAd();
            if (ad != null) {
                HSLog.e("======new");
                nativeAdTimer.resetTimer(NativeAdManager.getInstance().getNativeAdProxy(poolName).getCachedNativeAdTime());
                isCurrentNativeAdClicked = false;
                removeObserver();
                handler.removeCallbacks(frequentRunnable);
                bindDataToView(ad);
                if (!isPaused) {
                    if (nativeAdTimer.getRefreshInterval() > 0) {
                        handler.postDelayed(frequentRunnable, nativeAdTimer.getRefreshInterval());
                    } else {
                        isPaused = true;
                    }
                }
                return;
            }
            // fetch cached NativeAd
            ad = NativeAdManager.getInstance().getNativeAdProxy(poolName).getCachedNativeAd();
            if (ad != null) {
                HSLog.e("======old");
                bindDataToView(ad);
            }

            if (!isPaused) {
                addObserver();
                handler.removeCallbacks(frequentRunnable);
                handler.postDelayed(frequentRunnable, nativeAdTimer.getRefreshInterval());
            }
        } else if (!isPaused) {
            handler.postDelayed(frequentRunnable, nativeAdTimer.nextFetchNativeAdInterval());
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
            boolean flag = currentNativeAdHashCode == hsNativeAd.hashCode();
            HSBundle hsBundle = new HSBundle();
            hsBundle.putBoolean("Flag", flag);
            hsBundle.putString(NativeAdManager.NATIVE_AD_POOL_NAME, poolName);
            HSGlobalNotificationCenter.sendNotification(NOTIFICATION_NATIVE_AD_SHOWED, hsBundle);
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
                    HSBundle hsBundle = new HSBundle();
                    hsBundle.putString(NativeAdManager.NATIVE_AD_POOL_NAME, poolName);
                    HSGlobalNotificationCenter.sendNotification(NOTIFICATION_NATIVE_AD_CLIKED, hsBundle);
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
                HSLog.e("not expired: " + totalDisplayDuration);
                return false;
            }
            HSLog.e("expired: " + totalDisplayDuration);
            return true;
        }

        int getRefreshInterval() {
            return refreshInterval;
        }

        long nextFetchNativeAdInterval() {
            return refreshInterval - totalDisplayDuration;
        }
    }
}
