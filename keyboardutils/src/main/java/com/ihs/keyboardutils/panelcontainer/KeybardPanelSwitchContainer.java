package com.ihs.keyboardutils.panelcontainer;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Arthur on 16/10/21.
 */

public class KeybardPanelSwitchContainer extends RelativeLayout implements IPanelSwitcher {

    public static final int TABBAR_TOP = RelativeLayout.ABOVE;
    public static final int TABBAR_BOTTOM = RelativeLayout.BELOW;

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    private int barPosition = TABBAR_BOTTOM;
    private View tabBar = null;
    private PanelBean currentPanel = null;
    private Map<Class, PanelBean> panelMap = new HashMap<>();

    private KeybardPanelSwitchContainer(Context context) {
        super(context);
    }

    private KeybardPanelSwitchContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private KeybardPanelSwitchContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public KeybardPanelSwitchContainer(View tabBar, int barPosition) {
        super(HSApplication.getContext());
        if (tabBar.getParent() != null) {
            HSLog.e("tabBar already used in another container");
            return;
        }
        if (tabBar.getId() == View.NO_ID) {
            tabBar.setId(generateViewId());
        }
        this.barPosition = barPosition;
        this.tabBar = tabBar;
        addView(tabBar);
    }

    public void showPanel(Class panelClass) {
        showPanel(panelClass, true);
    }

    public void showPanel(Class panelClass, boolean autoRelease) {
        if (!BasePanel.class.isAssignableFrom(panelClass)) {
            Log.e("panelCOntainer", "wrong type");
            return;
        }

        if (currentPanel != null) {
            if (panelClass == currentPanel.getPanel().getClass()) {
                Log.e("panel", "panel Showed");
                return;
            } else {
                if (currentPanel.isAutoRelease()) {
                    removeView(currentPanel.getPanel().getPanelView());
                    currentPanel = null;
                    panelMap.remove(panelClass);
                    System.gc();
                    HSLog.e("cause GC");
                }
            }
        }

        BasePanel panel;
        if (panelMap.get(panelClass) != null) {
            panel = panelMap.get(panelClass).getPanel();
        } else {
            try {
                panel = (BasePanel) panelClass.getConstructor(IPanelSwitcher.class).newInstance(this);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }


        PanelBean panelBean = new PanelBean(panel, autoRelease);
        panelMap.put(panelClass, panelBean);
        currentPanel = panelBean;

        View view = panel.onCreatePanelView();
        if (view.getId() == View.NO_ID) {
            view.setId(generateViewId());
        }

        LayoutParams panelLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LayoutParams tabBarLayoutParams = (LayoutParams) tabBar.getLayoutParams();

        adjustViewPosition(panelLayoutParams, tabBarLayoutParams);

        addView(view, panelLayoutParams);
    }


    public void setTabBarPosition(int position) {
        if (tabBar == null || currentPanel == null) {
            HSLog.e("tabBar or panel didnt set yet");
            return;
        }
        adjustViewPosition((LayoutParams) currentPanel.getPanel().getPanelView().getLayoutParams(), (LayoutParams) tabBar.getLayoutParams());

//        barPosition = position;
//        LayoutParams layoutParams;
//        int tabBarAlign = 0;
//        switch (barPosition) {
//            case TABBAR_TOP:
//                tabBarAlign = ALIGN_PARENT_TOP;
//                break;
//            case TABBAR_BOTTOM:
//                tabBarAlign = ALIGN_PARENT_BOTTOM;
//                break;
//        }
//        if (tabBar.getParent() != null) {
//            layoutParams = (LayoutParams) tabBar.getLayoutParams();
//            layoutParams.addRule(ALIGN_PARENT_BOTTOM, 0);
//            layoutParams.addRule(ALIGN_PARENT_TOP, 0);
//            layoutParams.addRule(tabBarAlign, TRUE);
//            tabBar.setLayoutParams(layoutParams);
//            if (currentPanel != null && currentPanel.getPanel().getPanelView() != null) {
//                LayoutParams panelParams = (LayoutParams) currentPanel.getPanel().getPanelView().getLayoutParams();
//                panelParams.addRule(BELOW, 0);
//                panelParams.addRule(ABOVE, 0);
//                panelParams.addRule(barPosition, tabBar.getId());
//                currentPanel.getPanel().getPanelView().setLayoutParams(panelParams);
//            }
//            invalidate();
//        }
    }


    @Override
    public void setTabBarVisibility(boolean hide, boolean expandPanel) {
        if (hide) {
            if (expandPanel) {
                tabBar.setVisibility(GONE);
            } else {
                tabBar.setVisibility(INVISIBLE);
            }
        } else {
            tabBar.setVisibility(VISIBLE);
        }
    }

    public void addMoreContainer(View container) {
        if (container.getParent() != null) {
            ((ViewGroup) container.getParent()).removeView(container);
        }

        ViewGroup panelView = (ViewGroup) currentPanel.getPanel().getPanelView();
        if (currentPanel != null && panelView != null) {
            panelView.addView(container, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }


    public void removeContainer(View container) {
        if (container == null || container.getParent() == null) {
            return;
        }

        ViewGroup panelView = (ViewGroup) currentPanel.getPanel().getPanelView();
        if (currentPanel != null && panelView != null) {
            panelView.removeView(container);
        }
    }


    private void adjustViewPosition(LayoutParams panelLayoutParams, LayoutParams tabBarLayoutParams) {
        switch (barPosition) {
            case TABBAR_TOP:
                panelLayoutParams.addRule(ABOVE, 0);
                tabBarLayoutParams.addRule(ALIGN_PARENT_BOTTOM, 0);

                tabBarLayoutParams.addRule(ABOVE, currentPanel.getPanel().getPanelView().getId());
                panelLayoutParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);
                break;
            case TABBAR_BOTTOM:
                tabBarLayoutParams.addRule(ABOVE, 0);
                panelLayoutParams.addRule(ALIGN_PARENT_BOTTOM, 0);

                panelLayoutParams.addRule(ABOVE, tabBar.getId());
                tabBarLayoutParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);
                break;
        }
    }

    public static int generateViewId() {
        for (; ; ) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }
}
