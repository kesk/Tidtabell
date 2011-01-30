package com.bender.tidtabell;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOpenHelper extends SQLiteOpenHelper
{
	private static final int VERSION = 1;
	public static final String DATABASE_NAME = "tidtabell";
	public static final String TABLE_NAME = "stations";
	public static final String STATION_ID = "station_id";
	public static final String STATION_NAME = "station_name";

	public DatabaseOpenHelper(Context context)
	{
		super(context, DATABASE_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
		        + "id INTEGER PRIMARY KEY AUTOINCREMENT, " + STATION_ID
		        + " TEXT, " + STATION_NAME + " TEXT);");
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

		db.insert(TABLE_NAME, null, values);
		db.close();
	}

	public void removeFavorite(Stop stop)
	{
		SQLiteDatabase db = getWritableDatabase();
		String selection = STATION_ID + " = ?";
		String[] args = { stop.id };
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
}
