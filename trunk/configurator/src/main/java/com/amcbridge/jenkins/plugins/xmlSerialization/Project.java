package com.amcbridge.jenkins.plugins.xmlSerialization;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("project")
public class Project
{
	@XStreamAsAttribute
	private String pathToFile, baseProjectFolder;

	private Repository repository;

	private PathToArtefacts pathToArtefacts;
	private VersionFile versionFiles;
	private List<Config> configs;

	public Project()
	{
		configs = new ArrayList<Config>();
	}
	
	public void setPathToArtefacts(PathToArtefacts value)
	{
		pathToArtefacts = value;
	}

	public PathToArtefacts getPathToArtefacts()
	{
		return pathToArtefacts;
	}
	
	public void setPathToFile(String value)
	{
		pathToFile = value;
	}
	
	public String getPathToFile()
	{
		return pathToFile;
	}

	public String getBaseProjectFolder() {
		return baseProjectFolder;
	}

	public void setBaseProjectFolder(String value) {
		baseProjectFolder = value;
	}

	public void setRepository(Repository value)
	{
		repository = value;
	}
	
	public Repository getRepository()
	{
		return repository;
	}

	public void setVersionFiles(VersionFile value)
	{
		versionFiles = value;
	}
	
	public VersionFile getVersionFiles()
	{
		return versionFiles;
	}

	public void setConfigs(List<Config> value)
	{
		configs = value;
	}
	
	public List<Config> getConfigs()
	{
		return configs;
	}
	
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if((obj == null) || !(obj instanceof Project))
			return false;
		Project other = (Project)obj;
		if((this.baseProjectFolder == null && other.getBaseProjectFolder() != null) ||
			(this.baseProjectFolder != null && other.getBaseProjectFolder() == null))
			return false;
		return (this.pathToFile.equals(other.getPathToFile()) &&
				((this.baseProjectFolder == null && other.getBaseProjectFolder() == null) 
					|| this.baseProjectFolder.equals(other.getBaseProjectFolder())) &&
				this.repository.equals(other.getRepository()) &&
				this.pathToArtefacts.equals(other.getPathToArtefacts()) &&
				this.versionFiles.equals(other.getVersionFiles()) &&
				this.configs.containsAll(other.getConfigs()) &&
				other.getConfigs().containsAll(this.configs));
	}
	
	
}
