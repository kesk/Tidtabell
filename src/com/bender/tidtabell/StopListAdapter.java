package com.bender.tidtabell;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.opengl.Matrix;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TableLayout;
import android.widget.TextView;

public class StopListAdapter extends BaseAdapter
{
	Context mContext;
	Stop[] mStops;
	float mHeading = 0;
	int mOrientation;
	Location mLocation;

	public StopListAdapter(Context context, Stop[] stops)
	{
		mContext = context;
		mStops = stops;
	}

	@Override
	public int getCount()
	{
		return mStops.length;
	}

	@Override
	public Object getItem(int position)
	{
		return mStops[position];
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		Stop stop = mStops[position];
		SearchListItem sli;

		if (convertView == null)
			sli = new SearchListItem(mContext);
		else
			sli = (SearchListItem) convertView;

		sli.setName(stop.getName());
		sli.setStopLocation(stop.getLongitude(), stop.getLatitude());
		sli.setCounty(stop.getCounty());

		return sli;
	}

	public void updateList(Stop[] stops)
	{
		mStops = stops;
		notifyDataSetChanged();
	}

	public void setOrientation(int orientation)
	{
		mOrientation = orientation;
	}

	private class SearchListItem extends TableLayout
	{
		private TextView stopName, distance, county;
		private DirectionNeedle needle;
		private Location stopLoc;

		public SearchListItem(Context context)
		{
			super(context);
			stopLoc = new Location("vasttrafik");

			LayoutInflater inflater = (LayoutInflater) context
			        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(R.layout.stop_list_item, this);

			stopName = (TextView) findViewById(R.id.stop_name);
			needle = (DirectionNeedle) findViewById(R.id.direction_needle);
			distance = (TextView) findViewById(R.id.distance);
			county = (TextView) findViewById(R.id.county);
		}

		public void setName(String name)
		{
			stopName.setText(name);
		}

		public void setStopLocation(float longitude, float latitude)
		{
			stopLoc.setLatitude(latitude);
			stopLoc.setLongitude(longitude);

			if (mLocation != null)
			{
				if (latitude != 0 && longitude != 0)
					needle.setActive(true);
				// Set direction of needle
				float deg = mLocation.bearingTo(stopLoc);
				needle.setRotation(mHeading - deg);

				// Set distance in meters
				float m = mLocation.distanceTo(stopLoc);
				String s;
				if (m > 500)
					s = round(m / 1000, 1) + "km";
				else
					s = Math.round(m) + "m";
				distance.setText(s);
			}
		}
		public void setCounty(String county)
		{
			this.county.setText(county);
		}

		private float round(float val, int decPl)
		{
			float p = (float) Math.pow(10, decPl);
			float tmp = Math.round(val * p);
			return (float) tmp / p;
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

		private float[] mAccelValues;
		private float[] mMagnValues;
		private float[] mR = new float[16];
		private float[] mValues = new float[3];

		@Override
		public void onSensorChanged(SensorEvent event)
		{
			switch (event.sensor.getType())
			{
			case Sensor.TYPE_ACCELEROMETER:
				mAccelValues = event.values;
			case Sensor.TYPE_MAGNETIC_FIELD:
				mMagnValues = event.values;
			default:
				if (mAccelValues == null || mMagnValues == null)
					break;
				
				SensorManager.getRotationMatrix(mR, null, mAccelValues,
				        mMagnValues);
				
				// Don't want the needle to flip when the device is held upright
				Matrix.rotateM(mR, 0, -20, 1, 0, 0);
				SensorManager.getOrientation(mR, mValues);
				mHeading = (int) ((mValues[0] / Math.PI) * 180);

				// Compensate for when the device is in landscape mode
				if (mOrientation == Configuration.ORIENTATION_LANDSCAPE)
					mHeading -= 90;
				
				StopListAdapter.this.notifyDataSetChanged();
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy)
		{
		}
	};
}