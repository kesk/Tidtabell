package com.bender.tidtabell;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class NextTripHandler extends DefaultHandler
{
	boolean inForecast = false;
	boolean inItem = false;
	boolean inDestination = false;

	boolean parseError = false;

	private static final Pattern TIME_PATTERN = Pattern
	        .compile("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}");

	private Departure currentDeparture;
	private ArrayList<Departure> departureList = new ArrayList<Departure>();

	@Override
	public void startElement(String uri, String localName, String qName,
	        Attributes attributes) throws SAXException
	{
		// <forecast>
		if (!inForecast && qName.equalsIgnoreCase("forecast"))
			inForecast = true;
		// <item>
		else if (inForecast && !inItem && qName.equalsIgnoreCase("item"))
		{
			inItem = true;
			currentDeparture = new Departure();

			// Set line number
			String line = attributes.getValue("line_number");
			if (line == null)
			{
				parseError = true;
				return;
			}
			currentDeparture.setLine(line);

			// Set fore- and background color
			String foreground = attributes
			        .getValue("line_number_foreground_color");
			String background = attributes
			        .getValue("line_number_background_color");
			currentDeparture.setLineForegroundColor(foreground);
			currentDeparture.setLineBackgroundColor(background);

			// Set next departure time
			String next = attributes.getValue("next_trip_forecast_time");
			// Check that timeString has the right format
			Matcher m1 = TIME_PATTERN.matcher(next);
			if (next == null || !m1.matches())
			{
				parseError = true;
				return;
			}
			currentDeparture.setTime(parseTime(next));
			
			// next next trip
			String nextNext = attributes.getValue("next_next_trip_forecast_time");
			if (nextNext != null)
			{
				Matcher m2 = TIME_PATTERN.matcher(nextNext);
				if (m2.matches())
					currentDeparture.SetTimeNext(parseTime(nextNext));
			}

			// Traffic island
			String island = attributes.getValue("traffic_island");
			currentDeparture.setTrafficIsland(island);
		}
		// <destination>
		else if (inForecast && inItem && !inDestination
		        && qName.equalsIgnoreCase("destination"))
			inDestination = true;
	}

	@Override
	public void characters(char ch[], int start, int length)
	        throws SAXException
	{
		if (inForecast && inItem && inDestination)
			currentDeparture.setDestination(new String(ch, start, length));
	}

	@Override
	public void endElement(String uri, String localName, String qName)
	        throws SAXException
	{
		if (qName.equalsIgnoreCase("forecast"))
			inForecast = false;
		else if (qName.equalsIgnoreCase("item"))
		{
			inItem = false;

			// Something went wrong with the parse, throw it away and continue
			if (!parseError)
				departureList.add(currentDeparture);

			currentDeparture = null;
		}
		else if (qName.equalsIgnoreCase("destination"))
			inDestination = false;
	}

	public Departure[] getDepartureList()
	{
		return departureList.toArray(new Departure[departureList.size()]);
	}

	private GregorianCalendar parseTime(String s)
	{
		int[] time = new int[6];

		// year
		time[0] = Integer.parseInt(s.substring(0, 4));

		// month
		time[1] = Integer.parseInt(s.substring(5, 7));

		// day
		time[2] = Integer.parseInt(s.substring(8, 10));

		// hours
		time[3] = Integer.parseInt(s.substring(11, 13));

		// minutes
		time[4] = Integer.parseInt(s.substring(14, 16));

		// seconds
		time[5] = Integer.parseInt(s.substring(17, 19));

		GregorianCalendar date = new GregorianCalendar(
		        TimeZone.getTimeZone("Europe/Stockholm"));
		date.set(time[0], time[1] - 1, time[2], time[3], time[4], time[5]);

		return date;
	}
}