package com.ihs.keyboardutils.panelcontainer;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arthur on 16/10/21.
 */

public class KeyboardPanelSwitchContainer extends RelativeLayout implements BasePanel.OnStateChangedListener {

    public static final int BAR_TOP = RelativeLayout.ABOVE;
    public static final int BAR_BOTTOM = RelativeLayout.BELOW;

    public interface OnPanelChangedListener {

        void onPanelChanged(Class panelClass);
    }

    private int barPosition = BAR_TOP;
    private OnPanelChangedListener onPanelChangedListener;
    private FrameLayout barView = null;
    private FrameLayout panelView = null;
    private PanelBean currentPanel = null;
    private Map<Class, PanelBean> panelMap = new HashMap<>();
    private ArrayList panelStack = new ArrayList();

    public KeyboardPanelSwitchContainer() {
        super(HSApplication.getContext());
        barView = new FrameLayout(getContext());
        barView.setId(R.id.container_bar_id);

        panelView = new FrameLayout(getContext());
        panelView.setId(R.id.container_panel_id);

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


    public void showPanel(Class panelClass) {
        showPanel(panelClass, true);
    }

    public void showPanel(Class panelClass, boolean autoRelease) {

        addNewPanel(panelClass, autoRelease, false);
    }


    public void showPanelAndKeepSelf(Class panelClass) {
        showPanelAndKeepSelf(panelClass, true);
    }

    public void showPanelAndKeepSelf(Class panelClass, boolean autoRelease) {
        addNewPanel(panelClass, autoRelease, true);
    }

    private void addNewPanel(Class panelClass, boolean autoRelease, boolean keepCurrent) {
        if (!BasePanel.class.isAssignableFrom(panelClass)) {
            Log.e("panelCOntainer", "wrong type");
            return;
        }

        if (currentPanel != null) {
            if (panelClass == currentPanel.getPanel().getClass()) {
                Log.e("panel", "panel Showed");
                return;
            } else {
                if (!keepCurrent && currentPanel.isAutoRelease()) {
                    panelView.removeView(currentPanel.getPanel().getPanelView());
                    panelMap.remove(currentPanel.getPanel().getClass());
                    currentPanel = null;
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
                panel = (BasePanel) panelClass.getConstructor(BasePanel.OnStateChangedListener.class).newInstance(this);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }


        PanelBean panelBean = new PanelBean(panel, autoRelease);
        panelMap.put(panelClass, panelBean);
        currentPanel = panelBean;

        View view = panel.onCreatePanelView();

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        panelView.addView(view, layoutParams);

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
        barView.addView(view, layoutParams);
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
        LayoutParams barParams = (LayoutParams) barView.getLayoutParams();
        LayoutParams panelParams = (LayoutParams) panelView.getLayoutParams();
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
                panelParams.addRule(BELOW, barView.getId());
                panelParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);
                break;
            case BAR_BOTTOM:
                barParams.addRule(ALIGN_PARENT_TOP, 0);
                panelParams.addRule(BELOW, 0);
                panelParams.addRule(ALIGN_PARENT_BOTTOM, 0);

                panelParams.addRule(ALIGN_PARENT_TOP, TRUE);
                panelParams.addRule(ABOVE, barView.getId());
                barParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);
                break;
        }

        if (needAddView) {
            addView(barView, barParams);
            addView(panelView, panelParams);
        } else {
            requestLayout();
        }
    }

    @Override
    public void setBarVisibility(boolean hide, boolean expandPanel) {
        if (hide) {
            if (expandPanel) {
                barView.setVisibility(GONE);
            } else {
                barView.setVisibility(INVISIBLE);
            }
        } else {
            barView.setVisibility(VISIBLE);
        }
    }

    @Override
    public void setBarPosition(int position) {
        barPosition = position;
        adjustViewPosition();
    }

    @Override
    public void showChildPanel(Class panelClass) {
        showPanelAndKeepSelf(panelClass);
    }


    protected int dp2px(float dp) {
        final float scale = HSApplication.getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public void setOnPanelChangedListener(OnPanelChangedListener onPanelChangedListener) {
        if (onPanelChangedListener != null) {
            this.onPanelChangedListener = onPanelChangedListener;
        } else {
            throw new IllegalArgumentException("OnPanelChangedListener can not be null");
        }
    }
}
