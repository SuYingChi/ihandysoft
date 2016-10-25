package com.ihs.keyboardutilslib.panelcontainer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ihs.keyboardutilslib.R;
import com.ihs.keyboardutilslib.panelcontainer.lib.KeybardPanelSwitchContainer;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = (ViewGroup) ViewGroup.inflate(this, R.layout.activity_panel_container, null);
        setContentView(rootView);
        findViewById(R.id.text_panel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View bar = View.inflate(getApplicationContext(), R.layout.view_tabbar, null);

                panelContainer = new KeybardPanelSwitchContainer(bar, KeybardPanelSwitchContainer.TABBAR_TOP);
                rootView.addView(panelContainer);
                panelContainer.showPanel(DemoPanel.class);
                bar.findViewById(R.id.btn_showpanel).setOnClickListener(new View.OnClickListener() {
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
                bar.findViewById(R.id.btn_show2).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView textView = new TextView(getApplicationContext());
                        textView.setText("testing panel");
                        textView.setBackgroundColor(Color.GREEN);
                        KeybardPanelSwitchContainer contain = new KeybardPanelSwitchContainer(textView, KeybardPanelSwitchContainer.TABBAR_BOTTOM);
                        contain.showPanel(DemoPanel5.class);
                        panelContainer.addMoreContainer(contain);
                    }
                });
                bar.findViewById(R.id.btn_show3).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


//                        panelContainer.showPanel(DemoPanel4.class);
                    }
                });
                bar.findViewById(R.id.btn_show4).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handler.sendEmptyMessageDelayed(0, 1000);
//                        rootView.removeView(panelContainer);
//                        panelContainer = null;
//                        System.gc();
                    }
                });
//                panelContainer.setTabBar(bar);
//                panelContainer.showPanel(DemoPanel.class);
            }
        });


//        View tabbar = View.inflate(getApplicationContext(), R.layout.view_tabbar, null);
//        tabbar.findViewById(R.id.btn_showpanel).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
