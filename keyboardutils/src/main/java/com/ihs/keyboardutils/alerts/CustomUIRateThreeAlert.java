package com.ihs.keyboardutils.alerts;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageButton;
import android.view.View;
import android.widget.LinearLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.keyboardutils.R;

/**
 * Created by yang.liu on 2017/10/11.
 */

public class CustomUIRateThreeAlert extends CustomUIRateBaseAlert {

    private LinearLayout buttonYes;
    private LinearLayout buttonNope;
    private AppCompatImageButton closeAlert;

    public CustomUIRateThreeAlert (@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_ui_rate_three_alert);

        buttonYes = (LinearLayout) findViewById(R.id.layout_yes);
        buttonYes.setOnClickListener(this);
        buttonNope = (LinearLayout) findViewById(R.id.layout_nope);
        buttonNope.setOnClickListener(this);
        closeAlert = (AppCompatImageButton) findViewById(R.id.rate_alert_close);
        closeAlert.setOnClickListener(this);
        setCancelable(false);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v == buttonYes) {
            final String appPackageName = getContext().getPackageName();
            try {
                getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (android.content.ActivityNotFoundException anfe) {
                getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        } else if (v == buttonNope) {
            String email = HSApplication.getContext().getString(R.string.feedback_email);
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:" + email));
            emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                HSApplication.getContext().startActivity(emailIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (v == closeAlert) {
            dismiss();
        }
    }
}
