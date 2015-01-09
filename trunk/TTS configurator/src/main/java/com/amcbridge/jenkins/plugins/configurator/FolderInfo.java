package com.amcbridge.jenkins.plugins.configurator;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class FolderInfo {

	private Map<Integer, String> folders;

	public FolderInfo()
	{
		folders = new HashMap<Integer, String>();
	}

	public void add(Integer id, String name)
	{
		if (folders.containsKey(id))
		{
			folders.remove(id);
		}
		folders.put(id, name);	
	}

	public String getName(Integer id)
	{
		if (folders.containsKey(id))
		{
			return folders.get(id);
		}
		else
		{
			return StringUtils.EMPTY;
		}
	}

	public Integer getID(String name)
	{
		for (Integer key : folders.keySet())
		{
			if (folders.get(key).equals(name))
			{
				return key;
			}
		}
		return null;
	}

	public void remove(Integer id)
	{
		if (folders.containsKey(id))
		{
			folders.remove(id);
		}
	}
}
