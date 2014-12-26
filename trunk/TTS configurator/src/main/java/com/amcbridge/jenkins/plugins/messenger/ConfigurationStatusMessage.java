package com.amcbridge.jenkins.plugins.messenger;

public class ConfigurationStatusMessage implements MessageInfo
{
	private String subject, sendTo, description, projectName;

	public ConfigurationStatusMessage (String subject,
			String sendTo, String description, String projectName)
	{
		this.subject = subject;
		this.sendTo = sendTo;
		this.description = description;
		this.projectName = projectName;
	}

	public ConfigurationStatusMessage (String projectName)
	{
		this.projectName = projectName;
	}

	public String getProjectName()
	{
		return projectName;
	}

	public void setProjectName(String value)
	{
		projectName = value;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String value) {
		subject = value;
	}

	public String getDestinationAddress() {
		return sendTo;
	}

	public void setDestinationAddress(String value) {
		sendTo = value;
	}

	public void setDescription(String value) {
		description = value;
	}

	public String getMassageText() {
		return projectName + " - " + description;
	}
}
