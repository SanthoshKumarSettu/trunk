package com.dealersaleschannel.tv;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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

public class WeatherActivity extends Activity {

	Intent returnIntent;
	Timer timer;
	long remaining;
	long lastUpdate;
	Slide slide;
	JSONObject jsonWeatherObject;
	private static final int RESULT_SETTINGS = 1;
	// private static final String weatherApiKey = "6bdaf79ec852d0b0";
	// private static final String zipCode = "04401";
	private String cmsUrl;
	public String infoTable = "";
	private File weatherDir;
	private UtilityFunctions utils = new UtilityFunctions();
	private File dealerTvDir = null;
	private boolean paused = false;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			
			dealerTvDir = utils.getDealerTVDirectory(this.getBaseContext());
			
			slide = getIntent().getExtras().getParcelable("Slide");

			
			cmsUrl = MainActivity.cmsUrl;
			weatherDir = new File(dealerTvDir.getAbsolutePath()
					+ File.separator + "weather");

			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(getBaseContext());

			String channelNumber = settings.getString(
					getString(R.string.pref_channel), "0");

			int zoomLevel = Integer.parseInt(settings.getString(
					getString(R.string.pref_webViewZoomLevel), "100"));

			cmsUrl = cmsUrl + File.separator + "Api/Weather" + File.separator
					+ channelNumber;

			setContentView(R.layout.activity_weather);

			// //Main Layout
			// LinearLayout mainLayout = (LinearLayout)
			// findViewById(R.id.activity_weather);
			// mainLayout.setVisibility(View.INVISIBLE);

			// Get Layouts
			LinearLayout headerLayout = (LinearLayout) findViewById(R.id.weatherLinearLayout1);
			// headerLayout
			// .setBackgroundColor(Color.parseColor(slide.backgroundColor));
			LinearLayout textLayout = (LinearLayout) findViewById(R.id.weatherLinearLayout2);
			textLayout.setBackgroundResource(R.drawable.bg);
			// textLayout.setBackgroundColor(Color.parseColor("#3333FF"));
			LinearLayout footerLayout = (LinearLayout) findViewById(R.id.weatherLinearLayout3);
			// footerLayout
			// .setBackgroundColor(Color.parseColor(slide.backgroundColor));

			setHeaderImageViewImageAndLayoutWeight(headerLayout);

			setfooterImageViewImageAndLayoutWeight(footerLayout);

			// Set Text Layout
			float textLayoutWeight = Math
					.abs(1 - (headerLayout.getWeightSum() + footerLayout
							.getWeightSum()));

