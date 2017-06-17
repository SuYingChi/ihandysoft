package com.ihs.feature.junkclean;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.honeycomb.launcher.R;
import com.honeycomb.launcher.notificationcleaner.NotificationCleanerConstants;
import com.honeycomb.launcher.notificationcleaner.NotificationCleanerUtil;
import com.honeycomb.launcher.settings.BaseSettingsActivity;
import com.honeycomb.launcher.util.ActivityUtils;
import com.honeycomb.launcher.util.CommonUtils;
import com.honeycomb.launcher.util.ViewUtils;
import com.ihs.app.analytics.HSAnalytics;

import static com.flurry.sdk.bb.R;

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
        switch (v.getId()) {
            case R.id.notification_cleaner_rl:
                HSAnalytics.logEvent("NotificationCleaner_Enterance_Click", "type", NotificationCleanerConstants.JUNK_CLEANER_SETTINGS);
                NotificationCleanerUtil.checkToStartNotificationOrganizerActivity(JunkCleanerSettingsActivity.this, NotificationCleanerConstants.JUNK_CLEANER_SETTINGS);
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

}
