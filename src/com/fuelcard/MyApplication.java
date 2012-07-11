package com.fuelcard;

import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Application;
import android.util.Log;

import com.fuelcard.Utils.TaskProgressListener;

public class MyApplication extends Application {

	protected static final String TAG = "MyApplication";

	public AtomicBoolean isDatabaseSyncRunning;

	@Override
	public void onCreate() {
		isDatabaseSyncRunning = new AtomicBoolean(false);
		new DataBaseHelper(this);

		Utils.downloadAndExtractZip(
				"http://www.businessfuelcards.co.uk/fcuk/3D51179D-AC40-474F-8355-8CEEC742C2AB.zip",
				DataBaseHelper.EXTERNAL_DIR, downloadTaskProgressListener);
	}

	private TaskProgressListener downloadTaskProgressListener = new TaskProgressListener() {

		@Override
		public String taskStarted() {
			isDatabaseSyncRunning.set(true);
			Log.d(TAG, "started copying ");
			return null;
		}

		@Override
		public String taskComplete(Object object) {
			isDatabaseSyncRunning.set(false);
			Log.d(TAG, "copying-unzipping complete");

			return null;
		}

		@Override
		public String taskError(Exception exception) {
			isDatabaseSyncRunning.set(false);
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
			DataBaseHelper.openDataBase();
			Log.d(TAG, "database object " + DataBaseHelper.db);
			return null;
		}
	};

}
