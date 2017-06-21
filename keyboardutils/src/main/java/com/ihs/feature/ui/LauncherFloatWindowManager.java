package com.ihs.feature.ui;

import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.feature.boost.BoostSource;
import com.ihs.feature.boost.BoostTip;
import com.ihs.feature.boost.BoostType;
import com.ihs.feature.common.ConcurrentUtils;
import com.ihs.feature.tip.LauncherTipManager;
import com.ihs.feature.common.Thunk;
import com.ihs.keyboardutils.utils.CommonUtils;

public class LauncherFloatWindowManager extends FloatWindowManager {

    private static final long RESOLVER_WIZARD_AUTO_DISMISS_TIME = 2000;

    private BoostTip mBoostTip;
    private LayoutParams mBoostTipWindowParams;

    public enum PermissionGuideType {
        ICON_BADGE,
        ACCESSIBILITY,
        SET_AS_DEFAULT,
        SET_AS_DEFAULT_HUAWEI_KITKAT,
    }

    private boolean mEnabled = true;

    public static LauncherFloatWindowManager getInstance() {
        return (LauncherFloatWindowManager) FloatWindowManager.getInstance();
    }

    // Do NOT invoke this directly, use getInstance()
    @Thunk
    public LauncherFloatWindowManager() {
        final Runnable registerPhoneStateListener = new Runnable() {
            @Override
            public void run() {
                TelephonyManager tm = (TelephonyManager) HSApplication.getContext().getSystemService(Service.TELEPHONY_SERVICE);

                // PhoneStateListener must be instantiated on looper thread as it internally instantiates a Handler
                tm.listen(new PhoneStateListener() {
                    @Override
                    public void onCallStateChanged(int state, String incomingNumber) {
                        if (state == TelephonyManager.CALL_STATE_RINGING) {
                            dismissAnyModalTip();
                        }
                    }
                }, PhoneStateListener.LISTEN_CALL_STATE);
            }
        };
        if (Looper.myLooper() == Looper.getMainLooper()) {
            registerPhoneStateListener.run();
        } else {
            ConcurrentUtils.postOnMainThread(new Runnable() {
                @Override
                public void run() {
                    registerPhoneStateListener.run();
                }
            });
        }
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    //region Dialogs

    @Override
    public void showDialog(Context context, Type type, Object... extra) {
        if (!mEnabled) {
            HSLog.d("FloatWindowManager", "Disable at now");
            return;
        }
        super.showDialog(context, type, extra);
    }


    //endregion

    //region Permission Guides

    public void showBoostTip(Context context, BoostType type, int boostedPercentage, BoostSource source) {
        if (!mEnabled) {
            HSLog.d("FloatWindowManager", "Disable at now");
            return;
        }
        try {
            if (mBoostTip == null) {
                mBoostTip = new BoostTip(context, type, boostedPercentage, source);
                if (mBoostTipWindowParams == null) {
                    mBoostTipWindowParams = getDefaultLayoutParams();
                    mBoostTipWindowParams.y = CommonUtils.pxFromDp(80);
                    mBoostTipWindowParams.gravity = Gravity.TOP;

                    mBoostTipWindowParams.height = LayoutParams.WRAP_CONTENT;
                    mBoostTipWindowParams.flags |= LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | LayoutParams.FLAG_NOT_TOUCH_MODAL;
                }
                mBoostTip.setLayoutParams(mBoostTipWindowParams);
                getWindowManager().addView(mBoostTip, mBoostTipWindowParams);
                mBoostTip.onAddedToWindow(getWindowManager());
            }
        } catch (Exception e) {
            e.printStackTrace();
            HSLog.e("Error creating auto category tip: " + e.getMessage());
        }
    }

    public void removeBoostTip() {
        if (mBoostTip != null) {
            try {
                getWindowManager().removeView(mBoostTip);
            } catch (Exception ignored) {
            }
        }
        mBoostTip = null;
    }

    public boolean isShowingModalTip() {
        return super.isShowingModalTip()
                || mBoostTip != null;
    }


    public void dismissAnyModalTip() {
        super.dismissAnyModalTip();
        if (mBoostTip != null) {
            mBoostTip.dismiss(false);
        }
    }

    @Override
    public void onLauncherStop() {
        super.onLauncherStop();
        if (mBoostTip != null) {
            mBoostTip.dismiss(false);
        }
    }

    private LayoutParams getDefaultLayoutParams() {
        return getDefaultLayoutParams(true);
    }

    private LayoutParams getDefaultLayoutParams(boolean notFocusable) {
        LayoutParams lp = new LayoutParams();
        lp.width = LayoutParams.MATCH_PARENT;
        lp.height = LayoutParams.WRAP_CONTENT;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.type = LayoutParams.TYPE_PHONE;
        if (notFocusable) {
            lp.flags |= LayoutParams.FLAG_NOT_FOCUSABLE;
        }
        return lp;
    }

    @Override
    protected SafeWindowManager getWindowManager() {
        if (sWindowManager == null) {
            sWindowManager = new LauncherSafeWindowManager();
        }
        return sWindowManager;
    }

    private static class LauncherSafeWindowManager extends SafeWindowManager {
        @Override
        public void addView(View view, WindowManager.LayoutParams params) {
            super.addView(view, params);
        }

        @Override
        public void removeView(View view) {
            super.removeView(view);
            LauncherTipManager.getInstance().notifyDismiss();
        }
    }
}
