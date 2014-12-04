package com.amcbridge.jenkins.plugins.export;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class PathToArtefacts
{
	@XStreamImplicit(itemFieldName="file")
	private List<String> files;

	public PathToArtefacts()
	{
		files = new ArrayList<String>();
	}

	public void setFiles(List<String> value)
	{
		files = value;
	}

	public List<String> getFiles()
	{
		return files;
	}

	public void addFile(String value)
	{
		files.add(value);
	}
}
