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
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class NextTrip extends ListActivity
{
	public static final int ERROR_DIALOG = 0, PROGRESS_DIALOG = 1;

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
		String stopId;
		String stopName;

		if (b != null)
		{
			stopId = b.getString("stopId");
			stopName = b.getString("stopName");

			TextView tv = (TextView) findViewById(R.id.next_trip_header);
			tv.setText(stopName);

			StringBuilder s = new StringBuilder();
			s.append("http://vasttrafik.se/External_Services/");

			s.append("NextTrip.asmx/GetForecast");

			s.append("?identifier=");
			s.append(Tidtabell.IDENTIFIER);

			s.append("&stopId=");
			s.append(stopId);

			new NextTripQueryTask().execute(s.toString());
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
			mProgressDialog = ProgressDialog.show(NextTrip.this, "",
			        "Loading. Please wait...", true);
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
			mProgressDialog.dismiss();

			// Update the list y0!
			mListAdapter.updateData(result);
		}
	}
}