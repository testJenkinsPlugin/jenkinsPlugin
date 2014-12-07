package com.amcbridge.jenkins.plugins.configuration;

import hudson.model.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;
import com.amcbridge.jenkins.plugins.configurator.ConfigurationState;

public class BuildConfiguration
{
	private String projectName, email, creator, date, type;

	private ConfigurationState state;

	private List<ProjectToBuild> projectToBuild;
	private String[] scripts, buildMachineConfiguration;

	public BuildConfiguration()
	{
		setCreator();
	}

	public void setCurrentDate()
	{
		DateFormat df = new SimpleDateFormat(BuildConfigurationManager.DATE_FORMAT);
		Date dateobj = new Date();
		date = df.format(dateobj);	
	}

	public String getDate()
	{
		return date;
	}

	public void setProjectName (String value)
	{
		projectName = value;
	}

	public String getProjectName()
	{
		return projectName;
	}

	public void setType (String value)
	{
		type = value;
	}

	public String getType ()
	{
		return type;
	}
	
	public void setBuildMachineConfiguration (String[] value)
	{
		buildMachineConfiguration = value;
	}

	public String[] getBuildMachineConfiguration ()
	{
		return buildMachineConfiguration;
	}

	public void setEmail (String value)
	{
		email = value;
	}

	public String getEmail ()
	{
		return email;
	}

	public void setProjectToBuild (List<ProjectToBuild> value)
	{
		projectToBuild = value;
	}

	public List<ProjectToBuild> getProjectToBuild ()
	{
		return projectToBuild;
	}

	public void setScripts (String[] value)
	{
		scripts = value;
	}

	public String[] getScripts ()
	{
		return scripts;
	}

	public void setState (ConfigurationState value)
	{
		state = value;
	}

	public ConfigurationState getState ()
	{
		return state;
	}

	void setCreator()
	{
		if (User.current() != null)
			creator = User.current().getId();
		else
			creator = BuildConfigurationManager.STRING_EMPTY;
	}

	public void setCreator(String value)
	{
		creator = value;
	}

	public String getCreator()
	{
		return creator;
	}
}
