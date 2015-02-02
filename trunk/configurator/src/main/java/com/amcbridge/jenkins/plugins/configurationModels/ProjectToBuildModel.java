package com.amcbridge.jenkins.plugins.configurationModels;

import org.kohsuke.stapler.DataBoundConstructor;

import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;

public class ProjectToBuildModel {

	private String projectUrl, fileToBuild, projectFolderPath;

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

	public void setProjectFolderPath(String value)
	{
		projectFolderPath = value;
	}

	public String getProjectFolderPath()
	{
		return projectFolderPath;
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
	public ProjectToBuildModel(String projectUrl, String fileToBuild,
			String artefacts, String versionFiles, String projectFolderPath,
			Boolean isVersionFiles, BuilderConfigModel[] builders)
	{
		this.projectUrl = projectUrl;
		this.fileToBuild = fileToBuild;
		this.isVersionFiles = isVersionFiles;
		this.projectFolderPath = projectFolderPath;
		setArtefacts(BuildConfigurationManager.getPath(artefacts));
		setVersionFiles(BuildConfigurationManager.getPath(versionFiles));
		this.builders = builders;
	}
}
