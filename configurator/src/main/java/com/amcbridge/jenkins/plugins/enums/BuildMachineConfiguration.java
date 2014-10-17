package com.amcbridge.jenkins.plugins.enums;

public enum BuildMachineConfiguration {
	WINDOWS_X86("Windows - x86"),
	WINDOWS_X64("Windows - x64"),
	MACOS("MacOS"),
	LINUX("Linux");

	private String bmcValue;

	private BuildMachineConfiguration(String value)
	{ 
		this.bmcValue = value; 
	}

	@Override 
	public String toString()
	{ 
		return bmcValue; 
	} 
}
