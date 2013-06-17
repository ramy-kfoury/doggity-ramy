package mobi.ramy.test.activities;

import java.util.HashMap;

import mobi.ramy.test.R;
import mobi.ramy.test.models.Current;
import mobi.ramy.test.models.Venue;
import mobi.ramy.test.networking.VenueTask;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements OnItemClickListener, OnMyLocationChangeListener, OnMarkerClickListener  {
	Context mContext; 
	ProgressDialog mLoader; //this is the dialog (loading dialog)
	private GoogleMap mMap;
	private SupportMapFragment mMapFragment;
	private ViewGroup mMapContainer;
	private ViewGroup mListContainer;
	ListView mListView; //this is my venues listview
	ArrayAdapter<Venue> mAdapter; //the is the adapter of my listview
	private boolean pendingMarkers = false;
	private Location mLocation = null;
	private HashMap<Marker, String> markers;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this; 
		setContentView(R.layout.activity_main);
		mMapContainer = (ViewGroup) findViewById(R.id.rlmap);
		mListContainer = (ViewGroup) findViewById(R.id.rllist);
		//after setting the content view, get  views
		mListView = (ListView) findViewById(R.id.listView1);

		// listening to single list item on click
		mListView.setOnItemClickListener(this);  

		// start the download task
		getTheData();

		setUpMapIfNeeded();

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		venueClicked(mAdapter.getItem(position));
	}

	private void getTheData() {
		if(Current.get(mContext)._getVenues() == null && mLocation != null)
		{
			// get data if I have my location
			getTheDataFromTheInternet(false);
		}
		else if(Current.get(mContext)._getVenues() != null)
		{
			// venues are saved in defaults
			initAdapter();
		}
	}

	private void initAdapter() {
		mAdapter = new ArrayAdapter<Venue>(getBaseContext(), R.layout.list_item, R.id.name, Current.get(mContext)._getVenues()) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				((TextView)v.findViewById(R.id.name)).setText(getItem(position).getName());
				((TextView)v.findViewById(R.id.line1)).setText(getItem(position).getLine1());
				return v;
			}
		};
		mListView.setAdapter(mAdapter);
		if(mMap == null) pendingMarkers = true;
		else addMarkers();
	}

	private void addMarkers() 
	{
		mMap.clear();
		markers = new HashMap<Marker, String>();
		for(int i=0;i<mAdapter.getCount();i++) {
			markers.put(mMap.addMarker(new MarkerOptions().position(new LatLng(mAdapter.getItem(i).getLatitude(), mAdapter.getItem(i).getLongitude())).title(mAdapter.getItem(i).getName())), mAdapter.getItem(i).getID());
		}
	}

	private void getTheDataFromTheInternet(final boolean force) {
		new VenueTask("25000000", String.valueOf(mLocation.getLatitude()), String.valueOf(mLocation.getLongitude())) {
			protected void onPreExecute() {
				if(!force) {
					//before starting show a dialog
					mLoader = ProgressDialog.show(mContext, getString(R.string.loadingtitle), getString(R.string.loadingmessage), true);
				}
				else {
					if(mAdapter != null) {
						mAdapter = new ArrayAdapter<Venue>(getBaseContext(), R.layout.list_item, R.id.name, new Venue[0]);
						mListView.setAdapter(mAdapter);
					}
					mListView.setEmptyView(findViewById(R.id.llloading));
				}
			};
			protected void onPostExecute(Venue[] result) {
				if(!force) {
					//before anything, remove the dialog
					mLoader.dismiss();
				}
				if(result == null) {
					// handle the error
					
				}
				else {
					Current.get(mContext)._setVenues(result);
					initAdapter();
				}
			};
		}.execute();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the map.
		if (mMap == null) 
		{
			// Try to obtain the map from the SupportMapFragment.
			mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
			mMap = (mMapFragment).getMap();

			// Check if we were successful in obtaining the map.
			if (mMap != null)
			{
				setUpMap();
			}
		}
	}

	private void setUpMap() {
		mMap.setMyLocationEnabled(true);
		mMap.setOnMyLocationChangeListener(this);
		mMap.setOnMarkerClickListener(this);
		//		mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
		if(pendingMarkers) {
			addMarkers();
			pendingMarkers = false;
		}
	}

	@Override
	public void onMyLocationChange(Location location) {
		if(mLocation == null) {
			mLocation = location;
			getTheDataFromTheInternet(true);
			// stop retrieving new venues when location changes
			mMap.setOnMyLocationChangeListener(null);
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		String id = markers.get(marker);
		if(id != null) {
			Venue v = Current.get(mContext)._getVenue(id);
			if(v!=null) {
				venueClicked(v);
				return true;
			}
		}
		return false;
	}

	private void venueClicked(Venue v) {
		Intent i = new Intent(getApplicationContext(), FacebookLogin.class);
		// sending venue to the activity
		i.putExtra(FacebookLogin.EXTRA_VENUE, v);
		startActivity(i); 
	}
	
	
	public void listBtnClicked(View v)
	{
		LinearLayout.LayoutParams params;
		params = (LayoutParams) mMapContainer.getLayoutParams();
		params.weight = 0;
		params = (LayoutParams) mListContainer.getLayoutParams();
		params.weight = 1;
		
		findViewById(R.id.llroot).requestLayout();
	}
	
	public void mapBtnClicked(View v)
	{
		LinearLayout.LayoutParams params;
		params = (LayoutParams) mMapContainer.getLayoutParams();
		params.weight = 1;
		params = (LayoutParams) mListContainer.getLayoutParams();
		params.weight = 0;
		
		findViewById(R.id.llroot).requestLayout();
	}
	
	public void splitBtnClicked(View v)
	{
		LinearLayout.LayoutParams params;
		params = (LayoutParams) mMapContainer.getLayoutParams();
		params.weight = 1;
		params = (LayoutParams) mListContainer.getLayoutParams();
		params.weight = 1;
		
		findViewById(R.id.llroot).requestLayout();
	}

	//	@Override
	//	public boolean onCreateOptionsMenu(Menu menu) {
	//		// Inflate the menu; this adds items to the action bar if it is present.
	//		getMenuInflater().inflate(R.menu.main, menu);
	//		return true;
	//	}

}