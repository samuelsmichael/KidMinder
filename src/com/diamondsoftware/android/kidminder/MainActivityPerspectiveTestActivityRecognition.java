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

public class MainActivityPerspectiveTestActivityRecognition extends
		MainActivity {
	private Switch mSwitch;
	private TextView mCurrentRestTime;
	private TextView mActivityRecognition;
	private TextView mHeartbeatIndicator;
	private TextView mConfidence;
	
    private MyBroadcastReceiverActivityRecognition mBroadcastReceiver;
    // An intent filter for the broadcast receiver
    private IntentFilter mIntentFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_perspectivetestactivityrecognition);
		if(mBroadcastReceiver!=null) {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
		}
        // Create a new broadcast receiver to receive updates from the listeners and service
        mBroadcastReceiver = new MyBroadcastReceiverActivityRecognition(this);
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(GlobalStaticValues.NOTIFICATION_ACTIVITYRECOGNITION);
		// Register the broadcast receiver to receive status updates
		LocalBroadcastManager.getInstance(this).registerReceiver(
				mBroadcastReceiver, mIntentFilter);

    	mCurrentRestTime=(TextView)findViewById(R.id.currentresttime_idActivityRecognition);
        mHeartbeatIndicator=(TextView)findViewById(R.id.heartbeatcount_idActivityRecognition);
        mConfidence=(TextView)findViewById(R.id.activityconfidenceid);

    	mActivityRecognition=(TextView)findViewById(R.id.activityrecognitionstatus_idActivityRecognition);
        mSwitch=(Switch)findViewById(R.id.enabledSwitchActivityRecognition);
        mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(!isChecked) {
					pressedDisableButton();
				} else {
					pressedEnableButton();
				}
				MainActivityPerspectiveTestActivityRecognition.this.onResumeManageView();
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
		long currentRestTimeInSeconds=mSettingsManager.getCurrentRestTime();
		int minutes=(int)currentRestTimeInSeconds/60;
		int seconds=(int)currentRestTimeInSeconds % 60;
		String current=String.valueOf(minutes)+" m   "+String.valueOf(seconds)+ " s";
		mCurrentRestTime.setText(current);
		mActivityRecognition.setText(mSettingsManager.getActivityRecognition());
		this.mHeartbeatIndicator.setText(String.format(Locale.getDefault(), "%d", mSettingsManager.getHeartbeatTicksCount()));
		mConfidence.setText(String.valueOf(mSettingsManager.getConfidence()));
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
				.putExtra("livevstest", "test");						
			startActivity(i2);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

    /**
     * Define a Broadcast receiver that receives updates from connection listeners
     */
    public class MyBroadcastReceiverActivityRecognition extends BroadcastReceiver {
    	private Activity mActivity;
    	
    	public MyBroadcastReceiverActivityRecognition(Activity activity) {
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
            if (TextUtils.equals(action, GlobalStaticValues.NOTIFICATION_ACTIVITYRECOGNITION)) {
        		String activityRecognition=intent.getStringExtra(GlobalStaticValues.KEY_ACTIVITYRECOGNITION);
        		int confidence=intent.getIntExtra(GlobalStaticValues.KEY_ACTIVITYRECOGNITION_CONFIDENCE,0);
                new Logger(mSettingsManager.getLoggingLevel(),"MainActivityPerspectiveTest",MainActivityPerspectiveTestActivityRecognition.this).log("About to setCurrentActivityRecogition. Activity Recognition="+activityRecognition, GlobalStaticValues.LOG_LEVEL_INFORMATION);
            	mSettingsManager.setActivityRecognition(activityRecognition);
            	mSettingsManager.setConfidence(confidence);
            }
        	onResumeManageView();
        }
    }

}
