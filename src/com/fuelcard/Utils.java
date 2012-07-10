package com.fuelcard;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class Utils {

	private static final String ZIP_FILE_NAME = "fuelcards.zip";
	public static final String TAG = "Utils";
	public static String ZIP_FOLDER_NAME = "fuelcards";

	public static void downloadAndExtractZip(String urlString,
			String outputDirectory, TaskProgressListener listener) {
		try {
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();
			connection.connect();
			// download the file
			new DownloadUnzipTask(listener, url.openStream(), outputDirectory
					+ "/" + ZIP_FILE_NAME).execute("");
		} catch (Exception e) {
			e.printStackTrace();
			listener.taskError(e);
		}
	}

	public static void downloadAndExtractZip(InputStream inputStream,
			String outputDirectory, TaskProgressListener listener) {
		new DownloadUnzipTask(listener, inputStream, outputDirectory + "/"
				+ ZIP_FILE_NAME).execute("");
	}

	public static void copyDatabaseFromAssets(Context context,
			TaskProgressListener listener) {
		// new CopyDatabaseTask(listener,
		// context).execute(context.getExternalCacheDir().getAbsolutePath()+ "/"
		// + ZIP_FOLDER_NAME + "/" + DataBaseHelper.DB_NAME);
		new CopyDatabaseTask(listener, context).execute("");
	}

	private static class CopyDatabaseTask extends
			AsyncTask<String, Object, Object> {

		WeakReference<TaskProgressListener> listenerReference;
		Context context;

		public CopyDatabaseTask(TaskProgressListener listener, Context context) {
			listenerReference = new WeakReference<Utils.TaskProgressListener>(
					listener);
			this.context = context;
		}

		@Override
		protected Object doInBackground(String... params) {
			if (listenerReference.get() != null) {
				listenerReference.get().taskStarted();
				DataBaseHelper dataBaseHelper = new DataBaseHelper(context);
				try {
					dataBaseHelper.copyDatabaseFromAssets(context);
				} catch (IOException e) {
					e.printStackTrace();
					return e;
				}
			}
			return true;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if (listenerReference.get() != null) {
				if (result instanceof Exception)
					listenerReference.get().taskError((Exception) result);
				else if (result instanceof Boolean)
					listenerReference.get().taskComplete(result);

			}

		}
	}

	private static class DownloadUnzipTask extends
			AsyncTask<Object, Object, Object> {

		WeakReference<TaskProgressListener> listenerReference;
		InputStream inputStream;
		String outputFile;

		public DownloadUnzipTask(TaskProgressListener listener,
				InputStream inputStream, String outputFile) {
			listenerReference = new WeakReference<Utils.TaskProgressListener>(
					listener);
			this.inputStream = inputStream;
			this.outputFile = outputFile;
		}

		@Override
		protected Object doInBackground(Object... params) {
			if (listenerReference.get() != null) {
				listenerReference.get().taskStarted();
				try {
					downloadFile();
					Decompress decompress = new Decompress(outputFile,
							outputFile.substring(0,
									outputFile.lastIndexOf("/") + 1));
					decompress.unzip();
				} catch (Exception e) {
					e.printStackTrace();
					return e;
				}
			}
			return false;
		}

		private void downloadFile() throws MalformedURLException, IOException,
				FileNotFoundException {
			InputStream input = new BufferedInputStream(inputStream);
			OutputStream output = new FileOutputStream(outputFile);

			byte data[] = new byte[1024];
			int count;
			while ((count = input.read(data)) != -1)
				output.write(data, 0, count);

			output.flush();
			output.close();
			input.close();
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if (listenerReference.get() != null) {
				if (result instanceof Exception)
					listenerReference.get().taskError((Exception) result);
				else if (result instanceof Boolean)
					listenerReference.get().taskComplete(result);
			} else {
				Log.d(TAG, "reference garbage collected");
			}

			// Testing
			DataBaseHelper.openDataBase();
			Log.d(TAG, "database object " + DataBaseHelper.db);

		}
	}

	public interface TaskProgressListener {
		String taskStarted();

		String taskComplete(Object object);

		String taskError(Exception exception);
	}
}
