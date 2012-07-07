package com.fuelcard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Search extends Activity {
	Context context;
	Location loc;
	LocationManager locationManager;
	private locationListenerGPS locationListener;
	private String providerGPS;
	private String providerNW;
	private long minTime;
	private float minDistance;

	Button search, nearby;
	private Runnable searchService;
	private ProgressDialog dialog = null;

	// menu items
	// private static final int MENU_SEARCH = Menu.FIRST;
	private static final int MENU_PREF = Menu.FIRST;
	private static final int MENU_INFO = Menu.FIRST + 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		context = this;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);

		final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
		}

		search = (Button) this.findViewById(R.id.search);
		nearby = (Button) this.findViewById(R.id.nearby);

		search.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(Search.this, SearchCity.class);
				startActivity(intent);
			}
		});
		nearby.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				initGPSUpdates();
				initControls();
				searchService = new Runnable() {

					public void run() {
						getData();
					}
				};
				Thread thread = new Thread(null, searchService, "Background");
				thread.start();
				dialog = new ProgressDialog(context);
				dialog.setMessage("Searching...Please Wait");
				dialog.setIndeterminate(true);
				dialog.setCancelable(false);
				dialog.show();

			}
		});
	}

	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Yout GPS seems to be disabled, please enable it.")
				.setTitle("Warning")
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	public void getData() {
		CalculateResults c = new CalculateResults(context);
		Bundle b = c.getResults(loc.getLongitude(), loc.getLatitude());
		// System.out.println("Records........"+b.getStringArrayList("Site").size());
		Intent intent1 = new Intent(Search.this, LocationMap.class);
		if (b != null) {
			intent1.putExtras(b);
			intent1.putExtra("around", true);
		}
		startActivity(intent1);
		runOnUiThread(UIUpdate);
	}

	private Runnable UIUpdate = new Runnable() {

		public void run() {
			dialog.dismiss();
		}
	};

	// Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// menu.add(Menu.NONE, MENU_SEARCH, 0,
		// "Search").setIcon(android.R.drawable.ic_menu_search);
		menu.add(Menu.NONE, MENU_PREF, 0, "Preferences").setIcon(
				android.R.drawable.ic_menu_preferences);
		menu.add(Menu.NONE, MENU_INFO, 0, "Info").setIcon(
				android.R.drawable.ic_menu_info_details);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_PREF:
			Intent intent1 = new Intent(Search.this, Preferences.class);
			startActivity(intent1);
			break;
		case MENU_INFO:
			Intent intent = new Intent(Search.this, Info.class);
			startActivity(intent);
			break;
		}
		return true;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// Ignore orientation change not to restart activity
		super.onConfigurationChanged(newConfig);
	}

	private void initGPSUpdates() {
		providerGPS = LocationManager.GPS_PROVIDER;
		providerNW = LocationManager.NETWORK_PROVIDER;
		minTime = 60000;
		minDistance = 0;
		locationListener = new locationListenerGPS();
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(providerGPS, minTime,
				minDistance, locationListener);
		locationManager.requestLocationUpdates(providerNW, minTime,
				minDistance, locationListener);
	}

	public class locationListenerGPS implements LocationListener {

		public void onLocationChanged(Location location) {
			initControls();
		}

		public void onProviderDisabled(String provider) {

		}

		public void onProviderEnabled(String provider) {

		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

	public void initControls() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
				&& !locationManager
						.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			// gps disabled
		} else {
			loc = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			Log.d("MBT", "" + loc);
			if (loc == null) {

				loc = locationManager
						.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}
			try {

			} catch (Exception e) {
			}

		}
	}
}
