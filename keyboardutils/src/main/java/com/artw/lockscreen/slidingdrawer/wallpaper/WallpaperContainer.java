package com.artw.lockscreen.slidingdrawer.wallpaper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.artw.lockscreen.LockerActivity;
import com.artw.lockscreen.LockerMainFrame;
import com.ihs.keyboardutils.R;
import com.artw.lockscreen.common.NetworkChangeReceiver;
import com.artw.lockscreen.common.ToastUtils;
import com.artw.lockscreen.slidingdrawer.SlidingDrawerContent;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static android.view.animation.Animation.INFINITE;
import static android.view.animation.Animation.RESTART;
import static com.artw.lockscreen.LockerSettings.LOCKER_PREFS;


public class WallpaperContainer extends LinearLayout implements View.OnClickListener, INotificationObserver {

    private class CycleList {
        private int mCurrentIndex = 0;

        // must not be null or empty
        private ArrayList<HashMap> mData = new ArrayList();

        public CycleList(@NonNull ArrayList<HashMap<String, String>> list) {
            mData.addAll(list);
        }

        public HashMap<String, String> getNext() {
            mCurrentIndex %= mData.size();
            mCurrentIndex++;
            return mData.get(mCurrentIndex - 1);
        }
    }

    private class WallpaperChangeDisplayer extends FadeInBitmapDisplayer {
        private int mDuration;

        public WallpaperChangeDisplayer(int durationMillis) {
            super(durationMillis);
            mDuration = durationMillis;
        }

