package com.ihs.keyboardutilslib;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ihs.feature.battery.BatteryActivity;
import com.ihs.feature.boost.plus.BoostPlusActivity;
import com.ihs.feature.cpucooler.CpuCoolDownActivity;
import com.ihs.feature.junkclean.JunkCleanActivity;

public class DemoShowBoostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_show_boost);
    }

    public void startBoost(View view) {
        Intent intent = new Intent(this, BoostPlusActivity.class);
        startActivity(intent);
    }

    public void startClean(View view) {
        Intent intent = new Intent(this, JunkCleanActivity.class);
        startActivity(intent);
    }

    public void startCpu(View view) {
        Intent intent = new Intent(this, CpuCoolDownActivity.class);
        startActivity(intent);
    }

    public void startBattery(View view) {
        Intent intent = new Intent(this, BatteryActivity.class);
        startActivity(intent);
    }
}
