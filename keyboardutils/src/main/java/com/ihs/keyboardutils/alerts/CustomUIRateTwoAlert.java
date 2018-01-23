package com.ihs.keyboardutils.alerts;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.keyboardutils.R;
import com.kc.utils.KCAnalytics;

import java.util.Locale;

/**
 * Created by yang.liu on 2017/10/11.
 */

public class CustomUIRateTwoAlert extends CustomUIRateBaseAlert {

    private Button buttonYes;
    private Button buttonNope;
    private Button buttonNever;
    private Button buttonFullStar;
    private Button buttonFeedback;
    private String language;

    public CustomUIRateTwoAlert(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_ui_rate_two_alert);

        language = Locale.getDefault().getLanguage();

        int screenWidth = DisplayUtils.getScreenWidthPixels();
        int width = (int) getContext().getResources().getFraction(R.fraction.design_dialog_width, screenWidth, screenWidth);
        findViewById(R.id.root_view).getLayoutParams().width = width;

        buttonYes = (Button) findViewById(R.id.first_screen_btn_yes);
        buttonYes.setOnClickListener(this);
        buttonNope = (Button) findViewById(R.id.first_screen_btn_nope);
        buttonNope.setOnClickListener(this);
        buttonFullStar = (Button) findViewById(R.id.second_screen_btn_star);
        buttonFullStar.setOnClickListener(this);
        buttonFeedback = (Button) findViewById(R.id.first_screen_btn_feedback);
        buttonFeedback.setOnClickListener(this);
        setCancelable(false);

        setTextWithRightLanguage((TextView) findViewById(R.id.step_one_body), "en", "Application", "RateAlert", "Type2", "Step1", "body", language);
        setTextWithRightLanguage(buttonYes, "en", "Application", "RateAlert", "Type2", "Step1", "button2", language);
        setTextWithRightLanguage(buttonNope, "en", "Application", "RateAlert", "Type2", "Step1", "button1", language);
        setTextWithRightLanguage(buttonFeedback, "en", "Application", "RateAlert", "Type2",  "Step2", "NO", "button2", language);
        setTextWithRightLanguage(buttonFullStar, "en", "Application", "RateAlert", "Type2",  "Step2", "YES", "button2", language);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v == buttonYes) {
            KCAnalytics.logEvent("rate_alert_like_clicked");
            (findViewById(R.id.root_view)).setVisibility(View.INVISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    (findViewById(R.id.rate_first_screen)).setVisibility(View.GONE);
                    (findViewById(R.id.rate_second_screen)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.root_view)).setVisibility(View.VISIBLE);
                }
            }, 400);
            buttonNever = (Button) findViewById(R.id.second_screen_btn_never);
            setTextWithRightLanguage(buttonNever, "en", "Application", "RateAlert", "Type2", "Step2", "YES", "button1", language);
            buttonNever.setOnClickListener(this);
            setTextWithRightLanguage((TextView) findViewById(R.id.step_two_yes_title), "en", "Application", "RateAlert", "Type2", "Step2", "YES", "title", language);
        } else if (v == buttonNope) {
            ((ImageView) findViewById(R.id.top_img)).setImageResource(R.drawable.rate_alert_type_two_nope);
            (findViewById(R.id.step_one_body)).setVisibility(View.GONE);
            (findViewById(R.id.step_two_nope_body)).setVisibility(View.VISIBLE);
            setTextWithRightLanguage((TextView) findViewById(R.id.step_two_nope_body), "en", "Application", "RateAlert", "Type2", "Step2", "NO", "body", language);
            buttonNever = (Button) findViewById(R.id.first_screen_btn_never);
            setTextWithRightLanguage(buttonNever, "en", "Application", "RateAlert", "Type2", "Step2", "NO", "button1", language);
            buttonNever.setOnClickListener(this);
            (findViewById(R.id.root_view)).setVisibility(View.INVISIBLE);
            (findViewById(R.id.rate_first_screen)).setVisibility(View.INVISIBLE);
            findViewById(R.id.first_button_layout).setVisibility(View.GONE);
            findViewById(R.id.second_button_layout).setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    (findViewById(R.id.rate_first_screen)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.root_view)).setVisibility(View.VISIBLE);
                }
            }, 400);
        } else if (v == buttonNever) {
            dismiss();
            if (dismissListener != null) {
                dismissListener.onClick(v);
            }
        } else if (v == buttonFullStar) {
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
