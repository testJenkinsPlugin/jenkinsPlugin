package com.amcbridge.jenkins.plugins.configurator;

import java.io.IOException;

import javax.servlet.ServletException;

public class ConfigurationInfo {

	private String projectName, date;
	private ConfigurationState state;

	public void setState (ConfigurationState value)
	{
		state = value;
	}

	public ConfigurationState getState()
	{
		return state;
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
		BuildConfiguration conf = BuildConfigurationManager.load(nameConfig);
		setState(conf.getState());
		setProjectName(conf.getProjectName());
		setDate(conf.getDate());
	}
}
