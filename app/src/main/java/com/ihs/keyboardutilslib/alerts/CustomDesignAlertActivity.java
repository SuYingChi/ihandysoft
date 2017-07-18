package com.ihs.keyboardutilslib.alerts;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSActivity;
import com.ihs.keyboardutils.adbuffer.AdLoadingView;
import com.ihs.keyboardutils.alerts.CustomUIRateAlert;
import com.ihs.chargingscreen.activity.ChargingFullScreenAlertDialogActivity;
import com.ihs.keyboardutils.alerts.KCAlert;
import com.ihs.keyboardutilslib.R;

/**
 * Created by jixiang on 16/11/3.
 */

public class CustomDesignAlertActivity extends HSActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_design_alert);
    }

    public void showOneButtonAlert(View view) {
        new KCAlert.Builder(this)
                .setTitle("This is title")
                .setMessage("This is message")
                .setTopImageResource(R.drawable.keyboard_bg)
                .setPositiveButton("Positive button", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(CustomDesignAlertActivity.this, "Positive button clicked", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    public void showTwoButtonAlert(View view) {
        new KCAlert.Builder(this)
                .setTitle("This is title")
                .setMessage("This is message")
                .setTopImageResource(R.drawable.keyboard_bg)
                .setPositiveButton("Positive button", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(CustomDesignAlertActivity.this, "Positive button clicked", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Negative Button", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(CustomDesignAlertActivity.this, "Negative button clicked", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    public void showOneButtonAlertWithoutImage(View view) {
        new KCAlert.Builder(this)
                .setTitle("This is title")
                .setMessage("This is message")
                .setPositiveButton("Positive button", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(CustomDesignAlertActivity.this, "Positive button clicked", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    public void showTwoButtonAlertWithoutImage(View view) {
        new KCAlert.Builder(this)
                .setTitle("This is title")
                .setMessage("This is message")
                .setPositiveButton("Positive button", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(CustomDesignAlertActivity.this, "Positive button clicked", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Negative Button", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(CustomDesignAlertActivity.this, "Negative button clicked", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    public void showCustomUIRateAlert(View view) {
        final CustomUIRateAlert dialog = new CustomUIRateAlert(HSApplication.getContext());

        dialog.setPositiveButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(CustomDesignAlertActivity.this, "onPositiveButtonClick", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.setNegativeButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(CustomDesignAlertActivity.this, "onNegativeButtonClick", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.setNeutralButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(CustomDesignAlertActivity.this, "onNeutralButtonClick", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    public void showFullScreenChargingAlert(View view) {
        Intent intent = new Intent(this,ChargingFullScreenAlertDialogActivity.class);
        intent.putExtra("type","charging");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }

    public void showFullScreenLockerAlert(View view) {
        Intent intent = new Intent(this,ChargingFullScreenAlertDialogActivity.class);
        intent.putExtra("type","locker");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }
    public void showAdloadingView(View view) {
        AdLoadingView adLoadingView = new AdLoadingView(HSApplication.getContext());
        adLoadingView.configParams(null,null,"a","a","a",null,4000,false);
        adLoadingView.showInDialog();
        adLoadingView.startFakeLoading();

    }
}
