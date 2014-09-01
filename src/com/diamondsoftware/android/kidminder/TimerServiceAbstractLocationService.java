package com.diamondsoftware.android.kidminder;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

public abstract class TimerServiceAbstractLocationService extends TimerServiceAbstract  implements GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener,
com.google.android.gms.location.LocationListener {
	protected abstract void amMoving(double speed);
	protected abstract void amAtRest();
	protected abstract void performActionEqualsACTION_STARTING_FROM_MAINACTIVITY();
	
    int mNoreentry2=0;
    private LocationClient mLocationClient;
	protected int mJeDisSimulation=0;
    private LocationRequest mLocationRequest;

    @Override
    public void onCreate() {
    	super.onCreate();
    	
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval
        long updateInterval=(long)((long)mSettingsManager.getHeartbeatFrequency())*(long)GlobalStaticValues.MILLISECONDS_PER_SECOND;
        mLocationRequest.setInterval(updateInterval);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(GlobalStaticValues.FASTEST_INTERVAL);
        mSettingsManager.setCurrentSpeed(0);
        mSettingsManager.setCurrentRestTime(0);
    }
    

    
	protected void stop() {
		if (mLocationClient!=null) {
	        if (mLocationClient.isConnected()) {
	            /*
	             * Remove location updates for a listener.
	             * The current Activity is the listener, so
	             * the argument is "this".
	             */
	        	mLocationClient.removeLocationUpdates(this);
	            mLocationClient.disconnect();	
	        }
		}
		this.stopMyRestTimer();
		if(mSettingsManager.getCurrentSimilationStatus()) {
			mJeDisSimulation=-1;
		}
        mSettingsManager.setLatestLocationDate(new Date());
        mSettingsManager.setPriorLocation(0, 0);
        mSettingsManager.setCurrentSpeed(0);
	}
	private int mDontReenter=0;
	private void startIfNotAlreadyEnabled() {
		if(mDontReenter==0) {
			mDontReenter=1;
			if(this.mLocationClient==null || !(mLocationClient.isConnected() || mLocationClient.isConnecting())) {
			    if (! isGPSAlive() ) {
					notifyActivityThatGPSIsNotOn();
			    } else {
			    	stop();
			        long updateInterval=(long)((long)mSettingsManager.getHeartbeatFrequency())*(long)GlobalStaticValues.MILLISECONDS_PER_SECOND;
			        mLocationRequest.setInterval(updateInterval);

			        mLocationClient = new LocationClient(this, this, this);
			        mLocationClient.connect();
			    }
			}
			mDontReenter=0;
		}
	}
    

    @Override
    public void onLocationChanged(Location location) {
	    if(mNoreentry2==0) {
	    	mNoreentry2=1;
			double speed=0;
			// Broadcast message to listeners
			GregorianCalendar gc=new GregorianCalendar(Locale.getDefault());
			gc.setTime(new Date());
			Intent broadcastIntent2 = new Intent();
	        broadcastIntent2.setAction(GlobalStaticValues.NOTIFICATION_HEARTBEAT)
	        	.putExtra(GlobalStaticValues.KEY_LATESTLOCATION_DATESTAMP,GlobalStaticValues.MDATEFORMAT.format(gc.getTime()))
	        	.putExtra(GlobalStaticValues.KEY_LATESTLOCATION_LATITUDE,location.getLatitude())
	        	.putExtra(GlobalStaticValues.KEY_LATESTLOCATION_LONGITUDE,location.getLongitude());
	        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent2);
			
			if(location.hasSpeed()||(true&&mSettingsManager.getCurrentSimilationStatus())) {
				if(mSettingsManager.getCurrentSimilationStatus()) {
					if(mJeDisSimulation<5) {
						speed=10;
					} else {
						speed=0;
					}
					mJeDisSimulation++;
				} else {
					speed=(double)location.getSpeed();				
				}
				speed=(speed*(double)3600)/1609.34;
				Intent broadcastIntent = new Intent();
		        broadcastIntent.setAction(GlobalStaticValues.NOTIFICATION_GOTSPEED);
		        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
			} 
	        mSettingsManager.setCurrentSpeed(speed);
			Intent broadcastIntent = new Intent();
	        broadcastIntent.setAction(GlobalStaticValues.NOTIFICATION_SPEED)
	        	.putExtra("speed", speed);
	        // Broadcast whichever result occurred
	        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        	if(speed>mSettingsManager.getIsDrivingThreshhold()) {
	        	amMoving(speed);
        	} else {
        		amAtRest();
        	}
	        mNoreentry2=0;
	    }
    }
    
	
	@Override
	public void onConnected(Bundle arg0) {
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}


	@Override
	public void onDisconnected() {
	}	

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		new Logger(mSettingsManager.getLoggingLevel(), "TimerService:onConnectionFailed", this)
			.log("Failed connecting to LocationClient. Msg: " +connectionResult.toString(), GlobalStaticValues.LOG_LEVEL_FATAL);
	}	
	protected void doACTION_STOP() {
		stop();
	}
	protected void doACTION_HEARTBEAT_INTERVAL_CHANGED() {
		stop();
		startIfNotAlreadyEnabled();
	}
	protected void doACTION_STARTING_FROM_MAINACTIVITY() {
		resetRestTimerTimeValues();
		performActionEqualsACTION_STARTING_FROM_MAINACTIVITY();
		startIfNotAlreadyEnabled();
	}
	protected void doACTION_STARTING_FROM_BOOTUP() {
		mSettingsManager.setIsEnabled(true);  // force enabled when starting from bootup
		startIfNotAlreadyEnabled();
	}

}
