package com.fuelcard;

import java.io.File;
import java.net.Inet4Address;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.fuelcard.Utils.TaskProgressListener;

public class MyApplication extends Application {

	private static final String TAG = "MyApplication";

	/**
	 * used for local broadcasts
	 */
	public static final String ACTION_DBSYNC_STATUS = "com.fuelcard.action.dbsycnstatus";
	/**
	 * used in local broadcast intents
	 */
	public static final String EXTRA_DBSYNC_RUNNING = "isRunning";

	public AtomicBoolean isDatabaseCopyRunning;

	public String currentVersionString = null;

	@Override
	public void onCreate() {
		isDatabaseCopyRunning = new AtomicBoolean(false);
		new DataBaseHelper(this);
		File databaeFile = new File(DataBaseHelper.DB_PATH);
		if (!databaeFile.exists())
			Utils.copyDatabaseFromAssets(this, copyDatabaseTaskProgressListener);
		else
			Utils.getVersionFromServer(getVersionTaskProgressListener);
	}

	private TaskProgressListener downloadTaskProgressListener = new TaskProgressListener() {

		@Override
		public String taskStarted() {
			isDatabaseCopyRunning.set(true);
			Intent intent = new Intent(ACTION_DBSYNC_STATUS);
			intent.putExtra(EXTRA_DBSYNC_RUNNING, true);
			LocalBroadcastManager.getInstance(MyApplication.this)
					.sendBroadcast(intent);
			
			Log.d(TAG, "started copying ");
			return null;
		}

		@Override
		public String taskComplete(Object object) {
			Log.d(TAG, "copying-unzipping complete");

			// updating the current version in preferences
			SharedPreferences sharedPreferences = getSharedPreferences(
					Prefs.name, MODE_PRIVATE);
			Editor editor = sharedPreferences.edit();
			editor.putString(Prefs.currentVersion, currentVersionString);
			editor.commit();

			// send broadcast
			Intent intent = new Intent(ACTION_DBSYNC_STATUS);
			intent.putExtra(EXTRA_DBSYNC_RUNNING, true);
			LocalBroadcastManager.getInstance(MyApplication.this)
					.sendBroadcast(intent);
			isDatabaseCopyRunning.set(false);
			return null;
		}

		@Override
		public String taskError(Exception exception) {
			Log.d(TAG, "copying-unzipping error " + exception.getMessage());
			isDatabaseCopyRunning.set(false);
			return null;
		}
	};

	private TaskProgressListener copyDatabaseTaskProgressListener = new TaskProgressListener() {

		@Override
		public String taskStarted() {
			isDatabaseCopyRunning.set(true);
			Log.d(TAG, "started copying ");
			return null;
		}

		@Override
		public String taskError(Exception exception) {
			isDatabaseCopyRunning.set(false);
			Log.d(TAG, "copying error");
			return null;
		}

		@Override
		public String taskComplete(Object object) {
			Log.d(TAG, "copying complete");
			isDatabaseCopyRunning.set(false);
			Utils.getVersionFromServer(getVersionTaskProgressListener);
			return null;
		}
	};

	private TaskProgressListener getVersionTaskProgressListener = new TaskProgressListener() {

		@Override
		public String taskStarted() {
			Log.d(TAG, "getVersion running");
			return null;
		}

		@Override
		public String taskError(Exception exception) {
			Log.e(TAG, "getVersion api call failed");
			return null;
		}

		@Override
		public String taskComplete(Object object) {
			if (object != null) {
				currentVersionString = object.toString();
				SharedPreferences prefs = getSharedPreferences(Prefs.name,
						MODE_PRIVATE);
				String lastVersionString = prefs.getString(
						Prefs.currentVersion, null);
				if (lastVersionString == null
						|| !lastVersionString.equals(currentVersionString))
					Utils.downloadAndExtractZip(
							"http://www.businessfuelcards.co.uk/fcuk/"
									+ currentVersionString + ".zip",
							DataBaseHelper.EXTERNAL_DIR,
							downloadTaskProgressListener);
			} else
				Log.e(TAG, "null response received ");
			return "";
		}
	};

	interface Prefs {
		String name = "com.fuelcard.prefs";
		String currentVersion = "currentVersion";

	}

}
