package com.ihs.feature.ui;

import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.ihs.feature.common.LauncherAnimUtils;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.CommonUtils;


public class BasePermissionGuideDialog extends FrameLayout implements FloatWindowListener {

    private static final long SLIDE_IN_ANIM_DURATION = 300;
    public static final long ESTIMATED_ACTIVITY_SWITCH_TIME = 500;
    protected boolean mIsShowImmediately;

    public BasePermissionGuideDialog(Context context) {
        super(context);
        setVisibility(INVISIBLE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        LauncherFloatWindowManager.getInstance().removePermissionGuide(true);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            LauncherFloatWindowManager.getInstance().removePermissionGuide(true);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onAddedToWindow(SafeWindowManager windowManager) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                setVisibility(VISIBLE);
                View content = findViewById(R.id.permission_guide_content);
                if (null != content) {
                    int contentHeight = content.getHeight();
                    content.setTranslationY(contentHeight + CommonUtils.getNavigationBarHeight(getContext()));
                    content.animate().translationY(0f)
                            .setDuration(SLIDE_IN_ANIM_DURATION)
                            .setInterpolator(LauncherAnimUtils.DECELERATE_QUAD)
                            .start();
                }
            }
        }, mIsShowImmediately ? 0 : ESTIMATED_ACTIVITY_SWITCH_TIME);
    }

    protected void setShowContentImmediately(boolean isImmediately) {
        mIsShowImmediately = isImmediately;
    }
}
