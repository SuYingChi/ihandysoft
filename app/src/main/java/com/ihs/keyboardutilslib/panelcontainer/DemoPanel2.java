package com.ihs.keyboardutilslib.panelcontainer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.keyboardutils.panelcontainer.BasePanel;
import com.ihs.keyboardutilslib.R;

/**
 * Created by Arthur on 16/10/24.
 */

public class DemoPanel2 extends BasePanel {
    private boolean hidePanel;

    public DemoPanel2() {
        super();
    }

    @Override
    protected View onCreatePanelView() {
        rootView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.panel_container2, null);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePanel = !hidePanel;
                containerListener.setBarVisibility(hidePanel, false);
            }
        });

        rootView.findViewById(R.id.btn_demo2_jmp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChildPanel(PullDownPanel.class);
            }
        });
        return rootView;
    }
}
