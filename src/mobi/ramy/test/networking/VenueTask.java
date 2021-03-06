package mobi.ramy.test.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import mobi.ramy.test.models.Venue;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class VenueTask extends AsyncTask<Void, Void, Venue[]> {
	private static String url = "http://api.dev.friendbuy.de/venue/location";
	String mRange, mLatitude, mLongitude;
	public VenueTask(String range, String latitude, String longitude) {
		mRange = range;
		mLatitude = latitude;
		mLongitude = longitude;
	}

	@Override
	protected Venue[] doInBackground(Void... params) {
		// Making HTTP request
		try {
			// defaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
			nameValuePairs.add(new BasicNameValuePair("range", mRange));
			nameValuePairs.add(new BasicNameValuePair("latitude", mLatitude));
			nameValuePairs.add(new BasicNameValuePair("longitude", mLongitude));

			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			InputStream is = httpEntity.getContent();    

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "UTF-8"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			String response = sb.toString();

			Log.v("VenueTask", response);

			JSONObject oo = new JSONObject(response);
			JSONArray array = oo.getJSONObject("Venues").getJSONArray("Venue");
			Venue[] results = new Venue[array.length()];
			for(int i=0;i<array.length();i++) {
				results[i] = new Venue(array.getJSONObject(i));
			}
			return results;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}  catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


}
