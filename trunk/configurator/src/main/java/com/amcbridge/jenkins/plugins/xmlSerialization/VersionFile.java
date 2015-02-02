package com.amcbridge.jenkins.plugins.xmlSerialization;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class VersionFile
{
	@XStreamImplicit(itemFieldName="file")
	private List<String> files;

	@XStreamAsAttribute
	private Boolean versionFile;

	public VersionFile()
	{
		files = new ArrayList<String>();
		versionFile = false;
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

	public void setIsVersionFile(Boolean value)
	{
		versionFile = value;
	}

	public Boolean isVersionFile()
	{
		return versionFile;
	}
}
