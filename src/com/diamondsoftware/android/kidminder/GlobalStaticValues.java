package com.diamondsoftware.android.kidminder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.content.Intent;


public class GlobalStaticValues {
	public static enum TIMER_IMPLEMENTATIONS {
		ORIGINAL,
		ORIGINAL_REFACTORED,
		NEW,
		ACTIVITY_RECOGNITION,
		ACTIVITY_RECOGNITION_LIVE
	}
	public static final TIMER_IMPLEMENTATIONS myTimerImplementation=TIMER_IMPLEMENTATIONS.ACTIVITY_RECOGNITION_LIVE;
	public static Intent getIntentForTimer(Context context) {
		Intent intent;
		switch (myTimerImplementation) {
		case ORIGINAL:
			intent = new Intent(context,TimerService.class); 
			break;
		case ORIGINAL_REFACTORED:
			intent=new Intent(context,TimerServiceOriginal.class);
			break;
		case NEW:
			intent = new Intent(context,TimerServiceNew.class);
			break;
		case ACTIVITY_RECOGNITION:
		case ACTIVITY_RECOGNITION_LIVE:
			intent=new Intent(context,TimerServiceActivityRecognition.class);
			break;
		default:
			intent = new Intent(context,TimerService.class); 
			break;
		}
		return intent;
	}
	public static Class<?> getClassForMainActivity() {
		Class<?> classness;
		switch (myTimerImplementation) {
		case ACTIVITY_RECOGNITION_LIVE:
			classness=MainActivityPerspectiveLiveActivityRecognition.class;
			break;
		case ACTIVITY_RECOGNITION:
			classness=MainActivityPerspectiveTestActivityRecognition.class;
			break;
		default:
			classness = MainActivityPerspectiveTestLocationService.class; 
			break;
		}
		return classness;
		
	}
	public static Intent getIntentForMainActivity(Context context) {
		Intent intent;
		switch (myTimerImplementation) {
		case ACTIVITY_RECOGNITION:
			intent=new Intent(context,MainActivityPerspectiveTestActivityRecognition.class);
			break;
		case ACTIVITY_RECOGNITION_LIVE:
			intent=new Intent(context,MainActivityPerspectiveLiveActivityRecognition.class);
			break;
		default:
			intent = new Intent(context,MainActivityPerspectiveTestLocationService.class); 
			break;
		}
		return intent;
	}
	public static Intent getIntentForMainActivityAlert(Context context) {
		Intent intent;
		switch (myTimerImplementation) {
		case ACTIVITY_RECOGNITION:
			intent=new Intent(context,MainActivityPerspectiveTestActivityRecognition.class);
			break;
		case ACTIVITY_RECOGNITION_LIVE:
			intent=new Intent(context,ActivityAlert.class);
			break;
		default:
			intent = new Intent(context,MainActivityPerspectiveTestLocationService.class); 
			break;
		}
		return intent;
	}

	public static final String NOTIFICATION_SPEED ="Speed_Notification";
	public static final String NOTIFICATION_HEARTBEAT ="Heartbeat_Notification";
	public static final String NOTIFICATION_GOTSPEED ="GotSpeed_Notification";
	public static final String NOTIFICATION_GPS_NOT_ENABLED ="NOTIFICATION_GPS_NOT_ENABLED";
	public static final String NOTIFICATION_GPS_HASBEEN_ENABLED ="NOTIFICATION_GPS_HASBEEN_ENABLED";
	public static final String NOTIFICATION_POPUPALERT="notificationPopupAlert";
	public static final String NOTIFICATION_CURRENT_REST_TIME="currentresttime";
	public static final String NOTIFICATION_ACTIVITYRECOGNITION="NOTIFICATION_ACTIVITYRECOGNITION";
	
