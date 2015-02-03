package com.amcbridge.jenkins.plugins.enums;

public enum SCM {
	SUBVERSION("Subversion"),
	NONE("None");

	private String scm;

	private SCM(String value)
	{ 
		this.scm = value; 
	}

	@Override 
	public String toString()
	{ 
		return scm; 
	} 
}
