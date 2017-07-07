package com.ihs.feature.battery;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.artw.lockscreen.common.BaseSettingsActivity;
import com.artw.lockscreen.common.SystemSettingsManager;
import com.ihs.feature.common.ActivityUtils;
import com.ihs.feature.common.VectorCompat;
import com.ihs.feature.common.ViewUtils;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.CommonUtils;

import java.util.HashMap;

public class BatteryModeActivity extends BaseSettingsActivity implements View.OnClickListener {

    public static final String TAG = "Mode_Tag";

    private BatteryModeManager mBatteryModeManager;

    private ViewGroup mSmartSaverLayout;
    private ViewGroup mMaxSaverLayout;
    private ViewGroup mCurrentLayout;
    private RadioButton mSmartSaverRb;
    private RadioButton mMaxSaverRb;
    private RadioButton mCurrentRb;
    private TextView tvCurrent;
    private TextView tvCurrentDescription;
    private AppCompatImageView mCurrentIv;

    enum ModeType {
        SMART_SAVER,
        MAX_SAVER,
        CURRENT_SAVER
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBatteryModeManager = new BatteryModeManager(this);

        initView();
        setListeners();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_battery_mode;
    }

    @Override
    protected int getTitleId() {
        return R.string.battery_mode;
    }

    private void initView() {
        mSmartSaverLayout = ViewUtils.findViewById(this, R.id.smart_saver_rl);
        mMaxSaverLayout = ViewUtils.findViewById(this, R.id.max_saver_rl);
        mCurrentLayout = ViewUtils.findViewById(this, R.id.current_rl);
        mSmartSaverRb = ViewUtils.findViewById(this, R.id.smart_saver_rb);
        mMaxSaverRb = ViewUtils.findViewById(this, R.id.max_saver_rb);
        mCurrentRb = ViewUtils.findViewById(this, R.id.current_rb);
        mCurrentIv = ViewUtils.findViewById(this, R.id.current_iv);
        tvCurrent = ViewUtils.findViewById(this, R.id.current_tv);
        tvCurrentDescription = ViewUtils.findViewById(this, R.id.current_description_tv);

        if (BatteryModeManager.isFirstLaunch()) {
            BatteryModeManager.setFirstLaunched();
        } else {
            int lastMode = BatteryModeManager.getLastMode();
            if (lastMode == ModeType.SMART_SAVER.ordinal() && BatteryModeManager.isModeEqual(
                    mBatteryModeManager.getCurrentMode(), BatteryModeManager.MODE_SMART_SAVER)) {
                clickModeRadioButton(ModeType.SMART_SAVER);
                tvCurrent.setText(getString(R.string.battery_previous));
                tvCurrentDescription.setText(getString(R.string.battery_previous_content));
                mCurrentIv.setImageDrawable(VectorCompat.createVectorDrawable(this, R.drawable.battery_previous_svg));
            } else if (lastMode == ModeType.MAX_SAVER.ordinal() && BatteryModeManager.isModeEqual(
                    mBatteryModeManager.getCurrentMode(), BatteryModeManager.MODE_MAX_SAVER)) {
                clickModeRadioButton(ModeType.MAX_SAVER);
                tvCurrent.setText(getString(R.string.battery_previous));
                tvCurrentDescription.setText(getString(R.string.battery_previous_content));
                mCurrentIv.setImageDrawable(VectorCompat.createVectorDrawable(this, R.drawable.battery_previous_svg));
            } else {
                BatteryModeManager.setLastMode(ModeType.CURRENT_SAVER);
                clickModeRadioButton(ModeType.CURRENT_SAVER);
                mCurrentIv.setImageDrawable(VectorCompat.createVectorDrawable(this, R.drawable.battery_current_svg));
            }
        }

        mSmartSaverRb.setClickable(false);
        mMaxSaverRb.setClickable(false);
        mCurrentRb.setClickable(false);
    }

    private void setListeners() {
        mSmartSaverLayout.setOnClickListener(this);
        mMaxSaverLayout.setOnClickListener(this);
        mCurrentLayout.setOnClickListener(this);
    }

