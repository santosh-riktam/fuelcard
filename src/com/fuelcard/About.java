package com.fuelcard;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ScrollView;
import android.widget.TextView;

public class About extends Activity {
	// menu items
	private static final int MENU_SEARCH = Menu.FIRST;
	private static final int MENU_PREF = Menu.FIRST + 1;
	// private static final int MENU_INFO = Menu.FIRST + 1;
	private TextView txt;
	private ScrollView scrollView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		txt = (TextView) this.findViewById(R.id.txt);
		Linkify.addLinks(txt, 1);
		scrollView = (ScrollView) findViewById(R.id.sv);
		scrollView.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
						double paddingTop = scrollView.getMeasuredHeight() *0.6;
						scrollView.getViewTreeObserver()
								.removeGlobalOnLayoutListener(this);
						Log.d("ABOUT", "global layout listener called");

						// setting top padding to half of height+10 dp
						scrollView.setPadding(scrollView.getPaddingLeft(),
								(int) paddingTop, scrollView.getPaddingRight(),
								scrollView.getPaddingBottom());
					}
				});
	}

	Runnable adjustPaddingRunnable = new Runnable() {

		@Override
		public void run() {
			scrollView
					.setPadding(scrollView.getPaddingLeft(),
							scrollView.getLayoutParams().height / 2,
							scrollView.getPaddingRight(),
							scrollView.getPaddingBottom());
		}
	};

	// Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// menu.add(Menu.NONE, MENU_SEARCH, 0,
		// "Search").setIcon(android.R.drawable.ic_menu_search);
		menu.add(Menu.NONE, MENU_SEARCH, 0, "Search").setIcon(
				android.R.drawable.ic_menu_search);
		menu.add(Menu.NONE, MENU_PREF, 0, "Preferences").setIcon(
				android.R.drawable.ic_menu_preferences);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_PREF:
			Intent intent1 = new Intent(About.this, Preferences.class);
			startActivity(intent1);
			break;
		case MENU_SEARCH:
			Intent intent = new Intent(About.this, Search.class);
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
