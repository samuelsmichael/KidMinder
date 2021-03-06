package com.diamondsoftware.android.kidminder;

import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SettingsManager {
	private SharedPreferences mSharedPreferences;
	private Context mContext;
	
	public SettingsManager(Context context) {
		mSharedPreferences=context.getSharedPreferences(context.getPackageName() + "_preferences", Activity.MODE_PRIVATE);
		mContext=context;
	}
	private String getValue(String key, String defValue) {
		return mSharedPreferences.getString(key, defValue);
	}
	private void setValue(String key, String value) {
		Editor editor=mSharedPreferences.edit();
		editor.putString(key,value);
		editor.commit();				
	}
	public boolean getIsEnabled() {
		String value= getValue(GlobalStaticValues.KEY_ISENABLED,"true");
		return value.equals("true")?true:false;
	}
	public void setIsEnabled(boolean value) {
		Editor editor=mSharedPreferences.edit();
		editor.putString(GlobalStaticValues.KEY_ISENABLED, value?"true":"false");
		editor.commit();
	}
	public double getCurrentSpeed() {
		String value=getValue(GlobalStaticValues.KEY_CURRENTSPEED,"0");
		return Double.valueOf(value);
	}
	public void setCurrentSpeed(double value) {
		setValue(GlobalStaticValues.KEY_CURRENTSPEED, String.valueOf(value));
	}
	public int getLoggingLevel() {
		String value=getValue(GlobalStaticValues.KEY_LOGGINGLEVEL,String.valueOf(GlobalStaticValues.LOG_LEVEL_CRITICAL));
		return Integer.valueOf(value);
	}
	public void setLoggingLevel(int value) {
		setValue(GlobalStaticValues.KEY_LOGGINGLEVEL, String.valueOf(value));
	}
	public String getSoundType() {
		return getValue("soundtype",mContext.getString(R.string.soundtypedefault));
	}
	public int getHeartbeatFrequency() {
		String value=getValue(GlobalStaticValues.KEY_HEARTBEATFREQUENCY,mContext.getString(R.string.default_heartbeatfrequency_value));
		return Integer.valueOf(value);
	}
	public void setHeartbeatFrequency(int value) {
		setValue(GlobalStaticValues.KEY_HEARTBEATFREQUENCY, String.valueOf(value));
	}
	public int getHeartbeatTicksCount() {
		String value=getValue(GlobalStaticValues.KEY_HEARTBEAT_TICKS_COUNT,"0");
		return Integer.valueOf(value);
	}
	public void setHeartbeatTicksCount(int value) {
		setValue(GlobalStaticValues.KEY_HEARTBEAT_TICKS_COUNT, String.valueOf(value));
	}
	public void incrementHeartbeatTicksCount() {
		int count=getHeartbeatTicksCount();
		count++;
		setHeartbeatTicksCount(count);
	}
	public int getJeDisSimulationCount() {
		String value=getValue("JeDisSimulation","0");
		return Integer.valueOf(value);
	}
	public void setJeDisSimulationCount(int value) {
		setValue("JeDisSimulation", String.valueOf(value));
	}
	public void incrementJeDisSimulationCount() {
		int count=getJeDisSimulationCount();
		count++;
		setJeDisSimulationCount(count);
	}

	public int getGotspeedTicksCount() {
		String value=getValue(GlobalStaticValues.KEY_GOTSPEED_COUNT,"0");
		return Integer.valueOf(value);
	}
	public void setGotSpeedTicksCount(int value) {
		setValue(GlobalStaticValues.KEY_GOTSPEED_COUNT, String.valueOf(value));
	}
	public void incrementGotSpeedCount() {
		int count=getGotspeedTicksCount();
		count++;
		setGotSpeedTicksCount(count);
	}
	public int getWasMovingCount() {
		String value=getValue(GlobalStaticValues.KEY_WASMOVING,"0");
		return Integer.valueOf(value);
	}
	public void setWasMovingCount(int value) {
		setValue(GlobalStaticValues.KEY_WASMOVING, String.valueOf(value));
	}
	public void incrementWasMovingCount() {
		int count=getWasMovingCount();
		count++;
		setWasMovingCount(count);
	}
	public int getWasStoppedCount() {
		String value=getValue(GlobalStaticValues.KEY_WASSTOPPED,"0");
		return Integer.valueOf(value);
	}
	public void setWasStoppedCount(int value) {
		setValue(GlobalStaticValues.KEY_WASSTOPPED, String.valueOf(value));
	}
	public void incrementWasStoppedCount() {
		int count=getWasStoppedCount();
		count++;
		setWasStoppedCount(count);
	}
	public LatLng getPriorLocation() {
		return 
				new LatLng(
						Double.valueOf(getValue(GlobalStaticValues.KEY_PRIORLOCATION_LATITUDE,"0")),
						Double.valueOf(getValue(GlobalStaticValues.KEY_PRIORLOCATION_LONGITUDE,"0"))
					);
	}
	public void setPriorLocation(double latitude, double longitude) {
		setValue(
				GlobalStaticValues.KEY_PRIORLOCATION_LATITUDE,String.valueOf(latitude)
			);
		setValue(
				GlobalStaticValues.KEY_PRIORLOCATION_LONGITUDE,String.valueOf(longitude)
			);
	}
	public void setPriorLocationDate(Date value) {
		if(value==null) {
			value=new Date();
		}
		GregorianCalendar gc=new GregorianCalendar(Locale.getDefault());
		gc.setTime(value);
		setValue(GlobalStaticValues.KEY_PRIORLOCATION_DATESTAMP,GlobalStaticValues.MDATEFORMAT.format(gc.getTime()));
	}
	public Date getPriorLocationDate() {
		String dateString=getValue(GlobalStaticValues.KEY_PRIORLOCATION_DATESTAMP,null);
		if(dateString==null) {
			return null;
		} else {
			GregorianCalendar gc=new GregorianCalendar();
			try {
				gc.setTime(GlobalStaticValues.MDATEFORMAT.parse(dateString));
			} catch (ParseException e) {
				gc.setTime(new Date());
			}
			return gc.getTime();
		}
	}
	public void setLatestLocationDate(Date value) {
		GregorianCalendar gc=new GregorianCalendar(Locale.getDefault());
		gc.setTime(value);
		setValue(GlobalStaticValues.KEY_LATESTLOCATION_DATESTAMP,GlobalStaticValues.MDATEFORMAT.format(gc.getTime()));
	}
	public Date getLatestLocationDate() {
		String dateString=getValue(GlobalStaticValues.KEY_LATESTLOCATION_DATESTAMP,null);
		if(dateString==null) {
			return null;
		} else {
			GregorianCalendar gc=new GregorianCalendar();
			try {
				gc.setTime(GlobalStaticValues.MDATEFORMAT.parse(dateString));
			} catch (ParseException e) {
				gc.setTime(new Date());
			}
			return gc.getTime();
		}
	}
	public LatLng getLatestLocation() {
		return 
				new LatLng(
						Double.valueOf(getValue(GlobalStaticValues.KEY_LATESTLOCATION_LATITUDE,"0")),
						Double.valueOf(getValue(GlobalStaticValues.KEY_LATESTLOCATION_LONGITUDE,"0"))
					);
	}
	public void setLatestLocation(double latitude, double longitude) {
		setValue(
				GlobalStaticValues.KEY_LATESTLOCATION_LATITUDE,String.valueOf(latitude)
			);
		setValue(
				GlobalStaticValues.KEY_LATESTLOCATION_LONGITUDE,String.valueOf(longitude)
			);
	}
	public int getIsDrivingThreshhold() {
		String value=getValue(GlobalStaticValues.KEY_IS_DRIVING_THRESHHOLD,"5");
		int valueAsInt=GlobalStaticValues.DEFAULT_MPH_DRIVINGTHRESHHOLD; // default ... in case the entered garbage
		try {
			valueAsInt=Integer.valueOf(value);
		} catch (Exception e) {
			valueAsInt=GlobalStaticValues.DEFAULT_MPH_DRIVINGTHRESHHOLD;
			setIsDrivingThreshhold(valueAsInt);
		}
		return valueAsInt;
	}
	public void setIsDrivingThreshhold(int value) {
		setValue(GlobalStaticValues.KEY_IS_DRIVING_THRESHHOLD,"5");
	}
	public void setStoppedTimeMinutesBeforeNotification(int value) {
		setValue(GlobalStaticValues.KEY_STOPPEDTIME_MINUTES_BEFORE_NOTIFICATION,String.valueOf(value));
	}
	public int getStoppedTimeMinutesBeforeNotification() {
		String value=getValue(GlobalStaticValues.KEY_STOPPEDTIME_MINUTES_BEFORE_NOTIFICATION,mContext.getString(R.string.defaultstoptimeinminutes));
		int valueAsInt=GlobalStaticValues.DEFAULT_MINUTES_STOPPED; // default ... in case the entered garbage
		try {
			valueAsInt=Integer.valueOf(value);
		} catch (Exception e) {
			valueAsInt=GlobalStaticValues.DEFAULT_MINUTES_STOPPED;
			setStoppedTimeMinutesBeforeNotification(valueAsInt);
		}
		return valueAsInt;
	}
	public boolean getNotificationUsesSound() {
		boolean usesSound=mSharedPreferences.getBoolean(GlobalStaticValues.KEY_NOTIFICATION_USES_SOUND, true);
		return usesSound;
	}
	public boolean getNotificationUsesVibrate() {
		boolean usesSound=mSharedPreferences.getBoolean(GlobalStaticValues.KEY_NOTIFICATION_USES_VIBRATE, true);
		return usesSound;
	}
	public boolean getNotificationUsesPopup() {
		boolean usesSound=mSharedPreferences.getBoolean(GlobalStaticValues.KEY_NOTIFICATION_USES_POPUP, false);
		return usesSound;
	}
	public void setNotificationUsesPopup(boolean value) {
		Editor editor=mSharedPreferences.edit();
		editor.putBoolean(GlobalStaticValues.KEY_NOTIFICATION_USES_POPUP,value);
		editor.commit();				
	}
	public long getCurrentRestTime() {
		String value= getValue(GlobalStaticValues.KEY_CURRENT_REST_TIME,"0");
		return Long.valueOf(value);
	}
	public void setCurrentRestTime(long value) {
		setValue(GlobalStaticValues.KEY_CURRENT_REST_TIME,String.valueOf(value));
	}
	public boolean getCurrentSimilationStatus() {
		boolean simulate=mSharedPreferences.getBoolean(GlobalStaticValues.KEY_SIMULATION, false);
		return simulate;
	}
	public boolean getEnabledStateBeforeGPSWasTurnedOff() {
		boolean enabled=mSharedPreferences.getBoolean("EnabledStateBeforeGPSWasTurnedOff", false);
		return enabled;
	}
	public void setEnabledStateBeforeGPSWasTurnedOff(boolean value) {
		Editor editor=mSharedPreferences.edit();
		editor.putBoolean("EnabledStateBeforeGPSWasTurnedOff",value);
		editor.commit();				
	}
	public boolean getImOnTop() {
		boolean enabled=mSharedPreferences.getBoolean("ImOnTop", false);
		return enabled;
	}
	public void setImOnTop(boolean value) {
		Editor editor=mSharedPreferences.edit();
		editor.putBoolean("ImOnTop",value);
		editor.commit();				
	}
	public String getActivityRecognition() {
		return getValue(GlobalStaticValues.KEY_ACTIVITYRECOGNITION,"");
	}
	public void setActivityRecognition(String value) {
		setValue(GlobalStaticValues.KEY_ACTIVITYRECOGNITION, value);
	}
	public int getConfidence() {
		String value=getValue(GlobalStaticValues.KEY_ACTIVITYRECOGNITION_CONFIDENCE,"0");
		return Integer.valueOf(value);
	}
	public void setConfidence(int value) {
		setValue(GlobalStaticValues.KEY_ACTIVITYRECOGNITION_CONFIDENCE, String.valueOf(value));
	}
	public int getConfidencePercentage() {
		String value=getValue(GlobalStaticValues.KEY_CONFIDENCE_PERCENTAGE,mContext.getString(R.string.defaultconfidence));
		return Integer.valueOf(value);
	}
	public void setConfidencePercentage(int value) {
		setValue(GlobalStaticValues.KEY_CONFIDENCE_PERCENTAGE, String.valueOf(value));
	}
	public void setInVehicleCntThreshhold(int nbr) {
		setValue(GlobalStaticValues.KEY_INVEHICLECNTTHRESHHOLD,String.valueOf(nbr));
	}
	public int getInVehicleCntThreshhold() {
		String value=getValue(GlobalStaticValues.KEY_INVEHICLECNTTHRESHHOLD,mContext.getString(R.string.defaultinfrequencythreshhold));
		return Integer.valueOf(value);
	}
}
