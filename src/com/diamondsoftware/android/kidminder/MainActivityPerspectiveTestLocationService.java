package com.diamondsoftware.android.kidminder;


import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import com.google.android.gms.maps.model.LatLng;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivityPerspectiveTestLocationService extends MainActivity {
	private SpeedometerView speedometer;
	private Switch mSwitch;
	private TextView mHeartbeatIndicator;
	private TextView mGotSpeedIndicator;
	private TextView mCurrentRestTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_perspectivetestlocationservice);
        mSwitch=(Switch)findViewById(R.id.enabledSwitch);
        mHeartbeatIndicator=(TextView)findViewById(R.id.heartbeatcount_id);
        mGotSpeedIndicator=(TextView)findViewById(R.id.gotspeedcount_id);
/*    	
 		mPriorLocation=(TextView)findViewById(R.id.prior_location_id);
    	mLatestLocation=(TextView)findViewById(R.id.latest_location_id);
    	mCalculatedSpeed=(TextView)findViewById(R.id.calculated_distance_id);
*/
    	mCurrentRestTime=(TextView)findViewById(R.id.current_rest_time_id);
        
        mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(!isChecked) {
					pressedDisableButton();
				} else {
					pressedEnableButton();
				}
				MainActivityPerspectiveTestLocationService.this.onResumeManageView();
			}
        	
        });
        
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
    	super.onDestroy();
    }
    
	@Override
	protected void onResumeManageView() {
		mSwitch.setChecked(mSettingsManager.getIsEnabled());
		speedometer.setSpeed(mSettingsManager.getCurrentSpeed());
		this.mHeartbeatIndicator.setText(String.format(Locale.getDefault(), "%d", mSettingsManager.getHeartbeatTicksCount()));
		mGotSpeedIndicator.setText(NumberFormat.getNumberInstance(Locale.US).format(mSettingsManager.getGotspeedTicksCount()));
		LatLng priorLocation=mSettingsManager.getPriorLocation();
		LatLng latestLocation=mSettingsManager.getLatestLocation();
		Date priorLocationDate=mSettingsManager.getPriorLocationDate();
		Date latestLocationDate=mSettingsManager.getLatestLocationDate();
		if(priorLocationDate!=null && priorLocation != null && latestLocationDate!=null && latestLocation != null) {
			long intervalInSeconds=Math.abs((latestLocationDate.getTime() - priorLocationDate.getTime())/1000);
			Location priorLocationAsLocation=new Location("GPS");
			priorLocationAsLocation.setLatitude(priorLocation.latitude);
			priorLocationAsLocation.setLongitude(priorLocation.longitude);
			Location latestLocationAsLocation=new Location("GPS");
			latestLocationAsLocation.setLatitude(latestLocation.latitude);
			latestLocationAsLocation.setLongitude(latestLocation.longitude);
			if(priorLocation.latitude!=0) {
				float dxInMeters=priorLocationAsLocation.distanceTo(latestLocationAsLocation);
				double mph= (((double)dxInMeters/(double)intervalInSeconds)*(double)3600)/1609.34;
			} else {
			}
		}
		long currentRestTimeInSeconds=mSettingsManager.getCurrentRestTime();
		int minutes=(int)currentRestTimeInSeconds/60;
		int seconds=(int)currentRestTimeInSeconds % 60;
		mCurrentRestTime.setText(String.valueOf(minutes)+" m   "+String.valueOf(seconds)+ " s");
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
			Intent i2 = new Intent(this, Preferences.class)
				.putExtra(GlobalStaticValues.KEY_PREFERENCES_TYPE, GlobalStaticValues.myTimerImplementation.toString());
			startActivity(i2);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void baseStopTimerService() {
		super.baseStopTimerService();
		mSettingsManager.setCurrentSpeed(0);
		mSettingsManager.setGotSpeedTicksCount(0);
		mSettingsManager.setHeartbeatTicksCount(0);
		mSettingsManager.setLatestLocationDate(new Date());
		mSettingsManager.setPriorLocationDate(new Date());
		mSettingsManager.setPriorLocation(mSettingsManager.getLatestLocation().latitude, mSettingsManager.getLatestLocation().longitude);
	}
	
	@Override
	protected void baseStartTimerService() {
		super.baseStartTimerService();
		mSettingsManager.setCurrentSpeed(0);
		mSettingsManager.setGotSpeedTicksCount(0);
		mSettingsManager.setHeartbeatTicksCount(0);
	}

	@Override
	protected void stopTimer() {
		baseStopTimerService();
	}

	@Override
	protected void pressedEnableButton() {
		baseStartTimerService();
	}

	@Override
	protected void pressedDisableButton() {
		doAnyPressedDisableButtonActions();
		baseStopTimerService();
        mSettingsManager.setHeartbeatTicksCount(0);
	}
}
