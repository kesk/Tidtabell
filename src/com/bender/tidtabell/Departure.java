package com.bender.tidtabell;

import java.io.Serializable;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class Departure implements Serializable
{
	private static final long serialVersionUID = 964593260545572076L;

	private String mLine;
	private int[] mLineForegroundColor = { 255, 255, 255, 255 };
	private int[] mLineBackgroundColor = { 0, 0, 0, 0 };
	private String mDestination;
	private GregorianCalendar mNextForecast = null, mNextNextForecast = null,
	        mNextPlanned = null, mNextNextPlanned = null;
	private String mTrafficIsland;

	public void setLine(String line)
	{
		this.mLine = line;
	}

	public String getLine()
	{
		return mLine;
	}

	public void setDestination(String destination)
	{
		this.mDestination = destination;
	}

	public String getDestination()
	{
		return mDestination;
	}

	public void setNextForecast(String nextForecast)
	{
		if (!nextForecast.equals("0001-01-01T00:00:00"))
			mNextForecast = parseTime(nextForecast);
	}

	public GregorianCalendar getNextForecast()
	{
		return mNextForecast;
	}

	public void setNextNextForecast(String nextNextForecast)
	{
		if (!nextNextForecast.equals("0001-01-01T00:00:00"))
			mNextNextForecast = parseTime(nextNextForecast);
	}

	public GregorianCalendar getNextNextForecast()
	{
		return mNextNextForecast;
	}

	public void setNextPlanned(String nextPlanned)
	{
		if (!nextPlanned.equals("0001-01-01T00:00:00"))
			mNextPlanned = parseTime(nextPlanned);
	}

	public GregorianCalendar getNextPlanned()
	{
		return mNextPlanned;
	}

	public void setNextNextPlanned(String nextNextPlanned)
	{
		if (!nextNextPlanned.equals("0001-01-01T00:00:00"))
			mNextNextPlanned = parseTime(nextNextPlanned);
	}

	public GregorianCalendar getNextNextPlanned()
	{
		return mNextNextPlanned;
	}

	public void setLineForegroundColor(String lineForegroundColor)
	{
		this.mLineForegroundColor[0] = Integer.parseInt(
		        lineForegroundColor.substring(1, 3), 16);
		this.mLineForegroundColor[1] = Integer.parseInt(
		        lineForegroundColor.substring(3, 5), 16);
		this.mLineForegroundColor[2] = Integer.parseInt(
		        lineForegroundColor.substring(5, 7), 16);
	}

	public int[] getLineForegroundColor()
	{
		return mLineForegroundColor;
	}

	public void setLineBackgroundColor(String lineBackgroundColor)
	{
		this.mLineBackgroundColor[0] = Integer.parseInt(
		        lineBackgroundColor.substring(1, 3), 16);
		this.mLineBackgroundColor[1] = Integer.parseInt(
		        lineBackgroundColor.substring(3, 5), 16);
		this.mLineBackgroundColor[2] = Integer.parseInt(
		        lineBackgroundColor.substring(5, 7), 16);
	}

	public int[] getLineBackgroundColor()
	{
		return mLineBackgroundColor;
	}

	public void setTrafficIsland(String trafficIsland)
	{
		mTrafficIsland = trafficIsland;
	}

	public String getTrafficIsland()
	{
		return mTrafficIsland;
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