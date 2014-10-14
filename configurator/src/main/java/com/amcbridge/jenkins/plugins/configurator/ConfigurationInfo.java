package com.amcbridge.jenkins.plugins.configurator;

import java.io.IOException;

import javax.servlet.ServletException;

public class ConfigurationInfo {

	private String state, projectName, date;

	public void setState (String value)
	{
		state = value;
	}

	public ConfigurationState getState()
	{
		for (ConfigurationState configStates : ConfigurationState.values()) 
		{
			if (configStates.toString().equals(state))
				return configStates;
		}
		return null;
	}

	public void setProjectName (String value)
	{
		projectName = value;
	}

	public String getProjectName()
	{
		return projectName;
	}

	public void setDate (String value)
	{
		date = value;
	}

	public String getDate()
	{
		return date;
	}

	public ConfigurationInfo (String nameConfig) throws IOException, ServletException
	{
		BuildConfiguration conf = BuildConfiguration.load(nameConfig);
		setState(conf.getState());
		setProjectName(conf.getProjectName());
		setDate(conf.getDate());
	}
}
