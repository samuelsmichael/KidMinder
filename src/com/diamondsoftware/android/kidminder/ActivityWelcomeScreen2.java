package com.diamondsoftware.android.kidminder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class ActivityWelcomeScreen2 extends Activity {
	ImageButton btnContinue=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitywelcomescreen2);
        btnContinue=(ImageButton)findViewById(R.id.btn_welcome2continue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ActivityWelcomeScreen2.this.finish();
			}
		});
        
    }
}
