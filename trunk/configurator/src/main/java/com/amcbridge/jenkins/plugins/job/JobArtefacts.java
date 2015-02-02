package com.amcbridge.jenkins.plugins.job;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import hudson.tasks.ArtifactArchiver;

import com.amcbridge.jenkins.plugins.configurationModels.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.configurationModels.ProjectToBuildModel;
import com.amcbridge.jenkins.plugins.job.ElementDescription.JobElementDescription;

public class JobArtefacts implements JobElementDescription {

	private static final String ELEMENT_TAG = "hudson.tasks.ArtifactArchiver";
	private static final String PARENT_ELEMENT_TAG = "publishers";
	private static final String PATH_TAG = "artifacts";

	public String getElementTag() {
		return ELEMENT_TAG;
	}

	public String getParentElementTag() {
		return PARENT_ELEMENT_TAG;
	}

	public String generateXML(BuildConfigurationModel config) {
		String artf = getArtifacts(config);
		if (artf.isEmpty())
		{
			return StringUtils.EMPTY;
		}
		ArtifactArchiver artifact = new ArtifactArchiver(artf, StringUtils.EMPTY, false);

		return JobManagerGenerator.convertToXML(artifact);
	}

	public void appendToXML(BuildConfigurationModel config, Document xml)
	{
		Node node = xml.getElementsByTagName(PATH_TAG).item(0);
		String artf = getArtifacts(config);
		String artifacts;

		if (artf.isEmpty())
		{
			return;
		}

		if (!node.getTextContent().isEmpty())
		{
			artifacts = node.getTextContent() + JobManagerGenerator.COMMA_SEPARATOR + getArtifacts(config);
		}
		else
		{
			artifacts = getArtifacts(config);
		}
		node.setTextContent(artifacts);
	}

	private String getArtifacts(BuildConfigurationModel config)
	{
		String result = StringUtils.EMPTY;
		String local;
		if (config.getProjectToBuild() == null)
		{
			return result;
		}

		for(ProjectToBuildModel artf : config.getProjectToBuild())
		{
			local = StringUtils.join(artf.getArtefacts(), JobManagerGenerator.COMMA_SEPARATOR);
			if (result.length() > 0 && local.length() > 0)
			{
				result += JobManagerGenerator.COMMA_SEPARATOR + local;
			}
			else
			{
				result += local;
			}
		}
		return result;
	}
}