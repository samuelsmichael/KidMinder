package com.diamondsoftware.android.kidminder;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

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
    private boolean mImConnected2=false;
	private LocationManager mLocationManager = null;
	private Location mPreviousLocation=null;
	private Calendar mPreviousCalendar=null;
	
    boolean mUpdatesRequested;
	
    // Global constants
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
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
		Intent broadcastIntent2 = new Intent();
        broadcastIntent2.setAction(GlobalStaticValues.HEARTBEAT_NOTIFICATION);
        // Broadcast whichever result occurred
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
							reset();
							start();
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
        if (mLocationClient.isConnected()) {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
  //          this.getLocationManager().removeLocationUpdates(this);
        }
        /*
         * After disconnect() is called, the client is
         * considered "dead".
         */
        mLocationClient.disconnect();	
		
	}
	private void start() {
	    if (! getLocationManager().isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
	    } else {
	    	
	        mLocationRequest = LocationRequest.create();
	        // Use high accuracy
	        mLocationRequest.setPriority(
	                LocationRequest.PRIORITY_HIGH_ACCURACY);
	        // Set the update interval to 5 seconds
	        mLocationRequest.setInterval(UPDATE_INTERVAL);
	        // Set the fastest update interval to 1 second
	        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
	    	
	    	
			mSettingsManager=new SettingsManager(this);
	        mLocationClient = new LocationClient(this, this, this);
	        // Start with updates turned off
	        mUpdatesRequested = false;
	        mLocationClient.connect();
	    }
	}
	@Override
	public void onDestroy() {
    	reset();		
	}
	
	private void stopTimerHeartbeat() {
		mPreviousLocation=null;
	}	
	private void startTimerHeartbeat(long trigger, long interval) {
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
		reset();
	}	

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		reset();
		new Logger(mSettingsManager.getLoggingLevel(), "TimerService:onConnectionFailed", this)
			.log("Failed connecting to LocationClient. Msg: " +connectionResult.toString(), GlobalStaticValues.LOG_LEVEL_FATAL);
	}	
}
