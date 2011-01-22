package com.bender.tidtabell;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class StopSearchHandler extends DefaultHandler
{
	private boolean mInItem = false, mInStopName = false,
	        mInFriendlyName = false, mInCounty = false;

	private Vector<Stop> mStops;
	private Stop mCurrentStop;

	@Override
	public void startElement(String uri, String localName, String qName,
	        Attributes attributes)
	{
		// <item>
		if (qName.equalsIgnoreCase("item"))
		{
			mInItem = true;
			mCurrentStop = new Stop();

			String id = attributes.getValue("stop_id");
			mCurrentStop.setId(id);
		}
		// <stop_name>
		else if (qName.equalsIgnoreCase("stop_name"))
			mInStopName = true;
		// <county>
		else if (qName.equalsIgnoreCase("county"))
			mInCounty = true;
	}

	@Override
	public void endElement(String uri, String localName, String qName)
	{
		// </item>
		if (qName.equalsIgnoreCase("item"))
		{
			mInItem = false;
			mStops.add(mCurrentStop);
			mCurrentStop = null;
		}
		// </stop_name>
		else if (qName.equalsIgnoreCase("stop_name"))
			mInStopName = false;
		// </county>
		else if (qName.equalsIgnoreCase("county"))
			mInCounty = false;
	}

	@Override
	public void characters(char[] ch, int start, int length)
	{
		if (mInItem)
		{
			if (mInStopName)
				mCurrentStop.setName(new String(ch, start, length));
			else if (mInFriendlyName)
				mCurrentStop.setFriendlyName(new String(ch, start, length));
			else if (mInCounty)
				mCurrentStop.setCounty(new String(ch, start, length));
		}
	}

	public Vector<Stop> getStops()
	{
		return mStops;
	}
}
