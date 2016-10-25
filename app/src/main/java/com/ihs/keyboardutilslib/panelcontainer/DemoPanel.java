package com.ihs.keyboardutilslib.panelcontainer;

import android.view.LayoutInflater;
import android.view.View;

import com.ihs.keyboardutilslib.R;
import com.ihs.keyboardutilslib.panelcontainer.lib.BasePanel;
import com.ihs.keyboardutilslib.panelcontainer.lib.IPanelSwitcher;

/**
 * Created by Arthur on 16/10/24.
 */

public class DemoPanel extends BasePanel {
    public DemoPanel(IPanelSwitcher iPanelSwitcher) {
        super(iPanelSwitcher);
    }

    private boolean hidePanel = false;

    @Override
    public View onCreatePanelView() {
        rootView = LayoutInflater.from(context).inflate(R.layout.panel_container, null);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePanel = !hidePanel;
                iPanelSwitcher.setTabBarVisibility(hidePanel, true);
            }
        });
        return rootView;
    }
}
