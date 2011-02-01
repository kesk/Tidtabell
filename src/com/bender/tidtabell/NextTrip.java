package com.bender.tidtabell;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.database.CursorJoiner.Result;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class NextTrip extends ListActivity
{
	public static final int DIALOG_PROGRESS = 0;
	public static final String NEXT_TRIP_URL = "http://vasttrafik.se/External_Services/NextTrip.asmx/GetForecast?identifier="
	        + Tidtabell.IDENTIFIER;

	private ForecastQuery mForecastQuery;

	private DatabaseOpenHelper mDb;
	private DepartureListAdapter mListAdapter;
	private Stop mStop;
	private Vector<Departure> mDepartures = null;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (getLastNonConfigurationInstance() != null)
		{
			mForecastQuery = (ForecastQuery) getLastNonConfigurationInstance();
			mForecastQuery.setHandler(mHandler);
		}

		mDb = new DatabaseOpenHelper(this);

		// If this is a restored activity show old departures
		if (savedInstanceState != null)
		{
			mDepartures = (Vector<Departure>) savedInstanceState
			        .getSerializable("departures");
		}

		mListAdapter = new DepartureListAdapter(this);
		setListAdapter(mListAdapter);
		setContentView(R.layout.next_trip);

		Bundle b = getIntent().getExtras();
		if (b != null)
		{
			mStop = (Stop) b.getSerializable("stop");
		}

		// Abort if there is no stop to show
		if (mStop == null)
			return;

		TextView tv = (TextView) findViewById(R.id.stop_name);
		tv.setText(mStop.getName());

		final ToggleButton favToggle = (ToggleButton) findViewById(R.id.favorite_button);
		Cursor favoriteCursor = mDb.getFavorite(mStop.getId());

		// If this station is a favorite
		if (favoriteCursor.moveToFirst())
		{
			// Set star to checked
			favToggle.setChecked(true);
		}
		favoriteCursor.close();

		// Click listener for favorite toggle
		favToggle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				DatabaseOpenHelper db = new DatabaseOpenHelper(NextTrip.this);

				if (favToggle.isChecked())
				{
					Toast.makeText(NextTrip.this, R.string.add_favorite_toast,
					        Toast.LENGTH_SHORT).show();
					db.addFavorite(mStop);
				}
				else
				{
					Toast.makeText(NextTrip.this,
					        R.string.remove_favorite_toast, Toast.LENGTH_SHORT)
					        .show();
					db.removeFavorite(mStop);
				}
			}
		});

		if (mDepartures == null)
		{
			String url = NEXT_TRIP_URL + "&stopId=" + mStop.getId();

			if (savedInstanceState == null
			        && getLastNonConfigurationInstance() == null)
			{
				mForecastQuery = new ForecastQuery(mHandler, url);
				showDialog(DIALOG_PROGRESS, null);
				new Thread(mForecastQuery).start();
			}
		}
		else
		{
			mListAdapter.updateData(mDepartures);
		}
		
		mDb.close();
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
					mForecastQuery.stopThread();
					finish();
				}
			});
			return dialog;
		default:
			return null;
		}
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
	protected void onSaveInstanceState(Bundle outState)
	{
		if (mDepartures != null)
			outState.putSerializable("departures", mDepartures);
		
		super.onSaveInstanceState(outState);
	}

	@Override
	public Object onRetainNonConfigurationInstance()
	{
		return mForecastQuery;
	}

	private class ForecastQuery implements Runnable
	{
		public static final byte STATE_NOT_STARTED = 0, STATE_RUNNNING = 1,
		        STATE_DONE = 2;

		public static final int MESSAGE_COMPLETE = 0;
		
		private boolean mStopThread;

		Handler mHandler;
		String mAddress;
		Vector<Departure> mResult = null;
		byte mStatus = STATE_NOT_STARTED;

		public ForecastQuery(Handler handler, String address)
		{
			mHandler = handler;
			mAddress = address;
		}

		@Override
		public void run()
		{
			mStopThread = false;
			mStatus = STATE_RUNNNING;
			HttpURLConnection connection = null;

			// Connect to Västtrafik
			try
			{
				URL url = new URL(mAddress);
				connection = (HttpURLConnection) url.openConnection();
			}
			catch (MalformedURLException e)
			{
			}
			catch (IOException e)
			{
				// Connection could not be made
				e.printStackTrace();
			}

			NextTripHandler handler = new NextTripHandler();
			try
			{
				String xml = Tidtabell.getXmlData(connection.getInputStream());

				// Parse the xml
				SAXParserFactory saxFactory = SAXParserFactory.newInstance();
				SAXParser saxParser = saxFactory.newSAXParser();
				StringReader sr = new StringReader(xml);
				saxParser.parse(new InputSource(sr), handler);
			}
			catch (ParserConfigurationException e)
			{
			}
			catch (IOException e)
			{
				// Something went wrong with the IO during parse
				e.printStackTrace();
			}
			catch (SAXException e)
			{
				// Something went wrong with the parse
				e.printStackTrace();
			}

			
			// Get the result from the parse
			mResult = handler.getDepartureList();
			
			if (!mStopThread)
			{
    			mHandler.sendEmptyMessage(MESSAGE_COMPLETE);
    			mStatus = STATE_DONE;
			}
		}

		public void setHandler(Handler handler)
		{
			mHandler = handler;
		}

		public byte getStatus()
		{
			return mStatus;
		}

		public Vector<Departure> getResult()
		{
			return mResult;
		}
		
		public void stopThread()
		{
			mStopThread = true;
		}
	}

 	final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case ForecastQuery.MESSAGE_COMPLETE:
				dismissDialog(DIALOG_PROGRESS);
				mDepartures = mForecastQuery.getResult();
				mListAdapter.updateData(mDepartures);
				break;
			default:
				break;
			}
		}
	};

	private void saveToFile(String s)
	{
		String FILENAME = "xml_debug.txt";

		try
		{
			FileOutputStream fos = openFileOutput(FILENAME,
			        Context.MODE_PRIVATE);
			fos.write(s.getBytes());
			fos.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}