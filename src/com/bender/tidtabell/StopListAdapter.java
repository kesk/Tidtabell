package com.bender.tidtabell;

import java.util.Vector;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class StopListAdapter extends BaseAdapter
{
	Context mContext;
	Vector<Stop> mStops;
	float mOrientation = 0;
	Location mLocation;

	public StopListAdapter(Context context, Vector<Stop> stops)
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
			sli = new SearchListItem(mContext);
		else
			sli = (SearchListItem) convertView;

		sli.setName(stop.getName());
		sli.setDirection(stop.getLongitude(), stop.getLatitude());

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
		private DirectionNeedle needle;
		private Location stopLoc;

		public SearchListItem(Context context)
		{
			super(context);
			stopLoc = new Location("vasttrafik");
			
			LayoutInflater inflater = (LayoutInflater) context
			        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.stop_list_item, this);

			textView = (TextView) findViewById(R.id.stop_name);
			needle = (DirectionNeedle) findViewById(R.id.direction_needle);
		}

		public void setName(String name)
		{
			textView.setText(name);
		}

		public void setDirection(float longitude, float latitude)
		{
			stopLoc.setLatitude(latitude);
			stopLoc.setLongitude(longitude);
			
			Log.d("Tidtabell", "Stop loc: "+stopLoc.getLongitude()+"  "+stopLoc.getLatitude());

			if (mLocation != null)
			{
				float o = mLocation.bearingTo(stopLoc);
				needle.setOrientation(mOrientation - o);
			}
		}
	}

	public final LocationListener mLocationListener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
		}

		@Override
		public void onProviderEnabled(String provider)
		{
		}

		@Override
		public void onProviderDisabled(String provider)
		{
		}

		@Override
		public void onLocationChanged(Location location)
		{
			mLocation = location;
		}
	};

	public final SensorEventListener mSensorEventListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event)
		{
			mOrientation = event.values[0];
			StopListAdapter.this.notifyDataSetChanged();
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy)
		{
		}
	};
}