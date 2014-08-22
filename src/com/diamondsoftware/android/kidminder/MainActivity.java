package com.diamondsoftware.android.kidminder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public abstract class MainActivity extends Activity {
	protected abstract void onResumeManageView();
	protected abstract void resetTimer();
	protected abstract void pressedEnableButton();

	protected SettingsManager mSettingsManager;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettingsManager=new SettingsManager(this);
        Intent intent=new Intent(this,TimerService.class);
        startService(intent);
    }
    @Override
    protected void onResume() {
    	super.onResume();
    	onResumeManageView();
    }
    
    protected void enabledStatusChangedTo(boolean value) {
    	
    }
}
