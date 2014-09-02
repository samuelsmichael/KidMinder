package com.diamondsoftware.android.kidminder;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;


public class ActivityRecognitionIntentService extends IntentService {
	protected SettingsManager mSettingsManager;
	private int mWasMoving=0;
	private int mWasStopped=0;


	@Override
	public void onCreate() {
		super.onCreate();
		mSettingsManager=new SettingsManager(this);
	}
	public ActivityRecognitionIntentService(String name) {
		super(name);
	}
	public ActivityRecognitionIntentService() {
		super("wtf");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		int activityType=DetectedActivity.UNKNOWN;
		Intent broadcastIntent3 = new Intent();
        broadcastIntent3.setAction(GlobalStaticValues.NOTIFICATION_HEARTBEAT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent3);

        // If the incoming intent contains an update
        if (ActivityRecognitionResult.hasResult(intent)||(mSettingsManager.getCurrentSimilationStatus())) {
			if(mSettingsManager.getCurrentSimilationStatus()) {
				if(mSettingsManager.getJeDisSimulationCount()<5) {
					activityType=DetectedActivity.IN_VEHICLE;
				} else {
					activityType=DetectedActivity.STILL;
				}
				mSettingsManager.incrementJeDisSimulationCount();
			} else {
				
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
	            activityType = mostProbableActivity.getType();

			}
            String activityName = getNameFromType(activityType);
    		new Logger(mSettingsManager.getLoggingLevel(), "ActivityRecognition", this)
			.log("ActivityRecogonition: "+activityName, GlobalStaticValues.LOG_LEVEL_NOTIFICATION);

            // What is TILTING good for?  
            if(activityType==DetectedActivity.TILTING) {
            	return;
            }
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
            	if(mWasStopped>2) {
            		Intent intend=new Intent(this,TimerServiceActivityRecognition.class)
            			.setAction(GlobalStaticValues.ACTION_ACTION_STOP_RESTTIMER);
            		startService(intent);
            		mWasStopped=0;
            	}
            	mWasMoving++;
            } else {
            	if(mWasMoving>2) {
            		Intent intend=new Intent(this,TimerServiceActivityRecognition.class)
            			.setAction(GlobalStaticValues.ACTION_ACTION_START_RESTTIMER);
            		startService(intent);
            		mWasMoving=0;
            	}
            	mWasStopped++;
            }
        } else {
            /*
             * This implementation ignores intents that don't contain
             * an activity update. If you wish, you can report them as
             * errors.
             */
        }
    }
	
    /**
     * Map detected activity types to strings
     *@param activityType The detected activity type
     *@return A user-readable name for the type
     */
    private static String getNameFromType(int activityType) {
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

}
