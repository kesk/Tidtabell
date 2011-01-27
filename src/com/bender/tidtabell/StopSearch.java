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

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

	private StopListAdapter mListAdapter;
	private ProgressDialog mProgressDialog;
	private Vector<Stop> mStops = new Vector<Stop>();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mListAdapter = new StopListAdapter(this, mStops);
		setListAdapter(mListAdapter);

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

		// If the intent was a search intent then perform the search
		if (Intent.ACTION_SEARCH.equals(intent.getAction()))
		{
			String query = intent.getStringExtra(SearchManager.QUERY);
			String url = SEARCH_URL + "&searchString=" + query + "&count="
			        + NUM_SEARCH_RESULT;

			new StopSearchQueryTask().execute(url);
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

			new StopSearchQueryTask().execute(url);
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

	private class StopSearchQueryTask extends
	        AsyncTask<String, Void, Vector<Stop>>
	{
		@Override
		protected void onPreExecute()
		{
			// Show "loading" dialog
			mProgressDialog = ProgressDialog.show(StopSearch.this, "",
			        "Loading. Please wait...", true);
		}

		@Override
		protected Vector<Stop> doInBackground(String... params)
		{
			Vector<Stop> stops = null;

			try
			{
				URL url = new URL(params[0]);
				HttpURLConnection connection = (HttpURLConnection) url
				        .openConnection();

				String xml = Tidtabell.getXmlData(connection.getInputStream());

				// Parse the xml
				SAXParserFactory saxFactory = SAXParserFactory.newInstance();
				SAXParser saxParser = saxFactory.newSAXParser();
				StopSearchHandler handler = new StopSearchHandler();
				StringReader sr = new StringReader(xml);
				saxParser.parse(new InputSource(sr), handler);

				// Get the result from the parse
				stops = handler.getStops();
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

			return stops;
		}

		@Override
		protected void onPostExecute(Vector<Stop> result)
		{
			// Dismiss "loading" dialog
			mProgressDialog.dismiss();

			// Update the list y0!
			mStops = result;
			mListAdapter.updateList(result);
		}
	}
}
