package com.ihs.keyboardutilslib.panelcontainer;

import android.content.Context;
import android.view.View;

import com.ihs.keyboardutilslib.MyApplication;

/**
 * Created by Arthur on 16/10/24.
 */

public class BasePanel {
    Context context = MyApplication.getContextObject();

    protected IPanelSwitcher iPanelSwitcher;

    public BasePanel(IPanelSwitcher iPanelSwitcher) {
        this.iPanelSwitcher = iPanelSwitcher;
    }

    public View onCreatePanelView() {
        return null;
    }

}
