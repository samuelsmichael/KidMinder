package com.diamondsoftware.android.kidminder;

import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;

import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

public abstract class MainActivity extends Activity {
    private MyBroadcastReceiver mBroadcastReceiver;
    // An intent filter for the broadcast receiver
    private IntentFilter mIntentFilter;

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
		mIntentFilter.addAction(GlobalStaticValues.SPEED_NOTIFICATION);
		mIntentFilter.addAction(GlobalStaticValues.HEARTBEAT_NOTIFICATION);
		mIntentFilter.addAction(GlobalStaticValues.GOTSPEED_NOTIFICATION);

		// Register the broadcast receiver to receive status updates
		LocalBroadcastManager.getInstance(this).registerReceiver(
				mBroadcastReceiver, mIntentFilter);
        
		baseStartTimerService();
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
            if (TextUtils.equals(action, GlobalStaticValues.SPEED_NOTIFICATION)) {
        		double speed=intent.getDoubleExtra("speed", 0);
                new Logger(mSettingsManager.getLoggingLevel(),"MainActivityPerspectiveTest",MainActivity.this).log("About to setCurrentSpeed. speed="+String.valueOf(speed), GlobalStaticValues.LOG_LEVEL_INFORMATION);
            	mSettingsManager.setCurrentSpeed(speed);
            } else {
            	if (TextUtils.equals(action, GlobalStaticValues.HEARTBEAT_NOTIFICATION)) {
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
                	if (TextUtils.equals(action, GlobalStaticValues.GOTSPEED_NOTIFICATION)) {
                		mSettingsManager.incrementGotSpeedCount();        
                	}
            	}
            }
        	onResumeManageView();
        }
    }
    
}
