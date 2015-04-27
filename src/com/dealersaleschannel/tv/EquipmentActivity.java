package com.dealersaleschannel.tv;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class EquipmentActivity extends Activity {

	Intent returnIntent;
	long remaining;
	long lastUpdate;
	Timer timer;
	Slide slide;
	private UtilityFunctions utils = new UtilityFunctions();	
	private File dealerTvDir = null;
	private static final int RESULT_SETTINGS = 1;
	private boolean paused = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		try {
			super.onCreate(savedInstanceState);
			
			dealerTvDir = utils.getDealerTVDirectory(this.getBaseContext());

			slide = getIntent().getExtras().getParcelable("Slide");
			

			 if (slide.layout.equals("CustomBackgroundInfoTopNotesRight")) {
				
				if(slide.notes == null || slide.notes.isEmpty())
				{
					createCustomBackgroundInfoTopTableRightSlide(false,false,true);
				}else
				{
					createCustomBackGroundInfoTopNotesRightSlide(false);
				}
				
			}else if (slide.layout.equals("CustomBackgroundInfoBottomNotesRight")) {
				
				if(slide.notes == null || slide.notes.isEmpty())
				{
					createCustomBackgroundInfoBottomTableRightSlide(false,false,true);
				}else
				{
					createCustomBackGroundInfoBottomNotesRightSlide(false);
				}
				
			} else if (slide.layout.equals("CustomBackgroundInfoTopTableRight")) {				
				createCustomBackgroundInfoTopTableRightSlide(false,false,true);
			}else if (slide.layout.equals("CustomBackgroundInfoBottomTableRight")) {
				createCustomBackgroundInfoBottomTableRightSlide(false,false,true);
			} else if (slide.layout.equals("ImageLeftInfoTableRight")) {
				createImageLeftInfoTableRightView();
			} else if (slide.layout.equals("InfoTopImageLeftNotesRight")) {
				createInfoTopImageLeftNotesRightSlide();
			}else if (slide.layout.equals("CustomBackgroundInfoBottomTableRightWithCategory")) {
				createCustomBackgroundInfoBottomTableRightSlide(true,false,true);
			}else if (slide.layout.equals("CustomBackgroundInfoBottomNotesRightWithCategory")) {
				if(slide.notes == null || slide.notes.isEmpty())
				{
					createCustomBackgroundInfoBottomTableRightSlide(true,false,true);
				}else
				{
					createCustomBackGroundInfoBottomNotesRightSlide(true);
				}
			}else if (slide.layout.equals("CustomBackgroundInfoTopTableRightWithCategory")) {
				createCustomBackgroundInfoTopTableRightSlide(true,false,true);
			}else if (slide.layout.equals("CustomBackgroundInfoTopNotesRightWithCategory")) {
				if(slide.notes == null || slide.notes.isEmpty())
				{
					createCustomBackgroundInfoTopTableRightSlide(true,false,true);
				}else
				{
					createCustomBackGroundInfoTopNotesRightSlide(true);
				}
			}else if (slide.layout.equals("CustomBackgroundInfoBottomTableRightWithSalesMan")) {
				createCustomBackgroundInfoBottomTableRightSlide(false,true,true);
			}else if (slide.layout.equals("CustomBackgroundInfoBottomTableRightWithNoLocation"))
			{
				createCustomBackgroundInfoBottomTableRightSlide(false,false,false);
				
			}else if (slide.layout.equals("CustomBackgroundInfoTopTableRightWithNoLocation"))
			{
				createCustomBackgroundInfoTopTableRightSlide(false,false,false);				
			}

		} catch (Exception e) {

			slide = getIntent().getExtras().getParcelable("Slide");
			returnIntent = new Intent();
			returnIntent.putExtra("result", slide.order);
			setResult(RESULT_OK, returnIntent);
			finish();
			stopTimer();
		}
	}

	private void createInfoTopImageLeftNotesRightSlide() {
		setContentView(R.layout.activity_equipment2);

		// Get Layouts
		LinearLayout headerLayout = (LinearLayout) findViewById(R.id.equipmentLinearLayout1);
		headerLayout.setBackgroundColor(Color
				.parseColor(slide.backgroundColor));
		LinearLayout textLayout = (LinearLayout) findViewById(R.id.equipmentLinearLayout2);
		textLayout.setBackgroundColor(Color
				.parseColor(slide.backgroundColor));
		LinearLayout footerLayout = (LinearLayout) findViewById(R.id.equipmentLinearLayout3);
		footerLayout.setBackgroundColor(Color
				.parseColor(slide.backgroundColor));

		LinearLayout topInfoTextLayout = (LinearLayout) findViewById(R.id.equipmentLinearLayout4);
		topInfoTextLayout.setBackgroundColor(Color
				.parseColor(slide.backgroundColor));

		setHeaderImageViewImageAndLayoutWeight(headerLayout);

		setfooterImageViewImageAndLayoutWeight(footerLayout);

		int displaySeconds = (slide.displayTime == null || slide.displayTime == "") ? 1400
				: (Integer.parseInt(slide.displayTime) * 1000);

		// Required to pause timer when the menu is opened
		remaining = displaySeconds;
		lastUpdate = System.currentTimeMillis();

		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				setResult(RESULT_OK, returnIntent);
				finish();
				stopTimer();
			}
		}, displaySeconds);

		TextView topInfoText = (TextView) findViewById(R.id.equipmentTextView2);

		StringBuilder topInfoStringBuilder = new StringBuilder();

		if (slide.year != null && !slide.year.equals("")) {
			topInfoStringBuilder.append(slide.year + " - ");
		}

		if (slide.make != null && !slide.make.equals("")) {
			topInfoStringBuilder.append(slide.make + " - ");
		}

		if (slide.model != null && !slide.model.equals("")) {
			topInfoStringBuilder.append(slide.model + " - ");
		}

		if (slide.price != null && !slide.price.equals("") && !slide.price.equals("0.00")) {
			
			String price = NumberFormat.getCurrencyInstance(Locale.US)
			.format(Double.parseDouble(slide.price));

	
				if(price.contains("."))
				{
					price = price.substring(0,price.indexOf("."));
				}

				if(price != null && !price.equals(""))
				{
					topInfoStringBuilder.append(price);
				}
			
			
			
		}else if(slide.price.equals("0.00"))
		{
			topInfoStringBuilder.append("Call for Price");
		}
		
		

		String topInfoString = topInfoStringBuilder.toString().trim();
		if (topInfoString.endsWith("-"))
			topInfoString = topInfoString.substring(0,
					topInfoString.length() - 2);

		topInfoText.setText(topInfoString);
		topInfoText.setBackgroundColor(Color
				.parseColor(slide.backgroundColor));
		topInfoText.setTextColor(Color.parseColor(slide.textColor));
		topInfoText.setTextSize(Float.parseFloat(slide.textSize));
		topInfoText.setPadding(20, -10, 0, 0);

		AutoResizeTextView notesText = (AutoResizeTextView) findViewById(R.id.equipmentTextView);
		String notes = "";

		if (slide.notes != null && !slide.notes.equals("")) {
			notes = slide.notes;
		}

		notesText.setBackgroundColor(Color
				.parseColor(slide.backgroundColor));
		notesText.setTextColor(Color.parseColor(slide.textColor));
		notesText.setTextSize(Float.parseFloat(slide.textSize));
		notesText.setText(notes);

		ImageView imageView = (ImageView) findViewById(R.id.equipmentImageView);

		imageView.setImageURI(Uri.fromFile(new File(slide.data)));

		// Set Text Layout
		float textLayoutWeight = Math.abs(0.9f - (headerLayout
				.getWeightSum() + footerLayout.getWeightSum()));

		LayoutParams textLayoutParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, 0, textLayoutWeight);

		textLayout.setLayoutParams(textLayoutParams);
		textLayout.setWeightSum(textLayoutWeight);

		float topTextLayoutWeight = Math
				.abs(1 - (headerLayout.getWeightSum()
						+ footerLayout.getWeightSum() + textLayout
						.getWeightSum()));

		LayoutParams topInfoTextLayoutParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, 0, topTextLayoutWeight);

		topInfoTextLayout.setLayoutParams(topInfoTextLayoutParams);
		topInfoTextLayout.setWeightSum(topTextLayoutWeight);

		returnIntent = new Intent();
		returnIntent.putExtra("result", slide.order);
	}

	private void createImageLeftInfoTableRightView() {
		setContentView(R.layout.activity_equipment);

		// Get Layouts
		LinearLayout headerLayout = (LinearLayout) findViewById(R.id.equipmentLinearLayout1);
		headerLayout.setBackgroundColor(Color
				.parseColor(slide.backgroundColor));
		LinearLayout textLayout = (LinearLayout) findViewById(R.id.equipmentLinearLayout2);
		textLayout.setBackgroundColor(Color
				.parseColor(slide.backgroundColor));
		LinearLayout footerLayout = (LinearLayout) findViewById(R.id.equipmentLinearLayout3);
		footerLayout.setBackgroundColor(Color
				.parseColor(slide.backgroundColor));

		setHeaderImageViewImageAndLayoutWeight(headerLayout);

		setfooterImageViewImageAndLayoutWeight(footerLayout);

		int displaySeconds = (slide.displayTime == null || slide.displayTime == "") ? 1400
				: (Integer.parseInt(slide.displayTime) * 1000);

		// Required to pause timer when the menu is opened
		remaining = displaySeconds;
		lastUpdate = System.currentTimeMillis();

		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				setResult(RESULT_OK, returnIntent);
				finish();
				stopTimer();
			}
		}, displaySeconds);

		AutoResizeTextView tableTextView = (AutoResizeTextView) findViewById(R.id.equipmentTextView);
		String tableText = "";


		StringBuilder tableTextViewString = new StringBuilder();

		if (slide.make != null && !slide.make.equals("")) {
			tableTextViewString.append("Make: " + slide.make + "\r\n");
		}

		if (slide.model != null && !slide.model.equals("")) {
			tableTextViewString
					.append("Model: " + slide.model + "\r\n");
		}

		if (slide.stock != null && !slide.stock.equals("")) {
			tableTextViewString
					.append("Stock: " + slide.stock + "\r\n");
		}

		if (slide.location != null && !slide.location.equals("")) {
			tableTextViewString.append(slide.location
					+ "\r\n");
		}

		if (slide.price != null && !slide.price.equals("") && !slide.price.equals("0.00")) {
			
			String price = NumberFormat.getCurrencyInstance(Locale.US)
			.format(Double.parseDouble(slide.price));

			if(price.contains("."))
			{
				price = price.substring(0,price.indexOf("."));
			}

			
			if(price != null && !price.equals(""))
			{
				tableTextViewString.append("Price: " + price + "\r\n");
			}
			
					
		}else if(slide.price.equals("0.00"))
		{
			tableTextViewString.append("Price: Call\r\n");
		}

		if (slide.hours != null && !slide.hours.equals("")) {
			tableTextViewString
					.append("Hours: " + slide.hours + "\r\n");
		}

		tableText = tableTextViewString.toString().trim();

		tableTextView.setBackgroundColor(Color
				.parseColor(slide.backgroundColor));
		tableTextView.setTextColor(Color.parseColor(slide.textColor));
		tableTextView.setTextSize(Float.parseFloat(slide.textSize));
		tableTextView.setLineSpacing(1f, 1f);
		tableTextView.setText(tableText);

		ImageView imageView = (ImageView) findViewById(R.id.equipmentImageView);

		imageView.setImageURI(Uri.fromFile(new File(slide.data)));

		// Set Text Layout
		float textLayoutWeight = Math.abs(1 - (headerLayout
				.getWeightSum() + footerLayout.getWeightSum()));

		LayoutParams textLayoutoParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, 0, textLayoutWeight);

		textLayout.setLayoutParams(textLayoutoParams);
		textLayout.setWeightSum(textLayoutWeight);

		returnIntent = new Intent();
		returnIntent.putExtra("result", slide.order);
	}

	private void createCustomBackgroundInfoBottomTableRightSlide(boolean isWithCategory, boolean isWithSalesContact, boolean isWithLocation) {
		setContentView(R.layout.activity_equipmentcustombackground);

		RelativeLayout rl = (RelativeLayout) findViewById(R.id.activity_equipmentcustombackground);

		ImageView backgrounImageView = new ImageView(this);

		backgrounImageView.setImageURI(Uri.fromFile(new File(
				slide.backgroundimage)));

		RelativeLayout.LayoutParams params5 = new RelativeLayout.LayoutParams(
				1920, 1080);
		params5.leftMargin = 0;
		params5.topMargin = 0;

		rl.addView(backgrounImageView, params5);

		ImageView imageView = new ImageView(this);

		imageView.setImageURI(Uri.fromFile(new File(slide.data)));

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		// Returns null, sizes are in the options variable
		BitmapFactory.decodeFile(slide.data, options);

		double pictureIncrement = 1.8;
		int pictureHeight = (int) Math.round(options.outHeight
				* pictureIncrement);
		int pictureWidth = (int) Math.round(options.outWidth
				* pictureIncrement);
		int maxHeight = 783;
		int maxWidth = 1043;
		int minHeight = 700;

		while (pictureHeight < minHeight) {
			pictureIncrement = pictureIncrement + 0.1;
			pictureHeight = (int) Math.round(options.outHeight
					* pictureIncrement);
			pictureWidth = (int) Math.round(options.outWidth
					* pictureIncrement);
		}

		while (pictureHeight > maxHeight) {
			pictureIncrement = pictureIncrement - 0.1;
			pictureHeight = (int) Math.round(options.outHeight
					* pictureIncrement);
			pictureWidth = (int) Math.round(options.outWidth
					* pictureIncrement);
		}

		while (pictureWidth > maxWidth) {
			pictureIncrement = pictureIncrement - 0.1;
			pictureHeight = (int) Math.round(options.outHeight
					* pictureIncrement);
			pictureWidth = (int) Math.round(options.outWidth
					* pictureIncrement);
		}

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				pictureWidth, pictureHeight);
		params.leftMargin = 25;
		params.topMargin = 200;

		rl.addView(imageView, params);

		AutoResizeTextView equipmentInfoTable = new AutoResizeTextView(
				this);

		String tableText = "";

		StringBuilder tableTextViewString = new StringBuilder();

		if (slide.make != null && !slide.make.equals("")) {
			tableTextViewString.append("Make: " + slide.make + "\r\n");
		}

		if (slide.model != null && !slide.model.equals("")) {
			tableTextViewString
					.append("Model: " + slide.model + "\r\n");
		}

		if (slide.stock != null && !slide.stock.equals("")) {
			tableTextViewString
					.append("Stock: " + slide.stock + "\r\n");
		}

		
		if (isWithLocation && slide.location != null && !slide.location.equals("")) {
			tableTextViewString.append(slide.location
					+ "\r\n");
		}

		if (slide.price != null && !slide.price.equals("") && !slide.price.equals("0.00")) {
			
			String price = NumberFormat.getCurrencyInstance(Locale.US)
			.format(Double.parseDouble(slide.price));
			
			if(price.contains("."))
			{
				price = price.substring(0,price.indexOf("."));
			}

			if(price != null && !price.equals(""))
			{
				tableTextViewString.append("Price: " + price + "\r\n");
			}
			
			
		}else if(slide.price.equals("0.00"))
		{
			tableTextViewString.append("Price: Call\r\n");
		}

		if (slide.hours != null && !slide.hours.equals("")) {
			tableTextViewString
					.append("Hours: " + slide.hours + "\r\n");
		}
		
		if(isWithCategory)
		{
			if (slide.category != null && !slide.category.equals("")) {
				tableTextViewString
						.append(slide.category + "\r\n");
			}
		}
		
		if(isWithSalesContact)
		{
			if (slide.salescontact != null && !slide.salescontact.equals("")) {
				tableTextViewString
						.append("Ask for " + slide.salescontact + "\r\n");
			}else
			{
				tableTextViewString
				.append("Ask for Sales Manager"+ "\r\n");
			}
		}

		tableText = tableTextViewString.toString().trim();

		SpannableString equipmentOptionsSpannableString = new SpannableString(
				tableText);// equipmentOptionsStringBuilder.toString());
		equipmentOptionsSpannableString.setSpan(new StyleSpan(
				Typeface.BOLD), 0, equipmentOptionsSpannableString
				.length(), 0);
		equipmentInfoTable.setBackgroundColor(Color.TRANSPARENT);
		equipmentInfoTable.setTextColor(Color
				.parseColor(slide.textColor));
		equipmentInfoTable
				.setTextSize(Float.parseFloat(slide.textSize));
		equipmentInfoTable.setText(equipmentOptionsSpannableString);

		if (Boolean.parseBoolean(slide.istextdropshadow)) {
			int dy = (int) Math.round((0.05 * (equipmentInfoTable
					.getCalculatedTextSize())));
			int dx = (int) Math.round((0.05 * (equipmentInfoTable
					.getCalculatedTextSize())));
			float radius = (float) 0.05
					* (equipmentInfoTable.getCalculatedTextSize());

			equipmentInfoTable.setShadowLayer(radius, dx, dy,
					Color.parseColor(slide.textshadowcolor));
		}

		RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(
				1920-pictureWidth, pictureHeight);
		params3.leftMargin = (pictureWidth + 25) + 10;
		params3.rightMargin = 25;
		params3.topMargin = 190;

		rl.addView(equipmentInfoTable, params3);

		int displaySeconds = (slide.displayTime == null || slide.displayTime == "") ? 1400
				: (Integer.parseInt(slide.displayTime) * 1000);

		// Required to pause timer when the menu is opened
		remaining = displaySeconds;
		lastUpdate = System.currentTimeMillis();

		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				setResult(RESULT_OK, returnIntent);
				finish();
				stopTimer();
			}
		}, displaySeconds);

		returnIntent = new Intent();
		returnIntent.putExtra("result", slide.order);
	}
	
	private void createCustomBackgroundInfoTopTableRightSlide(boolean isWithCategory,boolean isWithSalesContact, boolean isWithLocation) {
		setContentView(R.layout.activity_equipmentcustombackground);

		RelativeLayout rl = (RelativeLayout) findViewById(R.id.activity_equipmentcustombackground);

		ImageView backgrounImageView = new ImageView(this);

		backgrounImageView.setImageURI(Uri.fromFile(new File(
				slide.backgroundimage)));

		RelativeLayout.LayoutParams params5 = new RelativeLayout.LayoutParams(
				1920, 1080);
		params5.leftMargin = 0;
		params5.topMargin = 0;

		rl.addView(backgrounImageView, params5);

		ImageView imageView = new ImageView(this);

		imageView.setImageURI(Uri.fromFile(new File(slide.data)));

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		// Returns null, sizes are in the options variable
		BitmapFactory.decodeFile(slide.data, options);

		double pictureIncrement = 1.8;
		int pictureHeight = (int) Math.round(options.outHeight
				* pictureIncrement);
		int pictureWidth = (int) Math.round(options.outWidth
				* pictureIncrement);
		int maxHeight = 783;
		int maxWidth = 1043;
		int minHeight = 700;

		while (pictureHeight < minHeight) {
			pictureIncrement = pictureIncrement + 0.1;
			pictureHeight = (int) Math.round(options.outHeight
					* pictureIncrement);
			pictureWidth = (int) Math.round(options.outWidth
					* pictureIncrement);
		}

		while (pictureHeight > maxHeight) {
			pictureIncrement = pictureIncrement - 0.1;
			pictureHeight = (int) Math.round(options.outHeight
					* pictureIncrement);
			pictureWidth = (int) Math.round(options.outWidth
					* pictureIncrement);
		}

		while (pictureWidth > maxWidth) {
			pictureIncrement = pictureIncrement - 0.1;
			pictureHeight = (int) Math.round(options.outHeight
					* pictureIncrement);
			pictureWidth = (int) Math.round(options.outWidth
					* pictureIncrement);
		}

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				pictureWidth, pictureHeight);
		params.leftMargin = 25;
		params.topMargin = (1080-(200+pictureHeight));

		rl.addView(imageView, params);

		AutoResizeTextView equipmentInfoTable = new AutoResizeTextView(
				this);

		String tableText = "";

		

		StringBuilder tableTextViewString = new StringBuilder();

		if (slide.make != null && !slide.make.equals("")) {
			tableTextViewString.append("Make: " + slide.make + "\r\n");
		}

		if (slide.model != null && !slide.model.equals("")) {
			tableTextViewString
					.append("Model: " + slide.model + "\r\n");
		}

		if (slide.stock != null && !slide.stock.equals("")) {
			tableTextViewString
					.append("Stock: " + slide.stock + "\r\n");
		}

		if (isWithLocation && slide.location != null && !slide.location.equals("")) {
			tableTextViewString.append(slide.location
					+ "\r\n");
		}

		if (slide.price != null && !slide.price.equals("") && !slide.price.equals("0.00")) {
			
			String price = NumberFormat.getCurrencyInstance(Locale.US)
			.format(Double.parseDouble(slide.price));


			if(price.contains("."))
			{
				price = price.substring(0,price.indexOf("."));
			}

			if(price != null && !price.equals(""))
			{
				tableTextViewString.append("Price: " + price + "\r\n");
			}
			
			
		}else if(slide.price.equals("0.00"))
		{
			tableTextViewString.append("Price: Call\r\n");
		}

		if (slide.hours != null && !slide.hours.equals("")) {
			tableTextViewString
					.append("Hours: " + slide.hours + "\r\n");
		}
		
		if(isWithCategory)
		{
			if (slide.category != null && !slide.category.equals("")) {
				tableTextViewString
						.append(slide.category + "\r\n");
			}
		}
		
		if(isWithSalesContact)
		{
			if (slide.salescontact != null && !slide.salescontact.equals("")) {
				tableTextViewString
						.append("Ask for " + slide.salescontact + "\r\n");
			}else
			{
				tableTextViewString
				.append("Ask for Sales Manager"+ "\r\n");
			}
		}

		tableText = tableTextViewString.toString().trim();

		SpannableString equipmentOptionsSpannableString = new SpannableString(
				tableText);// equipmentOptionsStringBuilder.toString());
		equipmentOptionsSpannableString.setSpan(new StyleSpan(
				Typeface.BOLD), 0, equipmentOptionsSpannableString
				.length(), 0);
		equipmentInfoTable.setBackgroundColor(Color.TRANSPARENT);
		equipmentInfoTable.setTextColor(Color
				.parseColor(slide.textColor));
		equipmentInfoTable
				.setTextSize(Float.parseFloat(slide.textSize));
		equipmentInfoTable.setText(equipmentOptionsSpannableString);

		if (Boolean.parseBoolean(slide.istextdropshadow)) {
			int dy = (int) Math.round((0.05 * (equipmentInfoTable
					.getCalculatedTextSize())));
			int dx = (int) Math.round((0.05 * (equipmentInfoTable
					.getCalculatedTextSize())));
			float radius = (float) 0.05
					* (equipmentInfoTable.getCalculatedTextSize());

			equipmentInfoTable.setShadowLayer(radius, dx, dy,
					Color.parseColor(slide.textshadowcolor));
		}

		RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(
				1920-pictureWidth, pictureHeight);
		params3.leftMargin = (pictureWidth + 25) + 10;
		params3.rightMargin = 25;
		params3.topMargin = (1080-(200+pictureHeight));

		rl.addView(equipmentInfoTable, params3);

		int displaySeconds = (slide.displayTime == null || slide.displayTime == "") ? 1400
				: (Integer.parseInt(slide.displayTime) * 1000);

		// Required to pause timer when the menu is opened
		remaining = displaySeconds;
		lastUpdate = System.currentTimeMillis();

		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				setResult(RESULT_OK, returnIntent);
				finish();
				stopTimer();
			}
		}, displaySeconds);

		returnIntent = new Intent();
		returnIntent.putExtra("result", slide.order);
	}

	private void createCustomBackGroundInfoBottomNotesRightSlide(boolean isWithCategory) {
		setContentView(R.layout.activity_equipmentcustombackground);

		RelativeLayout rl = (RelativeLayout) findViewById(R.id.activity_equipmentcustombackground);

		ImageView backgrounImageView = new ImageView(this);

		backgrounImageView.setImageURI(Uri.fromFile(new File(
				slide.backgroundimage)));

		RelativeLayout.LayoutParams params5 = new RelativeLayout.LayoutParams(
				1920, 1080);
		params5.leftMargin = 0;
		params5.topMargin = 0;

		rl.addView(backgrounImageView, params5);

		ImageView imageView = new ImageView(this);

		imageView.setImageURI(Uri.fromFile(new File(slide.data)));

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		// Returns null, sizes are in the options variable
		BitmapFactory.decodeFile(slide.data, options);

		double pictureIncrement = 1.8;
		int pictureHeight = (int) Math.round(options.outHeight
				* pictureIncrement);
		int pictureWidth = (int) Math.round(options.outWidth
				* pictureIncrement);
		int maxHeight = 783;
		int maxWidth = 1043;
		int minHeight = 700;

		while (pictureHeight < minHeight) {
			pictureIncrement = pictureIncrement + 0.1;
			pictureHeight = (int) Math.round(options.outHeight
					* pictureIncrement);
			pictureWidth = (int) Math.round(options.outWidth
					* pictureIncrement);
		}

		while (pictureHeight > maxHeight) {
			pictureIncrement = pictureIncrement - 0.1;
			pictureHeight = (int) Math.round(options.outHeight
					* pictureIncrement);
			pictureWidth = (int) Math.round(options.outWidth
					* pictureIncrement);
		}

		while (pictureWidth > maxWidth) {
			pictureIncrement = pictureIncrement - 0.1;
			pictureHeight = (int) Math.round(options.outHeight
					* pictureIncrement);
			pictureWidth = (int) Math.round(options.outWidth
					* pictureIncrement);
		}

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				pictureWidth, pictureHeight);
		params.leftMargin = 25;
		params.topMargin = 200;

		rl.addView(imageView, params);

		AutoResizeTextView equipmentInfo = new AutoResizeTextView(this);


		StringBuilder equipmentInfoStringBuilder = new StringBuilder();

		if (slide.year != null && !slide.year.equals("")) {
			equipmentInfoStringBuilder.append(slide.year + " ");
		}

		if (slide.make != null && !slide.make.equals("")) {
			equipmentInfoStringBuilder.append(slide.make + " ");
		}

		if (slide.model != null && !slide.model.equals("")) {
			equipmentInfoStringBuilder.append(slide.model + " ");
		}

		if (slide.price != null && !slide.price.equals("") && !slide.price.equals("0.00")) {
			
			String price = NumberFormat.getCurrencyInstance(Locale.US)
			.format(Double.parseDouble(slide.price));



			if(price.contains("."))
			{
				price = price.substring(0,price.indexOf("."));
			}

			
			if(price != null && !price.equals(""))
			{
				equipmentInfoStringBuilder.append(price);
			}
			
			
			
		}else if(slide.price.equals("0.00"))
		{
			equipmentInfoStringBuilder.append("Call for Price");
		}
		
		if(isWithCategory)
		{
			if (slide.category != null && !slide.category.equals("")) {
				equipmentInfoStringBuilder
						.append(" - "+slide.category + "\r\n");
			}
		}
		
		


		String equipmentInfoString = equipmentInfoStringBuilder
				.toString().trim();
		if (equipmentInfoString.endsWith("-"))
			equipmentInfoString = equipmentInfoString.substring(0,
					equipmentInfoString.length() - 2);

		SpannableString equipmentSpannableString = new SpannableString(
				equipmentInfoString);

		if (Boolean.parseBoolean(slide.ishighlightequipmentinfo)) {
			equipmentSpannableString
					.setSpan(
							new BackgroundColorSpan(
									Color.parseColor(slide.equipmentinfohighlightcolor)),
							0, equipmentSpannableString.length(), 0);
		}

		equipmentSpannableString.setSpan(new StyleSpan(Typeface.BOLD),
				0, equipmentSpannableString.length(), 0);
		equipmentInfo.setBackgroundColor(Color.TRANSPARENT);
		equipmentInfo.setTextColor(Color.parseColor(slide.textColor));
		equipmentInfo.setTextSize(Float.parseFloat(slide.textSize));
		equipmentInfo.setText(equipmentSpannableString);

		if (Boolean.parseBoolean(slide.istextdropshadow)) {
			int dy = (int) Math.round((0.05 * (equipmentInfo
					.getCalculatedTextSize())));
			int dx = (int) Math.round((0.05 * (equipmentInfo
					.getCalculatedTextSize())));
			float radius = (float) 0.05
					* (equipmentInfo.getCalculatedTextSize());

			equipmentInfo.setShadowLayer(radius, dx, dy,
					Color.parseColor(slide.textshadowcolor));
		}
		equipmentInfo.setPadding(25, -15, 0, 0);

		RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
				1920, (1080 - (200 + pictureHeight)));
		params2.leftMargin = 0;
		params2.topMargin = 200 + pictureHeight;

		rl.addView(equipmentInfo, params2);

		AutoResizeTextView equipmentNotesTextView = new AutoResizeTextView(
				this);
		// equipmentOptions.setNoWrap(true);
		// String equipmentOptionsString =
		// "-Suspended Boom Type,-60 ft. Boom Length,-Foam Markers,-Poly Tank Type,-Chemical Tank,-1000 Gal Main Tank Capacity,-Hydraulic Pump Drive,-320/85R38 Tire Size";
		// StringBuilder equipmentOptionsStringBuilder = new
		// StringBuilder();
		// String[] equipmentOptionsArray =
		// equipmentOptionsString.split(",");
		//
		// for (int i = 0; i < equipmentOptionsArray.length; i++)
		// {
		// if( i > 0)
		// {
		// equipmentOptionsStringBuilder.append("\r\n");
		// }
		// equipmentOptionsStringBuilder.append(equipmentOptionsArray[i]);
		// }
		//

		String equipmentNotes = "";
		
		if(slide.stock != null && !slide.stock.isEmpty())
		{
			equipmentNotes = equipmentNotes + String.format(" Stock: %s\n", slide.stock);
		}
						
		if(slide.hours != null && !slide.hours.isEmpty())
		{
			equipmentNotes = String.format("Hours: %s\n", slide.hours);
		}
		
		if(slide.notes != null && !slide.notes.isEmpty())
		{
			equipmentNotes = equipmentNotes + slide.notes;
		}
		 
		SpannableString equipmentOptionsSpannableString = new SpannableString(
				equipmentNotes);// equipmentOptionsStringBuilder.toString());
		equipmentOptionsSpannableString.setSpan(new StyleSpan(
				Typeface.BOLD), 0, equipmentOptionsSpannableString
				.length(), 0);
		equipmentNotesTextView.setBackgroundColor(Color.TRANSPARENT);
		equipmentNotesTextView.setTextColor(Color
				.parseColor(slide.textColor));
		equipmentNotesTextView.setTextSize(Float
				.parseFloat(slide.textSize));
		equipmentNotesTextView.setText(equipmentOptionsSpannableString);

		if (Boolean.parseBoolean(slide.istextdropshadow)) {
			int dy = (int) Math.round((0.05 * (equipmentNotesTextView
					.getCalculatedTextSize())));
			int dx = (int) Math.round((0.05 * (equipmentNotesTextView
					.getCalculatedTextSize())));
			float radius = (float) 0.05
					* (equipmentNotesTextView.getCalculatedTextSize());

			equipmentNotesTextView.setShadowLayer(radius, dx, dy,
					Color.parseColor(slide.textshadowcolor));
		}

		RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(
				1920 - pictureWidth, pictureHeight);
		params3.leftMargin = (pictureWidth + 25) + 10;
		params3.rightMargin = 25;
		params3.topMargin = 190;

		rl.addView(equipmentNotesTextView, params3);

		int displaySeconds = (slide.displayTime == null || slide.displayTime == "") ? 1400
				: (Integer.parseInt(slide.displayTime) * 1000);

		// Required to pause timer when the menu is opened
		remaining = displaySeconds;
		lastUpdate = System.currentTimeMillis();

		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				setResult(RESULT_OK, returnIntent);
				finish();
				stopTimer();
			}
		}, displaySeconds);

		returnIntent = new Intent();
		returnIntent.putExtra("result", slide.order);
	}
	
	private void createCustomBackGroundInfoTopNotesRightSlide(boolean isWithCategory) {
		setContentView(R.layout.activity_equipmentcustombackground);

		RelativeLayout rl = (RelativeLayout) findViewById(R.id.activity_equipmentcustombackground);

		ImageView backgrounImageView = new ImageView(this);

		backgrounImageView.setImageURI(Uri.fromFile(new File(
				slide.backgroundimage)));

		RelativeLayout.LayoutParams params5 = new RelativeLayout.LayoutParams(
				1920, 1080);
		params5.leftMargin = 0;
		params5.topMargin = 0;

		rl.addView(backgrounImageView, params5);

		ImageView imageView = new ImageView(this);

		imageView.setImageURI(Uri.fromFile(new File(slide.data)));

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		// Returns null, sizes are in the options variable
		BitmapFactory.decodeFile(slide.data, options);

		double pictureIncrement = 1.8;
		int pictureHeight = (int) Math.round(options.outHeight
				* pictureIncrement);
		int pictureWidth = (int) Math.round(options.outWidth
				* pictureIncrement);
		int maxHeight = 783;
		int maxWidth = 1043;
		int minHeight = 700;

		while (pictureHeight < minHeight) {
			pictureIncrement = pictureIncrement + 0.1;
			pictureHeight = (int) Math.round(options.outHeight
					* pictureIncrement);
			pictureWidth = (int) Math.round(options.outWidth
					* pictureIncrement);
		}

		while (pictureHeight > maxHeight) {
			pictureIncrement = pictureIncrement - 0.1;
			pictureHeight = (int) Math.round(options.outHeight
					* pictureIncrement);
			pictureWidth = (int) Math.round(options.outWidth
					* pictureIncrement);
		}

		while (pictureWidth > maxWidth) {
			pictureIncrement = pictureIncrement - 0.1;
			pictureHeight = (int) Math.round(options.outHeight
					* pictureIncrement);
			pictureWidth = (int) Math.round(options.outWidth
					* pictureIncrement);
		}

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				pictureWidth, pictureHeight);
		params.leftMargin = 25;
		params.topMargin = (1080-(200+pictureHeight));

		rl.addView(imageView, params);

		AutoResizeTextView equipmentInfo = new AutoResizeTextView(this);

		StringBuilder equipmentInfoStringBuilder = new StringBuilder();

		if (slide.year != null && !slide.year.equals("")) {
			equipmentInfoStringBuilder.append(slide.year + " ");
		}

		if (slide.make != null && !slide.make.equals("")) {
			equipmentInfoStringBuilder.append(slide.make + " ");
		}

		if (slide.model != null && !slide.model.equals("")) {
			equipmentInfoStringBuilder.append(slide.model + " ");
		}

		if (slide.price != null && !slide.price.equals("") && !slide.price.equals("0.00")) {
			
			String price = NumberFormat.getCurrencyInstance(Locale.US)
			.format(Double.parseDouble(slide.price));


			if(price.contains("."))
			{
				price = price.substring(0,price.indexOf("."));
			}

			if(price != null && !price.equals(""))
			{
				equipmentInfoStringBuilder.append(price);
			}
			
			
			
		}else if(slide.price.equals("0.00"))
		{
			equipmentInfoStringBuilder.append("Call for Price");
		}

		if(isWithCategory)
		{
			if (slide.category != null && !slide.category.equals("")) {
				equipmentInfoStringBuilder
						.append(" - "+slide.category + "\r\n");
			}		
		}
		
		String equipmentInfoString = equipmentInfoStringBuilder
				.toString().trim();
		if (equipmentInfoString.endsWith("-"))
			equipmentInfoString = equipmentInfoString.substring(0, equipmentInfoString.length() - 2);

		SpannableString equipmentSpannableString = new SpannableString(
				equipmentInfoString);

		if (Boolean.parseBoolean(slide.ishighlightequipmentinfo)) {
			equipmentSpannableString
					.setSpan(
							new BackgroundColorSpan(
									Color.parseColor(slide.equipmentinfohighlightcolor)),
							0, equipmentSpannableString.length(), 0);
		}

		equipmentSpannableString.setSpan(new StyleSpan(Typeface.BOLD),
				0, equipmentSpannableString.length(), 0);
		equipmentInfo.setBackgroundColor(Color.TRANSPARENT);
		equipmentInfo.setTextColor(Color.parseColor(slide.textColor));
		equipmentInfo.setTextSize(Float.parseFloat(slide.textSize));
		equipmentInfo.setText(equipmentSpannableString);

		if (Boolean.parseBoolean(slide.istextdropshadow)) {
			int dy = (int) Math.round((0.05 * (equipmentInfo
					.getCalculatedTextSize())));
			int dx = (int) Math.round((0.05 * (equipmentInfo
					.getCalculatedTextSize())));
			float radius = (float) 0.05
					* (equipmentInfo.getCalculatedTextSize());

			equipmentInfo.setShadowLayer(radius, dx, dy,
					Color.parseColor(slide.textshadowcolor));
		}
		equipmentInfo.setPadding(25, -15, 0, 0);

		RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
				1920, (1080 - (200 + pictureHeight)));
		params2.leftMargin = 0;
		params2.topMargin = 10;

		rl.addView(equipmentInfo, params2);

		AutoResizeTextView equipmentNotesTextView = new AutoResizeTextView(
				this);
		// equipmentOptions.setNoWrap(true);
		// String equipmentOptionsString =
		// "-Suspended Boom Type,-60 ft. Boom Length,-Foam Markers,-Poly Tank Type,-Chemical Tank,-1000 Gal Main Tank Capacity,-Hydraulic Pump Drive,-320/85R38 Tire Size";
		// StringBuilder equipmentOptionsStringBuilder = new
		// StringBuilder();
		// String[] equipmentOptionsArray =
		// equipmentOptionsString.split(",");
		//
		// for (int i = 0; i < equipmentOptionsArray.length; i++)
		// {
		// if( i > 0)
		// {
		// equipmentOptionsStringBuilder.append("\r\n");
		// }
		// equipmentOptionsStringBuilder.append(equipmentOptionsArray[i]);
		// }
		//

		String equipmentNotes = "";
		
		if(slide.stock != null && !slide.stock.isEmpty())
		{
			equipmentNotes = equipmentNotes + String.format(" Stock: %s\n", slide.stock);
		}
						
		if(slide.hours != null && !slide.hours.isEmpty())
		{
			equipmentNotes = String.format("Hours: %s\n", slide.hours);
		}
		
		if(slide.notes != null && !slide.notes.isEmpty())
		{
			equipmentNotes = equipmentNotes + slide.notes;
		}
		 
		SpannableString equipmentOptionsSpannableString = new SpannableString(
				equipmentNotes);// equipmentOptionsStringBuilder.toString());
		equipmentOptionsSpannableString.setSpan(new StyleSpan(
				Typeface.BOLD), 0, equipmentOptionsSpannableString
				.length(), 0);
		equipmentNotesTextView.setBackgroundColor(Color.TRANSPARENT);
		equipmentNotesTextView.setTextColor(Color
				.parseColor(slide.textColor));
		equipmentNotesTextView.setTextSize(Float
				.parseFloat(slide.textSize));
		equipmentNotesTextView.setText(equipmentOptionsSpannableString);

		if (Boolean.parseBoolean(slide.istextdropshadow)) {
			int dy = (int) Math.round((0.05 * (equipmentNotesTextView
					.getCalculatedTextSize())));
			int dx = (int) Math.round((0.05 * (equipmentNotesTextView
					.getCalculatedTextSize())));
			float radius = (float) 0.05
					* (equipmentNotesTextView.getCalculatedTextSize());

			equipmentNotesTextView.setShadowLayer(radius, dx, dy,
					Color.parseColor(slide.textshadowcolor));
		}

		RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(
				1920-pictureWidth, pictureHeight);
		params3.leftMargin = (pictureWidth + 25) + 10;
		params3.rightMargin = 25;
		params3.topMargin = (1080-(200+pictureHeight));

		rl.addView(equipmentNotesTextView, params3);

		int displaySeconds = (slide.displayTime == null || slide.displayTime == "") ? 1400
				: (Integer.parseInt(slide.displayTime) * 1000);

		// Required to pause timer when the menu is opened
		remaining = displaySeconds;
		lastUpdate = System.currentTimeMillis();

		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				setResult(RESULT_OK, returnIntent);
				finish();
				stopTimer();
			}
		}, displaySeconds);

		returnIntent = new Intent();
		returnIntent.putExtra("result", slide.order);
	}

	private void setfooterImageViewImageAndLayoutWeight(
			LinearLayout footerLayout) {
		if (slide.footerImage != null && !slide.footerImage.isEmpty()) {
			ImageView imageView = (ImageView) findViewById(R.id.equipmentFooterImageView);
			imageView.setImageURI(Uri.fromFile(new File(slide.footerImage)));

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;

			// Returns null, sizes are in the options variable
			BitmapFactory.decodeFile(slide.footerImage, options);

			LayoutParams footerLayoutParams = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, 0, (float) options.outHeight
							/ (float) 1080);

			footerLayout.setLayoutParams(footerLayoutParams);
			footerLayout.setWeightSum((float) options.outHeight / (float) 1080);

		} else {
			LayoutParams footerLayoutParams = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, 0, 0f);

			footerLayout.setLayoutParams(footerLayoutParams);
			footerLayout.setWeightSum(0f);
		}
	}

	private void setHeaderImageViewImageAndLayoutWeight(
			LinearLayout headerLayout) {
		if (slide.headerImage != null && !slide.headerImage.isEmpty()) {
			ImageView imageView = (ImageView) findViewById(R.id.equipmentHeaderImageView);
			imageView.setImageURI(Uri.fromFile(new File(slide.headerImage)));

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;

			// Returns null, sizes are in the options variable
			BitmapFactory.decodeFile(slide.headerImage, options);

			LayoutParams headerLayoutParams = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, 0, (float) options.outHeight
							/ (float) 1080);

			headerLayout.setLayoutParams(headerLayoutParams);
			headerLayout.setWeightSum((float) options.outHeight / (float) 1080);

		} else

		{

			LayoutParams headerLayoutParams = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, 0, 0f);

			headerLayout.setLayoutParams(headerLayoutParams);
			headerLayout.setWeightSum(0f);

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

		File configFile = new File(dealerTvDir+File.separator+"config.xml");
		if(!configFile.exists() || DownloadFile.syncing)
		{
			menu.removeItem(R.id.action_sync);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.action_settings) {
			Intent i = new Intent(this, SettingActivity.class);
			// startActivityForResult(i, RESULT_SETTINGS);
			startActivity(i);
		} else if (itemId == R.id.action_restart) {
			returnIntent = new Intent();
			returnIntent.putExtra("result", "restart");
			setResult(RESULT_OK, returnIntent);
			finish();
			stopTimer();
		} else if (itemId == R.id.action_activate) {
			activateDealerTvApplication();
		} else if (itemId == R.id.action_about) {
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
		}else if (itemId == R.id.action_sync) {
			
			item.setVisible(false);
			this.invalidateOptionsMenu();
			
			syncSlides();
			
		}
		return true;
	}

	private void syncSlides()
	{
		final Runnable sync = new Runnable() {
			public void run() {
				try
				{
					String[] params = new String[3];
					
					SharedPreferences settings = PreferenceManager
							.getDefaultSharedPreferences(getBaseContext());
					String channelNumber = settings.getString(
							getString(R.string.pref_channel), "0");
					
					DownloadFile downloadContentFile = new DownloadFile();
					DownloadFile.syncing = true;
					
				File configFile = new File(dealerTvDir+File.separator+"config.xml");							 
				ArrayList<Slide> slides= utils.loadSlidesListFromConfigXml(configFile, dealerTvDir);	
				params[2] = utils.createDownloadParametersCommaSeparatedString(slides,channelNumber,MainActivity.cmsUrl);
				
				if (params[2] != null && !params[2].equals("")) {

					
					downloadContentFile.status = "downloading";					
					downloadContentFile.execute(params);

					Log.i("DealerTv", "Sync Download In Progress");
				}else
				{
					DownloadFile.syncing = false;
				}
				
				}catch(Exception e)
				{
					Log.e("DealerTv", "Synchronization Failed: "+e.getMessage());
					DownloadFile.syncing = false;
				}
				
			}

		};

		// Production
		MainActivity.scheduler.schedule(sync, 1, SECONDS);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case RESULT_SETTINGS:

			break;
		}
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		super.onMenuOpened(featureId, menu);

		pauseActivityTimer();

		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_ESCAPE:
			/* Sample for handling the Menu button globally */
			openOptionsMenu();
			return true;

		case KeyEvent.KEYCODE_DPAD_RIGHT:
			returnIntent = new Intent();
			returnIntent.putExtra("result", slide.order);
			setResult(RESULT_OK, returnIntent);
			finish();
			stopTimer();

			return true;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			returnIntent = new Intent();
			returnIntent.putExtra("result",
					String.valueOf(Integer.parseInt(slide.order) * -1));
			setResult(RESULT_OK, returnIntent);
			finish();
			stopTimer();

			return true;
		case KeyEvent.KEYCODE_MEDIA_PAUSE:
			pauseActivityTimer();
			Toast pauseToast = Toast.makeText(this,
					"Paused",
					Toast.LENGTH_LONG);
			pauseToast.show();
			
			return true;
		case KeyEvent.KEYCODE_MEDIA_PLAY:
				stopTimer();
				resumeActivityTimer();
				Toast resumeToast = Toast.makeText(this,
						"Resumed",
						Toast.LENGTH_LONG);
				resumeToast.show();
	
			return true;
			
		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:

			if(paused)
			{
				stopTimer();
				resumeActivityTimer();
				Toast resumeToast2 = Toast.makeText(this,
						"Resumed",
						Toast.LENGTH_LONG);
				resumeToast2.show();
				
			}else
			{
				pauseActivityTimer();
				Toast pauseToast2 = Toast.makeText(this,
						"Paused",
						Toast.LENGTH_LONG);
				pauseToast2.show();
			}
			
			return true;
			
		case KeyEvent.KEYCODE_DPAD_UP:

			if(paused)
			{
				stopTimer();
				resumeActivityTimer();
				Toast resumeToast2 = Toast.makeText(this,
						"Resumed",
						Toast.LENGTH_LONG);
				resumeToast2.show();
				
			}else
			{
				pauseActivityTimer();
				Toast pauseToast2 = Toast.makeText(this,
						"Paused",
						Toast.LENGTH_LONG);
				pauseToast2.show();
			}
			
			return true;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			Toast infoToast = Toast.makeText(this,
					String.format("Slide Name: %s\nContent Type: %s \nSlide Order #: %s", slide.name,slide.contentType,slide.order),
					Toast.LENGTH_LONG);
			infoToast.show();
	
			return true;	
			
		}
		return false;
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		super.onOptionsMenuClosed(menu);

		if (Installation.activation(this, "") != null
				&& !Installation.activation(this, "").equals("")) {
			menu.removeItem(R.id.action_activate);
		}
		File configFile = new File(dealerTvDir+File.separator+"config.xml");
		if(!configFile.exists())
		{
			menu.removeItem(R.id.action_sync);
		}
		resumeActivityTimer();

	}

	private void pauseActivityTimer() {
		long now = System.currentTimeMillis();
		remaining -= (now - lastUpdate);
		stopTimer();
	}

	private void resumeActivityTimer() {
		lastUpdate = System.currentTimeMillis();

		if (remaining <= 0) {
			setResult(RESULT_OK, returnIntent);
			finish();
		} else {
			timer = new Timer();
			timer.schedule(new TimerTask() {
				public void run() {
					setResult(RESULT_OK, returnIntent);
					finish();
					stopTimer();
				}
			}, ((int) remaining));
		}
	}

	private void stopTimer() {
		if (timer != null) {
			timer.cancel();
			timer.purge();
		}
	}

	private void activateDealerTvApplication() {
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

		// Calendar c = Calendar.getInstance();
		//
		// SimpleDateFormat df = new SimpleDateFormat("dd/mm/yyyy", Locale.US);
		//
		// String today = df.format(c.getTime());

		String url = MainActivity.cmsUrl + "/Api/Activation";

		String result = "";
		String activationId = "";

		try {

			JSONObject activationJsonObj = new JSONObject();
			JSONObject resultJsonObj = null;

			activationJsonObj.put("InstallationId", InstallationId);
			activationJsonObj.put("DealerTvStationId", channelNumber);
			activationJsonObj.put("IsActive", "false");
			// activationJsonObj.put("ActivationDate", today.toString());
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

		Toast toast = Toast.makeText(this,
				"Activation Failed - Check Internet Connection",
				Toast.LENGTH_SHORT);

		if (result.equals("Pass - Active") && !activationId.equals("")
				&& activationId != null) {
			Installation.activation(this, activationId);

			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("IsActive", true);
			editor.commit();

			toast = Toast.makeText(this, "Activation Succesful",
					Toast.LENGTH_SHORT);

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

		toast.show();
	}
}