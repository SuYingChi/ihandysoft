package com.ihs.keyboardutilslib.adactivities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.ihs.app.framework.activity.HSActivity;
import com.ihs.commons.config.HSConfig;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;
import com.ihs.keyboardutilslib.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NativeAdDemoActivity extends HSActivity {

    private LinearLayout adContainer;
    private Spinner adPlacementSpinner;
    private Button showAdButton;
    private Button closeAdButton;

    private List<String> adPlacementList = new ArrayList<>();

    private Map<String, NativeAdView> adViewMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ad_main_activity);

        adContainer = (LinearLayout) findViewById(R.id.ad_container);
        adPlacementSpinner = (Spinner) findViewById(R.id.spinner_placement);
        showAdButton = (Button) findViewById(R.id.btn_show_ad);
        closeAdButton = (Button) findViewById(R.id.btn_close_ad);


        for (String adPlacement : getAdPlacementList()) {
            adPlacementList.add(adPlacement);
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, adPlacementList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adPlacementSpinner.setAdapter(arrayAdapter);
        adPlacementSpinner.setSelection(0);
        adPlacementSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateButtonStates();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        updateButtonStates();
    }

    private void updateButtonStates() {
        String placement = (String) adPlacementSpinner.getSelectedItem();
        if (adViewMap.containsKey(placement)) {
            showAdButton.setEnabled(false);
            closeAdButton.setEnabled(true);
        } else {
            showAdButton.setEnabled(true);
            closeAdButton.setEnabled(false);
        }
    }

    public void closeAd(View view) {
        String placement = (String) adPlacementSpinner.getSelectedItem();
        NativeAdView nativeAdView = adViewMap.get(placement);
        if (nativeAdView != null) {
            adContainer.removeView(nativeAdView);
            adViewMap.remove(placement);
            updateButtonStates();
            Toast.makeText(NativeAdDemoActivity.this, placement + " is released.", Toast.LENGTH_SHORT).show();
        }
    }

    public void showAd(View view) {
        String placement = (String) adPlacementSpinner.getSelectedItem();
        View adLayoutView = LayoutInflater.from(this).inflate(R.layout.ad_style_1, null);
        View adLoadingView = LayoutInflater.from(this).inflate(R.layout.ad_loading, null);
        NativeAdView nativeAdView = new NativeAdView(this, adLayoutView, adLoadingView);
        nativeAdView.configParams(new NativeAdParams(placement, adContainer.getWidth(), 1.9f));
        nativeAdView.setOnAdLoadedListener(adLoadedListener);
        nativeAdView.setOnAdClickedListener(adClickedListener);
        nativeAdView.setTag(placement);
        adContainer.addView(nativeAdView);
        adViewMap.put(placement, nativeAdView);
        Toast.makeText(NativeAdDemoActivity.this, placement + " is loading.", Toast.LENGTH_SHORT).show();

        updateButtonStates();
    }

    private NativeAdView.OnAdLoadedListener adLoadedListener = new NativeAdView.OnAdLoadedListener() {

        @Override
        public void onAdLoaded(NativeAdView adView) {
            String placement = (String) adView.getTag();
            Toast.makeText(NativeAdDemoActivity.this, placement + " is loaded.", Toast.LENGTH_SHORT).show();
        }
    };

    private NativeAdView.OnAdClickedListener adClickedListener = new NativeAdView.OnAdClickedListener() {

        @Override
        public void onAdClicked(NativeAdView adView) {
            String placement = (String) adView.getTag();
            Toast.makeText(NativeAdDemoActivity.this, placement + " is clicked.", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        for (Map.Entry<String, NativeAdView> adViewEntry : adViewMap.entrySet()) {
            NativeAdView nativeAdView = adViewEntry.getValue();
            nativeAdView.release();
        }
        super.onDestroy();
    }

    private static List<String> getAdPlacementList() {
        List<String> poolNames = new ArrayList<>();
        for (Map.Entry entry : HSConfig.getMap("nativeAds").entrySet()) {
            if ((entry.getValue() instanceof Map)) {
                poolNames.add(entry.getKey().toString());
            }
        }
        return poolNames;
    }
}
