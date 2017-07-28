package com.ihs.keyboardutilslib.configfile;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.utils.KCFeatureRestrictionConfig;
import com.ihs.keyboardutilslib.R;
import com.kc.commons.configfile.KCMap;
import com.kc.commons.configfile.KCParser;

import org.json.JSONObject;

import java.io.IOException;

public class ReadConfigSampleActivity extends AppCompatActivity {

    private TextView textView;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_config_sample);
        textView = (TextView) findViewById(R.id.output_view);
        handler = new Handler(Looper.getMainLooper());

        checkFeatureRestriction();
    }

    private void checkFeatureRestriction() {
        String[] featureNames = {"TestFeatureA", "TestFeatureB", "TestFeatureC"};
        for (String featureName : featureNames) {
            boolean restricted = KCFeatureRestrictionConfig.isFeatureRestricted(featureName);

            if (restricted) {
                HSLog.d(featureName + " is restricted");
            } else {
                HSLog.d(featureName + " is not restricted");
            }
        }
    }

    public void readKCFile(View view) {
        textView.setText(null);
        handler.post(new Runnable() {
            @Override
            public void run() {
                KCMap map = null;
                try {
                    map = KCParser.parseMap(getAssets().open("test.kc"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                textView.setText(new JSONObject(map).toString());
            }
        });
    }

    public void readJsonFile(View view) throws IOException {
        textView.setText(null);
        handler.post(new Runnable() {
            @Override
            public void run() {
                KCMap map = null;
                try {
                    map = KCParser.parseMap(getAssets().open("test.json"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                textView.setText(new JSONObject(map).toString());
            }
        });
    }

}
