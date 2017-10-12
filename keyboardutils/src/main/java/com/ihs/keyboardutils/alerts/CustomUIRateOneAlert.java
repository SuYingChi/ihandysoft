package com.ihs.keyboardutils.alerts;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.WindowManager;

import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.keyboardutils.R;

/**
 * Created by yang.liu on 2017/10/11.
 */

public class CustomUIRateOneAlert extends CustomUIRateBaseAlert {

    public CustomUIRateOneAlert (@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_ui_rate_one_alert);

        int screenWidth = DisplayUtils.getScreenWidthPixels();
        int width = (int) getContext().getResources().getFraction(R.fraction.design_dialog_width, screenWidth, screenWidth);
        findViewById(R.id.root_view).getLayoutParams().width = width;

        if (!(getContext() instanceof Activity)) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(HSApplication.getContext())) {
                getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
            } else {
                getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }
        }
    }

    @Override
    public void show() {
        super.show();

        /**
         * 设置dialog宽度全屏
         */
        WindowManager.LayoutParams params = getWindow().getAttributes();  //获取对话框当前的参数值、
        params.width = DisplayUtils.getScreenWidthPixels();    //宽度设置全屏宽度
        getWindow().setAttributes(params);     //设置生效
    }
}
