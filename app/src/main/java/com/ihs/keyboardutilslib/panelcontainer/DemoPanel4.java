package com.ihs.keyboardutilslib.panelcontainer;

import android.view.LayoutInflater;
import android.view.View;

import com.ihs.keyboardutilslib.R;
import com.ihs.keyboardutilslib.panelcontainer.lib.BasePanel;
import com.ihs.keyboardutilslib.panelcontainer.lib.IPanelSwitcher;

/**
 * Created by Arthur on 16/10/24.
 */

public class DemoPanel4 extends BasePanel {
    public DemoPanel4(IPanelSwitcher iPanelSwitcher) {
        super(iPanelSwitcher);
    }

    @Override
    public View onCreatePanelView() {
         rootView = LayoutInflater.from(context).inflate(R.layout.panel_container, null);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iPanelSwitcher.setTabBarVisibility(true, true);
            }
        });
        return rootView;
    }
}
