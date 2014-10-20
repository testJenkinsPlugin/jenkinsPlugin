package com.amcbridge.jenkins.plugins.controls;

public enum Builder {
	VS_2005("VS 2005"),
	VS_2008("VS 2008"),
	VS_2010("VS 2010"),
	VS_2012("VS 2012"),
	VS_2013("VS 2013"),
	XCODE_4_1("XCode 4.1"),
	JAVA_8("Java 8");

	private String builderValue;

	private Builder(String value)
	{ 
		this.builderValue = value; 
	}

	@Override 
	public String toString()
	{ 
		return builderValue; 
	} 
}
