package com.ihs.keyboardutils.panelcontainer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.R;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by Arthur on 16/10/21.
 */

public class KeyboardPanelSwitchContainer extends RelativeLayout implements BasePanel.OnStateChangedListener {

    public interface OnPanelChangedListener {
        void onPanelChanged(Class panelClass);
    }

    public static final int BAR_TOP = RelativeLayout.ABOVE;
    public static final int BAR_BOTTOM = RelativeLayout.BELOW;

    private int barPosition = BAR_TOP;
    private OnPanelChangedListener onPanelChangedListener;
    private FrameLayout barViewGroup = null;
    private FrameLayout panelViewGroup = null;
    private BasePanel currentPanel = null;
    private Map<Class, BasePanel> panelMap = new HashMap<>();
    private LinkedList<Class> panelStack = new LinkedList<>();

    private BasePanel keyboardPanel;
    private Class keyboardClass;


    public KeyboardPanelSwitchContainer() {
        super(HSApplication.getContext());
        barViewGroup = new FrameLayout(getContext());
        barViewGroup.setId(R.id.container_bar_id);

        panelViewGroup = new FrameLayout(getContext());
        panelViewGroup.setId(R.id.container_panel_id);

        adjustViewPosition();
    }

    private KeyboardPanelSwitchContainer(Context context) {
        super(context);
    }

    private KeyboardPanelSwitchContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private KeyboardPanelSwitchContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * show target panel and release current panel
     * -keyboard panel always get kept-
     *
     * @param panelClass
     */
    public void showPanel(Class panelClass) {
        panelStack.clear();
        addNewPanel(panelClass, false);
    }

    /**
     * show target panel and keep current panel
     * -keyboard panel always get kept-
     *
     * @param panelClass
     */
    public void showPanelAndKeepSelf(Class panelClass) {
        addNewPanel(panelClass, true);
    }

    private void addNewPanel(Class panelClass, boolean keepCurrent) {
        if (!BasePanel.class.isAssignableFrom(panelClass)) {
            HSLog.e("panelCOntainer", "wrong type");
            return;
        }

        if (keyboardClass == null) {
            HSLog.e("keyboard class didnt set yet");
            return;
        }

        if (currentPanel != null) {
            if (panelClass == currentPanel.getClass()) {
                HSLog.e("panel", "panel Showed");
                return;
            } else {
                panelViewGroup.removeView(currentPanel.getPanelView());

                if (!keepCurrent && keyboardClass != panelClass) {
                    panelMap.remove(currentPanel.getClass());
                    currentPanel = null;
                    System.gc();
                    HSLog.e("cause GC");
                } else {
                    panelMap.put(currentPanel.getClass(), currentPanel);
                }
            }
        }

        BasePanel panel;
        if (panelMap.get(panelClass) != null) {
            panel = panelMap.get(panelClass);
        } else {
            try {
                panel = (BasePanel) panelClass.getConstructor(BasePanel.OnStateChangedListener.class).newInstance(this);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }


        currentPanel = panel;
        if (keyboardClass == panelClass) {
            keyboardPanel = panel;
        }

        View view = panel.onCreatePanelView();

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if (view.getParent() != null && view.getParent() != panelViewGroup) {
            removeViewFromParent(view);
        }

        panelViewGroup.addView(view, layoutParams);

        if (onPanelChangedListener != null) {
            onPanelChangedListener.onPanelChanged(panelClass);
        }
    }

    public void setTabBarPosition(int position) {
        barPosition = position;
        adjustViewPosition();
    }

    public void setBarView(View view) {
        if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        barViewGroup.addView(view, layoutParams);
    }

    public void addMoreContainer(View container) {
        if (container.getParent() != null) {
            ((ViewGroup) container.getParent()).removeView(container);
        }
        addView(container, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void removeContainer(View container) {
        if (container == null || container.getParent() == null) {
            return;
        }
        removeView(container);
    }

    private void adjustViewPosition() {
        LayoutParams barParams = (LayoutParams) barViewGroup.getLayoutParams();
        LayoutParams panelParams = (LayoutParams) panelViewGroup.getLayoutParams();
        boolean needAddView = false;

        if (barParams == null || panelParams == null) {
            needAddView = true;
            barParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            panelParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        switch (barPosition) {
            case BAR_TOP:
                panelParams.addRule(ALIGN_PARENT_TOP, 0);
                panelParams.addRule(ABOVE, 0);
                barParams.addRule(ALIGN_PARENT_BOTTOM, 0);

                barParams.addRule(ALIGN_PARENT_TOP, TRUE);
                panelParams.addRule(BELOW, barViewGroup.getId());
                panelParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);
                break;
            case BAR_BOTTOM:
                barParams.addRule(ALIGN_PARENT_TOP, 0);
                panelParams.addRule(BELOW, 0);
                panelParams.addRule(ALIGN_PARENT_BOTTOM, 0);

                panelParams.addRule(ALIGN_PARENT_TOP, TRUE);
                panelParams.addRule(ABOVE, barViewGroup.getId());
                barParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);
                break;
        }

        if (needAddView) {
            addView(barViewGroup, barParams);
            addView(panelViewGroup, panelParams);
        } else {
            requestLayout();
        }
    }

    @Override
    public void setBarVisibility(boolean hide, boolean expandPanel) {
        if (hide) {
            if (expandPanel) {
                barViewGroup.setVisibility(GONE);
            } else {
                barViewGroup.setVisibility(INVISIBLE);
            }
        } else {
            barViewGroup.setVisibility(VISIBLE);
        }
    }

    @Override
    public void setBarPosition(int position) {
        barPosition = position;
        adjustViewPosition();
    }

    @Override
    public void showChildPanel(Class panelClass) {
        panelStack.add(currentPanel.getClass());
        showPanelAndKeepSelf(panelClass);
    }

    @Override
    public void backToParentPanel() {
        try {
            showPanel(panelStack.getLast());
            panelStack.removeLast();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOnPanelChangedListener(OnPanelChangedListener onPanelChangedListener) {
        if (onPanelChangedListener != null) {
            this.onPanelChangedListener = onPanelChangedListener;
        } else {
            throw new IllegalArgumentException("OnPanelChangedListener can not be null");
        }
    }

    public void setKeyboardClass(Class keyboardClass) {
        this.keyboardClass = keyboardClass;
    }

    @Override
    public View getKeyboardView() {
        if (keyboardPanel == null) {
            HSLog.e("KeyboardPanel didnt set iskeyboard or didnt load yet");
            return null;
        }
        View keyboardView = keyboardPanel.getPanelView();
        if (keyboardView != null) {
            removeViewFromParent(keyboardView);
        }
        return keyboardView;
    }

    public ViewGroup getPanelViewGroup() {
        return panelViewGroup;
    }


    public BasePanel getCurrentPanel() {
        return currentPanel;
    }


    private void removeViewFromParent(View view) {
        if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }

}
