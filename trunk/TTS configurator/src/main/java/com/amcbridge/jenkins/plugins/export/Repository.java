package com.amcbridge.jenkins.plugins.export;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("repository")
public class Repository
{
	@XStreamAsAttribute
	private String type;

	@XStreamAsAttribute
	private String url;

	public void setType(String value)
	{
		type = value;
	}

	public void setUrl(String value)
	{
		url = value;
	}
}

