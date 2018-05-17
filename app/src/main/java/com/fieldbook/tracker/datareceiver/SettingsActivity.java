package com.fieldbook.tracker.datareceiver;

import android.app.Activity;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.util.Log;

public class SettingsActivity extends Activity {

    final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SettingsFragment setting = new SettingsFragment();

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, setting)
                .commit();


        Bundle extras = getIntent().getExtras();
        String[] values = null;
        if (extras != null) {

            values = extras.getStringArray("devices");
        }

        getFragmentManager().executePendingTransactions();
        ListPreference devices = (ListPreference) setting.findPreference("deviceList");
        if (devices == null) {
            Log.e(TAG, "cannot get device list (ListPreference) instance");
            return;
        }
        devices.setEntries(values);
        devices.setEntryValues(values);

        CheckBoxPreference info = (CheckBoxPreference) setting.findPreference("opened");
        info.setDefaultValue(false);
    }
}
