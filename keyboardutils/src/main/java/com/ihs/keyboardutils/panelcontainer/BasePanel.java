package com.ihs.keyboardutils.panelcontainer;

import android.animation.Animator;
import android.content.Context;
import android.view.View;

import com.ihs.app.framework.HSApplication;

/**
 * Created by Arthur on 16/10/24.
 */

public abstract class BasePanel {
    public interface OnStateChangedListener {
        void setBarVisibility(boolean hide, boolean expandPanel);

        void setBarPosition(int position);

        void showChildPanel(Class panelClass);

        void backToParentPanel(boolean keepSelf);

        View getKeyboardView();

        void showPanel(Class panelClass);
    }

    protected Context context = HSApplication.getContext();
    protected View rootView = null;


    protected OnStateChangedListener containerListener;

    public BasePanel() {
    }

    protected abstract View onCreatePanelView();

    /**
     * return 代表是否执行动画，appearMode 参数为下一个panel出现的模式 为MODE_常量之一
     *
     * @param appearMode
     * @return
     */
    protected boolean onHidePanelView(int appearMode) {
        return false;
    }

    /**
     * return 代表是否执行动画，appearMode 参数为下一个panel出现的模式 为MODE_常量之一
     *
     * @param appearMode
     * @return
     */
    protected boolean onShowPanelView(int appearMode) {
        return false;
    }

    protected void onDestroy() {
    }

    public View getPanelView() {
        return rootView;
    }

    public void setPanelView(View rootView) {
        this.rootView = rootView;
    }

    public void showChildPanel(Class panelClass) {
        containerListener.showChildPanel(panelClass);
    }

    public void setBarVisibility(boolean hide, boolean expandPanel) {
        containerListener.setBarVisibility(hide, expandPanel);
    }

    public View getKeyboardView() {
        return containerListener.getKeyboardView();
    }

    public Animator getAppearAnimator() {
        return null;
    }

    public Animator getDismissAnimator() {
        return null;
    }

    public void setContainerListener(OnStateChangedListener onStateChangedListener) {
        this.containerListener = onStateChangedListener;
    }

    public OnStateChangedListener getContainerListener() {
        return containerListener;
    }

    public void backToParentPanel(boolean keepSelf) {
        containerListener.backToParentPanel(keepSelf);
    }
}
