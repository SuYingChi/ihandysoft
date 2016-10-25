package com.ihs.keyboardutilslib.panelcontainer;

import android.view.View;

import com.ihs.keyboardutilslib.R;

/**
 * Created by Arthur on 16/10/24.
 */

public class DemoPanel extends BasePanel {
    public DemoPanel(IPanelSwitcher iPanelSwitcher) {
        super(iPanelSwitcher);
    }

    @Override
    public View onCreatePanelView() {
        View rootView = View.inflate(context, R.layout.panel_container, null);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iPanelSwitcher.setTabBarVisibility(true, true);
            }
        });
        return rootView;
    }
}
