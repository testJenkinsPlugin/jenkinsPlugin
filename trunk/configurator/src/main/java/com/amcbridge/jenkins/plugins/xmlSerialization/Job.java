package com.amcbridge.jenkins.plugins.xmlSerialization;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("job")
public class Job
{
	@XStreamAsAttribute
	private String name;
	
	private List<Project> projects;
	
	private String[] scripts, buildMachineConfiguration;
	
	public Job(String name)
	{
		this.name = name;
	}
	
	public void setName(String value)
	{
		name = value;
	}

	public void setProjects(List<Project> value)
	{
		projects = value;
	}
	
	public void setBuildMachineConfiguration(String[] value)
	{
		buildMachineConfiguration = value;
	}
	
	public void setScripts(String[] value)
	{
		scripts = value;
	}
}
