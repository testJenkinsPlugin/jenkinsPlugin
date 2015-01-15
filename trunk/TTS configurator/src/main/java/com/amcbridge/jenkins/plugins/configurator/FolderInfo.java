package com.amcbridge.jenkins.plugins.configurator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.amcbridge.jenkins.plugins.TTS.TTSProject;

public class FolderInfo {

	private Map<Integer, TTSProject> folders;

	public FolderInfo()
	{
		folders = new HashMap<Integer, TTSProject>();
	}

	public void add(Integer id, TTSProject project)
	{
		if (folders.containsKey(id))
		{
			folders.remove(id);
		}
		folders.put(id, project);	
	}

	public String getName(Integer id)
	{
		if (folders.containsKey(id))
		{
			return folders.get(id).getName();
		}
		else
		{
			return StringUtils.EMPTY;
		}
	}

	public TTSProject getProject(Integer id)
	{
		if (folders.containsKey(id))
		{
			return folders.get(id);
		}
		return null;
	}

	public Boolean isActual(Integer id)
	{
		TTSProject ttsProject = getProject(id);
		if (ttsProject != null)
		{
			return ttsProject.isActual();
		}
		return false;
	}

	public void remove(Integer id)
	{
		if (folders.containsKey(id))
		{
			folders.remove(id);
		}
	}

	public List<String> getFoldersName()
	{
		List<String> result = new ArrayList<String>();
		for(TTSProject fol : folders.values())
		{
			result.add(fol.getName());
		}
		return result;
	}
}
