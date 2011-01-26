package com.bender.tidtabell;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class Tidtabell extends Activity
{
	public static final String IDENTIFIER = "1d1b034c-b4cc-49ec-a69e-70b91f5fb325";
	public static final String NEXT_TRIP_URL = "http://vasttrafik.se/"
	        + "External_Services/NextTrip.asmx/" + "GetForecast?identifier="
	        + IDENTIFIER;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
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
}