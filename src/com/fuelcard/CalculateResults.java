package com.fuelcard;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.android.maps.GeoPoint;

public class CalculateResults {
	Context context;
	Bundle b;
	private static int EARTH_RADIUS_KM = 6371;
	public static int MILLION = 1000000;
	double lat, lon;
	GeoPoint g1, g2;
	Cursor c, c1;

	SharedPreferences prefs = null;

	int dbCardColumn = 18, fuelcard = 0;
	int distance = 0, hgv = 0, hour = 0;
	String unit;
	private String card_columns[] = { "EuroshellFleetFuelCardMultiNetwork",
			"EuroshellCRTFuelCard", "EuroshellSingleNetwork",
			"EssoFuelCardSingleNetwork", "EssoFuelCardMultiNetwork",
			"EssoTruckCard", "TexacoFastfuelFuelCard", "KeyfuelsFuelCard",
			"RedEurodiesel", "LiquidCard" };
	private String dist_opt[] = { "20", "30", "40", "50", "60" };
	private String unit_opt[] = { "kms", "miles" };

	ArrayList<String> site = new ArrayList<String>();
	ArrayList<String> dist = new ArrayList<String>();
	ArrayList<String> address = new ArrayList<String>();
	ArrayList<Double> latitude = new ArrayList<Double>();
	ArrayList<Double> longitude = new ArrayList<Double>();

	ArrayList<String> site1 = new ArrayList<String>();
	ArrayList<Double> dist1 = new ArrayList<Double>();
	ArrayList<String> address1 = new ArrayList<String>();
	ArrayList<Double> latitude1 = new ArrayList<Double>();
	ArrayList<Double> longitude1 = new ArrayList<Double>();

	double lats[], lons[];
	int ids[];

	static NumberFormat formatter = new DecimalFormat("#0.00");

	public CalculateResults(Context c) {
		context = c;
	}

