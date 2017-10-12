package com.ihs.keyboardutils.alerts;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import com.ihs.app.framework.HSApplication;
import com.ihs.keyboardutils.R;

/**
 * Created by yang.liu on 2017/10/11.
 */

public class CustomUIRateTwoAlert extends CustomUIRateBaseAlert {

    private Button buttonYes;
    private Button buttonNope;
    private Button buttonLater;
    private Button buttonFullStar;
    private Button buttonFeedback;

    public CustomUIRateTwoAlert (@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_ui_rate_two_alert);

        buttonYes = (Button) findViewById(R.id.rate_alert_button_yes);
        buttonYes.setOnClickListener(this);
        buttonNope = (Button) findViewById(R.id.first_screen_btn_nope);
        buttonNope.setOnClickListener(this);
        buttonFullStar = (Button) findViewById(R.id.second_screen_btn_star);
        buttonFullStar.setOnClickListener(this);
        buttonFeedback = (Button) findViewById(R.id.rate_alert_button_feedback);
        buttonFeedback.setOnClickListener(this);
        setCancelable(false);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v == buttonYes) {
            buttonLater = (Button) findViewById(R.id.second_screen_btn_later);
            buttonLater.setOnClickListener(this);
            findViewById(R.id.rate_first_screen).setVisibility(View.GONE);
            findViewById(R.id.rate_second_screen).setVisibility(View.VISIBLE);
        } else if (v == buttonNope) {
            buttonLater = (Button) findViewById(R.id.first_screen_btn_later);
            buttonLater.setOnClickListener(this);
            findViewById(R.id.first_button_screen).setVisibility(View.GONE);
            findViewById(R.id.second_button_screen).setVisibility(View.VISIBLE);
        } else if (v == buttonLater) {
            dismiss();
        } else if (v == buttonFullStar) {
            final String appPackageName = getContext().getPackageName();
            try {
                getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (android.content.ActivityNotFoundException anfe) {
                getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        } else if (v == buttonFeedback) {
            String email = HSApplication.getContext().getString(R.string.feedback_email);
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:" + email));
            emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                HSApplication.getContext().startActivity(emailIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
