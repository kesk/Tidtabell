package com.bender.tidtabell;

import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.os.Handler;

public class QueryRunner implements Runnable
{
	public static final byte STATE_NOT_STARTED = 0, STATE_RUNNNING = 1,
	        STATE_DONE = 2;

	public static final int MESSAGE_COMPLETE = 0;

	private boolean mStopThread;

	DefaultHandler mParseHandler;
	Handler mMsgHandler;
	String mAddress;
	byte mStatus = STATE_NOT_STARTED;

	public QueryRunner(DefaultHandler parseHandler, Handler msgHandler, String address)
	{
		mParseHandler = parseHandler;
		mMsgHandler = msgHandler;
		mAddress = address;
	}

	@Override
	public void run()
	{
		mStopThread = false;
		mStatus = STATE_RUNNNING;
		HttpURLConnection connection = null;

		// Connect to Västtrafik
		try
		{
			URL url = new URL(mAddress);
			connection = (HttpURLConnection) url.openConnection();
		}
		catch (MalformedURLException e)
		{
		}
		catch (IOException e)
		{
			// Connection could not be made
			e.printStackTrace();
		}

		try
		{
			String xml = Tidtabell.getXmlData(connection.getInputStream());

			// Parse the xml
			SAXParserFactory saxFactory = SAXParserFactory.newInstance();
			SAXParser saxParser = saxFactory.newSAXParser();
			StringReader sr = new StringReader(xml);
			saxParser.parse(new InputSource(sr), mParseHandler);
		}
		catch (ParserConfigurationException e)
		{
		}
		catch (IOException e)
		{
			// Something went wrong with the IO during parse
			e.printStackTrace();
		}
		catch (SAXException e)
		{
			// Something went wrong with the parse
			e.printStackTrace();
		}

		if (!mStopThread)
		{
			mMsgHandler.sendEmptyMessage(MESSAGE_COMPLETE);
			mStatus = STATE_DONE;
		}
	}

	public void setMsgHandler(Handler handler)
	{
		mMsgHandler = handler;
	}

	public byte getStatus()
	{
		return mStatus;
	}

	public DefaultHandler getResult()
	{
		return mParseHandler;
	}

	public void stopThread()
	{
		mStopThread = true;
	}
}
