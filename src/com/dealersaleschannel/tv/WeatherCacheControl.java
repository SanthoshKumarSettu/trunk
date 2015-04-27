package com.dealersaleschannel.tv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;


import org.json.JSONObject;

public class WeatherCacheControl {
	
	public static String readWeatherFile(File file)
			throws IOException {
		RandomAccessFile f = new RandomAccessFile(file, "r");
		byte[] bytes = new byte[(int) f.length()];
		f.readFully(bytes);
		f.close();
		return new String(bytes);
	}

	public static void writeFile(File file, JSONObject weatherJson)
			throws IOException {
		FileOutputStream out = new FileOutputStream(file);
		out.write(weatherJson.toString().getBytes());
		out.close();
	}
	
	public static void writeFile(File file, String weatherInfo)
			throws IOException {
		FileOutputStream out = new FileOutputStream(file);
		out.write(weatherInfo.getBytes());
		out.close();
	}

	public static void deleteOldWeatherFiles()
			throws IOException 
	{
		
	}

}
