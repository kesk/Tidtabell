package com.bender.tidtabell;

import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Vector;
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
	private Vector<Departure> departureList = new Vector<Departure>();

	@Override
	public void startElement(String uri, String localName, String qName,
	        Attributes attributes) throws SAXException
	{
		if (qName.equalsIgnoreCase("forecast"))
			inForecast = true;
		else if (inForecast && qName.equalsIgnoreCase("item"))
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

			// Set departure time
			String timeString = attributes.getValue("next_trip_forecast_time");
			// Check that timeString has the right format
			Matcher m = TIME_PATTERN.matcher(timeString);
			if (timeString == null || !m.matches())
			{
				parseError = true;
				return;
			}
			int[] timeArray = parseTime(timeString);
			GregorianCalendar date = new GregorianCalendar(
			        TimeZone.getTimeZone("Europe/Stockholm"));
			date.set(timeArray[0], timeArray[1] - 1, // -1 because January = 0
			        timeArray[2], timeArray[3], timeArray[4], timeArray[5]);
			currentDeparture.setTime(date);
		}
		else if (inForecast && inItem && qName.equalsIgnoreCase("destination"))
		{
			inDestination = true;
		}
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

	public Vector<Departure> getDepartureList()
	{
		return departureList;
	}

	private int[] parseTime(String s)
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

		return time;
	}
}