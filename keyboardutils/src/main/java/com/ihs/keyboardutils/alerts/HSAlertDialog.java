package com.ihs.keyboardutils.alerts;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListAdapter;

import com.ihs.app.framework.HSApplication;
import com.ihs.keyboardutils.R;

/**
 * Created by ihandysoft on 16/9/8.
 * <p>
 * <p>
 * This Class provide functions to create an AlertDialog that follows the Material Design Spec,
 * the look and feel is defined as per AppCompatDialogSytle
 */


public class HSAlertDialog {

    private AlertDialog.Builder builder;
    private Context context;

    private HSAlertDialog(Context context, int style) {
        if (style == 0) {
            builder = new AlertDialog.Builder(context, R.style.AppCompactDialogStyle);
        } else {
            builder = new AlertDialog.Builder(context, style);
        }
        this.context = context;
    }

    public static HSAlertDialog build() {
        return new HSAlertDialog(HSApplication.getContext(), 0);
    }

    public static HSAlertDialog build(int style) {
        return new HSAlertDialog(HSApplication.getContext(), style);
    }

    public static HSAlertDialog build(@NonNull Activity activity) {
        return new HSAlertDialog(activity, 0);
    }

    public static HSAlertDialog build(@NonNull Activity activity, int style) {
        return new HSAlertDialog(activity, style);
    }

    public static HSAlertDialog build(@NonNull Context activity, int style) {
        return new HSAlertDialog(activity, style);
    }

    public HSAlertDialog setTitle(String title) {
        builder.setTitle(title);
        return this;
    }

    public HSAlertDialog setCustomTitle(View title) {
        builder.setCustomTitle(title);
        return this;
    }

    public HSAlertDialog setMessage(String message) {
        builder.setMessage(message);
        return this;
    }

    public HSAlertDialog setNegativeButton(String buttonText, DialogInterface.OnClickListener onClickListener) {
        builder.setNegativeButton(buttonText, onClickListener);
        return this;
    }

    public HSAlertDialog setPositiveButton(String buttonText, DialogInterface.OnClickListener onClickListener) {
        builder.setPositiveButton(buttonText, onClickListener);
        return this;
    }

    public HSAlertDialog setNeutralButton(String buttonText, DialogInterface.OnClickListener onClickListener) {
        builder.setNeutralButton(buttonText, onClickListener);
        return this;
    }

    public HSAlertDialog setIcon(Drawable drawable) {
        builder.setIcon(drawable);
        return this;
    }

    public HSAlertDialog setSingleChoiceItems(CharSequence[] items, int checkedItem,
                                              final DialogInterface.OnClickListener listener) {
        builder.setSingleChoiceItems(items, checkedItem, listener);
        return this;
    }

    public HSAlertDialog setSingleChoiceItems(ListAdapter adapter, int checkedItem,
                                              final DialogInterface.OnClickListener listener) {
        builder.setSingleChoiceItems(adapter, checkedItem, listener);
        return this;
    }

    public HSAlertDialog setView(View view) {
        builder.setView(view);
        return this;
    }

    public HSAlertDialog setView(int layout) {
        builder.setView(layout);
        return this;
    }

    public HSAlertDialog setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
        builder.setOnCancelListener(onCancelListener);
        return this;
    }

    public HSAlertDialog setCancelable(boolean cancelable) {
        builder.setCancelable(cancelable);
        return this;
    }

    public AlertDialog create() {
        AlertDialog dialog = builder.create();
        boolean showInActivity = context instanceof Activity;

        if (!showInActivity) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(HSApplication.getContext())) {
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
            } else {
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }
        }
        return dialog;
    }

}
