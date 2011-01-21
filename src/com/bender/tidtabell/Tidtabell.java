package com.bender.tidtabell;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Tidtabell extends Activity
{
	public static final String IDENTIFIER = "1d1b034c-b4cc-49ec-a69e-70b91f5fb325";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		Button button = (Button) findViewById(R.id.button);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), NextTrip.class);
				startActivity(i);
			}
		});
		
		Button b2 = (Button) findViewById(R.id.search_button);
		b2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), StopSearch.class);
				startActivity(i);
			}
		});
	}
}