package com.ihs.inputmethod.utils;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.FileObserver;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.alipay.euler.andfix.patch.PatchManager;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSPreferenceHelper;

import java.io.File;
import java.io.IOException;

/**
 * Created by yingchi.su on 2018/1/6.
 */

public class KCHotFixManager implements INotificationObserver {

    public static final String TAG = "KCHotFixManager";
    public static final String HotfixDefault = "HotfixDefault";
    private String HotfixpathURLKey = "HotfixpathURLKey";
    private static KCHotFixManager mKcHotFixManager;
    private PatchManager mPatchManager;
    private String mPatchFolderName = "";
    private Application mApplication;
    private String patchPathDir = "";
    private String hotFixURL = "";
    private String hotFixpathURLLocal = "";
    private FileObserver mFileObserver;
    private NetBroadcastReceiver receiver;
    //is addpatch success or not need to addpatch
    private boolean result = true;

    private KCHotFixManager(String patchFolder) {
        mPatchFolderName = patchFolder;
        mApplication = HSApplication.instance;
        mPatchManager = new PatchManager(mApplication);
        receiver = new NetBroadcastReceiver();
        mFileObserver = new FileObserver(creatPatchDirectory()) {

            @Override
            public void onEvent(int event, @Nullable String path) {
                final int action = event & FileObserver.ALL_EVENTS;
                switch (action) {
                    case FileObserver.ACCESS:
                        Log.d(TAG, " FileObserver.ACCESS ");
                        break;

                    case FileObserver.DELETE:
                        Log.d(TAG, " FileObserver.DELETE ");
                        break;

                    case FileObserver.OPEN:
                        Log.d(TAG, " FileObserver.OPEN ");
                        break;

                    case FileObserver.MODIFY:
                        Log.d(TAG, " FileObserver.MODIFY ");
                        if (!checkNetWorkState())
                            init_NoNetWork();
                        break;
                }
            }
        };
        mFileObserver.startWatching();
        Log.d(TAG, " addObserver  of internal patchdir");
        mApplication.registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        Log.d(TAG, " addObserver  of newworkstate");
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_CONFIG_CHANGED, this);
        Log.d(TAG, " addObserver  of HS_CONFIG_CHANGED");
    }

    public static synchronized KCHotFixManager getInstance(String patchFolder) {
        if (mKcHotFixManager == null) {
            mKcHotFixManager = new KCHotFixManager(patchFolder);
        }
        return mKcHotFixManager;
    }

    private boolean init() {
        Log.d(TAG, " init and loadPatch ");
        patchPathDir = creatPatchDirectory();
        hotFixURL = HSConfig.optString(HotfixDefault, HotfixpathURLKey);
        hotFixpathURLLocal = HSPreferenceHelper.getDefault().getString(HotfixpathURLKey, HotfixDefault);
        if ((!TextUtils.isEmpty(patchPathDir) && TextUtils.isEmpty(hotFixpathURLLocal) & !hotFixURL.equals(HotfixDefault)) || (!TextUtils.isEmpty(patchPathDir) && !hotFixURL.equals(HotfixDefault) & !hotFixURL.equals(hotFixpathURLLocal))) {
            Log.d(TAG, " start to downLoad patch ,hotFixpathURLLocal ====== " + hotFixpathURLLocal + "hotFixURL ======" + hotFixURL);
            HSHttpConnection mHSHttpConnection = new HSHttpConnection(hotFixURL);
            mHSHttpConnection.setConnectTimeout(6000);
            File patchFile = new File(patchPathDir + File.separator + hotFixURL);
            if (!patchFile.exists()) {
                try {
                    patchFile.createNewFile();
                    mHSHttpConnection.setDownloadFile(patchFile);
                    mHSHttpConnection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
                        @Override
                        public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                            Log.d(TAG, " downloadPatch success");
                            result = operatePatch(patchFile);
                        }

                        @Override
                        public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                            Log.d(TAG, " fail to download patchFile ");
                            result = false;
                        }
                    });
                    mHSHttpConnection.startAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, " patch init exception");
                    result = false;
                }
            } else {
                //there is same patch in local,not need to download again,addpatch direct
                result = init_NoNetWork();
            }

        } else if (init_NoNetWork()){
            Log.d(TAG, " not need to update patch in  net,hotFixpathURLLocal =======" + hotFixpathURLLocal + "               hotFixURL  =======" + hotFixURL );
            result = true;
        }else result = false;

        Log.d(TAG, "init()    excute        finish ---------------------");
        return result;

    }

    private boolean init_NoNetWork() {
        Log.d(TAG, " init_NoNetWork and loadPatch ");
        String patchName = getPatchNamefromCache();
        String patchFileString = "";
        hotFixpathURLLocal = HSPreferenceHelper.getDefault().getString(HotfixpathURLKey, HotfixDefault);
        if ((TextUtils.isEmpty(hotFixpathURLLocal) & !TextUtils.isEmpty(patchName)) || (!TextUtils.isEmpty(patchName) && !patchName.equals(hotFixpathURLLocal))) {
            try {
                patchFileString = creatPatchDirectory() + File.separator + patchName;
                if (new File(patchFileString).exists()) {
                    mPatchManager.removeAllPatch();
                    Log.d(TAG, " remove  allPatch ");
                    mPatchManager.addPatch(patchFileString);
                    Log.d(TAG, "add patch success");
                    HSPreferenceHelper.getDefault().putString(HotfixpathURLKey, patchName);
                    Log.d(TAG, "update patch tag ==========" + patchName);
                    boolean deletedResult = new File(patchFileString).delete();
                    if (deletedResult) {
                        Log.d(TAG, "deleted patchFile after addpatch--- success");
                    } else
                        Log.d(TAG, "deleted patchFile after addpatch---  fail");

                     result = true;
                } else {
                    Log.d(TAG, " patch isn't exist");
                     result = false;
                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, " patch init_NoNetWork exception");
                 result = false;

            }
        } else {
            Log.d(TAG, " not need to update apatch in notNetWork,      hotFixpathURLLocal =======" + hotFixpathURLLocal + "            patchName =======" + patchName);
            result = true;
        }

        Log.d(TAG, "init_NoNetWork  --------------------   excute finish-------------");
        return result;
    }

    private String getPatchNamefromCache() {
        File f = new File(creatPatchDirectory());
        if (f.exists()) {
            File[] ff = f.listFiles();
            for (int i = 0; i < ff.length; i++) {
                if (ff[i].isFile() && ff[i].getAbsolutePath().endsWith(".apatch") && ff[i].exists()) {
                    Log.d(TAG, " patchName is     " + ff[i].getName());
                    return ff[i].getName();
                }
            }
        }
        return "";
    }

    private String creatPatchDirectory() {
        File appCacheDir = new File(mApplication.getFilesDir(), mPatchFolderName);// /data/data/app_package_name/files/patchFolder

        if (!appCacheDir.exists()) {
            appCacheDir.mkdirs();
            Log.e(TAG, "creat apatchDir      apatchDir     ===== "+appCacheDir.getAbsolutePath());
        }
        Log.e(TAG, "      apatchDir     ===== "+appCacheDir.getAbsolutePath());
        return appCacheDir.getAbsolutePath();
    }

    //observer if remoteconfig change , to update patch
    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        Log.d(TAG, "onReceive s == " + s);
        if (TextUtils.equals(s, HSNotificationConstant.HS_CONFIG_CHANGED)) {
            openHotFix();

        }
    }

    private String getVersionName() {
        PackageManager packageManager = mApplication.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(mApplication.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo != null) {
            return packageInfo.versionName;
        } else
            return "";
    }


    private boolean operatePatch(File patchFile) {
        try {
            mPatchManager.removeAllPatch();
            Log.d(TAG, " remove  allPatch ");
            mPatchManager.addPatch(patchFile.getAbsolutePath());
            Log.d(TAG, "add patch success");
            //update patch tag in preference
            HSPreferenceHelper.getDefault().putString(HotfixpathURLKey, hotFixURL);
            Log.d(TAG, "update patch tag     =========== " + hotFixURL);
            if (patchFile.exists()) {
                boolean deletedResult = patchFile.delete();
                if (deletedResult) {
                    Log.d(TAG, "deleted patchFile after addpatch--- success");
                } else
                    Log.d(TAG, "deleted patchFile after addpatch---  fail");
            }
            result = true;
        } catch (IOException e) {
            Log.d(TAG, "operatePatch patch exception");
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public void unRegisterPatchChangeObserver() {

        HSGlobalNotificationCenter.removeObserver(this);

        Log.d(TAG, " remove Observer  of HS_CONFIG_CHANGED");

        mFileObserver.stopWatching();

        Log.d(TAG, " remove Observer  of  patchFile change");

        mApplication.unregisterReceiver(receiver);

        Log.d(TAG, " remove receiver of netState");
    }

    private boolean checkNetWorkState() {
        ConnectivityManager con = (ConnectivityManager) mApplication.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (con.getActiveNetworkInfo().isConnected()) {
            return con.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;
        }
        return false;
    }

    //offer public interface about start hotFix  to third app
    public void openHotFix() {
        mPatchManager.init(getVersionName());
        mPatchManager.loadPatch();
        if (checkNetWorkState() & !init())
            init_NoNetWork();
    }


    public class NetBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                Log.d(TAG, " onReceive  networkstatechange");
                openHotFix();

            }
        }

    }


}
