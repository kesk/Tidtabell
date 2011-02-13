package com.bender.tidtabell;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOpenHelper extends SQLiteOpenHelper
{
	private static final int VERSION = 1;
	public static final String 
		DATABASE_NAME = "tidtabell",
		TABLE_NAME = "stations",
		STATION_ID = "station_id",
		STATION_NAME = "station_name",
		FRIENDLY_NAME = "friendly_name",
		COUNTY = "county",
		LONGITUDE = "longitude",
		LATITUDE = "latitude";

	public DatabaseOpenHelper(Context context)
	{
		super(context, DATABASE_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
		        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
		        STATION_ID +" TEXT," +
		        STATION_NAME + " TEXT," +
		        FRIENDLY_NAME + " TEXT," +
		        COUNTY + " TEXT," +
		       	LATITUDE + " REAL," +
		       	LONGITUDE + " REAL);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{

	}

	// Add a stop to favorites
	public void addFavorite(Stop stop)
	{
		SQLiteDatabase db = getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(STATION_ID, stop.getId());
		values.put(STATION_NAME, stop.getName());
		values.put(FRIENDLY_NAME, stop.getFriendlyName());
		values.put(COUNTY, stop.getCounty());
		values.put(LATITUDE, stop.getLatitude());
		values.put(LONGITUDE, stop.getLongitude());

		db.insert(TABLE_NAME, null, values);
		db.close();
	}

	public void removeFavorite(Stop stop)
	{
		SQLiteDatabase db = getWritableDatabase();
		String selection = STATION_ID + " = ?";
		String[] args = { stop.getId() };
		db.delete(TABLE_NAME, selection, args);
		db.close();
	}

	public Cursor getFavorite(String stopId)
	{
		SQLiteDatabase db = getReadableDatabase();
		String selection = STATION_ID + " = ?";
		String[] args = { stopId };

		return db
		        .query(TABLE_NAME, null, selection, args, null, null, "id ASC");
	}

	public Cursor getAllFavourites()
	{
		SQLiteDatabase db = getReadableDatabase();

		return db.query(TABLE_NAME, null, null, null, null, null, "id ASC");
	}
	
	public static Stop[] mkStopArray(Cursor cursor)
	{
		// True if the cursor contains data
		if (cursor.moveToFirst())
		{
			Stop[] stops = new Stop[cursor.getCount()];
			int i = 0;
			do
			{
				Stop stop = new Stop();
				String id = cursor.getString(cursor
				        .getColumnIndex(DatabaseOpenHelper.STATION_ID));
				stop.setId(id);

				String name = cursor.getString(cursor
				        .getColumnIndex(DatabaseOpenHelper.STATION_NAME));
				stop.setName(name);

				String friendlyName = cursor.getString(cursor
				        .getColumnIndex(DatabaseOpenHelper.FRIENDLY_NAME));
				stop.setFriendlyName(friendlyName);

				String county = cursor.getString(cursor
				        .getColumnIndex(DatabaseOpenHelper.COUNTY));
				stop.setCounty(county);

				float latitude = cursor.getFloat(cursor
				        .getColumnIndex(DatabaseOpenHelper.LATITUDE));
				stop.setLatitude(latitude);

				float longitude = cursor.getFloat(cursor
				        .getColumnIndex(DatabaseOpenHelper.LONGITUDE));
				stop.setLongitude(longitude);

				stops[i] = stop;
				i++;
			} while (cursor.moveToNext());

			return stops;
		}
		// Return empty array if there are no saved stops
		else
			return new Stop[0];
	}
}
