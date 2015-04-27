package com.dealersaleschannel.tv;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ImageActivity extends Activity {
	Intent returnIntent;
	Timer timer;
	Slide slide;
	long remaining;
	long lastUpdate;
	private UtilityFunctions utils = new UtilityFunctions();
	private File dealerTvDir = null;
	private boolean paused = false;

	// private static final int RESULT_SETTINGS = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			setContentView(R.layout.activity_image);
			
			dealerTvDir = utils.getDealerTVDirectory(this.getBaseContext());

			slide = getIntent().getExtras().getParcelable("Slide");

			// Get Layouts
			LinearLayout headerLayout = (LinearLayout) findViewById(R.id.imageLinearLayout1);
			headerLayout.setBackgroundColor(Color
					.parseColor(slide.backgroundColor));
			LinearLayout imageLayout = (LinearLayout) findViewById(R.id.imageLinearLayout2);
			imageLayout.setBackgroundColor(Color
					.parseColor(slide.backgroundColor));
			LinearLayout footerLayout = (LinearLayout) findViewById(R.id.imageLinearLayout3);
			footerLayout.setBackgroundColor(Color
					.parseColor(slide.backgroundColor));

			setHeaderImageViewImageAndLayoutWeight(headerLayout);

			setfooterImageViewImageAndLayoutWeight(footerLayout);

			int displaySeconds = (slide.displayTime == null || slide.displayTime == "") ? 1400
					: (Integer.parseInt(slide.displayTime) * 1000);

			// Required to pause timer when the menu is opened
			remaining = displaySeconds;
			lastUpdate = System.currentTimeMillis();

			timer = new Timer();
			timer.schedule(new TimerTask() {
				public void run() {
					setResult(RESULT_OK, returnIntent);
					finish();
					stopTimer();
				}
			}, displaySeconds);

			ImageView imageView = (ImageView) findViewById(R.id.imageView);

			imageView.setImageURI(Uri.fromFile(new File(slide.data)));

			// Set Image Layout
			float imageLayoutWeight = Math
					.abs(1 - (headerLayout.getWeightSum() + footerLayout
							.getWeightSum()));

			LayoutParams imageLayoutoParams = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, 0, imageLayoutWeight);

			imageLayout.setLayoutParams(imageLayoutoParams);
			imageLayout.setWeightSum(imageLayoutWeight);

			setImageViewScale(imageView);

			returnIntent = new Intent();
			returnIntent.putExtra("result", slide.order);
		} catch (Exception e) {

			slide = getIntent().getExtras().getParcelable("Slide");
			returnIntent = new Intent();
			returnIntent.putExtra("result", slide.order);
			setResult(RESULT_OK, returnIntent);
			finish();
			stopTimer();
		}

	}

	private void setfooterImageViewImageAndLayoutWeight(
			LinearLayout footerLayout) {
		if (slide.footerImage != null && !slide.footerImage.isEmpty()) {
			ImageView imageView = (ImageView) findViewById(R.id.imageFooterImageView);
			imageView.setImageURI(Uri.fromFile(new File(slide.footerImage)));

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;

			// Returns null, sizes are in the options variable
			BitmapFactory.decodeFile(slide.footerImage, options);

			LayoutParams footerLayoutParams = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, 0, (float) options.outHeight
							/ (float) 1080);

			footerLayout.setLayoutParams(footerLayoutParams);
			footerLayout.setWeightSum((float) options.outHeight / (float) 1080);

		} else {
			LayoutParams footerLayoutParams = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, 0, 0f);

			footerLayout.setLayoutParams(footerLayoutParams);
			footerLayout.setWeightSum(0f);
		}
	}

	private void setHeaderImageViewImageAndLayoutWeight(
			LinearLayout headerLayout) {
		if (slide.headerImage != null && !slide.headerImage.isEmpty()) {
			ImageView imageView = (ImageView) findViewById(R.id.imageHeaderImageView);
			imageView.setImageURI(Uri.fromFile(new File(slide.headerImage)));

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;

			// Returns null, sizes are in the options variable
			BitmapFactory.decodeFile(slide.headerImage, options);

			LayoutParams headerLayoutParams = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, 0, (float) options.outHeight
							/ (float) 1080);

			headerLayout.setLayoutParams(headerLayoutParams);
			headerLayout.setWeightSum((float) options.outHeight / (float) 1080);

		} else

		{

			LayoutParams headerLayoutParams = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, 0, 0f);

			headerLayout.setLayoutParams(headerLayoutParams);
			headerLayout.setWeightSum(0f);

		}
	}

	private void setImageViewScale(ImageView imageView) {
		// Scale Scenarios
		// Footer, Header
		if ((slide.headerImage != null && !slide.headerImage.isEmpty())
				&& (slide.footerImage != null && !slide.footerImage.isEmpty())) {
			BitmapFactory.Options headerOptions = new BitmapFactory.Options();
			headerOptions.inJustDecodeBounds = true;

			// Returns null, sizes are in the options variable
			BitmapFactory.decodeFile(slide.headerImage, headerOptions);

			BitmapFactory.Options footerOptions = new BitmapFactory.Options();
			footerOptions.inJustDecodeBounds = true;

			// Returns null, sizes are in the options variable
			BitmapFactory.decodeFile(slide.footerImage, footerOptions);

			BitmapFactory.Options imageOptions = new BitmapFactory.Options();
			imageOptions.inJustDecodeBounds = true;

			// Returns null, sizes are in the options variable
			BitmapFactory.decodeFile(slide.data, imageOptions);

			int totalHeight = headerOptions.outHeight + imageOptions.outHeight
					+ footerOptions.outHeight;

			int totalWidth = headerOptions.outWidth + imageOptions.outWidth
					+ footerOptions.outWidth;

			if (totalWidth == 5760 && totalHeight == 1080) {
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			} else {
				imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			}

		}
		// Header Only
		if ((slide.headerImage != null && !slide.headerImage.isEmpty())
				&& (slide.footerImage == null || slide.footerImage.isEmpty())) {
			BitmapFactory.Options headerOptions = new BitmapFactory.Options();
			headerOptions.inJustDecodeBounds = true;

			// Returns null, sizes are in the options variable
			BitmapFactory.decodeFile(slide.headerImage, headerOptions);

			BitmapFactory.Options imageOptions = new BitmapFactory.Options();
			imageOptions.inJustDecodeBounds = true;

			// Returns null, sizes are in the options variable
			BitmapFactory.decodeFile(slide.data, imageOptions);

			int totalHeight = headerOptions.outHeight + imageOptions.outHeight;

			int totalWidth = headerOptions.outWidth + imageOptions.outWidth;

			if (totalWidth == 3840 && totalHeight == 1080) {
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			} else {
				imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			}
		}

		// Footer Only
		if ((slide.headerImage == null || slide.headerImage.isEmpty())
				&& (slide.footerImage != null && !slide.footerImage.isEmpty())) {
			BitmapFactory.Options footerOptions = new BitmapFactory.Options();
			footerOptions.inJustDecodeBounds = true;

			// Returns null, sizes are in the options variable
			BitmapFactory.decodeFile(slide.footerImage, footerOptions);

			BitmapFactory.Options imageOptions = new BitmapFactory.Options();
			imageOptions.inJustDecodeBounds = true;

			// Returns null, sizes are in the options variable
			BitmapFactory.decodeFile(slide.data, imageOptions);

			int totalHeight = imageOptions.outHeight + footerOptions.outHeight;

			int totalWidth = imageOptions.outWidth + footerOptions.outWidth;

			if (totalWidth == 3840 && totalHeight == 1080) {
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			} else {
				imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			}

		}

		// No Footer No Header
		if ((slide.headerImage == null || slide.headerImage.isEmpty())
				&& (slide.footerImage == null || slide.footerImage.isEmpty())) {
			BitmapFactory.Options imageOptions = new BitmapFactory.Options();
			imageOptions.inJustDecodeBounds = true;

			// Returns null, sizes are in the options variable
			BitmapFactory.decodeFile(slide.data, imageOptions);

			int totalHeight = imageOptions.outHeight;

			int totalWidth = imageOptions.outWidth;

			if (totalWidth == 1920 && totalHeight == 1080) {
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			} else {
				imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			}
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
	public boolean onMenuOpened(int featureId, Menu menu) {
		super.onMenuOpened(featureId, menu);

		pauseActivityTimer();

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
			stopTimer();
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
		resumeActivityTimer();

	}

	private void pauseActivityTimer() {
		long now = System.currentTimeMillis();
		remaining -= (now - lastUpdate);
		stopTimer();
		paused = true;
	}

	private void resumeActivityTimer() {
		lastUpdate = System.currentTimeMillis();

		if (remaining <= 0) {
			setResult(RESULT_OK, returnIntent);
			finish();
		} else {
			timer = new Timer();
			timer.schedule(new TimerTask() {
				public void run() {
					setResult(RESULT_OK, returnIntent);
					finish();
					stopTimer();
				}
			}, ((int) remaining));
		}
		
		paused = false;
	}

	private void stopTimer() {
		if (timer != null) {
			timer.cancel();
			timer.purge();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_ESCAPE:
			/* Sample for handling the Menu button globally */
			openOptionsMenu();
			return true;

		case KeyEvent.KEYCODE_DPAD_RIGHT:
			returnIntent = new Intent();
			returnIntent.putExtra("result", slide.order);
			setResult(RESULT_OK, returnIntent);
			finish();
			stopTimer();

			return true;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			returnIntent = new Intent();
			returnIntent.putExtra("result",
					String.valueOf(Integer.parseInt(slide.order) * -1));
			setResult(RESULT_OK, returnIntent);
			finish();
			stopTimer();
			return true;
		case KeyEvent.KEYCODE_MEDIA_PAUSE:
			pauseActivityTimer();
			Toast pauseToast = Toast.makeText(this,
					"Paused",
					Toast.LENGTH_LONG);
			pauseToast.show();
			
			return true;
		case KeyEvent.KEYCODE_MEDIA_PLAY:
				stopTimer();
				resumeActivityTimer();
				Toast resumeToast = Toast.makeText(this,
						"Resumed",
						Toast.LENGTH_LONG);
				resumeToast.show();
				return true;
				
		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:

			if(paused)
			{
				stopTimer();
				resumeActivityTimer();
				Toast resumeToast2 = Toast.makeText(this,
						"Resumed",
						Toast.LENGTH_LONG);
				resumeToast2.show();
				
			}else
			{
				pauseActivityTimer();
				Toast pauseToast2 = Toast.makeText(this,
						"Paused",
						Toast.LENGTH_LONG);
				pauseToast2.show();
			}
			
			return true;
			
		case KeyEvent.KEYCODE_DPAD_UP:

			if(paused)
			{
				stopTimer();
				resumeActivityTimer();
				Toast resumeToast2 = Toast.makeText(this,
						"Resumed",
						Toast.LENGTH_LONG);
				resumeToast2.show();
				
			}else
			{
				pauseActivityTimer();
				Toast pauseToast2 = Toast.makeText(this,
						"Paused",
						Toast.LENGTH_LONG);
				pauseToast2.show();
			}
			
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

	public Bitmap setImageToImageView(String filePath) {
		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, o);

		// The new size we want to scale to
		final int REQUIRED_SIZE = 1080;

		// Find the correct scale value. It should be the power of 2.
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE)
				break;
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}

		// Decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, o2);
		return bitmap;

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
}
