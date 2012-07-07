package com.fuelcard;

import java.io.IOException;

import com.fuelcard.Utils.TaskProgressListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class FuelCard extends Activity {
	protected static final String TAG = "FuelCard";
	Context context = this;
	boolean reg;
	SharedPreferences prefs = null;
	protected boolean active = true;
	protected int splashTime = 1000;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		initControls();
	}

	private void initControls() {
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		try {
			Utils.downloadAndExtractZip(getAssets().open("fuelcards.zip"), getExternalCacheDir().getAbsolutePath(), downloadTaskProgressListener);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// thread for displaying the SplashScreen
		Thread splashTread = new Thread() {

			@Override
			public void run() {
				try {
					int waited = 0;
					while (active && (waited < splashTime)) {
						sleep(50);
						if (active) {
							waited += 100;
						}

					}
				} catch (InterruptedException e) {
					// do nothing
				} finally {
					finish();
					Intent intentGame = null;
					reg = prefs.getBoolean("Registered", false);
					if (!reg) {
						intentGame = new Intent(FuelCard.this, UserInfo.class);
						SharedPreferences.Editor spe = prefs.edit();
						spe.putBoolean("Registered", true);
						spe.putInt("Card", 0);
						spe.putInt("Distance", 0);
						spe.putInt("Unit", 0);
						spe.putBoolean("Hour", false);
						spe.putBoolean("HGV", false);
						spe.commit();
					} else
						intentGame = new Intent(FuelCard.this, Search.class);
					startActivity(intentGame);
				}
			}
		};
		splashTread.start();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// Ignore orientation change not to restart activity
		super.onConfigurationChanged(newConfig);
	}

	private TaskProgressListener downloadTaskProgressListener = new TaskProgressListener() {

		@Override
		public String taskStarted() {
			Log.d(TAG, "started copying ");
			return null;
		}

		@Override
		public String taskComplete(Object object) {
			Log.d(TAG, "copying-unzipping complete");
			Utils.copyDatabase(getApplicationContext(), copyDatabaseTaskProgressListener);
			return null;
		}

		@Override
		public String taskError(Exception exception) {
			Log.d(TAG, "copying-unzipping error " + exception.getMessage());
			return null;
		}
	};

	private TaskProgressListener copyDatabaseTaskProgressListener = new TaskProgressListener() {

		@Override
		public String taskStarted() {
			Log.d(TAG, "started copying ");
			return null;
		}

		@Override
		public String taskError(Exception exception) {
			Log.d(TAG, "copying error");
			return null;
		}

		@Override
		public String taskComplete(Object object) {
			Log.d(TAG, "copying complete");
			return null;
		}
	};
}
