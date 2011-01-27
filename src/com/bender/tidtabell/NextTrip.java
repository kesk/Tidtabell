package com.bender.tidtabell;

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
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mListAdapter = new DepartureListAdapter(this);
		setListAdapter(mListAdapter);
		setContentView(R.layout.next_trip);

		Bundle b = getIntent().getExtras();
		final Stop stop;

		if (b != null)
		{
			stop = (Stop) b.getSerializable("stop");

			TextView tv = (TextView) findViewById(R.id.stop_name);
			tv.setText(stop.getName());

			final ToggleButton favToggle = (ToggleButton) findViewById(R.id.favorite_button);
			
			// If this station is a favorite
			DatabaseOpenHelper db = new DatabaseOpenHelper(this);
			if (db.isFavorite(stop))
			{
				// Set star to checked
				favToggle.setChecked(true);
			}
			db.close();
			
			favToggle.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v)
				{
					DatabaseOpenHelper db = new DatabaseOpenHelper(NextTrip.this);
					
					if (favToggle.isChecked())
					{
						Toast.makeText(NextTrip.this, R.string.add_favorite_toast, Toast.LENGTH_SHORT).show();
						db.addFavoriteStop(stop);
					}
					else
					{
						Toast.makeText(NextTrip.this, R.string.remove_favorite_toast, Toast.LENGTH_SHORT).show();
						db.removeFavorite(stop);
					}
				}
			});

			String url = NEXT_TRIP_URL + "&stopId=" + stop.getId();

			new NextTripQueryTask().execute(url);
		}
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
	}

	private class NextTripQueryTask extends
	        AsyncTask<String, Void, Vector<Departure>>
	{
		@Override
		protected void onPreExecute()
		{
			// Show "loading" dialog
//			mProgressDialog = ProgressDialog.show(NextTrip.this, "",
//			        "Loading. Please wait...", true);
			showDialog(PROGRESS_DIALOG);
		}

		@Override
		protected Vector<Departure> doInBackground(String... params)
		{
			Vector<Departure> departures = null;

			try
			{
				URL url = new URL(params[0]);
				HttpURLConnection connection = (HttpURLConnection) url
				        .openConnection();

				String xml = Tidtabell.getXmlData(connection.getInputStream());

				// Parse the xml
				SAXParserFactory saxFactory = SAXParserFactory.newInstance();
				SAXParser saxParser = saxFactory.newSAXParser();
				NextTripHandler handler = new NextTripHandler();
				StringReader sr = new StringReader(xml);
				saxParser.parse(new InputSource(sr), handler);

				// Get the result from the parse
				departures = handler.getDepartureList();
			}
			catch (MalformedURLException e)
			{
				Log.e("Tidtabell", e.toString());
			}
			catch (IOException e)
			{
				Log.e("Tidtabell", e.toString());
			}
			catch (ParserConfigurationException e)
			{
				Log.e("Tidtabell", e.toString());
			}
			catch (SAXException e)
			{
				Log.e("Tidtabell", e.toString());
			}

			return departures;
		}

		@Override
		protected void onPostExecute(Vector<Departure> result)
		{
			// Dismiss "loading" dialog
			//mProgressDialog.dismiss();
			dismissDialog(PROGRESS_DIALOG);

			// Update the list y0!
			mListAdapter.updateData(result);
		}
	}
}