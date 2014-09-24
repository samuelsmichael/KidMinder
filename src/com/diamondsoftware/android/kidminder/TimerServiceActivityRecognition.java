package com.diamondsoftware.android.kidminder;

import android.app.PendingIntent;
import android.app.Service;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.content.Intent;

public class TimerServiceActivityRecognition extends TimerServiceAbstract  implements
		ConnectionCallbacks, OnConnectionFailedListener {
    public enum REQUEST_TYPE {START, STOP,CLEANUP}
    private REQUEST_TYPE mRequestType;

    /*
     * Store the PendingIntent used to send activity recognition event
     * back to the app
     */
    private PendingIntent mActivityRecognitionPendingIntent;
    // Store the current activity recognition client
    private ActivityRecognitionClient mActivityRecognitionClient;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;
	
	private boolean mAmReceiving=false;

	@Override
	public void onDestroy() {		
        mRequestType=REQUEST_TYPE.CLEANUP;
        mActivityRecognitionClient.connect();
		super.onDestroy();
	}
    
    @Override
    public void onCreate() {
        super.onCreate();
        mInProgress=false;
        mSettingsManager.setHeartbeatTicksCount(0);
        mSettingsManager.setActivityRecognition("");
        /*
         * Instantiate a new activity recognition client. Since the
         * parent Activity implements the connection listener and
         * connection failure listener, the constructor uses "this"
         * to specify the values of those parameters.
         */
        mActivityRecognitionClient =
                new ActivityRecognitionClient(this, this, this);
        /*
         * Create the PendingIntent that Location Services uses
         * to send activity recognition updates back to this app.
         */
        Intent intent = new Intent(
                this, ActivityRecognitionIntentService.class)
        	.setAction(GlobalStaticValues.ACTION_ACTIVITY_RECOGNITION_CHANGE_ALERT);
        /*
         * Return a PendingIntent that starts the IntentService.
         */
        mActivityRecognitionPendingIntent =
                PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		if(intent!=null) {
			String action=intent.getAction();
			if(action!=null) {
				if(action.equals(GlobalStaticValues.ACTION_ACTION_START_RESTTIMER)) {
					this.startMyRestTimer();
				} else {
					if(action.equals(GlobalStaticValues.ACTION_ACTION_STOP_RESTTIMER)) {
						this.stopMyRestTimer();
					} 
				}
			}
		}
		return Service.START_STICKY;
	}
	
    @Override
	protected void doACTION_STOP() {
    	mThenStart=false;
    	stop();
    }

    private boolean mThenStart=false;
	@Override
	protected void doACTION_HEARTBEAT_INTERVAL_CHANGED() {
		mThenStart=true;
		stop();
	}

	@Override
	protected void doACTION_STARTING_FROM_MAINACTIVITY() {
		resetRestTimerTimeValues();
		start();
	}	

	@Override
	protected void doACTION_STARTING_FROM_BOOTUP() {		
		mSettingsManager.setIsEnabled(true);  // force enabled when starting from bootup
		resetRestTimerTimeValues();
		start();
	}

	private void start() {
	    if (! isGPSAlive() ) {
			notifyActivityThatGPSIsNotOn();
	    } else {
	
	        // If a request is not already underway
	        if (!mInProgress) {
	            // Indicate that a request is in progress
	            mInProgress = true;
	            mRequestType=REQUEST_TYPE.START;
	            // Request a connection to Location Services
	            mActivityRecognitionClient.connect();
	        //
	        } else {
	            /*
	             * A request is already underway. You can handle
	             * this situation by disconnecting the client,
	             * re-setting the flag, and then re-trying the
	             * request.
	             */
	        }
	    }
	}
	@Override
	protected void stop() {		
        mRequestType=REQUEST_TYPE.STOP;
        // Request a connection to Location Services
        mActivityRecognitionClient.connect();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		boolean thenStart=false;
	    /*
	     * Called by Location Services once the location client is connected.
	     *
	     * Continue by requesting activity updates.
	     */
		if(mRequestType==REQUEST_TYPE.START) {
			new Logger(mSettingsManager.getLoggingLevel(), "MainActivity", this)
			.log("Starting Activity Recognition Service", GlobalStaticValues.LOG_LEVEL_INFORMATION);
        /*
         * Request activity recognition updates using the preset
         * detection interval and PendingIntent. This call is
         * synchronous.
         */
			int heartbeatFrequency=mSettingsManager.getHeartbeatFrequency();
			mActivityRecognitionClient.requestActivityUpdates(
                heartbeatFrequency*1000,
                mActivityRecognitionPendingIntent);
				mAmReceiving=true;
		} else {
			if(mRequestType==REQUEST_TYPE.STOP) {
				new Logger(mSettingsManager.getLoggingLevel(), "MainActivity", this)
				.log("Stopping Activity Recognition Service", GlobalStaticValues.LOG_LEVEL_INFORMATION);
		        mActivityRecognitionClient.removeActivityUpdates(mActivityRecognitionPendingIntent);
		        mSettingsManager.setActivityRecognition("");
				Intent broadcastIntent2 = new Intent()
				.putExtra(GlobalStaticValues.KEY_ACTIVITYRECOGNITION, "");
				broadcastIntent2.setAction(GlobalStaticValues.NOTIFICATION_ACTIVITYRECOGNITION);
				LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent2);
				mAmReceiving=false;
				if(this.mThenStart) {
					thenStart=true;
				}
			} else {
				if(mRequestType==REQUEST_TYPE.CLEANUP) {
					new Logger(mSettingsManager.getLoggingLevel(), "TimerServiceActivityRecognition", this)
					.log("Did CLEANUP", GlobalStaticValues.LOG_LEVEL_NOTIFICATION);

					if(this.mAmReceiving) {
						mActivityRecognitionClient.removeActivityUpdates(mActivityRecognitionPendingIntent);
					}
				}
			}
		}
        /*
         * Since the preceding call is synchronous, turn off the
         * in progress flag and disconnect the client
         */
        mInProgress = false;
        mActivityRecognitionClient.disconnect();
        if(thenStart) {
	        start();
        }
	}

	@Override
	public void onDisconnected() {
        // Turn off the request flag
        mInProgress = false;
        // Delete the client
        mActivityRecognitionClient = null;		
	}

}
