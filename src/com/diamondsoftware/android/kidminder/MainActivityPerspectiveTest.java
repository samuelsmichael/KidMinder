package com.diamondsoftware.android.kidminder;


import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivityPerspectiveTest extends MainActivity {
	private SpeedometerView speedometer;
	private Switch mSwitch;
	private TextView mHeartbeatIndicator;
	private TextView mGotSpeedIndicator;
    private MyBroadcastReceiver mBroadcastReceiver;
    // An intent filter for the broadcast receiver
    private IntentFilter mIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_perspectivetest);
        
        mSwitch=(Switch)findViewById(R.id.enabledSwitch);
        mHeartbeatIndicator=(TextView)findViewById(R.id.heartbeatcount_id);
        mGotSpeedIndicator=(TextView)findViewById(R.id.gotspeedcount_id);
        
        mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(!isChecked) {
					MainActivityPerspectiveTest.this.resetTimer();
				} else {
					pressedEnableButton();
				}
				MainActivityPerspectiveTest.this.onResumeManageView();
			}
        	
        });
        
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


        
  	  // Customize SpeedometerView
  	  speedometer = (SpeedometerView) findViewById(R.id.speedometer);

  	  // Add label converter
  	  speedometer.setLabelConverter(new SpeedometerView.LabelConverter() {
  	      @Override
  	      public String getLabelFor(double progress, double maxProgress) {
  	          return String.valueOf((int) Math.round(progress));
  	      }
  	  });

  	  // configure value range and ticks
  	  speedometer.setMaxSpeed(100);
  	  speedometer.setMajorTickStep(10);
  	  speedometer.setMinorTicks(2);

  	  // Configure value range colors
  	  speedometer.addColoredRange(0, 30, Color.GREEN);
  	  speedometer.addColoredRange(30, 60, Color.YELLOW);
  	  speedometer.addColoredRange(60, 100, Color.RED);	
  	  
    }

    @Override
    protected void onDestroy() {
		if(mBroadcastReceiver!=null) {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
		}
    	super.onDestroy();
    }
    
	@Override
	protected void onResumeManageView() {
		mSwitch.setChecked(mSettingsManager.getIsEnabled());
		speedometer.setSpeed(mSettingsManager.getCurrentSpeed());
		this.mHeartbeatIndicator.setText(String.format(Locale.getDefault(), "%d", mSettingsManager.getHeartbeatTicksCount()));
		int gotSpeedTicks=mSettingsManager.getGotspeedTicksCount();
		mGotSpeedIndicator.setText(String.format(Locale.getDefault(), "%d", gotSpeedTicks));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.perspective_test, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent i2 = new Intent(this, Preferences.class);
			startActivity(i2);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}


    /**
     * Define a Broadcast receiver that receives updates from connection listeners and
     * the geofence transition service.
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
                new Logger(mSettingsManager.getLoggingLevel(),"MainActivityPerspectiveTest",MainActivityPerspectiveTest.this).log("About to setCurrentSpeed. speed="+String.valueOf(speed), GlobalStaticValues.LOG_LEVEL_INFORMATION);
            	mSettingsManager.setCurrentSpeed(speed);
            } else {
            	if (TextUtils.equals(action, GlobalStaticValues.HEARTBEAT_NOTIFICATION)) {
            		mSettingsManager.incrementHeartbeatTicksCount();
            	} else {
                	if (TextUtils.equals(action, GlobalStaticValues.GOTSPEED_NOTIFICATION)) {
                		mSettingsManager.incrementGotSpeedCount();        
                	}
            	}
            }
        	onResumeManageView();
        }
    }


	@Override
	protected void resetTimer() {	
		mSettingsManager.setCurrentSpeed(0);
		mSettingsManager.setGotSpeedTicksCount(0);
		mSettingsManager.setHeartbeatTicksCount(0);
		mSettingsManager.setIsEnabled(false);
		Intent intent=new Intent(this,TimerService.class)
			.setAction(GlobalStaticValues.ACTION_RESET);
		startService(intent);
	}

	@Override
	protected void pressedEnableButton() {
		mSettingsManager.setCurrentSpeed(0);
		mSettingsManager.setGotSpeedTicksCount(0);
		mSettingsManager.setHeartbeatTicksCount(0);
		mSettingsManager.setIsEnabled(true);
		Intent intent=new Intent(this,TimerService.class);
		startService(intent);
	}
}
