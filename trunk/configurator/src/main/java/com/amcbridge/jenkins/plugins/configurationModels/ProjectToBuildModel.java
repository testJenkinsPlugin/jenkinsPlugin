package com.amcbridge.jenkins.plugins.configurationModels;

import org.kohsuke.stapler.DataBoundConstructor;

import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;

public class ProjectToBuildModel {

	private String projectUrl, fileToBuild, localDirectoryPath, branchName;
	private String preScript, postScript;

	private Boolean isVersionFiles;

	private BuilderConfigModel[] builders;
	private String[] artefacts, versionFiles;

	public void setProjectUrl (String value)
	{
		projectUrl = value;
	}

	public String getProjectUrl ()
	{
		return projectUrl;
	}
        

	public void setBranchName (String value)
	{
		branchName = value;
	}

	public String getBranchName ()
	{
		return branchName;
	}                
        

	public void setPreScript (String value)
	{
		preScript = value;
	}

	public String getPreScript ()
	{
		return preScript;
	}            
        
	public void setPostScript (String value)
	{
		postScript = value;
	}

	public String getPostScript ()
	{
		return postScript;
	}            
        
                
	public Boolean IsVersionFiles()
	{
		return isVersionFiles;
	}

	public void setFileToBuild (String value)
	{
		fileToBuild = value;
	}

	public String getFileToBuild ()
	{
		return fileToBuild;
	}

	public void setLocalDirectoryPath(String value)
	{
		localDirectoryPath = value;
	}

	public String getLocalDirectoryPath()
	{
		return localDirectoryPath;
	}

	public void setBuilders (BuilderConfigModel[] value)
	{
		builders = value;
	}

	public BuilderConfigModel[] getBuilders ()
	{
		return builders;
	}

	public String[] getArtefacts ()
	{
		return artefacts;
	}

	public void setArtefacts (String[] values)
	{
		artefacts = values;
	}

	public void setVersionFiles (String[] values)
	{
		versionFiles = values;
	}

	public String[] getVersionFiles ()
	{
		return versionFiles;
	}

	@DataBoundConstructor
	public ProjectToBuildModel(String projectUrl, String branchName, String fileToBuild,
			String artefacts, String versionFiles, String localDirectoryPath,
			Boolean isVersionFiles, BuilderConfigModel[] builders)
	{
		this.projectUrl = projectUrl;
                this.branchName = branchName;
		this.fileToBuild = fileToBuild;
		this.isVersionFiles = isVersionFiles;
		this.localDirectoryPath = localDirectoryPath;
		setArtefacts(BuildConfigurationManager.getPath(artefacts));
		setVersionFiles(BuildConfigurationManager.getPath(versionFiles));
		this.builders = builders;
	}
}
