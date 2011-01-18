package com.bender.tidtabell;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Tidtabell extends ListActivity
{
	static final int PROGRESS_DIALOG = 0;
	private FetchXmlThread fetchXmlThread;
	private ProgressDialog mProgressDialog;
	private DepartureListAdapter mListAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mListAdapter = new DepartureListAdapter(getBaseContext());
		setListAdapter(mListAdapter);
		
		//Message handler for the FetchXml thread
		Handler handler = new Handler() {
			public void handleMessage(Message msg)
			{
				String xml = msg.getData().getString("xml");
				mProgressDialog.dismiss();
				
				try
				{
					Vector<Departure> mDepartures = parseXml(xml);
					mListAdapter.updateData(mDepartures);
				}
				catch (MalformedURLException e)
				{
					Log.e("Tidtabell", e.toString());
				}
				catch (IOException e)
				{
					Log.e("Tidtabell", e.toString());
				}
				catch (ParserConfigurationException e)
				{
					Log.e("Tidtabell", e.toString());
				}
				catch (SAXException e)
				{
					Log.e("Tidtabell", e.toString());
				}
			}
		};

		//Get next trip times from Västtrafik
		mProgressDialog = ProgressDialog.show(this, "", "Laddar...", true, true);
		fetchXmlThread = new FetchXmlThread(handler);
		fetchXmlThread.start();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
	}

	private Vector<Departure> parseXml(String xml)
	        throws ParserConfigurationException, SAXException, IOException
	{
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		SAXParser saxParser = saxFactory.newSAXParser();
		NextTripHandler nth = new NextTripHandler();
		
		StringReader sr = new StringReader(xml);
		saxParser.parse(new InputSource(sr), nth);

		return nth.getDepartureList();
	}
}