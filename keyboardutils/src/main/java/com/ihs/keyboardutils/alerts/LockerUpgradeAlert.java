package com.ihs.keyboardutils.alerts;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.artw.lockscreen.lockerappguide.LockerAppGuideManager;
import com.ihs.chargingscreen.utils.LockerChargingSpecialConfig;
import com.ihs.keyboardutils.R;

/**
 * Created by yang.liu on 2017/12/11.
 */

public class LockerUpgradeAlert extends AlertDialog implements View.OnClickListener {

    public LockerUpgradeAlert(@NonNull Context context) {
        super(context, R.style.AlertDialogTheme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locker_upgrade_alert);
        FrameLayout rootView = findViewById(R.id.root_view);

        Button upgradeButton = rootView.findViewById(R.id.upgrade_button);
        upgradeButton.setOnClickListener(view -> LockerAppGuideManager.directToMarket(null, null, LockerChargingSpecialConfig.BOUND_SERVICE_PACKAGE));

        setCancelable(false);

        ImageView closeIcon = rootView.findViewById(R.id.close_icon);
        closeIcon.setOnClickListener(view -> dismiss());
    }

    @Override
    public void onClick(View view) {

    }
}
