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
    public enum REQUEST_TYPE {START, STOP}
	private boolean mWasMoving=false;
    private REQUEST_TYPE mRequestType;
    /*
     * Store the PendingIntent used to send activity recognition events
     * back to the app
     */
    private PendingIntent mActivityRecognitionPendingIntent;
    // Store the current activity recognition client
    private ActivityRecognitionClient mActivityRecognitionClient;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;
	
    @Override
    public void onCreate() {
        super.onCreate();
        mInProgress=false;
        mSettingsManager.setCurrentRestTime(0);
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
                this, TimerServiceActivityRecognition.class)
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
				if(action.equals(GlobalStaticValues.ACTION_ACTIVITY_RECOGNITION_CHANGE_ALERT)) {
			        // If the incoming intent contains an update
			        if (ActivityRecognitionResult.hasResult(intent)) {
			            // Get the update
			            ActivityRecognitionResult result =
			                    ActivityRecognitionResult.extractResult(intent);
			            // Get the most probable activity
			            DetectedActivity mostProbableActivity =
			                    result.getMostProbableActivity();
			            /*
			             * Get the probability that this activity is the
			             * the user's actual activity
			             */
			            int confidence = mostProbableActivity.getConfidence();
			            /*
			             * Get an integer describing the type of activity
			             */
			            int activityType = mostProbableActivity.getType();
			            String activityName = getNameFromType(activityType);
			            /*
			             * At this point, you have retrieved all the information
			             * for the current update. You can display this
			             * information to the user in a notification, or
			             * send it to an Activity or Service in a broadcast
			             * Intent.
			             */
						Intent broadcastIntent2 = new Intent()
							.putExtra(GlobalStaticValues.KEY_ACTIVITYRECOGNITION, activityName);
				        broadcastIntent2.setAction(GlobalStaticValues.NOTIFICATION_ACTIVITYRECOGNITION);
				        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent2);
			            
			            if(activityType==DetectedActivity.IN_VEHICLE) {
			            	stopMyRestTimer();
			            	mWasMoving=true;
			            } else {
			            	if(mWasMoving) {
			            		startMyRestTimer();
			            		mWasMoving=false;
			            	}
			            }
			        } else {
			            /*
			             * This implementation ignores intents that don't contain
			             * an activity update. If you wish, you can report them as
			             * errors.
			             */
			        }
			    }
			}
		}
		return Service.START_STICKY;
	}
	
    /**
     * Map detected activity types to strings
     *@param activityType The detected activity type
     *@return A user-readable name for the type
     */
    private String getNameFromType(int activityType) {
        switch(activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
        }
        return "unknown";
    }
	
    @Override
	protected void doACTION_STOP() {
    	stop();
    }

	@Override
	protected void doACTION_HEARTBEAT_INTERVAL_CHANGED() {
		stop();
		start();

	}

	@Override
	protected void doACTION_STARTING_FROM_MAINACTIVITY() {
		start();
	}	

	@Override
	protected void doACTION_STARTING_FROM_BOOTUP() {		
		mSettingsManager.setIsEnabled(true);  // force enabled when starting from bootup
		start();
	}

	private void start() {
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
	    /*
	     * Called by Location Services once the location client is connected.
	     *
	     * Continue by requesting activity updates.
	     */
		if(mRequestType==REQUEST_TYPE.START) {
        /*
         * Request activity recognition updates using the preset
         * detection interval and PendingIntent. This call is
         * synchronous.
         */
        mActivityRecognitionClient.requestActivityUpdates(
                mSettingsManager.getHeartbeatFrequency()*1000,
                mActivityRecognitionPendingIntent);
		} else {
	        mActivityRecognitionClient.removeActivityUpdates(mActivityRecognitionPendingIntent);
	        mSettingsManager.setActivityRecognition("");
			Intent broadcastIntent2 = new Intent()
			.putExtra(GlobalStaticValues.KEY_ACTIVITYRECOGNITION, "");
			broadcastIntent2.setAction(GlobalStaticValues.NOTIFICATION_ACTIVITYRECOGNITION);
			LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent2);
	        
		}
        /*
         * Since the preceding call is synchronous, turn off the
         * in progress flag and disconnect the client
         */
        mInProgress = false;
        mActivityRecognitionClient.disconnect();
	}

	@Override
	public void onDisconnected() {
        // Turn off the request flag
        mInProgress = false;
        // Delete the client
        mActivityRecognitionClient = null;		
	}

}
