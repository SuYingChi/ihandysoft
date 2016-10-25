package com.ihs.keyboardutilslib.panelcontainer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.keyboardutilslib.R;

/**
 * Created by Arthur on 16/10/21.
 */

public class PanelContainerActivity extends Activity {
    private KeybardPanelSwitchContainer panelContainer;
    private ViewGroup rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = (ViewGroup) ViewGroup.inflate(this, R.layout.activity_panel_container, null);
        setContentView(rootView);
        findViewById(R.id.text_panel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                panelContainer = new KeybardPanelSwitchContainer(getApplicationContext());
                rootView.addView(panelContainer);
                View bar = View.inflate(getApplicationContext(), R.layout.view_tabbar, null);
                bar.findViewById(R.id.btn_showpanel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        panelContainer.showPanel(DemoPanel.class);
                    }
                });
                panelContainer.setTabBar(bar);
                panelContainer.showPanel(DemoPanel.class);

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

}
