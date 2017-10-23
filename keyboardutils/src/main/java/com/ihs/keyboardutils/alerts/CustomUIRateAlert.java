package com.ihs.keyboardutils.alerts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.RippleDrawableUtils;

import java.util.Locale;

public class CustomUIRateAlert extends CustomUIRateBaseAlert {
    private View.OnClickListener positiveButtonClickListener;
    private View.OnClickListener negativeButtonClickListener;
    private View.OnClickListener neutralButtonClickListener;
    private TextView positiveButton;
    private TextView negativeButton;
    private int rateStarNumber;
    private boolean useDefaultLanguage = false;
    private String language;

    public CustomUIRateAlert(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_ui_rate_alert);

        language = Locale.getDefault().getLanguage();

        if (HSConfig.optString("No such language!", "Application", "RateAlert", "Type0", "Step1", "title", language).equals("No such language!")) {
            useDefaultLanguage = true;
        }

        initViews();

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

    private void initViews() {
        initFirstScreen();
        initSecondScreen();
    }

    private void initFirstScreen() {
        TextView titleTextView = (TextView) findViewById(R.id.tv_first_screen_title);
        if (!useDefaultLanguage) {
            titleTextView.setText(HSConfig.optString("", "Application", "RateAlert", "Type0", "Step1", "title", language));
            ((TextView) findViewById(R.id.first_screen_body)).setText(HSConfig.optString("", "Application", "RateAlert", "Type0", "Step1", "body", language));
        } else {
            titleTextView.setText(HSConfig.optString("", "Application", "RateAlert", "Type0", "Step1", "title", "en"));
            ((TextView) findViewById(R.id.first_screen_body)).setText(HSConfig.optString("", "Application", "RateAlert", "Type0", "Step1", "body", "en"));
        }


        int starIds[] = {R.id.start_1, R.id.start_2, R.id.start_3, R.id.start_4, R.id.start_5};
        for (int id : starIds) {
            findViewById(id).setOnClickListener(this);
        }
    }

    private void initSecondScreen() {
        positiveButton = (TextView) findViewById(R.id.btn_positive);
        negativeButton = (TextView) findViewById(R.id.btn_negative);

        float radius = getContext().getResources().getDimension(R.dimen.design_base_corner_radius);

        if (positiveButton != null) {
            positiveButton.setOnClickListener(this);
            positiveButton.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(0xffd53bf1, radius));
        }

