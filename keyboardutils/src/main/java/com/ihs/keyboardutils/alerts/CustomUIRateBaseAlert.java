package com.ihs.keyboardutils.alerts;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.ihs.keyboardutils.R;

/**
 * Created by yang.liu on 2017/10/11.
 */

public class CustomUIRateBaseAlert extends AlertDialog implements View.OnClickListener{

    protected View.OnClickListener dismissListener;

    public CustomUIRateBaseAlert(@NonNull Context context) {
        super(context, R.style.DesignDialog);
    }

    public void show() {
        super.show();
    }

    public void setDismissListener(View.OnClickListener listener) {
        this.dismissListener = listener;
    }

    @Override
    public void onClick(View v) {}
}
