package com.diamondsoftware.android.kidminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

public class TimerServiceLocationManagerHelper implements android.location.LocationListener  {
	private LocationManager mLocationManager;
	private DoesTimerServiceFunctions mTimerService;
	private static final String ACTION_GPS = "android.location.PROVIDERS_CHANGED";
	private BroadcastReceiver yourReceiver;
	
	public TimerServiceLocationManagerHelper(DoesTimerServiceFunctions timerService) {
		mTimerService=timerService;
		this.initializeLocationManager();
	}

	public LocationManager getLocationManager() {
		return mLocationManager;
	}	

	
	//---------------------------------------  LocationManager used to kick-start GPS, and also used to know when user turns off GPS -----------------------------------------
    protected void initializeLocationManager() {/*bbhbb
        try {
        	mLocationManager = (android.location.LocationManager) mTimerService.getSystemService(Context.LOCATION_SERVICE);
        	String provider=LocationManager.GPS_PROVIDER;
            if(mLocationManager.isProviderEnabled(provider)) {
                mLocationManager.requestLocationUpdates(provider, 0, 0, this);
            }
    		registerReceiverGPS();

        } catch (Exception ee3) {
        }
        bbhbb */
    }
    private void registerReceiverGPS() {
    	
       // if (yourReceiver == null) {
            // INTENT FILTER FOR GPS MONITORING
        //    final IntentFilter theFilter = new IntentFilter();
          //  theFilter.addAction(ACTION_GPS);
//            yourReceiver = new BroadcastReceiver() {
  //              @Override
    //            public void onReceive(Context context, Intent intent) {
      //              if (intent != null) {
        //                String s = intent.getAction();
          //              if (s != null) {
            //                if (s.equals(ACTION_GPS)) {
              //                  checkGPS();
                //            }
                  //      }
                    //}
//                }
  //          };
    //        mTimerService.registerReceiver(yourReceiver, theFilter);
      //  }
    }
    private void checkGPS() { /* bbhbb
    	if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			mTimerService.notifyActivityThatGPSIsNotOn();
    	} else {
    		mTimerService.gpsIsBackOn();
    	}
    	bbhbb */
    }
    public boolean isGPSAlive() {
    	return true; /* bbhbb
    	return mLocationManager.isProviderEnabled( LocationManager.GPS_PROVIDER );
    	bbhbb */	
    }
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
	@Override
	public void onProviderEnabled(String provider) {
	}
	@Override
	public void onProviderDisabled(String provider) {
	}
	@Override
	public void onLocationChanged(Location location) {
	}
	public void onDestroy() { /* bbhbb
		mLocationManager.removeUpdates(this);
	    if (yourReceiver != null) {
	        mTimerService.unregisterReceiver(yourReceiver);
	        yourReceiver = null;
	        
	    }*/
	}

}
