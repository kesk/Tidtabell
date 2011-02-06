package com.bender.tidtabell;

import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import coordinatetransformation.Position;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class StopSearch extends ListActivity
{
	private static final String NUM_SEARCH_RESULT = "5";
	public static final String SEARCH_URL = "http://www.vasttrafik.se/External_Services/TravelPlanner.asmx/GetStopsSuggestions?identifier="
	        + Tidtabell.IDENTIFIER;

	public static final int DIALOG_PROGRESS = 0;

	private StopListAdapter mListAdapter;
	private Vector<Stop> mStops = new Vector<Stop>();
	private QueryRunner mQueryRunner;

	LocationManager mLocationManager;
	SensorManager mSensorManager;
	Sensor mSensor;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mListAdapter = new StopListAdapter(this, mStops);
		setListAdapter(mListAdapter);

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

		Intent intent = getIntent();

		// Setup click listeners for all search results in the list
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
			        int position, long id)
			{
				Intent intent = new Intent(StopSearch.this, NextTrip.class);
				Bundle b = new Bundle();
				b.putString("stopId", mStops.get((int) id).getId());
				b.putString("stopName", mStops.get((int) id).getName());
				b.putSerializable("stop", mStops.get((int) id));
				intent.putExtras(b);
				startActivity(intent);
			}
		});

		// There is already a search thread started
		if (getLastNonConfigurationInstance() != null)
		{
			mQueryRunner = (QueryRunner) getLastNonConfigurationInstance();
			mQueryRunner.setMsgHandler(mMsgHandler);
		}
		// Search has already been performed
		else if (savedInstanceState != null)
		{
			mStops = (Vector<Stop>) savedInstanceState.getSerializable("stops");
			mListAdapter.updateList(mStops);
		}
		// If the intent was a search intent then perform the search
		else if (Intent.ACTION_SEARCH.equals(intent.getAction()))
		{
			String query = intent.getStringExtra(SearchManager.QUERY);
			String url = SEARCH_URL + "&searchString=" + query + "&count="
			        + NUM_SEARCH_RESULT;

			mQueryRunner = new QueryRunner(new StopSearchHandler(),
			        mMsgHandler, url);
			showDialog(DIALOG_PROGRESS);
			new Thread(mQueryRunner).start();
		}
	}

	// The activity is sent a new search to handle
	@Override
	protected void onNewIntent(Intent intent)
	{
		if (Intent.ACTION_SEARCH.equals(intent.getAction()))
		{
			String query = intent.getStringExtra(SearchManager.QUERY);
			String url = SEARCH_URL + "&searchString=" + query + "&count="
			        + NUM_SEARCH_RESULT;

			if (mQueryRunner != null)
				mQueryRunner.stopThread();

			mQueryRunner = new QueryRunner(new StopSearchHandler(),
			        mMsgHandler, url);
			showDialog(DIALOG_PROGRESS);
			new Thread(mQueryRunner).start();
		}
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		//mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mListAdapter.mLocationListener);
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
	public void onSaveInstanceState(Bundle outState)
	{
		if (mStops != null)
			outState.putSerializable("stops", mStops);

		super.onSaveInstanceState(outState);
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
		return Tidtabell.menuItemSelectHandler(this, item)
		        || super.onOptionsItemSelected(item);
	}

	@Override
	public Object onRetainNonConfigurationInstance()
	{
		if (mQueryRunner == null
		        || mQueryRunner.getStatus() == QueryRunner.STATE_RUNNNING)
			return mQueryRunner;
		else
			return null;
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{
		ProgressDialog dialog;

		switch (id)
		{
		case DIALOG_PROGRESS:
			dialog = new ProgressDialog(this);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setMessage(getString(R.string.loading_dialog));
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog)
				{
					mQueryRunner.stopThread();
					finish();
				}
			});
			return dialog;
		default:
			return null;
		}
	}

	private Handler mMsgHandler = new Handler() {
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case QueryRunner.MESSAGE_COMPLETE:
				dismissDialog(DIALOG_PROGRESS);
				StopSearchHandler h = (StopSearchHandler) mQueryRunner
				        .getResult();
				mStops = h.getStops();
				mListAdapter.updateList(mStops);
				break;
			default:
				break;
			}
		}
	};
}
