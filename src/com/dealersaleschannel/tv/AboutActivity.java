package com.dealersaleschannel.tv;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

public class AboutActivity extends Activity 
{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		//Get Activation Information
		String about = "Installation ID: " + Installation.id(this);
		about = about +"\n\nActivation ID: " + Installation.activation(this, Installation.id(this));
		about = about +"\n\n\n";
		

		TextView textView = (TextView) findViewById(R.id.about);
		textView.setText(about);
		textView.setBackgroundColor(Color.parseColor("#000000"));
		textView.setTextColor(Color.parseColor("#FFFFFF"));
		textView.setTextSize(Float.parseFloat("20"));
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
	    if ((keyCode == KeyEvent.KEYCODE_BACK))
	    {
	        finish();
	    }
	    return super.onKeyDown(keyCode, event);
	}

}
