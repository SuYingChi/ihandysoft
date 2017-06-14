package com.ihs.keyboardutils.permission;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;


public class PermissionFloatWindow {
    private static PermissionFloatWindow instance;

    private PermissionTip permissionTip;
    private LayoutParams permissionTipWindowParams;

    public synchronized static PermissionFloatWindow getInstance() {
        if (null == instance) {
            instance = new PermissionFloatWindow();
        }
        return instance;
    }


    public WindowManager getWindowManager() {
//        if (!MBPermissionManager.isCanDrawOverlays()) {
//            MBPermissionManager.enableDrawOverlays();
//        }
        return (WindowManager) HSApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
    }

    public void createPermissionTip(int tipType) {
        final WindowManager windowManager = getWindowManager();
        if (windowManager == null) {
            return;
        }
        try {
            if (permissionTip == null) {
                permissionTip = new PermissionTip(HSApplication.getContext());
                permissionTip.setPermissionTipText(tipType);
                if (permissionTipWindowParams == null) {
                    permissionTipWindowParams = new LayoutParams();
                    permissionTipWindowParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
                    permissionTipWindowParams.height = LayoutParams.WRAP_CONTENT;
                    permissionTipWindowParams.width = LayoutParams.WRAP_CONTENT;
                    permissionTipWindowParams.format = PixelFormat.TRANSPARENT;
                    permissionTipWindowParams.windowAnimations = android.R.style.Animation_Toast;
                    permissionTipWindowParams.type = LayoutParams.TYPE_TOAST;
                    if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
                        permissionTipWindowParams.flags |= LayoutParams.FLAG_HARDWARE_ACCELERATED;
                    }
                    permissionTipWindowParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    permissionTipWindowParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE
                            | LayoutParams.FLAG_LAYOUT_NO_LIMITS
                            | LayoutParams.FLAG_KEEP_SCREEN_ON;
                    permissionTipWindowParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
                }
                permissionTip.setLayoutParams(permissionTipWindowParams);
                getWindowManager().addView(permissionTip, permissionTipWindowParams);
            } else {
                permissionTip.setPermissionTipText(tipType);
                getWindowManager().addView(permissionTip, permissionTipWindowParams);
                permissionTip.startHandAnimationAgain();
            }

            HSAnalytics.logEvent("Authorization_Mov_Viewed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removePermissionTip() {
        if (permissionTip != null) {
            try {
                permissionTip.clean();
                getWindowManager().removeView(permissionTip);
            } catch (Exception e) {
            }
        }
        permissionTip = null;
    }


}
