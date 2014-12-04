package com.amcbridge.jenkins.plugins.controls;

public enum SourceControlTool {
	SVN("SVN"),
	GIT("Git"),
	MERCURIAL("Mercurial");

	private String sctValue;

	private SourceControlTool(String value)
	{ 
		this.sctValue = value; 
	}

	@Override 
	public String toString()
	{ 
		return sctValue; 
	} 
}