        @Override
        public void display(final Bitmap bitmap, final ImageAware imageAware, LoadedFrom loadedFrom) {
            final ImageView thumb = (ImageView) imageAware.getWrappedView();
            if (thumb == null || bitmap == null) {
                return;
            }
            if (loadedFrom == LoadedFrom.NETWORK || loadedFrom == LoadedFrom.DISC_CACHE || loadedFrom == LoadedFrom.MEMORY_CACHE) {
                ObjectAnimator wallpaperOut = ObjectAnimator.ofFloat(thumb, "alpha", 1f, 0.5f);
                wallpaperOut.setDuration(mDuration / 2);
                wallpaperOut.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int alpha = (int) (animation.getAnimatedFraction() * MASK_HINT_COLOR_ALPHA);
                        thumb.setColorFilter(Color.argb(alpha, 0, 0, 0));
                    }
                });
                wallpaperOut.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        imageAware.setImageBitmap(bitmap);
                    }
                });

                ObjectAnimator wallpaperIn = ObjectAnimator.ofFloat(thumb, "alpha", 0.5f, 1f);
                wallpaperIn.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int alpha = (int) ((1f - animation.getAnimatedFraction()) * MASK_HINT_COLOR_ALPHA);
                        thumb.setColorFilter(Color.argb(alpha, 0, 0, 0));
                    }
                });
                wallpaperIn.setDuration(mDuration / 2);

                AnimatorSet change = new AnimatorSet();
                change.playSequentially(wallpaperOut, wallpaperIn);
                change.start();
            }
        }
    }

    private static final String TAG = "WallpaperContainer";

    private static final String PREF_KEY_WALLPAPER_FIRST_SHOWN = "wallpaper_first_shown";
    private static final String PREF_KEY_WALLPAPER_FIRST_VIEW_LAST_SUCCEED_THUMB = "wallpaper_first_view_thumb_url";
    private static final String PREF_KEY_WALLPAPER_SECOND_VIEW_LAST_SUCCEED_THUMB = "wallpaper_second_view_thumb_url";
    private static final String PREF_KEY_WALLPAPER_THIRD_VIEW_LAST_SUCCEED_THUMB = "wallpaper_third_view_thumb_url";
    private static final String PREF_KEY_WALLPAPER_FOURTH_VIEW_LAST_SUCCEED_THUMB = "wallpaper_fourth_view_thumb_url";

    private static final String PREF_KEY_WALLPAPER_FIRST_VIEW_LAST_SUCCEED_HD = "wallpaper_first_view_hd_url";
    private static final String PREF_KEY_WALLPAPER_SECOND_VIEW_LAST_SUCCEED_HD = "wallpaper_second_view_hd_url";
    private static final String PREF_KEY_WALLPAPER_THIRD_VIEW_LAST_SUCCEED_HD = "wallpaper_third_view_hd_url";
    private static final String PREF_KEY_WALLPAPER_FOURTH_VIEW_LAST_SUCCEED_HD = "wallpaper_fourth_view_hd_url";

    private static final String WALLPAPER_THUMB = "thumb";
    private static final String WALLPAPER_HD = "HD";
    private static final int MASK_HINT_COLOR_ALPHA = 0x4a;
    private static final int WALLPAPER_COUNT = 4;

    private ArrayList<ImageView> mIVImgs = new ArrayList<>(WALLPAPER_COUNT);
    private ArrayList<ImageView> mRefreshImgs = new ArrayList<>(WALLPAPER_COUNT);
    private ImageView mRefreshView;
    private RotateAnimation mRefreshRotation;
    private RotateAnimation mWallpaperRotation;

    private int mCurrentIndex = -1;
    private boolean mIsDownloadingWallpaper;
    private boolean mIsRefreshSwitchClicked;
    private boolean mFirstAutoRefresh;

    private CycleList mSourceWallpapers;
    private Random mRandomDelay = new Random();
    private ArrayList<String> mThumbUrls = new ArrayList<>(WALLPAPER_COUNT);
    private ArrayList<String> mHDUrls = new ArrayList<>(WALLPAPER_COUNT);
    private SparseBooleanArray mLoadingFinish = new SparseBooleanArray();
    private SparseBooleanArray mLoadingSucceed = new SparseBooleanArray();
    private HSPreferenceHelper mPrefer = HSPreferenceHelper.create(HSApplication.getContext(), LOCKER_PREFS);
    private ImageLoader mImageLoader = ImageLoader.getInstance();
    private DisplayImageOptions mOption = new DisplayImageOptions.Builder()
            .cacheOnDisk(true)
            .considerExifParams(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .displayer(new WallpaperChangeDisplayer(800))
            .build();

    private ImageLoadingListener mWallpaperThumbLoadingListener = new ImageLoadingListener() {
        @Override
        public void onLoadingStarted(String s, View view) {
            if (!mIsRefreshSwitchClicked || ((View) view.getTag()).getVisibility() == VISIBLE) {
                ((View) view.getTag()).startAnimation(mWallpaperRotation);
            }
        }

        @Override
        public void onLoadingFailed(String s, final View view, FailReason failReason) {
            view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(400)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ((View) view.getTag()).clearAnimation();
                            mLoadingFinish.put(view.getId(), true);
                            mLoadingSucceed.put(view.getId(), false);
                            if (isLoadingFinish()) {
                                mRefreshView.clearAnimation();
                                if (!mFirstAutoRefresh) {
                                    ToastUtils.showToast(R.string.locker_wallpaper_network_error, Toast.LENGTH_LONG);
                                }

                                //log flurry
                                if (mIsRefreshSwitchClicked) {
                                    int succeed = 0;
                                    for (int index = 0; index < mLoadingSucceed.size(); index++) {
                                        succeed += mLoadingSucceed.get(mLoadingSucceed.keyAt(index)) ? 1 : 0;
                                    }
                                    HSAnalytics.logEvent("Locker_Wallpaper_Refresh_Clicked", "success", "" + succeed);
                                    mIsRefreshSwitchClicked = false;
                                }
                            }
                        }
                    })
                    .start();
        }

        @Override
        public void onLoadingComplete(String s, final View view, Bitmap bitmap) {
            view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(400)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ((View) view.getTag()).clearAnimation();
                            mLoadingFinish.put(view.getId(), true);
                            mLoadingSucceed.put(view.getId(), true);
                            ((ImageView) view.getTag()).setVisibility(GONE);
                            if (isLoadingFinish()) {
                                mRefreshView.clearAnimation();

                                //log flurry
                                if (mIsRefreshSwitchClicked) {
                                    int succeed = 0;
                                    for (int index = 0; index < mLoadingSucceed.size(); index++) {
                                        succeed += mLoadingSucceed.get(mLoadingSucceed.keyAt(index)) ? 1 : 0;
                                    }
                                    HSAnalytics.logEvent("Locker_Wallpaper_Refresh_Clicked", "success", "" + succeed);
                                    mIsRefreshSwitchClicked = false;
                                }
                            }
                        }
                    })
                    .start();
        }

        @Override
        public void onLoadingCancelled(String s, View view) {
            ((View) view.getTag()).clearAnimation();
            mLoadingFinish.put(view.getId(), true);
            mLoadingSucceed.put(view.getId(), false);
        }
    };

    private ImageLoadingListener mWallpaperListener = new ImageLoadingListener() {
        @Override
        public void onLoadingStarted(String s, View view) {
            ((ImageView) mIVImgs.get(mCurrentIndex).getTag()).startAnimation(mRefreshRotation);
            ((ImageView) mIVImgs.get(mCurrentIndex).getTag()).setVisibility(VISIBLE);
        }

        @Override
        public void onLoadingFailed(String s, View view, FailReason failReason) {
            ((ImageView) mIVImgs.get(mCurrentIndex).getTag()).clearAnimation();
            ((ImageView) mIVImgs.get(mCurrentIndex).getTag()).setVisibility(GONE);
            ValueAnimator mask = maskAnimation(mIVImgs.get(mCurrentIndex), MASK_HINT_COLOR_ALPHA, 0x00, 400);
            if (mask != null) {
                mask.start();
            }
            mIsDownloadingWallpaper = false;
            ToastUtils.showToast(R.string.locker_wallpaper_network_error);
            HSAnalytics.logEvent("Locker_Wallpaper_Preview_Clicked", "name", s, "result", "fail");
        }

        @Override
        public void onLoadingComplete(String s, View view, final Bitmap bitmap) {
            final ImageView wallpaperView = ((LockerActivity) getContext()).getIvLockerWallpaper();
            SlidingDrawerContent silde = (SlidingDrawerContent) getParent().getParent();
            if (wallpaperView == null || silde == null) {
                mIsDownloadingWallpaper = false;
                return;
            }
            silde.setDrawerBg(bitmap);
            ObjectAnimator wallpaperOut = ObjectAnimator.ofFloat(wallpaperView, "alpha", 1f, 0.5f);
            wallpaperOut.setDuration(400);
            wallpaperOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    wallpaperView.setImageBitmap(bitmap);
                }
            });

            ObjectAnimator wallpaperIn = ObjectAnimator.ofFloat(wallpaperView, "alpha", 0.5f, 1f);
            wallpaperIn.setDuration(400);
            wallpaperIn.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int alpha = (int) ((1f - animation.getAnimatedFraction()) * MASK_HINT_COLOR_ALPHA);
                    mIVImgs.get(mCurrentIndex).setColorFilter(Color.argb(alpha, 0, 0, 0));
                }
            });
            wallpaperIn.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    ((ImageView) mIVImgs.get(mCurrentIndex).getTag()).clearAnimation();
                    ((ImageView) mIVImgs.get(mCurrentIndex).getTag()).setVisibility(GONE);
                    mIsDownloadingWallpaper = false;
                }
            });

            AnimatorSet change = new AnimatorSet();
            change.playSequentially(wallpaperOut, wallpaperIn);
            change.start();
            mPrefer.putString(LockerActivity.PREF_KEY_CURRENT_WALLPAPER_HD_URL, mHDUrls.get(mCurrentIndex));
            HSAnalytics.logEvent("Locker_Wallpaper_Preview_Clicked", "name", s, "result", "success");
        }

        @Override
        public void onLoadingCancelled(String s, View view) {
            ((ImageView) mIVImgs.get(mCurrentIndex).getTag()).clearAnimation();
            ((ImageView) mIVImgs.get(mCurrentIndex).getTag()).setVisibility(GONE);
            mIsDownloadingWallpaper = false;
        }
    };

    public WallpaperContainer(Context context) {
        super(context);
    }

    public WallpaperContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public WallpaperContainer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        initView();
        initFlags();
        fetchConfig();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mFirstAutoRefresh = true;
        if (!mPrefer.getBoolean(PREF_KEY_WALLPAPER_FIRST_SHOWN, false)) {
            refreshWallpapers();
        } else {
            prepareDownloading();
        }
        HSGlobalNotificationCenter.addObserver(LockerMainFrame.EVENT_SLIDING_DRAWER_OPENED, this);
        HSGlobalNotificationCenter.addObserver(NetworkChangeReceiver.NOTIFICATION_CONNECTIVITY_CHANGED, this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        HSGlobalNotificationCenter.removeObserver(this);
    }

    @Override
    public void onClick(View v) {

        if (mIsDownloadingWallpaper || !isLoadingFinish()) {
            return;
        }

        int caseInt = -1;
        if (v.getId() == R.id.iv_refresh) {
            caseInt = 0;
        } else if (v.getId() == R.id.iv_img1) {
            caseInt = 1;
        } else if (v.getId() == R.id.iv_img1_refresh) {
            caseInt = 2;
        } else if (v.getId() == R.id.iv_img2) {
            caseInt = 3;
        } else if (v.getId() == R.id.iv_img2_refresh) {
            caseInt = 4;
        } else if (v.getId() == R.id.iv_img3) {
            caseInt = 5;
        } else if (v.getId() == R.id.iv_img3_refresh) {
            caseInt = 6;
        } else if (v.getId() == R.id.iv_img4) {
            caseInt = 7;
        } else if (v.getId() == R.id.iv_img4_refresh) {
            caseInt = 8;
        }

        mIsRefreshSwitchClicked = false;


        switch (caseInt) {

            case 0:
                mFirstAutoRefresh = false;
                mIsRefreshSwitchClicked = true;
                refreshWallpapers();
                break;

            case 1:
                if (mRefreshImgs.get(0).getVisibility() == GONE) {
                    if (mCurrentIndex != 0) {
                        setLockerWallpaper(0);
                    }
                    break;
                }

            case 2:
                refreshWallpaper(0);
                break;

            case 3:
                if (mRefreshImgs.get(1).getVisibility() == GONE) {
                    if (mCurrentIndex != 1) {
                        setLockerWallpaper(1);
                    }
                    break;
                }

            case 4:
                refreshWallpaper(1);
                break;

            case 5:
                if (mRefreshImgs.get(2).getVisibility() == GONE) {
                    if (mCurrentIndex != 2) {
                        setLockerWallpaper(2);
                    }
                    break;
                }

            case 6:
                refreshWallpaper(2);
                break;

            case 7:
                if (mRefreshImgs.get(3).getVisibility() == GONE) {
                    if (mCurrentIndex != 3) {
                        setLockerWallpaper(3);
                    }
                    break;
                }

            case 8:
                refreshWallpaper(3);
                break;

            default:
                HSLog.e(TAG, "wrong view");
                break;

        }

    }

    private void initView() {
        mRefreshView = (ImageView) findViewById(R.id.iv_refresh);
        mRefreshView.setOnClickListener(this);

        mRefreshImgs.add((ImageView) findViewById(R.id.iv_img1_refresh));
        mRefreshImgs.add((ImageView) findViewById(R.id.iv_img2_refresh));
        mRefreshImgs.add((ImageView) findViewById(R.id.iv_img3_refresh));
        mRefreshImgs.add((ImageView) findViewById(R.id.iv_img4_refresh));
        for (ImageView view : mRefreshImgs) {
            view.setOnClickListener(this);
        }

        mIVImgs.add((ImageView) findViewById(R.id.iv_img1));
        mIVImgs.add((ImageView) findViewById(R.id.iv_img2));
        mIVImgs.add((ImageView) findViewById(R.id.iv_img3));
        mIVImgs.add((ImageView) findViewById(R.id.iv_img4));
        for (int index = 0; index < mIVImgs.size(); index++) {
            View view = mIVImgs.get(index);
            view.setOnClickListener(this);
            view.setTag(mRefreshImgs.get(index));
        }

        mRefreshRotation = new RotateAnimation(0.0f, 359.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRefreshRotation.setFillAfter(true);
        mRefreshRotation.setInterpolator(new LinearInterpolator());
        mRefreshRotation.setRepeatCount(INFINITE);
        mRefreshRotation.setRepeatMode(RESTART);
        mRefreshRotation.setDuration(1000);

        mWallpaperRotation = new RotateAnimation(0.0f, 359.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mWallpaperRotation.setFillAfter(true);
        mWallpaperRotation.setInterpolator(new LinearInterpolator());
        mWallpaperRotation.setRepeatCount(INFINITE);
        mWallpaperRotation.setRepeatMode(RESTART);
        mWallpaperRotation.setDuration(1000);
    }

    private void initFlags() {

        for (View view : mIVImgs) {
            mLoadingFinish.put(view.getId(), true);
            mLoadingSucceed.put(view.getId(), false);
        }
        mThumbUrls.add(mPrefer.getString(PREF_KEY_WALLPAPER_FIRST_VIEW_LAST_SUCCEED_THUMB, ""));
        mHDUrls.add(mPrefer.getString(PREF_KEY_WALLPAPER_FIRST_VIEW_LAST_SUCCEED_HD, ""));

        mThumbUrls.add(mPrefer.getString(PREF_KEY_WALLPAPER_SECOND_VIEW_LAST_SUCCEED_THUMB, ""));
        mHDUrls.add(mPrefer.getString(PREF_KEY_WALLPAPER_SECOND_VIEW_LAST_SUCCEED_HD, ""));

        mThumbUrls.add(mPrefer.getString(PREF_KEY_WALLPAPER_THIRD_VIEW_LAST_SUCCEED_THUMB, ""));
        mHDUrls.add(mPrefer.getString(PREF_KEY_WALLPAPER_THIRD_VIEW_LAST_SUCCEED_HD, ""));

        mThumbUrls.add(mPrefer.getString(PREF_KEY_WALLPAPER_FOURTH_VIEW_LAST_SUCCEED_THUMB, ""));
        mHDUrls.add(mPrefer.getString(PREF_KEY_WALLPAPER_FOURTH_VIEW_LAST_SUCCEED_HD, ""));
    }

    private void fetchConfig() {
        if (mSourceWallpapers == null) {
            ArrayList data = (ArrayList) HSConfig.getList("Application", "Locker", "Wallpapers");
            if (data != null && !data.isEmpty()) {
                mSourceWallpapers = new CycleList(data);
            }
        }
    }

    private void setLockerWallpaper(final int index) {
        mIsDownloadingWallpaper = true;
        mCurrentIndex = index;
        ValueAnimator mask = maskAnimation(mIVImgs.get(index), 0, MASK_HINT_COLOR_ALPHA, 400);
        if (mask == null) {
            return;
        }
        mask.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mImageLoader.loadImage(mHDUrls.get(index), mOption, mWallpaperListener);
            }
        });
        mask.start();
    }

    private void prepareDownloading() {
        for (int index = 0; index < mIVImgs.size(); index++) {
            if (mLoadingSucceed.get(mIVImgs.get(index).getId())) {
                continue;
            }
            final int rank = index;
            mIVImgs.get(index).animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(400)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            refreshWallpaper(rank);
                        }
                    })
                    .setStartDelay(mRandomDelay.nextInt(100))
                    .start();
        }
    }

    private void refreshWallpapers() {
        HSLog.d(TAG, "refresh wallpapers");
        fetchConfig();
        resetTags();
        refreshUrls();
        prepareDownloading();
    }

    private void refreshWallpaper(int index) {
        mLoadingFinish.put(mIVImgs.get(index).getId(), false);
        HSLog.d(TAG, "wallpaper index = " + index + "   thumb url = " + mThumbUrls.get(index));
        mImageLoader.displayImage(mThumbUrls.get(index), mIVImgs.get(index), mOption, mWallpaperThumbLoadingListener);
    }

    private void resetTags() {
        if (mRefreshView != null) {
            mCurrentIndex = -1;
            mRefreshView.startAnimation(mRefreshRotation);
            for (int i = 0; i < mLoadingFinish.size(); i++) {
                mLoadingFinish.put(mLoadingFinish.keyAt(i), false);
                mLoadingSucceed.put(mLoadingSucceed.keyAt(i), false);
            }
        }
    }

    private void refreshUrls() {
        if (!mPrefer.getBoolean(PREF_KEY_WALLPAPER_FIRST_SHOWN, false)) {
            mPrefer.putBoolean(PREF_KEY_WALLPAPER_FIRST_SHOWN, true);
            mThumbUrls.set(0, "drawable://" + R.drawable.wallpaper_locker_thumb);
            mHDUrls.set(0, "drawable://" + R.drawable.wallpaper_locker);
            generateUrls(3);
        } else {
            generateUrls(4);
        }

        mPrefer.putString(PREF_KEY_WALLPAPER_FIRST_VIEW_LAST_SUCCEED_THUMB, mThumbUrls.get(0));
        mPrefer.putString(PREF_KEY_WALLPAPER_FIRST_VIEW_LAST_SUCCEED_HD, mHDUrls.get(0));
        mPrefer.putString(PREF_KEY_WALLPAPER_SECOND_VIEW_LAST_SUCCEED_THUMB, mThumbUrls.get(1));
        mPrefer.putString(PREF_KEY_WALLPAPER_SECOND_VIEW_LAST_SUCCEED_HD, mHDUrls.get(1));
        mPrefer.putString(PREF_KEY_WALLPAPER_THIRD_VIEW_LAST_SUCCEED_THUMB, mThumbUrls.get(2));
        mPrefer.putString(PREF_KEY_WALLPAPER_THIRD_VIEW_LAST_SUCCEED_HD, mHDUrls.get(2));
        mPrefer.putString(PREF_KEY_WALLPAPER_FOURTH_VIEW_LAST_SUCCEED_THUMB, mThumbUrls.get(3));
        mPrefer.putString(PREF_KEY_WALLPAPER_FOURTH_VIEW_LAST_SUCCEED_HD, mHDUrls.get(3));
    }

    private void generateUrls(int count) {
        if (mSourceWallpapers != null) {
            for (int index = WALLPAPER_COUNT - count; index < WALLPAPER_COUNT; index++) {
                HashMap<String, String> url = mSourceWallpapers.getNext();
                mThumbUrls.set(index, url.get(WALLPAPER_THUMB));
                mHDUrls.set(index, url.get(WALLPAPER_HD));
            }
        }
    }

    private boolean isLoadingFinish() {
        boolean result = true;
        for (int i = 0; i < mLoadingFinish.size(); i++) {
            int id = mLoadingFinish.keyAt(i);
            if (id == R.id.iv_img1_refresh) {
                if (!mHDUrls.get(0).startsWith("drawable://")) {
                    result = result && mLoadingFinish.get(id);
                }
            } else {
                result = result && mLoadingFinish.get(id);
            }
        }
        return result;
    }

    private ValueAnimator maskAnimation(final ImageView view, int startAlpha, int endAlpha, int duration) {
        if (view == null) {
            return null;
        }
        ValueAnimator color = ValueAnimator.ofInt(startAlpha, endAlpha);
        color.setInterpolator(new AccelerateDecelerateInterpolator());
        color.setDuration(duration);
        color.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setColorFilter(Color.argb((Integer) animation.getAnimatedValue(), 0, 0, 0));
            }
        });
        return color;
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case LockerMainFrame.EVENT_SLIDING_DRAWER_OPENED:
                mIsRefreshSwitchClicked = false;
                mFirstAutoRefresh = false;
                if (!mPrefer.getBoolean(PREF_KEY_WALLPAPER_FIRST_SHOWN, false)) {
                    refreshWallpapers();
                } else {
                    prepareDownloading();
                }
                break;
            case NetworkChangeReceiver.NOTIFICATION_CONNECTIVITY_CHANGED:
                if (Utils.isNetworkAvailable(-1)) {
                    mFirstAutoRefresh = false;
                    prepareDownloading();
                }
                break;
            default:
                break;
        }
    }
}
