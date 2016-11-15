package com.ihs.keyboardutilslib.panelcontainer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.keyboardutils.panelcontainer.BasePanel;
import com.ihs.keyboardutilslib.R;

/**
 * Created by Arthur on 16/10/24.
 */

public class DemoPanel extends BasePanel {
    public DemoPanel() {
        super();
    }

    private boolean hidePanel = false;

    @Override
    protected View onCreatePanelView() {
        rootView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.panel_container, null);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePanel = !hidePanel;
                containerListener.setBarVisibility(hidePanel, true);
            }
        });

        return rootView;
    }
}
