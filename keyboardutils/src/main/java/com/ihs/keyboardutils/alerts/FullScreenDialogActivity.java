package com.ihs.keyboardutils.alerts;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.KCAnalyticUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arthur on 17/5/8.
 */

public class FullScreenDialogActivity extends Activity {

    public static final String NOTIFICATION_LOCKER_ENABLED = "notification_locker_enabled";

    private String alertType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            alertType= intent.getStringExtra("type");
            switch (alertType) {
                case "charging":
                    initChargingDialog();
                    break;
                case "locker":
                    initLockerDialog();
                    break;
            }
        }
    }

    private void initLockerDialog() {

        Map<String, String> chargingMap = getAlertConfigMap(alertType);
        new KCAlert.Builder(this)
                .setTitle(chargingMap.get("Title"))
                .setMessage(chargingMap.get("Body"))
                .setTopImageResource(R.drawable.top_pic_enable_locker)
                .setPositiveButton(chargingMap.get("Button"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_LOCKER_ENABLED);
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                })
                .setFullScreen(true)
                .setAdText(chargingMap.get("AdText"))
                .show();
    }

    private void initChargingDialog() {
        Map<String, String> chargingMap = getAlertConfigMap(alertType);
        new KCAlert.Builder(this)
                .setTitle(chargingMap.get("Title"))
                .setMessage(chargingMap.get("Body"))
                .setTopImageResource(R.drawable.top_pic_enable_charging)
                .setPositiveButton(chargingMap.get("Button"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ChargingManagerUtil.enableCharging(false,"alert");
                        Toast.makeText(HSApplication.getContext(), HSApplication.getContext().getString(R.string.charging_enable_alert_toast), Toast.LENGTH_SHORT).show();
                        KCAnalyticUtil.logEvent("alert_charging_click");
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                })
                .setFullScreen(true)
                .setAdText(chargingMap.get("AdText"))
                .show();
    }

    public static Map<String, String> getAlertConfigMap(String keyLowercase){
        Map<String,String> alertMap = new HashMap();

        switch (keyLowercase) {
            case "charging":
                alertMap = (Map<String, String>) HSConfig.getMap("Application","ChargeLocker","Alert");
                break;

            case "locker":
                alertMap = (Map<String, String>) HSConfig.getMap("Application","Locker","Alert");
                break;
        }
        return alertMap;
    }
}
