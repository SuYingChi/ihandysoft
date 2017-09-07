package com.launcher.locker;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

import com.artw.lockscreen.common.BaseSettingsActivity;
import com.ihs.commons.config.HSConfig;
import com.ihs.feature.common.ViewUtils;
import com.ihs.keyboardutils.R;

public class LockerSettingsActivity extends BaseSettingsActivity
        implements View.OnClickListener, SwitchCompat.OnCheckedChangeListener {

    private View mLockerEnabledLayout;
    private SwitchCompat mLockerEnabledToggle;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_locker_settings;
    }

    @Override
    protected int getTitleId() {
        return R.string.locker_settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLockerEnabledLayout = ViewUtils.findViewById(this, R.id.locker_enabled_cell);
        mLockerEnabledToggle = ViewUtils.findViewById(this, R.id.locker_enabled_button);

        mLockerEnabledToggle.setChecked(com.artw.lockscreen.LockerSettings.isLockerEnabled());

        mLockerEnabledLayout.setOnClickListener(this);
        mLockerEnabledToggle.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == mLockerEnabledLayout) {
            mLockerEnabledToggle.performClick();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mLockerEnabledToggle) {
            com.artw.lockscreen.LockerSettings.setLockerEnabled(isChecked);
        }
    }
}
