package com.ihs.keyboardutils.alerts;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import com.ihs.app.framework.HSApplication;

public class KCAlert {
    public static final int INVALID_COLOR = -1;

    private CustomDesignAlert alert;

    private KCAlert(Context context, boolean isFullScreen) {
        if (isFullScreen) {
            alert = new CustomDesignAlert(context, isFullScreen);
        } else {
            alert = new CustomDesignAlert(context);
        }
    }

    public void show() {
        alert.show();
    }

    public void dismiss() {
        alert.dismiss();
    }

    private static class AlertParams {
        private int topImageResId;
        private int positiveButtonColor;
        private boolean cancelable;
        private boolean canceledOnTouchOutside;
        private CharSequence title;
        private CharSequence message;
        private CharSequence adText;
        private CharSequence positiveButtonText;
        private CharSequence negativeButtonText;
        private View.OnClickListener positiveButtonClickListener;
        private View.OnClickListener negativeButtonClickListener;
        private DialogInterface.OnDismissListener onDismissListener;
        private DialogInterface.OnCancelListener onCancelListener;
        private boolean isFullScreen;

        AlertParams() {
            cancelable = true;
            canceledOnTouchOutside = true;
        }

        void apply(CustomDesignAlert alert) {
            if (!TextUtils.isEmpty(title)) {
                alert.setTitle(title);
            }

            if (!TextUtils.isEmpty(message)) {
                alert.setMessage(message);
            }

            if (!TextUtils.isEmpty(adText)) {
                alert.setAdText(adText);
            }

            if (!TextUtils.isEmpty(positiveButtonText)) {
                alert.setPositiveButton(positiveButtonText, positiveButtonClickListener, positiveButtonColor);
            }

            if (!TextUtils.isEmpty(negativeButtonText)) {
                alert.setNegativeButton(negativeButtonText, negativeButtonClickListener);
            }

            if (onDismissListener != null) {
                alert.setOnDismissListener(onDismissListener);
            }

            if (onCancelListener != null) {
                alert.setOnCancelListener(onCancelListener);
            }

            alert.setCancelable(cancelable);
            alert.setCanceledOnTouchOutside(canceledOnTouchOutside);
            alert.setTopImageResource(topImageResId);
            alert.setFullScreen(isFullScreen);
        }
    }

    public static class Builder {
        private Context context;
        private AlertParams alertParams;

        public Builder() {
            this(HSApplication.getContext());
        }

        public Builder(Context context) {
            this.context = context;
            alertParams = new AlertParams();
        }

        public Builder setTitle(CharSequence title) {
            alertParams.title = title;
            return this;
        }

        public Builder setAdText(CharSequence adText) {
            alertParams.adText = adText;
            return this;
        }

        public Builder setMessage(CharSequence message) {
            alertParams.message = message;
            return this;
        }

        public Builder setPositiveButton(CharSequence text, View.OnClickListener listener) {
            setPositiveButton(text, listener, INVALID_COLOR);
            return this;
        }

        public Builder setPositiveButton(CharSequence text, View.OnClickListener listener, int color) {
            alertParams.positiveButtonText = text;
            alertParams.positiveButtonClickListener = listener;
            alertParams.positiveButtonColor = color;
            return this;
        }

        public Builder setNegativeButton(CharSequence text, View.OnClickListener listener) {
            alertParams.negativeButtonText = text;
            alertParams.negativeButtonClickListener = listener;
            return this;
        }

        public Builder setTopImageResource(int resId) {
            alertParams.topImageResId = resId;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            alertParams.cancelable = cancelable;
            return this;
        }

        public Builder setCanceledOnTouchOutside(boolean canceledOnTouchOutside) {
            alertParams.canceledOnTouchOutside = canceledOnTouchOutside;
            return this;
        }

        public Builder setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
            alertParams.onDismissListener = onDismissListener;
            return this;
        }

        public Builder setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
            alertParams.onCancelListener = onCancelListener;
            return this;
        }

        public Builder setFullScreen(boolean isFullScreen) {
            alertParams.isFullScreen = isFullScreen;
            return this;
        }


        public KCAlert build() {
            KCAlert alert = new KCAlert(context, alertParams.isFullScreen);
            alertParams.apply(alert.alert);
            return alert;
        }

        public KCAlert show() {
            KCAlert alert = build();
            alert.show();
            return alert;
        }
    }
}
