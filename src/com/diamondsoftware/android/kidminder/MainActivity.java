package com.diamondsoftware.android.kidminder;

import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
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
import android.os.Vibrator;
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
	
    private MyBroadcastReceiver mBroadcastReceiver;
    // An intent filter for the broadcast receiver
    private IntentFilter mIntentFilter;
    private final Vibrator mVibrator= (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

	protected abstract void onResumeManageView();
	protected abstract void stopTimer();
	protected abstract void pressedEnableButton();
	protected abstract void pressedDisableButton();

	protected SettingsManager mSettingsManager;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettingsManager=new SettingsManager(this);
		if(mBroadcastReceiver!=null) {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
		}
        // Create a new broadcast receiver to receive updates from the listeners and service
        mBroadcastReceiver = new MyBroadcastReceiver();
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(GlobalStaticValues.NOTIFICATION_SPEED);
		mIntentFilter.addAction(GlobalStaticValues.NOTIFICATION_HEARTBEAT);
		mIntentFilter.addAction(GlobalStaticValues.NOTIFICATION_GOTSPEED);
		mIntentFilter.addAction(GlobalStaticValues.NOTIFICATION_GPS_NOT_ENABLED);
		mIntentFilter.addAction(GlobalStaticValues.NOTIFICATION_POPUPALERT);
		// Register the broadcast receiver to receive status updates
		LocalBroadcastManager.getInstance(this).registerReceiver(
				mBroadcastReceiver, mIntentFilter);
		// Perform the requested action
		String action=getIntent().getAction();
		if(action!=null) {
	        if(action.equals(GlobalStaticValues.ACTION_GPS_NOT_ENABLED)) {
	        	showGPSNotEnabledDialog();
	    		mSettingsManager.setIsEnabled(false);    	
	        } else {
	        	if(action.equals(GlobalStaticValues.ACTION_POPUPALERT)) {
	        		doPopupAlert();
	        	} else {
	        		if(action.equals(GlobalStaticValues.ACTION_STARTING_FROM_NOTIFICATION)) {
	        			// I don't know of anything I need to do special.  We know the service is alive and well, otherwise we wouldn't have got this notification
	        		} else {
			        	if(mSettingsManager.getIsEnabled()) {
			        		baseStartTimerService();
			        	} else {
			        		this.baseStopTimerService();
			        	}
		        	}
	        	}
	        }
		}
    }
    private void doPopupAlert() {
    	if(mSettingsManager.getNotificationUsesPopup()) {
	 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle("Kid Alert");//
	        String alertString=getString(R.string.alertnotificationdescription1) + 
	        		String.valueOf(mSettingsManager.getStoppedTimeMinutesBeforeNotification()) + "\n" + getString(R.string.alertnotificationdescription2);
	        builder.setMessage(alertString)
	        	.setCancelable(false)
				.setPositiveButton("Okay", new DialogInterface.OnClickListener() {			
					@Override
	               public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
						dialog.dismiss();
						mVibrator.cancel();
	               }
				}
			);
	        final AlertDialog alert = builder.create();
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
    	if(mSettingsManager.getNotificationUsesVibrate()) {
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
    	if(mSettingsManager.getNotificationUsesSound()) {
    		try {
    		    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    		    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
    		    r.play();
    		} catch (Exception e) {
    		    e.printStackTrace();
    		}    		
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
    protected void baseStartTimerService() {
		mSettingsManager.setCurrentSpeed(0);
		mSettingsManager.setGotSpeedTicksCount(0);
		mSettingsManager.setHeartbeatTicksCount(0);
		mSettingsManager.setIsEnabled(true);    	
        Intent intent=new Intent(this,TimerService.class)
    		.setAction("StartingFromMainActivity");
        startService(intent);
    }
    
    protected void baseStopTimerService() {
		Intent intent=new Intent(this,TimerService.class)
			.setAction(GlobalStaticValues.ACTION_STOP);
		startService(intent);
		mSettingsManager.setCurrentSpeed(0);
		mSettingsManager.setGotSpeedTicksCount(0);
		mSettingsManager.setHeartbeatTicksCount(0);
		mSettingsManager.setLatestLocationDate(new Date());
		mSettingsManager.setPriorLocationDate(new Date());
		mSettingsManager.setPriorLocation(mSettingsManager.getLatestLocation().latitude, mSettingsManager.getLatestLocation().longitude);
		mSettingsManager.setIsEnabled(false);
    }
    @Override
    protected void onDestroy() {
		if(mBroadcastReceiver!=null) {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
		}
    	super.onDestroy();
    }
        
    @Override
    protected void onResume() {
    	super.onResume();
    	onResumeManageView();
    }
    
    protected void enabledStatusChangedTo(boolean value) {
    	
    }

    /**
     * Define a Broadcast receiver that receives updates from connection listeners
     */
    public class MyBroadcastReceiver extends BroadcastReceiver {
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
                			mSettingsManager.setIsEnabled(false);    	
                			showGPSNotEnabledDialog();
                		} else {
                			if(TextUtils.equals(action, GlobalStaticValues.NOTIFICATION_POPUPALERT)) {
                				doPopupAlert();
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
