package com.dealersaleschannel.tv;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class WeatherCurrentConditions {
	private String apiKey;
	private String zipCode;
	private String cmsUrl;
	private final String weatherCurrentConditionsServiceUrl;
	private final String weatherAstronomyServiceUrl;
	private String day;
	private String weather;
	private String weatherImage;
	private String temperature;
	private String feelsLikeTemperature;
	private String windInfo;
	private String sunriseTime;
	private String sunsetTime;
	private String location;
	private String humidity;
	private String pressure;

	public WeatherCurrentConditions(String apiKey, String zipCode) {
		this.apiKey = apiKey;
		this.zipCode = zipCode;

		this.weatherCurrentConditionsServiceUrl = "http://api.wunderground.com/api/"
				+ apiKey + "/conditions/q/" + zipCode + ".json";

		this.weatherAstronomyServiceUrl = "http://api.wunderground.com/api/"
				+ apiKey + "/astronomy/q/" + zipCode + ".json";

		GenerateCurrentConditions();

	}

	public WeatherCurrentConditions(String cmsUrl, Context context) {

		this.cmsUrl = cmsUrl;

		this.weatherCurrentConditionsServiceUrl = this.cmsUrl + File.separator
				+ "conditions";

		this.weatherAstronomyServiceUrl = this.cmsUrl + File.separator
				+ "astronomy";

		// Check if cached file exists and is
		File[] currentConditionsFiles = context.getFilesDir().listFiles(
				new FilenameFilter() {

					@Override
					public boolean accept(File dir, String filename) {
						return filename.startsWith("conditions");
					}
				});
		
		File[] astronomyFiles = context.getFilesDir().listFiles(
				new FilenameFilter() {

					@Override
					public boolean accept(File dir, String filename) {
						return filename.startsWith("astronomy");
					}
				});
		

		if (currentConditionsFiles.length >= 1 && astronomyFiles.length >= 1) {
			// Order By Creation Date, Older first
			Arrays.sort(currentConditionsFiles, new Comparator<File>() {
				public int compare(File f1, File f2) {
					return Long.valueOf(f1.lastModified()).compareTo(
							f2.lastModified());
				}
			});
			
			Arrays.sort(astronomyFiles, new Comparator<File>() {
				public int compare(File f1, File f2) {
					return Long.valueOf(f1.lastModified()).compareTo(
							f2.lastModified());
				}
			});

//			long expirationDate = System.currentTimeMillis() - (11 * 60 * 1000);

			// Get File Last Modified Date of
			File latestCurrentConditionsFile = currentConditionsFiles[currentConditionsFiles.length - 1];
			File latestAstronomyFile = astronomyFiles[astronomyFiles.length - 1];
			

//			if (latestCurrentConditionsFile.lastModified() > expirationDate && latestAstronomyFile.lastModified() > expirationDate) {
				GenerateAlertsFromFile(latestCurrentConditionsFile, latestAstronomyFile);
//			} else {
//				GenerateAlertsFromCMSUrl();
//			}
		}else
		{
			GenerateAlertsFromCMSUrl();
		}

	}

	public String getApiKey() {
		return apiKey;
	}

	public String getZipCode() {
		return zipCode;
	}

	public String getCmsUrl() {
		return cmsUrl;
	}

	public String getDay() {
		return day;
	}

	private void setDay(String day) {
		this.day = day;
	}

	public String getWeather() {
		return weather;
	}

	private void setWeather(String weather) {
		this.weather = weather;
	}

	public String getWeatherImage() {
		return weatherImage;
	}

	private void setWeatherImage(String weatherImage) {
		this.weatherImage = weatherImage;
	}

	public String getTemperature() {
		return temperature;
	}

	private void setTemperature(String temperature) {
		this.temperature = temperature;
	}

	public String getFeelsLikeTemperature() {
		return feelsLikeTemperature;
	}

	private void setFeelsLikeTemperature(String feelsLikeTemperature) {
		this.feelsLikeTemperature = feelsLikeTemperature;
	}

	public String getWindInfo() {
		return windInfo;
	}

	private void setWindInfo(String windInfo) {
		this.windInfo = windInfo;
	}

	public String getSunriseTime() {
		return sunriseTime;
	}

	private void setSunriseTime(String sunriseTime) {
		this.sunriseTime = sunriseTime;
	}

	public String getSunsetTime() {
		return sunsetTime;
	}

	private void setSunsetTime(String sunsetTime) {
		this.sunsetTime = sunsetTime;
	}

	public String getLocation() {
		return location;
	}

	private void setLocation(String location) {
		this.location = location;
	}

	public String getHumidity() {
		return humidity;
	}

	private void setHumidity(String humidity) {
		this.humidity = humidity;
	}

	public String getPressure() {
		return pressure;
	}

	private void setPressure(String pressure) {
		this.pressure = pressure;
	}

	private void GenerateCurrentConditions() {

		JSONObject jsonCurrentConditions = JsonUtils
				.SendHttpGet(this.weatherCurrentConditionsServiceUrl);

		JSONObject jsonAstronomy = JsonUtils
				.SendHttpGet(this.weatherAstronomyServiceUrl);

		try {
			JSONObject moonPhase = jsonAstronomy.getJSONObject("moon_phase");

			JSONObject currentObservation = jsonCurrentConditions
					.getJSONObject("current_observation");

			setDay("Now");
			setWeather(currentObservation.getString("weather"));
			setWeatherImage(currentObservation.getString("icon") + ".png");
			setTemperature(currentObservation.getString("temp_f"));
			setFeelsLikeTemperature(currentObservation.getString("feelslike_f"));
			setWindInfo(currentObservation.getString("wind_string"));

			JSONObject sunrise = moonPhase.getJSONObject("sunrise");

			setSunriseTime(String.format("%s:%s AM", sunrise.get("hour"),
					sunrise.get("minute")));

			JSONObject sunset = moonPhase.getJSONObject("sunset");

			Integer hour = Integer.parseInt(sunset.getString("hour"));

			if (hour > 12) {
				hour = hour - 12;
			}

			setSunsetTime(String.format("%s:%s PM", hour.toString(),
					sunset.get("minute")));

			JSONObject displayLocation = currentObservation
					.getJSONObject("display_location");

			setLocation(displayLocation.getString("full"));

			setHumidity(currentObservation.getString("relative_humidity"));

			setPressure(currentObservation.getString("pressure_in"));

			// Moon phase is available but images are not --add here in the
			// future

		} catch (JSONException e) {
			Log.e("DealerTv",
					"Weather current conditions exception: "
							+ e.getLocalizedMessage());
		}

	}

	private void GenerateAlertsFromCMSUrl() {

		JSONObject jsonCurrentConditionsContainer = JsonUtils
				.SendHttpGet(this.weatherCurrentConditionsServiceUrl);

		JSONObject jsonAstronomyContainer = JsonUtils
				.SendHttpGet(this.weatherAstronomyServiceUrl);

		JSONObject jsonCurrentConditions = null;
		JSONObject jsonAstronomy = null;

		try {
			jsonCurrentConditions = new JSONObject(
					jsonCurrentConditionsContainer.getString("Json"));
			jsonAstronomy = new JSONObject(
					jsonAstronomyContainer.getString("Json"));
		} catch (JSONException e) {
			Log.e("DealerTv",
					"Weather current conditions exception: "
							+ e.getLocalizedMessage());
		}

		try {
			JSONObject moonPhase = jsonAstronomy.getJSONObject("moon_phase");

			JSONObject currentObservation = jsonCurrentConditions
					.getJSONObject("current_observation");

			setDay("Now");
			setWeather(currentObservation.getString("weather"));
			setWeatherImage(currentObservation.getString("icon") + ".png");
			setTemperature(currentObservation.getString("temp_f"));
			setFeelsLikeTemperature(currentObservation.getString("feelslike_f"));
			setWindInfo(currentObservation.getString("wind_string"));

			JSONObject sunrise = moonPhase.getJSONObject("sunrise");

			setSunriseTime(String.format("%s:%s AM", sunrise.get("hour"),
					sunrise.get("minute")));

			JSONObject sunset = moonPhase.getJSONObject("sunset");

			Integer hour = Integer.parseInt(sunset.getString("hour"));

			if (hour > 12) {
				hour = hour - 12;
			}

			setSunsetTime(String.format("%s:%s PM", hour.toString(),
					sunset.get("minute")));

			JSONObject displayLocation = currentObservation
					.getJSONObject("display_location");

			setLocation(displayLocation.getString("full"));

			setHumidity(currentObservation.getString("relative_humidity"));

			setPressure(currentObservation.getString("pressure_in"));

			// Moon phase is available but images are not --add here in the
			// future

		} catch (JSONException e) {
			Log.e("DealerTv",
					"Weather current conditions exception: "
							+ e.getLocalizedMessage());
		}

	}

	private void GenerateAlertsFromFile(File currentConditionsFile, File astronomyFile) {

		JSONObject jsonCurrentConditionsContainer = null;
		JSONObject jsonAstronomyContainer = null;

		try {
			
			jsonCurrentConditionsContainer = new JSONObject(WeatherCacheControl.readWeatherFile(currentConditionsFile));
			jsonAstronomyContainer = new JSONObject(WeatherCacheControl.readWeatherFile(astronomyFile));
			
		} catch (JSONException e) {
			Log.e("DealerTv",	"Weather alerts file read exception: " + e.getLocalizedMessage());
		} catch (IOException e) {
			Log.e("DealerTv",	"Weather alerts file read exception: " + e.getLocalizedMessage());
		}

		JSONObject jsonCurrentConditions = null;
		JSONObject jsonAstronomy = null;

		try {
			jsonCurrentConditions = new JSONObject(
					jsonCurrentConditionsContainer.getString("Json"));
			jsonAstronomy = new JSONObject(
					jsonAstronomyContainer.getString("Json"));
		} catch (JSONException e) {
			Log.e("DealerTv",
					"Weather current conditions exception: "
							+ e.getLocalizedMessage());
		}

		try {
			JSONObject moonPhase = jsonAstronomy.getJSONObject("moon_phase");

			JSONObject currentObservation = jsonCurrentConditions
					.getJSONObject("current_observation");

			setDay("Now");
			setWeather(currentObservation.getString("weather"));
			setWeatherImage(currentObservation.getString("icon") + ".png");
			setTemperature(currentObservation.getString("temp_f"));
			setFeelsLikeTemperature(currentObservation.getString("feelslike_f"));
			setWindInfo(currentObservation.getString("wind_string"));

			JSONObject sunrise = moonPhase.getJSONObject("sunrise");

			setSunriseTime(String.format("%s:%s AM", sunrise.get("hour"),
					sunrise.get("minute")));

			JSONObject sunset = moonPhase.getJSONObject("sunset");

			Integer hour = Integer.parseInt(sunset.getString("hour"));

			if (hour > 12) {
				hour = hour - 12;
			}

			setSunsetTime(String.format("%s:%s PM", hour.toString(),
					sunset.get("minute")));

			JSONObject displayLocation = currentObservation
					.getJSONObject("display_location");

			setLocation(displayLocation.getString("full"));

			setHumidity(currentObservation.getString("relative_humidity"));

			setPressure(currentObservation.getString("pressure_in"));

			// Moon phase is available but images are not --add here in the
			// future

		} catch (JSONException e) {
			Log.e("DealerTv",
					"Weather current conditions exception: "
							+ e.getLocalizedMessage());
		}

	}
}
