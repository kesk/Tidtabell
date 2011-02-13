package com.bender.tidtabell;

import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.os.Handler;
import android.os.Message;

public class QueryRunner implements Runnable
{
	public static enum Status
	{
		PENDING, RUNNING, FINISHED;
	}

	public static final int MSG_COMPLETE = 0, MSG_CLIENT_TIMEOUT = 1,
	        MSG_IO_ERROR = 2, MSG_PARSE_ERROR = 3, MSG_UNKNOWN_ERROR = 4;

	public volatile Handler msgHandler;

	private boolean mStopThread;

	DefaultHandler mParseHandler;
	String mAddress;
	Status mStatus = Status.PENDING;

	public QueryRunner(DefaultHandler parseHandler, Handler msgHandler,
	        String address)
	{
		mParseHandler = parseHandler;
		this.msgHandler = msgHandler;
		mAddress = address;
	}

	@Override
	public void run()
	{
		mStopThread = false;
		mStatus = Status.RUNNING;
		HttpURLConnection connection = null;
		Message msg = new Message();

		try
		{
			// Connect to Västtrafik
			URL url = new URL(mAddress);
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(15 * 1000);

			switch (connection.getResponseCode())
			{
			case HttpURLConnection.HTTP_OK:
				String xml = Tidtabell.getXmlData(connection.getInputStream());

				// Parse the xml
				SAXParserFactory saxFactory = SAXParserFactory.newInstance();
				SAXParser saxParser = saxFactory.newSAXParser();
				StringReader sr = new StringReader(xml);
				saxParser.parse(new InputSource(sr), mParseHandler);
				break;
			case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
				msg.what = MSG_CLIENT_TIMEOUT;
				msg.arg1 = R.string.query_runner_timeout;
				msgHandler.sendMessage(msg);
				break;
			default:
				msg.what = MSG_UNKNOWN_ERROR;
				msg.arg1 = R.string.query_runner_unknown;
				msgHandler.sendMessage(msg);
			}

			if (!mStopThread)
			{
				msgHandler.sendEmptyMessage(MSG_COMPLETE);
				mStatus = Status.FINISHED;
			}
		}
		catch (ParserConfigurationException e)
		{
		}
		catch (IOException e)
		{
			// Something went wrong with the InputStream
			msg.what = MSG_IO_ERROR;
			msg.arg1 = R.string.query_runner_io_error;
			msgHandler.sendMessage(msg);
		}
		catch (SAXException e)
		{
			// Something went wrong with the parse
			msg.what = MSG_PARSE_ERROR;
			msg.arg1 = R.string.query_runner_parse_error;
			msgHandler.sendMessage(msg);
		}
	}

	public Status getStatus()
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
