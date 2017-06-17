package com.ihs.feature.battery;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.honeycomb.launcher.R;
import com.honeycomb.launcher.chargingscreen.ChargingScreenSettings;
import com.honeycomb.launcher.settings.BaseSettingsActivity;
import com.honeycomb.launcher.util.ActivityUtils;
import com.honeycomb.launcher.util.CommonUtils;
import com.honeycomb.launcher.util.ViewUtils;

public class BatterySettingsActivity extends BaseSettingsActivity
        implements View.OnClickListener, SwitchCompat.OnCheckedChangeListener {

    private SwitchCompat mChargingScreenToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.findViewById(this, R.id.charging_screen_cell).setOnClickListener(this);
        mChargingScreenToggle = ViewUtils.findViewById(this, R.id.charging_screen_toggle_button);
        mChargingScreenToggle.setOnCheckedChangeListener(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_battery_settings;
    }

    @Override
    protected int getTitleId() {
        return R.string.menu_item_settings;
    }

    @Override protected void onResume() {
        super.onResume();

        mChargingScreenToggle.setChecked(ChargingScreenSettings.isChargingScreenEnabled());
    }

    @Override public void onAttachedToWindow() {
        super.onAttachedToWindow();

        CommonUtils.setupTransparentSystemBarsForLmp(this);
        View viewContainer = ViewUtils.findViewById(this, R.id.view_container);
        viewContainer.setPadding(0, CommonUtils.getStatusBarHeight(this), 0, 0);
        ActivityUtils.setNavigationBarColor(this, Color.BLACK);
    }

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.charging_screen_cell:
                mChargingScreenToggle.performClick();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mChargingScreenToggle) {
            ChargingScreenSettings.setChargingScreenEnabled(isChecked);
        }
    }
}
