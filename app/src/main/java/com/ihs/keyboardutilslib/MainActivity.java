package com.ihs.keyboardutilslib;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ihs.keyboardutilslib.PanelContainer.PanelContainerActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startPanelContainer(View view) {
        startActivity(new Intent(this, PanelContainerActivity.class));
    }
}
