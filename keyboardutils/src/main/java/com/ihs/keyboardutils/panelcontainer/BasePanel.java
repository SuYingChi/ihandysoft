package com.ihs.keyboardutils.panelcontainer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.app.framework.HSApplication;

/**
 * Created by Arthur on 16/10/24.
 */

public class BasePanel {
    public interface OnStateChangedListener {
        void setBarVisibility(boolean hide, boolean expandPanel);

        void setBarPosition(int position);

        void showChildPanel(Class panelClass);

        void backToParentPanel();

        View getKeyboardView();
    }

    protected Context context = HSApplication.getContext();
    protected View rootView = null;
    protected OnStateChangedListener containerListener;

    private KeyboardPanelSwitchContainer childContainer;


    private boolean isKeyboardPanel = false;

    public BasePanel(OnStateChangedListener containerListener) {
        this.containerListener = containerListener;
    }

    public View onCreatePanelView() {
        return null;
    }

    public ViewGroup getPanelView() {
        return (ViewGroup) rootView;
    }

    public void setPanelView(View rootView) {
        this.rootView = rootView;
    }


    public void showChildPanel(Class panelClass) {
        containerListener.showChildPanel(panelClass);
    }


    public boolean isKeyboardPanel() {
        return isKeyboardPanel;
    }

    public void setIsKeyboardPanel(boolean keyboardPanel) {
        isKeyboardPanel = keyboardPanel;
    }

    public void setBarVisibility(boolean hide, boolean expandPanel) {
        containerListener.setBarVisibility(hide, expandPanel);
    }

    public View getKeyboardView() {
        return containerListener.getKeyboardView();
    }
}
