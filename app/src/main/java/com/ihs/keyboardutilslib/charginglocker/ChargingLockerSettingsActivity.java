package com.ihs.keyboardutilslib.charginglocker;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import com.artw.lockscreen.LockerSettings;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.chargingscreen.utils.ChargingPrefsUtil;
import com.ihs.keyboardutilslib.R;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class ChargingLockerSettingsActivity extends AppCompatPreferenceActivity {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */


    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            if (ChargingPrefsUtil.isChargingMuted()) {
                getPreferenceScreen().removePreference(findPreference(getString(R.string.pref_header_key)));
            } else {
                SwitchPreference chargingSwitcher = (SwitchPreference) findPreference(getString(R.string.pref_header_key));
                int chargingEnableStates = ChargingPrefsUtil.getInstance().getChargingEnableStates();
                switch (chargingEnableStates) {
                    case ChargingPrefsUtil.CHARGING_DEFAULT_DISABLED:
                        chargingSwitcher.setChecked(false);
                        break;
                    case ChargingPrefsUtil.CHARGING_DEFAULT_ACTIVE:
                        chargingSwitcher.setChecked(true);
                        break;
                }

                chargingSwitcher.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if ((Boolean) newValue) {
                            ChargingManagerUtil.enableCharging(false);
                        } else {
                            ChargingManagerUtil.disableCharging();
                        }
                        return true;
                    }
                });
            }
            if (LockerSettings.isLockerMuted()) {
                getPreferenceScreen().removePreference(findPreference(getString(R.string.pref_locker_key)));
            } else {
                SwitchPreference lockerSwitcher = (SwitchPreference) findPreference(getString(R.string.pref_locker_key));
                int lockerEnableStates = LockerSettings.getLockerEnableStates();
                switch (lockerEnableStates) {
                    case LockerSettings.LOCKER_DEFAULT_DISABLED:
                        lockerSwitcher.setChecked(false);
                        break;
                    case LockerSettings.LOCKER_DEFAULT_ACTIVE:
                        lockerSwitcher.setChecked(true);
                        break;
                }

                lockerSwitcher.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        LockerSettings.setLockerEnabled((Boolean) newValue);
                        return true;
                    }
                });
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), ChargingLockerSettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
