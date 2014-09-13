package com.diamondsoftware.android.kidminder;

import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.diamondsoftware.android.kidminder.MainActivity.MyBroadcastReceiver;
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
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class MainActivityPerspectiveLiveActivityRecognition extends
		MainActivity {
	private Switch mSwitch;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettingsManager.setNotificationUsesPopup(false);
        setContentView(R.layout.activity_main_perspectiveliveactivityrecognition);
        mSwitch=(Switch)findViewById(R.id.enabledSwitchActivityRecognitionLive);
        mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(!isChecked) {
					pressedDisableButton();
				} else {
					pressedEnableButton();
				}
				MainActivityPerspectiveLiveActivityRecognition.this.onResumeManageView();
			}
        	
        });
    }
    
    @Override
    protected void baseStartTimerService(){
    	super.baseStartTimerService();
    	//TODO: Set ActivityManager indicator state ... or whatever
    }
    
    @Override
    protected void baseStopTimerService() {
		super.baseStopTimerService();
		//TODO: Set ActivityManager indicator state to: "off"
    }
    
	@Override
	protected void onResumeManageView() {
		mSwitch.setChecked(mSettingsManager.getIsEnabled());
	}

	@Override
	protected void stopTimer() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void pressedEnableButton() {
		baseStartTimerService();
	}

	@Override
	protected void pressedDisableButton() {
		this.baseStopTimerService();
        mSettingsManager.setHeartbeatTicksCount(0);
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
				.putExtra(GlobalStaticValues.KEY_PREFERENCES_TYPE, GlobalStaticValues.myTimerImplementation.toString())
				.putExtra("livevstest", "live");			
			startActivity(i2);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

}
