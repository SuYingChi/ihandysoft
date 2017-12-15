package com.artw.lockscreen;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.keyboardutils.R;


class PremiumLockerAdapter extends PagerAdapter {

    public static final int PAGE_INDEX_UNLOCK = 0;
    public static final int PAGE_INDEX_MAINFRAME = 1;

    PremiumLockerMainFrame lockerMainFrame;
    private View unlockFrame;
    private Context mContext;

    PremiumLockerAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View view = null;
        if (PAGE_INDEX_UNLOCK == position) {
            if (null == unlockFrame) {
                createUnlockFrame();
            }
            container.addView(unlockFrame);
            view = unlockFrame;
        } else if (PAGE_INDEX_MAINFRAME == position) {
            if (null == lockerMainFrame) {
                createMainFrame();
            }
            container.addView(lockerMainFrame);
            view = lockerMainFrame;
        }
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

    }

    @Override
    public int getItemPosition(Object object) {
        if (lockerMainFrame == object) return PAGE_INDEX_MAINFRAME;
        if (unlockFrame == object) return PAGE_INDEX_UNLOCK;
        return -1;
    }

    @SuppressLint("InflateParams")
    private void createUnlockFrame() {
        unlockFrame = new View(mContext);
    }

    @SuppressLint("InflateParams")
    private void createMainFrame() {
        lockerMainFrame = (PremiumLockerMainFrame) LayoutInflater.from(mContext).inflate(R.layout.premium_locker_main_frame, null);
    }
}