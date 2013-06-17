package mobi.ramy.test.models;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Venue implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5715384829156254166L;
	private String id;
	private String name;
	private String line1;
	private String postCode;
	private String area;
	private String city;
	private String description;
	private String water;
	private String distance;
	private String type;
	private String maxDogSize;
	private double longitude;
	private double latitude;
	private boolean postToFacebook;
	
	// Create Venue object from JSON Object
	public Venue(JSONObject o) throws JSONException {
		id = o.getString("id");
		name = o.getString("name");
		line1 = o.getString("line1");
		postCode = o.optString("postCode");
		area = o.optString("area");
		city = o.optString("city");
		description = o.optString("description");
		water = o.optString("water");
		distance = o.optString("distance");
		type = o.optString("type");
		maxDogSize = o.optString("maxDogSize");
		longitude = o.getDouble("longitude");
		latitude = o.getDouble("latitude");
		postToFacebook = o.optBoolean("postToFacebook", false);
	}
	public String getID() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getLine1() {
		return line1;
	}
	public double getLatitude() {
		return latitude;
	}
	public double getLongitude() {
		return longitude;
	}
}
