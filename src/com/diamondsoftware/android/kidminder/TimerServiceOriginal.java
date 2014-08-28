package com.diamondsoftware.android.kidminder;

import java.util.Date;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;

public class TimerServiceOriginal extends TimerServiceAbstract {
	private boolean mDrivingFlag=false;
    @Override
    public void onCreate() {
    	super.onCreate();
        mDrivingFlag=false;
    }
	
	private void originalHasLocationProcessing(double speed) {
        if(mDrivingFlag) {
        	if(speed>mSettingsManager.getIsDrivingThreshhold()) {
        		this.resetRestTimerTimeValues();
        	} else {
				long intervalInSeconds=Math.abs((mRestTimerCurrent.getTime() - mTimeWhenRestTimerStarted.getTime())/1000);
				int intervalInMinutes=(int)((float)intervalInSeconds/60f);
				if(intervalInMinutes>=mSettingsManager.getStoppedTimeMinutesBeforeNotification()) {
					mDrivingFlag=false;
					resetRestTimerTimeValues();
					this.stopMyRestTimer();
					alarm();
				}

        	}
        } else {
        	if(mSettingsManager.getCurrentSpeed()>mSettingsManager.getIsDrivingThreshhold()) {
        		this.startMyRestTimer();
        		mDrivingFlag=true;
        	}
        }
		
	}
	@Override
	protected void amMoving(double speed) {
		originalHasLocationProcessing(speed);
	}

	@Override
	protected void amAtRest() {
		originalHasLocationProcessing(0);
	}
	@Override
	protected void performActionEqualsACTION_STARTING_FROM_MAINACTIVITY() {
        mDrivingFlag=false;
	}
	@Override
	protected void restTimerPopped() {
	}
}
