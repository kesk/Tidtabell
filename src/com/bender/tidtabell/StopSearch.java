package com.bender.tidtabell;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class StopSearch extends ListActivity
{
	ListAdapter mListAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		
		if(Intent.ACTION_SEARCH.equals(intent.getAction()))
		{
			String query = intent.getStringExtra(SearchManager.QUERY);
		}
	}
	
	private class SearchListAdapter extends BaseAdapter
	{

		@Override
        public int getCount()
        {
	        // TODO Auto-generated method stub
	        return 0;
        }

		@Override
        public Object getItem(int position)
        {
	        // TODO Auto-generated method stub
	        return null;
        }

		@Override
        public long getItemId(int position)
        {
	        // TODO Auto-generated method stub
	        return 0;
        }

		@Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
	        // TODO Auto-generated method stub
	        return null;
        }
	}
	
	private class SearchListItem extends RelativeLayout
	{

		public SearchListItem(Context context, String text)
        {
	        super(context);
	        TextView tv = new TextView(context);
	        tv.setText(text);
	        tv.setTextSize(12);
        }
	}
}
