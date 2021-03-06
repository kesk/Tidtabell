package com.bender.tidtabell;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class FetchXmlThread extends Thread
{
	public static final int NEXT_TRIP = 0;
	private Handler mHandler;
	private URL mUrl;

	public FetchXmlThread(Handler handler, int service, String identifier,
	        String stopId) throws MalformedURLException
	{
		this.mHandler = handler;

		StringBuilder s = new StringBuilder();
		s.append("http://vasttrafik.se/External_Services/");

		switch (service)
		{
		case NEXT_TRIP:
			s.append("NextTrip.asmx/GetForecast");
			break;
		}

		s.append("?identifier=");
		s.append(identifier);

		s.append("&stopId=");
		s.append(stopId);

		this.mUrl = new URL(s.toString());
	}

	public void run()
	{
		Message msg = mHandler.obtainMessage();

		try
		{
			HttpURLConnection connection = (HttpURLConnection) mUrl
			        .openConnection();

			String s = getXmlData(connection.getInputStream());

			Bundle b = new Bundle();
			b.putString("xml", s);
			msg.setData(b);
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

		mHandler.sendMessage(msg);
	}

	private String getXmlData(InputStream is)
	        throws ParserConfigurationException, IOException, SAXException
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(is);

		return doc.getDocumentElement().getTextContent();
	}
}