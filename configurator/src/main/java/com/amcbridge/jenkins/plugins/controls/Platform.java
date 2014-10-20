package com.amcbridge.jenkins.plugins.controls;

public enum Platform {
	X86("x 86"),
	X64("x 64"),
	ANY_CPY("Any Cpu"),
	WIN_32("Win 32");

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
