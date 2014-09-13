package com.diamondsoftware.android.kidminder;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;


public class ActivityRecognitionIntentService extends IntentService {
	protected SettingsManager mSettingsManager;
	
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

		int confidence;
		int activityType=DetectedActivity.UNKNOWN;
		Intent broadcastIntent3 = new Intent();
        broadcastIntent3.setAction(GlobalStaticValues.NOTIFICATION_HEARTBEAT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent3);

        // If the incoming intent contains an update
        if (ActivityRecognitionResult.hasResult(intent)||(mSettingsManager.getCurrentSimilationStatus())) {
			if(mSettingsManager.getCurrentSimilationStatus()) {
				int heartbeat=mSettingsManager.getHeartbeatFrequency();
				float factor1=(30f)/((float)heartbeat/5f) * (float)mSettingsManager.getStoppedTimeMinutesBeforeNotification();
				float factor2=factor1/2;
				int factor1int=((int)factor1+1);
				int factor2int=factor1int/2;
				if(mSettingsManager.getJeDisSimulationCount()%factor1int<factor2int) {
					activityType=DetectedActivity.IN_VEHICLE;
				} else {
					activityType=DetectedActivity.STILL;
				}
				confidence=100;
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
	            confidence = mostProbableActivity.getConfidence();
	            /*
	             * Get an integer describing the type of activity
	             */
	            activityType = mostProbableActivity.getType();

			}
            String activityName = getNameFromType(activityType);
    		new Logger(mSettingsManager.getLoggingLevel(), "ActivityRecognition", this)
			.log("ActivityRecogonition: "+activityName+ " confidence: "+String.valueOf(confidence), GlobalStaticValues.LOG_LEVEL_NOTIFICATION);

            // What is TILTING good for?  
            if(activityType==DetectedActivity.TILTING || activityType==DetectedActivity.UNKNOWN) {
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
				.putExtra(GlobalStaticValues.KEY_ACTIVITYRECOGNITION, activityName)
				.putExtra(GlobalStaticValues.KEY_ACTIVITYRECOGNITION_CONFIDENCE,confidence);
	        broadcastIntent2.setAction(GlobalStaticValues.NOTIFICATION_ACTIVITYRECOGNITION);
	        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent2);
	        int confidenceThreshhold=mSettingsManager.getConfidencePercentage();
	        if(confidence<confidenceThreshhold&&activityType==DetectedActivity.IN_VEHICLE) {
	        	return;
	        }
	        int wasStopped=mSettingsManager.getWasStoppedCount();
	        int wasMoving=mSettingsManager.getWasMovingCount();
            if(activityType==DetectedActivity.IN_VEHICLE) {
            	if(1==1 || mSettingsManager.getWasStoppedCount()>2) {
            		Intent intent2=new Intent(this,TimerServiceActivityRecognition.class)
            			.setAction(GlobalStaticValues.ACTION_ACTION_STOP_RESTTIMER);
            		startService(intent2);
            		mSettingsManager.setWasStoppedCount(0);
            	}
            	mSettingsManager.incrementWasMovingCount();
            } else {
            	int threshhold=mSettingsManager.getInVehicleCntThreshhold();
            	if(mSettingsManager.getWasMovingCount()>=threshhold) {
            		Intent intent2=new Intent(this,TimerServiceActivityRecognition.class)
            			.setAction(GlobalStaticValues.ACTION_ACTION_START_RESTTIMER);
            		startService(intent2);
            		mSettingsManager.setWasMovingCount(0);
            	}
            	mSettingsManager.incrementWasStoppedCount();
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
