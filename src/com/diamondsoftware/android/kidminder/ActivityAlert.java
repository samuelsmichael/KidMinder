package com.diamondsoftware.android.kidminder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class ActivityAlert extends Activity {
	ImageButton btn_advancedsettings_alert;
	ImageButton btn_closewindow_alert;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			if(GlobalStaticValues.getRingtone(getApplicationContext()).isPlaying()) {
				GlobalStaticValues.getRingtone(getApplicationContext()).stop();
			}
		} catch (Exception e2) {}

		this.setContentView(R.layout.activity_alert);
        setTitle(getString(R.string.app_name)+" - Reminder");
        btn_closewindow_alert=(ImageButton)this.findViewById(R.id.btn_closewindow_alert);
        btn_advancedsettings_alert=(ImageButton)findViewById(R.id.btn_advancedsettings_alert);
        btn_advancedsettings_alert.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent i2 = new Intent(ActivityAlert.this, Preferences.class)
				.putExtra(GlobalStaticValues.KEY_PREFERENCES_TYPE, GlobalStaticValues.myTimerImplementation.toString())
				.putExtra("livevstest", "live");			
			startActivity(i2);
			}
		});
        btn_closewindow_alert.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				ActivityAlert.this.finish();
			}
		});
	}
}
