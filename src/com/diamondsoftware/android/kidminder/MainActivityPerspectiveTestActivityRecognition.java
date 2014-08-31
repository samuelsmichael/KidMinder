package com.diamondsoftware.android.kidminder;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class MainActivityPerspectiveTestActivityRecognition extends
		MainActivity {
	private Switch mSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_perspectivetestlocationservice);
        
        mSwitch=(Switch)findViewById(R.id.enabledSwitch);
        mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(!isChecked) {
					pressedDisableButton();
				} else {
					pressedEnableButton();
				}
				MainActivityPerspectiveTestActivityRecognition.this.onResumeManageView();
			}
        	
        });
    }
	@Override
	protected void onResumeManageView() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void stopTimer() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void pressedEnableButton() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void pressedDisableButton() {
		// TODO Auto-generated method stub

	}

}
