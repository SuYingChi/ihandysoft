package com.ihs.keyboardutils.alerts;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.ihs.keyboardutils.R;

/**
 * Created by yang.liu on 2017/10/11.
 */

public class CustomUIRateTwoAlert extends CustomUIRateBaseAlert {

    public CustomUIRateTwoAlert (@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_ui_rate_two_alert);
    }

    @Override
    public void show() {
        super.show();
    }
}
