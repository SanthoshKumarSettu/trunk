package com.dealersaleschannel.tv;

public class WeatherAlert {

	private String type;
	private String description;
	private String date;
	private String expires;
	private String message;
	
	public String getType() {
		return type;
	}

	protected void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	protected void setDescription(String description) {
		this.description = description;
	}

	public String getDate() {
		return date;
	}

	protected void setDate(String date) {
		this.date = date;
	}

	public String getExpires() {
		return expires;
	}

	protected void setExpires(String expires) {
		this.expires = expires;
	}

	public String getMessage() {
		return message;
	}

	protected void setMessage(String message) {
		this.message = message;
	}
	
}
