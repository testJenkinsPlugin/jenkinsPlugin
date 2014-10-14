package com.amcbridge.jenkins.plugins.configurator;

public enum FormResult {
	CREATE("create"),
	EDIT("edit"),
	APPROVED("approved"),
	REJECT("reject"),
	CANCEL("cancel");

	private String resultValue;

	private FormResult(String value)
	{ 
		this.resultValue = value; 
	}

	@Override 
	public String toString()
	{ 
		return resultValue; 
	} 
}
