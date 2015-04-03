package com.amcbridge.jenkins.plugins.configurationModels;

import hudson.model.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;
import com.amcbridge.jenkins.plugins.enums.ConfigurationState;

public class BuildConfigurationModel
{
	private String projectName, email, creator, date, rejectionReason, scm, configEmail;
	private Boolean isJobUpdate;
	
	private ConfigurationState state;

	private List<ProjectToBuildModel> projectToBuild;
	private String[] scripts, buildMachineConfiguration;

	public BuildConfigurationModel()
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

	public void setScm (String value)
	{
		scm = value;
	}

	public String getScm ()
	{
		return scm;
	}

	public Boolean IsJobUpdate()
	{
		return isJobUpdate;
	}
	
	public void setJobUpdate(Boolean value)
	{
		isJobUpdate = value;
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

	public void setConfigEmail(String value)
	{
		configEmail = value;
	}
	
	public String getConfigEmail()
	{
		return configEmail;
	}
	
	public void setProjectToBuild (List<ProjectToBuildModel> value)
	{
		projectToBuild = value;
	}

	public List<ProjectToBuildModel> getProjectToBuild ()
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
	
	public String getFullNameCreator()
	{
		User user = User.get(creator);
		String fullname = user.getFullName();
		return fullname;
	}
	
	public void setRejectionReason(String reject)
	{
		rejectionReason = reject;
	}
	
	public String getRejectionReason()
	{
		return rejectionReason;
	}
}
