package com.diamondsoftware.android.kidminder;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;

public abstract class TimerServiceAbstract extends Service implements DoesTimerServiceFunctions  {
	protected abstract void doACTION_STOP();
	protected abstract void doACTION_HEARTBEAT_INTERVAL_CHANGED();
	protected abstract void doACTION_STARTING_FROM_MAINACTIVITY();
	protected abstract void doACTION_STARTING_FROM_BOOTUP();
	protected abstract void stop();
	
	private TimerServiceLocationManagerHelper mTimerServiceLocationManagerHelper;
	protected SettingsManager mSettingsManager;
	private Timer mRestTimer=null;
	protected Date mTimeWhenRestTimerStarted;
	protected Date mRestTimerCurrent;


    @Override
    public void onCreate() {
    	super.onCreate();
    	
		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandlerTimer(
				this));
        mTimerServiceLocationManagerHelper=new TimerServiceLocationManagerHelper(this);
		mSettingsManager=new SettingsManager(this);
        this.mTimeWhenRestTimerStarted=new Date();
        this.mRestTimerCurrent=new Date();
        resetRestTimerTimeValues();


    }
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		if(intent!=null) {
			String action=intent.getAction();
			if(action!=null) {
				if(action.equals(GlobalStaticValues.ACTION_STOP)) {
					doACTION_STOP();
					stopMyRestTimer();
				} else {
					if(action.equals(GlobalStaticValues.ACTION_HEARTBEAT_INTERVAL_CHANGED)) {
						doACTION_HEARTBEAT_INTERVAL_CHANGED();
					} else {
						if(action.equals(GlobalStaticValues.ACTION_STARTING_FROM_MAINACTIVITY)) {
							doACTION_STARTING_FROM_MAINACTIVITY();
						} else {
							if(action.equals(GlobalStaticValues.ACTION_STARTING_FROM_BOOTUP)) {
								doACTION_STARTING_FROM_BOOTUP();
							}
						}
					}
				}
			}
		}
		return Service.START_STICKY;
	}		

	public void notifyActivityThatGPSIsNotOn() {
    	if(isMyActivityRunning()) {
    		Intent broadcastIntent = new Intent()
    				.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
	        broadcastIntent.setAction(GlobalStaticValues.NOTIFICATION_GPS_NOT_ENABLED);
	        // Broadcast whichever result occurred
	        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    	} else {
	    	Intent intent=GlobalStaticValues.getIntentForMainActivity(this)
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

	public boolean isMyActivityRunning(){
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		 List< ActivityManager.RunningTaskInfo > runningTaskInfo = manager.getRunningTasks(200); 
		 for(ActivityManager.RunningTaskInfo info : runningTaskInfo) {
			 String className=info.baseActivity.getClassName();
			 if(className.indexOf("MainActivity")!=-1 && info.baseActivity.getPackageName().equals(getPackageName())) {
				 return true;
			 }
		 }
		return false;
	}
    
    
	@Override
	public void onDestroy() {
    	stop();	
    	mTimerServiceLocationManagerHelper.onDestroy();
	}

	protected boolean isGPSAlive() {
		return mTimerServiceLocationManagerHelper.isGPSAlive();
	}
    protected void alarm() {
		new Logger(mSettingsManager.getLoggingLevel(), "MainActivity", this)
		.log("Alarm Occurred", GlobalStaticValues.LOG_LEVEL_NOTIFICATION);
		if(mSettingsManager.getNotificationUsesPopup()) {
	    	if(this.isMyActivityRunning()) {
	    		Intent broadcastIntentAlert=GlobalStaticValues.getIntentForMainActivity(this);
	    		broadcastIntentAlert.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
		        broadcastIntentAlert.setAction(GlobalStaticValues.NOTIFICATION_POPUPALERT);
		        // Broadcast whichever result occurred
		        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntentAlert);
	    	} else {
	    		Intent intent=GlobalStaticValues.getIntentForMainActivity(this);
	    		intent.setAction(GlobalStaticValues.ACTION_POPUPALERT);
		    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    	startActivity(intent);
	    	}
		} else {
	        // Create an explicit content Intent that starts the main Activity
			Intent notificationIntent=GlobalStaticValues.getIntentForMainActivityAlert(this);
			notificationIntent.setAction(GlobalStaticValues.ACTION_STARTING_FROM_NOTIFICATION_ALERT);

	        // Construct a task stack
	        android.support.v4.app.TaskStackBuilder stackBuilder = android.support.v4.app.TaskStackBuilder.create(this);

	        // Adds the main Activity to the task stack as the parent
	        stackBuilder.addParentStack(GlobalStaticValues.getClassForMainActivity());

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
	        
	        builder.setSmallIcon(R.drawable.app_icon)
	               .setContentTitle(this.getString(R.string.alertnotificationtitle))
	               .setContentText(alertString)
	               .setContentIntent(notificationPendingIntent)
	               .setAutoCancel(true)
	               .setPriority(NotificationCompat.PRIORITY_MAX)
	               .setOnlyAlertOnce(true);
	        
			boolean ringerModeNormal=false;
			boolean ringerModeSilent=false;
			boolean ringerModeVibrate=false;
			AudioManager audio = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);				
			switch( audio.getRingerMode() ){
				case AudioManager.RINGER_MODE_NORMAL:
					ringerModeNormal=true;
				   break;
				case AudioManager.RINGER_MODE_SILENT:
					ringerModeSilent=true;
				   break;
				case AudioManager.RINGER_MODE_VIBRATE:
					ringerModeVibrate=true;
				   break;
			}				

	        
	        if(mSettingsManager.getNotificationUsesSound() &&
	        		(ringerModeNormal)
	        		) {
	        	if(!GlobalStaticValues.DoLoopingAlarm) {
		        	if(mSettingsManager.getSoundType().equals(GlobalStaticValues.KEY_NOTIFICATION_SOUND_ALARM)) {

		        		try {
		        		    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		        			final Ringtone ringTone = RingtoneManager.getRingtone(getApplicationContext(), notification);
		        		    GlobalStaticValues.getRingtone(getApplicationContext()).play();   
		        			final Timer stopToneTimer = new Timer("RestTimer");
		        			stopToneTimer.schedule(new TimerTask() {
		        				public void run() {
		        					try {
		        						if(GlobalStaticValues.getRingtone(getApplicationContext()).isPlaying()) {
		    	        					GlobalStaticValues.getRingtone(getApplicationContext()).stop();
		        						}
		        					} catch (Exception e2) {}
		        					try {
		        						stopToneTimer.cancel();
		        						stopToneTimer.purge();
		        					} catch (Exception e) {
		        					}
		        				}
		        			}, 13200, 13200);
		        		} catch (Exception e) {
		        		    e.printStackTrace();
		        		}    	
		        	} else {
		        		if(mSettingsManager.getSoundType().equals(GlobalStaticValues.KEY_NOTIFICATION_SOUND_NOTIFICATION)) {
			 	               builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));	        			
				        }
		        	}
	        	} else {
		        	if(mSettingsManager.getSoundType().equals(GlobalStaticValues.KEY_NOTIFICATION_SOUND_ALARM)) {
		        		builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
		        	} else {
		        		if(mSettingsManager.getSoundType().equals(GlobalStaticValues.KEY_NOTIFICATION_SOUND_NOTIFICATION)) {
		 	               builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));	        			
			        	}
	        		}
	        	}	        		
        	}
	        if(mSettingsManager.getNotificationUsesVibrate() && 
	        		(ringerModeNormal || ringerModeVibrate)) {
	               builder.setVibrate(new long[] {0, 1000,500,1000,500,1000,500,1000,500,1000,500,1000,500,1000});
	        }
	        // Get an instance of the Notification manager
	        NotificationManager mNotificationManager =
	            (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

	       	mNotificationManager.notify((int)new Date().getTime(), builder.build());
		}					    	
    }
	// -----------------------------------------  RestTimer ----------------------------------------------------------------------------
	protected void resetRestTimerTimeValues() {
		this.mTimeWhenRestTimerStarted=new Date();
		this.mRestTimerCurrent=new Date();

	}


	private Timer getRestTimer() {
		if (mRestTimer == null) {
			mRestTimer = new Timer("RestTimer");
		}
		return mRestTimer;
	}	
	protected void stopMyRestTimer() {
		if (mRestTimer != null) {
			try {
				mRestTimer.cancel();
				mRestTimer.purge();
			} catch (Exception e) {
			}
			mRestTimer = null;
			mSettingsManager.setCurrentRestTime(0);
    		Intent broadcastIntentAlert = new Intent()
    			.setAction(GlobalStaticValues.NOTIFICATION_CURRENT_REST_TIME)
    			.putExtra(GlobalStaticValues.KEY_CURRENT_REST_TIME, 0l);
	        // Broadcast whichever result occurred
	        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntentAlert);
	        this.resetRestTimerTimeValues();
		}
	}	
	protected void startMyRestTimer() {
		stopMyRestTimer();
		mTimeWhenRestTimerStarted=new Date();
		mSettingsManager.setCurrentRestTime(0);
		getRestTimer().schedule(new TimerTask() {
			public void run() {
				mRestTimerCurrent=new Date();
				long millisFromTimerWhenRestTimerStarted=mTimeWhenRestTimerStarted.getTime();
				long millisFromRestTimeCurrent=mRestTimerCurrent.getTime();
				double timeInSeconds=((double)millisFromRestTimeCurrent-(double)millisFromTimerWhenRestTimerStarted)/1000;
				long timeInSecondsLong=(long)timeInSeconds;
	    		Intent broadcastIntentAlert = new Intent()
    			.setAction(GlobalStaticValues.NOTIFICATION_CURRENT_REST_TIME)
    			.putExtra(GlobalStaticValues.KEY_CURRENT_REST_TIME, timeInSecondsLong);
		        // Broadcast whichever result occurred
		        LocalBroadcastManager.getInstance(TimerServiceAbstract.this).sendBroadcast(broadcastIntentAlert);
		        restTimerPopped();
			}
		}, GlobalStaticValues.RestTimerInterval*1000, GlobalStaticValues.RestTimerInterval*1000);
	}

	protected void restTimerPopped() {
		long intervalInSeconds=Math.abs((mRestTimerCurrent.getTime() - mTimeWhenRestTimerStarted.getTime())/1000);
		int intervalInMinutes=(int)((float)intervalInSeconds/60f);
		if(intervalInMinutes>=mSettingsManager.getStoppedTimeMinutesBeforeNotification()) {
			this.stopMyRestTimer();
			resetRestTimerTimeValues();
			alarm();
		}		
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
