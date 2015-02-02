package com.amcbridge.jenkins.plugins.job;

import hudson.tasks.Mailer;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.amcbridge.jenkins.plugins.configurationModels.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.job.ElementDescription.JobElementDescription;

public class JobMailer implements JobElementDescription {

	private static final String ELEMENT_TAG = "hudson.tasks.Mailer";
	private static final String PARENT_ELEMENT_TAG = "publishers";
	private static final String RECIPIENT_TAG = "recipients";

	public String getElementTag() {
		return ELEMENT_TAG;
	}

	public String getParentElementTag() {
		return PARENT_ELEMENT_TAG;
	}

	public String generateXML(BuildConfigurationModel config) {

		if (config.getEmail().isEmpty())
		{
			return StringUtils.EMPTY;
		}

		Mailer mailer = new Mailer();
		mailer.recipients = config.getEmail().trim();

		return JobManagerGenerator.convertToXML(mailer);
	}

	public void appendToXML(BuildConfigurationModel config, Document xml) {
		Node node = xml.getElementsByTagName(RECIPIENT_TAG).item(0);
		String mail;

		if (config.getEmail().isEmpty())
		{
			return;
		}

		if (!node.getTextContent().isEmpty())
		{
			mail = node.getTextContent() + JobManagerGenerator.COMMA_SEPARATOR + config.getEmail();
		}
		else
		{
			mail = config.getEmail();
		}
		node.setTextContent(mail);
	}

}
