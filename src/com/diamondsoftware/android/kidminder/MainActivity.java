package com.diamondsoftware.android.kidminder;

import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public abstract class MainActivity extends Activity {
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private WakeLock screenLock=null;
	
    private MyBroadcastReceiver mBroadcastReceiver;
    // An intent filter for the broadcast receiver
    private IntentFilter mIntentFilter;
    private Vibrator mVibrator;
    private Ringtone mRingTone;

	protected abstract void onResumeManageView();
	protected abstract void stopTimer();
	protected abstract void pressedEnableButton();
	protected abstract void pressedDisableButton();

	protected SettingsManager mSettingsManager;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandlerTimer(
				this));
        mVibrator= (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mSettingsManager=new SettingsManager(this);
		if(mBroadcastReceiver!=null) {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
		}
        // Create a new broadcast receiver to receive updates from the listeners and service
        mBroadcastReceiver = new MyBroadcastReceiver(this);
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(GlobalStaticValues.NOTIFICATION_SPEED);
		mIntentFilter.addAction(GlobalStaticValues.NOTIFICATION_HEARTBEAT);
		mIntentFilter.addAction(GlobalStaticValues.NOTIFICATION_GOTSPEED);
		mIntentFilter.addAction(GlobalStaticValues.NOTIFICATION_GPS_NOT_ENABLED);
		mIntentFilter.addAction(GlobalStaticValues.NOTIFICATION_POPUPALERT);
		mIntentFilter.addAction(GlobalStaticValues.NOTIFICATION_CURRENT_REST_TIME);
		mIntentFilter.addAction(GlobalStaticValues.NOTIFICATION_GPS_HASBEEN_ENABLED);
		// Register the broadcast receiver to receive status updates
		LocalBroadcastManager.getInstance(this).registerReceiver(
				mBroadcastReceiver, mIntentFilter);
		// Perform the requested action
		Intent intent=getIntent();
		String action=intent.getAction();
		if(action!=null) {
	        if(action.equals(GlobalStaticValues.ACTION_GPS_NOT_ENABLED)) {
	        	showGPSNotEnabledDialog();
	    		mSettingsManager.setIsEnabled(false);    	
	        } else {
	        	if(action.equals(GlobalStaticValues.ACTION_POPUPALERT)) {
	        		doPopupAlert(false);
	        	} else {
	        		if(action.equals(GlobalStaticValues.ACTION_STARTING_FROM_NOTIFICATION_ALERT)) {
	        			doPopupAlert(true);
	        		} else {
	        			if(action.equals("android.intent.action.MAIN")) {
	        			} else {
	        			}
		        	}
	        	}
	        }
		}
    }
    private void doPopupAlert(boolean justDoWindow) {
    	if(mSettingsManager.getNotificationUsesPopup() || justDoWindow) {
			/* This makes it happen even if the system is sleeping or locked */
    		if(screenLock==null) {
				screenLock = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(
					     PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
				screenLock.acquire();
    		}
	 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle("Kid Alert");//
	        String alertString=getString(R.string.alertnotificationdescription1) + " " +
	        		String.valueOf(mSettingsManager.getStoppedTimeMinutesBeforeNotification())+" minute"+ (mSettingsManager.getStoppedTimeMinutesBeforeNotification()>1?"s":"") + getString(R.string.alertnotificationdescription2);
	        builder.setMessage(alertString)
	        	.setCancelable(false)
				.setPositiveButton("Okay", new DialogInterface.OnClickListener() {			
					@Override
	               public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
						dialog.dismiss();
						mVibrator.cancel();
						if(mRingTone!=null) {
							mRingTone.stop();
							mRingTone=null;
						}
	               }
				}
			);
	        final AlertDialog alert = builder.create();
	        alert.setCanceledOnTouchOutside(false);

	        ErrorDialogFragment errorFragment =
	                new ErrorDialogFragment();
	        errorFragment.setDialog(alert);
	        // Set the dialog in the DialogFragment
	        // Show the error dialog in the DialogFragment
	        errorFragment.show(
	                getFragmentManager(),
	                "Alert Dialog");
    	}
    	if(mSettingsManager.getNotificationUsesVibrate() && !justDoWindow) {
			// Start without a delay
			// Vibrate for 1000 milliseconds
			// Sleep for 150 milliseconds
			final long[] pattern = {0, 1000, 150};

			// The '0' here means to repeat indefinitely
			// '-1' would play the vibration once
		    new Thread(new Runnable() {
		        public void run() {
		        	mVibrator.vibrate(pattern, 0);
		        }	        
		    }).start();		
    	}
    	if(mSettingsManager.getNotificationUsesSound() && !justDoWindow) {
    		try {
    		    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    		    mRingTone = RingtoneManager.getRingtone(getApplicationContext(), notification);
    		    mRingTone.play();    		    
    		} catch (Exception e) {
    		    e.printStackTrace();
    		}    		
    	} else {
    		mRingTone=null;
    	}
    }
    private void showGPSNotEnabledDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("GPS is disabled");//
        builder.setMessage("For best results, your GPS should be enabled. Do you want to enable it?")
        	.setCancelable(false)
			.setNegativeButton("No", new DialogInterface.OnClickListener() {			
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {			
				@Override
               public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
					dialog.dismiss();
					startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
               }
			}
		);
        final AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(false);       
        ErrorDialogFragment errorFragment =
                new ErrorDialogFragment();
        // Set the dialog in the DialogFragment
        errorFragment.setDialog(alert);
        // Show the error dialog in the DialogFragment
        errorFragment.show(
                getFragmentManager(),
                "GPS not enabled");

        alert.show();	    	
    }
    
    protected void doAnyPressedDisableButtonActions() {
		mVibrator.cancel();
		if(mRingTone!=null) {
			mRingTone.stop();
			mRingTone=null;
		}
    }
    
    protected void baseStartTimerService() {
		mSettingsManager.setIsEnabled(true);    	
        Intent intent=GlobalStaticValues.getIntentForTimer(this)
    		.setAction("StartingFromMainActivity");
        startService(intent);
    }
    
	protected boolean isMyServiceRunning(){
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		 List< RunningServiceInfo > runningServices = manager.getRunningServices(200); 
		 for(ActivityManager.RunningServiceInfo info : runningServices) {
			 String className=info.service.getClassName();
			 if(className.indexOf(getPackageName())!=-1) {
				 int bkhere=3;
				 int bkthere=bkhere;
			 }
			 if(className.indexOf("TimerService")!=-1 && className.indexOf(getPackageName())!=-1) {
				 return true;
			 }
		 }
		return false;
	}

    
    protected void baseStopTimerService() {
		Intent intent=GlobalStaticValues.getIntentForTimer(this)
			.setAction(GlobalStaticValues.ACTION_STOP);
		startService(intent);
		mSettingsManager.setIsEnabled(false);
    }
    @Override
    protected void onDestroy() {
		if(mBroadcastReceiver!=null) {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
		}
		if(screenLock!=null) {
			screenLock.release();		
		}
    	super.onDestroy();
    }
        
    @Override
    protected void onResume() {
    	super.onResume();
    	mSettingsManager.setImOnTop(true);
    	onResumeManageView();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	mSettingsManager.setImOnTop(false);
    }
    
    protected void enabledStatusChangedTo(boolean value) {
    	
    }

    /**
     * Define a Broadcast receiver that receives updates from connection listeners
     */
    public class MyBroadcastReceiver extends BroadcastReceiver {
    	private Activity mActivity;
    	
    	public MyBroadcastReceiver(Activity activity) {
    		mActivity=activity;
    	}
        /*
         * Define the required method for broadcast receivers
         * This method is invoked when a broadcast Intent triggers the receiver
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            // Check the action code and determine what to do
            String action = intent.getAction();
            if (TextUtils.equals(action, GlobalStaticValues.NOTIFICATION_SPEED)) {
        		double speed=intent.getDoubleExtra("speed", 0);
                new Logger(mSettingsManager.getLoggingLevel(),"MainActivityPerspectiveTest",MainActivity.this).log("About to setCurrentSpeed. speed="+String.valueOf(speed), GlobalStaticValues.LOG_LEVEL_INFORMATION);
            	mSettingsManager.setCurrentSpeed(speed);
            } else {
            	if (TextUtils.equals(action, GlobalStaticValues.NOTIFICATION_HEARTBEAT)) {
            		mSettingsManager.incrementHeartbeatTicksCount();
            		// save date and location, after having copied to the "prior" fields
            		mSettingsManager.setPriorLocationDate(mSettingsManager.getLatestLocationDate());
            		LatLng latlng=mSettingsManager.getLatestLocation();
            		mSettingsManager.setPriorLocation(latlng.latitude, latlng.longitude);
        			GregorianCalendar gc=new GregorianCalendar();
        			try {
        				gc.setTime(GlobalStaticValues.MDATEFORMAT.parse(intent.getStringExtra(GlobalStaticValues.KEY_LATESTLOCATION_DATESTAMP)));
        			} catch (ParseException e) {
        				gc.setTime(new Date());
        			}        			
            		mSettingsManager.setLatestLocationDate(gc.getTime());
            		mSettingsManager.setLatestLocation(
            				intent.getDoubleExtra(GlobalStaticValues.KEY_LATESTLOCATION_LATITUDE, 0), 
            				intent.getDoubleExtra(GlobalStaticValues.KEY_LATESTLOCATION_LONGITUDE, 0));
            	} else {
                	if (TextUtils.equals(action, GlobalStaticValues.NOTIFICATION_GOTSPEED)) {
                		mSettingsManager.incrementGotSpeedCount();        
                	} else {
                		if(TextUtils.equals(action, GlobalStaticValues.NOTIFICATION_GPS_NOT_ENABLED)) {
                			mSettingsManager.setEnabledStateBeforeGPSWasTurnedOff(mSettingsManager.getIsEnabled());
                			mSettingsManager.setIsEnabled(false);    	
                			pressedDisableButton();
            				if(mSettingsManager.getImOnTop()) {
            					MainActivity.this.showGPSNotEnabledDialog();
            				} else { 
	                			Intent jdIntent=new Intent(mActivity, MainActivityPerspectiveTestLocationService.class)
	                			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
	                			.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
	                			.setAction(GlobalStaticValues.ACTION_GPS_NOT_ENABLED);
	                			startActivity(jdIntent);
            				}
                		} else {
                			if(TextUtils.equals(action, GlobalStaticValues.NOTIFICATION_POPUPALERT)) {
                				if(mSettingsManager.getImOnTop()) {
                					MainActivity.this.doPopupAlert(false);
                				} else {
                    			Intent jdIntent=new Intent(mActivity, MainActivityPerspectiveTestLocationService.class)
                    			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    			.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    			.setAction(GlobalStaticValues.ACTION_POPUPALERT);
                    			startActivity(jdIntent);
                				}
                			} else {
                				if(TextUtils.equals(action, GlobalStaticValues.NOTIFICATION_CURRENT_REST_TIME)) {
                					mSettingsManager.setCurrentRestTime(intent.getLongExtra(GlobalStaticValues.KEY_CURRENT_REST_TIME, 0));
                				} else {
                					if(TextUtils.equals(action, GlobalStaticValues.NOTIFICATION_GPS_HASBEEN_ENABLED)) {
                						if(mSettingsManager.getEnabledStateBeforeGPSWasTurnedOff()) {
                							pressedEnableButton();
                							mSettingsManager.setIsEnabled(true);
                						}
                					}
                				}
                			}
                		}
                	}
            	}
            }
        	onResumeManageView();
        }
    }
    
    // ------------------------------------------------   Generic Popup Dialog ---------------------------------------------------------------------------------
    
    // ------------------------------------------------   Insuring that Play Services is on this computer  -----------------------------------------------------
    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
        
    }
    @Override 
    public void onSaveInstanceState(Bundle outState) {
    	
    }

    /*
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
            switch (resultCode) {
                case Activity.RESULT_OK :
                	baseStartTimerService();
                break;
            }
        }
    }    
    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates",
                    "Google Play services is available.");
            // Continue
            return true;
        // Google Play services was not available for some reason
        } else {
            // Get the error code
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);
            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(
                        getFragmentManager(),
                        "Location Updates");
            }
            return false;
        }
    }
}
