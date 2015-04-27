package com.dealersaleschannel.tv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JsonUtils {

	private static EncryptionTools encryptionTools = new EncryptionTools();

	public static JSONObject sendJson(String url, JSONObject activationJsonObj) {
		JSONObject resultJsonObj = null;

		try {

			String encryptedDataString = encryptionTools.encrypt(
					activationJsonObj.toString(),
					"21EA0993-4AD3-4452-ACEC-52E8B474A729");

			JSONObject encryptedJsonObjSend = new JSONObject();
			encryptedJsonObjSend.put("Hash", encryptedDataString);

			JSONObject encryptedJsonObjReceived = JsonUtils
					.SendHttpPost(url, encryptedJsonObjSend);

			String decryptedJsonObject = encryptionTools.decrypt(
					encryptedJsonObjReceived.getString("Hash"),
					"21EA0993-4AD3-4452-ACEC-52E8B474A729");

			resultJsonObj = new JSONObject(decryptedJsonObject);

		} catch (InvalidKeyException e) {

			Log.e("DealerTv", e.getMessage());// + e.getMessage());
		} catch (UnsupportedEncodingException e) {

			Log.e("DealerTv", e.getMessage());// + e.getMessage());
		} catch (NoSuchAlgorithmException e) {

			Log.e("DealerTv", e.getMessage());// + e.getMessage());
		} catch (NoSuchPaddingException e) {

			Log.e("DealerTv", e.getMessage());// + e.getMessage());
		} catch (InvalidAlgorithmParameterException e) {

			Log.e("DealerTv", e.getMessage());// + e.getMessage());
		} catch (IllegalBlockSizeException e) {

			Log.e("DealerTv", e.getMessage());// + e.getMessage());
		} catch (BadPaddingException e) {

			Log.e("DealerTv", e.getMessage());// + e.getMessage());
		} catch (JSONException e) {

			Log.e("DealerTv", e.getMessage());// + e.getMessage());

		} catch (Exception e) {

			Log.i("DealerTv", e.getMessage());// +
																	// e.getMessage());

		}

		return resultJsonObj;
	}

	public static JSONObject SendHttpPost(final String URL,
			final JSONObject jsonObjSend) {

		JSONObject returnedJSONObject = null;

		ExecutorService executor = Executors.newSingleThreadExecutor();
		Callable<JSONObject> callable = new Callable<JSONObject>() {

			public JSONObject call() {

				try {

					DefaultHttpClient httpclient = new DefaultHttpClient();
					HttpPost httpPostRequest = new HttpPost(URL);

					StringEntity se;
					se = new StringEntity(jsonObjSend.toString());

					// Set HTTP parameters
					httpPostRequest.setEntity(se);
					httpPostRequest.setHeader("Accept", "application/json");
					httpPostRequest.setHeader("Content-type",
							"application/json");

					long t = System.currentTimeMillis();
					HttpResponse response = (HttpResponse) httpclient
							.execute(httpPostRequest);
					Log.i("DealerTv",
							"HTTPResponse received in ["
									+ (System.currentTimeMillis() - t) + "ms]");

					// Get hold of the response entity (-> the data):
					HttpEntity entity = response.getEntity();

					if (entity != null) {
						// Read the content stream
						InputStream instream = entity.getContent();

						// convert content stream to a String
						String resultString = convertStreamToString(instream);
						instream.close();

						// Transform the String into a JSONObject
						JSONObject jsonObjRecv = new JSONObject(resultString);

						// Raw DEBUG output of our received JSON object:
						// Log.i("DealerTv", "<JSONObject>\n" +
						// jsonObjRecv.toString() +
						// "\n</JSONObject>");

						return jsonObjRecv;
					}

				} catch (Exception e) {
					// More about HTTP exception handling in another tutorial.
					// For now we just print the stack trace.
					Log.i("DealerTv", "JsonPost did not return any data");// +
																			// e.getMessage());

				}

				return null;

			}
		};

		Future<JSONObject> future = executor.submit(callable);

		try {
			returnedJSONObject = future.get();
		} catch (InterruptedException e) {

			Log.e("DealerTv", "JSonPost Failed");
		} catch (ExecutionException e) {

			Log.e("DealerTv", "JSonPost Failed");
		}

		executor.shutdown();

		return returnedJSONObject;
	}

	public static JSONObject SendHttpGet(final String URL) {

		JSONObject returnedJSONObject = null;

		ExecutorService executor = Executors.newSingleThreadExecutor();
		Callable<JSONObject> callable = new Callable<JSONObject>() {

			public JSONObject call() {

				try {

					DefaultHttpClient httpclient = new DefaultHttpClient();
					HttpGet httpGetRequest = new HttpGet(URL);

					// Set HTTP parameters
					httpGetRequest.setHeader("Accept", "application/json");
					httpGetRequest.setHeader("Content-type",
							"application/json");

					long t = System.currentTimeMillis();
					HttpResponse response = (HttpResponse) httpclient
							.execute(httpGetRequest);
					Log.i("DealerTv",
							"HTTPResponse received in ["
									+ (System.currentTimeMillis() - t) + "ms]");

					// Get hold of the response entity (-> the data):
					HttpEntity entity = response.getEntity();

					if (entity != null) {
						// Read the content stream
						InputStream instream = entity.getContent();

						// convert content stream to a String
						String resultString = convertStreamToString(instream);
						instream.close();

						// Transform the String into a JSONObject
						JSONObject jsonObjRecv = new JSONObject(resultString);

						// Raw DEBUG output of our received JSON object:
						// Log.i("DealerTv", "<JSONObject>\n" +
						// jsonObjRecv.toString() +
						// "\n</JSONObject>");

						return jsonObjRecv;
					}

				} catch (Exception e) {
					// More about HTTP exception handling in another tutorial.
					// For now we just print the stack trace.
					Log.i("DealerTv", "JsonPost did not return any data");// +
																			// e.getMessage());

				}

				return null;

			}
		};

		Future<JSONObject> future = executor.submit(callable);

		try {
			returnedJSONObject = future.get();
		} catch (InterruptedException e) {

			Log.e("DealerTv", "JSonPost Failed");
		} catch (ExecutionException e) {

			Log.e("DealerTv", "JSonPost Failed");
		}

		executor.shutdown();

		return returnedJSONObject;
	}
	
	private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			Log.e("DealerTv", "Stream to String Conversion failed.");
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				Log.e("DealerTv", "Stream to String Conversion failed.");
			}
		}
		return sb.toString();
	}

}
