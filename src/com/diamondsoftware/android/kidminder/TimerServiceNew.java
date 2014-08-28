package com.diamondsoftware.android.kidminder;

public class TimerServiceNew extends TimerServiceAbstract {
	private boolean mWasMoving=false;

	@Override
	protected void amMoving(double speed) {
		stopMyRestTimer();
		mWasMoving=true;
	}

	@Override
	protected void amAtRest() {
		if(mWasMoving) {
			startMyRestTimer();
			mWasMoving=false;
		}
	}
	
	@Override
	protected void restTimerPopped() {
		long intervalInSeconds=Math.abs((mRestTimerCurrent.getTime() - mTimeWhenRestTimerStarted.getTime())/1000);
		int intervalInMinutes=(int)((float)intervalInSeconds/60f);
		if(intervalInMinutes>=mSettingsManager.getStoppedTimeMinutesBeforeNotification()) {
			mWasMoving=false;
			this.stopMyRestTimer();
			resetRestTimerTimeValues();
			alarm();
		}		
	}

	@Override
	protected void performActionEqualsACTION_STARTING_FROM_MAINACTIVITY() {
	}

}
