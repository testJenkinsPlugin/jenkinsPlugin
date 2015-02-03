package com.amcbridge.jenkins.plugins.job.ElementDescription;

import org.w3c.dom.Document;

import com.amcbridge.jenkins.plugins.configurationModels.BuildConfigurationModel;

public interface JobElementDescription {
	public String getElementTag();
	public String getParentElementTag();
	public String generateXML(BuildConfigurationModel config);
	public void appendToXML(BuildConfigurationModel config, Document xml);
}
