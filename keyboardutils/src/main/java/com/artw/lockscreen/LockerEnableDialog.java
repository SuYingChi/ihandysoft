package com.artw.lockscreen;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.alerts.HSAlertDialog;
import com.ihs.keyboardutils.utils.KCAnalyticUtil;
import com.kc.commons.utils.KCCommonUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yanxia on 2017/7/21.
 */

public class LockerEnableDialog extends Dialog {
    private TextView mTvTime;
    private TextView mTvDate;
    private View rootView;

    private Context context;
    private String bgUrl = "";
    private boolean showAppliedText = true;

    public interface OnLockerBgLoadingListener {
        void onFinish();
    }


    private void init() {
        rootView = View.inflate(getContext(), R.layout.dialog_locker_enable, null);
    }

    public LockerEnableDialog(@NonNull Context context, Drawable drawable, String url, boolean showAppliedText) {
        super(context, R.style.LockerEnableDialogTheme);
        this.context = context;
        init();
        rootView.setBackgroundDrawable(drawable);
        bgUrl = url;
        this.showAppliedText = showAppliedText;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);


        setContentView(rootView);
        ImageView exitButton = (ImageView) findViewById(R.id.exit_btn);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        TextView enableButton = (TextView) findViewById(R.id.enable_btn);
        enableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KCAnalyticUtil.logEvent("keyboard_lockeralert_ok_clicked");
                LockerSettings.setLockerEnabled(true);
                LockerSettings.setLockerBgUrl(bgUrl);
                dismiss();
            }
        });
//        enableButton.setBackgroundDrawable(RippleDrawableUtils.getButtonRippleBackground(Color.parseColor("#536dfe")));

        mTvTime = (TextView) findViewById(R.id.locker_enable_time_tv);
        mTvDate = (TextView) findViewById(R.id.locker_enable_data_tv);
        if (!showAppliedText) {
            TextView enableTv = (TextView) findViewById(R.id.enable_title);
            enableTv.setText(R.string.locker_enable_title_no_desc);
        }
        refreshClock();


    }

    private void refreshClock() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        if (!android.text.format.DateFormat.is24HourFormat(HSApplication.getContext()) && hour != 12) {
            hour = hour % 12;
        }
        mTvTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
        DateFormat format = new SimpleDateFormat("MMMM, dd\nEEE", Locale.getDefault());
        mTvDate.setText(format.format(new Date()));
    }

    /**
     * Start the dialog and display it on screen.  The window is placed in the
     * application layer and opaque.  Note that you should not override this
     * method to do initialization when the dialog is shown, instead implement
     * that in {@link #onStart}.
     */
    @Override
    public void show() {
        try {
            if (!(context instanceof Activity)) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(HSApplication.getContext())) {
                    getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
                } else {
                    getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                }
            }
            super.show();
        } catch (Exception ex) {

        }

        KCAnalyticUtil.logEvent("keyboard_lockeralert_show");
        LockerSettings.addLockerEnableShowCount();
    }

    public static void showLockerEnableDialog(Context activity, String url, OnLockerBgLoadingListener bgLoadingListener) {
        showLockerEnableDialog(activity, url, true, bgLoadingListener);
    }

    public static void showLockerEnableDialog(Context activity, String url, boolean showAppliedText, OnLockerBgLoadingListener bgLoadingListener) {
        if (TextUtils.isEmpty(url)) {
            if (bgLoadingListener != null) {
                bgLoadingListener.onFinish();
            }
            return;
        }

        AlertDialog savingDialog = HSAlertDialog.build(activity, 0).setView(R.layout.layout_dialog_applying).setCancelable(false).create();
        savingDialog.setCanceledOnTouchOutside(false);
        ImageLoader.getInstance().loadImage(url, LockerActivity.lockerBgOption, new ImageLoadingListener() {
            private boolean isImgLoaded = false;
            Handler handler = new Handler();
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isImgLoaded) {
                            KCCommonUtils.showDialog(savingDialog);
                        }
                    }
                }, 200);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                isImgLoaded = true;
                handler.removeCallbacksAndMessages(null);
                KCCommonUtils.dismissDialog(savingDialog);
                if (bgLoadingListener != null) {
                    bgLoadingListener.onFinish();
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                isImgLoaded = true;
                handler.removeCallbacksAndMessages(null);
                KCCommonUtils.dismissDialog(savingDialog);

                LockerEnableDialog lockerEnableDialog = new LockerEnableDialog(activity, new BitmapDrawable(activity.getResources(), loadedImage), url, showAppliedText);
                KCCommonUtils.showDialog(lockerEnableDialog);
                lockerEnableDialog.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (bgLoadingListener != null) {
                            bgLoadingListener.onFinish();
                        }
                    }
                });
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                isImgLoaded = true;
                handler.removeCallbacksAndMessages(null);
                KCCommonUtils.dismissDialog(savingDialog);
                if (bgLoadingListener != null) {
                    bgLoadingListener.onFinish();
                }
            }
        });
    }
}
