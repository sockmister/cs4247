package com.cs4247;

import org.json.JSONException;
import org.json.JSONObject;

public class Event {
	private String address1;
	private String address2;
	private String contactemail;
	private String contactphone;
	private String description;
	private String eventname;
	private String image1;
	private String image2;
	private String image3;
	private String image4;
	private String image5;
	private Double la;
	private Double lo;
	
	public Event(JSONObject json){
		try {
			address1 = json.getString("address1");
			address2 = json.getString("address2");
			contactemail = json.getString("contactemail");
			contactphone = json.getString("contactphone");
			description = json.getString("description");
			eventname = json.getString("eventname");
			image1 = json.getString("image1");
			image2 = json.getString("image2");
			image3 = json.getString("image3");
			image4 = json.getString("image4");
			image5 = json.getString("image5");
			la = Double.valueOf(json.getString("la"));
			lo = Double.valueOf(json.getString("lo"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getAddress1() {
		return address1;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public String getContactemail() {
		return contactemail;
	}

	public void setContactemail(String contactemail) {
		this.contactemail = contactemail;
	}

	public String getContactphone() {
		return contactphone;
	}

	public void setContactphone(String contactphone) {
		this.contactphone = contactphone;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEventname() {
		return eventname;
	}

	public void setEventname(String eventname) {
		this.eventname = eventname;
	}

	public String getImage1() {
		return image1;
	}

	public void setImage1(String image1) {
		this.image1 = image1;
	}

	public String getImage2() {
		return image2;
	}

	public void setImage2(String image2) {
		this.image2 = image2;
	}

	public String getImage3() {
		return image3;
	}

	public void setImage3(String image3) {
		this.image3 = image3;
	}

	public String getImage4() {
		return image4;
	}

	public void setImage4(String image4) {
		this.image4 = image4;
	}

	public String getImage5() {
		return image5;
	}

	public void setImage5(String image5) {
		this.image5 = image5;
	}

	public Double getLa() {
		return la;
	}

	public void setLat(Double la) {
		this.la = la;
	}

	public Double getLo() {
		return lo;
	}

	public void setLon(Double lo) {
		this.lo = lo;
	}
}
