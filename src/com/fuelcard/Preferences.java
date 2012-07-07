package com.fuelcard;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Preferences extends Activity {
	Intent recivedIntent;
	private ArrayAdapter<String> adaptercard, adapterdist, adapterunit;
	private Spinner card, dist, unit;
	private ToggleButton hour, hgv;
	private ImageView card_pic;
	SharedPreferences prefs = null;

	private String card_opt[] = { "Euroshell Fleet Fuel Card (multi network)",
			"Euroshell CRT Fuel Card", "Euroshell single network",
			"Esso Fuel Card (single network)",
			"Esso Fuel Card (multi network)", "Esso Truck Card",
			"Texaco Fastfuel Fuel Card", "Keyfuels Fuel Card" };
	private String pic_opt[] = { "shell_fuel_card_multi",
			"shell_fuel_card_international", "shell_fuel_card_single",
			"esso_fuel_card_single", "esso_fuel_card_multi",
			"esso_fuel_card_truck", "texaco_fuel_card", "keyfuels_fuel_card" };
	private String dist_opt[] = { "20", "30 ", "40", "50", "60" };
	private String unit_opt[] = { "kms", "miles" };

	// menu items
	private static final int MENU_SEARCH = Menu.FIRST;
	private static final int MENU_INFO = Menu.FIRST + 1;

	// private static final int MENU_INFO = Menu.FIRST + 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences);

		recivedIntent = getIntent();

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// Initialising views
		card = (Spinner) this.findViewById(R.id.fuel);
		dist = (Spinner) this.findViewById(R.id.distance);
		unit = (Spinner) this.findViewById(R.id.unit);
		card_pic = (ImageView) this.findViewById(R.id.fuel_card);
		hour = (ToggleButton) this.findViewById(R.id.hour);
		hgv = (ToggleButton) this.findViewById(R.id.hgv);

		adaptercard = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, card_opt);
		adaptercard
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		card.setAdapter(adaptercard);

		adapterdist = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, dist_opt);
		adapterdist
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		dist.setAdapter(adapterdist);

		adapterunit = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, unit_opt);
		adapterunit
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		unit.setAdapter(adapterunit);

		// Setting view values that are stored in Preferences
		card.setSelection(prefs.getInt("Card", 0));
		dist.setSelection(prefs.getInt("Distance", 0));
		unit.setSelection(prefs.getInt("Unit", 0));
		hour.setChecked(prefs.getBoolean("Hour", false));
		hgv.setChecked(prefs.getBoolean("HGV", false));

		// Implementing Listeners
		card.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				Resources r = getResources();
				int picId = r.getIdentifier(pic_opt[position], "drawable",
						"com.fuelcard");
				System.out.println(picId);
				card_pic.setImageResource(picId);
				SharedPreferences.Editor spe = prefs.edit();
				spe.putInt("Card", position);
				spe.commit();
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		dist.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				SharedPreferences.Editor spe = prefs.edit();
				spe.putInt("Distance", position);
				spe.commit();
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		unit.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				SharedPreferences.Editor spe = prefs.edit();
				spe.putInt("Unit", position);
				spe.commit();
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		hour.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton cb, boolean b) {
				SharedPreferences.Editor spe = prefs.edit();
				spe.putBoolean("Hour", b);
				spe.commit();
			}
		});

		hgv.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton cb, boolean b) {
				SharedPreferences.Editor spe = prefs.edit();
				spe.putBoolean("HGV", b);
				spe.commit();
			}
		});
	}

	// Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// menu.add(Menu.NONE, MENU_SEARCH, 0,
		// "Search").setIcon(android.R.drawable.ic_menu_search);
		menu.add(Menu.NONE, MENU_SEARCH, 0, "Search").setIcon(
				android.R.drawable.ic_menu_search);
		menu.add(Menu.NONE, MENU_INFO, 0, "Info").setIcon(
				android.R.drawable.ic_menu_info_details);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_INFO:
			Intent intent1 = new Intent(Preferences.this, Info.class);
			startActivity(intent1);
			break;
		case MENU_SEARCH:
			if (recivedIntent.getStringExtra("city") != null) {
				finish();
			} else {
				Intent intent = new Intent(Preferences.this, Search.class);
				startActivity(intent);
			}
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
