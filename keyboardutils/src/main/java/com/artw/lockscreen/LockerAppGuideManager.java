package com.artw.lockscreen;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import com.artw.lockscreen.statusbar.CustomDesignAlert;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.utils.HSMarketUtils;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.alerts.HSAlertDialog;
import com.ihs.keyboardutils.utils.CommonUtils;
import com.kc.commons.utils.KCCommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jixiang on 17/11/17.
 */

public class LockerAppGuideManager {
    private static final LockerAppGuideManager ourInstance = new LockerAppGuideManager();
    private String lockerAppPkgName = "";
    private boolean shouldGuideToLockerApp = false;

    public static LockerAppGuideManager getInstance() {
        return ourInstance;
    }

    private boolean isLockerInstall = false;
    private List<ILockerInstallStatusChangeListener> lockerInstallStatusChangeListeners;


    private LockerAppGuideManager() {
    }

    public void init(String pkgName, boolean shouldGuideToLockerApp) {
        isLockerInstall = CommonUtils.isPackageInstalled(pkgName);
        if (!isLockerInstall) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
            intentFilter.addDataScheme("package");

            PackageInstallReceiver packageInstallReceiver = new PackageInstallReceiver();
            HSApplication.getContext().registerReceiver(packageInstallReceiver, intentFilter);
        }
        lockerAppPkgName = pkgName;
        this.shouldGuideToLockerApp = shouldGuideToLockerApp;
    }

    public void addLockerInstallStatusChangeListener(ILockerInstallStatusChangeListener lockerInstallStatusChangeListener) {
        if (lockerInstallStatusChangeListeners == null) {
            lockerInstallStatusChangeListeners = new ArrayList<>();
        }
        lockerInstallStatusChangeListeners.add(lockerInstallStatusChangeListener);
    }

    public void removeLockerInstallStatusChangeListener(ILockerInstallStatusChangeListener lockerInstallStatusChangeListener) {
        if (lockerInstallStatusChangeListeners != null) {
            lockerInstallStatusChangeListeners.remove(lockerInstallStatusChangeListener);
        }
    }

    private void setLockerInstall() {
        isLockerInstall = true;
        if (lockerInstallStatusChangeListeners == null) {
            for (ILockerInstallStatusChangeListener lockerInstallStatusChangeListener : lockerInstallStatusChangeListeners) {
                lockerInstallStatusChangeListener.onLockerInstallStatusChange();
            }
        }
    }

    public boolean isLockerInstall() {
        return isLockerInstall;
    }

    public boolean isShouldGuideToLockerApp() {
        return shouldGuideToLockerApp;
    }

    public boolean shouldGuideToDownloadLocker() {
        return isLockerInstall && shouldGuideToLockerApp;
    }


    public void downloadOrRedirectToLockerApp() {
        if (isLockerInstall) {
            openApp(lockerAppPkgName);
        } else {
            HSMarketUtils.browseAPP(lockerAppPkgName);
        }
    }

    public void showDownloadLockerAlert(Context context, String msg) {
        CustomDesignAlert lockerDialog = new CustomDesignAlert(HSApplication.getContext());
        lockerDialog.setTitle(context.getString(R.string.locker_alert_title));
        lockerDialog.setMessage(msg);
        lockerDialog.setImageResource(R.drawable.enable_tripple_alert_top_image);//locker image
        lockerDialog.setCancelable(true);

        HSAlertDialog.build(context, R.style.AppCompactDialogStyle).setTitle(context.getString(R.string.locker_guide_unlock_for_free_dialog_title))
                .setPositiveButton(context.getString(R.string.enable), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        HSMarketUtils.browseAPP(lockerAppPkgName);
                        KCCommonUtils.dismissDialog((Dialog) dialogInterface);
                    }
                })
                .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        KCCommonUtils.dismissDialog((Dialog) dialogInterface);
                    }
                }).create().show();
    }

    private static class PackageInstallReceiver extends BroadcastReceiver {
        private PackageInstallReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            final String packageName = intent.getData().getEncodedSchemeSpecificPart();
            if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                if (HSApplication.getContext().getResources().getString(R.string.smart_locker_app_package_name).endsWith(packageName)) {
                    LockerAppGuideManager.getInstance().setLockerInstall();
                }
            }
        }
    }

    public interface ILockerInstallStatusChangeListener {
        void onLockerInstallStatusChange();
    }

    public static boolean openApp(String packageName) {
        PackageManager manager = HSApplication.getContext().getPackageManager();
        try {
            Intent i = manager.getLaunchIntentForPackage(packageName);
            if (i == null) {
                return false;
                //throw new ActivityNotFoundException();
            }
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            HSApplication.getContext().startActivity(i);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }
}
