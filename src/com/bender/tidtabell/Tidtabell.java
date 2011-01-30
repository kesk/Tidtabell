package com.bender.tidtabell;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class Tidtabell extends ListActivity
{
	public static final String IDENTIFIER = "1d1b034c-b4cc-49ec-a69e-70b91f5fb325";
	
	DatabaseOpenHelper mDb;
	Cursor mFavoriteStops;
	StopListAdapter mListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mDb = new DatabaseOpenHelper(this);
		mFavoriteStops = mDb.getAllFavourites();
		final Vector<Stop> stops = mkStopList(mFavoriteStops);
		mListAdapter = new StopListAdapter(this, stops);
		setListAdapter(mListAdapter);
		
		ListView listView = getListView();
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
	            Intent intent = new Intent(Tidtabell.this, NextTrip.class);
	            Bundle b = new Bundle();
	            b.putSerializable("stop", stops.get((int) id));
	            intent.putExtras(b);
	            startActivity(intent);
            }
		});
	}
	
	@Override
	protected void onRestart()
	{
		super.onRestart();
		mFavoriteStops.requery();
		Vector<Stop> stops = mkStopList(mFavoriteStops);
		mListAdapter.updateList(stops);
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
		mFavoriteStops.deactivate();
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		mFavoriteStops.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.tidtabell, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return menuItemSelectHandler(this, item)
		        || super.onOptionsItemSelected(item);
	}

	public static boolean menuItemSelectHandler(Activity activity, MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_search:
			activity.onSearchRequested();
			return true;
		default:
			return false;
		}
	}

	// Takes the XML that Västtrafik returns and extracts the XML stored as a
	// string inside the XML (ARGH!)
	protected static String getXmlData(InputStream is)
	        throws ParserConfigurationException, IOException, SAXException
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(is);

		return doc.getDocumentElement().getTextContent();
	}
	
	private Vector<Stop> mkStopList(Cursor cursor)
	{
		Vector<Stop> stops = new Vector<Stop>();

		if (cursor.moveToFirst())
		{
			stops = new Vector<Stop>();

			do
			{
				Stop stop = new Stop();
				String id = cursor.getString(cursor.getColumnIndex(DatabaseOpenHelper.STATION_ID));
				stop.setId(id);

				String name = cursor.getString(cursor
				        .getColumnIndex(DatabaseOpenHelper.STATION_NAME));
				stop.setName(name);

				stops.add(stop);
			} while (cursor.moveToNext());
		}
		
		return stops;
	}
}