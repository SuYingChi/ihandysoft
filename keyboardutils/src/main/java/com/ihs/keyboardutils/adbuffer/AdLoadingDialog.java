package com.ihs.keyboardutils.adbuffer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.view.Window;
import android.view.WindowManager;

import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.keyboardutils.R;

/**
 * Created by Arthur
 * <p>
 * <p>
 */

class AdLoadingDialog extends Dialog {

    private Context context;

    public AdLoadingDialog(@NonNull Context context) {
        super(context, R.style.Theme_AppCompat_Light_Dialog);
        init(context);
    }

    private void init(Context context) {
        setCanceledOnTouchOutside(false);
        this.context = context;
    }

    public AdLoadingDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        init(context);
    }

    public AdLoadingDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    @Override
    public void show() {
        try {
            Window window = getWindow();
            if (!(context instanceof Activity) && window != null) {
                window.setLayout((int) (DisplayUtils.getDisplay().getWidth() * 0.96), window.getAttributes().height);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(HSApplication.getContext())) {
                    window.setType(WindowManager.LayoutParams.TYPE_TOAST);
                } else {
                    window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
                }
            }
            super.show();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
