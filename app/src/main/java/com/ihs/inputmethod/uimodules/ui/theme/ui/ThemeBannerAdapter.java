package com.ihs.inputmethod.uimodules.ui.theme.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.keyboard.HSKeyboardTheme;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.api.utils.HSImageLoader;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.theme.analytics.ThemeAnalyticsReporter;
import com.ihs.inputmethod.uimodules.ui.theme.iap.IAPManager;
import com.ihs.inputmethod.uimodules.utils.ViewConvertor;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;
import com.keyboard.core.themes.custom.KCCustomThemeManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jixiang on 16/8/22.
 */
public class ThemeBannerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {
    private final static int AUTO_SCROLL_DELAY_DEFAULT = 6000;
    private final static int MSG_WHAT_START = 1;
    private final static int MSG_WHAT_LOOP = 2;
    private static int AUTO_SCROLL_DELAY;

    private ViewPager viewPager;
    private Activity activity;
    private List<HSKeyboardTheme> keyboardThemeList;
    private boolean isStartLoop = false;
    private boolean isLoop = true;
    private boolean isInfinite = true;
    private boolean hasInit = false;
    private long lastScrollTime = 0;
    private final static int loopMultiple = 500;

    private CardView adCardView;
    private NativeAdView nativeAdView;

    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .displayer(new RoundedBitmapDisplayer(HSDisplayUtils.dip2px(2)))
            .cacheInMemory(true).cacheOnDisk(true)
            .build();
    private boolean themeAnalyticsEnabled = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_START:
                    if (isLoop) {
                        handler.sendEmptyMessageDelayed(MSG_WHAT_LOOP, AUTO_SCROLL_DELAY > 0 ? AUTO_SCROLL_DELAY : AUTO_SCROLL_DELAY_DEFAULT);
                    }
                    break;
                case MSG_WHAT_LOOP:
                    long castTimeUtilLastScrollTime = System.currentTimeMillis() - lastScrollTime;
                    if (castTimeUtilLastScrollTime >= AUTO_SCROLL_DELAY) {
                        int position = viewPager.getCurrentItem() + 1;
                        if (position >= getCount()) {
                            position = getInitItem();
                        }
                        int delayTime = AUTO_SCROLL_DELAY > 0 ? AUTO_SCROLL_DELAY : AUTO_SCROLL_DELAY_DEFAULT;
                        if (adCardView != null && keyboardThemeList.get(position % getRealCount()) == null) {
                            delayTime = HSConfig.optInteger(AUTO_SCROLL_DELAY_DEFAULT, "Application", "ThemeContents", "themeConfig", "BannerNativeAdAutoScrollDelay");
                        }
                        viewPager.setCurrentItem(position);
                        if (isLoop) {
                            handler.sendEmptyMessageDelayed(MSG_WHAT_LOOP, delayTime);
                        }
                    } else {
                        if (isLoop) {
                            handler.sendEmptyMessageDelayed(MSG_WHAT_LOOP, (AUTO_SCROLL_DELAY > 0 ? AUTO_SCROLL_DELAY : AUTO_SCROLL_DELAY_DEFAULT) - castTimeUtilLastScrollTime);
                        }
                    }
                    break;
            }
        }
    };


    public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
        viewPager.addOnPageChangeListener(this);
    }

    public void startToFetchNativeAd() {
        if (!IAPManager.getManager().hasPurchaseNoAds()) {
            if (adCardView == null) {
                View view = LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.ad_style_1, null);
                nativeAdView = new NativeAdView(activity, view);
                nativeAdView.setOnAdLoadedListener(new NativeAdView.OnAdLoadedListener() {
                    @Override
                    public void onAdLoaded(NativeAdView nativeAdView) {
                        nativeAdView.setTag("NativeAd");
                        adCardView = ViewConvertor.toCardView(nativeAdView);
                        adCardView.setCardBackgroundColor(0xfff6f6f6);
                        scrollToPreAdPosition();
                    }
                });
                nativeAdView.configParams(new NativeAdParams(HSApplication.getContext().getString(R.string.ad_placement_cardad)));
            }
        }
    }

    private void removeNativeAdView() {
        if (adCardView != null) {
            nativeAdView.release();
            adCardView.removeAllViews();
            adCardView = null;

            int count = getRealCount();
            int currentItem = viewPager.getCurrentItem();
            keyboardThemeList.remove(null);
            notifyDataSetChanged();
            int newCurrent = currentItem + (currentItem % count - currentItem % (count - 1));
            viewPager.setCurrentItem(newCurrent, false);
        }
    }

    private final INotificationObserver notificationObserver = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (HSKeyboardThemeManager.HS_NOTIFICATION_THEME_LIST_CHANGED.equals(s)) {
                updateData();
                notifyDataSetChanged();
            } else if (ThemeHomeFragment.NOTIFICATION_THEME_HOME_DESTROY.equals(s)) {
                recycle();
            } else if (ThemeHomeFragment.NOTIFICATION_THEME_HOME_STOP.equals(s)) {
                stopAutoScroll();
            } else if (ThemeHomeFragment.NOTIFICATION_REMOVEADS_PURCHASED.equals(s)) {
                removeNativeAdView();
            }

        }
    };

    public void updateData() {
        LinkedList<HSKeyboardTheme> keyboardThemeArrayList = new LinkedList<>();
        // 获取所有的非自定义主题
        List<HSKeyboardTheme> allKeyboardThemesExceptCustomTheme = new ArrayList<>();
        allKeyboardThemesExceptCustomTheme.addAll(HSKeyboardThemeManager.getAllKeyboardThemeList());
        allKeyboardThemesExceptCustomTheme.removeAll(KCCustomThemeManager.getInstance().getAllCustomThemes());

        // 获取banner主题
        List<String> bannerThemeNames = (List<String>) HSConfig.getList("Application", "ThemeContents", "BannerList");
        for (String bannerThemeName : bannerThemeNames) {
            for (HSKeyboardTheme keyboardTheme : allKeyboardThemesExceptCustomTheme) {
                if (keyboardTheme.mThemeName.equals(bannerThemeName)) {
                    keyboardThemeArrayList.add(keyboardTheme);
                }
            }
        }

        LinkedList<HSKeyboardTheme> unDownloadBannerThemes = new LinkedList<>();

        Iterator<HSKeyboardTheme> iterator = keyboardThemeArrayList.iterator();
        while(iterator.hasNext()) {
            HSKeyboardTheme theme = iterator.next();
            if(!HSKeyboardThemeManager.getDownloadedThemeList().contains(theme)) {
                unDownloadBannerThemes.add(theme);
            }
        }

        int count = keyboardThemeList.size();
        if(unDownloadBannerThemes.size() > 0) {
            for(HSKeyboardTheme keyboardTheme : unDownloadBannerThemes) {
                if(!keyboardThemeList.contains(keyboardTheme)) {
                    keyboardThemeList.add(keyboardTheme);
                    break;
                }
            }

            Iterator<HSKeyboardTheme> iterator1 = keyboardThemeList.iterator();
            while(iterator1.hasNext() && keyboardThemeList.size() > count) {
                HSKeyboardTheme theme = iterator1.next();
                if(HSKeyboardThemeManager.getDownloadedThemeList().contains(theme)) {
                    iterator1.remove();
                }
            }
        }

        setData(keyboardThemeList.subList(0, count));
    }

    public void initData() {
        LinkedList<HSKeyboardTheme> keyboardThemeArrayList = new LinkedList<>();
        // 获取所有的非自定义主题
        List<HSKeyboardTheme> allKeyboardThemesExceptCustomTheme = new ArrayList<>();
        allKeyboardThemesExceptCustomTheme.addAll(HSKeyboardThemeManager.getAllKeyboardThemeList());
        allKeyboardThemesExceptCustomTheme.removeAll(HSKeyboardThemeManager.getCustomThemeList());

        // 获取banner主题
        List<String> bannerThemeNames = (List<String>) HSConfig.getList("Application", "ThemeContents", "BannerList");
        for (String bannerThemeName : bannerThemeNames) {
            for (HSKeyboardTheme keyboardTheme : allKeyboardThemesExceptCustomTheme) {
                if (keyboardTheme.mThemeName.equals(bannerThemeName)) {
                    keyboardThemeArrayList.add(keyboardTheme);
                }
            }
        }

        int bannerThemeCount = bannerThemeNames.size();

        // 调整keyboardThemeArrayList的size = 5
        Iterator<HSKeyboardTheme> iterator = keyboardThemeArrayList.descendingIterator();
        while (iterator.hasNext()) {
            if (keyboardThemeArrayList.size() > 5) {
                HSKeyboardTheme theme = iterator.next();
                for (HSKeyboardTheme downloadTheme : HSKeyboardThemeManager.getDownloadedThemeList()) {
                    if (downloadTheme.mThemeName.equals(theme.mThemeName)) {
                        iterator.remove();
                        break;
                    }
                }

            } else {
                break;
            }
        }

        // 查找展示次数没有超过5次的主题索引
        int count = keyboardThemeArrayList.size();
        List<Integer> fetchShouldShowIndex = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (fetchShouldShowIndex.size() < 5) {
                HSKeyboardTheme theme = keyboardThemeArrayList.get(i);
                if (HSPreferenceHelper.getDefault().getInt(theme.mThemeName + "_show_count", 0) <= 5) {
                    fetchShouldShowIndex.add(i);
                }
            } else {
                break;
            }
        }

        // 当少于5个时，从头部取，补足5个
        int fetchShouldShowIndexCount = fetchShouldShowIndex.size();
        if (fetchShouldShowIndexCount < 5) {
            int i = 0;
            while (fetchShouldShowIndex.size() < 5 && i < bannerThemeCount) {
                if (!fetchShouldShowIndex.contains(i)) {
                    fetchShouldShowIndex.add(i);
                }
                i++;
            }
        }
        // 如果 所有的 都已经超过 5次，则清空 计数器
        if (fetchShouldShowIndexCount == 0) {
            for (int i = 0; i < count; i++) {
                HSKeyboardTheme theme = keyboardThemeArrayList.get(i);
                HSPreferenceHelper.getDefault().putInt(theme.mThemeName + "_show_count", 0);
            }
        }

        List<HSKeyboardTheme> result = new ArrayList<>();

        for (Integer index : fetchShouldShowIndex) {
            result.add(keyboardThemeArrayList.get(index));
        }

        setData(result);
    }

    public void setData(List<HSKeyboardTheme> keyboardThemeList) {
        this.keyboardThemeList = keyboardThemeList;
        if (keyboardThemeList != null) {
            if (keyboardThemeList.size() == 0) {
                viewPager.setVisibility(View.GONE);
                setLoop(false);
            } else {
                setLoop(true);
            }
        }
    }

    public ThemeBannerAdapter(Activity activity) {
        this.activity = activity;
        AUTO_SCROLL_DELAY = HSConfig.optInteger(AUTO_SCROLL_DELAY_DEFAULT, "Application", "ThemeContents", "themeConfig", "bannerAutoScrollDelay");
        HSGlobalNotificationCenter.addObserver(ThemeHomeFragment.NOTIFICATION_THEME_HOME_DESTROY, notificationObserver);
        HSGlobalNotificationCenter.addObserver(ThemeHomeFragment.NOTIFICATION_THEME_HOME_STOP, notificationObserver);
        HSGlobalNotificationCenter.addObserver(HSKeyboardThemeManager.HS_NOTIFICATION_THEME_LIST_CHANGED, notificationObserver);
        HSGlobalNotificationCenter.addObserver(ThemeHomeFragment.NOTIFICATION_REMOVEADS_PURCHASED, notificationObserver);
    }

    private int getInitItem() {
        if (isInfinite && getRealCount() > 1) {
            return getRealCount() * loopMultiple / 2 -
                    (getRealCount() * loopMultiple / 2 % getRealCount());
        } else {
            return 0;
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return isLoop ? getRealCount() * loopMultiple : getRealCount();
    }

    public int getRealCount() {
        if (keyboardThemeList == null || keyboardThemeList.size() == 0) {
            return 0;
        }
        return keyboardThemeList.size();
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // 如果当前是广告位,并且广告存在
        if (adCardView != null && keyboardThemeList.get(position % getRealCount()) == null) {
            if (adCardView.getParent() == container) {
                container.removeView(adCardView);
            }
            container.addView(adCardView);
            return adCardView;
        }

        int newPosition = position % getRealCount();
        ThemeBannerView view = (ThemeBannerView) View.inflate(container.getContext(), R.layout.item_theme_banner, null);

        if (keyboardThemeList.size() > 0) {
            final HSKeyboardTheme keyboardTheme = keyboardThemeList.get(newPosition);
            if (themeAnalyticsEnabled && !ThemeAnalyticsReporter.getInstance().isThemeReported(keyboardTheme.mThemeName)) {
                view.setUpOnScrollChangedListener();
                view.setTag(keyboardTheme.mThemeName);
            } else {
                view.shutDownOnScrollChangedListener();
            }
            final ImageView imageView = (ImageView) view.findViewById(R.id.theme_banner_image);
            if (keyboardTheme.getThemeBannerImgUrl() != null) {
                imageView.setImageResource(R.drawable.image_placeholder);
                HSImageLoader.getInstance().displayImage(keyboardTheme.getThemeBannerImgUrl(), imageView, displayImageOptions);

            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, ThemeDetailActivity.class);
                    intent.putExtra(ThemeDetailActivity.INTENT_KEY_THEME_NAME, keyboardTheme.mThemeName);
                    activity.startActivity(intent);
                    HSGoogleAnalyticsUtils.getInstance().logKeyboardEvent("store_banner_clicked", keyboardTheme.mThemeName);
                    if (ThemeAnalyticsReporter.getInstance().isThemeAnalyticsEnabled()) {
                        ThemeAnalyticsReporter.getInstance().recordBannerThemeClick(keyboardTheme.mThemeName);
                    }
                }
            });
        }
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (object != adCardView) {
            container.removeView((View) object);
        }
    }


    public void startAutoScroll() {
        if (!isStartLoop && getRealCount() > 1 && isLoop) {
            isStartLoop = true;
            handler.removeMessages(MSG_WHAT_START);
            handler.sendEmptyMessage(MSG_WHAT_START);
            if (!hasInit) {
                hasInit = true;
                int initItem = getInitItem();
                viewPager.setCurrentItem(initItem);
            }
        }
    }

    public void stopAutoScroll() {
        if (isStartLoop) {
            isStartLoop = false;
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        lastScrollTime = System.currentTimeMillis();
    }

    @Override
    public void onPageSelected(int position) {
        // 记录当前item显示次数
        int newPosition = position % getRealCount();
        if (keyboardThemeList.get(newPosition) == null) {
            return;
        }
        String themeName = keyboardThemeList.get(newPosition).mThemeName;
        HSLog.e("CurrentPosition:" + themeName + "; Position:" + newPosition);
        int currentShowCount = HSPreferenceHelper.getDefault().getInt(themeName + "_show_count", 0);
        ++currentShowCount;
        HSPreferenceHelper.getDefault().putInt(themeName + "_show_count", currentShowCount);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    /**
     * @return the is Loop
     */
    public boolean isLoop() {
        return isLoop;
    }

    /**
     * @param isLoop the is InfiniteLoop to set
     */
    public void setLoop(boolean isLoop) {
        this.isLoop = isLoop;
    }

    public void recycle() {
        if (adCardView != null) {
            ((NativeAdView) adCardView.getChildAt(0)).release();
        }
        viewPager.removeAllViews();

        HSGlobalNotificationCenter.removeObserver(notificationObserver);
    }

    public void scrollToPreAdPosition() {
        if (adCardView != null) {
            if (viewPager != null) {
                int count = getRealCount();
                int currentItem = viewPager.getCurrentItem();
                keyboardThemeList.add(currentItem % getRealCount() + 1, null);
                notifyDataSetChanged();
                int newCurrent = currentItem + (currentItem % count - currentItem % (count + 1));
                viewPager.setCurrentItem(newCurrent, false);
                final int fNewCurrent = newCurrent + 1;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        viewPager.setCurrentItem(fNewCurrent);
                    }
                }, 800);

            }
        }
    }

    public void setThemeAnalyticsEnabled(boolean isThemeAnalyticsEnabled) {
        this.themeAnalyticsEnabled = isThemeAnalyticsEnabled;
    }
}
