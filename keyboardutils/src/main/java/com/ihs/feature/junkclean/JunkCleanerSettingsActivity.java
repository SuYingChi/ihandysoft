package com.ihs.feature.junkclean;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.artw.lockscreen.common.BaseSettingsActivity;
import com.ihs.feature.common.ActivityUtils;
import com.ihs.feature.common.ViewUtils;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.CommonUtils;
import com.ihs.keyboardutils.utils.ToastUtils;


public class JunkCleanerSettingsActivity extends BaseSettingsActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar).findViewById(R.id.inner_tool_bar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        View notificationCleanerV = ViewUtils.findViewById(this, R.id.notification_cleaner_rl);
        notificationCleanerV.setOnClickListener(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_junk_cleaner_settings;
    }

    @Override
    protected int getTitleId() {
        return R.string.menu_item_settings;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        CommonUtils.setupTransparentSystemBarsForLmp(this);
        View viewContainer = ViewUtils.findViewById(this, R.id.view_container);
        viewContainer.setPadding(0, CommonUtils.getStatusBarHeight(this), 0, 0);
        ActivityUtils.setNavigationBarColor(this, ContextCompat.getColor(this, android.R.color.black));
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.notification_cleaner_rl) {
            ToastUtils.showToast("这里打开Notification收纳界面");
//            KCAnalytics.logEvent("NotificationCleaner_Enterance_Click", "type", NotificationCleanerConstants.JUNK_CLEANER_SETTINGS);
//            NotificationCleanerUtil.checkToStartNotificationOrganizerActivity(JunkCleanerSettingsActivity.this, NotificationCleanerConstants.JUNK_CLEANER_SETTINGS);

        } else {
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

}
