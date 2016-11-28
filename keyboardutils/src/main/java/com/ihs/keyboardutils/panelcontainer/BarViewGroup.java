package com.ihs.keyboardutils.panelcontainer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by Arthur on 16/11/28.
 */

public class BarViewGroup extends FrameLayout {
    private OnBarViewVisibilityChanged onBarViewVisibilityChanged;

    interface OnBarViewVisibilityChanged {
        void onBarVisibilityChanged(int visibility);
    }

    public BarViewGroup(Context context, OnBarViewVisibilityChanged onBarViewVisibilityChanged) {
        super(context);
        this.onBarViewVisibilityChanged = onBarViewVisibilityChanged;
    }

    public BarViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BarViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        onBarViewVisibilityChanged.onBarVisibilityChanged(getVisibility());
    }
}
