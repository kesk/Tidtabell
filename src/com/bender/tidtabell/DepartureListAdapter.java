package com.bender.tidtabell;

import java.util.GregorianCalendar;

import android.content.Context;
import android.graphics.Color;
import android.net.TrafficStats;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

public class DepartureListAdapter extends BaseAdapter
{
	private Context mContext;
	private Departure[] mDepartures;

	public DepartureListAdapter(Context context, Departure[] departures)
	{
		mContext = context;
		mDepartures = departures;
	}

	@Override
	public int getCount()
	{
		return mDepartures.length;
	}

	@Override
	public Object getItem(int position)
	{
		return mDepartures[position];
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		Departure departure = mDepartures[position];
		DepartureListItem dli;

		if (convertView == null)
			dli = new DepartureListItem(mContext);
		else
			dli = (DepartureListItem) convertView;

		dli.setLine(departure.getLine());
		dli.setLineForegroundColor(departure.getLineForegroundColor());
		dli.setLineBackgroundColor(departure.getLineBackgroundColor());
		dli.setDestination(departure.getDestination());
		dli.setTime(departure.getTime());
		if (departure.getTimeNext().compareTo(new GregorianCalendar()) > 0)
			dli.setTimeNext(departure.getTimeNext());
		dli.setTrafficIsland(departure.getTrafficIsland());

		return dli;
	}

	public void updateData(Departure[] departures)
	{
		mDepartures = departures;
		notifyDataSetChanged();
	}

	public class DepartureListItem extends TableLayout
	{
		private TextView mLine, mLineForeground, mLineBackground, mDestination,
		        mTime, mTimeNext, mTrafficIsland;

		public DepartureListItem(Context context)
		{
			super(context);
			LayoutInflater inflater = (LayoutInflater) context
			        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.departure_list_item, this);

			mLine = (TextView) findViewById(R.id.line_number);
			mLineForeground = (TextView) findViewById(R.id.line_number);
			mLineBackground = (TextView) findViewById(R.id.line_number);
			mDestination = (TextView) findViewById(R.id.destination);
			mTime = (TextView) findViewById(R.id.time_left);
			mTimeNext = (TextView) findViewById(R.id.time_left_next);
			mTrafficIsland = (TextView) findViewById(R.id.traffic_island);
		}

		public void setLine(String line)
		{
			if (line.matches("[0-9][0-9]?"))
				mLine.setTextSize(TypedValue.COMPLEX_UNIT_PT, 20);
			else if (line.matches("[0-9]{3}"))
				mLine.setTextSize(TypedValue.COMPLEX_UNIT_PT, 15);
			else
			{
				mLine.setTextSize(TypedValue.COMPLEX_UNIT_PT, 6);
				line = line.toUpperCase();
			}

			mLine.setText(line);
		}

		public void setLineForegroundColor(int[] color)
		{
			mLineForeground.setTextColor(Color.argb(255, color[0], color[1],
			        color[2]));
		}

		public void setLineBackgroundColor(int[] color)
		{
			mLineBackground.setBackgroundColor(Color.argb(255, color[0],
			        color[1], color[2]));
		}

		public void setDestination(String destination)
		{
			mDestination.setText(destination);
		}

		public void setTime(GregorianCalendar time)
		{
			String s = mkTimeString(time);
			mTime.setText(s);
		}

		public void setTimeNext(GregorianCalendar time)
		{
			String s = mkTimeString(time);
			mTimeNext.setText("(" + s + ")");
		}

		public void setTrafficIsland(String island)
		{
			mTrafficIsland.setText(island);
		}

		private String mkTimeString(GregorianCalendar time)
		{
			GregorianCalendar now = new GregorianCalendar();
			long diff = time.getTimeInMillis() - now.getTimeInMillis();

			long hours = (diff / (1000 * 60 * 60)), minutes = (diff / (1000 * 60)) % 60, seconds = (diff / 1000) % 60;

			String s = "";

			if (hours > 0)
				s += hours + "h";
			if (minutes > 0 && seconds >= 30)
				s += (minutes + 1) + "m";
			else if (minutes > 0)
				s += minutes + "m";
			if (seconds <= 0)
				s = "nu";

			return s;
		}
	}
}
