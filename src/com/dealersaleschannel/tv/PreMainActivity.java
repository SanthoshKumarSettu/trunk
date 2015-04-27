package com.dealersaleschannel.tv;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class PreMainActivity extends Activity {
	 public static Boolean ENABLE_RESTART = false;
	 
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  ENABLE_RESTART = true;
	  restartMain();
	 }
	 
	 @Override
	 public void onRestart() {
	  super.onRestart();
	  restartMain();
	 }
	   
	 public void restartMain(){
	  if(ENABLE_RESTART == true){
	   Intent mainIntent = new Intent(this, MainActivity.class);
	   mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP  );
	   startActivity(mainIntent);
	   finish();
	  }else{
	   finish();
	  }
	  ENABLE_RESTART   = false;
	 }    
	}
