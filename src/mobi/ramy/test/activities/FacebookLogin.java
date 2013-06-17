package mobi.ramy.test.activities;

import java.io.File;

import mobi.ramy.test.R;
import mobi.ramy.test.models.Current;
import mobi.ramy.test.models.User;
import mobi.ramy.test.models.Venue;
import mobi.ramy.test.networking.FacebookTask;

import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.HttpClientImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class FacebookLogin extends Activity implements StatusCallback {
	StatusCallback statusCallback = this;
	// private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	public static final String EXTRA_VENUE = "venue";
	Context mContext;
	Venue mVenue;
	ProgressDialog mLoader; // this is the loading dialog

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.facebook_login);
		Object o = getIntent().getSerializableExtra(EXTRA_VENUE);
		if(o == null)
		{
			// object not loaded, end activity
			finish();
			return;
		}
		else {
			mVenue = (Venue) o;
		}
		TextView titleLbl = (TextView) findViewById(R.id.titleLabel);
		titleLbl.setText(mVenue.getName());

		if (Current.get(mContext)._getFacebookId() != null) {
			//we are logged in, change view
			welcome();
		}
		else {
			//we are not logged in, load default view
		}
	}
	// change to logged in view
	private void welcome() {
		final TextView welcome = (TextView) findViewById(R.id.welcome);
		welcome.setText(Current.get(mContext)._getFirstName() + " " + Current.get(mContext)._getLastName());
		Toast.makeText(getApplicationContext(), getText(R.string.welcome) + " " + Current.get(mContext)._getFirstName(), Toast.LENGTH_SHORT).show();
		findViewById(R.id.fbbtn).setEnabled(false);
		findViewById(R.id.llloggedinas).setVisibility(View.VISIBLE);
		initImageLoader();
		ImageLoader.getInstance().displayImage("http://graph.facebook.com/"+Current.get(mContext)._getFacebookId()+"/picture?type=normal", (ImageView) findViewById(R.id.image));
	}

	public void backBtnClicked(View v) {
		finish();
	}

	public void loginBtnClicked(final View v) {
		// start Facebook Login
		Session.openActiveSession(this, true, statusCallback);
	}

	private void doRequest(JSONObject jsonObj) {
		String profileString = jsonObj.toString();
		String authString = null;
		java.util.Date expiryDate = Session.getActiveSession().getExpirationDate();
		long currentTimestamp = System.currentTimeMillis();
		long diff = (expiryDate.getTime() - currentTimestamp) / 1000;
		//		String expires = String.valueOf(diff);

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("expiresIn", diff);
			jsonObject.put("userID", Current.get(mContext)._getFacebookId());
			jsonObject.put("accessToken", Session.getActiveSession().getAccessToken());
			authString = jsonObject.toString();
//			String signedRequest;
			//				 signedRequest = calculateRFC2104HMAC(authString, getString(R.string.app_secret));
			//				jsonObject.put("signedRequest", signedRequest);
			authString = jsonObject .toString();

			new FacebookTask(profileString, authString) {
				protected void onPreExecute() {
					// before starting show a dialog
					mLoader = ProgressDialog .show(mContext, getString(R.string.loadingfbtitle), getString(R.string.loadingmessage), true);
				};

				protected void onPostExecute( User result) {
					// before anything, remove the dialog
					mLoader.dismiss();
					if (result == null) {
						// handle the error
						Toast.makeText(getApplicationContext(), getText(R.string.requestfailed), Toast.LENGTH_LONG).show();

					} else {
						// display in long period of time
						Toast.makeText(getApplicationContext(), getText(R.string.welcome) + " " + result.getName(), Toast.LENGTH_LONG).show();
					}
				};
			}.execute();

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/*
	public static String calculateRFC2104HMAC(String data, String appSecret)
			throws java.security.SignatureException {
		String result;
		try {

			// get an hmac_sha1 key from the raw key bytes
			SecretKeySpec signingKey = new SecretKeySpec(appSecret.getBytes(),
					HMAC_SHA1_ALGORITHM);

			// get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);

			// compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(data.getBytes());
			// base64-encode the hmac
			// encoding byte array into base 64
			result = Base64.encodeToString(rawHmac, 0);

		} catch (Exception e) {
			throw new SignatureException("Failed to generate HMAC : "
					+ e.getMessage());
		}
		return result;
	}
	 */


	// Facebook callback
	@Override
	public void call(Session session, SessionState state, Exception exception) {
		if (session.isOpened()) {
			// make request to the /me API
			Request.executeMeRequestAsync(session, new GraphUserCallback() {

				@Override
				public void onCompleted(GraphUser user, Response response) {
					if (user != null && response.getError() == null) {
						Current.get(mContext)._setUser(user.getId(), user.getFirstName(), user.getLastName());
						welcome();
						doRequest(user.getInnerJSONObject());
					}
				}


			});
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(Session.getActiveSession() != null)
			Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}


	// Image loader class
	private void initImageLoader() {
		ImageLoader imageLoader = ImageLoader.getInstance();
		File cacheDir = StorageUtils.getOwnCacheDirectory(mContext, "shushspace/imagecache");
		HttpParams params = new BasicHttpParams();
		// Turn off stale checking. Our connections break all the time anyway,
		// and it's not worth it to pay the penalty of checking every time.
		HttpConnectionParams.setStaleCheckingEnabled(params, false);
		// Default connection and socket timeout of 10 seconds. Tweak to taste.
		HttpConnectionParams.setConnectionTimeout(params, 10 * 1000);
		HttpConnectionParams.setSoTimeout(params, 10 * 1000);
		HttpConnectionParams.setSocketBufferSize(params, 8192);

		// Don't handle redirects -- return them to the caller. Our code
		// often wants to re-POST after a redirect, which we must do ourselves.
		HttpClientParams.setRedirecting(params, true);
		// Set the specified user agent and register standard protocols.
		HttpProtocolParams.setUserAgent(params, "ANDROID-SHUSH-LOADER");
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

		ClientConnectionManager manager = new ThreadSafeClientConnManager(params, schemeRegistry);
		ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(mContext)
		.threadPoolSize(3)
		.tasksProcessingOrder(QueueProcessingType.LIFO)
		.denyCacheImageMultipleSizesInMemory()
		.memoryCache(new WeakMemoryCache()) // You can pass your own memory cache implementation
		.discCache(new LimitedAgeDiscCache(cacheDir, 2*24*60*60)) // You can pass your own disc cache implementation
		.discCacheFileNameGenerator(new HashCodeFileNameGenerator())
		.imageDownloader(new HttpClientImageDownloader(mContext, new DefaultHttpClient(manager, params)))
		.defaultDisplayImageOptions
		(
				new DisplayImageOptions.Builder()
				//					           .showStubImage(R.drawable.stub_image)
				//					           .showImageForEmptyUri(mNullDrawable)
				//					           .cacheInMemory()
				.cacheOnDisc()
				.resetViewBeforeLoading()
				.imageScaleType(ImageScaleType.EXACTLY)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.build()
				);
		// Initialize ImageLoader with created configuration. Do it once on Application start.
		imageLoader.init(builder.build());
	}
}
