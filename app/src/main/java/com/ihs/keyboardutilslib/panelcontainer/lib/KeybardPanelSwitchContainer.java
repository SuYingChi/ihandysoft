package com.ihs.keyboardutilslib.panelcontainer.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutilslib.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arthur on 16/10/21.
 */

public class KeybardPanelSwitchContainer extends RelativeLayout implements IPanelSwitcher {

    public static final int TABBAR_TOP = RelativeLayout.BELOW;
    public static final int TABBAR_BOTTOM = RelativeLayout.ABOVE;

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
        if (tabBar.getId() == View.NO_ID) {
            tabBar.setId(R.id.tab_bar_id);
        }
        this.barPosition = barPosition;

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        switch (barPosition) {
            case TABBAR_TOP:
                layoutParams.addRule(ALIGN_PARENT_TOP, TRUE);
                break;
            case TABBAR_BOTTOM:
                layoutParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);
                break;
        }

        addView(tabBar, layoutParams);

        this.tabBar = tabBar;
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
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(barPosition, tabBar.getId());
        addView(view, layoutParams);
    }

    public void setTabBarPosition(int position) {
        if (tabBar == null) {
            HSLog.e("tabBar didnt set yet");
            return;
        }
        barPosition = position;
        LayoutParams layoutParams;
        int tabBarAlign = 0;
        switch (barPosition) {
            case TABBAR_TOP:
                tabBarAlign = ALIGN_PARENT_TOP;
                break;
            case TABBAR_BOTTOM:
                tabBarAlign = ALIGN_PARENT_BOTTOM;
                break;
        }
        if (tabBar.getParent() != null) {
            layoutParams = (LayoutParams) tabBar.getLayoutParams();
            layoutParams.addRule(ALIGN_PARENT_BOTTOM, 0);
            layoutParams.addRule(ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(tabBarAlign, TRUE);
            tabBar.setLayoutParams(layoutParams);
            if (currentPanel != null && currentPanel.getPanel().getPanelView() != null) {
                LayoutParams panelParams = (LayoutParams) currentPanel.getPanel().getPanelView().getLayoutParams();
                panelParams.addRule(BELOW, 0);
                panelParams.addRule(ABOVE, 0);
                panelParams.addRule(barPosition, tabBar.getId());
                currentPanel.getPanel().getPanelView().setLayoutParams(panelParams);
            }
            invalidate();
        }
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
            HSLog.e("child has parent");
            return;
        }

        ViewGroup panelView = (ViewGroup) currentPanel.getPanel().getPanelView();
        if (currentPanel != null && panelView != null) {
            panelView.addView(container, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }


}
