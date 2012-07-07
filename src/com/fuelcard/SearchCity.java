package com.fuelcard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class SearchCity extends Activity{
	Context context;
	private Button searchbtn;
	private EditText txt;
	String search="london";
	double lat;
	double lon;
	String searchEncoded;
	String searchStart="0"; 
	String URL;
	private String enc="UTF-8";
	private Runnable searchService;
	private ProgressDialog dialog = null;
	
	//menu items
	//private static final int MENU_SEARCH = Menu.FIRST;
    private static final int MENU_PREF = Menu.FIRST;
    private static final int MENU_INFO = Menu.FIRST + 1;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	context=this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchcity);
        
        txt=(EditText)this.findViewById(R.id.pin);
        searchbtn=(Button)this.findViewById(R.id.searchplace);
        
        searchbtn.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		if(!getErrors()){
        		searchService = new Runnable(){
		            
		            public void run() {
		                getData();
		            }
		        };
		        Thread thread =  new Thread(null, searchService, "Background");
		        thread.start();
		        dialog =new ProgressDialog(context);
		        dialog.setMessage("Searching...Please Wait");
		        dialog.setIndeterminate(true);
		        dialog.setCancelable(false);
		        dialog.show();
        		}	
        	}
        });
    }
    public void getData()
    {
    	getJSON(txt.getText().toString());
		CalculateResults c=new CalculateResults(context);
  		Bundle b=c.getResults(lon,lat);
  		//System.out.println("Records........"+b.getDoubleArray("Latitude").length);
		Intent intent1= new Intent(SearchCity.this,LocationMap.class);
		if(b!=null){
			intent1.putExtras(b);
			intent1.putExtra("around",false);
			intent1.putExtra("searchlat",lat);
			intent1.putExtra("searchlon",lon);
		}
		startActivity(intent1);
		runOnUiThread(UIUpdate);
    }
    private Runnable UIUpdate = new Runnable() {
        
        public void run() {
        	dialog.dismiss();
        }
    };
    public boolean getErrors(){
    	if(txt.getText().toString().trim().length()==0){
    		Toast.makeText(this,"Please enter location",Toast.LENGTH_LONG).show();
    		return true;
    	}
    	return false;
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
            	Intent intent1= new Intent(SearchCity.this,Preferences.class );
            	intent1.putExtra("city",txt.getText().toString());
        		startActivity(intent1);
            break;
            case MENU_INFO:
            	Intent intent= new Intent(SearchCity.this,Info.class );
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
    public void getJSON(String search){
    	
		try { searchEncoded=URLEncoder.encode(search+",UK", enc); } catch (UnsupportedEncodingException e1) { e1.printStackTrace(); }
		
		URL="http://maps.google.com/maps/api/geocode/json?address="+searchEncoded+"&sensor=true";
		 
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(URL);
		HttpResponse httpResponse;
		HttpEntity entity;
		JSONObject json;
        try {
        	httpResponse = httpClient.execute(httpget);
            Log.i("Status",httpResponse.getStatusLine().toString());
            entity = httpResponse.getEntity();
 
            if (entity != null) {
 
                // A Simple JSON Response Read
                InputStream instream = entity.getContent();
                String result= convertStreamToString(instream);
 
                // A Simple JSONObject Creation
                json=new JSONObject(result);
                getJsonData(json);
                //System.out.println(json.get("lng"));
                // Closing the input stream will trigger connection release
                instream.close();   
            }
        } catch (ClientProtocolException e) { e.printStackTrace(); }
        catch (IOException e) { e.printStackTrace(); }
        catch (JSONException e) { e.printStackTrace(); }
	}
    public void getJsonData(JSONObject json){
		try {
			
			String responseData=json.getString("results");
				JSONArray responseJSONArr=new JSONArray(responseData);
				JSONObject resultJSONObj=responseJSONArr.getJSONObject(0);
				JSONObject mainJSONObj = resultJSONObj.getJSONObject("geometry");
					lat=Double.valueOf(mainJSONObj.getJSONObject("location").getString("lat"));
					lon=Double.valueOf(mainJSONObj.getJSONObject("location").getString("lng"));
				//}	
		
		} catch (JSONException e) { e.printStackTrace(); }
	}
    private static String convertStreamToString(InputStream is) {

    	BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
 
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) { e.printStackTrace(); }
        finally {
            try { is.close(); } catch (IOException e) { e.printStackTrace(); }
        }
        return sb.toString();
    }
}