			LayoutParams textLayoutoParams = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, 0, textLayoutWeight);

			textLayout.setLayoutParams(textLayoutoParams);
			textLayout.setWeightSum(textLayoutWeight);

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

			// webView.setVisibility(View.INVISIBLE);
			// webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			// webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
			// webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

			webView.getSettings().setJavaScriptEnabled(true);
			// webView.getSettings().setBuiltInZoomControls(false);
			webView.setHorizontalScrollBarEnabled(false);
			webView.setVerticalScrollBarEnabled(false);
			webView.getSettings().setUseWideViewPort(false);
			webView.setPadding(0, 0, 0, 0);
			webView.setInitialScale(zoomLevel);
			webView.setBackgroundColor(0x00000000);
			webView.getSettings().setSupportZoom(false);

			// webView.setBackgroundResource(R.drawable.bg);
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

			File[] weatherFiles = weatherDir.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String filename) {
					return filename.startsWith("weather")
							&& !filename.endsWith(".Temp");
				}
			});

			// Get Latest File
			if (weatherFiles.length >= 1) {
				// Order By Creation Date, Older first
				Arrays.sort(weatherFiles, new Comparator<File>() {
					public int compare(File f1, File f2) {
						return Long.valueOf(f1.lastModified()).compareTo(
								f2.lastModified());
					}
				});

				try {
					File latestWeatherFile = weatherFiles[weatherFiles.length - 1];

					infoTable = WeatherCacheControl
							.readWeatherFile(latestWeatherFile);
				} catch (IOException e) {
					Log.e("Dealer Tv: ", e.getMessage());
				}
			}

			if (infoTable.isEmpty() || !infoTable.endsWith("</html>"))
				infoTable = CreateWeatherInfoTable(cmsUrl,
						this.getBaseContext());

			if (infoTable.isEmpty() || !infoTable.endsWith("</html>")) {
				setResult(RESULT_OK, returnIntent);
				finish();
				stopTimer();
			}

			webView.loadDataWithBaseURL("file:///android_asset/weather/",
					infoTable, "text/html", "UTF-8", null);

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

	public static String CreateWeatherInfoTable(String cmsUrl, Context context) {

		StringBuilder infoTable = new StringBuilder();

		WeatherAlerts weatherAlerts = new WeatherAlerts(cmsUrl, context);
		// WeatherAlerts weatherAlerts = new WeatherAlerts(weatherApiKey,
		// zipCode);

		WeatherForecast weatherForecast = new WeatherForecast(cmsUrl, context);
		// WeatherForecast weatherForecast = new
		// WeatherForecast(weatherApiKey,zipCode);
		WeatherCurrentConditions currentConditions = new WeatherCurrentConditions(
				cmsUrl, context);
		// WeatherCurrentConditions currentConditions = new
		// WeatherCurrentConditions(weatherApiKey,zipCode);

		infoTable
				.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
		infoTable.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		infoTable.append("<head>");
		infoTable.append("<meta content=\"off\" name=\"gtv-autozoom\">");
		infoTable
				.append("<meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\" />");
		infoTable
				.append("<link href=\"styles.css\" rel=\"stylesheet\" type=\"text/css\" />");
		// infoTable.append("<script type=\"text/javascript\" src=\"jquery-2.0.3.min.js\"></script>");
		// infoTable.append("<script type=\"text/javascript\">");
		// infoTable.append("$(document).ready(function()");
		// infoTable.append("{");
		// infoTable.append("var w = screen.width;");
		// infoTable.append("var h = screen.height;");
		// infoTable.append("var bw = $(window).width();");
		// infoTable.append("var bh = $(window).height();");
		// infoTable.append("var wRatio = bw/w;");
		// infoTable.append("var hRatio = bh/h;");
		// infoTable.append("var ratio = (wRatio + hRatio) / 2.05;");
		// //infoTable.append("var ratio = 0.3;");
		// infoTable.append("$('body').css('zoom', ratio);");
		// infoTable.append("});");
		// infoTable.append("</script>");
		infoTable.append("</head>");
		infoTable.append("<body>");
		// infoTable.append("<div class=\"zoom-control\">");
		infoTable
				.append(String
						.format("<div id=\"locale-container\"><div class=\"location-holder\"><h1>%s</h1></div></div>",
								currentConditions.getLocation()));
		infoTable.append("<!-- Now_Weather_Starts -->");
		infoTable.append("<div id=\"nowweather-holder\">");
		infoTable.append("<div class=\"col1 align-left\">");
		infoTable.append("<h2 class=\"now\">NOW</h2>");
		infoTable
				.append(String
						.format("<img style=\"margin-top:-65px;z-index:2000;position:relative;\" src=\"%s\" width=\"301\" height=\"241\" />",
								currentConditions.getWeatherImage()));
		infoTable.append("</div>");
		infoTable.append("<div class=\"col2 align-right\">");
		infoTable
				.append(String
						.format("<h2 class=\"now-temp\">%s<span class=\"smaller\">&deg;F</span></h2>",
								currentConditions.getTemperature()));
		infoTable.append(String.format(
				"<span class=\"now-forecast-ylw\">%s</span>",
				currentConditions.getWeather()));
		// infoTable.append(String.format(
		// "<span class=\"now-feels\">Feels Like: %s&deg;F</span>",
		// currentConditions.getFeelsLikeTemperature()));
		infoTable.append("</div>");
		infoTable.append("<div class=\"col3 align-left\">");
		infoTable.append("<table style=\"height:100%;width:100%;\">");
		infoTable.append("<tr>");
		infoTable.append(String.format(
				"<td colspan=\"2\">Feels Like %s&deg;F</td>",
				currentConditions.getFeelsLikeTemperature()));
		infoTable.append("</tr>");
		infoTable.append("<tr>");
		infoTable.append(String.format("<td colspan=\"2\">Wind %s</td>",
				currentConditions.getWindInfo()));
		infoTable.append("</tr>");
		infoTable.append("<tr>");
		infoTable.append(String.format("<td>Sunrise&nbsp;%s</td>",
				currentConditions.getSunriseTime()));
		infoTable.append(String.format("<td>Humidity&nbsp;%s</td>",
				currentConditions.getHumidity()));
		infoTable.append("</tr>");
		infoTable.append("<tr>");
		infoTable.append(String.format(
				"<td style=\"height:38px !important;\">Sunset&nbsp;%s</td>",
				currentConditions.getSunsetTime()));
		infoTable
				.append(String
						.format("<td style=\"height:38px !important;\">Pressure&nbsp;%s&nbsp;in.</td>",
								currentConditions.getPressure()));
		infoTable.append("</tr>");
		infoTable.append("</table>");
		infoTable.append("</div>");
		infoTable.append("<div class=\"clear-fix\"></div>");
		infoTable.append("</div>");
		infoTable.append("<!-- Now_Weather_Ends -->");

		infoTable.append("<!-- Forecast_Starts -->");
		infoTable.append("<div id=\"forecast-holder\">");

		for (WeatherForecastDay currentForecastDay : weatherForecast
				.getWeather10DayForecast()) {

			if (weatherForecast.getWeather10DayForecast().indexOf(
					currentForecastDay) == 5) {
				infoTable.append("<div class=\"forecast-bx-last\">");
			} else {
				infoTable.append("<div class=\"forecast-bx\">");
			}

			infoTable.append(String.format("<div class=\"day-head\">%s</div>",
					currentForecastDay.getDay()));
			infoTable.append("<div class=\"forecast-content\">");
			infoTable
					.append(String
							.format("<img style=\"margin:0 auto 0 auto;\" src=\"%s\" width=\"128\" height=\"128\" />",
									currentForecastDay.getWeatherImage()));
			infoTable.append(String.format("%s<br/>%s Chance of Precipitation",
					currentForecastDay.getWeather(),
					currentForecastDay.getPercentOfPrecipitation()));
			infoTable.append("</div>");
			infoTable.append(String.format(
					"<div class=\"high\">%s&deg;F</div>",
					currentForecastDay.getHighTemperature()));
			infoTable.append(String.format("<div class=\"low\">%s&deg;F</div>",
					currentForecastDay.getLowTemperature()));
			infoTable.append("</div>");

		}

		infoTable.append("<div class=\"clear-fix\"></div>");
		infoTable.append("</div>");
		infoTable.append("<!-- Forecast_Ends -->");

		if (weatherAlerts.getWeatherAlerts().size() > 0) {
			infoTable.append("<!--Alerts_Starts-->");
			infoTable.append("<div id=\"alerts-holder\">&nbsp;");
			infoTable.append(String.format("Severe Weather Alert! %s",
					weatherAlerts.getAllWeatherAlertsString()));
			infoTable.append("</div>");
			infoTable.append("<!--Alerts_End-->");
		} else {

			infoTable.append("<div id=\"footer\">");
			infoTable
					.append("<img style=\"float:right;margin-top:10px;\" src=\"weatherunderground-logo.png\" height=\"40\" />");
			infoTable.append("</div>");
		}
		// Zoom Control Closing Div
		// infoTable.append("</div>");
		infoTable.append("</body>");
		infoTable.append("</html>");

		return infoTable.toString();
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
