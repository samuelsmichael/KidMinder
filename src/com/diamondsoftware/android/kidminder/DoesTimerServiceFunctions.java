package com.diamondsoftware.android.kidminder;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

public interface DoesTimerServiceFunctions {
	public void notifyActivityThatGPSIsNotOn();
	public void gpsIsBackOn();
	public Object getSystemService(String value);
	public Intent registerReceiver(BroadcastReceiver yourReceiver, IntentFilter theFilter);
	public void unregisterReceiver(BroadcastReceiver yourReceiver);
}
