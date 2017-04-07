package com.ihs.keyboardutils.alerts;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
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

    CustomDesignAlert(@NonNull Context context) {
        super(context, R.style.DesignDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_design_alert);

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

        int screenWidth = DisplayUtils.getScreenWidthPixels();
        int width = (int) getContext().getResources().getFraction(R.fraction.design_dialog_width, screenWidth, screenWidth);
        findViewById(R.id.root_view).getLayoutParams().width = width;

        findViewById(R.id.iv_image).getLayoutParams().height = width / 2;

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
            positiveButton.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(positiveButtonColor, radius));
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
            } else {
                findViewById(layout).setVisibility(View.GONE);
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

        super.show();

        /**
         * 设置dialog宽度全屏
         */
        WindowManager.LayoutParams params = getWindow().getAttributes();  //获取对话框当前的参数值、
        params.width = DisplayUtils.getScreenWidthPixels();    //宽度设置全屏宽度
        getWindow().setAttributes(params);     //设置生效
    }
}
