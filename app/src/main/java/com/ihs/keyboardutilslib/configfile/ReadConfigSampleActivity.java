package com.ihs.keyboardutilslib.configfile;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ihs.keyboardutils.configfile.KCMap;
import com.ihs.keyboardutils.configfile.KCParser;
import com.ihs.keyboardutilslib.R;

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
