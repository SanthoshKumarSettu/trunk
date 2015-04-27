package com.dealersaleschannel.tv;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;

public class DownloadFile extends AsyncTask<String, Integer, String> {

	public String status = "ready";
	public static boolean syncing = false;

	// Parameter 0 should be the URL
	// Parameter 1 should be the fileSavePath
	// Parameter 2 should be the fileSavePaths for Bulk Downloading
	@Override
	protected String doInBackground(String... params) {
		try {

			if (params.length == 2) {

				URL url = new URL(params[0]);
				HttpURLConnection  connection = (HttpURLConnection)url.openConnection();
				connection.connect();
				// this will be useful so that you can show a typical 0-100%
				// progress bar
				// int fileLength = connection.getContentLength();

				// download the file
				InputStream input = new BufferedInputStream(url.openStream());

				OutputStream output = new FileOutputStream(params[1] + ".Temp");

				File oldfile = new File(params[1] + ".Temp");
				File newfile = new File(params[1]);

				byte data[] = new byte[1024];

				try {

					// long total = 0;
					int count;
					while ((count = input.read(data)) != -1) {
						// total += count;
						// publishing the progress....
						// publishProgress((int) (total * 100 / fileLength));
						output.write(data, 0, count);
					}

					if (oldfile.renameTo(newfile)) {
						System.out.println("Download Succesful: " + params[1]);
					} else {
						System.out.println("Download Failed : " + params[1]);
					}

				} finally {

					output.flush();
					output.close();
					input.close();
					connection.disconnect();
					
					output = null;
					input = null;
					output = null;
					connection = null;
					url = null;
					oldfile = null;
					newfile = null;
					data = null;

				}

			} else

			{

				
				String urlAndFilePathCombinationsString = params[2];

				if (urlAndFilePathCombinationsString.startsWith(",,")) {
					urlAndFilePathCombinationsString = urlAndFilePathCombinationsString
							.substring(2);
				}

				String[] urlAndFilePathCombinations = urlAndFilePathCombinationsString
						.split(",,");
				
				int throttle = 0;

				for (String urlAndFilePath : urlAndFilePathCombinations) {
					try {
						throttle = throttle + 1;
						if(throttle > 100)
						{
							System.gc();							
							throttle = 0;
							
						}
						
						String[] params2 = urlAndFilePath.split(",");

						URL url = new URL(params2[0]);
						HttpURLConnection  connection = (HttpURLConnection)url.openConnection();
						connection.connect();
						// this will be useful so that you can show a typical
						// 0-100%
						// progress bar
						// int fileLength = connection.getContentLength();

						// download the file
						InputStream input = new BufferedInputStream(
								url.openStream());

						OutputStream output = new FileOutputStream(params2[1]
								+ ".Temp");

						File oldfile = new File(params2[1] + ".Temp");
						File newfile = new File(params2[1]);

						byte data[] = new byte[1024];

						try {

							// long total = 0;
							int count;
							while ((count = input.read(data)) != -1) {
								// total += count;
								// publishing the progress....
								// publishProgress((int) (total * 100 /
								// fileLength));
								output.write(data, 0, count);
							}

							if (oldfile.renameTo(newfile)) {
								System.out.println("Download Succesful: "
										+ params2[1]);
							} else {
								System.out.println("Download Failed : "
										+ params2[1]);
							}

						} finally {

							output.flush();
							output.close();
							input.close();
							connection.disconnect();
							
							
							output = null;
							input = null;
							output = null;
							connection = null;
							url = null;
							oldfile = null;
							newfile = null;
							data = null;
							
						}

					} catch (Exception e) {
						System.out
								.println("Download Failed: " + e.getMessage());
					}

				}
				syncing = false;
				System.out.println("Sync Download Completed");
			}

			status = "ready";

		} catch (Exception e) {

			System.out.println("Download Failed: " + e.getMessage());
			status = "ready";
			syncing = false;

		}

		return status;
	}

}
