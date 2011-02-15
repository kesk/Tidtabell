package com.bender.tidtabell;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class NextTrip extends ListActivity
{
	public static final int DIALOG_PROGRESS = 0, DIALOG_ERROR = 1;
	public static final String NEXT_TRIP_URL = "http://vasttrafik.se/External_Services/NextTrip.asmx/GetForecast?identifier="
	        + Tidtabell.IDENTIFIER;

	private DatabaseOpenHelper mDb;
	private Cursor mDbCursor;
	private ToggleButton mFavToggle;
	private DepartureListAdapter mListAdapter;
	private Stop mStop;
	private Departure[] mDepartures = new Departure[0];
	private QueryRunner mQueryRunner;
	private final PeriodicUpdates mPeriodicUpdates = new PeriodicUpdates();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle(R.string.title_next_trip);
		setContentView(R.layout.next_trip);

		// This activity needs a stop to show, stop if none was sent with the
		// intent.
		Bundle b = getIntent().getExtras();
		if (b != null)
			mStop = (Stop) b.getSerializable("stop");
		else
			finish();

		mDb = new DatabaseOpenHelper(this);

		// If there is a previously started search thread, update its
		// message handler or get result if it has finished.
		if (getLastNonConfigurationInstance() != null)
		{
			mQueryRunner = (QueryRunner) getLastNonConfigurationInstance();
			switch (mQueryRunner.getStatus())
			{
			case RUNNING:
				mQueryRunner.msgHandler = mMsgHandler;
				break;
			case FINISHED:
				NextTripHandler h = (NextTripHandler) mQueryRunner.getResult();
				mDepartures = h.getDepartureList();
				break;
			default:
				break;
			}
		}
		// If this is a restored activity show departures saved from that
		// instance.
		else if (savedInstanceState != null)
		{
			mDepartures = (Departure[]) savedInstanceState
			        .getSerializable("departures");
		}
		// Otherwise this is a new activity
		else
		{
			runQuery();
		}

		mListAdapter = new DepartureListAdapter(this, mDepartures);
		setListAdapter(mListAdapter);

		// Set the name of the stop at the top
		TextView tv = (TextView) findViewById(R.id.stop_name);
		tv.setText(mStop.getName());

		// Setup favorite (star) button
		mFavToggle = (ToggleButton) findViewById(R.id.favorite_button);
		mDbCursor = mDb.getFavorite(mStop.getId());

		// If this station is a favorite set star to checked
		mFavToggle.setChecked(mDbCursor.moveToFirst());

		// Click listener for favorite toggle
		mFavToggle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				DatabaseOpenHelper db = new DatabaseOpenHelper(NextTrip.this);

				if (mFavToggle.isChecked())
				{
					Toast.makeText(NextTrip.this, R.string.add_favorite_toast,
					        Toast.LENGTH_SHORT).show();
					db.addFavorite(mStop);
				}
				else
				{
					Toast.makeText(NextTrip.this,
					        R.string.remove_favorite_toast, Toast.LENGTH_SHORT)
					        .show();
					db.removeFavorite(mStop);
				}
			}
		});
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		mDbCursor.requery();
		mFavToggle.setChecked(mDbCursor.moveToFirst());
		
		//Start the periodic updater
		new Thread(mPeriodicUpdates).start();
	}

	@Override
	protected void onPause()
	{
		mDbCursor.deactivate();
		
		// Stop periodic updates
		mPeriodicUpdates.stopThread = true;
		super.onPause();
	}

	@Override
	protected void onDestroy()
	{
		mDb.close();
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		if (mDepartures != null)
			outState.putSerializable("departures", mDepartures);

		super.onSaveInstanceState(outState);
	}

	@Override
	public Dialog onCreateDialog(int id, Bundle bundle)
	{
		ProgressDialog dialog;

		switch (id)
		{
		case DIALOG_PROGRESS:
			dialog = new ProgressDialog(this);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setMessage(getString(R.string.loading_dialog));
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog)
				{
					mQueryRunner.stopThread();
					finish();
				}
			});
			return dialog;
		case DIALOG_ERROR:
			int errMsg = bundle.getInt("message");
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Error")
			        .setMessage(errMsg)
			        .setNeutralButton(R.string.ok,
			                new DialogInterface.OnClickListener() {
				                @Override
				                public void onClick(DialogInterface dialog,
				                        int which)
				                {
					                dismissDialog(DIALOG_ERROR);
					                finish();
				                }
			                });
			return builder.create();
		default:
			return null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.next_trip, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_home:
			Intent intent = new Intent(this, Tidtabell.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		case R.id.menu_refresh:
			runQuery();
			return true;
		case R.id.menu_search:
			onSearchRequested();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance()
	{
		return mQueryRunner;
	}

	final Handler mMsgHandler = new Handler() {
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case QueryRunner.MSG_COMPLETE:
				dismissDialog(DIALOG_PROGRESS);
				NextTripHandler h = (NextTripHandler) mQueryRunner.getResult();
				mDepartures = h.getDepartureList();
				mListAdapter.updateData(mDepartures);
				break;
			default:
				dismissDialog(DIALOG_PROGRESS);
				Bundle b = new Bundle();
				b.putInt("message", msg.arg1);
				showDialog(DIALOG_ERROR, b);
				break;
			}
		}
	};
	
	// Periodic updates of the list
	private class PeriodicUpdates implements Runnable
	{
		volatile boolean stopThread = false;
		
		@Override
		public void run()
		{
			while (!stopThread)
			{
				try
				{
					Thread.sleep(20 * 1000);
					mListAdapter.notifyDataSetChanged();
				}
				catch (InterruptedException e)
				{
				}
			}
		}
	}

	private void runQuery()
	{
		String url = NEXT_TRIP_URL + "&stopId=" + mStop.getId();

		showDialog(DIALOG_PROGRESS, null);
		mQueryRunner = new QueryRunner(new NextTripHandler(), mMsgHandler, url);
		new Thread(mQueryRunner).start();
	}

	private void saveToFile(String s)
	{
		String FILENAME = "xml_debug.txt";

		try
		{
			FileOutputStream fos = openFileOutput(FILENAME,
			        Context.MODE_PRIVATE);
			fos.write(s.getBytes());
			fos.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}