package com.ihs.keyboardutils.alerts;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.keyboardutils.R;

import java.util.Locale;

/**
 * Created by yang.liu on 2017/10/11.
 */

public class CustomUIRateOneAlert extends CustomUIRateBaseAlert {

    private TextView buttonYes;
    private TextView buttonNope;
    private TextView buttonNever;
    private TextView buttonFeedback;
    private TextView buttonFullStar;
    private String language;
    private boolean useDefaultLanguage = false;
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

        buttonYes = (TextView) findViewById(R.id.btn_yes);
        buttonYes.setOnClickListener(this);
        buttonNope = (TextView) findViewById(R.id.btn_nope);
        buttonNope.setOnClickListener(this);
        buttonNever = (TextView) findViewById(R.id.btn_never);
        buttonNever.setOnClickListener(this);
        buttonFeedback = (TextView) findViewById(R.id.btn_feedback);
        buttonFeedback.setOnClickListener(this);
        buttonFullStar = (TextView) findViewById(R.id.btn_full_star);
        buttonFullStar.setOnClickListener(this);
        setCancelable(false);

        language = Locale.getDefault().getLanguage();

        if (HSConfig.optString("No such language!", "Application", "RateAlert", "Type1", "Step1", "title", language).equals("No such language!")) {
            useDefaultLanguage = true;
        }
        if (!useDefaultLanguage) {
            ((TextView)findViewById(R.id.rate_alert_title)).setText(HSConfig.optString("default", "Application", "RateAlert", "Type1", "Step1", "title", language));
            ((TextView)findViewById(R.id.rate_alert_subtitle)).setText(HSConfig.optString("default", "Application", "RateAlert", "Type1", "Step1", "body", language));
            buttonYes.setText(HSConfig.optString("", "Application", "RateAlert", "Type1", "Step1", "button2", language));
            buttonNope.setText(HSConfig.optString("", "Application", "RateAlert", "Type1", "Step1", "button1", language));
            buttonNever.setText(HSConfig.optString("", "Application", "RateAlert", "Type1", "Step2", "NO", "button1", language));
            buttonFeedback.setText(HSConfig.optString("", "Application", "RateAlert", "Type1", "Step2", "NO", "button2", language));
            buttonFullStar.setText(HSConfig.optString("", "Application", "RateAlert", "Type1", "Step2", "YES", "button2", language));
        } else {
            ((TextView)findViewById(R.id.rate_alert_title)).setText(HSConfig.optString("default", "Application", "RateAlert", "Type1", "Step1", "title", "en"));
            ((TextView)findViewById(R.id.rate_alert_subtitle)).setText(HSConfig.optString("default", "Application", "RateAlert", "Type1", "Step1", "body", "en"));
            buttonYes.setText(HSConfig.optString("", "Application", "RateAlert", "Type1", "Step1", "button2", "en"));
            buttonNope.setText(HSConfig.optString("", "Application", "RateAlert", "Type1", "Step1", "button1", "en"));
            buttonNever.setText(HSConfig.optString("", "Application", "RateAlert", "Type1", "Step2", "NO", "button1", "en"));
            buttonFeedback.setText(HSConfig.optString("", "Application", "RateAlert", "Type1", "Step2", "NO", "button2", "en"));
            buttonFullStar.setText(HSConfig.optString("", "Application", "RateAlert", "Type1", "Step2", "YES", "button2", "en"));
        }
    }

    private void updateButton () {
        buttonYes.setVisibility(View.GONE);
        buttonNope.setVisibility(View.GONE);
        buttonNever.setVisibility(View.VISIBLE);
    }

    private void switchScreenAnimation() {
        View rootView = findViewById(R.id.dialog_root);

        rootView.setVisibility(View.INVISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                rootView.setVisibility(View.VISIBLE);
            }
        }, 400);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v == buttonYes) {
            updateButton();
            switchScreenAnimation();
            buttonFullStar.setVisibility(View.VISIBLE);
            if (!useDefaultLanguage) {
                ((TextView)findViewById(R.id.rate_alert_title)).setText(HSConfig.optString("", "Application", "RateAlert", "Type1", "Step2", "YES", "body", language));
                ((TextView)findViewById(R.id.rate_alert_subtitle)).setText(HSConfig.optString("", "Application", "RateAlert", "Type1", "Step2", "YES", "title", language));
            } else {
                ((TextView)findViewById(R.id.rate_alert_title)).setText(HSConfig.optString("", "Application", "RateAlert", "Type1", "Step2", "YES", "body", "en"));
                ((TextView)findViewById(R.id.rate_alert_subtitle)).setText(HSConfig.optString("", "Application", "RateAlert", "Type1", "Step2", "YES", "title", "en"));
            }
        } else if (v == buttonNope) {
            updateButton();
            switchScreenAnimation();
            buttonFeedback.setVisibility(View.VISIBLE);
            if (!useDefaultLanguage) {
                ((TextView)findViewById(R.id.rate_alert_title)).setText(HSConfig.optString("", "Application", "RateAlert", "Type1", "Step2", "NO", "body", language));
                ((TextView)findViewById(R.id.rate_alert_subtitle)).setText(HSConfig.optString("", "Application", "RateAlert", "Type1", "Step2", "NO", "title", language));
            } else {
                ((TextView)findViewById(R.id.rate_alert_title)).setText(HSConfig.optString("", "Application", "RateAlert", "Type1", "Step2", "NO", "body", "en"));
                ((TextView)findViewById(R.id.rate_alert_subtitle)).setText(HSConfig.optString("", "Application", "RateAlert", "Type1", "Step2", "NO", "title", "en"));
            }
        } else if (v == buttonNever) {
            dismiss();
            if (dismissListener != null) {
                dismissListener.onClick(v);
            }
        } else if (v == buttonFullStar) {
            dismiss();
            if (dismissListener != null) {
                dismissListener.onClick(v);
            }
            final String appPackageName = getContext().getPackageName();
            try {
                getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (android.content.ActivityNotFoundException anfe) {
                getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        } else if (v == buttonFeedback) {
            dismiss();
            if (dismissListener != null) {
                dismissListener.onClick(v);
            }
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
