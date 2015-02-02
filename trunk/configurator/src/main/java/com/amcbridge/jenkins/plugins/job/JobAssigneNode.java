package com.amcbridge.jenkins.plugins.job;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.amcbridge.jenkins.plugins.configurationModels.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.job.ElementDescription.JobElementDescriptionCheckBox;

public class JobAssigneNode implements JobElementDescriptionCheckBox {

	private static final String ELEMENT_TAG = "assignedNode";
	private static final String PARENT_ELEMENT_TAG = "project";
	private static final String NODE_SEPARATOR = " || ";
	private static final String CHECK_TAG = "canRoam";

	public String getElementTag() {
		return ELEMENT_TAG;
	}

	public String getParentElementTag() {
		return PARENT_ELEMENT_TAG;
	}

	public String generateXML(BuildConfigurationModel config) {
		DocumentBuilderFactory docFactory;
		DocumentBuilder docBuilder;
		Document doc;
		Node node = null;

		if (getNodes(config).isEmpty())
		{
			return StringUtils.EMPTY;
		}

		try
		{
			docFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.newDocument();
			node = doc.createElement(ELEMENT_TAG);
			node.setTextContent(getNodes(config));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return JobManagerGenerator.documentToXML(node);
	}

	public void appendToXML(BuildConfigurationModel config, Document xml) {
		Node node = xml.getElementsByTagName(ELEMENT_TAG).item(0);
		String nodes = getNodes(config);
		if (nodes.isEmpty())
		{
			return;
		}

		if (!node.getTextContent().isEmpty())
		{
			nodes = node.getTextContent() + JobManagerGenerator.COMMA_SEPARATOR + getNodes(config);
		}
		else
		{
			nodes = getNodes(config);
		}
		node.setTextContent(nodes);
	}

	private String getNodes(BuildConfigurationModel config)
	{
		String result = StringUtils.EMPTY;
		if (config.getBuildMachineConfiguration() == null ||
				config.getBuildMachineConfiguration().length == 0)
		{
			return result;
		}

		result = StringUtils.join(config.getBuildMachineConfiguration(), NODE_SEPARATOR);

		return result;
	}

	public void uncheck(Document doc) {
		if (doc.getElementsByTagName(CHECK_TAG).getLength() != 0)
		{
			Node node = doc.getElementsByTagName(CHECK_TAG).item(0);
			node.setTextContent(Boolean.TRUE.toString());
		}
	}

	public void check(Document doc) {
		if (doc.getElementsByTagName(CHECK_TAG).getLength() == 0)
		{
			return;
		}
		Node node = doc.getElementsByTagName(CHECK_TAG).item(0);
		node.setTextContent(Boolean.FALSE.toString());
	}
}
