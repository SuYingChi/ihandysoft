package com.launcher;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.ihs.app.framework.HSApplication;
import com.ihs.feature.common.CompatUtils;
import com.ihs.keyboardutils.utils.CommonUtils;

/**
 * Compatibility utilities for lock screen float window.
 */
public class FloatWindowCompat {

    public static void initLockScreen(Context context) {
        if (needsSystemErrorFloatWindow()) {
            FloatWindowController.init(context);
            FloatWindowController.getInstance().start();
        } else {

            /*
            https://www.fabric.io/keyboardandroid/android/apps/com.smartkeyboard.emoji/issues/59dd38ecbe077a4dcc137231?time=last-seven-days

            应该是部分手机特有crash
            https://stackoverflow.com/questions/38764497/security-exception-unable-to-start-service-user-0-is-restricted

            Below is some translation from the post: After auto screen off for a while,
            the system will start battery management module, it will forbid any app start up.
            but there is a bug, it should force stop the app instead throw exception.
            From developer side, they give a solution: use "try catch" when starting the service.
             */
            try {
                context.startService(LockScreenService.getIntent());
            } catch (Exception e) {
            }
        }
    }

    static WindowManager.LayoutParams getLockScreenParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        if (needsSystemErrorFloatWindow()) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            layoutParams.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            layoutParams.systemUiVisibility |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            layoutParams.systemUiVisibility |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            int phoneWidth = CommonUtils.getPhoneWidth(HSApplication.getContext());
            int phoneHeight = CommonUtils.getPhoneHeight(HSApplication.getContext());
            layoutParams.width = phoneWidth < phoneHeight ? phoneWidth : phoneHeight;
            layoutParams.height = phoneWidth > phoneHeight ? phoneWidth : phoneHeight;
        }
        layoutParams.format = PixelFormat.TRANSPARENT;
        layoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        layoutParams.gravity = Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            layoutParams.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
            layoutParams.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        }
        return layoutParams;
    }

    public static boolean needsSystemErrorFloatWindow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                || (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP && CompatUtils.IS_SAMSUNG_DEVICE);
    }
}
