package com.fuelcard;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

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

	/**
	 * copies database in assets folder to sdcard
	 * 
	 * @param context
	 * @param listener
	 */
	public static void copyDatabaseFromAssets(Context context,
			TaskProgressListener listener) {
		// new CopyDatabaseTask(listener,
		// context).execute(context.getExternalCacheDir().getAbsolutePath()+ "/"
		// + ZIP_FOLDER_NAME + "/" + DataBaseHelper.DB_NAME);
		new CopyDatabaseTask(listener, context).execute("");
	}

	/**
	 * makes getVersion soap call on the server
	 * 
	 * @param taskProgressListener
	 */
	public static void getVersionFromServer(
			TaskProgressListener taskProgressListener) {
		new GetVersionTask(taskProgressListener).execute("");
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

	/**
	 * AsyncTask for making soap request to get the version number
	 * 
	 * @author Santosh Kumar D
	 * 
	 */

	private static class GetVersionTask extends
			AsyncTask<Object, Object, Object> {

		WeakReference<TaskProgressListener> listenerReference;
		private static final String URL = "http://sites.fuelcarddirect.co.uk/siteservice.asmx";
		/**
		 * passed as parameter to web services
		 */
		private static final String WEB_ACCESS_KEY = "1A564B81-A426-4BB9-9584-E73E86D74797";

		public GetVersionTask(TaskProgressListener listener) {
			listenerReference = new WeakReference<Utils.TaskProgressListener>(
					listener);
		}

		@Override
		protected Object doInBackground(Object... params) {
			if (listenerReference.get() != null) {
				listenerReference.get().taskStarted();

				HttpPost post = new HttpPost(URL);
				// adding headers for soap request
				post.addHeader("Host", " sites.fuelcarddirect.co.uk");
				post.addHeader("SOAPAction", "http://tempuri.org/GetVersion");
				post.addHeader("Content-Type", "text/xml; charset=utf-8");
				try {
					post.setEntity(new StringEntity(
							"<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><GetVersion xmlns=\"http://tempuri.org/\"><WebAccessKey>"
									+ WEB_ACCESS_KEY
									+ "</WebAccessKey></GetVersion></soap:Body></soap:Envelope>"));
					HttpResponse httpResponse = new DefaultHttpClient()
							.execute(post);
					String response = EntityUtils.toString(httpResponse
							.getEntity());
					response = response.substring(
							response.indexOf("<GetVersionResult>") + 18,
							response.indexOf("</GetVersionResult>"));
					JSONObject jsonObject = new JSONObject(response);
					String versionStamp = jsonObject.getString("VersionStamp");
					return versionStamp;
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			return new Exception("soap request failed");
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if (listenerReference.get() != null) {
				if (result instanceof Exception)
					listenerReference.get().taskError((Exception) result);
				else if (result instanceof String)
					listenerReference.get().taskComplete(result);
			} else {
				Log.d(TAG, "reference garbage collected");
			}

		}
	}

	private static class DownloadUnzipTask extends
			AsyncTask<Object, Object, Object> {

		WeakReference<TaskProgressListener> listenerReference;
		InputStream inputStream;
		String outputFileString;

		public DownloadUnzipTask(TaskProgressListener listener,
				InputStream inputStream, String outputFile) {
			listenerReference = new WeakReference<Utils.TaskProgressListener>(
					listener);
			this.inputStream = inputStream;
			this.outputFileString = outputFile;
		}

		@Override
		protected Object doInBackground(Object... params) {

			if (listenerReference.get() != null) {
				listenerReference.get().taskStarted();
				try {
					downloadFile();
					Decompress decompress = new Decompress(outputFileString,
							outputFileString.substring(0,
									outputFileString.lastIndexOf("/") + 1));
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

			File outputFile = new File(outputFileString);
			outputFile.getParentFile().mkdirs();
			outputFile.createNewFile();

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
		/**
		 * called when task starts - usually from background
		 * 
		 * @return
		 */
		String taskStarted();

		/**
		 * called when the background task completes
		 * 
		 * @param object
		 * @return
		 */
		String taskComplete(Object object);

		/**
		 * called when and error occurs during task execution
		 * 
		 * @param exception
		 * @return
		 */
		String taskError(Exception exception);
	}
}
