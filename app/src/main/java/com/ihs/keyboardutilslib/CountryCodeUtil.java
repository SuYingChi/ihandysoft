package com.ihs.keyboardutilslib;

import android.app.Activity;
import android.os.Bundle;

import com.ihs.commons.utils.HSLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 * Created by Arthur on 17/6/8.
 */

public class CountryCodeUtil extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("country.txt")));

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                //process line
                String[] split = mLine.split("_");
                Locale loc;
                if (split.length > 1) {
                    loc = new Locale(split[0], split[1]);
                } else {
                    loc = new Locale(split[0]);
                }
//                loc = new Locale(split[0]);
                HSLog.e(loc.getDisplayLanguage() + "(" + loc.getDisplayCountry() + ")");
            }
        } catch (IOException e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }
    }

}
