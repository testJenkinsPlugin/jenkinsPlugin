package com.amcbridge.jenkins.plugins.enums;

public enum Platform {
	X86("x86"),
	X64("x64"),
	ANY_CPY("Any CPU"),
	WIN_32("Win32");

	private String platformValue;

	private Platform(String value)
	{ 
		this.platformValue = value; 
	}

	@Override 
	public String toString()
	{ 
		return platformValue; 
	} 
}
