package com.fuelcard;

import java.util.ArrayList;
import com.fuelcard.Search.locationListenerGPS;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class LocationList extends ListActivity{
	private Button map;
	Bundle bundle=new Bundle();
	SharedPreferences prefs=null;
	
	Location loc;
	LocationManager locationManager;
	private locationListenerGPS locationListener;
	private String providerGPS;
	private String providerNW;
	private long minTime;
	private float minDistance;
	
	ArrayList<String> site=new ArrayList<String>();
	ArrayList<String> dist=new ArrayList<String>();
	ArrayList<String> address=new ArrayList<String>();
	String siteArr[];
	double lats[],lons[];
		
	//menu items
	//private static final int MENU_SEARCH = Menu.FIRST;
    private static final int MENU_PREF = Menu.FIRST;
    private static final int MENU_INFO = Menu.FIRST + 1;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	prefs = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
      
        Intent recivedIntent=getIntent();
        bundle=recivedIntent.getExtras();
        if(bundle!=null){
        	
        	site=bundle.getStringArrayList("Site");
        	siteArr=new String[site.size()];
        	siteArr=site.toArray(siteArr);
        	dist=bundle.getStringArrayList("Distance");
        	address=bundle.getStringArrayList("Address");
        	lats=bundle.getDoubleArray("Latitude");
        	lons=bundle.getDoubleArray("Longitude");
        	
        	final SharedPreferences.Editor spe = prefs.edit();
        	if(prefs.getBoolean("Reminder",true)){
        	new AlertDialog.Builder(this)
    	    .setTitle("HELP")
    	    .setMessage("Click on any of the stations in the list to get directions to it.")
    	    .setNegativeButton("Skip",new DialogInterface.OnClickListener(){
    	    	public void onClick(DialogInterface dialog, int id){
   					spe.putBoolean("Reminder",false);
   					spe.commit();
    	    	}
    	    })
    	    .setPositiveButton("Remind Later",new DialogInterface.OnClickListener(){
    	    	public void onClick(DialogInterface dialog, int id){
   					spe.putBoolean("Reminder",true);
   					spe.commit();
    	    	}
    	    })
    	    .show();
        	}
        	setListAdapter(new ListItemsAdapter(this));
        }
        else{
        	new AlertDialog.Builder(this).setTitle("List").setMessage("No Fuel Stations matched with your preferences near the selected location.")
        	.setNegativeButton("Ok",new DialogInterface.OnClickListener(){
        		public void onClick(DialogInterface dg,int i){
        			finish();
        		}
        	})
        	.show();
        }
       
        map=(Button)this.findViewById(R.id.view);
        
        map.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		if(v==map){
        			/*Intent intent=new Intent(LocationList.this,LocationMap.class);
        			startActivity(intent);*/
        			finish();
        	   	}
        	}
        	});
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
            	Intent intent1= new Intent(LocationList.this,Preferences.class );
            	startActivity(intent1);
            break;
            case MENU_INFO:
            	Intent intent= new Intent(LocationList.this,Info.class );
        		startActivity(intent);
                break;
        }
        return true;
    }
    class ListItemsAdapter extends ArrayAdapter<Object> {
		Activity context;
		String temp;
		ListItemsAdapter(Context context) {
			super(context, R.layout.row, siteArr);
			this.context = (Activity) context;
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = context.getLayoutInflater();
			View row = inflater.inflate(R.layout.row, null);
			TextView name = (TextView) row.findViewById(R.id.name);
			TextView disttxt = (TextView) row.findViewById(R.id.dist);
			TextView addr = (TextView) row.findViewById(R.id.addr);
			if(site.get(position).length()>27){
				name.setText(site.get(position).substring(0,27)+"...");
			}
			else
				name.setText(site.get(position));
			disttxt.setText(dist.get(position));
			addr.setText(address.get(position));
			return (row);
    	}
	}
    //@SuppressWarnings("unchecked")
	public void onListItemClick(ListView parent, View v, final int position, long id) {
				initGPSUpdates();
    			initControls();
    	    	Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
    	    			Uri.parse("http://maps.google.com/maps?saddr="+loc.getLatitude()+","+loc.getLongitude()+"&daddr="+lats[position]+","+lons[position]));
    	    			startActivity(intent);
    }
	private void initGPSUpdates() {
		providerGPS=LocationManager.GPS_PROVIDER;
		providerNW=LocationManager.NETWORK_PROVIDER;
		minTime=60000;
		minDistance=0;
		locationListener= new locationListenerGPS();
		locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(providerGPS, minTime, minDistance,locationListener);
		locationManager.requestLocationUpdates(providerNW, minTime, minDistance,locationListener);
	}
	@Override
	protected void onStop() {
		super.onStop();
		if(locationManager!=null && locationListener!=null)
		locationManager.removeUpdates(locationListener);
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
		locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
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
    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		// Ignore orientation change not to restart activity
		super.onConfigurationChanged(newConfig);
		}
}


