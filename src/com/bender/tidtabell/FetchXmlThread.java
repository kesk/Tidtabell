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
	private Handler mHandler;

	public FetchXmlThread(Handler handler)
	{
		this.mHandler = handler;
	}

	public void run()
	{
		Message msg = mHandler.obtainMessage();
		
		try
		{
			URL url = new URL(
			        "http://vasttrafik.se/External_Services/"
			                + "NextTrip.asmx/GetForecast"
			                + "?identifier=1d1b034c-b4cc-49ec-a69e-70b91f5fb325"
			                + "&stopId=00007171");

			HttpURLConnection connection = (HttpURLConnection) url
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