package com.diamondsoftware.android.kidminder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompleteReceiver extends BroadcastReceiver {
	public BootCompleteReceiver() {
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
    	Intent jdItent2=GlobalStaticValues.getIntentForTimer(context)
    		.setAction(GlobalStaticValues.ACTION_STARTING_FROM_BOOTUP);
		context.startService(jdItent2);
	}	
}