    @SuppressLint("NewApi")
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        CommonUtils.setupTransparentSystemBarsForLmp(this);
        View viewContainer = ViewUtils.findViewById(this, R.id.view_container);
        viewContainer.setPadding(0, CommonUtils.getStatusBarHeight(this), 0, 0);
        ActivityUtils.setNavigationBarColor(this, Color.BLACK);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBatteryModeManager.release();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.smart_saver_rl || i == R.id.smart_saver_rb) {
            showSaverDialog(ModeType.SMART_SAVER);

        } else if (i == R.id.max_saver_rl || i == R.id.max_saver_rb) {
            showSaverDialog(ModeType.MAX_SAVER);

        } else if (i == R.id.current_rl || i == R.id.current_rb) {
            showSaverDialog(ModeType.CURRENT_SAVER);

        }
    }

    private void clickModeRadioButton(ModeType modeType) {
        switch (modeType) {
            case SMART_SAVER:
                mSmartSaverRb.setChecked(true);
                mMaxSaverRb.setChecked(false);
                mCurrentRb.setChecked(false);
                break;
            case MAX_SAVER:
                mMaxSaverRb.setChecked(true);
                mSmartSaverRb.setChecked(false);
                mCurrentRb.setChecked(false);
                break;
            case CURRENT_SAVER:
                mCurrentRb.setChecked(true);
                mMaxSaverRb.setChecked(false);
                mSmartSaverRb.setChecked(false);
                break;
            default:
                break;
        }
    }

    private void showSaverDialog(final ModeType modeType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        View contentView = LayoutInflater.from(BatteryModeActivity.this).inflate(R.layout.battery_alert_content, null);
        builder.setView(contentView);
        builder.setTitle(getDialogTitleRes(modeType));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            contentView.findViewById(R.id.mobile_data_rl).setVisibility(View.GONE);
        }
        final boolean isEqual = setDialogContent(contentView, modeType);
        final boolean isPositiveButtonGone = (isEqual && modeType.ordinal() == BatteryModeManager.getLastMode());

        builder.setPositiveButton(BatteryModeActivity.this.getString(R.string.apply_btn),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Handler(getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                onDialogPositiveButtonClicked(modeType, isPositiveButtonGone);
                            }
                        });
                    }
                });

        builder.setNegativeButton(BatteryModeActivity.this.getString(android.R.string.cancel), (dialog2, which) -> {});

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.battery_dialog_cancel));
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                if (isPositiveButtonGone) {
                    positiveButton.setVisibility(View.GONE);
                } else {
                    if (modeType.ordinal() == BatteryModeManager.getLastMode()) {
                        positiveButton.setText(getString(R.string.battery_reapply));
                    }
                    positiveButton.setTextColor(
                            ContextCompat.getColor(BatteryModeActivity.this, R.color.battery_mode_green));
                }
            }
        });
        dialog.show();
    }

    private int getDialogTitleRes(ModeType type) {
        switch (type) {
            case SMART_SAVER:
                return R.string.battery_smart_saver;
            case MAX_SAVER:
                return R.string.battery_max_saver;
            case CURRENT_SAVER:
                if (BatteryModeManager.getLastMode() != ModeType.CURRENT_SAVER.ordinal()) {
                    return R.string.battery_previous;
                } else {
                    return R.string.battery_current;
                }
            default:
                return R.string.battery_smart_saver;
        }
    }

    private boolean setDialogContent(View contentView, ModeType modeType) {
        TextView brightnessSwitchTv = (TextView) contentView.findViewById(R.id.brightness_switch_tv);
        TextView screenTimeOutSwitchTv = (TextView) contentView.findViewById(R.id.screen_timeout_switch_tv);
        TextView vibrateSwitchTv = (TextView) contentView.findViewById(R.id.vibrate_switch_tv);
        TextView wifiSwitchTv = (TextView) contentView.findViewById(R.id.wifi_switch_tv);
        TextView bluetoothSwitchTv = (TextView) contentView.findViewById(R.id.bluetooth_switch_tv);
        TextView autoSyncSwitchTv = (TextView) contentView.findViewById(R.id.auto_sync_switch_tv);
        TextView mobileDataSwitchTv = (TextView) contentView.findViewById(R.id.mobile_data_switch_tv);
        TextView hapticFeedbackSwitchTv = (TextView) contentView.findViewById(R.id.haptic_feedback_switch_tv);

        HashMap<SystemSettingsManager.SettingsItem, Integer> dstMode;
        HashMap<SystemSettingsManager.SettingsItem, Integer> srcMode = mBatteryModeManager.getCurrentMode();
        if (modeType == ModeType.CURRENT_SAVER) {
            if (BatteryModeManager.getLastMode() == 2) {
                dstMode = srcMode;
            } else {
                dstMode = BatteryModeManager.getPreviousMode();
            }
        } else if (modeType == ModeType.MAX_SAVER) {
            dstMode = BatteryModeManager.MODE_MAX_SAVER;
        } else {
            dstMode = BatteryModeManager.MODE_SMART_SAVER;
        }

        // brightness
        int brightness = dstMode.get(SystemSettingsManager.SettingsItem.BRIGHTNESS);
        if (brightness == -1) {
            brightnessSwitchTv.setText(getString(R.string.battery_auto));
        } else {
            brightnessSwitchTv.setText(String.format("%d%%", (int) (brightness / 255f * 100)));
        }
        checkHighlight(dstMode, srcMode, SystemSettingsManager.SettingsItem.BRIGHTNESS, brightnessSwitchTv);
        // screen timeout
        screenTimeOutSwitchTv.setText(formatScreenTimeoutString(dstMode.get(SystemSettingsManager.SettingsItem.SCREEN_TIMEOUT)));
        checkHighlight(dstMode, srcMode, SystemSettingsManager.SettingsItem.SCREEN_TIMEOUT, screenTimeOutSwitchTv);
        // vibration
        vibrateSwitchTv.setText(dstMode.get(SystemSettingsManager.SettingsItem.VIBRATE) == 1 ? getString(R.string.battery_on) : getString(R.string.battery_off));
        checkHighlight(dstMode, srcMode, SystemSettingsManager.SettingsItem.VIBRATE, vibrateSwitchTv);
        // wifi
        wifiSwitchTv.setText(dstMode.get(SystemSettingsManager.SettingsItem.WIFI) == 1 ? getString(R.string.battery_on) : getString(R.string.battery_off));
        checkHighlight(dstMode, srcMode, SystemSettingsManager.SettingsItem.WIFI, wifiSwitchTv);
        // bluetooth
        bluetoothSwitchTv.setText(dstMode.get(SystemSettingsManager.SettingsItem.BLUETOOTH) == 1 ? getString(R.string.battery_on) : getString(R.string.battery_off));
        checkHighlight(dstMode, srcMode, SystemSettingsManager.SettingsItem.BLUETOOTH, bluetoothSwitchTv);
        // auto sync
        autoSyncSwitchTv.setText(dstMode.get(SystemSettingsManager.SettingsItem.AUTO_SYNC) == 1 ? getString(R.string.battery_on) : getString(R.string.battery_off));
        checkHighlight(dstMode, srcMode, SystemSettingsManager.SettingsItem.AUTO_SYNC, autoSyncSwitchTv);
        // mobile data
        mobileDataSwitchTv.setText(dstMode.get(SystemSettingsManager.SettingsItem.MOBILE_DATA) == 1 ? getString(R.string.battery_on) : getString(R.string.battery_off));
        checkHighlight(dstMode, srcMode, SystemSettingsManager.SettingsItem.MOBILE_DATA, mobileDataSwitchTv);
        // haptic feedback
        hapticFeedbackSwitchTv.setText(dstMode.get(SystemSettingsManager.SettingsItem.HAPTIC_FEEDBACK) == 1 ? getString(R.string.battery_on) : getString(R.string.battery_off));
        checkHighlight(dstMode, srcMode, SystemSettingsManager.SettingsItem.HAPTIC_FEEDBACK, hapticFeedbackSwitchTv);

        return BatteryModeManager.isModeEqual(srcMode, dstMode);
    }

    private void checkHighlight(HashMap<SystemSettingsManager.SettingsItem, Integer> dstMode,
                                HashMap<SystemSettingsManager.SettingsItem, Integer> srcMode,
                                SystemSettingsManager.SettingsItem item,
                                TextView tv) {
        if (dstMode.get(item) == srcMode.get(item)) {
            tv.setTextColor(getResources().getColor(R.color.battery_mode_title));
        } else {
            tv.setTextColor(Color.RED);
        }
    }

    private void onDialogPositiveButtonClicked(ModeType modeType, boolean isPositiveButtonGone) {
        switch (modeType) {
            case SMART_SAVER:
                if (BatteryModeManager.getLastMode() == ModeType.CURRENT_SAVER.ordinal()) {
                    BatteryModeManager.setPreviousDetail(mBatteryModeManager.getCurrentMode());
                }
                mBatteryModeManager.switchToMode(BatteryModeManager.MODE_SMART_SAVER);
                clickModeRadioButton(ModeType.SMART_SAVER);
                tvCurrent.setText(getString(R.string.battery_previous));
                tvCurrentDescription.setText(getString(R.string.battery_previous_content));
                mCurrentIv.setImageDrawable(VectorCompat.createVectorDrawable(this, R.drawable.battery_previous_svg));
                BatteryModeManager.setLastMode(ModeType.SMART_SAVER);
                break;
            case MAX_SAVER:
                if (BatteryModeManager.getLastMode() == ModeType.CURRENT_SAVER.ordinal()) {
                    BatteryModeManager.setPreviousDetail(mBatteryModeManager.getCurrentMode());
                }
                mBatteryModeManager.switchToMode(BatteryModeManager.MODE_MAX_SAVER);
                clickModeRadioButton(ModeType.MAX_SAVER);
                tvCurrent.setText(getString(R.string.battery_previous));
                mCurrentIv.setImageDrawable(VectorCompat.createVectorDrawable(this, R.drawable.battery_previous_svg));
                tvCurrentDescription.setText(getString(R.string.battery_previous_content));
                BatteryModeManager.setLastMode(ModeType.MAX_SAVER);
                break;
            case CURRENT_SAVER:
                clickModeRadioButton(ModeType.CURRENT_SAVER);
                tvCurrent.setText(getString(R.string.battery_current));
                mCurrentIv.setImageDrawable(VectorCompat.createVectorDrawable(this, R.drawable.battery_current_svg));
                tvCurrentDescription.setText(getString(R.string.battery_current_content));
                if (BatteryModeManager.getLastMode() != ModeType.CURRENT_SAVER.ordinal()) {
                    mBatteryModeManager.switchToMode(BatteryModeManager.getPreviousMode());
                    BatteryModeManager.setLastMode(ModeType.CURRENT_SAVER);
                }
                break;
        }
    }

    private String formatScreenTimeoutString(int timeout) {
        if (timeout < 60) {
            return String.format("%ds", timeout);
        } else {
            return String.format("%dmin", timeout / 60);
        }
    }
}
