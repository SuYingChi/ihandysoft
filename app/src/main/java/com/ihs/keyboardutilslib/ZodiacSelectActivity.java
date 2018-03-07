package com.ihs.keyboardutilslib;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.ihs.feature.zodiac.ZodiacUtils;

import net.appcloudbox.service.AcbHoroscopeData;

/**
 * ZodiacSelectActivity.java
 * Created by yanxia on 2018/3/3.
 */

public class ZodiacSelectActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zodiac_select);
        Spinner spinner = findViewById(R.id.spinner);
        String[] mItems = {getString(R.string.zodiac_name_aries), getString(R.string.zodiac_name_taurus),
                getString(R.string.zodiac_name_gemini), getString(R.string.zodiac_name_cancer),
                getString(R.string.zodiac_name_leo), getString(R.string.zodiac_name_virgo),
                getString(R.string.zodiac_name_libra), getString(R.string.zodiac_name_scorpio),
                getString(R.string.zodiac_name_sagittarius), getString(R.string.zodiac_name_capricorn),
                getString(R.string.zodiac_name_aquarius), getString(R.string.zodiac_name_pisces)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setPrompt("Select your zodiac");
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                ZodiacUtils.setZodiacIndex(AcbHoroscopeData.HoroscopeType.valueOf(pos));
                Toast.makeText(ZodiacSelectActivity.this, "你点击的是:" + mItems[pos], Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });
        Button button = findViewById(R.id.clear_zodiac_info_button);
        button.setOnClickListener(v -> ZodiacUtils.removeZodiacIndex());
    }
}
