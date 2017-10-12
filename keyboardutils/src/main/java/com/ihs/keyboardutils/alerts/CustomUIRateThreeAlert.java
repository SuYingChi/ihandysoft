package com.ihs.keyboardutils.alerts;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.ihs.keyboardutils.R;

/**
 * Created by yang.liu on 2017/10/11.
 */

public class CustomUIRateThreeAlert extends CustomUIRateBaseAlert {

    public CustomUIRateThreeAlert (@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_ui_rate_three_alert);
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }
}
