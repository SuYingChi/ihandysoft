package com.ihs.keyboardutilslib.panelcontainer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.keyboardutils.panelcontainer.BasePanel;
import com.ihs.keyboardutilslib.R;

/**
 * Created by Arthur on 16/10/24.
 */

public class DemoPanel4 extends BasePanel {
    private boolean hidePanel;

    public DemoPanel4(OnStateChangedListener barListener) {
        super(barListener);
    }

    @Override
    public View onCreatePanelView() {
        rootView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.panel_container4, null);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePanel = !hidePanel;
                if (hidePanel) {
                    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(300, 300);
                    ((ViewGroup) rootView).addView(getKeyboardView(), layoutParams);
                } else {
                    ((ViewGroup) rootView).removeView(getKeyboardView());
                }
            }
        });
        return rootView;
    }
}
