package com.bender.tidtabell;

import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class NextTripHandler extends DefaultHandler implements Response
{
	
	boolean inItem = false;
	boolean inDestination = false;
	
	private Departure currentDeparture;
	private Vector<Departure> departureList = new Vector<Departure>();
	
	@Override
	public void startElement(String uri, String localName, String qName, 
			Attributes attributes) 
		throws SAXException
	{
		if (qName.equalsIgnoreCase("item")) 
		{
			inItem = true;
			currentDeparture = new Departure();
			
			//set line number
			String line = attributes.getValue("line_number");
			currentDeparture.setLine(line);
			
			//set fore- and background color
			String foreground = attributes.getValue("line_number_foreground_color");
			String background = attributes.getValue("line_number_background_color");
			if (foreground != null)
				currentDeparture.setLineForegroundColor(foreground);
			if (background != null)
				currentDeparture.setLineBackgroundColor(background);
			
			//set departure time
			String timeString = attributes.getValue("next_trip_forecast_time");
			int[] timeArray = parseTime(timeString);
			GregorianCalendar date = new GregorianCalendar(TimeZone.getTimeZone("Europe/Stockholm"));
			date.set(timeArray[0],
					timeArray[1]-1, //-1 because January = 0
					timeArray[2],
					timeArray[3],
					timeArray[4],
					timeArray[5]);
			currentDeparture.setTime(date);
		}
		else if (qName.equalsIgnoreCase("destination")) 
		{
			inDestination = true;
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) 
		throws SAXException
	{
		if (qName.equalsIgnoreCase("item")) 
		{
			inItem = false;
			departureList.add(currentDeparture);
			currentDeparture = null;
		}
	}
	
	@Override
	public void characters(char ch[], int start, int length) 
		throws SAXException
	{
		if (inDestination)
			currentDeparture.setDestination(new String(ch, start, length));
	}
	
	public Vector<Departure> getDepartureList()
	{
		return departureList;
	}
	
	private int[] parseTime(String s)
	{
		int[] time = new int[6];
		
		//year
		time[0] = Integer.parseInt(s.substring(0, 4));
		
		//month
		time[1] = Integer.parseInt(s.substring(5, 7));
		
		//day
		time[2] = Integer.parseInt(s.substring(8, 10));
		
		//hours
		time[3] = Integer.parseInt(s.substring(11, 13));
		
		//minutes
		time[4] = Integer.parseInt(s.substring(14, 16));
		
		//seconds
		time[5] = Integer.parseInt(s.substring(17, 19));
		
		return time;
	}

	@Override
	public String errorMessage()
	{
		return null;
	}

	@Override
	public boolean success()
	{
		return true;
	}
}