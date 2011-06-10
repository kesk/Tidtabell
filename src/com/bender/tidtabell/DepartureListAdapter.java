package com.bender.tidtabell;

import java.util.GregorianCalendar;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
	
	public DepartureListAdapter(Context context)
	{
		mContext = context;
		mDepartures = new Departure[0];
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
		dli.setTrafficIsland(departure.getTrafficIsland());
		
		String next, nextNext;
		
		if (departure.getNextForecast() != null)
			next = mkTimeString(departure.getNextForecast());
		else if (departure.getNextPlanned() != null)
			next = "~" + mkTimeString(departure.getNextPlanned());
		else 
			next = "";
		
		if (departure.getNextNextForecast() != null)
			nextNext = "(" + mkTimeString(departure.getNextNextForecast()) + ")";
		else if (departure.getNextNextPlanned() != null)
			nextNext = "(~" + mkTimeString(departure.getNextNextPlanned()) + ")";
		else
			nextNext = "";
		
		dli.setNextTime(next);
		dli.setNextNextTime(nextNext);

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
			mLine.setTextScaleX(1.0f);
			if (line.matches("[0-9][0-9]?"))
				mLine.setTextSize(TypedValue.COMPLEX_UNIT_PT, 20);
			else if (line.matches("[0-9]{3}"))
				mLine.setTextSize(TypedValue.COMPLEX_UNIT_PT, 15);
			else if (line.length() < 5)
			{
				mLine.setTextSize(TypedValue.COMPLEX_UNIT_PT, 15);
				mLine.setTextScaleX(0.6f);
				line = line.toUpperCase();
			}
			else
			{
				mLine.setTextSize(TypedValue.COMPLEX_UNIT_PT, 15);
				mLine.setTextScaleX(0.4f);
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

		public void setNextTime(String time)
		{
			mTime.setText(time);
		}

		public void setNextNextTime(String time)
		{
			mTimeNext.setText(time);
		}

		public void setTrafficIsland(String island)
		{
			mTrafficIsland.setText(island);
		}
	}

	private String mkTimeString(GregorianCalendar time)
	{
		GregorianCalendar now = new GregorianCalendar();
		long diff = time.getTimeInMillis() - now.getTimeInMillis();

		long hours = (diff / (1000 * 60 * 60));
		long minutes = (diff / (1000 * 60)) % 60;
		long seconds = (diff / 1000) % 60;

		String s;

		if (hours <= 0 && minutes <= 0)
		{
			if (seconds > 0)
    			s = "<1m";
    		else
    			s = "nu";
		}
		else
		{
			s = "";
			
			if (hours > 0)
    			s += hours + "h";
    		if (minutes > 0 && seconds >= 30)
    			s += (minutes + 1) + "m";
    		else if (minutes > 0)
    			s += minutes + "m";
		}
		
		return s;
	}
}
