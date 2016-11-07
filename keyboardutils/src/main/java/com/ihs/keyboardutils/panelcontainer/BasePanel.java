package com.ihs.keyboardutils.panelcontainer;

import android.content.Context;
import android.view.View;

import com.ihs.app.framework.HSApplication;

/**
 * Created by Arthur on 16/10/24.
 */

public class BasePanel {
    public interface OnStateChangedListener {
        void setBarVisibility(boolean hide, boolean expandPanel);

        void setBarPosition(int position);

        void showChildPanel(Class panelClass);
    }

    protected Context context = HSApplication.getContext();
    protected View rootView = null;
    protected OnStateChangedListener containerListener;

    private KeyboardPanelSwitchContainer childContainer;

    private boolean autoRelease = true;

    public BasePanel(OnStateChangedListener containerListener) {
        this.containerListener = containerListener;
    }

    public View onCreatePanelView() {
        return null;
    }

    public View getPanelView() {
        return rootView;
    }


    public void showChildPanel(Class panelClass) {
        containerListener.showChildPanel(panelClass);
    }

    public boolean isAutoRelease() {
        return autoRelease;
    }

    public void setAutoRelease(boolean autoRelease) {
        this.autoRelease = autoRelease;
    }

}
