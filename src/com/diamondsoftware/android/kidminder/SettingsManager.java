package com.diamondsoftware.android.kidminder;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SettingsManager {
	private static String KEY_ISENABLED = "IsEnabled";
	private static String KEY_CURRENTSPEED = "CurrentSpeed";
	private SharedPreferences mSharedPreferences;
	
	public SettingsManager(Context context) {
		mSharedPreferences=context.getSharedPreferences(context.getPackageName() + "_preferences", Activity.MODE_PRIVATE);
	}
	private String getValue(String key, String defValue) {
		return mSharedPreferences.getString(key, defValue);
	}
	public boolean getIsEnabled() {
		String value= getValue(KEY_ISENABLED,"true");
		return value.equals("true")?true:false;
	}
	public void setIsEnabled(boolean value) {
		Editor editor=mSharedPreferences.edit();
		editor.putString(KEY_ISENABLED, value?"true":"false");
		editor.commit();
	}
	public double getCurrentSpeed() {
		String value=getValue(KEY_CURRENTSPEED,"0");
		return Double.valueOf(value);
	}
	public void setCurrentSpeed(double value) {
		Editor editor=mSharedPreferences.edit();
		editor.putString(KEY_CURRENTSPEED, String.valueOf(value));
		editor.commit();		
	}
	public int getLoggingLevel() {
		String value=getValue(GlobalStaticValues.KEY_LOGGINGLEVEL,"0");
		return Integer.valueOf(value);
	}
	public void setLoggingLevel(int value) {
		Editor editor=mSharedPreferences.edit();
		editor.putString(GlobalStaticValues.KEY_LOGGINGLEVEL, String.valueOf(value));
		editor.commit();		
	}
	public int getHeartbeatFrequency() {
		String value=getValue(GlobalStaticValues.KEY_HEARTBEATFREQUENCY,"2");
		return Integer.valueOf(value);
	}
	public void setHeartbeatFrequency(int value) {
		Editor editor=mSharedPreferences.edit();
		editor.putString(GlobalStaticValues.KEY_HEARTBEATFREQUENCY, String.valueOf(value));
		editor.commit();		
	}
}
