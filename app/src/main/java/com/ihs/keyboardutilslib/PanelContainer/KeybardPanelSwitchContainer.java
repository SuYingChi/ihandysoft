package com.ihs.keyboardutilslib.panelcontainer;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arthur on 16/10/21.
 */

public class KeybardPanelSwitchContainer extends LinearLayout implements IPanelSwitcher {

    public static final int TABBAR_TOP = 0;
    public static final int TABBAR_BOTTOM = 1;

    private int barPosition = TABBAR_TOP;
    private View tabBar = null;
    private PanelBean currentPanel = null;
    private Map<Class, PanelBean> panelMap = new HashMap<>();

    public KeybardPanelSwitchContainer(Context context) {
        super(context);
    }

    public KeybardPanelSwitchContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeybardPanelSwitchContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTabBar(View tabBar) {
        if (tabBar == null) {
            return;
        }
        addView(tabBar);
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
                    currentPanel = null;
                    panelMap.remove(panelClass);
                    System.gc();
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

        addView(panel.onCreatePanelView(), new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
    }

    public void setTabBarPosition(int position) {
        barPosition = position;
        removeView(tabBar);
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


}
