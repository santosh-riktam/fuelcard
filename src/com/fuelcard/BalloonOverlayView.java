package com.fuelcard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fuelcard.LocationList.locationListenerGPS;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class BalloonOverlayView extends FrameLayout {

	private LinearLayout layout;
	private TextView title;
	private TextView snippet;
	private Button dir;
	private double lat,lon;
	
	Location loc;
	LocationManager locationManager;
	private locationListenerGPS locationListener;
	private String providerGPS;
	private String providerNW;
	private long minTime;
	private float minDistance;
	final Context context; 

	public BalloonOverlayView(final Context context, int balloonBottomOffset) {

		super(context);

		setPadding(10, 0, 10, balloonBottomOffset);
		layout = new LinearLayout(context);
		layout.setVisibility(VISIBLE);

		this.context=context;
		
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.balloon_map_overlay, layout);
		title = (TextView) v.findViewById(R.id.balloon_item_title);
		snippet = (TextView) v.findViewById(R.id.balloon_item_snippet);
		dir = (Button) v.findViewById(R.id.balloon_item_dir);

		ImageView close = (ImageView) v.findViewById(R.id.close_img_button);
		close.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				layout.setVisibility(GONE);
			}
		});
		dir.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				initGPSUpdates();
				initControls();
		    	Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
		    			Uri.parse("http://maps.google.com/maps?saddr="+loc.getLatitude()+","+loc.getLongitude()+"&daddr="+lat+","+lon));
		    			Activity a=(Activity)context;
		    			a.startActivity(intent);
				layout.setVisibility(GONE);
			}
		});  
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.NO_GRAVITY;

		addView(layout, params);

	}

	public void setData(OverlayItem item) {
		
		layout.setVisibility(VISIBLE);
		if (item.getTitle() != null) {
			title.setVisibility(VISIBLE);
			title.setText(item.getTitle());
		} else {
			title.setVisibility(GONE);
		}
		if (item.getSnippet() != null) {
			snippet.setVisibility(VISIBLE);
			snippet.setText(item.getSnippet());
		} else {
			snippet.setVisibility(GONE);
		}
		
	}
	public void setLatLon(GeoPoint point)
	{
		lat=point.getLatitudeE6()/1E6;
		lon=point.getLongitudeE6()/1E6;
	}
	private void initGPSUpdates() {
		providerGPS=LocationManager.GPS_PROVIDER;
		providerNW=LocationManager.NETWORK_PROVIDER;
		minTime=60000;
		minDistance=0;
		locationListener= new locationListenerGPS();
		locationManager=(LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(providerGPS, minTime, minDistance,locationListener);
		locationManager.requestLocationUpdates(providerNW, minTime, minDistance,locationListener);
	}
	
	public class locationListenerGPS implements LocationListener{
		
		public void onLocationChanged(Location location) {
			initControls();
		}
		
		public void onProviderDisabled(String provider) {
			
		}
		
		public void onProviderEnabled(String provider) {
			
		}
		
		public void onStatusChanged(String provider, int status,Bundle extras) {
		}
	}

	public void initControls() {
		locationManager=(LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		if(!locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER)&&!locationManager.isProviderEnabled( LocationManager.NETWORK_PROVIDER)){
			//gps diabled
		}
		else{
			loc=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if(loc==null){
				loc=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);	
			}			
			try{
				
			}catch(Exception e){
			}

		}
	}
}
