package com.ihs.feature.boost.plus;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import com.artw.lockscreen.common.BaseSettingsActivity;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.feature.boost.auto.AutoCleanService;
import com.ihs.feature.boost.auto.LockHelper;
import com.ihs.feature.common.AdminReceiver;
import com.ihs.feature.tip.LauncherTipManager;
import com.ihs.feature.common.Profiler;
import com.ihs.feature.common.ViewUtils;
import com.ihs.feature.tip.BoostPlusAutoCleanTip;
import com.ihs.keyboardutils.BuildConfig;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.permission.PermissionUtils;


public class BoostPlusSettingsActivity extends BaseSettingsActivity implements
        View.OnClickListener, SwitchCompat.OnCheckedChangeListener {

    public static final String PREF_KEY_AUTO_BOOST_ENABLED = "auto_boost_enabled";

    private SwitchCompat mAutoBoostToggle;
    private SwitchCompat mDeviceAdminToggle;
    private View mDeviceAdminCell;

    private boolean mInObserverMode;
    private String mCurrentToastType;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_boost_plus_settings;
    }

    @Override
    protected int getTitleId() {
        return R.string.launcher_widget_boost_plus_title;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            Profiler.start("BoostPlusSettingsActivity-create");
        }
        super.onCreate(savedInstanceState);
        boolean currentBoostState = HSPreferenceHelper.getDefault().getBoolean(PREF_KEY_AUTO_BOOST_ENABLED, false);

        RelativeLayout autoBoostCell = ViewUtils.findViewById(this, R.id.boost_plus_settings_auto_boost_cell);
        mAutoBoostToggle = ViewUtils.findViewById(this, R.id.auto_boost_toggle_button);
        mDeviceAdminCell = ViewUtils.findViewById(this, R.id.boost_plus_settings_device_admin_cell);
        mDeviceAdminToggle = ViewUtils.findViewById(this, R.id.device_admin_toggle_button);

        autoBoostCell.setOnClickListener(this);
        mAutoBoostToggle.setChecked(currentBoostState);
        mAutoBoostToggle.setOnCheckedChangeListener(this);

        mDeviceAdminCell.setOnClickListener(this);

        if (BuildConfig.DEBUG) {
            Profiler.end("BoostPlusSettingsActivity-create");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Remove boost+ float button in parent onResume
    }

    private void refresh() {
        boolean currentState = HSPreferenceHelper.getDefault().getBoolean(PREF_KEY_AUTO_BOOST_ENABLED, false);
        // User may change status over observerMode
        boolean newState = mInObserverMode ? checkDeviceEnvironment(false) : (currentState && checkDeviceEnvironment(false));
        mAutoBoostToggle.setChecked(newState);

        if (AdminReceiver.isActiveAdmin(this)) {
            mDeviceAdminCell.setVisibility(View.VISIBLE);
            mDeviceAdminToggle.setChecked(true);
        } else {
            mDeviceAdminCell.setVisibility(View.GONE);
        }
        mInObserverMode = false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !"AlertRoot".equals(mCurrentToastType)) {
            refresh();
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.boost_plus_settings_auto_boost_cell) {
            if (mAutoBoostToggle.isChecked() || checkDeviceEnvironment(true)) {
                mAutoBoostToggle.performClick();
            }

        } else if (i == R.id.boost_plus_settings_device_admin_cell) {
            if (mDeviceAdminToggle.isChecked()) {
                mInObserverMode = true;
                // deativeAdmin is async
                boolean deactived = AdminReceiver.deactiveAdmin(this);
                if (deactived) {
                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            refresh();
                        }
                    }, 200);
                }
            }

        } else {
        }
    }

    private boolean checkDeviceEnvironment(boolean showToast) {
        int rootStatus = RootHelper.grantRootPermissionWithTimeout();
        if (RootHelper.isPermissionGranted(rootStatus)) {
            mCurrentToastType = "AlertRoot";
            return true;
        }

        // Root time > 1s , it mean has dialog show.
        if (RootHelper.isUserActionInvolved(rootStatus)) {
            mCurrentToastType = "AlertRoot";
            return false;
        }
        if (showToast) {
            mInObserverMode = true;
        }

        if (!PermissionUtils.isAccessibilityGranted()) {
            if (showToast) {
                mCurrentToastType = "Alert1";
                LauncherTipManager.getInstance().showTip(this, LauncherTipManager.TipType.AUTO_CLEAN_AUTHORIZE,
                        BoostPlusAutoCleanTip.SETTING_ACCESSIBILITY);
            }
            return false;

        } else if (!AdminReceiver.isActiveAdmin(this)) {
            if (showToast) {
                mCurrentToastType = "Alert2";
                LauncherTipManager.getInstance().showTip(this, LauncherTipManager.TipType.AUTO_CLEAN_AUTHORIZE,
                        BoostPlusAutoCleanTip.SETTING_DEVICE);
            }
            return false;
        } else {
            int keyguardState = LockHelper.getKeyguardState(this);
            if (keyguardState == LockHelper.LOCK_NO_DELAY) {
                if (showToast) {
                    mCurrentToastType = "Alert3";
                    LauncherTipManager.getInstance().showTip(this, LauncherTipManager.TipType.AUTO_CLEAN_AUTHORIZE,
                            BoostPlusAutoCleanTip.SETTING_LOCK_DELAY);
                }
                return false;
            } else if (keyguardState == LockHelper.LOCK_INSTANTLY) {
                if (showToast) {
                    mCurrentToastType = "Alert4";
                    LauncherTipManager.getInstance().showTip(this, LauncherTipManager.TipType.AUTO_CLEAN_AUTHORIZE,
                            BoostPlusAutoCleanTip.SETTING_LOCK_INSTANTLY);
                }
                return false;
            }
        }

        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mAutoBoostToggle) {
            HSPreferenceHelper.getDefault().putBoolean(PREF_KEY_AUTO_BOOST_ENABLED, isChecked);
            if (isChecked) {
                AutoCleanService.start(getApplicationContext());
            } else {
                AutoCleanService.stop(getApplicationContext());
            }
        }
    }

    @Override
    protected void onDestroy() {
        LockHelper.cancelAllTasksAndCallbacks(this);
        super.onDestroy();
    }

    @Override
    protected boolean registerCloseSystemDialogsReceiver() {
        return true;
    }
}
