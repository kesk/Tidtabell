package com.bender.tidtabell;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
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
	Vector<Stop> mStops;
	StopListAdapter mListAdapter;

	LocationManager mLocationManager;
	SensorManager mSensorManager;
	Sensor mSensor;
	float[] mOrientation = {0,0,0};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

		mDb = new DatabaseOpenHelper(this);
		mFavoriteStops = mDb.getAllFavourites();
		mStops = mkStopList(mFavoriteStops);
		mListAdapter = new StopListAdapter(this, mStops);
		setListAdapter(mListAdapter);

		ListView listView = getListView();
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
	            Intent intent = new Intent(Tidtabell.this, NextTrip.class);
	            Bundle b = new Bundle();
	            b.putSerializable("stop", mStops.get((int) id));
	            intent.putExtras(b);
	            startActivity(intent);
            }
		});
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mListAdapter.mLocationListener);
		mSensorManager.registerListener(mListAdapter.mSensorEventListener, mSensor,
                SensorManager.SENSOR_DELAY_UI);
	}
	
	@Override
	public void onPause()
	{
		mLocationManager.removeUpdates(mListAdapter.mLocationListener);
		mSensorManager.unregisterListener(mListAdapter.mSensorEventListener);
		super.onPause();
	}
	
	@Override
	protected void onRestart()
	{
		super.onRestart();
		mFavoriteStops.requery();
		mStops = mkStopList(mFavoriteStops);
		mListAdapter.updateList(mStops);
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
		Document dom = db.parse(is);
		
		StringBuilder sb = new StringBuilder();
		Element root = dom.getDocumentElement();
		NodeList items = root.getElementsByTagName("string").item(0).getChildNodes();
		for (int i=0; i<items.getLength(); i++)
		{
			Node node = items.item(i);
			sb.append(node.getNodeValue());
		}
		
		return sb.toString();
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