package mobi.ramy.test.models;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class User implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8661584144837902254L;
	private String id;
	private String name;
	private String email;
	private String accessToken;
	private String facebookId;
	private String picture;
	private boolean isSuperuser;
	
	// Create User object from JSON Object
		public User(JSONObject o) throws JSONException {
			id = o.getString("id");
			name = o.getString("name");
			email = o.getString("email");
			accessToken = o.optString("accessToken");
			facebookId = o.optString("facebookId");
			picture = o.optString("picture");
			isSuperuser = o.optBoolean("isSuperuser", false);
		}
		
		public String getId()
		{
			return id;
		}
		
		public String getName()
		{
			return name;
		}
		
		public String getEmail()
		{
			return email;
		}
		
		public String accessToken()
		{
			return accessToken;
		}

		public String getFacebookID()
		{
			return facebookId;
		}
		
		public String getPicture()
		{
			return picture;
		}
		
		public Boolean getIsSuperUser()
		{
			return isSuperuser;
		}
}
