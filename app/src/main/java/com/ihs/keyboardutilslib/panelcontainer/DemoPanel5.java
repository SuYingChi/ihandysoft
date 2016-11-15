package com.ihs.keyboardutilslib.panelcontainer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.keyboardutils.panelcontainer.BasePanel;
import com.ihs.keyboardutils.panelcontainer.KeyboardPanelSwitchContainer;
import com.ihs.keyboardutilslib.R;

/**
 * Created by Arthur on 16/10/24.
 */

public class DemoPanel5 extends BasePanel {
    private boolean hidePanel;

    public DemoPanel5() {
        super();
    }

    @Override
    protected View onCreatePanelView() {
        rootView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.panel_container5, null);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                hidePanel = !hidePanel;
//                containerListener.setBarVisibility(hidePanel, false);
                KeyboardPanelSwitchContainer container = new KeyboardPanelSwitchContainer();
                container.showPanel(DemoPanel5.class);
                ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                container.setLayoutParams(layoutParams);
                ((ViewGroup) rootView).addView(container);
            }
        });

        rootView.findViewById(R.id.btn_demo5_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                containerListener.backToParentPanel(false);
            }
        });
        rootView.setPadding(30, 30, 30, 30);
        return rootView;
    }
}
