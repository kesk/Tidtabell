package com.bender.tidtabell;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
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
	Cursor mFavoriteCursor;
	Stop[] mStops;
	StopListAdapter mListAdapter;

	LocationManager mLocationManager;
	SensorManager mSensorManager;
	Sensor mAccelSensor;
	Sensor mMagnSensor;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelSensor = mSensorManager
		        .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mMagnSensor = mSensorManager
		        .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		mDb = new DatabaseOpenHelper(this);
		mFavoriteCursor = mDb.getAllFavourites();
		mStops = DatabaseOpenHelper.mkStopArray(mFavoriteCursor);

		mListAdapter = new StopListAdapter(this, mStops);
		setListAdapter(mListAdapter);
		mListAdapter.setOrientation(getResources().getConfiguration().orientation);

		// Click listener for each favorite stop
		ListView listView = getListView();
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
			        int position, long id)
			{
				Intent intent = new Intent(Tidtabell.this, NextTrip.class);
				Bundle b = new Bundle();
				b.putSerializable("stop", mStops[(int) id]);
				intent.putExtras(b);
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onRestart()
	{
		super.onRestart();
		mFavoriteCursor.requery();
		mStops = DatabaseOpenHelper.mkStopArray(mFavoriteCursor);
		mListAdapter.updateList(mStops);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		mLocationManager.requestLocationUpdates(
		        LocationManager.GPS_PROVIDER, 0, 0,
		        mListAdapter.mLocationListener);
		mLocationManager.requestLocationUpdates(
		        LocationManager.NETWORK_PROVIDER, 0, 0,
		        mListAdapter.mLocationListener);
		mSensorManager.registerListener(mListAdapter.mSensorListener,
		        mAccelSensor, SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(mListAdapter.mSensorListener,
		        mMagnSensor, SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	protected void onPause()
	{
		mLocationManager.removeUpdates(mListAdapter.mLocationListener);
		mSensorManager.unregisterListener(mListAdapter.mSensorListener);
		super.onPause();
	}

	@Override
	protected void onStop()
	{
		mFavoriteCursor.deactivate();
		super.onStop();
	}

	@Override
	protected void onDestroy()
	{
		mDb.close();
		super.onDestroy();
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
		switch (item.getItemId())
		{
		case R.id.menu_search:
			onSearchRequested();
			return true;
		case R.id.menu_proximity_search:
			Intent i = new Intent(this, StopSearch.class);
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
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
		NodeList items = root.getElementsByTagName("string").item(0)
		        .getChildNodes();
		for (int i = 0; i < items.getLength(); i++)
		{
			Node node = items.item(i);
			sb.append(node.getNodeValue());
		}

		String s = sb.toString();
		return s;
	}
}