package com.diamondsoftware.android.kidminder;

import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

public class TimerService extends Service implements GooglePlayServicesClient.ConnectionCallbacks,
													 GooglePlayServicesClient.OnConnectionFailedListener {
	private Timer mTimer=null;
	private SettingsManager mSettingsManager;
    private LocationClient mLocationClient;
    private boolean mImConnected=false;

	private void doS(Location location) {
		double speed=0;
		if(location.hasSpeed()) {
			speed=location.getSpeed();
		} 
		Intent broadcastIntent = new Intent();
	        broadcastIntent.setAction(GlobalStaticValues.SPEED_NOTIFICATION)
	        	.putExtra("speed", location.getSpeed());
	        // Broadcast whichever result occurred
	        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandlerTimer(
				this));
		mSettingsManager=new SettingsManager(this);
        mLocationClient = new LocationClient(this, this, this);		
        mLocationClient.connect();
		return START_STICKY;
	}		

	@Override
	public void onDestroy() {
    	stopTimer2();		
	}
	
	private void stopTimer2() {
		if (mTimer != null) {
			try {
				mTimer.cancel();
				mTimer.purge();
			} catch (Exception e) {
			}
			mTimer = null;
		}
	}	
	private void startTimer2(long trigger, long interval) {
		getTimer().schedule(new TimerTask() {
			public void run() {
				try {
					Location location=mLocationClient.getLastLocation();
					doS(location);
				} catch (Exception ee) {
					
				}
			}
		}, trigger, interval);
	}

	private Timer getTimer() {
		if (mTimer == null) {
			mTimer = new Timer("KidMinderHeartbeat");
		}
		return mTimer;
	}

	public TimerService() {		
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onConnected(Bundle arg0) {
		if(!mImConnected) {
			Location location=mLocationClient.getLastLocation();
			if(location!=null) {
				doS(location);
				int frequency=mSettingsManager.getHeartbeatFrequency();
				startTimer2(1000*frequency,1000*frequency);
			}
			mImConnected=true;
		}
	}


	@Override
	public void onDisconnected() {
		mImConnected=false;
	}	

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
	}	
}
