package com.bender.tidtabell;

import com.bender.tidtabell.QueryRunner.Status;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import coordinatetransformation.positions.RT90Position;
import coordinatetransformation.positions.RT90Position.RT90Projection;
import coordinatetransformation.positions.WGS84Position;

public class StopSearch extends ListActivity
{
	private static final String NUM_SEARCH_RESULT = "5";
	public static final String SEARCH_URL = "http://www.vasttrafik.se/External_Services/"
	        + "TravelPlanner.asmx/GetStopsSuggestions"
	        + "?identifier="
	        + Tidtabell.IDENTIFIER;
	public static final String PROXIMITY_SEARCH_URL = "http://www.vasttrafik.se/External_Services/"
	        + "TravelPlanner.asmx/GetStopListBasedOnCoordinate"
	        + "?identifier=" + Tidtabell.IDENTIFIER;

	public static final int DIALOG_PROGRESS = 0, DIALOG_ERROR = 1;

	private StopListAdapter mListAdapter;
	private Stop[] mStops = new Stop[0];
	private QueryRunner mQueryRunner;
	private boolean mProximitySearch = false;

	private LocationManager mLocationManager;
	private SensorManager mSensorManager;
	private Sensor mAccelSensor;
	private Sensor mMagnSensor;

	private TextView mErrorMargin;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.stop_search);

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelSensor = mSensorManager
		        .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mMagnSensor = mSensorManager
		        .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		mListAdapter = new StopListAdapter(this, mStops);
		setListAdapter(mListAdapter);
		mListAdapter
		        .setOrientation(getResources().getConfiguration().orientation);

		Intent intent = getIntent();

		// If there's already a search thread started or
		if (getLastNonConfigurationInstance() != null)
		{
			mQueryRunner = (QueryRunner) getLastNonConfigurationInstance();
			switch (mQueryRunner.getStatus())
			{
			case RUNNING:
				mQueryRunner.msgHandler = mMsgHandler;
				break;
			case FINISHED:
				StopSearchHandler h = (StopSearchHandler) mQueryRunner
				        .getResult();
				mStops = h.getStops();
				break;
			default:
				break;
			}
		}
		// Search has already been performed
		else if (savedInstanceState != null)
		{
			mStops = (Stop[]) savedInstanceState.getSerializable("stops");
			mListAdapter.updateList(mStops);
		}
		// If the intent was a search intent then perform the search
		else if (Intent.ACTION_SEARCH.equals(intent.getAction()))
		{
			String query = intent.getStringExtra(SearchManager.QUERY);
			// Remove trailing spaces from query
			query.replaceAll("\\s+$", "");
			String url = SEARCH_URL + "&searchString=" + query + "&count="
			        + NUM_SEARCH_RESULT;

			mQueryRunner = new QueryRunner(new StopSearchHandler(),
			        mMsgHandler, url);
			showDialog(DIALOG_PROGRESS);
			new Thread(mQueryRunner).start();
		}
		// Closest stops
		else
		{
			setTitle(R.string.title_closest_stops);
			mProximitySearch = true;
			mErrorMargin = (TextView) findViewById(R.id.error_margin_value);
		}

		// Setup click listeners for all search results in the list
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
			        int position, long id)
			{
				Intent intent = new Intent(StopSearch.this, NextTrip.class);
				Bundle b = new Bundle();
				b.putString("stopId", mStops[(int) id].getId());
				b.putString("stopName", mStops[(int) id].getName());
				b.putSerializable("stop", mStops[(int) id]);
				intent.putExtras(b);
				startActivity(intent);
			}
		});
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
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
		        0, 0, mListAdapter.mLocationListener);
		mLocationManager.requestLocationUpdates(
		        LocationManager.NETWORK_PROVIDER, 0, 0,
		        mListAdapter.mLocationListener);
		mSensorManager.registerListener(mListAdapter.mSensorEventListener,
		        mAccelSensor, SensorManager.SENSOR_DELAY_UI);
		mSensorManager.registerListener(mListAdapter.mSensorEventListener,
		        mMagnSensor, SensorManager.SENSOR_DELAY_UI);

		// Proximity search
		if (mProximitySearch)
		{
			mLocationManager
			        .requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
			                0, 200, mLocationListener);
			mLocationManager.requestLocationUpdates(
			        LocationManager.GPS_PROVIDER, 0, 200, mLocationListener);
		}
	}

	@Override
	public void onPause()
	{
		mLocationManager.removeUpdates(mListAdapter.mLocationListener);
		mSensorManager.unregisterListener(mListAdapter.mSensorEventListener);

		if (mProximitySearch)
			mLocationManager.removeUpdates(mLocationListener);

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
		inflater.inflate(R.menu.search, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_home:
			Intent intent = new Intent(this, Tidtabell.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		case R.id.menu_search:
			onSearchRequested();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance()
	{
		return mQueryRunner;
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle bundle)
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
		case DIALOG_ERROR:
			int errMsg = bundle.getInt("message");
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Error").setMessage(errMsg)
			        .setNeutralButton(R.string.ok, new OnClickListener() {
				        @Override
				        public void onClick(DialogInterface dialog, int which)
				        {
					        dismissDialog(DIALOG_ERROR);
					        finish();
				        }
			        });
			return builder.create();
		default:
			return null;
		}
	}

	// Message handler for the QueryRunner
	private Handler mMsgHandler = new Handler() {
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case QueryRunner.MSG_COMPLETE:
				if (mProximitySearch)
					setProgressBarIndeterminateVisibility(false);
				else
					dismissDialog(DIALOG_PROGRESS);
				StopSearchHandler h = (StopSearchHandler) mQueryRunner
				        .getResult();
				mStops = h.getStops();
				mListAdapter.updateList(mStops);
				break;
			default:
				dismissDialog(DIALOG_PROGRESS);
				Bundle b = new Bundle();
				b.putInt("message", msg.arg1);
				showDialog(DIALOG_ERROR, b);
				break;
			}
		}
	};

	public final LocationListener mLocationListener = new LocationListener() {

		float mLastBestAcc = Float.MAX_VALUE;

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
		}

		@Override
		public void onProviderEnabled(String provider)
		{
		}

		@Override
		public void onProviderDisabled(String provider)
		{
		}

		@Override
		public void onLocationChanged(Location location)
		{
			// Show accuracy
			mErrorMargin.setText(location.getAccuracy() + "m");

			if (location.getAccuracy() <= mLastBestAcc / 2
			        && (mQueryRunner == null || mQueryRunner.getStatus() == Status.FINISHED))
			{
				mLastBestAcc = location.getAccuracy();

				WGS84Position wgs84 = new WGS84Position(
				        (double) location.getLatitude(),
				        (double) location.getLongitude());
				RT90Position rt90 = new RT90Position(wgs84,
				        RT90Projection.rt90_2_5_gon_v);

				String x = Double.toString(rt90.getLatitude());
				String y = Double.toString(rt90.getLongitude());

				String url = PROXIMITY_SEARCH_URL + "&xCoord=" + x + "&yCoord="
				        + y;

				mQueryRunner = new QueryRunner(new StopSearchHandler(),
				        mMsgHandler, url);
				setProgressBarIndeterminateVisibility(true);
				new Thread(mQueryRunner).start();
			}
		}
	};
}
