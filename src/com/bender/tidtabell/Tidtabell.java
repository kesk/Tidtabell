package com.bender.tidtabell;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.sax.Element;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Tidtabell extends ListActivity
{
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		try {
	    	URL url = new URL("http://vasttrafik.se/External_Services/NextTrip.asmx/" +
					"GetForecast" +
					"?identifier=1d1b034c%2db4cc%2d49ec%2da69e%2d70b91f5fb325" +
					"&stopId=00007171");

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        	
            StringReader sr = getXmlData(connection.getInputStream());
            
            SAXParserFactory saxFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxFactory.newSAXParser();
            NextTripHandler nth = new NextTripHandler();
            saxParser.parse(new InputSource(sr), nth);
            
//            Vector<Departure> departureList = nth.getDepartureList();
//            if (departureList.size() > 0) {
//	            departureList.get(0);
//	            TextView tv = (TextView) findViewById(R.id.hello);
//	            tv.setText(departureList.get(0).getDestination());
//            }
            
            DepartureListAdapter listAdapter = new DepartureListAdapter(getBaseContext(), nth.getDepartureList());
            setListAdapter(listAdapter);
            
		} catch (MalformedURLException e) {
			Log.e("Tidtabell", e.toString());
		} catch (IOException e) {
			Log.e("Tidtabell", e.toString());
		} catch (ParserConfigurationException e) {
			Log.e("Tidtabell", e.toString());
		} catch (SAXException e) {
			Log.e("Tidtabell", e.toString());
		}
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
    	super.onSaveInstanceState(outState);
    }
    
    private StringReader getXmlData(InputStream is) throws ParserConfigurationException, IOException, SAXException {
    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	DocumentBuilder db = dbf.newDocumentBuilder();
    	Document doc = db.parse(is);
    	
    	return new StringReader(doc.getDocumentElement().getTextContent());
    }
}