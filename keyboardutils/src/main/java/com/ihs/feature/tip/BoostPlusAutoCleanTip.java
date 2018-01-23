package com.ihs.feature.tip;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.View;
import android.widget.Toast;

import com.artw.lockscreen.common.NavUtils;
import com.ihs.commons.utils.HSLog;
import com.ihs.feature.boost.auto.LockHelper;
import com.ihs.feature.boost.plus.BoostPlusSettingsActivity;
import com.ihs.feature.common.CompatUtils;
import com.ihs.feature.ui.DefaultButtonDialog2;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.ToastUtils;
import com.kc.utils.KCAnalytics;

public class BoostPlusAutoCleanTip extends DefaultButtonDialog2 {
    public static final int SETTING_LOCK_INSTANTLY = 1;
    public static final int SETTING_LOCK_DELAY = 2;
    public static final int SETTING_DEVICE = 3;
    public static final int SETTING_ACCESSIBILITY = 4;
    private static final String TAG = BoostPlusSettingsActivity.class.getSimpleName();

    private int mSubType;

    public BoostPlusAutoCleanTip(Context context, int type) {
        super(context);
        mSubType = type;
        init(context);
    }

    @Override
    protected boolean customInit() {
        return true;
    }

    @Override
    protected int getPositiveButtonStringId() {
        if (mSubType == SETTING_LOCK_DELAY || mSubType == SETTING_LOCK_INSTANTLY) {
            return R.string.set_up;
        }
        return R.string.advanced_boost_authorize_btn;
    }

    @Override
    protected void onClickPositiveButton(View v) {
        final Context context = v.getContext().getApplicationContext();

        if (mSubType == SETTING_LOCK_DELAY ){
            openSecureSettings(context);
            LockHelper.startObservingLockDelayTime(context, new Runnable() {
                @Override
                public void run() {
                    startThisActivity(context);
                }
            });
        } else if( mSubType == SETTING_LOCK_INSTANTLY) {
            openSecureSettings(context);
            LockHelper.startObservingLockInstantly(context, new Runnable() {
                @Override
                public void run() {
                    startThisActivity(context);
                }
            });
        }
        KCAnalytics.logEvent("Boost+_Setting_PowerNap_AlertOpen", "type", getEventLogType());
        super.onClickPositiveButton(v);
    }

    private void startThisActivity(Context context) {
        HSLog.d(TAG, "BoostPlusSettingsActivity start");
        Intent intent = new Intent(context, BoostPlusSettingsActivity.class);
        NavUtils.startActivitySafely(context, intent);
    }

    private void openSecureSettings(Context context) {
        openSettings(shouldGoToTopSettings() ? Settings.ACTION_SETTINGS : Settings.ACTION_SECURITY_SETTINGS, context);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showToast(R.string.boost_plus_security_setting_for_samsung_device, Toast.LENGTH_LONG);
            }
        }, 300);
    }

    private void openSettings(String action, Context context) {
        Intent intent = new Intent(action);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        NavUtils.startActivitySafely(context, intent);
    }

    private boolean shouldGoToTopSettings() {
        return CompatUtils.IS_SAMSUNG_DEVICE;
    }

    private String getEventLogType() {
        switch (mSubType) {
            case SETTING_DEVICE:
                return "Alert2";
            case SETTING_LOCK_DELAY:
                return "Alert3";
            case SETTING_LOCK_INSTANTLY:
                return "Alert4";
            case SETTING_ACCESSIBILITY:
                return "Alert1";
        }
        return "";
    }

    @Override
    protected String getDialogTitle() {
        if (mSubType != SETTING_ACCESSIBILITY) {
            return getResources().getString(R.string.boost_plus_settings_auto_boost_title);
        }
        return "";
    }

    @Override
    protected CharSequence getDialogDesc() {
        switch (mSubType) {
            case SETTING_DEVICE:
                 String str = getResources().getString(R.string.boost_plus_dialog_device_admin);
                 return Html.fromHtml(str);
            case SETTING_LOCK_DELAY:
                return getResources().getString(R.string.boost_plus_settings_lock_delay);
            case SETTING_LOCK_INSTANTLY:
                return getResources().getString(R.string.boost_plus_settings_lock_instantly);
            case SETTING_ACCESSIBILITY:
                return getResources().getString(R.string.boost_plus_dialog_accessibility);

        }
        return "";
    }

    @Override
    protected Drawable getTopImageDrawable() {
        if (mSubType == SETTING_ACCESSIBILITY) {
            return ContextCompat.getDrawable(mActivity, R.drawable.boost_plus_authorize_tip_top);
        }
        return null;
    }

    public static boolean show(Context context, int extra) {
        return new BoostPlusAutoCleanTip(context, extra).show();
    }
}
