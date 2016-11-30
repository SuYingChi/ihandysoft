package com.ihs.keyboardutils.panelcontainer;

import android.animation.Animator;
import android.content.Context;
import android.view.View;

import com.ihs.app.framework.HSApplication;

/**
 * Created by Arthur on 16/10/24.
 */

public abstract class BasePanel {
    public interface OnPanelActionListener {
        void setBarVisibility(int visibility);

        void setBarPosition(int position);

        void showChildPanel(Class panelClass);

        void backToParentPanel(boolean keepSelf);

        View getKeyboardView();

        View getBarView();

        void showPanel(Class panelClass);
    }

    protected Context context = HSApplication.getContext();
    protected View rootView = null;


    protected OnPanelActionListener panelActionListener;

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
        panelActionListener.showChildPanel(panelClass);
    }

    public void setBarVisibility(int visibility) {
        panelActionListener.setBarVisibility(visibility);
    }

    public View getKeyboardView() {
        return panelActionListener.getKeyboardView();
    }

    public Animator getAppearAnimator() {
        return null;
    }

    public Animator getDismissAnimator() {
        return null;
    }

    public void setPanelActionListener(OnPanelActionListener onPanelActionListener) {
        this.panelActionListener = onPanelActionListener;
    }

    public OnPanelActionListener getPanelActionListener() {
        return panelActionListener;
    }

    public void backToParentPanel(boolean keepSelf) {
        panelActionListener.backToParentPanel(keepSelf);
    }
}
