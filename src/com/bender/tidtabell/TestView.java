package com.bender.tidtabell;

import android.app.Activity;
import android.os.Bundle;

public class TestView extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		DirectionNeedle d = new DirectionNeedle(this);
		
		setContentView(d);
		//setContentView(R.layout.test_layout);
	}
}
