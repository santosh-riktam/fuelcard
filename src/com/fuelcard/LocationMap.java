package com.fuelcard;

import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class LocationMap extends MapActivity{	
	Context context;
	private LocationManager locationManager = null;
	private LocationListener locationListener = null;
	private Location currentLocation;
	private String provider;
	private long minTime;
	private float minDistance;
	private MapController mapController;
	private GeoPoint geoPoint;
	double lat;
	double lon;
	private boolean around=false;
	private MyLocationOverlay myLocationOverlay;
	List<Overlay> mapOverlays;
	private boolean ref_bool=false;
	ItemLayOver currentPositionOverlay ;
	OverlayItem currentPosition;
	ItemLayOver searchPositionOverlay ;
	OverlayItem searchPosition;
	ItemLayOver fuelStationOverlay;
	
	private Button list;
	private MapView map;
	Bundle bundle=new Bundle();
	SharedPreferences prefs=null;
	int distance=0,unit=0;
	
	
	ArrayList<String> site=new ArrayList<String>();
	ArrayList<String> dist=new ArrayList<String>();
	ArrayList<String> address=new ArrayList<String>();
	double lats[],lons[];
	
	//menu items
	//private static final int MENU_SEARCH = Menu.FIRST;
    private static final int MENU_PREF = Menu.FIRST;
    private static final int MENU_INFO = Menu.FIRST + 1;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        
        Intent recivedIntent=getIntent();
        bundle=recivedIntent.getExtras();
                        
        map=(MapView)this.findViewById(R.id.map);
        list=(Button)this.findViewById(R.id.view);
        
        list.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		if(v==list){
        			Intent intent=new Intent(LocationMap.this,LocationList.class);
        			if(bundle!=null)
        				intent.putExtras(bundle);
        			startActivity(intent);
        	   	}
        	}
        	});
        initGPSControls();
        CurrentLocation();
        if(bundle!=null){
        	around=bundle.getBoolean("around");
        	site=bundle.getStringArrayList("Site");
           	dist=bundle.getStringArrayList("Distance");
        	address=bundle.getStringArrayList("Address");
        	lats=bundle.getDoubleArray("Latitude");
        	lons=bundle.getDoubleArray("Longitude");
        	setStations();
        }
        else{
        	new AlertDialog.Builder(this).setTitle("Map").setMessage("No Fuel Stations matched with your preferences near the selected location.")
        	.setPositiveButton("Ok",new DialogInterface.OnClickListener(){
        		public void onClick(DialogInterface dg,int i){
        			initGPSControls();
        			navigateToLocation(lat, lon, map);
        		}
        	})
        	.show();
        }
    }
    private void initGPSControls() {
		
		locationListener= new locationListenerGPS();
		provider=LocationManager.GPS_PROVIDER;
		minTime=1000;
		minDistance=20;
		
		locationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(provider, minTime, minDistance,locationListener);
		
		map = (MapView) findViewById(R.id.map);
		mapController=map.getController();
		map.setBuiltInZoomControls(true);
		mapOverlays = map.getOverlays();
		currentPositionOverlay = new ItemLayOver(getResources().getDrawable(R.drawable.current), LocationMap.this,true);
        currentLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        initMyLocation();
    }
    public class locationListenerGPS implements LocationListener{

		
		public void onLocationChanged(Location location) {
			if(location!=null){
				currentLocation=location;
				lat=currentLocation.getLatitude();
				lon=currentLocation.getLongitude();
				geoPoint = LatLongToPoint(String.valueOf(currentLocation.getLatitude()),String.valueOf(currentLocation.getLongitude()));
				//navigateToLocation(lat, lon, map);
			}
		}
		public void onProviderDisabled(String provider) {

		}
		public void onProviderEnabled(String provider) {
			CurrentLocation();
		}
		
		public void onStatusChanged(String provider, int status,Bundle extras) {
		}
    }
    private void CurrentLocation() {
		if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)&&!locationManager.isProviderEnabled( LocationManager.NETWORK_PROVIDER)){
	        //runOnUiThread(actionGPS);
		}
		else{
			//if(geoPoint!=null){
				currentLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if(currentLocation==null){
					currentLocation=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				}
				geoPoint = LatLongToPoint(String.valueOf(currentLocation.getLatitude()),String.valueOf(currentLocation.getLongitude()));
				lat=currentLocation.getLatitude();
				lon=currentLocation.getLongitude();
				//navigateToLocation(lat, lon, map);
			/*}
			else{
				Toast.makeText(this, "Waiting for Location ...", Toast.LENGTH_LONG).show();
			}*/
		}
	}
    private void initMyLocation() {
		myLocationOverlay=new MyLocationOverlay(this, map );
		myLocationOverlay.runOnFirstFix(new Runnable(){
			
			public void run() {
				CurrentLocation(myLocationOverlay.getMyLocation());
			}});
        
	}
    private void CurrentLocation(GeoPoint point) {
		geoPoint=point;
		CurrentLocation();
	}
    private GeoPoint LatLongToPoint(String latitude,String longitude){
		double lat = Double.parseDouble(latitude);
        double lng = Double.parseDouble(longitude);
        GeoPoint geoPoint = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
        return geoPoint;
		
	}
	protected void navigateToLocation(double latitude, double longitude, MapView map) {
		geoPoint=LatLongToPoint(String.valueOf(latitude),String.valueOf(longitude));
        currentPosition = new OverlayItem(geoPoint, "Current Position", "");
        currentPositionOverlay.addOverlay(currentPosition);
        mapOverlays.add(currentPositionOverlay);
        mapOverlays.set(0, currentPositionOverlay);
        mapController.animateTo(currentPositionOverlay.getCenter());
		mapController.setZoom(12);
        ref_bool=true;
	}
	public void setStations(){
		/*int maxLatitude=(int)lats[lats.length-1];
		int maxLongitude=(int)lons[lons.length-1];
		int minLatitude=(int)lats[0];
		int minLongitude=(int)lons[0];*/
		
		for(int i=0;i<site.size();i++){
			fuelStationOverlay = new ItemLayOver(getResources().getDrawable(R.drawable.marker_red),LocationMap.this,false);
			GeoPoint geopoint =new GeoPoint((int)(lats[i]* 1E6),(int)( lons[i]* 1E6));
			fuelStationOverlay.addOverlay(new OverlayItem(geopoint,site.get(i),dist.get(i)+"\n"+address.get(i)));
			mapOverlays.add(fuelStationOverlay);
		}
		if(!around){
			double minLatitude=bundle.getDouble("searchlat");
			double minLongitude=bundle.getDouble("searchlon");
			GeoPoint geoPoint1=LatLongToPoint(String.valueOf(minLatitude),String.valueOf(minLongitude));
			searchPositionOverlay = new ItemLayOver(getResources().getDrawable(R.drawable.current), LocationMap.this,true);
			searchPosition = new OverlayItem(geoPoint1, "Searched Location", "");
	        searchPositionOverlay.addOverlay(searchPosition);
	        //mapOverlays.add(currentPositionOverlay);
			mapController.animateTo(searchPositionOverlay.getCenter());
			mapController.setCenter(geoPoint1);
		}
		else{
			System.out.println(lat+"              "+lon);
			geoPoint=LatLongToPoint(String.valueOf(lat),String.valueOf(lon));
	        currentPosition = new OverlayItem(geoPoint, "Current Position", "");
	        currentPositionOverlay.addOverlay(currentPosition);
	        //mapOverlays.add(currentPositionOverlay);
			mapController.animateTo(currentPositionOverlay.getCenter());
			mapController.setCenter(geoPoint);
			/*minLatitude=(int)lat;
			minLongitude=(int)lon;*/
		}
		
		distance=bundle.getInt("distance");
   		unit=bundle.getInt("unit");
   		System.out.println(unit+"    "+distance);
   		//check for distance in kms and miles and decide zoomlevel accordingly
		/*switch(unit){
		case 0:
			switch(distance){
			case 20:
				mapController.setZoom(10);
			break;
			case 30:
				mapController.setZoom(9);
			break;
			case 40:
				mapController.setZoom(9);
			break;
			case 50:
				mapController.setZoom(9);
			break;
			case 60:
				mapController.setZoom(8);
			break;
			}
		break;
		case 1:
			switch(distance){
			case 20:
				mapController.setZoom(9);
			break;
			case 30:
				mapController.setZoom(9);
			break;
			case 40:
				mapController.setZoom(8);
			break;
			case 50:
				mapController.setZoom(8);
			break;
			case 60:
				mapController.setZoom(8);
			break;
			}
		break;
		}*/
			
		mapController.setZoom(10);
		map.invalidate();
	}
	
	//Menu
    @Override 
    public boolean onCreateOptionsMenu(Menu menu) {
    	//menu.add(Menu.NONE, MENU_SEARCH, 0, "Search").setIcon(android.R.drawable.ic_menu_search);
        menu.add(Menu.NONE, MENU_PREF, 0, "Preferences").setIcon(android.R.drawable.ic_menu_preferences);
        menu.add(Menu.NONE, MENU_INFO, 0, "Info").setIcon(android.R.drawable.ic_menu_info_details);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case MENU_PREF:
            	Intent intent1= new Intent(LocationMap.this,Preferences.class );
            	startActivity(intent1);
            break;
            case MENU_INFO:
            	Intent intent= new Intent(LocationMap.this,Info.class );
        		startActivity(intent);
                break;
        }
        return true;
    }
   @Override
	protected boolean isRouteDisplayed() {
		return false;
	}
   @Override
	public void onConfigurationChanged(Configuration newConfig) {
		// Ignore orientation change not to restart activity
		super.onConfigurationChanged(newConfig);
		}
   public class ItemLayOver extends BalloonItemizedOverlay<OverlayItem> {
		
		private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();
		Context context;
		
		public ItemLayOver(Drawable defaultMarker, Context context,boolean current) {
			super(boundCenterBottom(defaultMarker), map,current);
			this.context=context;
		}

		@Override
		protected OverlayItem createItem(int i) {
		  return overlays.get(i);
		}

		@Override
		public int size() {
		  return overlays.size();
		}
		
		public void addOverlay(OverlayItem overlay) {
		    overlays.add(overlay);
		    populate();
		}
		@Override
		protected boolean onBalloonTap(int index) {
			return true;
		}
	}
}

