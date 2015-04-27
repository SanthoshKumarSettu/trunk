package com.dealersaleschannel.tv;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends Activity {


	private UtilityFunctions utils = new UtilityFunctions();
	public File dealerTvDir = null;
	File configFile;
	ArrayList<Slide> slides = new ArrayList<Slide>();
	public static final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);
	private DownloadFile downloadConfigFile;
	private DownloadFile downloadContentFile;
	public static final String cmsUrl = "http://dealertv.equipmentlocator.com";	
	private boolean isSlideShowPlaying = false;
	private boolean isCurrentlyUpdatingTimeInterval = false;	
	private int initialDelayForFirstUpdateInSeconds = 30;
	private boolean isSlideListLoadedAtLeastOnce = false;
	private int updateFailureRetries = 3;
	private boolean isUpdating = false;
	private boolean isSlideListOnInitialLoad = true;

	private OnSharedPreferenceChangeListener listener;

	private ScheduledFuture<?> updateConfigFileScheduledFuture;

	//Android Events
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		dealerTvDir = utils.getDealerTVDirectory(this.getBaseContext());

		listener = new SharedPreferences.OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(
					SharedPreferences sharedPreferences, String key) {

				if (updateConfigFileScheduledFuture != null) {
					if (updateConfigFileScheduledFuture.cancel(false)) {
						updateOnSetUpdateInterval();
					} else {
						if (!isCurrentlyUpdatingTimeInterval) {
							retryToStopScheduledUpdateConfigFileTask();
						}
					}
				}
			}
		};

		PreferenceManager.getDefaultSharedPreferences(getBaseContext())
				.registerOnSharedPreferenceChangeListener(listener);

		// Send this id through web service, and check if it is enabled
		Installation.id(this);

		setContentView(R.layout.activity_main);

		checkActivationEveryTenMinutes();

		cleanupUnusedEquipmentImagesEveryFifteenMinutes();

		configFile = new File(dealerTvDir + File.separator + "config.xml");

		if (!isInstallationActivated()) {

			RelativeLayout rl = (RelativeLayout) findViewById(R.id.MainLayout);

			ImageView splashScreenImage = new ImageView(this);

			// get input stream
			InputStream ims;
			try {
				ims = getAssets().open("SplashScreen-black.jpg");

				// load image as Drawable
				Drawable d = Drawable.createFromStream(ims, null);

				splashScreenImage.setImageDrawable(d);

			} catch (IOException e) {
				Log.e("DealerTv", e.getMessage());
			}

			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					1920, 1080);
			params.leftMargin = 0;
			params.topMargin = 0;

			rl.removeAllViewsInLayout();
			rl.addView(splashScreenImage, params);

		} else if ((isInstallationActivated() && isInstallationActive())) {

			downloadConfigFile = new DownloadFile();
			downloadContentFile = new DownloadFile();

			selectSplashScreen();
			
			loadlistInTheBackground();
			
			startSlideShow();

			// cleanUpBeforeDownload();
			updateOnSetUpdateInterval();
			checkForPendingDownloadsEveryTenSeconds();
			checkForNewWeatherEveryFiveMinutes();
			LogActivityEveryThirtyMinutes();

		} else if (!isInstallationActive()
				&& !isInstallationActivatedAndActiveOnCMS()) {
			// TextView textView = (TextView) findViewById(R.id.welcome);
			// textView.setText("This installation of Dealer Sales Channel TV has been deactivated.  If you need further assistance contact "
			// + "Dealer Sales Channel TV 1-956-412-6600.");

			RelativeLayout rl = (RelativeLayout) findViewById(R.id.MainLayout);

			ImageView splashScreenImage = new ImageView(this);

			// get input stream
			InputStream ims;
			try {
				ims = getAssets().open("DeactivateScreen.jpg");

				// load image as Drawable
				Drawable d = Drawable.createFromStream(ims, null);

				splashScreenImage.setImageDrawable(d);

			} catch (IOException e) {
				Log.e("DealerTv", e.getMessage());
			}

			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					1920, 1080);
			params.leftMargin = 0;
			params.topMargin = 0;

			rl.removeAllViewsInLayout();
			rl.addView(splashScreenImage, params);

		} else if (isInstallationActivatedAndActiveOnCMS()) {
			configFile = new File(dealerTvDir + File.separator + "config.xml");

			downloadConfigFile = new DownloadFile();
			downloadContentFile = new DownloadFile();

			selectSplashScreen();
			
			loadlistInTheBackground();

			startSlideShow();

			// cleanUpBeforeDownload();
			updateOnSetUpdateInterval();
			checkForPendingDownloadsEveryTenSeconds();
			checkForNewWeatherEveryFiveMinutes();
			LogActivityEveryThirtyMinutes();

		}

	}

	@Override 
	protected void onDestroy() {
		PreferenceManager.getDefaultSharedPreferences(getBaseContext())
				.unregisterOnSharedPreferenceChangeListener(listener);

		super.onDestroy();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (isInstallationActivated() && !isInstallationActive()) {
			finish();
			restartActivity();

		} else {

			try {
				// Check if Exist result was returned
				String result = data.getStringExtra("result");

				if (result.equals("restart")) {

					RelativeLayout rl = (RelativeLayout) findViewById(R.id.MainLayout);

					ImageView splashScreenImage = new ImageView(this);

					// get input stream
					InputStream ims;
					try {
						ims = getAssets().open("Restart.jpg");

						// load image as Drawable
						Drawable d = Drawable.createFromStream(ims, null);

						splashScreenImage.setImageDrawable(d);

					} catch (IOException e) {
						Log.e("DealerTv", e.getMessage());
					}

					RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
							1920, 1080);
					params.leftMargin = 0;
					params.topMargin = 0;

					rl.removeAllViewsInLayout();
					rl.addView(splashScreenImage, params);

					loadlistInTheBackground();

					startSlideShow();

				} else {

					// Get current Slide Order
					int currentOrder = Integer.parseInt(result);

					boolean moveForward = true;

					// If currentOrder is negative that means we are trying
					// to reach the previous slide
					if (currentOrder >> 31 != 0) {
						moveForward = false;
						currentOrder = Math.abs(currentOrder);
					}

					// Slide Array has shrunk, currentOrder is last slide order
					// number
					// or the end has been reached
					// Set currentOrder to nextSlide Order or reset to first
					// slide
					if (currentOrder >= slides.size()) {
						currentOrder = 1;

					} else {
						if (moveForward) {
							currentOrder = currentOrder + 1;

						} else {
							currentOrder = currentOrder - 1;

							if (currentOrder <= 0) {
								currentOrder = slides.size();
							}
						}
					}

					Slide firstSlide = null;

					try {

						firstSlide = slides.get(currentOrder - 1);

					} catch (Exception e) {

						Log.e("DealerTv", e.getMessage());
					}

					int numberOfTries = 0;

					boolean isSlideViewable = IsSlideViewable(firstSlide);
					while (!isSlideViewable) {

						// check slides array
						if (slides.size() == 0) {
							// TextView textView = (TextView)
							// findViewById(R.id.welcome);
							// textView.setText("There are no viewable slides.  Check your files make sure the "
							// +
							// "image and video files for each image, equipment and video slide exist in the DealerTv directory, "
							// +
							// "and that text slides have text in them.  If you have an internet connection, enter the your channel number "
							// +
							// "in the settings and the image, and video files will download automatically. "
							// +
							// "You can trigger the download by restarting the program.  If you don't have an internet connection, you need to"
							// +
							// " copy the image, and video files manually into the dealer tv directory.  If you need further assistance contact "
							// + "Dealer Sales Channel TV 1-956-412-6600.");

							RelativeLayout rl = (RelativeLayout) findViewById(R.id.MainLayout);

							ImageView splashScreenImage = new ImageView(this);

							// get input stream
							InputStream ims;
							try {
								ims = getAssets().open(
										"NoViewableSlidesScreen.jpg");

								// load image as Drawable
								Drawable d = Drawable.createFromStream(ims,
										null);

								splashScreenImage.setImageDrawable(d);

							} catch (IOException e) {
								Log.e("DealerTv", e.getMessage());
							}

							RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
									1920, 1080);
							params.leftMargin = 0;
							params.topMargin = 0;

							rl.removeAllViewsInLayout();
							rl.addView(splashScreenImage, params);

							startSlideShowOnceViewableSlideBecomesAvailable();

							break;
						}

						if (firstSlide.contentType.equals("video")) {
							// Set currentOrder to nextSlide Order or break if
							// you
							// reached
							// the end
							if (currentOrder >= slides.size()) {
								currentOrder = 1;
							} else {
								if (moveForward) {
									currentOrder = currentOrder + 1;

								} else {
									currentOrder = currentOrder - 1;

									if (currentOrder <= 0) {
										currentOrder = slides.size();
									}
								}
							}

						} else {

							if (numberOfTries > 1) {
								currentOrder = 1;
							} else {
								if (currentOrder >= slides.size()) {
									currentOrder = 1;
								} else {
									if (moveForward) {
										currentOrder = currentOrder + 1;

									} else {
										currentOrder = currentOrder - 1;

										if (currentOrder <= 0) {
											currentOrder = slides.size() - 1;
										}
									}
								}
							}

							numberOfTries = numberOfTries + 1;

						}

						try {
							firstSlide = slides.get(currentOrder - 1);

						} catch (Exception e) {
							Log.e("DealerTv", e.getMessage());

						}

						isSlideViewable = IsSlideViewable(firstSlide);

					}

					if (isSlideViewable) {
						int newRequestCode = 1;

						if (firstSlide.contentType.equals("video")) {
							Intent intent = new Intent(this,
									VideoActivity.class);
							intent.putExtra("Slide", firstSlide);
							startActivityForResult(intent, newRequestCode);

						} else if (firstSlide.contentType.equals("image")) {
							Intent intent = new Intent(this,
									ImageActivity.class);
							intent.putExtra("Slide", firstSlide);

							startActivityForResult(intent, newRequestCode);

						} else if (firstSlide.contentType.equals("equipment")) {
							Intent intent = new Intent(this,
									EquipmentActivity.class);
							intent.putExtra("Slide", firstSlide);

							startActivityForResult(intent, newRequestCode);

						} else if (firstSlide.contentType.equals("text")) {
							Intent intent = new Intent(this, TextActivity.class);
							intent.putExtra("Slide", firstSlide);

							startActivityForResult(intent, newRequestCode);
						} else if (firstSlide.contentType.equals("weather")) {
							if (firstSlide.data
									.equalsIgnoreCase("currentconditionsandforecast")) {
								Intent intent = new Intent(this,
										WeatherActivity.class);
								intent.putExtra("Slide", firstSlide);
								startActivityForResult(intent, requestCode);

							} else if (firstSlide.data
									.equalsIgnoreCase("radar")) {
								Intent intent = new Intent(this,
										WeatherRadarActivity.class);
								intent.putExtra("Slide", firstSlide);
								startActivityForResult(intent, requestCode);
							}
						}
					}
				}
			} catch (Exception e) {

				finish();
				restartActivity();
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

		File configFile = new File(dealerTvDir + File.separator + "config.xml");
		if (!configFile.exists() || DownloadFile.syncing) {
			menu.removeItem(R.id.action_sync);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.action_settings) {
			Intent i = new Intent(this, SettingActivity.class);
			startActivity(i);
		} else if (itemId == R.id.action_restart) {
			finish();
			restartActivity();
		} else if (itemId == R.id.action_activate) {
			activateDealerTvApplication();
		} else if (itemId == R.id.action_about) {
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
		} else if (itemId == R.id.action_sync) {
			String[] params = new String[3];

			try {

				SharedPreferences settings = PreferenceManager
						.getDefaultSharedPreferences(getBaseContext());
				String channelNumber = settings.getString(
						getString(R.string.pref_channel), "0");

				loadSlidesListFromConfigXml();

				params[2] = utils.createDownloadParametersCommaSeparatedString(
						slides, channelNumber, cmsUrl);

				if (params[2] != null && !params[2].equals("")) {

					downloadContentFile = new DownloadFile();
					downloadContentFile.status = "downloading";
					downloadContentFile.execute(params);

					Log.i("DealerTv", "Sync Download In Progress");
				}
			} catch (Exception e) {
				Log.e("DealerTv", "Synchronization Failed: " + e.getMessage());
			}
		}

		return true;
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		super.onOptionsMenuClosed(menu);

		if (Installation.activation(this, "") != null
				&& !Installation.activation(this, "").equals("")) {
			menu.removeItem(R.id.action_activate);
		}

		File configFile = new File(dealerTvDir + File.separator + "config.xml");
		if (!configFile.exists()) {
			menu.removeItem(R.id.action_sync);
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_ESCAPE:
			/* Sample for handling the Menu button globally */
			openOptionsMenu();
			return true;
		}
		return false;
	}

	//Helper Methods
	//Loading
	private void selectSplashScreen() {
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.MainLayout);

		ImageView splashScreenImage = new ImageView(this);
		
		// get input stream
		InputStream ims;

		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				1920, 1080);
		params.leftMargin = 0;
		params.topMargin = 0;
		
		if (configFile != null && !configFile.exists()) 
		{

			try {
				ims = getAssets().open("Standby.jpg");

				// load image as Drawable
				Drawable d = Drawable.createFromStream(ims, null);

				splashScreenImage.setImageDrawable(d);

			} catch (IOException e) {
				Log.e("DealerTv", e.getMessage());
			}

			Log.e("DealerTv", "Error: The configuration file is missing.");

		}else if(configFile != null && configFile.exists() && slides.isEmpty())
		{
			
			try {
				ims = getAssets().open("Loading.jpg");

				// load image as Drawable
				Drawable d = Drawable.createFromStream(ims, null);

				splashScreenImage.setImageDrawable(d);

			} catch (IOException e) {
				Log.e("DealerTv", e.getMessage());
			}
		}else if (configFile != null && configFile.exists() && !slides.isEmpty()) {

			initialDelayForFirstUpdateInSeconds = 180;	
			
			try {
				ims = getAssets().open("NoViewableSlidesScreen.jpg");

				// load image as Drawable
				Drawable d = Drawable.createFromStream(ims, null);

				splashScreenImage.setImageDrawable(d);

			} catch (IOException e) {
				Log.e("DealerTv", e.getMessage());
			}

		}
		
		
		rl.removeAllViewsInLayout();
		rl.addView(splashScreenImage, params);
	}

	public void restartActivity() {
		PreMainActivity.ENABLE_RESTART = true;
		Intent i = new Intent(this, PreMainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(i);
	}
	
	//Downloading
	private void setVideoDownloadParameters(String[] params) {
		// Find New File to Download
		File file;

		for (Slide slide : slides) {

			if (slide.contentType.equals("video") && slide.data != null
					&& !slide.data.equals("")) {
				file = new File(slide.data);

				if (!file.exists()) {
					String url = params[0] + File.separator + "videos"
							+ File.separator + file.getName();

					url = utils.addURLSpecialCharacterEncoding(url);

					if (fileExistsOnServer(url)) {
						params[0] = url;
						params[1] = file.getAbsolutePath();

						break;
					}
				}
			}
		}
	}

	private void setImageDownloadParameters(String[] params) {
		// Find New File to Download
		try {
			File file;

			for (Slide slide : slides) {

				if (slide.headerImage != null && !slide.headerImage.equals("")) {
					file = new File(slide.headerImage);

					if (!file.exists()) {
						String url = params[0] + File.separator + "images"
								+ File.separator + file.getName();

						// Add URL encoding
						url = utils.addURLSpecialCharacterEncoding(url);

						if (fileExistsOnServer(url)) {
							params[0] = url;
							params[1] = file.getAbsolutePath();

							break;
						}
					}
				}

				if (slide.footerImage != null && !slide.footerImage.equals("")) {
					file = new File(slide.footerImage);

					if (!file.exists()) {
						String url = params[0] + File.separator + "images"
								+ File.separator + file.getName();

						url = utils.addURLSpecialCharacterEncoding(url);

						if (fileExistsOnServer(url)) {
							params[0] = url;
							params[1] = file.getAbsolutePath();

							break;
						}
					}
				}

				if (slide.contentType.equals("image") && slide.data != null
						&& !slide.data.equals("")) {
					file = new File(slide.data);

					if (!file.exists()) {
						String url = params[0] + File.separator + "images"
								+ File.separator + file.getName();

						url = utils.addURLSpecialCharacterEncoding(url);

						if (fileExistsOnServer(url)) {
							params[0] = url;
							params[1] = file.getAbsolutePath();
						}

						break;
					}
				}

				if (slide.contentType.equals("equipment") && slide.data != null
						&& !slide.data.equals("")) {
					file = new File(slide.data);

					if (!file.exists()) {
						String url = params[0] + File.separator + "images"
								+ File.separator + file.getName();

						url = utils.addURLSpecialCharacterEncoding(url);

						if (fileExistsOnServer(url)) {
							params[0] = url;
							params[1] = file.getAbsolutePath();
						}

						break;
					}
				}

				if (slide.contentType.equals("equipment")
						&& slide.backgroundimage != null
						&& !slide.backgroundimage.equals("")) {
					file = new File(slide.backgroundimage);

					if (!file.exists()) {
						String url = params[0] + File.separator + "images"
								+ File.separator + file.getName();

						url = utils.addURLSpecialCharacterEncoding(url);

						if (fileExistsOnServer(url)) {
							params[0] = url;
							params[1] = file.getAbsolutePath();
						}

						break;
					}
				}
			}
		} catch (Exception e) {
			Log.e("DealerTv", e.getMessage());
		}
	}

	//File
	private boolean fileExistsOnServer(String URLName) {
		try {
			HttpURLConnection.setFollowRedirects(false);
			// note : you may also need
			// HttpURLConnection.setInstanceFollowRedirects(false)
			HttpURLConnection con = (HttpURLConnection) new URL(URLName)
					.openConnection();
			con.setRequestMethod("HEAD");
			return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (Exception e) {
			Log.e("DealerTv",
					"File exists on Server Check error:" + e.getMessage());
			return false;
		}
	}

	private Boolean CopyFile(String copyFromFilePath, String copyToFilePath) {
		InputStream inStream = null;
		OutputStream outStream = null;

		try {

			File afile = new File(copyFromFilePath);
			File bfile = new File(copyToFilePath);

			if (!bfile.exists()) {
				bfile.createNewFile();
			}

			inStream = new FileInputStream(afile);
			outStream = new FileOutputStream(bfile);

			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = inStream.read(buffer)) > 0) {

				outStream.write(buffer, 0, length);

			}

			inStream.close();
			outStream.close();

			if (afile.length() != bfile.length()) {
				return false;
			}

			return true;

		} catch (IOException e) {
			Log.e("DealerTv", "Config File Copy failed:" + e.getMessage());
			return false;
		}

	}

	private boolean isGifCorrupted(String fileName) {
		File file = new File(fileName);

		byte[] firstThreeBytes = toByteArray(file, 0, 3);

		if (firstThreeBytes[0] == 71 && firstThreeBytes[1] == 73
				&& firstThreeBytes[2] == 70) {
			return false;
		}

		return true;

	}

	private static byte[] toByteArray(File file, int offset, int count) {

		byte[] bytes = new byte[count];

		try {
			BufferedInputStream buf = new BufferedInputStream(
					new FileInputStream(file));
			buf.read(bytes, offset, count);
			buf.close();

		} catch (FileNotFoundException e) {
			Log.e("DealerTv", "Radar File Error: " + e.getMessage());

		} catch (IOException e) {
			Log.e("DealerTv", "Radar File Error: " + e.getMessage());
		}

		return bytes;

	}

	private boolean updateConfigFile() {

		boolean updated = false;

		try {
			if (!downloadConfigFile.status.equals("downloading")) {

				isUpdating = true;
				
				SharedPreferences settings = PreferenceManager
						.getDefaultSharedPreferences(getBaseContext());

				String channelNumber = settings.getString(
						getString(R.string.pref_channel), "0");

				String currentConfigFilePath = dealerTvDir + File.separator
						+ "config.xml";
				String updateConfigFilePath = dealerTvDir + File.separator
						+ "updateConfig.xml";

				// Update Happens Here
				// First Download new Xml config file
				String[] params = new String[2];
				params[0] = cmsUrl + File.separator + "Channels"
						+ File.separator + channelNumber + File.separator
						+ "config.xml";
				params[1] = updateConfigFilePath;

				downloadConfigFile = new DownloadFile();
				downloadConfigFile.execute(params);

				// Do not continue until file we have a file download
				// result
				// check every 10 seconds
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					Log.e("DealerTv",
							"File is being downloaded interruption failed:"
									+ e.getMessage());
				}

				String afterDownloadMD5Sum = utils.getMD5Checksum(params[0],
						true);

				// Compare new configuration file and current
				// configuration
				// file
				// if files are different then, replace current
				// configuration with
				// new file and update download list
				String currentFileMd5Sum = utils
						.getMD5Checksum(currentConfigFilePath);
				String updateConfigFileMd5Sum = utils
						.getMD5Checksum(updateConfigFilePath);

				if (!currentFileMd5Sum.equals(updateConfigFileMd5Sum)
						&& (!updateConfigFileMd5Sum.equals(""))
						&& (updateConfigFileMd5Sum.equals(afterDownloadMD5Sum))
						&& IsUpdateConfigFileValid(updateConfigFilePath)) {
					// At this point we know something has changed
					// Then replace configFile with new one
					while (!CopyFile(updateConfigFilePath,
							currentConfigFilePath)) {

					}

					
					loadSlidesListFromConfigXml();
					

					Log.i("DealerTv", "Updated config file");
					updated = true;
					isUpdating = false;
				}

			}
		} catch (Exception e) {
			Log.e("DealerTv", "Updating config file failed" + e.getMessage());
			if(updateFailureRetries >0)
			{
				updateConfigFile();
				updateFailureRetries = updateFailureRetries - 1;
			}
			isUpdating = false;
		}

		return updated;
	}

	private boolean IsUpdateConfigFileValid(String updateConfigFilePath) {
		File file = new File(updateConfigFilePath);
		Scanner scanner;

		try {
			scanner = new Scanner(file);
			// now read the file line by line...
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.contains("<channel />") || line.contains("</channel>")) {
					return true;
				}
			}

		} catch (FileNotFoundException e) {
			return false;
		}

		return false;
	}

	
	//Slides
	private Slide getNextViewableSlide(ArrayList<Slide> allSlides) {
		Slide nextViewableSlide = null;

		try {
			for (Slide slide : allSlides) {
				if (IsSlideViewable(slide)) {
					nextViewableSlide = slide;
					break;
				}
			}
		} catch (Exception e) {

		}

		return nextViewableSlide;
	}

	private Boolean IsSlideViewable(Slide slide) {
		Boolean isViewable = false;

		if (slide == null)
			return isViewable;

		if (slide.contentType.equals("video")) {

			if (new File(slide.data).isFile()) {

				isViewable = true;
			}

		} else if (slide.contentType.equals("image")) {

			if (new File(slide.data).isFile()) {

				isViewable = true;

			}

		} else if (slide.contentType.equals("equipment")) {

			if (new File(slide.data).isFile()
					|| slide.data.toLowerCase(Locale.getDefault()).equals(
							"none")) {
				if (slide.backgroundimage != null
						&& !slide.backgroundimage.equals("")
						&& new File(slide.backgroundimage).isFile()) {

					isViewable = true;

				} else {
					isViewable = false;
				}

			}

		} else if (slide.contentType.equals("text")) {

			if (slide.data != null && !slide.data.equals("")) {
				isViewable = true;
			}

		} else if (slide.contentType.equals("weather")) {

			if (slide.data != null && !slide.data.equals("")
					&& isNetworkAvailable()) {

				if (slide.data.equals("CurrentConditionsAndForecast")) {
					File[] weatherFiles = new File(dealerTvDir + File.separator
							+ "weather").listFiles(new FilenameFilter() {

						@Override
						public boolean accept(File dir, String filename) {
							return filename.startsWith("weather")
									&& !filename.endsWith(".Temp");
						}
					});

					if (weatherFiles != null && weatherFiles.length > 0) {
						isViewable = true;
					}

				}
				if (slide.data.equals("Radar")) {
					File[] weatherFiles = new File(dealerTvDir + File.separator
							+ "weather").listFiles(new FilenameFilter() {

						@Override
						public boolean accept(File dir, String filename) {
							return filename.startsWith("radar")
									&& !filename.endsWith(".Temp")
									&& !isGifCorrupted(dir.getAbsolutePath()
											+ File.separator + filename);
						}
					});

					if (weatherFiles != null && weatherFiles.length > 0) {

						isViewable = true;
					}
				}

			}
		}

		return isViewable;
	}
	
	private void loadSlidesListFromConfigXml() {

		if (configFile != null && configFile.exists()) {
			try {

				

				// Get Slide Info from configuration File and Create Slide
				// Objects
				DataHandler handler = new DataHandler();
				if(!isSlideListLoadedAtLeastOnce)
				{
					handler.isInitialLoad = true;					
				}

				ArrayList<Slide> tempSlidesList;

				tempSlidesList = handler.getData(configFile.getAbsolutePath());

				

				// Sort Slide List by order
				Collections.sort(tempSlidesList, new SlideOrderComparator());

				for (Slide slide : tempSlidesList) {
					if (slide.contentType.equals("video")) {
						slide.data = dealerTvDir + File.separator + "videos"
								+ File.separator + slide.data;

					} else if (slide.contentType.equals("image")) {
						slide.data = dealerTvDir + File.separator + "images"
								+ File.separator + slide.data;

					} else if (slide.contentType.equals("equipment")) {
						slide.data = dealerTvDir + File.separator + "images"
								+ File.separator + slide.data;
					}

					if ((slide.headerImage != null)
							&& !slide.headerImage.isEmpty()) {
						slide.headerImage = dealerTvDir + File.separator
								+ "images" + File.separator + slide.headerImage;

					}

					if ((slide.footerImage != null)
							&& !slide.footerImage.isEmpty()) {
						slide.footerImage = dealerTvDir + File.separator
								+ "images" + File.separator + slide.footerImage;

					}

					if ((slide.backgroundimage != null)
							&& !slide.backgroundimage.isEmpty()) {
						slide.backgroundimage = dealerTvDir + File.separator
								+ "images" + File.separator
								+ slide.backgroundimage;

					}

				}

				slides = tempSlidesList;
				handler = null;
				tempSlidesList = null;
				
				if(isSlideListLoadedAtLeastOnce)
				{
					isSlideListOnInitialLoad = false;
				}
				
				isSlideListLoadedAtLeastOnce = true;
				
				System.gc();

			} catch (Exception e) {
				Log.e("DealerTv", "Exception " + e.getMessage());
				

			}
		}
	}

	private void startSlideShow() {
		Slide firstSlide = null;

		firstSlide = getNextViewableSlide(slides);

		if (firstSlide != null) {

			if (firstSlide.contentType.equals("video")) {
				Intent intent = new Intent(this, VideoActivity.class);
				intent.putExtra("Slide", firstSlide);
				int requestCode = 1;
				startActivityForResult(intent, requestCode);

			} else if (firstSlide.contentType.equals("image")) {
				Intent intent = new Intent(this, ImageActivity.class);
				intent.putExtra("Slide", firstSlide);
				int requestCode = 1;
				startActivityForResult(intent, requestCode);

			} else if (firstSlide.contentType.equals("equipment")) {
				Intent intent = new Intent(this, EquipmentActivity.class);
				intent.putExtra("Slide", firstSlide);
				int requestCode = 1;
				startActivityForResult(intent, requestCode);

			} else if (firstSlide.contentType.equals("text")) {
				Intent intent = new Intent(this, TextActivity.class);
				intent.putExtra("Slide", firstSlide);
				int requestCode = 1;
				startActivityForResult(intent, requestCode);
			} else if (firstSlide.contentType.equals("weather")) {
				if (firstSlide.data
						.equalsIgnoreCase("currentconditionsandforecast")) {
					Intent intent = new Intent(this, WeatherActivity.class);
					intent.putExtra("Slide", firstSlide);
					int requestCode = 1;
					startActivityForResult(intent, requestCode);

				} else if (firstSlide.data.equalsIgnoreCase("radar")) {
					Intent intent = new Intent(this, WeatherRadarActivity.class);
					intent.putExtra("Slide", firstSlide);
					int requestCode = 1;
					startActivityForResult(intent, requestCode);
				}
			}

			isSlideShowPlaying = true;

		} else {
			isSlideShowPlaying = false;

			startSlideShowOnceViewableSlideBecomesAvailable();

		}

		
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();

		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	private static boolean isCMSOnline() {

		boolean isCMSOnline = false;
		HttpURLConnection connection = null;
		BufferedReader rd = null;

		URL serverAddress = null;

		try {
			serverAddress = new URL(cmsUrl);
			// set up out communications stuff
			connection = null;

			// Set up the initial connection
			connection = (HttpURLConnection) serverAddress.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			connection.setReadTimeout(10000);

			connection.connect();

			// read the result from the server
			rd = new BufferedReader(new InputStreamReader(
					connection.getInputStream()), 10);

			if (rd.readLine() != null) {
				isCMSOnline = true;
			}

		} catch (MalformedURLException e) {
			Log.e("DealerTv", e.getMessage());
		} catch (ProtocolException e) {
			Log.e("DealerTv", e.getMessage());
		} catch (IOException e) {
			Log.e("DealerTv", e.getMessage());
		} finally {
			// close the connection, set all objects to null
			connection.disconnect();
			rd = null;
			connection = null;
		}

		return isCMSOnline;
	}

	
	//Activation and Activity
	private boolean isInstallationActivatedAndActiveOnCMS() {
		try {

			boolean isInstallationActivated = isInstallationActivated();
			boolean isCMSOnline = isCMSOnline();

			if (isNetworkAvailable() && isInstallationActivated && isCMSOnline) {

				SharedPreferences settings = PreferenceManager
						.getDefaultSharedPreferences(getBaseContext());

				String InstallationId = Installation.id(getBaseContext());

				String SavedActivationId = Installation.activation(
						getBaseContext(), "");

				String channelNumber = settings.getString(
						getString(R.string.pref_channel), "0");

				String url = cmsUrl + "/Api/Activation/" + channelNumber;

				String result = "";

				try {

					JSONObject activationJsonObj = new JSONObject();
					JSONObject resultJsonObj = null;

					activationJsonObj.put("InstallationId", InstallationId);
					activationJsonObj.put("DealerTvStationId", channelNumber);
					activationJsonObj.put("IsActive", "false");
					activationJsonObj.put("Field", "");
					activationJsonObj.put("Field2", "");
					activationJsonObj.put("Location", "");
					activationJsonObj.put("ActivationId", SavedActivationId);

					resultJsonObj = JsonUtils.sendJson(url, activationJsonObj);

					if (resultJsonObj != null) {
						result = resultJsonObj.getString("Result");

					}

					if (result.equals("Fail - Not Active")) {
						// Deactivate Application
						SharedPreferences.Editor editor = settings.edit();
						editor.putBoolean("IsActive", false);
						editor.commit();
						Log.i("DealerTv", "Activation Check: Inactive");

						return false;

					}
					if (result.equals("Pass - Active")) {
						boolean restart = isInstallationActivated()
								&& !isInstallationActive();

						// Activate Application
						SharedPreferences.Editor editor = settings.edit();
						editor.putBoolean("IsActive", true);

						editor.commit();
						Log.i("DealerTv", "Activation Check: Active");

						if (restart) {
							finish();
							restartActivity();
						}

						return true;
					}

				} catch (JSONException e) {

					Log.e("DealerTv", e.getMessage());
				}

			}
		} catch (Exception e) {
			Log.e("DealerTv", "Activation Check Failed: " + e.getMessage());
		}

		return true;
	}

	private boolean isInstallationActivated() {
		String InstallationActivation = Installation.activation(
				getBaseContext(), "");
		boolean isInstallationActivated = (InstallationActivation != null && !InstallationActivation
				.equals(""));
		return isInstallationActivated;
	}

	private void activateDealerTvApplication() {

		Boolean missingRequiredFields = false;
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

		String url = cmsUrl + "/Api/Activation";

		String result = "";
		String activationId = "";
		
		Toast toast = Toast.makeText(this,
				"Activation Failed - Check Internet Connection",
				Toast.LENGTH_SHORT);
		
		if(LastKnownLocation == null  || LastKnownLocation.isEmpty())
		{
			toast = Toast.makeText(this,
					"Activation Failed - A zip code is required.",
					Toast.LENGTH_SHORT);

			Log.i("DealerTv",
					"Activation Failed - A zip code is required.");

			missingRequiredFields = true;
		}

		if(userName == null  || userName.isEmpty())
		{
			toast = Toast.makeText(this,
					"Activation Failed - A user name is required.",
					Toast.LENGTH_SHORT);

			Log.i("DealerTv",
					"Activation Failed - A zip code is required.");
			
			missingRequiredFields = true;
		}
		
		if(userPassword == null  || userPassword.isEmpty())
		{
			toast = Toast.makeText(this,
					"Activation Failed - A password is required.",
					Toast.LENGTH_SHORT);

			Log.i("DealerTv",
					"Activation Failed - A password is required.");
			
			missingRequiredFields = true;
		}
		
		if(!missingRequiredFields)
		{
			try {
	
				JSONObject activationJsonObj = new JSONObject();
				JSONObject resultJsonObj = null;
	
				activationJsonObj.put("InstallationId", InstallationId);
				activationJsonObj.put("DealerTvStationId", channelNumber);
				activationJsonObj.put("IsActive", "false");
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
	
	
	
			if (result.equals("Pass - Active") && !activationId.equals("")
					&& activationId != null) {
				Installation.activation(this, activationId);
	
				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean("IsActive", true);
				editor.commit();
	
				toast = Toast.makeText(this, "Activation Succesful",
						Toast.LENGTH_SHORT);
	
				finish();
				restartActivity();
	
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
		}

		toast.show();

	}
	
	private boolean isInstallationActive() {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		boolean isActive = settings.getBoolean("IsActive", true);
		Log.i("DealerTv", "IsActive: " + Boolean.toString(isActive));

		return isActive;
	}
	
	//Scheduled Tasks
	private void updateOnSetUpdateInterval() {
		final Runnable update = new Runnable() {
			public void run() {

				try {
					if (!DownloadFile.syncing && !isUpdating) {
						updateConfigFile();
					}
				} catch (Exception e) {
					Log.e("DealerTv", "Failed to update config file.");
				}
			}

		};

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		String updateInterval = settings.getString(
				getString(R.string.pref_updateInterval), "15");

		if (updateInterval.equals("") || !utils.isInteger(updateInterval)) {
			updateInterval = "15";
		}

		long updateIntervalInSeconds = Long.parseLong(updateInterval) * 60;

		// Production
		updateConfigFileScheduledFuture = scheduler.scheduleAtFixedRate(update,
				initialDelayForFirstUpdateInSeconds, updateIntervalInSeconds, SECONDS);
		// Testing
		// updateConfigFileScheduledFuture =
		// scheduler.scheduleAtFixedRate(update, 30,
		// Integer.parseInt(updateInterval), SECONDS);

	}

	private void loadlistInTheBackground() {
		final Runnable update = new Runnable() {
			public void run() {

				try {

			
					loadSlidesListFromConfigXml();

					

				} catch (Exception e) {
					Log.e("DealerTv", "Failed to update config file.");

				}
			}

		};

		// Production
		updateConfigFileScheduledFuture = scheduler
				.schedule(update, 1, SECONDS);
	}

	private void checkForPendingDownloadsEveryTenSeconds() {
		final Runnable download = new Runnable() {

			public void run() {
				if (!DownloadFile.syncing) {
					try {
						// if currently downloading something wait until it
						// finishes
						if (!downloadContentFile.status.equals("downloading")) {

							SharedPreferences settings = PreferenceManager
									.getDefaultSharedPreferences(getBaseContext());
							String channelNumber = settings.getString(
									getString(R.string.pref_channel), "0");

							String[] params = new String[2];
							params[0] = cmsUrl + File.separator + "Channels"
									+ File.separator + channelNumber;

							setImageDownloadParameters(params);

							if (params[1] == null || params[1].equals("")) {
								params = new String[2];
								params[0] = cmsUrl + File.separator
										+ "Channels" + File.separator
										+ channelNumber;
								setVideoDownloadParameters(params);
							}

							if (params[1] != null && !params[1].equals("")) {

								downloadContentFile = new DownloadFile();
								downloadContentFile.status = "downloading";
								downloadContentFile.execute(params);

								Log.i("DealerTv", "Downloading Content: "
										+ params[0]);
							}

						}
					} catch (Exception e) {
						Log.e("DealerTv",
								"Downloading Content failed: " + e.getMessage());
					}
				}
			}

		};
		// Production
		scheduler.scheduleAtFixedRate(download, 30, 10, SECONDS);
		// Testing
		// scheduler.scheduleAtFixedRate(download, 0,15, SECONDS);
	}

	private void checkForNewWeatherEveryFiveMinutes() {
		final Runnable download = new Runnable() {
			public void run() {

				try {

					SharedPreferences settings = PreferenceManager
							.getDefaultSharedPreferences(getBaseContext());

					String channelNumber = settings.getString(
							getString(R.string.pref_channel), "0");

					// Hit Current Conditions Json Service
					String currentConditionsUrl = cmsUrl + File.separator
							+ "Api/Weather/" + channelNumber + File.separator
							+ "conditions";

					JSONObject jsonCurrentConditionsContainer = JsonUtils
							.SendHttpGet(currentConditionsUrl);

					Calendar calendar = Calendar.getInstance();
					SimpleDateFormat sdf = new SimpleDateFormat(
							"MM/dd/yyyy HH:mm:ss a", Locale.ENGLISH);
					calendar.setTime(sdf.parse(jsonCurrentConditionsContainer
							.getString("CreatedOn")));

					File currentConditionsFile = new File(dealerTvDir
							+ File.separator + "weather", "conditions_"
							+ calendar.get(Calendar.MONTH) + "_"
							+ calendar.get(Calendar.DAY_OF_MONTH) + "_"
							+ calendar.get(Calendar.YEAR) + "_"
							+ calendar.get(Calendar.HOUR) + "_"
							+ calendar.get(Calendar.MINUTE) + "_"
							+ calendar.get(Calendar.SECOND));

					// Save CurrentContitions Data with Date Info on
					if (!currentConditionsFile.exists())
						WeatherCacheControl.writeFile(currentConditionsFile,
								jsonCurrentConditionsContainer);

					// Delete Older Files
					File[] currentConditionsFiles = new File(dealerTvDir
							+ File.separator + "weather")
							.listFiles(new FilenameFilter() {

								@Override
								public boolean accept(File dir, String filename) {
									return filename.startsWith("conditions");
								}
							});

					// Leave 2 files and delete the rest
					if (currentConditionsFiles.length > 2) {
						// Order By Creation Date, Older first
						Arrays.sort(currentConditionsFiles,
								new Comparator<File>() {
									public int compare(File f1, File f2) {
										return Long.valueOf(f1.lastModified())
												.compareTo(f2.lastModified());
									}
								});

						for (int i = 0; i < currentConditionsFiles.length - 2; i++) {
							currentConditionsFiles[i].delete();
						}
					}

					// Hit Astronomy Json Service
					String astronomyUrl = cmsUrl + File.separator
							+ "Api/Weather/" + channelNumber + File.separator
							+ "astronomy";

					JSONObject jsonAstronomyContainer = JsonUtils
							.SendHttpGet(astronomyUrl);

					calendar.setTime(sdf.parse(jsonAstronomyContainer
							.getString("CreatedOn")));

					File astronomyFile = new File(dealerTvDir + File.separator
							+ "weather", "astronomy_"
							+ calendar.get(Calendar.MONTH) + "_"
							+ calendar.get(Calendar.DAY_OF_MONTH) + "_"
							+ calendar.get(Calendar.YEAR) + "_"
							+ calendar.get(Calendar.HOUR) + "_"
							+ calendar.get(Calendar.MINUTE) + "_"
							+ calendar.get(Calendar.SECOND));

					// Save Astronomy Data with Date Info on
					if (!astronomyFile.exists())
						WeatherCacheControl.writeFile(astronomyFile,
								jsonAstronomyContainer);

					// Delete Older Files
					File[] astronomyFiles = new File(dealerTvDir
							+ File.separator + "weather")
							.listFiles(new FilenameFilter() {

								@Override
								public boolean accept(File dir, String filename) {
									return filename.startsWith("astronomy");
								}
							});

					// Leave 2 files and delete the rest
					if (astronomyFiles.length > 2) {
						// Order By Creation Date, Older first
						Arrays.sort(astronomyFiles, new Comparator<File>() {
							public int compare(File f1, File f2) {
								return Long.valueOf(f1.lastModified())
										.compareTo(f2.lastModified());
							}
						});

						for (int i = 0; i < astronomyFiles.length - 2; i++) {
							astronomyFiles[i].delete();
						}
					}

					// Hit forecast10day Json Service
					String forecast10DayUrl = cmsUrl + File.separator
							+ "Api/Weather/" + channelNumber + File.separator
							+ "forecast10day";

					JSONObject jsonForecast10DayContainer = JsonUtils
							.SendHttpGet(forecast10DayUrl);

					calendar.setTime(sdf.parse(jsonForecast10DayContainer
							.getString("CreatedOn")));

					File forecast10DayFile = new File(dealerTvDir
							+ File.separator + "weather", "forecast10day_"
							+ calendar.get(Calendar.MONTH) + "_"
							+ calendar.get(Calendar.DAY_OF_MONTH) + "_"
							+ calendar.get(Calendar.YEAR) + "_"
							+ calendar.get(Calendar.HOUR) + "_"
							+ calendar.get(Calendar.MINUTE) + "_"
							+ calendar.get(Calendar.SECOND));

					// Save forecast10day Data with Date Info on
					if (!forecast10DayFile.exists())
						WeatherCacheControl.writeFile(forecast10DayFile,
								jsonForecast10DayContainer);

					// Delete Older Files
					File[] forecast10dayFiles = new File(dealerTvDir
							+ File.separator + "weather")
							.listFiles(new FilenameFilter() {

								@Override
								public boolean accept(File dir, String filename) {
									return filename.startsWith("forecast10day");
								}
							});

					// Leave 2 files and delete the rest
					if (forecast10dayFiles.length > 2) {
						// Order By Creation Date, Older first
						Arrays.sort(forecast10dayFiles, new Comparator<File>() {
							public int compare(File f1, File f2) {
								return Long.valueOf(f1.lastModified())
										.compareTo(f2.lastModified());
							}
						});

						for (int i = 0; i < forecast10dayFiles.length - 2; i++) {
							forecast10dayFiles[i].delete();
						}
					}

					// Hit alerts Json Service
					String alertsUrl = cmsUrl + File.separator + "Api/Weather/"
							+ channelNumber + File.separator + "alerts";

					JSONObject alertsContainer = JsonUtils
							.SendHttpGet(alertsUrl);

					calendar.setTime(sdf.parse(alertsContainer
							.getString("CreatedOn")));

					File alertsFile = new File(dealerTvDir + File.separator
							+ "weather", "alerts_"
							+ calendar.get(Calendar.MONTH) + "_"
							+ calendar.get(Calendar.DAY_OF_MONTH) + "_"
							+ calendar.get(Calendar.YEAR) + "_"
							+ calendar.get(Calendar.HOUR) + "_"
							+ calendar.get(Calendar.MINUTE) + "_"
							+ calendar.get(Calendar.SECOND));

					// Save alerts Data with Date Info on
					if (!alertsFile.exists())
						WeatherCacheControl.writeFile(alertsFile,
								alertsContainer);

					// Delete Older Files
					File[] alertsFiles = new File(dealerTvDir + File.separator
							+ "weather").listFiles(new FilenameFilter() {

						@Override
						public boolean accept(File dir, String filename) {
							return filename.startsWith("alerts");
						}
					});

					// Leave 2 files and delete the rest
					if (alertsFiles.length > 2) {
						// Order By Creation Date, Older first
						Arrays.sort(alertsFiles, new Comparator<File>() {
							public int compare(File f1, File f2) {
								return Long.valueOf(f1.lastModified())
										.compareTo(f2.lastModified());
							}
						});

						for (int i = 0; i < alertsFiles.length - 2; i++) {
							alertsFiles[i].delete();
						}
					}

					// Hit radar Json Service
					String radarUrl = cmsUrl + File.separator + "Api/Weather/"
							+ channelNumber + File.separator + "radar";

					JSONObject radarJsonContainer = JsonUtils
							.SendHttpGet(radarUrl);

					// Save Astronomy GIF
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

					String[] params = new String[2];
					params[0] = url;
					params[1] = new File(dealerTvDir + File.separator
							+ "weather")
							+ File.separator + radarGIFfileName;

					if (!new File(params[1]).exists()) {
						DownloadFile downloadFile = new DownloadFile();
						downloadFile.execute(params);
					}

					// Delete Older Files
					File[] radarFiles = new File(dealerTvDir + File.separator
							+ "weather").listFiles(new FilenameFilter() {

						@Override
						public boolean accept(File dir, String filename) {
							return filename.startsWith("radar");
						}
					});

					// Leave 2 files and delete the rest
					if (radarFiles.length > 2) {
						// Order By Creation Date, Older first
						Arrays.sort(radarFiles, new Comparator<File>() {
							public int compare(File f1, File f2) {
								return Long.valueOf(f1.lastModified())
										.compareTo(f2.lastModified());
							}
						});

						for (int i = 0; i < radarFiles.length - 2; i++) {
							radarFiles[i].delete();
						}
					}

					// Create WeatherInfoTable html file
					String weatherServiceUrl = cmsUrl + File.separator
							+ "Api/Weather" + File.separator + channelNumber;

					String weatherInfoTableHtml = WeatherActivity
							.CreateWeatherInfoTable(weatherServiceUrl,
									getBaseContext());
					Calendar currentCalendar = Calendar.getInstance();

					File weatherHtmlFile = new File(dealerTvDir
							+ File.separator + "weather", "weather_"
							+ currentCalendar.get(Calendar.MONTH) + "_"
							+ currentCalendar.get(Calendar.DAY_OF_MONTH) + "_"
							+ currentCalendar.get(Calendar.YEAR) + "_"
							+ currentCalendar.get(Calendar.HOUR) + "_"
							+ currentCalendar.get(Calendar.MINUTE) + "_"
							+ currentCalendar.get(Calendar.SECOND));

					// SaveWeatherHtmlFile
					if (!weatherHtmlFile.exists())
						WeatherCacheControl.writeFile(weatherHtmlFile,
								weatherInfoTableHtml);

					// Delete Older Files
					File[] weatherFiles = new File(dealerTvDir + File.separator
							+ "weather").listFiles(new FilenameFilter() {

						@Override
						public boolean accept(File dir, String filename) {
							return filename.startsWith("weather");
						}
					});

					// Leave 2 files and delete the rest
					if (weatherFiles.length > 2) {
						// Order By Creation Date, Older first
						Arrays.sort(weatherFiles, new Comparator<File>() {
							public int compare(File f1, File f2) {
								return Long.valueOf(f1.lastModified())
										.compareTo(f2.lastModified());
							}
						});

						for (int i = 0; i < weatherFiles.length - 2; i++) {
							weatherFiles[i].delete();
						}
					}

					Log.i("Dealer Tv Conditions Files Found: ",
							String.valueOf(currentConditionsFiles.length));
					Log.i("Dealer Tv Astronomy Files Found: ",
							String.valueOf(astronomyFiles.length));
					Log.i("Dealer Tv Forecast10day Files Found: ",
							String.valueOf(forecast10dayFiles.length));
					Log.i("Dealer Tv Alerts Files Found: ",
							String.valueOf(alertsFiles.length));
					Log.i("Dealer Tv Weather Files Found: ",
							String.valueOf(weatherFiles.length));
					Log.i("Dealer Tv Radar Files Found: ",
							String.valueOf(radarFiles.length));

				} catch (Exception e) {
					Log.e("DealerTv",
							"Weather Update failed: " + e.getMessage());
				}
			}

		};

		// Production
		scheduler.scheduleAtFixedRate(download, 0, 5, MINUTES);
		// Test
		// scheduler.scheduleAtFixedRate(download, 0, 10, SECONDS);

	}

	private void checkActivationEveryTenMinutes() {
		final Runnable activation = new Runnable() {
			public void run() {

				try {

					isInstallationActivatedAndActiveOnCMS();

				} catch (Exception e) {
					Log.e("DealerTv",
							"Activation Check Failed: " + e.getMessage());
				}
			}

		};

		// Production
		scheduler.scheduleAtFixedRate(activation, 0, 10, MINUTES);
		// Testing
		// scheduler.scheduleAtFixedRate(activation, 0, 15, SECONDS);
	}

	private void cleanupUnusedEquipmentImagesEveryFifteenMinutes() {
		final Runnable cleanup = new Runnable() {
			public void run() {
				if (!slides.isEmpty() && !isSlideListOnInitialLoad) {
					try {

						// Get Equipment Slide File Names Only
						ArrayList<String> equipmentSlideImageFilenames = new ArrayList<String>();

						for (Slide slide : slides) {
							if (slide.contentType.equals("equipment")) {
								equipmentSlideImageFilenames.add(slide.data);
							}

						}

						File[] equipmentImageFiles = new File(dealerTvDir
								+ File.separator + "images")
								.listFiles(new FilenameFilter() {

									@Override
									public boolean accept(File dir,
											String filename) {
										return (filename.contains("-1_GUID") && filename
												.endsWith(".jpg"))
												|| filename.endsWith("-1.jpg");
									}
								});

						for (File file : equipmentImageFiles) {
							if (!equipmentSlideImageFilenames.contains(file
									.getAbsolutePath())) {
								Log.i("DealerTv",
										"Deleted File: " + file.getName());
								file.delete();

							}
						}

					} catch (Exception e) {
						Log.e("DealerTv", "Cleanup Equipment Images Failed: "
								+ e.getMessage());
					}
				}
			}
		};

		// Production
		scheduler.scheduleAtFixedRate(cleanup, 0, 915, SECONDS);
		// Testing
		// scheduler.scheduleAtFixedRate(cleanup, 0, 60, SECONDS);
	}

	private void LogActivityEveryThirtyMinutes() {
		final Runnable activity = new Runnable() {
			public void run() {

				try {
					SharedPreferences settings = PreferenceManager
							.getDefaultSharedPreferences(getBaseContext());

					boolean isActive = settings.getBoolean("IsActive", false);

					if (isNetworkAvailable() && isActive) {
						String InstallationId = Installation
								.id(getBaseContext());
						String LastKnownLocation = Installation
								.getLastKnownLocation(getBaseContext());

						String userName = settings.getString(
								getString(R.string.pref_username), "");

						String userPassword = settings.getString(
								getString(R.string.pref_password), "");

						String SavedActivationId = Installation.activation(
								getBaseContext(), "");

						String channelNumber = settings.getString(
								getString(R.string.pref_channel), "0");

						String url = cmsUrl + "/Api/Activity";

						String result = "";
						String activationId = "";

						try {

							JSONObject activationJsonObj = new JSONObject();
							JSONObject resultJsonObj = null;

							activationJsonObj.put("InstallationId",
									InstallationId);
							activationJsonObj.put("DealerTvStationId",
									channelNumber);
							activationJsonObj.put("Field", userName);
							activationJsonObj.put("Field2", userPassword);
							activationJsonObj
									.put("Location", LastKnownLocation);
							activationJsonObj.put("ActivationId",
									SavedActivationId);

							resultJsonObj = JsonUtils.sendJson(url,
									activationJsonObj);

							if (resultJsonObj != null) {
								result = resultJsonObj.getString("Result");
								activationId = resultJsonObj
										.getString("ActivationId");
							}

						} catch (JSONException e) {

							Log.e("DealerTv", e.getMessage());
						}

						if (result.equals("Fail - Unknown Error")
								|| !Installation.activation(getBaseContext(),
										"").equals(activationId)) {
							Log.i("DealerTv", "Activity Logging Failed");

						}
						if (result.equals("Pass - Activity Logged")
								&& Installation
										.activation(getBaseContext(), "")
										.equals(activationId)) {
							Log.i("DealerTv", "Activity Logged Succesfully");
						}

					}
				} catch (Exception e) {

					Log.e("DealerTv", "Activity  Log Failed: " + e.getMessage());

				}
			}
		};

		// Production
		scheduler.scheduleAtFixedRate(activity, 0, 30, MINUTES);
		// Testing
		// scheduler.scheduleAtFixedRate(activity, 0, 15, SECONDS);
	}

	private void startSlideShowOnceViewableSlideBecomesAvailable() {
		final Runnable startSlideShow = new Runnable() {
			public void run() {

				if (!isSlideShowPlaying) {

					startSlideShow();

				}
			}
		};
		scheduler.schedule(startSlideShow, 10, SECONDS);

	}

	private void retryToStopScheduledUpdateConfigFileTask() {
		final Runnable stopScheduledUpdateConfigFileTask = new Runnable() {
			public void run() {

				try {
					isCurrentlyUpdatingTimeInterval = true;
					if (updateConfigFileScheduledFuture.cancel(false)) {
						updateOnSetUpdateInterval();
						isCurrentlyUpdatingTimeInterval = false;
					} else {
						retryToStopScheduledUpdateConfigFileTask();
					}

				} catch (Exception e) {
					Log.e("DealerTv", e.getMessage());
				}

			}
		};

		scheduler.schedule(stopScheduledUpdateConfigFileTask, 10, SECONDS);
	}

}
