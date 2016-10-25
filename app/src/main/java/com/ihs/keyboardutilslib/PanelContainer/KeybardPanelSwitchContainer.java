package com.ihs.keyboardutilslib.panelcontainer;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

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

    private int barPosition = TABBAR_TOP;
    private View tabBar = null;
    private PanelBean currentPanel = null;
    private Map<Class, PanelBean> panelMap = new HashMap<>();

    public KeybardPanelSwitchContainer(Context context, View tabBar) {
        super(context);
        if (tabBar.getId() == View.NO_ID) {
            tabBar.setId(R.id.tab_bar_id);
        }
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(ALIGN_PARENT_TOP);
        addView(tabBar, layoutParams);

        this.tabBar = tabBar;
//        addView(tabBar);
    }

    private KeybardPanelSwitchContainer(Context context) {
        super(context);
    }

    private KeybardPanelSwitchContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private KeybardPanelSwitchContainer(Context context, AttributeSet attrs, int defStyleAttr) {
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
//        layoutParams.addRule();
        addView(view, layoutParams);
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
