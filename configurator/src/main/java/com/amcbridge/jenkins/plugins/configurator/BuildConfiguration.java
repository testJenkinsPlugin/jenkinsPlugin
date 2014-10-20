package com.amcbridge.jenkins.plugins.configurator;

import hudson.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.amcbridge.jenkins.plugins.controls.*;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import jenkins.model.Jenkins;

@XStreamAlias("buildConfiguration")
@XmlRootElement
public class BuildConfiguration {

	static {Jenkins.XSTREAM.processAnnotations(BuildConfiguration.class);}

	private String creator, projectName, url, sourceControlTool,	buildMachineConfiguration,
	email, configuration, userConfiguration, date;
	private ConfigurationState state;
	private String[] files, artefacts, versionFile, scripts;
	private List<String> builders, platforms;

	public BuildConfiguration()
	{
		setCreator();
		builders = new ArrayList<String>();
		platforms = new ArrayList<String>();
	}

	public void setState(ConfigurationState newState)
	{
		state = newState;
	}

	public String getProjectName()
	{
		return projectName;
	}

	public ConfigurationState getState()
	{
		return state;
	}

	public String getDate()
	{
		return date;
	}

	public void setCurrentDate()
	{
		Date currentDate = new Date();
		date = new SimpleDateFormat(BuildConfigurationManager.DATE_FORMAT).format(currentDate);
	}

	public void setUserConfiguration(String userConfig)
	{
		userConfiguration = userConfig;
	}

	public String getUserConfiguration()
	{
		return userConfiguration;
	}

	public void setConfiguration(String config)
	{
		configuration = config;
	}

	public String getConfiguration()
	{
		return configuration;
	}

	public void setScripts(String[] scriptsValue)
	{
		scripts = scriptsValue;
	}

	public String[] getScripts()
	{
		return scripts;
	}

	public void setVersionFile(String[] versionFileValue)
	{
		versionFile = versionFileValue;
	}

	public String[] getVersionFile()
	{
		return versionFile;
	}

	public void setArtefacts(String[] artefactsValue)
	{
		artefacts = artefactsValue;
	}

	public String[] getArtefacts()
	{
		return artefacts;
	}

	public void setFiles(String[] filesValue)
	{
		files = filesValue;
	}

	public String[] getFiles()
	{
		return files;
	}

	public void addPlatform(Platform platform)
	{
		platforms.add(platform.name());
	}

	public List<String> getPlatforms()
	{
		return platforms;
	}

	public void addBuilders(Builder value)
	{
		builders.add(value.name());
	}

	public List<String> getBuilders()
	{
		return builders;
	}

	public void setEmail(String emailValue)
	{
		email = emailValue;
	}

	public String getEmail()
	{
		return email;
	}

	public void setBuildMachineConfiguration(String config)
	{
		buildMachineConfiguration = config;
	}

	public String getBuildMachineConfiguration()
	{
		return buildMachineConfiguration;
	}

	public void setSourceControlTool(String control)
	{
		sourceControlTool = control;
	}

	public String getSourceControlTool()
	{
		return sourceControlTool;
	}

	public void setProjectName(String name)
	{
		projectName = name;
	}

	public void setUrl(String urlValue)
	{
		url = urlValue;
	}

	public String getUrl()
	{
		return url;
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