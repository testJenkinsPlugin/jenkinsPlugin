package com.amcbridge.jenkins.plugins.configurator;

public enum Configuration {
	RELEASE("Release"),
	DEBUG("Debug"),
	OTHER("Other");

	private String configurationValue;

	private Configuration(String value)
	{ 
		this.configurationValue = value; 
	}

	@Override 
	public String toString()
	{ 
		return configurationValue; 
	} 
}
