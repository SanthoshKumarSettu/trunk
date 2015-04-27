package com.dealersaleschannel.tv;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class WeatherAlerts {
	
	private String apiKey;
	private String zipCode;
	private String cmsUrl;
	private final String weatherAlertsServiceUrl;
	private List<WeatherAlert> weatherAlerts; 
	private String allWeatherAlertsString;
	

	public WeatherAlerts(String apiKey, String zipCode)
	{
		this.apiKey = apiKey;
		this.zipCode = zipCode;
		
		this.weatherAlerts = new ArrayList<WeatherAlert>();
		
		this.weatherAlertsServiceUrl = "http://api.wunderground.com/api/"
				+ apiKey + "/alerts/q/" + zipCode + ".json";
		
		GenerateAlerts();
	}
	
	public WeatherAlerts(String cmsUrl, Context context)
	{
		this.cmsUrl = cmsUrl;		
		
		this.weatherAlerts = new ArrayList<WeatherAlert>();
		
		this.weatherAlertsServiceUrl = this.cmsUrl + File.separator + "alerts";
		
		
		//Check if cached file exists and is 
		File[] alertsFiles = context.getFilesDir().listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String filename) {							
				return filename.startsWith("alerts");
			}
		});
		
		if(alertsFiles.length >= 1)
		{
			//Order By Creation Date, Older first
			Arrays.sort(alertsFiles, new Comparator<File>(){
			    public int compare(File f1, File f2)
			    {
			        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
			    } });
			
//			long expirationDate = System.currentTimeMillis() - (11 * 60 * 1000);
//			
			//Get File Last Modified Date of 
			File latestAlertsFile = alertsFiles[alertsFiles.length-1]; 
//			
//			if(latestAlertsFile.lastModified() > expirationDate)
//			{
				GenerateAlertsFromFile(latestAlertsFile);
//			}else
//			{
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
	
	public List<WeatherAlert> getWeatherAlerts() {
		return weatherAlerts;
	}

	private void GenerateAlerts()
	{
		JSONObject jsonAlerts = JsonUtils
				.SendHttpGet(this.weatherAlertsServiceUrl);
		
		String allWeatherAlerts = "";
		
		try
		{
			JSONArray alerts = jsonAlerts.getJSONArray("alerts");
			
			for (int i = 0; i < alerts.length(); i++) {

				WeatherAlert alert = new WeatherAlert();

				JSONObject jsonAlert = (JSONObject) alerts.get(i);

				alert.setType(jsonAlert.getString("type"));
				alert.setDescription(jsonAlert.getString("description"));
				alert.setDate(jsonAlert.getString("date"));
				alert.setExpires(jsonAlert.getString("expires"));
				alert.setMessage(jsonAlert.getString("message").replaceAll("\n", " "));
				
				this.getWeatherAlerts().add(alert);
				
				allWeatherAlerts = allWeatherAlerts + String.format("-----%s till %s ", alert.getDescription(), alert.getExpires());
			}
			
			this.setAllWeatherAlertsString(allWeatherAlerts);
			
		}catch (JSONException  e) {
			Log.e("DealerTv",	"Weather alerts exception: " + e.getLocalizedMessage());
		}
	
	}
	
	private void GenerateAlertsFromCMSUrl()
	{
		JSONObject jsonAlertsContainer = JsonUtils
				.SendHttpGet(this.weatherAlertsServiceUrl);
		
		JSONObject jsonAlerts = null;
		
		try {
			jsonAlerts = new JSONObject(jsonAlertsContainer.getString("Json"));
		} catch (JSONException e) {
			Log.e("DealerTv",	"Weather alerts exception: " + e.getLocalizedMessage());
		}
		
		String allWeatherAlerts = "";
		
		try
		{
			JSONArray alerts = jsonAlerts.getJSONArray("alerts");
			
			for (int i = 0; i < alerts.length(); i++) {

				WeatherAlert alert = new WeatherAlert();

				JSONObject jsonAlert = (JSONObject) alerts.get(i);

				alert.setType(jsonAlert.getString("type"));
				alert.setDescription(jsonAlert.getString("description"));
				alert.setDate(jsonAlert.getString("date"));
				alert.setExpires(jsonAlert.getString("expires"));
				alert.setMessage(jsonAlert.getString("message").replaceAll("\n", " "));
				
				this.getWeatherAlerts().add(alert);
				
				allWeatherAlerts = allWeatherAlerts + String.format("-----%s till %s ", alert.getDescription(), alert.getExpires());
			}
			
			this.setAllWeatherAlertsString(allWeatherAlerts);
			
		}catch (JSONException  e) {
			Log.e("DealerTv",	"Weather alerts exception: " + e.getLocalizedMessage());
		}
	
	}
	
	private void GenerateAlertsFromFile(File alertsFile)
	{
		
		
		JSONObject jsonAlertsContainer = null;
		try {
			jsonAlertsContainer = new JSONObject(WeatherCacheControl.readWeatherFile(alertsFile));
		} catch (JSONException e) {
			Log.e("DealerTv",	"Weather alerts file read exception: " + e.getLocalizedMessage());
		} catch (IOException e) {
			Log.e("DealerTv",	"Weather alerts file read exception: " + e.getLocalizedMessage());
		}
		
		JSONObject jsonAlerts = null;
		
		try {
			jsonAlerts = new JSONObject(jsonAlertsContainer.getString("Json"));
		} catch (JSONException e) {
			Log.e("DealerTv",	"Weather alerts exception: " + e.getLocalizedMessage());
		}
		
		String allWeatherAlerts = "";
		
		try
		{
			JSONArray alerts = jsonAlerts.getJSONArray("alerts");
			
			for (int i = 0; i < alerts.length(); i++) {

				WeatherAlert alert = new WeatherAlert();

				JSONObject jsonAlert = (JSONObject) alerts.get(i);

				alert.setType(jsonAlert.getString("type"));
				alert.setDescription(jsonAlert.getString("description"));
				alert.setDate(jsonAlert.getString("date"));
				alert.setExpires(jsonAlert.getString("expires"));
				alert.setMessage(jsonAlert.getString("message").replaceAll("\n", " "));
				
				this.getWeatherAlerts().add(alert);
				
				allWeatherAlerts = allWeatherAlerts + String.format("-----%s till %s ", alert.getDescription(), alert.getExpires());
			}
			
			this.setAllWeatherAlertsString(allWeatherAlerts);
			
		}catch (JSONException  e) {
			Log.e("DealerTv",	"Weather alerts exception: " + e.getLocalizedMessage());
		}
	
	}
	
	
	public String getAllWeatherAlertsString() {
		return allWeatherAlertsString;
	}

	private void setAllWeatherAlertsString(String allWeatherAlertsString) {
		this.allWeatherAlertsString = allWeatherAlertsString;
	}
}
