package com.amcbridge.jenkins.plugins.xmlSerialization;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("config")
public class Config
{
	@XStreamAsAttribute
	private String builder, platform, userConfig, configuration;
	
	public Config()
	{}
	
	public Config(String configuration, String builder, String platform)
	{
		this.builder = builder;
		this.configuration = configuration;
		this.platform = platform;
	}

	public void setBuilder(String value)
	{
		builder = value;
	}
	
	public void setConfigurator(String value)
	{
		configuration = value;
	}
	
	public void setPlatform(String value)
	{
		platform = value;
	}
	
	public void setUserConfig(String value)
	{
		userConfig = value;
	}
}
