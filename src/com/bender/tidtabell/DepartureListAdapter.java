package com.bender.tidtabell;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import android.content.Context;
import android.graphics.Color;
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
	private Vector<Departure> mDepartures;
	
	DepartureListAdapter(Context context, Vector<Departure> departures)
	{
		mContext = context;
		mDepartures = departures;
	}
	
	@Override
	public int getCount()
	{
		return mDepartures.size();
	}

	@Override
	public Object getItem(int position)
	{
		return mDepartures.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		Departure departure = mDepartures.get(position);
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

		return dli;
	}

	public class DepartureListItem extends RelativeLayout
	{
		public DepartureListItem(Context context)
		{
			super(context);
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.departure_list_item, this);
		}
		
		public void setLine(String line)
		{
			TextView tv = (TextView) findViewById(R.id.line_number);
			tv.setText(line);
		}
		
		public void setLineForegroundColor(int[] color)
		{
			TextView tv = (TextView) findViewById(R.id.line_number);
			tv.setTextColor(Color.argb(255, color[0], color[1], color[2]));
		}
		
		public void setLineBackgroundColor(int[] color)
		{
			TextView tv = (TextView) findViewById(R.id.line_number);
			tv.setBackgroundColor(Color.argb(255, color[0], color[1], color[2]));
		}
		
		public void setDestination(String destination)
		{
			TextView tv = (TextView) findViewById(R.id.destination);
			tv.setText(destination);
		}
		
		public void setTime(GregorianCalendar time)
		{
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM hh:mm:ss");
			GregorianCalendar now = new GregorianCalendar();
			long diff = time.getTimeInMillis() - now.getTimeInMillis();
			
			long hours = (diff / (1000 * 60 * 60)),
				 minutes = (diff / (1000 * 60)) % 60,
				 seconds = (diff / 1000) % 60;
			
			String s = "";
			
			if (hours > 0)
				s += hours+"h";
			if (minutes > 0)
				s += minutes+"m";
			if (seconds >= 0)
				s += seconds+"s";
			else
				s = "nu";
			
			TextView tv = (TextView) findViewById(R.id.timeLeft);
			tv.setText(s);
		}
	}
}
