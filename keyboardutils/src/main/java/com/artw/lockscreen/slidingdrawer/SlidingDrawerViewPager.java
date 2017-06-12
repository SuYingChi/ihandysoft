package com.artw.lockscreen.slidingdrawer;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ihs.keyboardutils.R;

public class SlidingDrawerViewPager extends ViewPager {

    private static final int PAGE_NUM = 1;
    private static final int PAGE_TOGGLE = 0;
    private static final int PAGE_ADS = 1;

    public SlidingDrawerViewPager(Context context) {
        this(context, null);
    }

    public SlidingDrawerViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        setAdapter(new SlidingDrawerPagerAdapter(getContext()));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private class SlidingDrawerPagerAdapter extends PagerAdapter {

        private Context mContext;

        public SlidingDrawerPagerAdapter(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view;
            if (position == PAGE_TOGGLE) {
                view = LayoutInflater.from(mContext).inflate(R.layout.locker_drawer_content, null);
            } else {
                view = new ImageView(mContext);
            }

            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return PAGE_NUM;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
