package com.amcbridge.jenkins.plugins.vsc;

public enum CommitError
{
	NONE_PROPERTY("Please set all information, in Jenkins manage, for exporting XML file to SVN repository."),
	FAIL("Cannot connect with SVN repository. Please check your settings in manage Jenkins.");

	private String bmcValue;

	private CommitError(String value)
	{ 
		this.bmcValue = value; 
	}

	@Override 
	public String toString()
	{ 
		return bmcValue; 
	} 
}
