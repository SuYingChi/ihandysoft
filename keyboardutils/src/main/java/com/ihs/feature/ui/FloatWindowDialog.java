package com.ihs.feature.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.WindowManager;
import android.widget.FrameLayout;

public abstract class FloatWindowDialog extends FrameLayout implements FloatWindowListener {

    public FloatWindowDialog(Context context) {
        super(context);
    }

    public FloatWindowDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatWindowDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * @return {@code true} to indicate that this dialog has no need to show and the control flow should continue as if
     * the dialog is never requested to show.
     */
    public boolean hasNoNeedToShow() {
        return false;
    }

    protected abstract FloatWindowManager.Type getType();

    public abstract void dismiss();

    public abstract WindowManager.LayoutParams getLayoutParams();

    public abstract boolean shouldDismissOnLauncherStop();
}
