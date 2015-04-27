package com.dealersaleschannel.tv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class UtilityFunctions {
	
	public String createDownloadParametersCommaSeparatedString(ArrayList<Slide> slides, String channelNumber, String cmsUrl)
	{
		
		StringBuilder ImageDownloadParametersCommaSeparatedString = new StringBuilder();


		StringBuilder urlBeginning = new StringBuilder();
		urlBeginning.append(cmsUrl);
		urlBeginning.append(File.separator);
		urlBeginning.append("Channels");
		urlBeginning.append(File.separator);
		urlBeginning.append(channelNumber);
		
		StringBuilder url = new StringBuilder();
		StringBuilder urlWithSpecialCharacerEncoding = new StringBuilder();
		
		File file;
		
		ArrayList<String> urlAndFilePathCombinations = new ArrayList<String>();
		
		int syncFileLimit = 2000;
		
		imageSlideLoop:
		for (Slide slide : slides) {
			
			if(syncFileLimit <=0)
			{
				break imageSlideLoop;
			}
			
			if (slide.headerImage != null && !slide.headerImage.equals("")) {
				file = new File(slide.headerImage);
				
				if (!file.exists()) {
					url.setLength(0);
					url.append(urlBeginning);
					url.append(File.separator);
					url.append("images");
					url.append(File.separator);
					url.append(file.getName());

					// Add URL encoding
					urlWithSpecialCharacerEncoding.setLength(0);
					urlWithSpecialCharacerEncoding.append(addURLSpecialCharacterEncoding(url.toString()));
									
			
					
					urlWithSpecialCharacerEncoding.append(",");
					urlWithSpecialCharacerEncoding.append(file.getAbsolutePath());
						
						
						if(!urlAndFilePathCombinations.contains(urlWithSpecialCharacerEncoding.toString()))
						{
							urlAndFilePathCombinations.add(urlWithSpecialCharacerEncoding.toString());
							syncFileLimit = syncFileLimit - 1;
						}
						
					
				}
			}

			if (slide.footerImage != null && !slide.footerImage.equals("")) {
				file = new File(slide.footerImage);

				if (!file.exists()) {
					url.setLength(0);
					url.append(urlBeginning);
					url.append(File.separator);
					url.append("images");
					url.append(File.separator);
					url.append(file.getName());

					// Add URL encoding
					urlWithSpecialCharacerEncoding.setLength(0);
					urlWithSpecialCharacerEncoding.append(addURLSpecialCharacterEncoding(url.toString()));
									
	

			
					
					urlWithSpecialCharacerEncoding.append(",");
					urlWithSpecialCharacerEncoding.append(file.getAbsolutePath());
						
						
						if(!urlAndFilePathCombinations.contains(urlWithSpecialCharacerEncoding.toString()))
						{
							urlAndFilePathCombinations.add(urlWithSpecialCharacerEncoding.toString());
							syncFileLimit = syncFileLimit - 1;
						}
						
					
				}
	
					
				
			}

			if (slide.contentType.equals("image") && slide.data != null
					&& !slide.data.equals("")) {
				file = new File(slide.data);
				
				if (!file.exists()) {
					url.setLength(0);
					url.append(urlBeginning);
					url.append(File.separator);
					url.append("images");
					url.append(File.separator);
					url.append(file.getName());

					// Add URL encoding
					urlWithSpecialCharacerEncoding.setLength(0);
					urlWithSpecialCharacerEncoding.append(addURLSpecialCharacterEncoding(url.toString()));
									
	

			
					
					urlWithSpecialCharacerEncoding.append(",");
					urlWithSpecialCharacerEncoding.append(file.getAbsolutePath());
						
						
						if(!urlAndFilePathCombinations.contains(urlWithSpecialCharacerEncoding.toString()))
						{
							urlAndFilePathCombinations.add(urlWithSpecialCharacerEncoding.toString());
							syncFileLimit = syncFileLimit - 1;
						}
						
					
				}
					

				
				
			}

			if (slide.contentType.equals("equipment") && slide.data != null
					&& !slide.data.equals("")) {
				file = new File(slide.data);

				if (!file.exists()) {
					url.setLength(0);
					url.append(urlBeginning);
					url.append(File.separator);
					url.append("images");
					url.append(File.separator);
					url.append(file.getName());

					// Add URL encoding
					urlWithSpecialCharacerEncoding.setLength(0);
					urlWithSpecialCharacerEncoding.append(addURLSpecialCharacterEncoding(url.toString()));
									
	

			
					
					urlWithSpecialCharacerEncoding.append(",");
					urlWithSpecialCharacerEncoding.append(file.getAbsolutePath());
						
						
						if(!urlAndFilePathCombinations.contains(urlWithSpecialCharacerEncoding.toString()))
						{
							urlAndFilePathCombinations.add(urlWithSpecialCharacerEncoding.toString());
							syncFileLimit = syncFileLimit - 1;
						}
						
					
				}

			
			}

			if (slide.contentType.equals("equipment")
					&& slide.backgroundimage != null
					&& !slide.backgroundimage.equals("")) {
				file = new File(slide.backgroundimage);

				if (!file.exists()) {
					url.setLength(0);
					url.append(urlBeginning);
					url.append(File.separator);
					url.append("images");
					url.append(File.separator);
					url.append(file.getName());

					// Add URL encoding
					urlWithSpecialCharacerEncoding.setLength(0);
					urlWithSpecialCharacerEncoding.append(addURLSpecialCharacterEncoding(url.toString()));
									
	

			
					
					urlWithSpecialCharacerEncoding.append(",");
					urlWithSpecialCharacerEncoding.append(file.getAbsolutePath());
						
						
						if(!urlAndFilePathCombinations.contains(urlWithSpecialCharacerEncoding.toString()))
						{
							urlAndFilePathCombinations.add(urlWithSpecialCharacerEncoding.toString());
							syncFileLimit = syncFileLimit - 1;
						}
						
					
				}
					

		
			}
			
			file = null;
		}
		
		videoSlideLoop:
		for (Slide slide : slides) {
			
			if(syncFileLimit <=0)
			{
				break videoSlideLoop;
			}

			if (slide.contentType.equals("video") && slide.data != null
					&& !slide.data.equals("")) {
				file = new File(slide.data);

				if (!file.exists()) {
					url.setLength(0);
					url.append(urlBeginning);
					url.append(File.separator);
					url.append("videos");
					url.append(File.separator);
					url.append(file.getName());

					// Add URL encoding
					urlWithSpecialCharacerEncoding.setLength(0);
					urlWithSpecialCharacerEncoding.append(addURLSpecialCharacterEncoding(url.toString()));
									
	

			
					
					urlWithSpecialCharacerEncoding.append(",");
					urlWithSpecialCharacerEncoding.append(file.getAbsolutePath());
						
						
						if(!urlAndFilePathCombinations.contains(urlWithSpecialCharacerEncoding.toString()))
						{
							urlAndFilePathCombinations.add(urlWithSpecialCharacerEncoding.toString());
							syncFileLimit = syncFileLimit - 1;
						}
						
					
				}

					
					
				}
			file = null;
			
		}
		
		for (String urlAndFilePathCombination : urlAndFilePathCombinations) {
			
			ImageDownloadParametersCommaSeparatedString.append(urlAndFilePathCombination);
			ImageDownloadParametersCommaSeparatedString.append(",,");
		}
		
		System.gc();
	
		return ImageDownloadParametersCommaSeparatedString.toString();
	}
	
	public String addURLSpecialCharacterEncoding(String url) {
		url = url.replace("%", "%25");
		url = url.replace(" ", "%20");
		url = url.replace("!", "%21");
		url = url.replace("#", "%23");
		url = url.replace("$", "%24");	
		url = url.replace("&", "%26");
		url = url.replace("'", "%27");
		url = url.replace("(", "%28");
		url = url.replace(")", "%29");
		url = url.replace("+", "%2B");
		url = url.replace(",", "%2C");
		url = url.replace(";", "%3B");
		url = url.replace("=", "%3D");
		url = url.replace("@", "%40");
		url = url.replace("[", "%5B");
		url = url.replace("]", "%5D");
		url = url.replace("{", "%7B");
		url = url.replace("}", "%7D");
		url = url.replace("^", "%5E");
		url = url.replace("_", "%5F");		
		url = url.replace("~", "%7E");
		url = url.replace("`", "%80");
		url = url.replace("-", "%2D");		
		
		
		return url;
	}

	public String getMD5Checksum(String filePath) {
		return getMD5Checksum(filePath, false);
	}

	public String getMD5Checksum(String filePath, boolean isURL) {

		String md5Sum = "";

		try {

			MessageDigest fileMd5 = MessageDigest.getInstance("MD5");
			InputStream fileStream;

			if (isURL) {
				fileStream = new URL(filePath).openStream();

			} else {
				fileStream = new FileInputStream(filePath);
			}

			byte[] dataBytes = new byte[1024];

			int nread = 0;
			while ((nread = fileStream.read(dataBytes)) != -1) {
				fileMd5.update(dataBytes, 0, nread);
			}

			byte[] mdbytes = fileMd5.digest();

			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < mdbytes.length; i++) {
				String hex = Integer.toHexString(0xff & mdbytes[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}

			md5Sum = hexString.toString();
			fileStream.close();

		} catch (NoSuchAlgorithmException e) {

			Log.e("DealerTv", "MD5 Sum Check failed:" + e.getMessage());
		} catch (FileNotFoundException e) {

			Log.e("DealerTv", "MD5 Sum Check failed:" + e.getMessage());
		} catch (IOException e) {
			Log.e("DealerTv", "MD5 Sum Check failed:" + e.getMessage());
		}

		return md5Sum;
	}

	public  ArrayList<Slide> loadSlidesListFromConfigXml(File configFile, File dealertvdir) {

		
		ArrayList<Slide> slides = new ArrayList<Slide>();
		
		if (configFile != null && configFile.exists()) {
			try {
				// Get Slide Info from configuration File and Create Slide
				// Objects
				DataHandler handler = new DataHandler();
				slides = null;
				slides = handler.getData(configFile.getAbsolutePath());

				// Sort Slide List by order
				Collections.sort(slides, new SlideOrderComparator());

				for (Slide slide : slides) {
					if (slide.contentType.equals("video")) {
						slide.data = dealertvdir + File.separator + "videos"
								+ File.separator + slide.data;
						

					} else if (slide.contentType.equals("image")) {
						slide.data = dealertvdir + File.separator + "images"
								+ File.separator + slide.data;
						

					} else if (slide.contentType.equals("equipment")) {
						slide.data = dealertvdir + File.separator + "images"
								+ File.separator + slide.data;
						
					}

					if ((slide.headerImage != null)
							&& !slide.headerImage.isEmpty()) {
						slide.headerImage = dealertvdir + File.separator
								+ "images" + File.separator + slide.headerImage;
						
					}

					if ((slide.footerImage != null)
							&& !slide.footerImage.isEmpty()) {
						slide.footerImage = dealertvdir + File.separator
								+ "images" + File.separator + slide.footerImage;
						
					}

					if ((slide.backgroundimage != null)
							&& !slide.backgroundimage.isEmpty()) {
						slide.backgroundimage = dealertvdir + File.separator
								+ "images" + File.separator
								+ slide.backgroundimage;
						
					}

				}
				
				handler = null;
				System.gc();

			} catch (Exception e) {
				Log.e("DealerTv", "Exception " + e.getMessage());

			}
		}
		

		
		return slides;
	}

	public File getDealerTVDirectory(Context context) {

		File file = null;
		
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		
		boolean useInternalStorage = settings.getBoolean(context.getString(R.string.pref_use_internal_storage), false);
		
		String storageRootPath = useInternalStorage ? context.getFilesDir().getAbsolutePath() : settings.getString(context.getString(R.string.pref_external_storage_path), context.getString(R.string.pref_external_storage_path_value));
		
		
		
		try {
			

			File mediaDir = new File(storageRootPath);		
			search: if (mediaDir.exists() && mediaDir.isDirectory()) {
				List<String> mediaFiles = Arrays.asList(mediaDir.list());

				for (String mediaFile : mediaFiles) {
					File possibleDir = new File(mediaDir.getAbsolutePath()
							+ File.separator + mediaFile);
					if (possibleDir.isDirectory()) {
						List<String> possibleDealerTvDirs = Arrays
								.asList(possibleDir.list());
						for (String possibleDealerTvDir : possibleDealerTvDirs) {
							File currentDir = new File(
									mediaDir.getAbsolutePath() + File.separator
											+ mediaFile + File.separator
											+ possibleDealerTvDir);
							if (currentDir.isDirectory()
									&& currentDir.getName()
											.toLowerCase(Locale.getDefault())
											.equals("dealertv")) {
								file = currentDir;

								break search;

							}
						}
					}
				}

			}

			// Create DealerTvDirectory
			if (file == null)
				file = createDealerTvDirectory(storageRootPath, useInternalStorage);

			List<String> dealerTvSubDirectories = Arrays.asList(file.list());

			// Check if images Directory exists
			// if it doesn't, then create it
			if (!dealerTvSubDirectories.contains("images")) {
				File imagesDir = new File(file.getAbsolutePath()
						+ File.separator + "images");

				imagesDir.mkdir();
			}

			// Check if videos Directory exists
			// if it doesn't, then create it
			if (!dealerTvSubDirectories.contains("videos")) {
				File videosDir = new File(file.getAbsolutePath()
						+ File.separator + "videos");

				videosDir.mkdir();

			}

			// Check if weather Directory exists
			// if it doesn't, then create it
			if (!dealerTvSubDirectories.contains("weather")) {
				File weatherDir = new File(file.getAbsolutePath()
						+ File.separator + "weather");

				weatherDir.mkdir();
			}

			// Set Directory to writeable and all subdirectories
			if (file != null) {

				file.setExecutable(true, false);
				file.setReadable(true, false);
				file.setReadable(true, false);

				List<String> subFiles = Arrays.asList(file.list());
				for (String subFile : subFiles) {
					File currentSubFile = new File(subFile);

					currentSubFile.setExecutable(true, false);
					currentSubFile.setReadable(true, false);
					currentSubFile.setReadable(true, false);
				}
			}

		} catch (Exception e) {
			Log.e("DealerTv", "Unable to find or create DealerTv Directory:"
					+ e.getLocalizedMessage());
		}

		return file;
	}

	private static File createDealerTvDirectory(String storageRootPath, boolean useInternalStorage) {
		File dealerTvDirectory = null;
		File mediaDir = new File(storageRootPath);		

		if(useInternalStorage)
		{
			dealerTvDirectory = new File(storageRootPath
					+ "/DealerTv");
			if (!dealerTvDirectory.exists())
				dealerTvDirectory.mkdir();
			File imagesDirectory = new File(
					dealerTvDirectory.getAbsolutePath() + "/images");
			if (!imagesDirectory.exists())
				imagesDirectory.mkdir();
			File videosDirectory = new File(
					dealerTvDirectory.getAbsolutePath() + "/videos");
			if (!videosDirectory.exists())
				videosDirectory.mkdir();

		
		}
		else
		{
			search: if (mediaDir.exists() && mediaDir.isDirectory()) {
				List<String> mediaFiles = Arrays.asList(mediaDir.list());
	
				for (String mediaFile : mediaFiles) {
					File possibleDir = new File(mediaDir.getAbsolutePath()
							+ File.separator + mediaFile);
					if (possibleDir.isDirectory()
							&& possibleDir.getName()
									.toLowerCase(Locale.getDefault())
									.startsWith("usb")) {
	
						dealerTvDirectory = new File(possibleDir.getAbsolutePath()
								+ "/DealerTv");
						if (!dealerTvDirectory.exists())
							dealerTvDirectory.mkdir();
						File imagesDirectory = new File(
								dealerTvDirectory.getAbsolutePath() + "/images");
						if (!imagesDirectory.exists())
							imagesDirectory.mkdir();
						File videosDirectory = new File(
								dealerTvDirectory.getAbsolutePath() + "/videos");
						if (!videosDirectory.exists())
							videosDirectory.mkdir();
	
						break search;
	
					}
				}
	
			}
		}

		return dealerTvDirectory;
	}
	
	public boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    // only got here if we didn't return false
	    return true;
	}
}
