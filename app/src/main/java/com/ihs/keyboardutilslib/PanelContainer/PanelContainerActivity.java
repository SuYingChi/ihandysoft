package com.ihs.keyboardutilslib.panelcontainer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.keyboardutils.panelcontainer.KeybardPanelSwitchContainer;
import com.ihs.keyboardutilslib.R;

/**
 * Created by Arthur on 16/10/21.
 */

public class PanelContainerActivity extends Activity {
    private KeybardPanelSwitchContainer panelContainer;
    private ViewGroup rootView;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what < 1000) {
                if (msg.what % 2 == 0) {
                    panelContainer.showPanel(DemoPanel5.class);
                } else {
                    panelContainer.showPanel(DemoPanel2.class);
                }
                sendEmptyMessageDelayed(++msg.what, 0);
            }
        }
    };

    private boolean barTop;
    private boolean switchPanel;
    private boolean addPanel;
    KeybardPanelSwitchContainer contain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = (ViewGroup) ViewGroup.inflate(this, R.layout.activity_panel_container, null);
        setContentView(rootView);
        final View outerBar = View.inflate(getApplicationContext(), R.layout.view_tabbar, null);
        initouter(outerBar);


        final View innerBar = View.inflate(getApplicationContext(), R.layout.view_tabbar, null);
        contain = new KeybardPanelSwitchContainer(innerBar, KeybardPanelSwitchContainer.TABBAR_BOTTOM);
        contain.showPanel(DemoPanel5.class);
    }

    private void initouter(View outerBar) {
        panelContainer = new KeybardPanelSwitchContainer(outerBar, KeybardPanelSwitchContainer.TABBAR_TOP);
        rootView.addView(panelContainer);
        panelContainer.showPanel(DemoPanel.class);
        outerBar.findViewById(R.id.btn_showpanel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                barTop = !barTop;
                if (barTop) {
                    panelContainer.setTabBarPosition(KeybardPanelSwitchContainer.TABBAR_BOTTOM);
                } else {
                    panelContainer.setTabBarPosition(KeybardPanelSwitchContainer.TABBAR_TOP);
                }
            }
        });
        outerBar.findViewById(R.id.btn_show2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPanel = !addPanel;

                if (addPanel) {
                    panelContainer.addMoreContainer(contain);
                } else {
                    panelContainer.removeContainer(contain);
                }

            }
        });
        outerBar.findViewById(R.id.btn_show3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchPanel = !switchPanel;
                if (switchPanel) {
                    panelContainer.showPanel(DemoPanel4.class);
                } else {
                    panelContainer.showPanel(DemoPanel.class);
                }
                addPanel = false;
            }
        });
        outerBar.findViewById(R.id.btn_show4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.sendEmptyMessageDelayed(0, 1);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
