package com.diamondsoftware.android.kidminder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
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
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class MainActivityPerspectiveLiveActivityRecognition extends
		MainActivity {
	private Switch mSwitch;
	ImageButton btn_advancedsettings_alert;
	ImageButton btn_closewindow_alert;
	LinearLayout whenEnabled;
	LinearLayout whenDisabled;
	String currentVersion="Version 1.00";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettingsManager.setNotificationUsesPopup(false);
        setContentView(R.layout.activity_main_perspectiveliveactivityrecognition);
        try {
        	currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {}
		if (!initializeVersion()) {
			Intent intent = new Intent(this,ActivityWelcomeScreen1.class);
			startActivity(intent);
		}

        whenEnabled=(LinearLayout)findViewById(R.id.middlearea_whenenabled);
        whenDisabled=(LinearLayout)findViewById(R.id.middlearea_whendisabled);
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
        btn_closewindow_alert=(ImageButton)this.findViewById(R.id.btn_closewindow_alert);
        btn_advancedsettings_alert=(ImageButton)findViewById(R.id.btn_advancedsettings_alert);
        btn_advancedsettings_alert.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent i2 = new Intent(MainActivityPerspectiveLiveActivityRecognition.this, Preferences.class)
				.putExtra(GlobalStaticValues.KEY_PREFERENCES_TYPE, GlobalStaticValues.myTimerImplementation.toString())
				.putExtra("livevstest", "live");			
			startActivity(i2);
			}
		});
        btn_closewindow_alert.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				MainActivityPerspectiveLiveActivityRecognition.this.finish();
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
		if(mSwitch.isChecked()) {
			whenEnabled.setVisibility(View.VISIBLE);
			whenDisabled.setVisibility(View.GONE);
		} else {
			whenDisabled.setVisibility(View.VISIBLE);
			whenEnabled.setVisibility(View.GONE);
		}
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
	/*
	 * return true if file existed beforehand
	 */
	public boolean initializeVersion() {
		boolean retValue = false;
		if (!getExistsVersionFile()) {
			stampVersion(0);
			retValue = false;
		} else {
			retValue = true;
		}
		return retValue;
	}

	private boolean getExistsVersionFile() {
		boolean retValue = false;
		File file = null;
		if (isSdPresent()) {
			file = new File("/sdcard/kidminder/version" + currentVersion
					+ "a.txt");
			if (file.exists()) {
				retValue = true;
			}
		} else {
			file = new File("/data/data/" + getPackageName()
					+ "/files/version" + currentVersion + "a.txt");
			if (file.exists()) {
				retValue = true;
			}
		}
		return retValue;
	}

	public boolean isSdPresent() {
		String sdState = android.os.Environment.getExternalStorageState();
		return sdState.equals(android.os.Environment.MEDIA_MOUNTED);
	}

	public void stampVersion(int nbrOfAlertSets) {
		FileOutputStream fos = null;
		PrintWriter pw = null;
		try {
			fos = getVersionOutputStream();
			pw = new PrintWriter(fos);
			pw.write(currentVersion);
			pw.write("~." + String.valueOf(nbrOfAlertSets));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				pw.close();
			} catch (Exception eee) {
			}
			try {
				fos.close();
			} catch (Exception eee) {
			}
		}
	}

	private FileOutputStream getVersionOutputStream()
			throws FileNotFoundException {
		FileOutputStream fileOutputStream_Version = null;
		File file = null;
		if (isSdPresent()) {
			file = new File("/sdcard/kidminder/version");
			if (!file.exists()) {
				file.mkdirs();
			}
			fileOutputStream_Version = new FileOutputStream(
					"/sdcard/kidminder/version" + currentVersion + "a.txt",
					false);
		} else {
			fileOutputStream_Version = openFileOutput("version"
					+ currentVersion + "a.txt",
					Context.MODE_WORLD_READABLE);
		}
		return fileOutputStream_Version;
	}


}
