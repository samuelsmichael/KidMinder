package com.diamondsoftware.android.kidminder;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
	        setTitle(getString(R.string.app_name)+" - Preferences");
	        // Load the preferences from an XML resource
	        addPreferencesFromResource(R.xml.preferences);
	        
	        ListPreference lp1=(ListPreference)findPreference(GlobalStaticValues.KEY_LOGGINGLEVEL); 
	        lp1.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					new SettingsManager(Preferences.this).setLoggingLevel(Integer.valueOf(newValue.toString()));
					return true;
				}
	        });
	        
	        ListPreference lp2=(ListPreference)findPreference(GlobalStaticValues.KEY_HEARTBEATFREQUENCY); 
	        lp2.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					new SettingsManager(Preferences.this).setHeartbeatFrequency(Integer.valueOf(newValue.toString()));
					Intent intent=GlobalStaticValues.getIntentForTimer(Preferences.this)
						.setAction(GlobalStaticValues.ACTION_HEARTBEAT_INTERVAL_CHANGED);
					startService(intent);
					return true;
				}
	        });
	        
        } catch (Exception eee) {}
    }
}
