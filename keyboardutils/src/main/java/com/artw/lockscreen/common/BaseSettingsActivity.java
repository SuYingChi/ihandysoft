package com.artw.lockscreen.common;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.view.View;

import com.ihs.feature.common.ActivityUtils;
import com.ihs.feature.common.BasePermissionActivity;
import com.ihs.keyboardutils.R;


public abstract class BaseSettingsActivity extends BasePermissionActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutId());
        ActivityUtils.configSimpleAppBar(this, getString(getTitleId()), ContextCompat.getColor(this, R.color.material_text_black_primary), Color.WHITE, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityUtils.setWhiteStatusBar(this);
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            ActivityUtils.setCustomColorStatusBar(this, Color.BLACK);
        }
    }

    protected abstract @LayoutRes int getLayoutId();

    protected abstract @StringRes int getTitleId();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
