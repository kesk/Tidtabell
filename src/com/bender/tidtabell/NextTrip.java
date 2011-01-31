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
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
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
	public static final int ERROR_DIALOG = 0, PROGRESS_DIALOG = 1;
	public static final String NEXT_TRIP_URL = "http://vasttrafik.se/External_Services/NextTrip.asmx/GetForecast?identifier="
	        + Tidtabell.IDENTIFIER;

	private ProgressDialog mProgressDialog;
	private DepartureListAdapter mListAdapter;
	private DatabaseOpenHelper mDb;
	private NextTripQueryTask mNextTripQueryTask;
	private Stop mStop;
	private Vector<Departure> mDepartures = null;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mDb = new DatabaseOpenHelper(this);

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

		if (mStop != null)
		{
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

			favToggle.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v)
				{
					DatabaseOpenHelper db = new DatabaseOpenHelper(
					        NextTrip.this);

					if (favToggle.isChecked())
					{
						Toast.makeText(NextTrip.this,
						        R.string.add_favorite_toast, Toast.LENGTH_SHORT)
						        .show();
						db.addFavorite(mStop);
					}
					else
					{
						Toast.makeText(NextTrip.this,
						        R.string.remove_favorite_toast,
						        Toast.LENGTH_SHORT).show();
						db.removeFavorite(mStop);
					}
				}
			});

			if (mDepartures == null)
			{
				String url = NEXT_TRIP_URL + "&stopId=" + mStop.getId();

				if (savedInstanceState == null)
					mNextTripQueryTask = (NextTripQueryTask) new NextTripQueryTask()
					        .execute(url);
				else
					dismissDialog(PROGRESS_DIALOG);
			}
			else
			{
				mListAdapter.updateData(mDepartures);
			}
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		if (mNextTripQueryTask != null)
			mNextTripQueryTask.cancel(true);
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch (id)
		{
		case PROGRESS_DIALOG:
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setMessage(getString(R.string.loading_dialog));
			return mProgressDialog;
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
		super.onSaveInstanceState(outState);

		if (mDepartures != null)
			outState.putSerializable("departures", mDepartures);
	}

	private class NextTripQueryTask extends
	        AsyncTask<String, Void, Vector<Departure>>
	{
		@Override
		protected void onPreExecute()
		{
			// Show "loading" dialog
			showDialog(PROGRESS_DIALOG);
		}

		@Override
		protected Vector<Departure> doInBackground(String... params)
		{
			Vector<Departure> departures = null;
			HttpURLConnection connection = null;

			// Connect to Västtrafik
			try
			{
				URL url = new URL(params[0]);
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
			departures = handler.getDepartureList();

			return departures;
		}

		@Override
		protected void onPostExecute(Vector<Departure> result)
		{
			// Dismiss "loading" dialog
			dismissDialog(PROGRESS_DIALOG);

			// Update the list y0!
			mDepartures = result;
			mListAdapter.updateData(result);
		}
	}

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