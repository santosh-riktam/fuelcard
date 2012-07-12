package com.fuelcard;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Provides functionality common to most activities
 * 
 * @author Santosh Kumar D
 * 
 */
public class BaseActivity extends Activity {

	private ProgressDialog progressDialog;
	protected DBSyncStatusReceiver dbSyncStatusReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dbSyncStatusReceiver = new DBSyncStatusReceiver();
		IntentFilter intentFilter = new IntentFilter(
				MyApplication.ACTION_DBSYNC_STATUS);
		LocalBroadcastManager.getInstance(this).registerReceiver(
				dbSyncStatusReceiver, intentFilter);

	}

	/**
	 * called when db sync starts
	 */
	protected void onDbSyncStart() {
		progressDialog = ProgressDialog.show(this, null,
				"database sync in progress");

	}

	/**
	 * called when database sync ends
	 */
	protected void onDbSyncEnd() {
		progressDialog.dismiss();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				dbSyncStatusReceiver);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	class DBSyncStatusReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean isDbSyncRunning = intent.getBooleanExtra(
					MyApplication.EXTRA_DBSYNC_RUNNING, false);
			if (isDbSyncRunning)
				onDbSyncStart();
			else
				onDbSyncEnd();
		}
	}

}
