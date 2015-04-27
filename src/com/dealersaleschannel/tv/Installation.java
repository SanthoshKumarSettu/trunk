package com.dealersaleschannel.tv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;


public class Installation {
	private static String sID = null;
	private static String aID = null;
	private static final String INSTALLATION = "INSTALLATION";
	private static final String ACTIVATION = "ACTIVATION";	

	public synchronized static String id(Context context) {
		if (sID == null) {
			File installation = new File(context.getFilesDir(), INSTALLATION);
			try {
				if (!installation.exists())
					writeInstallationFile(installation);
				sID = readFile(installation);
			} catch (Exception e) {
				Log.e("DealerTv", e.getMessage());
			}
		}
		return sID;
	}
	
	public synchronized static String activation(Context context, String activationId) {
		//File activations = new File(context.getFilesDir(), ACTIVATION);
		//activations.delete();
		if (aID == null) {
			File activation = new File(context.getFilesDir(), ACTIVATION);
			try {
				if (!activation.exists() && !activationId.equals("") && activationId != null)
					writeActivationFile(activation, activationId);									
				aID = readFile(activation);
			} catch (Exception e) {
				Log.e("DealerTv", e.getMessage());
			}
		}
		
		return aID;
	}

	private static String readFile(File file)
			throws IOException {
		RandomAccessFile f = new RandomAccessFile(file, "r");
		byte[] bytes = new byte[(int) f.length()];
		f.readFully(bytes);
		f.close();
		return new String(bytes);
	}

	private static void writeInstallationFile(File installation)
			throws IOException {
		FileOutputStream out = new FileOutputStream(installation);
		String id = UUID.randomUUID().toString();
		out.write(id.getBytes());
		out.close();
	}
	
	private static void writeActivationFile(File activation, String activationId)
			throws IOException {
		FileOutputStream out = new FileOutputStream(activation);
		out.write(activationId.getBytes());
		out.close();
	}

	public static String getLastKnownLocation(Context context) {
		
		try {
			
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(context);
			
			String zipCode = settings.getString(context.getString(R.string.pref_zip_code), "");
			
			return zipCode;

		} catch (Exception e) {
			Log.e("Dealer Tv", e.getMessage());
			return "";
		}
		
		
	}
}