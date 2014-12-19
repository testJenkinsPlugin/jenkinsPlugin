package com.amcbridge.jenkins.plugins.job.ElementDescription;

import org.w3c.dom.Document;

import com.amcbridge.jenkins.plugins.configuration.BuildConfiguration;

public interface JobElementDescription {
	public String getElementTag();
	public String getParentElementTag();
	public String generateXML(BuildConfiguration config);
	public void appendToXML(BuildConfiguration config, Document xml);
}
