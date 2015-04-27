package com.dealersaleschannel.tv;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class WeatherRadarActivity extends Activity {

	Intent returnIntent;
	Timer timer;
	long remaining;
	long lastUpdate;
	Slide slide;
	JSONObject jsonWeatherObject;
	private static final int RESULT_SETTINGS = 1;
	// private static final String weatherApiKey = "6bdaf79ec852d0b0";
	// private String zipCode = "04401";
	private String cmsUrl;
	private File weatherDir;
	private UtilityFunctions utils = new UtilityFunctions();
	private File dealerTvDir = null;
	private boolean paused = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			
			dealerTvDir = utils.getDealerTVDirectory(this.getBaseContext());
			
			slide = getIntent().getExtras().getParcelable("Slide");
			// zipCode = getIntent().getExtras().getString("ZipCode");
			cmsUrl = MainActivity.cmsUrl;

			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(getBaseContext());

			String channelNumber = settings.getString(
					getString(R.string.pref_channel), "0");

			int zoomLevel = Integer.parseInt(settings.getString(
					getString(R.string.pref_webViewZoomLevel), "100"));

			cmsUrl = cmsUrl + File.separator + "Api/Weather" + File.separator
					+ channelNumber;

			weatherDir = new File(dealerTvDir.getAbsolutePath()
					+ File.separator + "weather");

			setContentView(R.layout.activity_weather);

			// Get Layouts
			LinearLayout headerLayout = (LinearLayout) findViewById(R.id.weatherLinearLayout1);
			// headerLayout
			// .setBackgroundColor(Color.parseColor(slide.backgroundColor));
			LinearLayout textLayout = (LinearLayout) findViewById(R.id.weatherLinearLayout2);
			// textLayout.setBackgroundColor(Color.parseColor(slide.backgroundColor));
			LinearLayout footerLayout = (LinearLayout) findViewById(R.id.weatherLinearLayout3);
			// footerLayout
			// .setBackgroundColor(Color.parseColor(slide.backgroundColor));

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

			returnIntent = new Intent();
			returnIntent.putExtra("result", slide.order);

			WebView webView = (WebView) findViewById(R.id.weatherWebView);

			webView.setWebViewClient(new WebViewClient() {
				@Override
				public void onReceivedError(WebView view, int errorCode,
						String description, String failingUrl) {
					super.onReceivedError(view, errorCode, description,
							failingUrl);
					setResult(RESULT_OK, returnIntent);
					finish();
					stopTimer();
				}
			});

			webView.setBackgroundColor(0x00000000);
			webView.setHorizontalScrollBarEnabled(false);
			webView.setVerticalScrollBarEnabled(false);
			webView.setPadding(0, 0, 0, 0);
			webView.getSettings().setJavaScriptEnabled(false);
			// webView.getSettings().setBuiltInZoomControls(false);
			webView.setHorizontalScrollBarEnabled(false);
			webView.setVerticalScrollBarEnabled(false);
			webView.getSettings().setUseWideViewPort(false);
			webView.getSettings().setSupportZoom(false);
			webView.setInitialScale(zoomLevel);

			// String infoTable = CreateWeatherInfoTable();
			// String url
			// ="http://api.wunderground.com/api/"+weatherApiKey+"/animatedradar/q/"+zipCode+".gif?newmaps=1&timelabel=1&timelabel.y=10&num=5&delay=50&width=1920&height=1080";

			File[] radarFiles = weatherDir.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String filename) {
					return filename.startsWith("radar")
							&& !filename.endsWith(".Temp") && !isGifCorrupted(dir.getAbsolutePath()+File.separator+filename) ;
				}
			});

			if (radarFiles.length >= 1) {
				// Order By Creation Date, Older first
				Arrays.sort(radarFiles, new Comparator<File>() {
					public int compare(File f1, File f2) {
						return Long.valueOf(f1.lastModified()).compareTo(
								f2.lastModified());
					}
				});

				// long expirationDate = System.currentTimeMillis() - (11 * 60 *
				// 1000);
				//
				try {
					// Get File Last Modified Date of
					File latestRadarFile = radarFiles[radarFiles.length-1];
					//
					// if(latestRadarFile.lastModified() > expirationDate)
					// {
					// webView.loadUrl("file:///"+latestRadarFile.getAbsolutePath());
					String dir = latestRadarFile.getParent() + File.separator;

					String name = latestRadarFile.getName();

					webView.loadDataWithBaseURL(
							"file:///" + dir,
							"<html><head><meta content=\"off\" name=\"gtv-autozoom\"></head><body><img src=\""
									+ name
									+ "\" width=\"1920\" height=\"1080\" /></body></html>",
							"text/html", "UTF-8", null);

				} catch (Exception e) {
					Log.e("Dealer Tv: ", e.getMessage());

					setResult(RESULT_OK, returnIntent);
					finish();
					stopTimer();
				}
				// }else
				// {
				// JSONObject radarJsonContainer = JsonUtils
				// .SendHttpGet(cmsUrl+File.separator+"radar");
				// JSONObject radarJson = null;
				// try {
				// radarJson = new
				// JSONObject(radarJsonContainer.getString("Json"));
				// } catch (JSONException e) {
				// Log.e("DealerTv", "Weather radar exception: " +
				// e.getLocalizedMessage());
				// }
				//
				// String radarGIFfileName = "";
				// try {
				// radarGIFfileName = radarJson.getString("fileName");
				// } catch (JSONException e) {
				// Log.e("DealerTv", "Weather radar exception: " +
				// e.getLocalizedMessage());
				// }
				//
				//
				// String url = String.format("%s%s%s%s%s%s%s%s%s",
				// MainActivity.cmsUrl,File.separator,"Channels",File.separator,channelNumber,File.separator,"weatherImages",File.separator,radarGIFfileName);
				//
				// webView.loadUrl(url);
				// }
			} else {
				JSONObject radarJsonContainer = JsonUtils.SendHttpGet(cmsUrl
						+ File.separator + "radar");
				JSONObject radarJson = null;
				try {
					radarJson = new JSONObject(
							radarJsonContainer.getString("Json"));
				} catch (JSONException e) {
					Log.e("DealerTv",
							"Weather radar exception: "
									+ e.getLocalizedMessage());
				}

				String radarGIFfileName = "";
				try {
					radarGIFfileName = radarJson.getString("fileName");
				} catch (JSONException e) {
					Log.e("DealerTv",
							"Weather radar exception: "
									+ e.getLocalizedMessage());
				}

				String url = String.format("%s%s%s%s%s%s%s%s%s",
						MainActivity.cmsUrl, File.separator, "Channels",
						File.separator, channelNumber, File.separator,
						"weatherImages", File.separator, radarGIFfileName);

				webView.loadUrl(url);
				webView.loadDataWithBaseURL(
						url,
						"<html><head><meta content=\"off\" name=\"gtv-autozoom\"></head><body><img src=\""
								+ radarGIFfileName
								+ "\" width=\"1920\" height=\"1080\" /></body></html>",
						"text/html", "UTF-8", null);
			}

			webView.requestFocus();
			webView.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_ESCAPE) {
						openOptionsMenu();
						return true;
					}

					if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
						returnIntent = new Intent();
						returnIntent.putExtra("result", slide.order);
						setResult(RESULT_OK, returnIntent);
						finish();
						stopTimer();

						return true;
					}
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						returnIntent = new Intent();
						returnIntent.putExtra(
								"result",
								String.valueOf(Integer.parseInt(slide.order)
										* -1));
						setResult(RESULT_OK, returnIntent);
						finish();
						stopTimer();

						return true;
					}
					if (keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
						pauseActivityTimer();
						Toast pauseToast = Toast.makeText(v.getContext(),
								"Paused",
								Toast.LENGTH_LONG);
						pauseToast.show();
						
						return true;
					
					}
					if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
						
						stopTimer();
						resumeActivityTimer();
						Toast resumeToast = Toast.makeText(v.getContext(),
								"Resumed",
								Toast.LENGTH_LONG);
						resumeToast.show();
			
					return true;
					}
					if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
						
						Toast infoToast = Toast.makeText(v.getContext(),
								String.format("Slide Name: %s\nContent Type: %s \nSlide Order #: %s", slide.name,slide.contentType,slide.order),
								Toast.LENGTH_LONG);
						infoToast.show();
				
						return true;
					}
					if(keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
						
						if(paused)
						{
							stopTimer();
							resumeActivityTimer();
							Toast resumeToast2 = Toast.makeText(v.getContext(),
									"Resumed",
									Toast.LENGTH_LONG);
							resumeToast2.show();
							
						}else
						{
							pauseActivityTimer();
							Toast pauseToast2 = Toast.makeText(v.getContext(),
									"Paused",
									Toast.LENGTH_LONG);
							pauseToast2.show();
						}
						
						return true;
					}
					
					if(keyCode == KeyEvent.KEYCODE_DPAD_UP) {
	
						if(paused)
						{
							stopTimer();
							resumeActivityTimer();
							Toast resumeToast2 = Toast.makeText(v.getContext(),
									"Resumed",
									Toast.LENGTH_LONG);
							resumeToast2.show();
							
						}else
						{
							pauseActivityTimer();
							Toast pauseToast2 = Toast.makeText(v.getContext(),
									"Paused",
									Toast.LENGTH_LONG);
							pauseToast2.show();
						}
						
						return true;
					}
						

					return false;
				}
			});

			// Set Text Layout
			float textLayoutWeight = Math
					.abs(1 - (headerLayout.getWeightSum() + footerLayout
							.getWeightSum()));

			LayoutParams textLayoutoParams = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, 0, textLayoutWeight);

			textLayout.setLayoutParams(textLayoutoParams);
			textLayout.setWeightSum(textLayoutWeight);
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
			ImageView imageView = (ImageView) findViewById(R.id.weatherFooterImageView);
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
			ImageView imageView = (ImageView) findViewById(R.id.weatherHeaderImageView);
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case RESULT_SETTINGS:

			break;
		}
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		super.onMenuOpened(featureId, menu);

		pauseActivityTimer();

		return true;
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
		case KeyEvent.KEYCODE_DPAD_DOWN:
			Toast infoToast = Toast.makeText(this,
					String.format("Slide Name: %s\nContent Type: %s \nSlide Order #: %s", slide.name,slide.contentType,slide.order),
					Toast.LENGTH_LONG);
			infoToast.show();
	
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
		}
		return false;
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
	
	private boolean isGifCorrupted(String fileName)
	{
		File file = new File(fileName);
		
		byte[] firstThreeBytes = toByteArray(file, 0, 3);
		
		if(firstThreeBytes[0] == 71 && firstThreeBytes[1] == 73  && firstThreeBytes[2] == 70)
		{
			return false;
		}
		
		return true;
		
	}
	
	private static byte[] toByteArray(File file, int offset, int count){
	    
	    byte[] bytes = new byte[count];
	    
	    try {
	        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
	        buf.read(bytes, offset, count);
	        buf.close();
	        
	        
	    } catch (FileNotFoundException e) {
	        Log.e("DealerTv","Radar File Error: "+e.getMessage());
	        
	    } catch (IOException e) {
	    	Log.e("DealerTv","Radar File Error: "+e.getMessage());
	    }
	    
	    return bytes;
		
	}

}
