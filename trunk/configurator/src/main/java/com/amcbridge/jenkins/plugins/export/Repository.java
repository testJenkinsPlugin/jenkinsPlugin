package com.amcbridge.jenkins.plugins.export;

import com.amcbridge.jenkins.plugins.controls.SourceControlTool;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("repository")
public class Repository
{
	@XStreamAsAttribute
	private SourceControlTool type;

	@XStreamAsAttribute
	private String url;

	public void setType(SourceControlTool value)
	{
		type = value;
	}

	public void setUrl(String value)
	{
		url = value;
	}
}

