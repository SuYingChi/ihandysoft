package com.ihs.keyboardutils.panelcontainer;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
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

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


/**
 * Created by Arthur on 16/10/21.
 */

public class KeyboardPanelSwitchContainer extends RelativeLayout implements BasePanel.OnPanelActionListener, BarViewGroup.OnBarViewVisibilityChanged {


    public interface OnPanelChangedListener {
        void onPanelChanged(Class panelClass);
    }

    public static final int BAR_TOP = RelativeLayout.ABOVE;
    public static final int BAR_BOTTOM = RelativeLayout.BELOW;

    public static final int MODE_NORMAL = 0;
    public static final int MODE_SHOW_CHILD = 1;
    public static final int MODE_BACK_PARENT = 2;
    public static final int MODE_NORM_KEEP_SELF = 3;

    private int barPosition = BAR_TOP;

    private OnPanelChangedListener onPanelChangedListener;

    private FrameLayout barViewGroup = null;
    private FrameLayout panelViewGroup = null;
    private BasePanel keyboardPanel;
    private BasePanel currentPanel = null;
    private Map<Class, BasePanel> panelMap = new HashMap<>();
    private LinkedList<Class> parentChildStack = new LinkedList<>();

    private Bitmap backgroundBitmap;
    private Rect backgroundRect;

    private int heightMode = MATCH_PARENT;

    private View barView;

//    private Rect gRect = new Rect(), lRect = new Rect(), cRect = new Rect();

