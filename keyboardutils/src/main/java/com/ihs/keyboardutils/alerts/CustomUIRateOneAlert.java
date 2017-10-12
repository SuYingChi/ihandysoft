package com.ihs.keyboardutils.alerts;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageButton;
import android.view.View;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.keyboardutils.R;

/**
 * Created by yang.liu on 2017/10/11.
 */

public class CustomUIRateOneAlert extends CustomUIRateBaseAlert {

    private TextView buttonYes;
    private TextView buttonNope;
    private TextView buttonLater;
    private TextView buttonFeedback;
    private TextView buttonFullStar;
    private AppCompatImageButton closeAlert;

    public CustomUIRateOneAlert (@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_ui_rate_one_alert);

        buttonYes = (TextView) findViewById(R.id.btn_yes);
        buttonYes.setOnClickListener(this);
        buttonNope = (TextView) findViewById(R.id.btn_nope);
        buttonNope.setOnClickListener(this);
        buttonLater = (TextView) findViewById(R.id.btn_later);
        buttonLater.setOnClickListener(this);
        buttonFeedback = (TextView) findViewById(R.id.btn_feedback);
        buttonFeedback.setOnClickListener(this);
        buttonFullStar = (TextView) findViewById(R.id.btn_full_star);
        buttonFullStar.setOnClickListener(this);
        setCancelable(false);
    }

    private void updateButton () {
        buttonYes.setVisibility(View.GONE);
        buttonNope.setVisibility(View.GONE);
        buttonLater.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v == buttonYes) {
            updateButton();
            buttonFullStar.setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.rate_alert_title)).setText("Glad to hear that! Rate it 5-Star");
            ((TextView)findViewById(R.id.rate_alert_subtitle)).setText("((TextView)findViewById(R.id.rate_alert_title)).setText(\"Glad to hear that! Rate it 5-Star\");");
        } else if (v == buttonNope) {
            updateButton();
            buttonFeedback.setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.rate_alert_title)).setText("Glad to hear that! Rate it 5-Star");
            ((TextView)findViewById(R.id.rate_alert_subtitle)).setText("((TextView)findViewById(R.id.rate_alert_title)).setText(\"Glad to hear that! Rate it 5-Star\");");
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
