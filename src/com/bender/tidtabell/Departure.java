package com.bender.tidtabell;

import java.io.Serializable;
import java.util.GregorianCalendar;

public class Departure
{
	private String mLine;
	private int[] mLineForegroundColor = { 255, 255, 255, 255 };
	private int[] mLineBackgroundColor = { 0, 0, 0, 0 };
	private String mDestination;
	private GregorianCalendar mTime;

	public Departure()
	{
	}

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

	public void setTime(GregorianCalendar date)
	{
		this.mTime = date;
	}

	public GregorianCalendar getTime()
	{
		return mTime;
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
}