    public KeyboardPanelSwitchContainer() {
        super(HSApplication.getContext());
        barViewGroup = new BarViewGroup(getContext(), this);
        barViewGroup.setId(R.id.container_bar_id);

        panelViewGroup = new FrameLayout(getContext());
        panelViewGroup.setId(R.id.container_panel_id);

        adjustViewPosition();
//        setWillNotDraw(false);
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
     * 该方法并没有判断键盘是否已经设置过，所以可以重复设置view，为了方便键盘reload
     *
     * @param panelClass   键盘class
     * @param keyboardView 键盘view
     */
    public void setKeyboardPanel(Class panelClass, View keyboardView) {
        if (!BasePanel.class.isAssignableFrom(panelClass)) {
            HSLog.e("panelCOntainer", "wrong type");
            return;
        }

        BasePanel panel;
        try {
            panel = (BasePanel) panelClass.getConstructor().newInstance();
            panel.setPanelActionListener(this);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        panel.setPanelView(keyboardView);
        panelMap.put(panelClass, panel);
        keyboardPanel = panel;
    }

    /**
     * show target panel and release current panel
     * -keyboard panel always get kept-
     *
     * @param panelClass
     */
    public void showPanel(Class panelClass) {
        addNewPanel(panelClass, false, MODE_NORMAL);
    }

    /**
     * show target panel and keep current panel
     * -keyboard panel always get kept-
     *
     * @param panelClass
     */
    public void showPanelAndKeepSelf(Class panelClass) {
        addNewPanel(panelClass, true, MODE_NORM_KEEP_SELF);
    }


    private void addNewPanel(Class panelClass, boolean keepCurrent, int showingType) {
        if (!BasePanel.class.isAssignableFrom(panelClass)) {
            HSLog.e("panelCOntainer", "wrong type");
            return;
        }

        if (currentPanel != null) {
            if (panelClass == currentPanel.getClass()) {
//                if (currentPanel.getPanelView().getParent() == null) {
//                    panelViewGroup.addView(currentPanel.getPanelView());
//                    currentPanel.onShowPanelView(showingType);
//                }
                HSLog.e("panel", "panel Showed");
                return;
            } else {
                dismissCurrentPanel(keepCurrent, showingType, panelClass);
            }
        } else {
            addPanelViewToRoot(panelClass, showingType);
        }

    }

    private void addPanelViewToRoot(Class panelClass, int showingType) {
        BasePanel panel;
        if (panelMap.get(panelClass) != null) {
            panel = panelMap.get(panelClass);
        } else {
            try {
                panel = (BasePanel) panelClass.getConstructor().newInstance();
                panel.setPanelActionListener(this);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        currentPanel = panel;

        //todo 这里可以分开，如果要保留状态就不要重新create
        View view = panel.getPanelView();
        if (view == null) {
            view = panel.onCreatePanelView();
            panel.setPanelView(view);
        }

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if (view.getParent() != null && view.getParent() != panelViewGroup) {
            removeViewFromParent(view);
        }

        panelViewGroup.addView(view, layoutParams);
        boolean withAnimation = panel.onShowPanelView(showingType);

        if (onPanelChangedListener != null) {
            onPanelChangedListener.onPanelChanged(panelClass);
        }

        Animator appearAnimtor = panel.getAppearAnimator();
        if (appearAnimtor != null && withAnimation) {
            appearAnimtor.start();
        }
    }

    public void setBarView(View view) {
        if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        barView = view;
        barViewGroup.addView(view, layoutParams);
    }

    private void adjustViewPosition() {
        LayoutParams barParams = (LayoutParams) barViewGroup.getLayoutParams();
        LayoutParams panelParams = (LayoutParams) panelViewGroup.getLayoutParams();
        boolean needAddView = false;

        if (barParams == null || panelParams == null) {
            needAddView = true;
            barParams = new LayoutParams(MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            panelParams = new LayoutParams(MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        barParams.addRule(ABOVE, 0);
        barParams.addRule(BELOW, 0);
        barParams.addRule(ALIGN_PARENT_BOTTOM, 0);

        panelParams.addRule(BELOW, 0);
        panelParams.addRule(ABOVE, 0);
        panelParams.addRule(ALIGN_PARENT_BOTTOM, 0);

        switch (barPosition) {
            case BAR_TOP:
                if (heightMode == MATCH_PARENT) {
                    barParams.addRule(ABOVE, panelViewGroup.getId());
                    panelParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);
                } else {
                    panelParams.addRule(BELOW, barViewGroup.getId());
                }
                break;
            case BAR_BOTTOM:
                if (heightMode == MATCH_PARENT) {
                    if (barViewGroup.getVisibility() == GONE) {
                        panelParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);
                    } else {
                        panelParams.addRule(ABOVE, barViewGroup.getId());
                        barParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);
                    }
                } else {
                    barParams.addRule(BELOW, panelViewGroup.getId());
                }
                break;
        }

        if (needAddView) {
            addView(barViewGroup, barParams);
            addView(panelViewGroup, panelParams);
        } else {
            requestLayout();
        }
    }


    private void dismissCurrentPanel(final boolean keepCurrent, final int showingType, @Nullable final Class panelClassToShow) {
        boolean withAnimation;
        if (currentPanel != null) {
            final BasePanel panelToRemove = currentPanel;
            withAnimation = panelToRemove.onHidePanelView(showingType);
            //animator removed
            Animator dismissAnimtor = panelToRemove.getDismissAnimator();
            if (dismissAnimtor != null && withAnimation) {
                dismissAnimtor.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        removeCurrentPanel(keepCurrent, showingType, panelClassToShow, panelToRemove);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                dismissAnimtor.start();
            } else {
                removeCurrentPanel(keepCurrent, showingType, panelClassToShow, panelToRemove);
            }
        }
    }

    private void removeCurrentPanel(boolean keepCurrent, int showingType, @Nullable Class panelClassToShow, BasePanel panelToRemove) {
        switch (showingType) {
            case MODE_SHOW_CHILD:
                parentChildStack.add(currentPanel.getClass());
                BasePanel panel = currentPanel;
                panelMap.put(panel.getClass(), panel);
                break;
            case MODE_BACK_PARENT:

                panelViewGroup.removeView(panelToRemove.getPanelView());
                panelToRemove.onDestroy();
                parentChildStack.remove(panelToRemove.getClass());

                break;
            case MODE_NORMAL:
//                for (Class panelClass : parentChildStack) {
//                    BasePanel keptPanel = panelMap.get(panelClass);
//                    panelViewGroup.removeView(keptPanel.getPanelView());
//                    withAnimation = keptPanel.onHidePanelView(showingType);
//                }
                panelViewGroup.removeAllViews();
                parentChildStack.clear();

                break;
            case MODE_NORM_KEEP_SELF:
                panelViewGroup.removeView(panelToRemove.getPanelView());
                parentChildStack.clear();
                break;
        }

        if (!keepCurrent && !parentChildStack.contains(panelToRemove.getClass())
                && (keyboardPanel == null || keyboardPanel.getClass() != panelToRemove.getClass())
                ) {

            panelToRemove.onDestroy();
            panelMap.remove(panelToRemove.getClass());

            if (currentPanel != null && currentPanel.getClass() == panelToRemove
                    .getClass()) {
                currentPanel = null;
            }
            System.gc();
            HSLog.e("cause GC");
        } else {
            panelMap.put(panelToRemove.getClass(), panelToRemove);
        }

        if (panelClassToShow != null) {
            addPanelViewToRoot(panelClassToShow, showingType);
        } else {
            if (parentChildStack.size() > 0) {
                currentPanel = panelMap.get(parentChildStack.getLast());
                currentPanel.onShowPanelView(showingType);
            }
        }
    }

    public ViewGroup getPanelViewGroup() {
        return panelViewGroup;
    }

    public ViewGroup getBarViewGroup() {
        return barViewGroup;
    }

    public Map getKeptPanelMap() {
        return panelMap;
    }

    public BasePanel getCurrentPanel() {
        return currentPanel;
    }

    private void removeViewFromParent(View view) {
        if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    @Override
    public void setBarPosition(int position) {
        barPosition = position;
        adjustViewPosition();
    }

    @Override
    public void showChildPanel(Class panelClass) {
        addNewPanel(panelClass, true, MODE_SHOW_CHILD);
    }

    @Override
    public void backToParentPanel(boolean keepSelf) {
        dismissCurrentPanel(keepSelf, MODE_BACK_PARENT, null);
    }

    public void setOnPanelChangedListener(OnPanelChangedListener onPanelChangedListener) {
        if (onPanelChangedListener != null) {
            this.onPanelChangedListener = onPanelChangedListener;
        } else {
            throw new IllegalArgumentException("OnPanelChangedListener can not be null");
        }
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

    @Override
    public View getBarView() {
        return barView;
    }

    @Override
    public void setBarVisibility(int visibility) {
        barViewGroup.setVisibility(visibility);
    }

    public void setThemeBackground(Drawable drawable) {
        // 取 drawable 的长宽
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

        // 取 drawable 的颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        // 建立对应 bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        // 建立对应 bitmap 的画布
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        // 把 drawable 内容画到画布中
        drawable.draw(canvas);
        this.backgroundBitmap = bitmap;
        this.backgroundRect = new Rect();
        invalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (backgroundBitmap != null) {
//            getLocalVisibleRect(lRect);
//            backgroundRect.set(lRect);
            if (barViewGroup.getVisibility() == VISIBLE && barPosition == BAR_TOP) {
                backgroundRect.set(0, (int) barViewGroup.getY(), getWidth(), getHeight());
            } else {
                backgroundRect.set(0, (int) panelViewGroup.getY(), getWidth(), getHeight());
            }
//            getGlobalVisibleRect(gRect);
//            getChildVisibleRect(barViewGroup,cRect,null);
            canvas.drawBitmap(backgroundBitmap, null, backgroundRect, null);
        }
        super.dispatchDraw(canvas);
    }

    public void onDestroy() {
        if (currentPanel != null) {
            currentPanel.onDestroy();
            currentPanel = null;
        }
        for (BasePanel panel : panelMap.values()) {
            panel.onDestroy();
        }
        panelMap.clear();
        if (backgroundBitmap != null) {
            backgroundBitmap.recycle();
            backgroundBitmap = null;
        }
    }

    //当wrap content的时候直接设置高度
    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        if (heightMode != params.height) {
            if (params.height == WRAP_CONTENT) {
                heightMode = WRAP_CONTENT;
            } else if (params.height == MATCH_PARENT) {
                heightMode = MATCH_PARENT;
            }
            adjustViewPosition();
        }
        super.setLayoutParams(params);
    }

//    @Override
//    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        if (heightMode == ViewGroup.LayoutParams.WRAP_CONTENT) {
//            t = b - (barViewGroup.getMeasuredHeight() + panelViewGroup.getMeasuredHeight());
//            getLayoutParams().height = (barViewGroup.getVisibility() == VISIBLE ? barViewGroup.getMeasuredHeight() : 0) + panelViewGroup.getMeasuredHeight();
//            setMeasuredDimension(getMeasuredWidth(), getLayoutParams().height);
//        }
//        super.onLayout(changed, l, t, r, b);
//    }


    @Override
    public void onBarVisibilityChanged(int visibility) {
        if (barPosition == BAR_BOTTOM && heightMode == MATCH_PARENT) {
            adjustViewPosition();
        }
    }

}
