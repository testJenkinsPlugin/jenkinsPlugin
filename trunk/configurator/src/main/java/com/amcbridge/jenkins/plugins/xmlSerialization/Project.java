package com.amcbridge.jenkins.plugins.xmlSerialization;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("project")
public class Project
{
	@XStreamAsAttribute
	private String pathToFile;

	private Repository repository;

	private PathToArtefacts pathToArtefacts;
	private VersionFile versionFiles;
	private List<Config> configs;

	public void setPathToArtefacts(PathToArtefacts value)
	{
		pathToArtefacts = value;
	}

	public void setPathToFile(String value)
	{
		pathToFile = value;
	}

	public void setRepository(Repository value)
	{
		repository = value;
	}

	public void setVersionFiles(VersionFile value)
	{
		versionFiles = value;
	}

	public void setConfigs(List<Config> value)
	{
		configs = value;
	}
}