        if (negativeButton != null) {
            if (!useDefaultLanguage) {
                negativeButton.setText(HSConfig.optString("", "Application", "RateAlert", "Type0", "Step2", "NO", "button1", language));
            } else {
                negativeButton.setText(HSConfig.optString("", "Application", "RateAlert", "Type0", "Step2", "NO", "button1", "en"));
            }
            negativeButton.setOnClickListener(this);
            negativeButton.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(Color.WHITE, radius));
        }
    }

    private void updateSecondScreen() {
        TextView titleTextView = (TextView) findViewById(R.id.tv_second_screen_title);

        if (rateStarNumber < 5) {
            if (!useDefaultLanguage) {
                titleTextView.setText(HSConfig.optString("", "Application", "RateAlert", "Type0", "Step2", "NO", "body", language));
                positiveButton.setText(HSConfig.optString("", "Application", "RateAlert", "Type0", "Step2", "NO", "button2", language));
            } else {
                titleTextView.setText(HSConfig.optString("", "Application", "RateAlert", "Type0", "Step2", "NO", "body", "en"));
                positiveButton.setText(HSConfig.optString("", "Application", "RateAlert", "Type0", "Step2", "NO", "button2", "en"));
            }
        } else {
            if (!useDefaultLanguage) {
                titleTextView.setText(HSConfig.optString("", "Application", "RateAlert", "Type0", "Step2", "YES", "body", language));
                positiveButton.setText(HSConfig.optString("", "Application", "RateAlert", "Type0", "Step2", "YES", "button2", language));
            } else {
                titleTextView.setText(HSConfig.optString("", "Application", "RateAlert", "Type0", "Step2", "YES", "body", "en"));
                positiveButton.setText(HSConfig.optString("", "Application", "RateAlert", "Type0", "Step2", "YES", "button2", "en"));
            }
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v == positiveButton) {
            dismiss();

            if (rateStarNumber != 5) {
                if (neutralButtonClickListener != null) {
                    neutralButtonClickListener.onClick(v);
                }

                sendEmail();
            } else {
                if (positiveButtonClickListener != null) {
                    positiveButtonClickListener.onClick(v);
                }
            }
        } else if (v == negativeButton) {
            dismiss();
            if (negativeButtonClickListener != null) {
                negativeButtonClickListener.onClick(v);
            }
        } else if (v.getId() == R.id.start_1) {
            rate(1);
        } else if (v.getId() == R.id.start_2) {
            rate(2);
        } else if (v.getId() == R.id.start_3) {
            rate(3);
        } else if (v.getId() == R.id.start_4) {
            rate(4);
        } else if (v.getId() == R.id.start_5) {
            rate(5);
        }
    }

    public void setPositiveButtonOnClickListener(View.OnClickListener listener) {
        this.positiveButtonClickListener = listener;
    }

    public void setNegativeButtonOnClickListener(View.OnClickListener listener) {
        this.negativeButtonClickListener = listener;
    }

    public void setNeutralButtonOnClickListener(View.OnClickListener listener) {
        this.neutralButtonClickListener = listener;
    }

    @Override
    public void show() {
        super.show();

        doIconAnimation();

        /**
         * 设置dialog宽度全屏
         */
        WindowManager.LayoutParams params = getWindow().getAttributes();  //获取对话框当前的参数值、
        params.width = DisplayUtils.getScreenWidthPixels();    //宽度设置全屏宽度
        getWindow().setAttributes(params);     //设置生效
    }

    private void rate(final int starNumber) {
        this.rateStarNumber = starNumber;

        // Disable all click event
        final int starIds[] = {R.id.start_1, R.id.start_2, R.id.start_3, R.id.start_4, R.id.start_5};
        for (int i = 0; i < starIds.length; ++i) {
            ImageView iv = (ImageView) findViewById(starIds[i]);
            iv.setEnabled(false);

            if (i < starNumber) {
                iv.setImageResource(R.drawable.star_golden);
            }
        }

        positiveButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                switchToSecondScreen();
            }
        }, 500);
    }

    private void switchToSecondScreen() {
        // Update second screen content by count
        updateSecondScreen();

        // Do transition animation
        final View firstScreen = findViewById(R.id.rl_first_screen);
        final View secondScreen = findViewById(R.id.rl_second_screen);

        final AlphaAnimation alphaIn = new AlphaAnimation(0.0f, 1.0f);
        alphaIn.setDuration(500);
        alphaIn.setFillAfter(true);

        AlphaAnimation alphaOut = new AlphaAnimation(1.0f, 0.0f);
        alphaOut.setDuration(500);
        alphaOut.setFillAfter(true);
        alphaOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                firstScreen.setVisibility(View.INVISIBLE);
                secondScreen.setVisibility(View.VISIBLE);
                secondScreen.startAnimation(alphaIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        firstScreen.startAnimation(alphaOut);

    }

    private void doIconAnimation() {
        View headView = findViewById(R.id.icon_head);
        final View footView = findViewById(R.id.icon_foot);

        TranslateAnimation raiseUpAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0);
        raiseUpAnim.setDuration(500);
        raiseUpAnim.setFillAfter(true);
        raiseUpAnim.setInterpolator(new DecelerateInterpolator());
        raiseUpAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                footView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        headView.startAnimation(raiseUpAnim);
    }

    private void sendEmail() {
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
