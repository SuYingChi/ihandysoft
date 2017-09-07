package com.launcher;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;

import com.artw.lockscreen.common.NavUtils;
import com.ihs.app.framework.HSApplication;

/**
 * Created by yanxia on 2017/9/5.
 */

public class PermissionImpl {

    private Context mContext;

    public PermissionImpl(Context context) {
        mContext = context;
    }

    public static boolean canDrawOverlays() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(HSApplication.getContext());
        }
        return true;
    }

    public void requestDrawOverlays(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + activity.getPackageName()));
            NavUtils.startActivityForResultSafely(activity, intent, requestCode);
        }
    }

    public int checkPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.checkSelfPermission(permission);
        }
        return PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermissions(Context context, @NonNull String[] permissions,
                                   int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context instanceof Activity) {
                ((Activity) context).requestPermissions(
                        permissions, requestCode);
            }
        }
    }

    public boolean canReadPhoneState() {
        return checkPermission(
                Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean canReadContact() {
        return checkPermission(
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

}
