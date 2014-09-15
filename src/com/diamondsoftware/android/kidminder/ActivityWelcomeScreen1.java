package com.diamondsoftware.android.kidminder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class ActivityWelcomeScreen1 extends Activity {
	ImageButton btnContinue=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitywelcomescreen1);
        btnContinue=(ImageButton)findViewById(R.id.btn_welcome1continue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(ActivityWelcomeScreen1.this,ActivityWelcomeScreen2.class);
				startActivity(intent);
				finish();
			}
		});
    }
}
