package com.ihs.keyboardutils.alerts;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.artw.lockscreen.PremiumLockerActivity;
import com.artw.lockscreen.lockerappguide.LockerAppGuideManager;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.keyboardutils.R;

public class LockerUpgradeAlert extends AlertDialog {

    public LockerUpgradeAlert(@NonNull Context context) {
        super(context, R.style.AlertDialogTheme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locker_upgrade_alert);
        FrameLayout rootView = findViewById(R.id.root_view);

        assert rootView != null;
        Button upgradeButton = rootView.findViewById(R.id.upgrade_button);
        upgradeButton.setOnClickListener(view -> {
            LockerAppGuideManager.directToMarket(null, null, LockerAppGuideManager.getInstance().getLockerAppPkgName());
            HSGlobalNotificationCenter.sendNotification(PremiumLockerActivity.EVENT_FINISH_SELF);
        });

        setCancelable(false);

        ImageView closeIcon = rootView.findViewById(R.id.close_icon);
        closeIcon.setOnClickListener(view -> dismiss());
    }
}
