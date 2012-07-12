package com.fuelcard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//database helper class that help creating database from the assets folder 
public class DataBaseHelper extends SQLiteOpenHelper {
	public static SQLiteDatabase db = null;
	public static String DB_PATH = "";
	public static String DB_NAME = "FuelCard.sqlite";
	public static String ASSETS_PACKAGE_NAME = "fuelcards.zip";
	public static String EXTERNAL_DIR = "";
	private final Context myContext;

	public DataBaseHelper(Context context) {
		super(context, DB_NAME, null, 1);
		this.myContext = context;
		EXTERNAL_DIR = context.getExternalCacheDir().getAbsolutePath();
		DB_PATH = EXTERNAL_DIR + "/db/" + DB_NAME;
	}

	public static synchronized void openDataBase() throws SQLException {
		// close db if already opened
		closeDatabase();

		// Open the database
		if (db == null || !db.isOpen())
			db = SQLiteDatabase.openDatabase(DB_PATH, null,
					SQLiteDatabase.OPEN_READWRITE
							| SQLiteDatabase.NO_LOCALIZED_COLLATORS);

	}

	public static synchronized void closeDatabase() {
		if (db != null && db.isOpen()) {
			db.close();
			db = null;
		}

	}

	/**
	 * copies and extracts zip package in assets to external directory
	 * 
	 * @param context
	 * @return
	 * @throws IOException
	 */

	public boolean copyDatabaseFromAssets(Context context) throws IOException {
		File outputFile = new File(EXTERNAL_DIR + "/" + ASSETS_PACKAGE_NAME);
		if (!outputFile.getParentFile().exists())
			outputFile.getParentFile().mkdirs();

		FileOutputStream outputStream = new FileOutputStream(outputFile);

		byte buffer[] = new byte[1024];
		InputStream inputStream = context.getAssets().open(ASSETS_PACKAGE_NAME);
		int length;
		while ((length = inputStream.read(buffer)) > -1)
			outputStream.write(buffer, 0, length);
		outputStream.flush();
		outputStream.close();
		Decompress decompress = new Decompress(outputFile.getAbsolutePath(),
				outputFile.getParent());
		decompress.unzip();

		return true;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}
