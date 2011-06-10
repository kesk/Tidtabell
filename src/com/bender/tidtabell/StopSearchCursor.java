package com.bender.tidtabell;

import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.database.AbstractCursor;

public class StopSearchCursor extends AbstractCursor
{
	public static final String ID = "id", NAME = "name",
	        FRIENDLY_NAME = "friendly_name", COUNTY = "county";
	private static final String[] COLUMN_NAMES = { ID, NAME, FRIENDLY_NAME,
	        COUNTY };

	Stop[] mStops;

	public StopSearchCursor(String query)
	{
		String xml = stopQuery(query);
		if (xml != null)
			mStops = parseXml(xml);
		else
			mStops = new Stop[0];
	}

	private String stopQuery(String query)
	{
		String queryUrl = StopSearch.SEARCH_URL + "&searchString=" + query
		        + "&count=" + 5;
		HttpURLConnection connection = null;
		// Connect to Västtrafik
		URL url;
		try
		{
			url = new URL(queryUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(15 * 1000);

			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
				return Tidtabell.getXmlData(connection.getInputStream());
			else
				return null;
		}
		catch (MalformedURLException e)
		{
		}
		catch (IOException e)
		{
		}
		catch (ParserConfigurationException e)
		{
		}
		catch (SAXException e)
		{
		}
		return null;
	}

	private Stop[] parseXml(String xml)
	{
		StopSearchHandler parseHandler = new StopSearchHandler();
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();

		try
		{
			// Parse the xml
			SAXParser saxParser = saxFactory.newSAXParser();
			StringReader sr = new StringReader(xml);
			saxParser.parse(new InputSource(sr), parseHandler);
			return parseHandler.getStops();
		}
		catch (SAXException e)
		{
		}
		catch (IOException e)
		{
		}
		catch (ParserConfigurationException e)
		{
		}

		return new Stop[0];
	}

	@Override
	public int getCount()
	{
		return mStops.length;
	}

	@Override
	public String[] getColumnNames()
	{
		return COLUMN_NAMES;
	}

	@Override
	public String getString(int column)
	{
		Stop stop = mStops[mPos];
		
		switch (column)
		{
		case 0: // id
			return stop.getId();
		case 1: // name
			return stop.getName();
		case 2: // friendly name
			return stop.getFriendlyName();
		case 3: // county
			return stop.getCounty();
		default:
			return "";
		}
	}

	@Override
	public short getShort(int column)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getInt(int column)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getLong(int column)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getFloat(int column)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getDouble(int column)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isNull(int column)
	{
		return false;
	}
}
