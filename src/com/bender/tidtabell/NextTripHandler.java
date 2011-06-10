package com.bender.tidtabell;

import java.util.ArrayList;
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
	boolean inFreeText = false;
	boolean inText = false;

	boolean parseError = false;

	private static final Pattern TIME_PATTERN = Pattern
	        .compile("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}");

	private Departure currentDeparture;
	private ArrayList<Departure> departureList = new ArrayList<Departure>();
	private ArrayList<String> freeText = new ArrayList<String>();

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
			String nf = attributes.getValue("next_trip_forecast_time");
			String nnf = attributes.getValue("next_next_trip_forecast_time");
			String np = attributes.getValue("next_trip_planned_time");
			String nnp = attributes.getValue("next_next_trip_planned_time");

			Matcher m = TIME_PATTERN.matcher(nf);
			if (!m.matches())
			{
				parseError = true;
				return;
			}
			currentDeparture.setNextForecast(nf);
			currentDeparture.setNextNextForecast(nnf);
			currentDeparture.setNextPlanned(np);
			currentDeparture.setNextNextPlanned(nnp);

			// Traffic island
			String island = attributes.getValue("traffic_island");
			currentDeparture.setTrafficIsland(island);
		}
		// <destination>
		else if (inForecast && inItem && !inDestination
		        && qName.equalsIgnoreCase("destination"))
			inDestination = true;
		// <free_text>
		else if (!inFreeText && qName.equalsIgnoreCase("free_text"))
			inFreeText = true;
		else if (inFreeText && !inItem && qName.equalsIgnoreCase("item"))
			inItem = true;
		else if (inFreeText && inItem && !inText && qName.equalsIgnoreCase("text"))
			inText = true;
	}

	@Override
	public void characters(char ch[], int start, int length)
	        throws SAXException
	{
		if (inForecast && inItem && inDestination)
			currentDeparture.setDestination(new String(ch, start, length));
		else if (inFreeText && inItem && inText)
			freeText.add(new String(ch, start, length));
	}

	@Override
	public void endElement(String uri, String localName, String qName)
	        throws SAXException
	{
		if (inForecast && qName.equalsIgnoreCase("forecast"))
			inForecast = false;
		else if (inForecast && inItem && qName.equalsIgnoreCase("item"))
		{
			inItem = false;

			// Something went wrong with the parse, throw it away and continue
			if (!parseError)
				departureList.add(currentDeparture);

			currentDeparture = null;
		}
		else if (inForecast && inItem && inDestination
		        && qName.equalsIgnoreCase("destination"))
			inDestination = false;
		else if (inFreeText && qName.equalsIgnoreCase("free_text"))
			inFreeText = false;
		else if (inFreeText && inItem && qName.equalsIgnoreCase("item"))
			inItem = false;
		else if (inFreeText && inItem && inText && qName.equalsIgnoreCase("text"))
			inText = false;
	}

	public Departure[] getDepartureList()
	{
		return departureList.toArray(new Departure[departureList.size()]);
	}
	
	public String[] getFreeText()
	{
		return freeText.toArray(new String[freeText.size()]);
	}
}