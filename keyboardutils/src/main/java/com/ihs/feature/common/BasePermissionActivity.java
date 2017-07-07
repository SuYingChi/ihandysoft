package com.ihs.feature.common;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.utils.CommonUtils;

public abstract class BasePermissionActivity extends BaseCenterActivity {

    private static final String TAG = "BasePermissionActivity";
    private BroadcastReceiver mCloseSystemDialogsReceiver;
    protected boolean mIsHomeKeyClicked;
    private Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (registerCloseSystemDialogsReceiver()) {
            mCloseSystemDialogsReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                        String reason = intent.getStringExtra("reason");
                        HSLog.d(TAG, "CloseSystemDialogsReceiver onReceive reason = " + reason);
                        if ("homekey".equals(reason) || "recentapps".equals(reason) || "voiceinteraction".equals(reason)
                                || "lock".equals(reason) || "assist".equals(reason)) {
//                            LauncherFloatWindowManager.getInstance().removeFloatButton();
                        }
                        mIsHomeKeyClicked = "homekey".equals(reason);
                    }
                }
            };
            registerReceiver(mCloseSystemDialogsReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        LauncherFloatWindowManager.getInstance().removeFloatButton();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registerCloseSystemDialogsReceiver() && null != mCloseSystemDialogsReceiver) {
            CommonUtils.unregisterReceiver(this, mCloseSystemDialogsReceiver);
            mCloseSystemDialogsReceiver = null;
        }
    }

    public boolean showDialog(Dialog dialog) {
        if (isFinishing()) {
            return false;
        }

        dismissDialog();

        mDialog = dialog;
        dialog.show();

        return true;
    }

    @Override
    public void dismissDialog() {
        super.dismissDialog();
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    protected boolean registerCloseSystemDialogsReceiver() {
        return false;
    }
}
