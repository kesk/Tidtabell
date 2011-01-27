package com.bender.tidtabell;

import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOpenHelper extends SQLiteOpenHelper
{
	private static final int VERSION = 1;
	private static final String DATABASE_NAME = "tidtabell";
	private static final String TABLE_NAME = "stations";
	private static final String STATION_ID = "station_id";
	private static final String STATION_NAME = "station_name";

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
	public void addFavoriteStop(Stop stop)
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

	public boolean isFavorite(Stop stop)
	{
		SQLiteDatabase db = getReadableDatabase();
		String[] columns = { STATION_ID };
		String selection = STATION_ID + " = ?";
		String[] args = { stop.getId() };
		Cursor cursor = db.query(TABLE_NAME, columns, selection, args, null,
		        null, null);

		return cursor.moveToFirst();
	}

	public Vector<Stop> getFavouriteStops()
	{
		SQLiteDatabase db = getReadableDatabase();

		Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null,
		        "id ASC");
		Vector<Stop> result = new Vector<Stop>();

		if (cursor.moveToFirst())
		{
			result = new Vector<Stop>();

			do
			{
				Stop stop = new Stop();
				String id = cursor.getString(cursor.getColumnIndex(STATION_ID));
				stop.setId(id);

				String name = cursor.getString(cursor
				        .getColumnIndex(STATION_NAME));
				stop.setName(name);

				result.add(stop);
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();

		return result;
	}
}
