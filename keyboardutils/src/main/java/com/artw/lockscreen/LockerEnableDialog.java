package com.artw.lockscreen;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.KCAnalyticUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yanxia on 2017/7/21.
 */

public class LockerEnableDialog extends Dialog {

    private ImageView exitButton;
    private TextView mTvTime;
    private TextView mTvDate;
    private TextView enableButton;

    public LockerEnableDialog(@NonNull Context context) {
        super(context, R.style.LockerEnableDialogTheme);
    }

    public LockerEnableDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_locker_enable);

        exitButton = (ImageView) findViewById(R.id.exit_btn);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        enableButton = (TextView) findViewById(R.id.enable_btn);
        enableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KCAnalyticUtil.logEvent("keyboard_lockeralert_ok_clicked");
                LockerSettings.setLockerEnabled(true);
                onBackPressed();
            }
        });
        mTvTime = (TextView) findViewById(R.id.locker_enable_time_tv);
        mTvDate = (TextView) findViewById(R.id.locker_enable_data_tv);
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
        super.show();
        KCAnalyticUtil.logEvent("keyboard_lockeralert_show");
        LockerSettings.addLockerEnableShowCount();
    }
}
