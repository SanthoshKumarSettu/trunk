package com.dealersaleschannel.tv;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {

	private UtilityFunctions utils = new UtilityFunctions();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.settings);
		
		
		
		boolean isActive = this.getPreferenceManager().getSharedPreferences().getBoolean("IsActive", false);
		
		if(isActive)
		{		
			EditTextPreference zipCodePreference = (EditTextPreference) this.findPreference(this.getString(R.string.pref_zip_code));
			this.getPreferenceScreen().removePreference(zipCodePreference);
		}		

	}

	@Override
	public void onResume() {
		super.onResume();
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);

	}

	@Override
	public void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

				if (key.equals(getString(R.string.pref_updateInterval))) {

					SharedPreferences settings = sharedPreferences;

					String updateInterval = settings.getString(
							getString(R.string.pref_updateInterval), "15");

					if (updateInterval.equals("")
							|| !utils.isInteger(updateInterval)) {
						updateInterval = "15";

						// SharedPreferences.Editor editor = settings.edit();
						//
						// editor.putString(getString(R.string.pref_updateInterval),
						// updateInterval);
						//
						// editor.commit();

						EditTextPreference updateIntervalPreference = (EditTextPreference) this.findPreference(key);

						updateIntervalPreference.setText(updateInterval);

					}

				}

				if (key.equals(getString(R.string.pref_webViewZoomLevel))) {

					SharedPreferences settings = sharedPreferences;

					String webViewZoomLevel = settings.getString(
							getString(R.string.pref_webViewZoomLevel), "100");

					if (webViewZoomLevel.equals("")
							|| !utils.isInteger(webViewZoomLevel)) {
						webViewZoomLevel = "100";

						// SharedPreferences.Editor editor = settings.edit();
						//
						// editor.putString(getString(R.string.pref_webViewZoomLevel),
						// webViewZoomLevel);
						//
						// editor.commit();

						EditTextPreference updateIntervalPreference = (EditTextPreference) this.findPreference(key);

						updateIntervalPreference.setText(webViewZoomLevel);

					}
				}	
				
			

	}
	
}
