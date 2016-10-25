package com.ihs.keyboardutilslib.panelcontainer;

import android.content.Context;
import android.view.View;

import com.ihs.app.framework.HSApplication;

/**
 * Created by Arthur on 16/10/24.
 */

public class BasePanel {
    Context context = HSApplication.getContext();
    protected View rootView = null;

    protected IPanelSwitcher iPanelSwitcher;

    public BasePanel(IPanelSwitcher iPanelSwitcher) {
        this.iPanelSwitcher = iPanelSwitcher;
    }

    public View onCreatePanelView() {
        return null;
    }

    public View getPanelView() {
        return rootView;
    }
}
