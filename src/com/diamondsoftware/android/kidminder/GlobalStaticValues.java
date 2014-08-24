package com.diamondsoftware.android.kidminder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class GlobalStaticValues {
	public static final String NOTIFICATION_SPEED ="Speed_Notification";
	public static final String NOTIFICATION_HEARTBEAT ="Heartbeat_Notification";
	public static final String NOTIFICATION_GOTSPEED ="GotSpeed_Notification";
	public static final String NOTIFICATION_GPS_NOT_ENABLED ="NOTIFICATION_GPS_NOT_ENABLED";
	public static final String NOTIFICATION_POPUPALERT="notificationPopupAlert";
	
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
	public static final String KEY_NOTIFICATION_USES_POPUP = "popup";
	public static final String KEY_IS_DRIVING_THRESHHOLD = "isdriving";
	public static final String KEY_STOPPEDTIME_MINUTES_BEFORE_NOTIFICATION = "stoptime";
	
	public static final String ACTION_GPS_NOT_ENABLED = "ACTIONGPGNOTENABLED";
	public static final String ACTION_STOP="ACTION_STOP";
	public static final String ACTION_HEARTBEAT_INTERVAL_CHANGED="HEARTBEAT_INTERVAL_CHANGED";
	public static final String ACTION_STARTING_FROM_MAINACTIVITY="StartingFromMainActivity";
	public static final String ACTION_STARTING_FROM_BOOTUP="StartingFromBootup";
	public static final String ACTION_POPUPALERT="actionPopupAlert";
	public static final String ACTION_STARTING_FROM_NOTIFICATION="actionstartingfromnotification";
	
	public static final int LOG_LEVEL_INFORMATION=0;
	public static final int LOG_LEVEL_NOTIFICATION=1;
	public static final int LOG_LEVEL_CRITICAL=2;
	public static final int LOG_LEVEL_FATAL=3;
	
	public static final int DEFAULT_MPH_DRIVINGTHRESHHOLD=5;
	public static final int DEFAULT_MINUTES_STOPPED=7;
	public static final long RestTimerInterval=30;
	
    // Milliseconds per second
    public static final int MILLISECONDS_PER_SECOND = 1000;
    // A fast frequency ceiling in milliseconds
    public static final long FASTEST_INTERVAL = 5000;

	public static final DateFormat MDATEFORMAT = new SimpleDateFormat(
	"yyyy-MM-dd HH:mm:ss.S");
    
}
