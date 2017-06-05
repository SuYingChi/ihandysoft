package com.ihs.permission;

/**
 * Created by liuzhongtao on 17/6/5.
 *
 */

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.provider.Settings;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.util.List;

public class PermissionUtils {

    private PermissionUtils() {
    }

    public static boolean isUsageAccessGranted() {
        boolean granted = false;
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            try {
                PackageManager packageManager = HSApplication.getContext().getPackageManager();
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                granted = list.size() == 0;
                if (!granted) {
                    AppOpsManager appOps = (AppOpsManager) HSApplication.getContext().getSystemService(Context.APP_OPS_SERVICE);
                    int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), HSApplication.getContext().getPackageName());
                    granted = mode == AppOpsManager.MODE_ALLOWED;
                }
            } catch (Exception e) {
                HSLog.d("exception:" + e.getMessage());
            }
        } else {
            granted = true;
        }
        return granted;
    }

    public static void enableUsageAccessPermission() {
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && !PermissionUtils.isUsageAccessGranted()) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
            HSApplication.getContext().startActivity(intent);
            FloatWindowManager.getInstance().createPermissionTip(PermissionTip.TYPE_TEXT_USAGE);
        }
    }
}
