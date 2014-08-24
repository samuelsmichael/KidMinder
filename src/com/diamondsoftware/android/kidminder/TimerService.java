package com.diamondsoftware.android.kidminder;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

public class TimerService extends Service implements GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener,
LocationListener  {
	private SettingsManager mSettingsManager;
    private LocationClient mLocationClient;
	private LocationManager mLocationManager = null;
	
    // Global constants
    // Define an object that holds accuracy and frequency parameters
    LocationRequest mLocationRequest;
	
	private LocationManager getLocationManager() {
		if (mLocationManager == null) {
			mLocationManager = (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
		}
		return mLocationManager;
	}	
    @Override
    public void onLocationChanged(Location location) {
		double speed=0;
		// Broadcast message to listeners
		GregorianCalendar gc=new GregorianCalendar(Locale.getDefault());
		gc.setTime(new Date());
		Intent broadcastIntent2 = new Intent();
        broadcastIntent2.setAction(GlobalStaticValues.HEARTBEAT_NOTIFICATION)
        	.putExtra(GlobalStaticValues.KEY_LATESTLOCATION_DATESTAMP,GlobalStaticValues.MDATEFORMAT.format(gc.getTime()))
        	.putExtra(GlobalStaticValues.KEY_LATESTLOCATION_LATITUDE,location.getLatitude())
        	.putExtra(GlobalStaticValues.KEY_LATESTLOCATION_LONGITUDE,location.getLongitude());
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent2);
		
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

    }
	
    @Override
    public void onCreate() {
		mSettingsManager=new SettingsManager(this);
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval
        long updateInterval=(long)((long)mSettingsManager.getHeartbeatFrequency())*(long)GlobalStaticValues.MILLISECONDS_PER_SECOND;
        mLocationRequest.setInterval(updateInterval);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(GlobalStaticValues.FASTEST_INTERVAL);
    		    	

    }
    
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandlerTimer(
				this));
		if(intent!=null) {
			String action=intent.getAction();
			if(action!=null) {
				if(action.equals(GlobalStaticValues.ACTION_STOP)) {
					stop();
				} else {
					if(action.equals(GlobalStaticValues.ACTION_HEARTBEAT_INTERVAL_CHANGED)) {
							stop();
							startIfNotAlreadyEnabled();
					} else {
						if(action.equals(GlobalStaticValues.ACTION_STARTING_FROM_MAINACTIVITY)) {
							startIfNotAlreadyEnabled();
						}
					}
				}
			}
		}
		return Service.START_STICKY;
	}		
	private void stop() {
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
	        mSettingsManager.setLatestLocationDate(new Date());
	        mSettingsManager.setPriorLocation(0, 0);
		}
	}
	private void startIfNotAlreadyEnabled() {
		if(this.mLocationClient==null || !(mLocationClient.isConnected() || mLocationClient.isConnecting())) {
		    if (! getLocationManager().isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
		    	//TODO: Notification that GPS isn't enabled
		    } else {
		    	stop();
		        mLocationClient = new LocationClient(this, this, this);
		        mLocationClient.connect();
		    }
		}
	}
	@Override
	public void onDestroy() {
    	stop();		
	}
	

	public TimerService() {		
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
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
}
