package com.dealersaleschannel.tv;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

public class VideoActivity extends Activity {

	Intent returnIntent;
	Timer timer;
	Slide slide;
	VideoView videoView;
	private UtilityFunctions utils = new UtilityFunctions();
	private File dealerTvDir = null;


	// private static final int RESULT_SETTINGS = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			setContentView(R.layout.activity_video);

			dealerTvDir = utils.getDealerTVDirectory(this.getBaseContext());
			
			slide = getIntent().getExtras().getParcelable("Slide");

			videoView = (VideoView) findViewById(R.id.VideoView);
			videoView.setVideoURI(Uri.fromFile(new File(slide.data)));
			videoView.setMediaController(new MediaController(this));
			videoView.requestFocus();
			videoView
					.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
						public void onCompletion(MediaPlayer mp) {

							videoView.clearFocus();
							returnIntent = new Intent();
							returnIntent.putExtra("result", slide.order);
							setResult(RESULT_OK, returnIntent);
							finish();
						}
					});

			videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {

				public boolean onError(MediaPlayer mp, int what, int extra) {
					videoView.clearFocus();
					returnIntent = new Intent();
					returnIntent.putExtra("result", slide.order);
					setResult(RESULT_OK, returnIntent);
					finish();

					return true;
				}

			});

			videoView.start();
		} catch (Exception e) {

			slide = getIntent().getExtras().getParcelable("Slide");
			returnIntent = new Intent();
			returnIntent.putExtra("result", slide.order);
			setResult(RESULT_OK, returnIntent);
			finish();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);

		if (Installation.activation(this, "") != null
				&& !Installation.activation(this, "").equals("")) {
			menu.removeItem(R.id.action_activate);
		}
		File configFile = new File(dealerTvDir+File.separator+"config.xml");
		if(!configFile.exists() || DownloadFile.syncing)
		{
			menu.removeItem(R.id.action_sync);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.action_settings) {
			Intent i = new Intent(this, SettingActivity.class);
			// startActivityForResult(i, RESULT_SETTINGS);
			startActivity(i);
		} else if (itemId == R.id.action_restart) {
			returnIntent = new Intent();
			returnIntent.putExtra("result", "restart");
			setResult(RESULT_OK, returnIntent);
			finish();
		} else if (itemId == R.id.action_activate) {
			activateDealerTvApplication();
		} else if (itemId == R.id.action_about) {
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
		}else if (itemId == R.id.action_sync) {
			
			item.setVisible(false);
			this.invalidateOptionsMenu();
			
			syncSlides();			
		}
		return true;
	}

	private void syncSlides()
	{
		final Runnable sync = new Runnable() {
			public void run() {
				try
				{
					String[] params = new String[3];
					
					SharedPreferences settings = PreferenceManager
							.getDefaultSharedPreferences(getBaseContext());
					String channelNumber = settings.getString(
							getString(R.string.pref_channel), "0");
					
					DownloadFile downloadContentFile = new DownloadFile();
					DownloadFile.syncing = true;
					
				File configFile = new File(dealerTvDir+File.separator+"config.xml");							 
				ArrayList<Slide> slides= utils.loadSlidesListFromConfigXml(configFile, dealerTvDir);	
				params[2] = utils.createDownloadParametersCommaSeparatedString(slides,channelNumber,MainActivity.cmsUrl);
				
				if (params[2] != null && !params[2].equals("")) {

					
					downloadContentFile.status = "downloading";
					downloadContentFile.execute(params);

					Log.i("DealerTv", "Sync Download In Progress");
				}else
				{
					DownloadFile.syncing = false;
				}
				}catch(Exception e)
				{
					Log.e("DealerTv", "Synchronization Failed: "+e.getMessage());
					DownloadFile.syncing = false;
				}
				
			}

		};

		// Production
		MainActivity.scheduler.schedule(sync, 1, SECONDS);
	}
	
	private void activateDealerTvApplication() {

		String InstallationId = Installation.id(this);
		String LastKnownLocation = Installation.getLastKnownLocation(this);

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		String channelNumber = settings.getString(
				getString(R.string.pref_channel), "0");

		String userName = settings.getString(getString(R.string.pref_username),
				"");

		String userPassword = settings.getString(
				getString(R.string.pref_password), "");

		// Calendar c = Calendar.getInstance();
		//
		// SimpleDateFormat df = new SimpleDateFormat("dd/mm/yyyy", Locale.US);
		//
		// String today = df.format(c.getTime());

		String url = MainActivity.cmsUrl + "/Api/Activation";

		String result = "";
		String activationId = "";

		try {

			JSONObject activationJsonObj = new JSONObject();
			JSONObject resultJsonObj = null;

			activationJsonObj.put("InstallationId", InstallationId);
			activationJsonObj.put("DealerTvStationId", channelNumber);
			activationJsonObj.put("IsActive", "false");
			// activationJsonObj.put("ActivationDate", today.toString());
			activationJsonObj.put("Field", userName);
			activationJsonObj.put("Field2", userPassword);
			activationJsonObj.put("Location", LastKnownLocation);
			activationJsonObj.put("ActivationId", "");

			resultJsonObj = JsonUtils.sendJson(url, activationJsonObj);

			if (resultJsonObj != null) {
				result = resultJsonObj.getString("Result");
				activationId = resultJsonObj.getString("ActivationId");
			}

		} catch (JSONException e) {

			Log.e("DealerTv", e.getMessage());
		}

		if (result.equals("Pass - Active") && !activationId.equals("")
				&& activationId != null) {
			Installation.activation(this, activationId);
		}

		Toast toast = Toast.makeText(this,
				"Activation Failed - Check Internet Connection",
				Toast.LENGTH_SHORT);

		if (result.equals("Pass - Active") && !activationId.equals("")
				&& activationId != null) {
			Installation.activation(this, activationId);

			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("IsActive", true);
			editor.commit();

			toast = Toast.makeText(this, "Activation Succesful",
					Toast.LENGTH_SHORT);

		} else {

			if (result.equals("Fail - User is not valid")) {
				toast = Toast.makeText(this,
						"Activation Failed - Invalid User Name or Password",
						Toast.LENGTH_SHORT);

				Log.i("DealerTv",
						"Activation Failed - Invalid User Name or Password");
			}
			if (result.equals("Fail - Activation Exists")) {
				toast = Toast.makeText(this,
						"Activation Failed - Activation Exists",
						Toast.LENGTH_SHORT);

				Log.i("DealerTv", "Activation Failed - Activation Exists");
			}

			if (result.equals("Fail - Unknown Error")) {
				toast = Toast
						.makeText(this, "Activation Failed - Unknown Error",
								Toast.LENGTH_SHORT);

				Log.i("DealerTv", "Activation Failed - Unknown Error");
			}

		}

		toast.show();
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		super.onMenuOpened(featureId, menu);
		videoView.pause();

		return true;
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		super.onOptionsMenuClosed(menu);

		if (Installation.activation(this, "") != null
				&& !Installation.activation(this, "").equals("")) {
			menu.removeItem(R.id.action_activate);
		}
		
		File configFile = new File(dealerTvDir+File.separator+"config.xml");
		if(!configFile.exists())
		{
			menu.removeItem(R.id.action_sync);
		}

		videoView.start();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_ESCAPE:
			/* Sample for handling the Menu button globally */
			openOptionsMenu();
			return true;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			videoView.stopPlayback();
			videoView.clearFocus();
			returnIntent = new Intent();
			returnIntent.putExtra("result", slide.order);
			setResult(RESULT_OK, returnIntent);
			finish();

			return true;

		case KeyEvent.KEYCODE_DPAD_LEFT:
			videoView.clearFocus();
			returnIntent = new Intent();
			returnIntent.putExtra("result",
					String.valueOf(Integer.parseInt(slide.order) * -1));
			setResult(RESULT_OK, returnIntent);
			finish();
			return true;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			Toast infoToast = Toast.makeText(this,
					String.format("Slide Name: %s\nContent Type: %s \nSlide Order #: %s", slide.name,slide.contentType,slide.order),
					Toast.LENGTH_LONG);
			infoToast.show();
	
			return true;	

			

		}
		return false;
	}

}
