package com.diamondsoftware.android.kidminder;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

public class TimerService extends Service implements GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener,
com.google.android.gms.location.LocationListener  {
	private SettingsManager mSettingsManager;
    private LocationClient mLocationClient;
	private LocationManager mLocationManager = null;
	private boolean mDrivingFlag=false;
	private Timer mRestTimer=null;
	private Date mTimeWhenRestTimerStarted;
	private Date mRestTimerCurrent;
	private int mJeDisSimulation=0;
	private boolean DO_SIMULATION_SO_YOU_DONT_HAVE_TO_BE_DRIVING=false;
	private TimerServiceLocationManagerHelper mTimerServiceLocationManagerHelper;

	
    // Global constants
    // Define an object that holds accuracy and frequency parameters
    LocationRequest mLocationRequest;
	
    @Override
    public void onLocationChanged(Location location) {
		double speed=0;
		// Broadcast message to listeners
		GregorianCalendar gc=new GregorianCalendar(Locale.getDefault());
		gc.setTime(new Date());
		Intent broadcastIntent2 = new Intent();
        broadcastIntent2.setAction(GlobalStaticValues.NOTIFICATION_HEARTBEAT)
        	.putExtra(GlobalStaticValues.KEY_LATESTLOCATION_DATESTAMP,GlobalStaticValues.MDATEFORMAT.format(gc.getTime()))
        	.putExtra(GlobalStaticValues.KEY_LATESTLOCATION_LATITUDE,location.getLatitude())
        	.putExtra(GlobalStaticValues.KEY_LATESTLOCATION_LONGITUDE,location.getLongitude());
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent2);
		
		if(location.hasSpeed()||(true&&DO_SIMULATION_SO_YOU_DONT_HAVE_TO_BE_DRIVING)) {
			if(DO_SIMULATION_SO_YOU_DONT_HAVE_TO_BE_DRIVING) {
				if(mJeDisSimulation<5) {
					speed=10;
				} else {
					speed=0;
				}
				mJeDisSimulation++;
			} else {
				speed=(double)location.getSpeed();				
			}
			speed=(speed*(double)3600)/1609.34;
			Intent broadcastIntent = new Intent();
	        broadcastIntent.setAction(GlobalStaticValues.NOTIFICATION_GOTSPEED);
	        // Broadcast whichever result occurred
	        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
		} 
        mSettingsManager.setCurrentSpeed(speed);
		Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(GlobalStaticValues.NOTIFICATION_SPEED)
        	.putExtra("speed", speed);
        // Broadcast whichever result occurred
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        if(mDrivingFlag) {
        	if(speed>mSettingsManager.getIsDrivingThreshhold()) {
        		this.resetRestTimerTimeValues();
        	} else {
				long intervalInSeconds=Math.abs((mRestTimerCurrent.getTime() - mTimeWhenRestTimerStarted.getTime())/1000);
				int intervalInMinutes=(int)((float)intervalInSeconds/60f);
				if(intervalInMinutes>=mSettingsManager.getStoppedTimeMinutesBeforeNotification()) {
					mDrivingFlag=false;
					this.stopMyRestTimer();

					// do alarm notification
					if(DO_SIMULATION_SO_YOU_DONT_HAVE_TO_BE_DRIVING) {
						mJeDisSimulation=-1;
					}
					if(mSettingsManager.getNotificationUsesPopup()) {
				    	if(this.isMyActivityRunning()) {
				    		Intent broadcastIntentAlert = new Intent();
					        broadcastIntentAlert.setAction(GlobalStaticValues.NOTIFICATION_POPUPALERT);
					        // Broadcast whichever result occurred
					        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntentAlert);
				    	} else {
					    	Intent intent=new Intent(this,MainActivityPerspectiveTest.class)
					    		.setAction(GlobalStaticValues.ACTION_POPUPALERT);
					    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					    	startActivity(intent);
				    	}
					} else {
				        // Create an explicit content Intent that starts the main Activity
				        Intent notificationIntent =
				                new Intent(this,MainActivityPerspectiveTest.class)
				        			.setAction(GlobalStaticValues.ACTION_STARTING_FROM_NOTIFICATION); 

				        // Construct a task stack
				        android.support.v4.app.TaskStackBuilder stackBuilder = android.support.v4.app.TaskStackBuilder.create(this);

				        // Adds the main Activity to the task stack as the parent
				        stackBuilder.addParentStack(MainActivityPerspectiveTest.class);

				        // Push the content Intent onto the stack
				        stackBuilder.addNextIntent(notificationIntent);

				        // Get a PendingIntent containing the entire back stack
				        PendingIntent notificationPendingIntent =
				                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

				        // Get a notification builder that's compatible with platform versions >= 4
				        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

				        // Set the notification contents
				        String alertString=getString(R.string.alertnotificationdescription1) + 
				        		String.valueOf(mSettingsManager.getStoppedTimeMinutesBeforeNotification()) + "\n" + getString(R.string.alertnotificationdescription2);
				        
				        builder.setSmallIcon(R.drawable.ic_launcher)
				               .setContentTitle(this.getString(R.string.alertnotificationtitle))
				               .setContentText(alertString)
				               .setContentIntent(notificationPendingIntent)
				               .setAutoCancel(true)
				               .setPriority(NotificationCompat.PRIORITY_MAX)
				               .setOnlyAlertOnce(true);
				        if(mSettingsManager.getNotificationUsesSound()) {
				               builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
				        }
				        if(mSettingsManager.getNotificationUsesVibrate()) {
				               builder.setVibrate(new long[] {0, 1000,500,1000,500,1000,500,1000,500,1000,500,1000,500,1000});
				        }
				        // Get an instance of the Notification manager
				        NotificationManager mNotificationManager =
				            (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

				       	mNotificationManager.notify((int)new Date().getTime(), builder.build());

					}					
				}

        	}
        } else {
        	if(mSettingsManager.getCurrentSpeed()>mSettingsManager.getIsDrivingThreshhold()) {
        		this.startMyRestTimer();
        		mDrivingFlag=true;
        	}
        }
    }
	
    @Override
    public void onCreate() {
    	super.onCreate();
		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandlerTimer(
				this));
        mTimerServiceLocationManagerHelper=new TimerServiceLocationManagerHelper(this);
		mSettingsManager=new SettingsManager(this);
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval
        long updateInterval=(long)((long)mSettingsManager.getHeartbeatFrequency())*(long)GlobalStaticValues.MILLISECONDS_PER_SECOND;
        mLocationRequest.setInterval(updateInterval);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(GlobalStaticValues.FASTEST_INTERVAL);
        mSettingsManager.setCurrentSpeed(0);
        mSettingsManager.setCurrentRestTime(0);
        this.mTimeWhenRestTimerStarted=new Date();
        this.mRestTimerCurrent=new Date();
        mDrivingFlag=false;
    }
    
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		if(intent!=null) {
			String action=intent.getAction();
			if(action!=null) {
				if(action.equals(GlobalStaticValues.ACTION_STOP)) {
					stop();
				} else {
					if(action.equals(GlobalStaticValues.ACTION_HEARTBEAT_INTERVAL_CHANGED)) {
							stop();
							startIfNotAlreadyEnabled();
					} else {
						if(action.equals(GlobalStaticValues.ACTION_STARTING_FROM_MAINACTIVITY)) {
					        this.mTimeWhenRestTimerStarted=new Date();
					        this.mRestTimerCurrent=new Date();
					        mDrivingFlag=false;

							startIfNotAlreadyEnabled();
						} else {
							if(action.equals(GlobalStaticValues.ACTION_STARTING_FROM_BOOTUP)) {
								mSettingsManager.setIsEnabled(true);  // force enabled when starting from bootup
								startIfNotAlreadyEnabled();
							}
						}
					}
				}
			}
		}
		return Service.START_STICKY;
	}		
	private void stop() {
		if (mLocationClient!=null) {
	        if (mLocationClient.isConnected()) {
	            /*
	             * Remove location updates for a listener.
	             * The current Activity is the listener, so
	             * the argument is "this".
	             */
	        	mLocationClient.removeLocationUpdates(this);
	            mLocationClient.disconnect();	
	        }
		}
		this.stopMyRestTimer();
		if(DO_SIMULATION_SO_YOU_DONT_HAVE_TO_BE_DRIVING) {
			mJeDisSimulation=-1;
		}
        mSettingsManager.setLatestLocationDate(new Date());
        mSettingsManager.setPriorLocation(0, 0);
        mSettingsManager.setCurrentSpeed(0);
	}
	private int mDontReenter=0;
	public void notifyActivityThatGPSIsNotOn() {
    	if(this.isMyActivityRunning()) {
    		Intent broadcastIntent = new Intent();
	        broadcastIntent.setAction(GlobalStaticValues.NOTIFICATION_GPS_NOT_ENABLED);
	        // Broadcast whichever result occurred
	        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    	} else {
	    	Intent intent=new Intent(this,MainActivityPerspectiveTest.class)
	    		.setAction(GlobalStaticValues.ACTION_GPS_NOT_ENABLED);
	    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	startActivity(intent);
    	}
	}
	public void gpsIsBackOn() {
		Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(GlobalStaticValues.NOTIFICATION_GPS_HASBEEN_ENABLED);
        // Broadcast whichever result occurred
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
	}
	private void startIfNotAlreadyEnabled() {
		if(mDontReenter==0) {
			mDontReenter=1;
			if(this.mLocationClient==null || !(mLocationClient.isConnected() || mLocationClient.isConnecting())) {
			    if (! mTimerServiceLocationManagerHelper.isGPSAlive() ) {
					notifyActivityThatGPSIsNotOn();
			    } else {
			    	stop();
			        mLocationClient = new LocationClient(this, this, this);
			        mLocationClient.connect();
			    }
			}
			mDontReenter=0;
		}
	}
	@Override
	public void onDestroy() {
    	stop();	
    	mTimerServiceLocationManagerHelper.onDestroy();
	}
	

	public TimerService() {		
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onConnected(Bundle arg0) {
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}


	@Override
	public void onDisconnected() {
	}	

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		new Logger(mSettingsManager.getLoggingLevel(), "TimerService:onConnectionFailed", this)
			.log("Failed connecting to LocationClient. Msg: " +connectionResult.toString(), GlobalStaticValues.LOG_LEVEL_FATAL);
	}	
	public boolean isMyActivityRunning(){
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		 List< ActivityManager.RunningTaskInfo > runningTaskInfo = manager.getRunningTasks(100); 
		 for(ActivityManager.RunningTaskInfo info : runningTaskInfo) {
			 String className=info.baseActivity.getClassName();
			 if(className.indexOf("MainActivityPerspectiveTest")!=-1) {
				 return true;
			 }
		 }
		return false;
	}
	// -----------------------------------------  RestTimer ----------------------------------------------------------------------------
	private Timer getRestTimer() {
		if (mRestTimer == null) {
			mRestTimer = new Timer("RestTimer");
		}
		return mRestTimer;
	}
	private void resetRestTimerTimeValues() {
		// RestTimer=0
		this.mTimeWhenRestTimerStarted=new Date();
		this.mRestTimerCurrent=new Date();

	}
	private void stopMyRestTimer() {
		if (mRestTimer != null) {
			try {
				mRestTimer.cancel();
				mRestTimer.purge();
			} catch (Exception e) {
			}
			mRestTimer = null;
    		Intent broadcastIntentAlert = new Intent()
    			.setAction(GlobalStaticValues.NOTIFICATION_CURRENT_REST_TIME)
    			.putExtra(GlobalStaticValues.KEY_CURRENT_REST_TIME, 0l);
	        // Broadcast whichever result occurred
	        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntentAlert);
	        this.resetRestTimerTimeValues();
		}
	}	
	private void startMyRestTimer() {
		stopMyRestTimer();
		mTimeWhenRestTimerStarted=new Date();
		getRestTimer().schedule(new TimerTask() {
			public void run() {
				mRestTimerCurrent=new Date();
				long millisFromTimerWhenRestTimerStarted=mTimeWhenRestTimerStarted.getTime();
				long millisFromRestTimeCurrent=mRestTimerCurrent.getTime();
				double timeInSeconds=((double)mRestTimerCurrent.getTime()-(double)mTimeWhenRestTimerStarted.getTime())/1000;
				long timeInSecondsLong=(long)timeInSeconds;
	    		Intent broadcastIntentAlert = new Intent()
    			.setAction(GlobalStaticValues.NOTIFICATION_CURRENT_REST_TIME)
    			.putExtra(GlobalStaticValues.KEY_CURRENT_REST_TIME, timeInSecondsLong);
	        // Broadcast whichever result occurred
	        LocalBroadcastManager.getInstance(TimerService.this).sendBroadcast(broadcastIntentAlert);

			}
		}, GlobalStaticValues.RestTimerInterval*1000, GlobalStaticValues.RestTimerInterval*1000);
	}

}
