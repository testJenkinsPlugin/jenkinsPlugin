package com.amcbridge.jenkins.plugins.enums;

public enum ConfigurationState {
	NEW("New", "new"),
	UPDATED("Updated", "updated"),
	APPROVED("Approved", "approved"),
	REJECTED("Rejected", "rejected"),
	FOR_DELETION("For Deletion", "for-deletion");

	private String Value;
	private String CSSClassName;

	private ConfigurationState(String value, String cssClass)
	{ 
		this.Value = value; 
		this.CSSClassName = cssClass;
	}

	@Override 
	public String toString()
	{ 
		return Value; 
	}

	public String getCSSClassName()
	{
		return CSSClassName;
	}
}