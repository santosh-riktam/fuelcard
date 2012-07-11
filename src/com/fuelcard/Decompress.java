package com.fuelcard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.util.Log;

/**
 * 
 * @author jon
 */
public class Decompress {
	private String _zipFile;
	private String _location;

	public Decompress(String zipFile, String location) {
		_zipFile = zipFile;
		_location = location;

		_dirChecker("");
	}

	public void unzip() {
		try {
			FileInputStream fin = new FileInputStream(_zipFile);
			ZipInputStream zin = new ZipInputStream(fin);
			ZipEntry ze = null;
			while ((ze = zin.getNextEntry()) != null) {
				Log.v("Decompress", "Unzipping " + ze.getName());
				// TODO dont want to unzip sql file. to be removed when zip is
				// modified
				if (ze.getName().endsWith(".sql"))
					continue;
				if (ze.isDirectory()) {
					_dirChecker(ze.getName());
				} else {
					File outFile = new File(_location + ze.getName());
					if (!outFile.getParentFile().exists())
						outFile.getParentFile().mkdirs();

					FileOutputStream fout = new FileOutputStream(outFile);
					for (int c = zin.read(); c != -1; c = zin.read()) {
						fout.write(c);
					}

					zin.closeEntry();
					fout.close();
				}

			}
			zin.close();
		} catch (Exception e) {
			Log.e("Decompress", "unzip", e);
		}

	}

	private void _dirChecker(String dir) {
		File f = new File(_location + dir);

		if (!f.isDirectory()) {
			f.mkdirs();
		}
	}
}
