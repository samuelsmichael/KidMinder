package com.diamondsoftware.android.kidminder;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

public class TimerService extends Service implements GooglePlayServicesClient.ConnectionCallbacks,
													 GooglePlayServicesClient.OnConnectionFailedListener {
	private Timer mTimer=null;
	private SettingsManager mSettingsManager;
    private LocationClient mLocationClient;
    private boolean mImConnected2=false;
	private LocationManager mLocationManager = null;
	private Location mPreviousLocation=null;
	private Calendar mPreviousCalendar=null;
	
	private LocationManager getLocationManager() {
		if (mLocationManager == null) {
			mLocationManager = (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
		}
		return mLocationManager;
	}	


	private void doS(Location location) {
		double speed=0;
		
		if(location.hasSpeed()) {
			speed=(double)location.getSpeed();
			speed=(speed*(double)3600)/1609.34;
			Intent broadcastIntent = new Intent();
	        broadcastIntent.setAction(GlobalStaticValues.GOTSPEED_NOTIFICATION);
	        // Broadcast whichever result occurred
	        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
		} 
		Intent broadcastIntent = new Intent();
	        broadcastIntent.setAction(GlobalStaticValues.SPEED_NOTIFICATION)
	        	.putExtra("speed", speed);
	        // Broadcast whichever result occurred
	        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
		
		/*
		if(mPreviousLocation==null) {
			mPreviousLocation=location;
		}
		double dxInMeters=(double)location.distanceTo(mPreviousLocation);
		double heartBeatInSeconds=mSettingsManager.getHeartbeatFrequency();
		if(heartBeatInSeconds!=0 && dxInMeters>5) {
			double dxPerSecond=dxInMeters/heartBeatInSeconds;
			speed=(dxPerSecond*(double)3600)/(double)1609.34;
		}
        new Logger(mSettingsManager.getLoggingLevel(),"TimerService",this).log("Timer doS(). speed="+String.valueOf(speed), GlobalStaticValues.LOG_LEVEL_INFORMATION);
		Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(GlobalStaticValues.SPEED_NOTIFICATION)
        	.putExtra("speed", speed);
        // Broadcast whichever result occurred
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
		mPreviousLocation=location;
		*/
		/* hasSpeed() is unreliable
		if(location.hasSpeed()) {
			speed=(location.getSpeed()*3600)/1609.34;
	        new Logger(mSettingsManager.getLoggingLevel(),"TimerService",this).log("Timer doS(). speed="+String.valueOf(speed), GlobalStaticValues.LOG_LEVEL_INFORMATION);
			Intent broadcastIntent = new Intent();
		        broadcastIntent.setAction(GlobalStaticValues.SPEED_NOTIFICATION)
		        	.putExtra("speed", speed);
		        // Broadcast whichever result occurred
		        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
		} 
		*/
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandlerTimer(
				this));
		if(intent!=null) {
			String action=intent.getAction();
			if(action!=null) {
				if(action.equals(GlobalStaticValues.ACTION_RESET)) {
					reset();
				} else {
					if(action.equals(GlobalStaticValues.ACTION_HEARTBEAT_INTERVAL_CHANGED)) {
						if(mTimer!=null) {
							reset();
							start();
						}
					}
				}
			} else {
				if(this.mLocationClient==null || !(mLocationClient.isConnected() || mLocationClient.isConnecting())) {
					start();
				}
			}
		}
		return Service.START_STICKY;
	}		
	private void reset() {
		this.stopTimerHeartbeat();
		if(mLocationClient!=null) {
			mLocationClient.disconnect();
			mLocationClient=null;
		}
	}
	private void start() {
	    if (! getLocationManager().isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
	    } else {
			mSettingsManager=new SettingsManager(this);
	        mLocationClient = new LocationClient(this, this, this);		
	        mLocationClient.connect();
	        mPreviousLocation=null;
	    }
	}
	@Override
	public void onDestroy() {
    	reset();		
	}
	
	private void stopTimerHeartbeat() {
		if (mTimer != null) {
			try {
				mTimer.cancel();
				mTimer.purge();
			} catch (Exception e) {
			}
			mTimer = null;
		}
		mPreviousLocation=null;
	}	
	private void startTimerHeartbeat(long trigger, long interval) {
		getTimer().schedule(new TimerTask() {
			public void run() {
				try {
					Intent broadcastIntent = new Intent();
			        broadcastIntent.setAction(GlobalStaticValues.HEARTBEAT_NOTIFICATION);
			        // Broadcast whichever result occurred
			        LocalBroadcastManager.getInstance(TimerService.this).sendBroadcast(broadcastIntent);

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
		Location location=mLocationClient.getLastLocation();
		if(location!=null) {
			doS(location);
		}
		int frequency=mSettingsManager.getHeartbeatFrequency();
		startTimerHeartbeat(1000*frequency,1000*frequency);
	}


	@Override
	public void onDisconnected() {
		reset();
	}	

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		reset();
		new Logger(mSettingsManager.getLoggingLevel(), "TimerService:onConnectionFailed", this)
			.log("Failed connecting to LocationClient. Msg: " +connectionResult.toString(), GlobalStaticValues.LOG_LEVEL_FATAL);
	}	
}
