package com.ihs.keyboardutilslib.panelcontainer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.ihs.keyboardutils.panelcontainer.KeyboardPanelSwitchContainer;
import com.ihs.keyboardutilslib.R;
import com.ihs.keyboardutilslib.panelcontainer.SwitchTab.CommonTabLayout;
import com.ihs.keyboardutilslib.panelcontainer.SwitchTab.CustomTabEntity;
import com.ihs.keyboardutilslib.panelcontainer.SwitchTab.OnTabSelectListener;
import com.ihs.keyboardutilslib.panelcontainer.SwitchTab.TabEntity;
import com.ihs.keyboardutilslib.panelcontainer.SwitchTab.ViewFindUtils;

import java.util.ArrayList;

public class CommonTabActivity extends Activity {
    private Context mContext = this;

    private String[] mTitles = {"首页", "消息", "联系人", "更多"};
    private int[] mIconUnselectIds = {
            R.mipmap.tab_home_unselect, R.mipmap.tab_speech_unselect,
            R.mipmap.tab_contact_unselect, R.mipmap.tab_more_unselect};
    private int[] mIconSelectIds = {
            R.mipmap.tab_home_select, R.mipmap.tab_speech_select,
            R.mipmap.tab_contact_select, R.mipmap.tab_more_select};
    private ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();
    private View mDecorView;
    private CommonTabLayout mTabLayout_1;
    private CommonTabLayout mTabLayout_8;
    private KeyboardPanelSwitchContainer panelContainer;


    private ArrayList<Class> panelList = new ArrayList<>();

    private Handler handler =  new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);


        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_tab);

        panelList.add(DemoPanel.class);
        panelList.add(DemoPanel2.class);
        panelList.add(DemoPanel4.class);
        panelList.add(DemoPanel5.class);

        for (int i = 0; i < mTitles.length; i++) {
            mTabEntities.add(new TabEntity(mTitles[i], mIconSelectIds[i], mIconUnselectIds[i]));
        }

        mDecorView = getWindow().getDecorView();
        /** with nothing */
        mTabLayout_1 = ViewFindUtils.find(mDecorView, R.id.tl_1);
        /** indicator圆角色块 */
        mTabLayout_8 = ViewFindUtils.find(mDecorView, R.id.tl_8);

        FrameLayout changVG = ViewFindUtils.find(mDecorView, R.id.fl_change);

        mTabLayout_1.setTabData(mTabEntities);
        mTabLayout_8.setTabData(mTabEntities);
        mTabLayout_8.setCurrentTab(2);

        panelContainer = new KeyboardPanelSwitchContainer();

        ((ViewGroup) mTabLayout_1.getParent()).removeView(mTabLayout_1);
        panelContainer.setBarView(mTabLayout_1);
        changVG.addView(panelContainer);

        panelContainer.setOnPanelChangedListener(new KeyboardPanelSwitchContainer.OnPanelChangedListener() {
            @Override
            public void onPanelChanged(Class panelClass) {
                mTabLayout_1.setCurrentTab(panelList.indexOf(panelClass));
//                mTabLayout_1.
            }
        });

        mTabLayout_1.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                panelContainer.showPanel(panelList.get(position));
            }

            @Override
            public void onTabReselect(int position) {

            }
        });


        mTabLayout_1.setCurrentTab(1);
//        panelContainer.showPanel(panelList.get(1));
    }

}
