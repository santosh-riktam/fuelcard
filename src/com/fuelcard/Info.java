package com.fuelcard;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Info extends Activity{
	Button about,recommend;
	//menu items
	private static final int MENU_SEARCH = Menu.FIRST;
    private static final int MENU_PREF = Menu.FIRST+1;
   // private static final int MENU_INFO = Menu.FIRST + 1;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info);
        about=(Button)this.findViewById(R.id.about);
        recommend=(Button)this.findViewById(R.id.recommend);
        
        about.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		Intent intent= new Intent(Info.this,About.class );
        		startActivity(intent);  
        	}
        });
        recommend.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        
        		final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND); 
        		emailIntent .setType("plain/text"); 
        		emailIntent .putExtra(android.content.Intent.EXTRA_EMAIL, ""); 
        		emailIntent .putExtra(android.content.Intent.EXTRA_SUBJECT, "Fuel Card Android application"); 
        		emailIntent .putExtra(android.content.Intent.EXTRA_TEXT, "Hi,\n I have found Fuel Card Android application which is very useful to find nearby fuel station.\n\nSent from my Android Phone."); 
        		startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        	}
        });
    }
    //Menu
    @Override 
    public boolean onCreateOptionsMenu(Menu menu) {
    	//menu.add(Menu.NONE, MENU_SEARCH, 0, "Search").setIcon(android.R.drawable.ic_menu_search);
        menu.add(Menu.NONE, MENU_SEARCH, 0, "Search").setIcon(android.R.drawable.ic_menu_search);
        menu.add(Menu.NONE, MENU_PREF, 0, "Preferences").setIcon(android.R.drawable.ic_menu_preferences);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case MENU_PREF:
            	Intent intent1= new Intent(Info.this,Preferences.class);
            	startActivity(intent1);
            break;
            case MENU_SEARCH:
            	Intent intent= new Intent(Info.this,Search.class );
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
}

