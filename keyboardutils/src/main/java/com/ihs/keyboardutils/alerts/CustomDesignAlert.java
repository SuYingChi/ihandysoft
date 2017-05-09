package com.ihs.keyboardutils.alerts;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.RippleDrawableUtils;

class CustomDesignAlert extends AlertDialog implements View.OnClickListener {
    private static final String TAG = CustomDesignAlert.class.getSimpleName();
    private int imageResId;
    private CharSequence title;
    private CharSequence message;
    private CharSequence positiveButtonText;
    private CharSequence negativeButtonText;
    private View.OnClickListener positiveButtonClickListener;
    private View.OnClickListener negativeButtonClickListener;
    private TextView positiveButton;
    private TextView negativeButton;
    private boolean isFullScreen;

    CustomDesignAlert(@NonNull Context context) {
        super(context, R.style.DesignDialog);
    }

    CustomDesignAlert(@NonNull Context context,boolean isFullScreen) {
        super(context, R.style.FullscreenDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        if (!isFullScreen) {
            setContentView(R.layout.custom_design_alert);
        } else {
            setContentView(R.layout.custom_design_alert_full);
        }

        TextView titleTextView = (TextView) findViewById(R.id.tv_title);
        titleTextView.setText(title);

        TextView messageTextView = (TextView) findViewById(R.id.tv_message);
        messageTextView.setText(message);

        ImageView imageView = (ImageView) findViewById(R.id.iv_image);
        if (imageResId != 0) {
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageResource(imageResId);
        } else {
            imageView.setVisibility(View.GONE);
        }

        prepareButtons();

        if (isFullScreen) {
            View iv_close = findViewById(R.id.iv_close);
            if (iv_close != null) {
                iv_close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cancel();
                    }
                });
                iv_close.setBackgroundDrawable(RippleDrawableUtils.getTransparentRippleBackground());
            }
        } else {
            int screenWidth = DisplayUtils.getScreenWidthPixels();
            int width = (int) getContext().getResources().getFraction(R.fraction.design_dialog_width, screenWidth, screenWidth);
            findViewById(R.id.root_view).getLayoutParams().width = width;
            findViewById(R.id.iv_image).getLayoutParams().height = width / 2;
        }

        if (!(getContext() instanceof Activity)) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(HSApplication.getContext())) {
                getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
            } else {
                getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }
        }
    }

    private void prepareButtons() {
        if (TextUtils.isEmpty(negativeButtonText)) {
            prepareButtons(R.id.ll_single_button, R.id.btn_positive_single, 0xff4db752, 0, 0);
        } else {
            prepareButtons(R.id.ll_buttons, R.id.btn_positive, 0xff3d6efa, R.id.btn_negative, Color.WHITE);
        }
    }

    private void prepareButtons(int visibleButtonLayoutId, int positiveButtonId, int positiveButtonColor, int negativeButtonId, int negativeButtonColor) {
        prepareButtonLayout(visibleButtonLayoutId);

        positiveButton = (TextView) findViewById(positiveButtonId);
        negativeButton = (TextView) findViewById(negativeButtonId);

        float radius = getContext().getResources().getDimension(R.dimen.design_base_corner_radius);

        if (positiveButton != null) {
            positiveButton.setText(positiveButtonText);
            positiveButton.setOnClickListener(this);
            if (isFullScreen) {
                positiveButton.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(Color.WHITE, radius));
            } else {
                positiveButton.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(positiveButtonColor, radius));
            }
        }

        if (negativeButton != null) {
            negativeButton.setText(negativeButtonText);
            negativeButton.setOnClickListener(this);
            negativeButton.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(negativeButtonColor, radius));
        }
    }

    private void prepareButtonLayout(int visibleLayoutId) {
        int layouts[] = {R.id.ll_single_button, R.id.ll_buttons};

        for (int layout : layouts) {
            if (layout == visibleLayoutId) {
                findViewById(layout).setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        dismiss();

        if (v == positiveButton) {
            if (positiveButtonClickListener != null) {
                positiveButtonClickListener.onClick(v);
            }
        } else if (v == negativeButton) {
            if (negativeButtonClickListener != null) {
                negativeButtonClickListener.onClick(v);
            }
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        this.title = title;
    }

    @Override
    public void setMessage(CharSequence message) {
        this.message = message;
    }

    void setPositiveButton(CharSequence text, View.OnClickListener listener) {
        HSLog.d(TAG, text.toString());
        this.positiveButtonText = text;
        this.positiveButtonClickListener = listener;
    }

    void setNegativeButton(CharSequence text, View.OnClickListener listener) {
        HSLog.d(TAG, text.toString());
        this.negativeButtonText = text;
        this.negativeButtonClickListener = listener;
    }

    boolean isFullScreen() {
        return isFullScreen;
    }

    void setFullScreen(boolean fullScreen) {
        isFullScreen = fullScreen;
    }

    void setTopImageResource(int resId) {
        this.imageResId = resId;
    }

    private boolean isInvalid() {
        return TextUtils.isEmpty(positiveButtonText);
    }

    @Override
    public void show() {
        if (isInvalid()) {
            HSLog.e(TAG, "Invalid dialog");
            return;
        }

        if (isFullScreen) {
            /**
             * 设置dialog宽度全屏
             */
//            WindowManager.LayoutParams params = getWindow().getAttributes();  //获取对话框当前的参数值、

            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

            layoutParams.format = PixelFormat.RGBA_8888;
            layoutParams.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN;
            layoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            layoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

            layoutParams.width = DisplayUtils.getScreenWidthPixels();    //宽度设置全屏宽度
            layoutParams.height = DisplayUtils.getScreenHeightPixels();    //宽度设置全屏宽度


//            getWindow().setAttributes(layoutParams);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        super.show();

    }
}
