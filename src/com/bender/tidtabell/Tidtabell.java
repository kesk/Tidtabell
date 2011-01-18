package com.bender.tidtabell;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Tidtabell extends Activity
{
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
	}
}