package com.ihs.keyboardutilslib.panelcontainer;

import android.view.LayoutInflater;
import android.view.View;

import com.ihs.keyboardutils.panelcontainer.BasePanel;
import com.ihs.keyboardutilslib.R;

/**
 * Created by Arthur on 16/10/24.
 */

public class DemoPanel5 extends BasePanel {
    private boolean hidePanel;

    public DemoPanel5(OnStateChangedListener barListener) {
        super(barListener);
    }

    @Override
    public View onCreatePanelView() {
        rootView = LayoutInflater.from(context).inflate(R.layout.panel_container5, null);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePanel = !hidePanel;
                containerListener.setBarVisibility(hidePanel, false);
            }
        });

        rootView.findViewById(R.id.btn_demo5_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return rootView;
    }
}
