package com.ihs.feature.softgame;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Window;
import android.view.WindowManager;

import com.ihs.keyboardutils.R;
import com.kc.utils.KCAnalytics;

public class SoftGameDisplayActivity extends FragmentActivity implements ViewPager.OnPageChangeListener {
    public static final String SHOW_WHEN_LOCKED = "show_when_locked";
    public static final String TOP_50_GAME = "http://api.famobi.com/feed?a=A-KCVWU&n=50&sort=top_games";
    public static final String TOP_NEW_GAME = "http://api.famobi.com/feed?a=A-KCVWU&n=50";
    private ViewPager mViewPager;
    public static final String SOFT_GAME_PLACEMENT_MESSAGE = "soft_game_placement_msg";
    private String placementName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        boolean showWhenLocked = getIntent().getBooleanExtra(SHOW_WHEN_LOCKED, false);
        if (showWhenLocked) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }
        setContentView(R.layout.activity_soft_game_display);

        if (getIntent() != null) {
            placementName = getIntent().getStringExtra(SOFT_GAME_PLACEMENT_MESSAGE);
            if (getIntent().getBooleanExtra("fromShortcut", false)) {
                KCAnalytics.logEvent("h5games_shortcut_clicked");
            }
        }

        TabLayout mTabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(this);

        mTabLayout.setupWithViewPager(mViewPager);//将TabLayout和ViewPager关联起来。
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }


    @Override
    public void onPageSelected(int position) {
        //设置当前要显示的View
        mViewPager.setCurrentItem(position);
        //选中对应的Tab
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        private String[] mTitles = new String[]{"HOT GAMES"};

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            switch (pos) {
                case 0:
                    return SoftGameItemFragment.newInstance(TOP_50_GAME, placementName);
                case 1:
                    return SoftGameItemFragment.newInstance(TOP_NEW_GAME, placementName);
                default:
                    return SoftGameItemFragment.newInstance(TOP_50_GAME, placementName);
            }
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }
    }
}