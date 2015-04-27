package com.dealersaleschannel.tv;

import android.os.Parcel;
import android.os.Parcelable;

public class Slide implements Parcelable
{
	String order;
	String contentType;
	String backgroundColor;
	String displayTime;
	String textColor;
	String textSize;
	String textFileMD5;
	String data;
	String headerImage;
	String headerImageMD5;
	String footerImage;
	String footerImageMD5;
	String make;
	String model;
	String stock;
	String year;
	String industry;
	String location;
	String price;
	String hours;
	String notes;
	String layout;
	String backgroundimage;
	String equipmentinfohighlightcolor;
	String textshadowcolor;
	String istextdropshadow;
	String ishighlightequipmentinfo;
	String backgroundImageMD5;
	String name;
	String category;
	String salescontact;
	
	public Slide()
	{
		
	}
	
	public Slide(Parcel in)
	{
		readFromParcel(in);
	}
	
	@SuppressWarnings("rawtypes")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Slide createFromParcel(Parcel in) {
            return new Slide (in);
        }

        public Slide [] newArray(int size) {
            return new Slide [size];
        }
    };
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(order);
        dest.writeString(contentType);
        dest.writeString(backgroundColor);
        dest.writeString(displayTime);
        dest.writeString(textColor);
        dest.writeString(textSize);
        dest.writeString(textFileMD5);
        dest.writeString(data);
        dest.writeString(headerImage);
        dest.writeString(headerImageMD5);
        dest.writeString(footerImage);
        dest.writeString(footerImageMD5);
        dest.writeString(make);
        dest.writeString(model);
        dest.writeString(stock);
        dest.writeString(year);
        dest.writeString(industry);
        dest.writeString(location);
        dest.writeString(price);
        dest.writeString(hours);
        dest.writeString(notes);
        dest.writeString(layout);
        dest.writeString(backgroundimage);
        dest.writeString(equipmentinfohighlightcolor);
        dest.writeString(textshadowcolor);
        dest.writeString(istextdropshadow);
        dest.writeString(ishighlightequipmentinfo);
        dest.writeString(backgroundImageMD5);
        dest.writeString(name);
        dest.writeString(category);
        dest.writeString(salescontact);

	}
		
	private void readFromParcel(Parcel in) {
		order = in.readString();
		contentType = in.readString();
		backgroundColor = in.readString();
		displayTime = in.readString();
		textColor = in.readString();
		textSize = in.readString();
		textFileMD5 = in.readString();
		data = in.readString();
		headerImage = in.readString();
		headerImageMD5 = in.readString();
		footerImage = in.readString();
		footerImageMD5 = in.readString();
		make = in.readString();
		model = in.readString();
		stock = in.readString();
		year = in.readString();
		industry = in.readString();
		location = in.readString();
		price = in.readString();
		hours = in.readString();
		notes = in.readString();
		layout = in.readString();
		backgroundimage = in.readString();
		equipmentinfohighlightcolor = in.readString();
		textshadowcolor = in.readString();
		istextdropshadow = in.readString();
		ishighlightequipmentinfo = in.readString();
		backgroundImageMD5 = in.readString();
		name = in.readString();
		category = in.readString();
		salescontact = in.readString();
	}

	public boolean slideAttributeEquals(String slideAttributeOne, String slideAttributeTwo)
	{
		if(slideAttributeOne == null && slideAttributeTwo == null)
		{
			return true;
			
		}else if(slideAttributeOne != null && slideAttributeTwo == null)
		{
			return false;
			
		}else if(slideAttributeOne == null && slideAttributeTwo != null)
		{
			return false;
			
		}else if(slideAttributeOne != null && slideAttributeTwo != null)
		{
			if(slideAttributeOne.equals(slideAttributeTwo))
			{
				return true;				
			}					
		}
			
		return false;
		
	}
}
