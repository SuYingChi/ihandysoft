package com.ihs.keyboardutilslib.panelcontainer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.keyboardutils.panelcontainer.KeyboardPanelSwitchContainer;
import com.ihs.keyboardutilslib.R;

import static android.R.color.black;
import static com.ihs.keyboardutilslib.R.id.btn_show4;

/**
 * Created by Arthur on 16/10/21.
 */

public class PanelContainerActivity extends Activity {
    private KeyboardPanelSwitchContainer panelContainer;
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
    private boolean barTop2;

    KeyboardPanelSwitchContainer contain;
    private boolean switchPanel2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = (ViewGroup) ViewGroup.inflate(this, R.layout.activity_panel_container, null);
        setContentView(rootView);

        final View outerBar = View.inflate(getApplicationContext(), R.layout.view_tabbar, null);
        initOuter(outerBar);

        final View innerBar = View.inflate(getApplicationContext(), R.layout.view_tabbar, null);
        initInnerPanle(innerBar);
    }

    private void initInnerPanle(View innerBar) {
        innerBar.setBackgroundColor(getResources().getColor(black));
        contain = new KeyboardPanelSwitchContainer(KeyboardPanelSwitchContainer.BAR_BOTTOM);
        contain.setBarView(innerBar);
        contain.showPanel(DemoPanel5.class);
        innerBar.findViewById(R.id.btn_show4).setVisibility(View.GONE);
        innerBar.findViewById(R.id.btn_show2).setVisibility(View.GONE);
        innerBar.findViewById(R.id.btn_showpanel).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (barTop2) {
                    contain.setTabBarPosition(KeyboardPanelSwitchContainer.BAR_BOTTOM);
                } else {
                    contain.setTabBarPosition(KeyboardPanelSwitchContainer.BAR_TOP);
                }
                barTop2 = !barTop2;
            }
        });


        innerBar.findViewById(R.id.btn_show3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchPanel2 = !switchPanel2;
                if (switchPanel2) {
                    contain.showPanel(DemoPanel2.class);
                } else {
                    contain.showPanel(DemoPanel5.class);
                }
            }
        });
    }

    private void initOuter(View outerBar) {
        panelContainer = new KeyboardPanelSwitchContainer(KeyboardPanelSwitchContainer.BAR_TOP);
        panelContainer.setBarView(outerBar);
        rootView.addView(panelContainer);
        panelContainer.showPanel(DemoPanel.class);
        outerBar.findViewById(R.id.btn_showpanel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                barTop = !barTop;
                if (barTop) {
                    panelContainer.setTabBarPosition(KeyboardPanelSwitchContainer.BAR_BOTTOM);
                } else {
                    panelContainer.setTabBarPosition(KeyboardPanelSwitchContainer.BAR_TOP);
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
        outerBar.findViewById(btn_show4).setOnClickListener(new View.OnClickListener() {
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
