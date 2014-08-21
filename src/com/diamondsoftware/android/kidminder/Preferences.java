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
	        
	        ListPreference lp1=(ListPreference)findPreference("LoggingLevel"); 
	        lp1.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					new SettingsManager(Preferences.this).setLoggingLevel(Integer.valueOf(newValue.toString()));
					return true;
				}
	        });
	        
        } catch (Exception eee) {}
    }
}