	public Bundle getResults(double lon, double lat) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		this.lat = lat;
		this.lon = lon;
		try {

			fuelcard = prefs.getInt("Card", 0);
			distance = Integer.parseInt(dist_opt[prefs.getInt("Distance", 0)]);
			unit = unit_opt[prefs.getInt("Unit", 0)];
			hgv = prefs.getBoolean("HGV", false) ? 1 : 0;
			hour = prefs.getBoolean("Hour", false) ? 1 : 0;

			// helper class that creates the database from assets folder
			DataBaseHelper myDbHelper = new DataBaseHelper(context);
			DataBaseHelper.openDataBase();
			
			double latmin = lat - 1.0;
			double latmax = lat + 1.0;
			double lonmin = lon - 1.5;
			double lonmax = lon + 1.5;

			// Cases for 24hour and HGV
			if (hgv == 1 && hour == 1)// both are needed
				c1 = DataBaseHelper.db.query(
						"fuelcard",
						new String[] { "*" },
						new String(
								"CAST(lon as float)>? and  CAST(lon as float)<? and  CAST(lat as float)>? and  CAST(lat as float)<? and "
										+ card_columns[fuelcard]
										+ "=? and twentyfourhour=? and HGV=?"),
						new String[] { String.valueOf(lonmin),
								String.valueOf(lonmax), String.valueOf(latmin),
								String.valueOf(latmax), "1", "1", "1" }, null,
						null, null);
			else if (hgv == 0 && hour == 0)// none is needed
				c1 = DataBaseHelper.db.query(
						"fuelcard",
						new String[] { "*" },
						new String(
								"CAST(lon as float)>? and  CAST(lon as float)<? and  CAST(lat as float)>? and  CAST(lat as float)<? and "
										+ card_columns[fuelcard] + "=?"),
						new String[] { String.valueOf(lonmin),
								String.valueOf(lonmax), String.valueOf(latmin),
								String.valueOf(latmax), "1" }, null, null, null);
			else if (hgv == 0 && hour == 1)// 24hour is needed
				c1 = DataBaseHelper.db.query(
						"fuelcard",
						new String[] { "*" },
						new String(
								"CAST(lon as float)>? and  CAST(lon as float)<? and  CAST(lat as float)>? and  CAST(lat as float)<? and "
										+ card_columns[fuelcard]
										+ "=? and twentyfourhour=?"),
						new String[] { String.valueOf(lonmin),
								String.valueOf(lonmax), String.valueOf(latmin),
								String.valueOf(latmax), "1", "1" }, null, null,
						null);
			else if (hgv == 1 && hour == 0)// hgv is needed
				c1 = DataBaseHelper.db.query(
						"fuelcard",
						new String[] { "*" },
						new String(
								"CAST(lon as float)>? and  CAST(lon as float)<? and  CAST(lat as float)>? and  CAST(lat as float)<? and "
										+ card_columns[fuelcard]
										+ "=? and HGV=?"), new String[] {
								String.valueOf(lonmin), String.valueOf(lonmax),
								String.valueOf(latmin), String.valueOf(latmax),
								"1", "1" }, null, null, null);

			System.out.println("hello................." + c1.getCount() + " "
					+ lon + " " + lat);
			c1.moveToFirst();
			for (int j = 0; j < c1.getCount(); j++) {

				double lat1, lon1, kms, miles;
				lon1 = Double.valueOf(c1.getString(16));
				lat1 = Double.valueOf(c1.getString(17));

				kms = distanceKm(lat, lon, lat1, lon1);
				// kms=Double.valueOf(formatter.format(kms));

				if (unit.equals(unit_opt[0]))// if unit is kms
				{
					if (kms <= distance) {
						// putRecord(String.valueOf(formatter.format(kms))+" kms",lat1,lon1);
						putRecord(kms, lat1, lon1);
					}
				}
				if (unit.equals(unit_opt[1]))// if unit is miles
				{
					miles = km2miles(kms);

					if (miles <= distance) {
						// putRecord(String.valueOf(formatter.format(miles))+" miles",lat1,lon1);
						putRecord(miles, lat1, lon1);
					}
				}

				c1.moveToNext();
			}
			b = new Bundle();
			sortLists(unit);
			b.putStringArrayList("Site", site);
			b.putStringArrayList("Distance", dist);
			b.putStringArrayList("Address", address);
			lats = new double[latitude.size()];
			lons = new double[latitude.size()];
			for (int k = 0; k < latitude.size(); k++) {
				lats[k] = latitude.get(k);
				lons[k] = longitude.get(k);
			}
			b.putDoubleArray("Latitude", lats);
			b.putDoubleArray("Longitude", lons);
			b.putInt("distance", distance);
			b.putInt("unit", prefs.getInt("Unit", 0));
			c1.close();
			if (site.size() > 0)
				return b;
			else
				return null;
		} catch (Exception e) {
			System.out.println(e.toString());
			return null;
		}

	}

	public void sortLists(String unit) {
		double distArr[] = new double[dist1.size()], tempdist;
		ids = new int[dist1.size()];
		int tempids;

		for (int k = 0; k < dist1.size(); k++) {
			distArr[k] = dist1.get(k);
			ids[k] = k;
		}
		for (int i = 0; i < distArr.length; i++) {
			for (int j = 0; j < i; j++) {
				if (distArr[i] < distArr[j]) {
					// swap distance
					tempdist = distArr[i];
					distArr[i] = distArr[j];
					distArr[j] = tempdist;

					// swap ids
					tempids = ids[i];
					ids[i] = ids[j];
					ids[j] = tempids;
				}
			}
		}
		for (int i = 0; i < ids.length; i++) {
			site.add(site1.get(ids[i]));
			address.add(address1.get(ids[i]));
			latitude.add(latitude1.get(ids[i]));
			longitude.add(longitude1.get(ids[i]));
			dist.add(String.valueOf(formatter.format(dist1.get(ids[i]))) + " "
					+ unit);
		}
	}

	public static double distanceKm(double lat1, double lon1, double lat2,
			double lon2) {
		double lat1Rad = Math.toRadians(lat1);
		double lat2Rad = Math.toRadians(lat2);
		double deltaLonRad = Math.toRadians(lon2 - lon1);

		return Math
				.acos(Math.sin(lat1Rad) * Math.sin(lat2Rad) + Math.cos(lat1Rad)
						* Math.cos(lat2Rad) * Math.cos(deltaLonRad))
				* EARTH_RADIUS_KM;
	}

	public static double distanceKm(GeoPoint p1, GeoPoint p2) {
		double lat1 = p1.getLatitudeE6() / (double) MILLION;
		double lon1 = p1.getLongitudeE6() / (double) MILLION;
		double lat2 = p2.getLatitudeE6() / (double) MILLION;
		double lon2 = p2.getLongitudeE6() / (double) MILLION;

		return distanceKm(lat1, lon1, lat2, lon2);
	}

	public static double km2miles(double km) {
		double miles = km * 0.621371192;
		return miles;
	}

	public void putRecord(double dist, double lat1, double lon1) {
		site1.add(c1.getString(2));
		this.dist1.add(dist);
		address1.add(c1.getString(3) + ", " + c1.getString(4) + ", "
				+ c1.getString(5) + ", " + c1.getString(7) + ", \nPH:"
				+ c1.getString(8));
		latitude1.add(lat1);
		longitude1.add(lon1);
		// System.out.println(".........."+c1.getString(2)+" , "+c1.getString(3)+" , "+lat1+" , "+lon1+" , "+dist);
	}
}
