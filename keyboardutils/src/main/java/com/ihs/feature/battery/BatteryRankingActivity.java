package com.ihs.feature.battery;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.honeycomb.launcher.R;
import com.honeycomb.launcher.settings.BaseSettingsActivity;
import com.honeycomb.launcher.util.ActivityUtils;
import com.honeycomb.launcher.util.CommonUtils;
import com.honeycomb.launcher.util.ViewUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;

public class BatteryRankingActivity extends BaseSettingsActivity implements View.OnClickListener, INotificationObserver {

    public static final String TAG = "BatteryRankingActivity";

    public static final String NOTIFICATION_APP_REMOVED = "NOTIFICATION_APP_REMOVED";
    public static final String KEY_APP_REMOVED_PACKAGE_NAME = "KEY_APP_REMOVED_PACKAGE_NAME";

    private BatteryAppsRecyclerView mAppsRecyclerView;
    private AppCompatCheckBox mHideSystemAppsCheckBox;
    private BatteryDataManager mBatteryDataManager;

    @Override
    public void onReceive(String name, HSBundle bundle) {
        switch (name) {
            case NOTIFICATION_APP_REMOVED:
                if (null != bundle) {
                    final String packageName = bundle.getString(KEY_APP_REMOVED_PACKAGE_NAME, "");
                    HSLog.d(TAG, "BatteryRankingActivity onReceive packageName = " + packageName);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (null != mAppsRecyclerView) {
                                mAppsRecyclerView.refreshForRemove(packageName);
                            }
                        }
                    });
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBatteryDataManager = (BatteryActivity.mBatteryDataManager != null) ? BatteryActivity.mBatteryDataManager : new BatteryDataManager(this);
        initView();
        setListeners();
        getRankAppsAndRefresh();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_battery_rank;
    }

    @Override
    protected int getTitleId() {
        return R.string.battery_rank;
    }

    private void getRankAppsAndRefresh() {
        mAppsRecyclerView.refresh(mBatteryDataManager.getAllRankBatteryApps(false));
    }

    private void initView() {
        mAppsRecyclerView = ViewUtils.findViewById(this, R.id.rank_apps_rv);
        mHideSystemAppsCheckBox = ViewUtils.findViewById(this, R.id.hide_system_apps_cb);
    }

    private void setListeners() {
        mHideSystemAppsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mAppsRecyclerView.refresh(mBatteryDataManager.getAllRankBatteryApps(false));
                } else {
                    mAppsRecyclerView.refresh(mBatteryDataManager.getAllRankBatteryApps(true));
                }
            }
        });
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_APP_REMOVED, this);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HSGlobalNotificationCenter.removeObserver(this);
    }
}
