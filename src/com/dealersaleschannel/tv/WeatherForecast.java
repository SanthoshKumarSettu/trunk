package com.dealersaleschannel.tv;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class WeatherForecast {
	private List<WeatherForecastDay> weather10DayForecast;
	private String apiKey;
	private String zipCode;
	private String cmsUrl;
	private final String weather10dayForecastServiceUrl;

	public WeatherForecast(String apiKey, String zipCode) {
		this.apiKey = apiKey;
		this.zipCode = zipCode;

		this.weather10dayForecastServiceUrl = "http://api.wunderground.com/api/"
				+ apiKey + "/forecast10day/q/" + zipCode + ".json";

		this.weather10DayForecast = new ArrayList<WeatherForecastDay>();

		Generate10DayForecastList();
	}

	public WeatherForecast(String cmsUrl, Context context) {
		this.cmsUrl = cmsUrl;

		this.weather10dayForecastServiceUrl = this.cmsUrl + File.separator
				+ "forecast10day";

		this.weather10DayForecast = new ArrayList<WeatherForecastDay>();

		// Check if cached file exists and is
		File[] forecast10dayFiles = context.getFilesDir().listFiles(
				new FilenameFilter() {

					@Override
					public boolean accept(File dir, String filename) {
						return filename.startsWith("forecast10day");
					}
				});

		if (forecast10dayFiles.length >= 1) {
			// Order By Creation Date, Older first
			Arrays.sort(forecast10dayFiles, new Comparator<File>() {
				public int compare(File f1, File f2) {
					return Long.valueOf(f1.lastModified()).compareTo(
							f2.lastModified());
				}
			});

//			long expirationDate = System.currentTimeMillis() - (11 * 60 * 1000);
//
			// Get File Last Modified Date of
			File latestForecast10dayFile = forecast10dayFiles[forecast10dayFiles.length - 1];
//
//			if (latestForecast10dayFile.lastModified() > expirationDate) {
				Generate10DayForecastListFromFile(latestForecast10dayFile);
//			} else {
//				Generate10DayForecastListFromCMSUrl();
//			}
		}else
		{
			Generate10DayForecastListFromCMSUrl();
		}

	}

	public String getApiKey() {
		return apiKey;
	}

	public String getZipCode() {
		return zipCode;
	}

	public List<WeatherForecastDay> getWeather10DayForecast() {
		return weather10DayForecast;
	}

	private void Generate10DayForecastList() {
		JSONObject jsonForecast10day = JsonUtils
				.SendHttpGet(this.weather10dayForecastServiceUrl);

		try {
			JSONObject forecast = jsonForecast10day.getJSONObject("forecast");

			JSONObject simpleForecast = forecast
					.getJSONObject("simpleforecast");

			JSONArray forecastDay = simpleForecast.getJSONArray("forecastday");

			for (int i = 0; i < 6; i++) {

				WeatherForecastDay day = new WeatherForecastDay();

				JSONObject jsonDayObj = (JSONObject) forecastDay.get(i);

				if (jsonDayObj.getString("period").equals("1")) {
					day.setDay("Today");
				}

				if (jsonDayObj.getString("period").equals("2")) {
					day.setDay("Tomorrow");
				}

				if (!jsonDayObj.getString("period").equals("1")
						&& !jsonDayObj.getString("period").equals("2")) {
					String dayNames[] = new DateFormatSymbols().getWeekdays();

					Calendar calendar = Calendar.getInstance();

					int dayNumber = calendar.get(Calendar.DAY_OF_WEEK)
							+ (Integer.parseInt(jsonDayObj.getString("period")) - 1);

					while (dayNumber > 7) {
						dayNumber = dayNumber - 7;
					}

					// To be fixed
					String dayName = dayNames[dayNumber];

					day.setDay(dayName);

				}

				day.setWeather(jsonDayObj.getString("conditions"));
				day.setWeatherImage(jsonDayObj.getString("icon") + ".png");

				JSONObject high = jsonDayObj.getJSONObject("high");

				day.setHighTemperature(high.getString("fahrenheit"));

				JSONObject low = jsonDayObj.getJSONObject("low");

				day.setLowTemperature(low.getString("fahrenheit"));

				day.setPercentOfPrecipitation(jsonDayObj.getString("pop") + "%");

				this.getWeather10DayForecast().add(day);

			}

		} catch (JSONException e) {

			Log.e("DealerTv",
					"Weather forecast exception: " + e.getLocalizedMessage());
		}

	}

	private void Generate10DayForecastListFromCMSUrl() {

		JSONObject jsonForecast10dayContainer = JsonUtils
				.SendHttpGet(this.weather10dayForecastServiceUrl);

		JSONObject jsonForecast10day = null;
		try {

			jsonForecast10day = new JSONObject(
					jsonForecast10dayContainer.getString("Json"));

		} catch (JSONException e) {
			Log.e("DealerTv",
					"Weather forecast10day exception: "
							+ e.getLocalizedMessage());
		}

		try {
			JSONObject forecast = jsonForecast10day.getJSONObject("forecast");

			JSONObject simpleForecast = forecast
					.getJSONObject("simpleforecast");

			JSONArray forecastDay = simpleForecast.getJSONArray("forecastday");

			for (int i = 0; i < 6; i++) {

				WeatherForecastDay day = new WeatherForecastDay();

				JSONObject jsonDayObj = (JSONObject) forecastDay.get(i);

				if (jsonDayObj.getString("period").equals("1")) {
					day.setDay("Today");
				}

				if (jsonDayObj.getString("period").equals("2")) {
					day.setDay("Tomorrow");
				}

				if (!jsonDayObj.getString("period").equals("1")
						&& !jsonDayObj.getString("period").equals("2")) {
					String dayNames[] = new DateFormatSymbols().getWeekdays();

					Calendar calendar = Calendar.getInstance();

					int dayNumber = calendar.get(Calendar.DAY_OF_WEEK)
							+ (Integer.parseInt(jsonDayObj.getString("period")) - 1);

					while (dayNumber > 7) {
						dayNumber = dayNumber - 7;
					}

					// To be fixed
					String dayName = dayNames[dayNumber];

					day.setDay(dayName);

				}

				day.setWeather(jsonDayObj.getString("conditions"));
				day.setWeatherImage(jsonDayObj.getString("icon") + ".png");

				JSONObject high = jsonDayObj.getJSONObject("high");

				day.setHighTemperature(high.getString("fahrenheit"));

				JSONObject low = jsonDayObj.getJSONObject("low");

				day.setLowTemperature(low.getString("fahrenheit"));

				day.setPercentOfPrecipitation(jsonDayObj.getString("pop") + "%");

				this.getWeather10DayForecast().add(day);

			}

		} catch (JSONException e) {

			Log.e("DealerTv",
					"Weather forecast exception: " + e.getLocalizedMessage());
		}

	}

	private void Generate10DayForecastListFromFile(File forecast10dayFile) {

		JSONObject jsonForecast10dayContainer = null;
		try {
			jsonForecast10dayContainer = new JSONObject(
					WeatherCacheControl.readWeatherFile(forecast10dayFile));
		} catch (JSONException e) {
			Log.e("DealerTv",
					"Weather alerts file read exception: "
							+ e.getLocalizedMessage());
		} catch (IOException e) {
			Log.e("DealerTv",
					"Weather alerts file read exception: "
							+ e.getLocalizedMessage());
		}

		JSONObject jsonForecast10day = null;
		try {

			jsonForecast10day = new JSONObject(
					jsonForecast10dayContainer.getString("Json"));

		} catch (JSONException e) {
			Log.e("DealerTv",
					"Weather forecast10day exception: "
							+ e.getLocalizedMessage());
		}

		try {
			JSONObject forecast = jsonForecast10day.getJSONObject("forecast");

			JSONObject simpleForecast = forecast
					.getJSONObject("simpleforecast");

			JSONArray forecastDay = simpleForecast.getJSONArray("forecastday");

			for (int i = 0; i < 6; i++) {

				WeatherForecastDay day = new WeatherForecastDay();

				JSONObject jsonDayObj = (JSONObject) forecastDay.get(i);

				if (jsonDayObj.getString("period").equals("1")) {
					day.setDay("Today");
				}

				if (jsonDayObj.getString("period").equals("2")) {
					day.setDay("Tomorrow");
				}

				if (!jsonDayObj.getString("period").equals("1")
						&& !jsonDayObj.getString("period").equals("2")) {
					String dayNames[] = new DateFormatSymbols().getWeekdays();

					Calendar calendar = Calendar.getInstance();

					int dayNumber = calendar.get(Calendar.DAY_OF_WEEK)
							+ (Integer.parseInt(jsonDayObj.getString("period")) - 1);

					while (dayNumber > 7) {
						dayNumber = dayNumber - 7;
					}

					// To be fixed
					String dayName = dayNames[dayNumber];

					day.setDay(dayName);

				}

				day.setWeather(jsonDayObj.getString("conditions"));
				day.setWeatherImage(jsonDayObj.getString("icon") + ".png");

				JSONObject high = jsonDayObj.getJSONObject("high");

				day.setHighTemperature(high.getString("fahrenheit"));

				JSONObject low = jsonDayObj.getJSONObject("low");

				day.setLowTemperature(low.getString("fahrenheit"));

				day.setPercentOfPrecipitation(jsonDayObj.getString("pop") + "%");

				this.getWeather10DayForecast().add(day);

			}

		} catch (JSONException e) {

			Log.e("DealerTv",
					"Weather forecast exception: " + e.getLocalizedMessage());
		}

	}

}
