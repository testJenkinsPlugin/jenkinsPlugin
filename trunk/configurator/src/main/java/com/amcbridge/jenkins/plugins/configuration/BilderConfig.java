package com.amcbridge.jenkins.plugins.configuration;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

import com.amcbridge.jenkins.plugins.controls.Configuration;

public class BilderConfig {

	private String builder, platform, userConfig;

	private List<Configuration> configs;

	@DataBoundConstructor
	public BilderConfig(String builder, String platform, String userConfig,
			Boolean release, Boolean debug, Boolean other)
	{
		this.configs = new ArrayList<Configuration>();

		this.builder = builder;
		this.platform = platform;
		this.userConfig = userConfig;
		if (release)
			configs.add(Configuration.RELEASE);
		if (debug)
			configs.add(Configuration.DEBUG);
		if (other)
			configs.add(Configuration.OTHER);
	}

	public List<Configuration> getConfigs()
	{
		return configs;
	}

	public Boolean isConfigChecked(String value)
	{
		Configuration conf = Configuration.valueOf(value);
		if (configs.indexOf(conf)!=-1)
			return true;
		else
			return false;
	}

	public void setBuilder (String value)
	{
		builder = value;
	}

	public String getBuilder ()
	{
		return builder;
	}

	public void setPlatform (String value)
	{
		platform = value;
	}

	public String getPlatform ()
	{
		return platform;
	}

	public void setUserConfig (String value)
	{
		userConfig = value;
	}

	public String getUserConfig ()
	{
		return userConfig;
	}
}
