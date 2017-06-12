package com.ihs.feature.common;

import android.text.TextUtils;

import com.ihs.commons.config.HSConfig;

import java.util.List;
import java.util.Locale;

/**
 * Created by Arthur on 17/6/1.
 */

public class WeatherSettings {
    public static final String PREF_KEY_DISPLAY_FAHRENHEIT = "display.fahrenheit";

    public static boolean shouldDisplayFahrenheit() {
        PreferenceHelper prefs = PreferenceHelper.get(LauncherFiles.WEATHER_PREFS);
        if (!prefs.contains(PREF_KEY_DISPLAY_FAHRENHEIT)) {
            String currentCountry = Locale.getDefault().getCountry().toUpperCase();
            @SuppressWarnings("unchecked") List<String> fahrenheitCountries =
                    (List<String>) HSConfig.getList("Application", "Units", "FahrenheitDisplayCountries");
            boolean fahrenheit = false;
            for (String fahrenheitCountry : fahrenheitCountries) {
                if (TextUtils.equals(currentCountry, fahrenheitCountry)) {
                    fahrenheit = true;
                    prefs.putBoolean(PREF_KEY_DISPLAY_FAHRENHEIT, true);
                    break;
                }
            }
            if (!fahrenheit) {
                prefs.putBoolean(PREF_KEY_DISPLAY_FAHRENHEIT, false);
            }
        }
        return prefs.getBoolean(PREF_KEY_DISPLAY_FAHRENHEIT, false);
    }
}
