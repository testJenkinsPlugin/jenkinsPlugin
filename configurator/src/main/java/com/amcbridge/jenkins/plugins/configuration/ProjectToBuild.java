package com.amcbridge.jenkins.plugins.configuration;

import org.kohsuke.stapler.DataBoundConstructor;

import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;
import com.amcbridge.jenkins.plugins.controls.SourceControlTool;

public class ProjectToBuild {

	private String projectUrl, fileToBuild;

	private Boolean isVersionFiles;

	private BilderConfig[] builders;
	private String[] artefacts, versionFiles;

	private SourceControlTool type;

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

	public void setBuilders (BilderConfig[] value)
	{
		builders = value;
	}

	public BilderConfig[] getBuilders ()
	{
		return builders;
	}

	public void setType (SourceControlTool value)
	{
		type = value;
	}

	public SourceControlTool getType ()
	{
		return type;
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
	public ProjectToBuild(String projectUrl, String fileToBuild,
			String type, String artefacts, String versionFiles,
			Boolean isVersionFiles, BilderConfig[] builders)
	{
		this.projectUrl = projectUrl;
		this.fileToBuild = fileToBuild;
		this.isVersionFiles = isVersionFiles;
		if (!type.equals(BuildConfigurationManager.STRING_EMPTY))
		{
			this.type = SourceControlTool.valueOf(type);
		}
		setArtefacts(BuildConfigurationManager.getPath(artefacts));
		setVersionFiles(BuildConfigurationManager.getPath(versionFiles));
		this.builders = builders;
	}
}
