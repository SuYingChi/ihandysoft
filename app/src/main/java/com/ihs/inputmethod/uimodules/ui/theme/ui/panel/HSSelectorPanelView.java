package com.ihs.inputmethod.uimodules.ui.theme.ui.panel;

import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.utils.HSResourceUtils;

/**
 * Created by jixiang on 18/3/8.
 */

public class HSSelectorPanelView extends PercentRelativeLayout {
    public HSSelectorPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setLayoutParams(new FrameLayout.LayoutParams(HSApplication.getContext().getResources().getDisplayMetrics().widthPixels, HSResourceUtils.getDefaultKeyboardHeight(getResources())));
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
