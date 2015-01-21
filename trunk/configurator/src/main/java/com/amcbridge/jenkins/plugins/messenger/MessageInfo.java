package com.amcbridge.jenkins.plugins.messenger;

public interface MessageInfo
{
	public String getSubject();

	public String getDestinationAddress();

	public String getMassageText();
	
	public String getCC();
}