	public static final String KEY_LOGGINGLEVEL= "LoggingLevel";
	public static final String KEY_HEARTBEATFREQUENCY="HeartbeatFrequency";	
	public static final String KEY_HEARTBEAT_TICKS_COUNT="HeartbeatTicksCount";
	public static final String KEY_GOTSPEED_COUNT="GotSpeedCount";
	public static final String KEY_ISENABLED = "IsEnabled";
	public static final String KEY_CURRENTSPEED = "CurrentSpeed";
	public static final String KEY_PRIORLOCATION_LATITUDE = "PRIORLOCATION_LATITUDE";
	public static final String KEY_PRIORLOCATION_LONGITUDE = "PRIORLOCATION_LONGTUDE";
	public static final String KEY_LATESTLOCATION_LATITUDE = "CURRENTLOCATION_LATITUDE";
	public static final String KEY_LATESTLOCATION_LONGITUDE = "CURRENTLOCATION_LONGITUDE";
	public static final String KEY_PRIORLOCATION_DATESTAMP = "PRIORLOCATIONDATESTAMP";
	public static final String KEY_LATESTLOCATION_DATESTAMP = "LATESTLOCATIONDATESTAMP";
	public static final String KEY_NOTIFICATION_USES_SOUND = "sound";
	public static final String KEY_NOTIFICATION_USES_VIBRATE = "vibrate";
	public static final String KEY_NOTIFICATION_USES_POPUP = "popup_liveversion";
	public static final String KEY_IS_DRIVING_THRESHHOLD = "isdriving";
	public static final String KEY_STOPPEDTIME_MINUTES_BEFORE_NOTIFICATION = "stoptime";
	public static final String KEY_CURRENT_REST_TIME="CURRENTRestTime";
	public static final String KEY_SIMULATION="key_simulation";
	public static final String KEY_PREFERENCES_TYPE="key_prefences_type";
	public static final String KEY_ACTIVITYRECOGNITION = "key activityrecognition";
	public static final String KEY_ACTIVITYRECOGNITION_CONFIDENCE = "key activityrecognition CONFIDENCE";
	public static final String KEY_WASMOVING = "key WAS MOVING";
	public static final String KEY_WASSTOPPED = "key was stopped";
	public static final String KEY_CONFIDENCE_PERCENTAGE ="confidencepercentage";
	public static final String KEY_INVEHICLECNTTHRESHHOLD = "invehiclethreshhold";
	public static final String KEY_NOTIFICATION_SOUND_ALARM = "0";
	public static final String KEY_NOTIFICATION_SOUND_NOTIFICATION="1";
	
	public static final String ACTION_GPS_NOT_ENABLED = "ACTIONGPGNOTENABLED";
	public static final String ACTION_STOP="ACTION_STOP";
	public static final String ACTION_HEARTBEAT_INTERVAL_CHANGED="HEARTBEAT_INTERVAL_CHANGED";
	public static final String ACTION_STARTING_FROM_MAINACTIVITY="StartingFromMainActivity";
	public static final String ACTION_STARTING_FROM_BOOTUP="StartingFromBootup";
	public static final String ACTION_POPUPALERT="actionPopupAlert";
	public static final String ACTION_STARTING_FROM_NOTIFICATION_ALERT="actionstartingfromnotification";
	public static final String ACTION_ACTIVITY_RECOGNITION_CHANGE_ALERT="ACTIONactivityrecognitionchangealert";
	public static final String ACTION_ACTION_START_RESTTIMER="STARTRESTTIMER1";
	public static final String ACTION_ACTION_STOP_RESTTIMER="STOPRESTTIMER1";
	
	public static final int LOG_LEVEL_INFORMATION=0;
	public static final int LOG_LEVEL_NOTIFICATION=1;
	public static final int LOG_LEVEL_CRITICAL=2;
	public static final int LOG_LEVEL_FATAL=3;
	
	public static final int DEFAULT_MPH_DRIVINGTHRESHHOLD=5;
	public static final int DEFAULT_MINUTES_STOPPED=7;
	public static final long RestTimerInterval=2;
	
    // Milliseconds per second
    public static final int MILLISECONDS_PER_SECOND = 1000;
    // A fast frequency ceiling in milliseconds
    public static final long FASTEST_INTERVAL = 5000;

	public static final DateFormat MDATEFORMAT = new SimpleDateFormat(
	"yyyy-MM-dd HH:mm:ss.S");
    
}
