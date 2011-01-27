package com.bender.tidtabell;

import java.io.Serializable;

import android.database.sqlite.SQLiteDatabase;

public class Stop implements Serializable
{
	private static final long serialVersionUID = 2466085735607712099L;
	
	String id, name, friendlyName, county;

	public String getCounty()
    {
    	return county;
    }

	public void setCounty(String county)
    {
    	this.county = county;
    }

	public String getFriendlyName()
    {
    	return friendlyName;
    }

	public void setFriendlyName(String friendlyName)
    {
    	this.friendlyName = friendlyName;
    }

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
