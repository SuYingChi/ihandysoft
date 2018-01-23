package com.ihs.keyboardutils.alerts;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageButton;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.keyboardutils.R;
import com.kc.utils.KCAnalytics;

import java.util.Locale;

/**
 * Created by yang.liu on 2017/10/11.
 */

public class CustomUIRateThreeAlert extends CustomUIRateBaseAlert {

    private LinearLayout buttonYes;
    private LinearLayout buttonNope;
    private AppCompatImageButton closeAlertIcon;

    public CustomUIRateThreeAlert (@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_ui_rate_three_alert);

        int screenWidth = DisplayUtils.getScreenWidthPixels();
        int width = (int) getContext().getResources().getFraction(R.fraction.design_dialog_width, screenWidth, screenWidth);
        findViewById(R.id.root_view).getLayoutParams().width = width;

        String language = Locale.getDefault().getLanguage();

        setTextWithRightLanguage((TextView)findViewById(R.id.yes_body), "en", "Application", "RateAlert", "Type3", "YES", "body", language);
        setTextWithRightLanguage((TextView)findViewById(R.id.yes_title), "en", "Application", "RateAlert", "Type3", "YES", "title", language);
        setTextWithRightLanguage((TextView)findViewById(R.id.nope_body), "en", "Application", "RateAlert", "Type3", "NO", "body", language);
        setTextWithRightLanguage((TextView)findViewById(R.id.nope_title), "en", "Application", "RateAlert", "Type3", "NO", "title", language);

        buttonYes = (LinearLayout) findViewById(R.id.layout_yes);
        buttonYes.setOnClickListener(this);
        buttonNope = (LinearLayout) findViewById(R.id.layout_nope);
        buttonNope.setOnClickListener(this);
        closeAlertIcon = (AppCompatImageButton) findViewById(R.id.rate_alert_close);
        closeAlertIcon.setOnClickListener(this);
        setCancelable(false);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v == buttonYes) {
            KCAnalytics.logEvent("rate_alert_to_GP");
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
        } else if (v == buttonNope) {
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
        } else if (v == closeAlertIcon) {
            dismiss();
            if (dismissListener != null) {
                dismissListener.onClick(v);
            }
        }
    }
}
