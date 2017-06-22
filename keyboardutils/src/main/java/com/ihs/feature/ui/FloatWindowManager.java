package com.ihs.feature.ui;

import android.content.Context;
import android.os.Build;
import android.view.WindowManager;

import com.ihs.commons.utils.HSLog;

import java.util.HashMap;
import java.util.Map;

public class FloatWindowManager {

    private static final String TAG = FloatWindowManager.class.getSimpleName();

    /**
     * Type for {@link FloatWindowDialog}s.
     */
    public enum Type {
        BOOST_PLUS_AUTHORIZE, // Asks for Accessibility permission
        USAGE_ACCESS_AUTHORIZE, // Asks for Usage Access permission
        DEVICE_ADMIN_AUTHORIZE, // Asks user to set us as Device Admin
        BOOST_PLUS_CLEAN, // Accessibility permission has turned on
        BOOST_PLUS_ACCESSIBILITY, // Accessibility notice dialog
        SET_AS_DEFAULT, // Asks user to set us as default home
        SET_AS_DEFAULT_RETRY, // Asks user to set us as default home again
        SET_AS_DEFAULT_BOOSTED, // Asks user to set us as default home after boosted
        FIVE_STAR_RATE, // Asks user to rate on Google Play
        NOTIFICATION_AUTHORIZE, // Asks for Notification Access permission
        DISCOVER_THEME, // Invites user to customize center for more themes
        APPLY_THEME, // Invites user to apply a newly installed theme
        UPDATE_APK, // Update new version
        UPDATE_INSTALL_APK, // New version app ready, install
        GUIDE_DOWNLOAD_LAUNCHER, // Invites user to download air launcher
        UNPACK_FOLDER, // Asks user to confirm unpack folder
        REMOVE_FOLDER, // Asks user to confirm remove folder
        REMOVE_SEARCH_BAR, // Asks user to confirm remove search bar
        REMOVE_MOMENT, // Asks user to confirm remove air moment
        BOOST_SETTING_AUTO_CLEAN,
        BATTERY_LOW,
        CHARING_SCREEN_GUIDE,
        GENERAL, // This just for common
        JUNK_CLEAN_INSTALL,
        JUNK_CLEAN_UNINSTALL,
        PROMOTION_GUIDE,
    }

    private volatile static FloatWindowManager sInstance;
    public static boolean isRemoveDialogFrozen;


    protected FloatWindowDialog createDialog(Context context, Type type, Object... extra) {
        return null;
    }

    public static FloatWindowManager getInstance() {
        if (sInstance == null) {
            Class<?> klass = null;
            try {
                klass = Class.forName("com.ihs.feature.ui.LauncherFloatWindowManager");
            } catch (ClassNotFoundException ignored) {
            }
            if (klass != null) {
                //launcher
                //noinspection TryWithIdenticalCatches
                try {
                    sInstance = (FloatWindowManager) klass.newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    klass = Class.forName("com.themelab.launcher.dialog.ThemeFloatWindowManager");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                if (klass != null) {
                    //theme
                    //noinspection TryWithIdenticalCatches
                    try {
                        sInstance = (FloatWindowManager) klass.newInstance();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (sInstance == null) {
                throw new RuntimeException("neither LauncherFloatWindowManager nor ThemeFloatWindowManager, file include error!");
            }

        }
        return sInstance;
    }

    protected Map<Type, FloatWindowDialog> mDialogs = new HashMap<>(6);

    protected SafeWindowManager sWindowManager;

    public void showDialog(Context context, Type type, Object... extra) {
        FloatWindowDialog dialog = mDialogs.get(type);
        try {
            if (dialog == null) {
                dialog = createDialog(context, type, extra);
                if (dialog.hasNoNeedToShow()) {
                    HSLog.i(TAG, "Dialog " + type + " has no need to show");
                    return;
                }
                mDialogs.put(type, dialog);
                final WindowManager.LayoutParams windowParams = dialog.getLayoutParams();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    windowParams.type = WindowManager.LayoutParams.TYPE_TOAST;
                }
                getWindowManager().addView(dialog, windowParams);
                dialog.onAddedToWindow(getWindowManager());
            }
        } catch (Exception e) {
            e.printStackTrace();
            HSLog.e("Error creating dialog: " + e.getMessage());
        }
    }

    public FloatWindowDialog getDialog(Type type) {
        return mDialogs.get(type);
    }

    public void removeDialog(Type type) {
        if (isRemoveDialogFrozen && type == FloatWindowManager.Type.BOOST_PLUS_CLEAN) {
            return;
        }
        FloatWindowDialog dialog = mDialogs.get(type);
        if (dialog != null) {
            try {
                getWindowManager().removeView(dialog);
            } catch (Exception ignored) {
            }
        }
        mDialogs.remove(type);
    }

    public boolean isDialogShowing(Type type) {
        FloatWindowDialog dialog = mDialogs.get(type);
        return (dialog != null);
    }

    public boolean isShowingModalTip() {
        return !mDialogs.isEmpty();
    }

    public void dismissAnyModalTip() {
        for (Type type : Type.values()) {
            FloatWindowDialog dialog = mDialogs.get(type);
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    }

    public void onLauncherStop() {
        for (Type type : Type.values()) {
            FloatWindowDialog dialog = mDialogs.get(type);
            if (dialog != null && dialog.shouldDismissOnLauncherStop()) {
                dialog.dismiss();
            }
        }
    }

    protected SafeWindowManager getWindowManager() {
        if (sWindowManager == null) {
            sWindowManager = new SafeWindowManager();
        }
        return sWindowManager;
    }
}
