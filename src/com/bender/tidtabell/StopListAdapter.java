package com.bender.tidtabell;

import java.util.Vector;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class StopListAdapter extends BaseAdapter
{
	Context mContext;
	Vector<Stop> mStops;
	float[] mOrientation = new float[3];
	
	public StopListAdapter(Context context, Vector<Stop> stops, float[] orientation)
	{
		mContext = context;
		mStops = stops;
		mOrientation = orientation;
	}

	@Override
	public int getCount()
	{
		return mStops.size();
	}

	@Override
	public Object getItem(int position)
	{
		return mStops.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		Stop stop = mStops.get(position);
		SearchListItem sli;

		if (convertView == null)
			sli = new SearchListItem(mContext, stop);
		else
		{
			sli = (SearchListItem) convertView;
			sli.setName(stop.getName());
		}

		return sli;
	}
	
	public void updateList(Vector<Stop> stops)
	{
		mStops = stops;
		notifyDataSetChanged();
	}
	
	public void updateOrientation(float[] orientation)
	{
		mOrientation = orientation;
	}

	private class SearchListItem extends RelativeLayout
	{
		private TextView textView, distance;

		public SearchListItem(Context context, Stop stop)
		{
			super(context);
			
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.stop_list_item, this);
			
			textView = (TextView) findViewById(R.id.stop_name);
			textView.setText(stop.getName());
			
			DirectionNeedle needle = (DirectionNeedle) findViewById(R.id.direction_needle);
			needle.setOrientation(mOrientation);
		}

		public void setName(String name)
		{
			textView.setText(name);
		}
	}
}