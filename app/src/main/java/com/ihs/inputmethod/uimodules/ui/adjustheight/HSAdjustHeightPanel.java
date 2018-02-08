package com.ihs.inputmethod.uimodules.ui.adjustheight;

import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.panelcontainer.BasePanel;

/**
 * Created by jixiang on 18/2/7.
 */

public class HSAdjustHeightPanel extends BasePanel {

    @Override
    protected View onCreatePanelView() {
        return View.inflate(HSApplication.getContext(), R.layout.item_panel_keyboard_height, null);
    }
}
