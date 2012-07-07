package com.fuelcard;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

//database helper class that help creating database from the assets folder 
public class DataBaseHelper extends SQLiteOpenHelper {
	public static SQLiteDatabase db = null;
	public static String DB_PATH = "/data/data/com.fuelcard/databases/";
	public static String DB_NAME = "FuelCard_v2.0.sqlite";
	private final Context myContext;

	public DataBaseHelper(Context context) {
		super(context, DB_NAME, null, 1);
		this.myContext = context;
	}

	public void createDataBase() throws IOException {
		boolean dbExist = checkDataBase();
		if (dbExist) {
		} else {
			this.getReadableDatabase();
			try {
				copyDataBase();
			} catch (IOException e) {
				throw new Error("Error copying database");
			}
		}
	}

	private boolean checkDataBase() {// Check if the database already exist
		SQLiteDatabase checkDB = null;
		try {
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
		} catch (SQLiteException e) {

		}
		if (checkDB != null)
			checkDB.close();
		return checkDB != null ? true : false;
	}

	private void copyDataBase() throws IOException {// Copies your database from
													// your local assets-folder
													// to the just created empty
													// database
		// Open your local db as the input stream
		InputStream myInput = myContext.getAssets().open(DB_NAME);
		// Path to the just created empty db
		String outFileName = DB_PATH + DB_NAME;
		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);
		// transfer bytes from the input file to the output file
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}
		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}

	public static synchronized void openDataBase() throws SQLException {
		// Open the database
		if (db == null || !db.isOpen())
			db = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READONLY);

	}

	public synchronized void closeDatabase() {
		if (db != null)
			db.close();
		// TODO not sure if we can call this here
		super.close();

	}

	public boolean copyDatabaseFrom(InputStream inputStream) throws IOException {
		FileOutputStream outputStream = new FileOutputStream(DB_PATH + DB_NAME);
		byte buffer[] = new byte[1024];
		int length;
		while ((length = inputStream.read(buffer)) > -1)
			outputStream.write(buffer, 0, length);
		outputStream.flush();
		outputStream.close();
		return true;
	}

	public boolean copyDatabaseFrom(String file) throws IOException {
		return copyDatabaseFrom(new FileInputStream(file));
	}

	/**
	 * adds android_metadata table to the database(the downloaded database doesnot have this required table)
	 * 
	 * @return
	 */
	public static boolean addMetaDataTable() {
		openDataBase();
		
		db.rawQuery("CREATE TABLE \"android_metadata\" (\"locale\" TEXT DEFAULT 'en_US')", null);
		db.rawQuery("INSERT INTO \"android_metadata\" VALUES ('en_US')", null);

		return true;

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}
