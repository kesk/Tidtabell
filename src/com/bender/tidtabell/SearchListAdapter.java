package com.bender.tidtabell;

import java.util.Vector;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SearchListAdapter extends BaseAdapter
{
	Context mContext;
	Vector<Stop> mStops;

	public SearchListAdapter(Context context, Vector<Stop> stops)
	{
		mContext = context;
		mStops = stops;
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
			sli = new SearchListItem(mContext, stop.getName());
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

	private class SearchListItem extends RelativeLayout
	{
		private TextView textView;

		public SearchListItem(Context context, String text)
		{
			super(context);
			textView = new TextView(context);
			textView.setText(text);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_PT, 12);
			addView(textView);
		}

		public void setName(String name)
		{
			textView.setText(name);
		}
	}
}