package com.bender.tidtabell;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import coordinatetransformation.positions.RT90Position;
import coordinatetransformation.positions.RT90Position.RT90Projection;
import coordinatetransformation.positions.WGS84Position;

public class StopSearchHandler extends DefaultHandler
{
	// XML tag names
	public static final String SUGGESTIONS = "suggestions", ITEM = "item",
	        ITEMS = "items", STOP_NAME = "stop_name",
	        FRIENDLY_NAME = "friendly_name", COUNTY = "county";

	// XML attribute names
	public static final String STOP_ID = "stop_id", RT90_X = "rt90_x",
	        RT90_Y = "rt90_y";

	private boolean mParseFail = false;
	private boolean mInSuggestions = false, mInItems = false, mInItem = false,
	        mInStopName = false, mInFriendlyName = false, mInCounty = false;

	private ArrayList<Stop> mStops = new ArrayList<Stop>();
	private Stop mCurrentStop;

	@Override
	public void startElement(String uri, String localName, String qName,
	        Attributes attributes)
	{
		// <items>
		if (!mInItems && qName.equalsIgnoreCase(ITEMS))
			mInItems = true;
		else if (mInItems)
		{
			// <item>
			if (!mInItem && qName.equalsIgnoreCase(ITEM))
			{
				mInItem = true;
				mCurrentStop = new Stop();

				// ID
				String id = attributes.getValue(STOP_ID);
				mCurrentStop.setId(id);

				// Location
				double rt90X = new Double(attributes.getValue(RT90_X));
				double rt90Y = new Double(attributes.getValue(RT90_Y));
				RT90Position rt90Pos = new RT90Position(rt90X, rt90Y,
				        RT90Projection.rt90_2_5_gon_v);
				WGS84Position wgs84Pos = rt90Pos.toWGS84();
				mCurrentStop.setLatitude(wgs84Pos.getLatitude());
				mCurrentStop.setLongitude(wgs84Pos.getLongitude());
			}
			else if (mInItem)
			{
				if (!mInStopName && qName.equalsIgnoreCase(STOP_NAME))
					mInStopName = true;
				// <friendly_name>
				else if (!mInStopName && qName.equals(FRIENDLY_NAME))
					mInFriendlyName = true;
				// <county>
				else if (!mInStopName && qName.equalsIgnoreCase(COUNTY))
					mInCounty = true;
			}
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
	{
		if (mInItems && mInItem)
		{
			if (mInStopName)
				mCurrentStop.setName(new String(ch, start, length));
			else if (mInFriendlyName)
				mCurrentStop.setFriendlyName(new String(ch, start, length));
			else if (mInCounty)
				mCurrentStop.setCounty(new String(ch, start, length));
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
	{
		// </items>
		if (mInItems && qName.equalsIgnoreCase(ITEMS))
			mInItems = false;
		else if (mInItems && mInItem)
		{
			// </item>
			if (qName.equalsIgnoreCase(ITEM))
			{
				mInItem = false;
				if (!mParseFail)
					mStops.add(mCurrentStop);
				mCurrentStop = null;
				mParseFail = false;
			}
			// </stop_name>
			else if (mInStopName && qName.equalsIgnoreCase(STOP_NAME))
				mInStopName = false;
			// </friendly_name>
			else if (mInFriendlyName && qName.equalsIgnoreCase(FRIENDLY_NAME))
				mInFriendlyName = false;
			// </county>
			else if (mInCounty && qName.equalsIgnoreCase(COUNTY))
				mInCounty = false;
		}
	}

	public Stop[] getStops()
	{
		return mStops.toArray(new Stop[mStops.size()]);
	}
}
