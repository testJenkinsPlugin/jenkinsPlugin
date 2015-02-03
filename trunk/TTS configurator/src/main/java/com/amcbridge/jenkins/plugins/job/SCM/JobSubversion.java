package com.amcbridge.jenkins.plugins.job.SCM;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.amcbridge.jenkins.plugins.configurationModels.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.job.JobManagerGenerator;
import com.amcbridge.jenkins.plugins.job.JobSCM;
import com.amcbridge.jenkins.plugins.job.ElementDescription.JobElementDescription;

public class JobSubversion implements JobElementDescription {

	private static final String URL_TAG = "remote";
	private static final String LOCAL_TAG = "local";
	private static final String MODULE_TAG = "hudson.scm.SubversionSCM_-ModuleLocation";
	private static final String DEFAULT_LOCAL = "Development";
	private static final String LOCATIONS_TAG = "locations";

	private static final String TEMPLATE_PATH = "\\plugins\\configurator\\job\\scm\\subversion.xml";

	public String getElementTag() {
		return JobSCM.ELEMENT_TAG;
	}

	public String getParentElementTag() {
		return JobSCM.PARENT_ELEMENT_TAG;
	}

	public String generateXML(BuildConfigurationModel config) {

		if (config.getProjectToBuild() == null)
		{
			return StringUtils.EMPTY;
		}

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try
		{
			docBuilder = docFactory.newDocumentBuilder();
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}		

		Document doc = JobManagerGenerator.loadTemplate(TEMPLATE_PATH);
		String folder;
		Node node, imported_node;
		Document module = docBuilder.newDocument();
		if (doc.getElementsByTagName(MODULE_TAG).getLength() > 0)
		{
			module.appendChild(module.importNode(doc.getElementsByTagName(MODULE_TAG).item(0), true));
		}
		Node locations;

		if (doc.getElementsByTagName(LOCATIONS_TAG).getLength() > 0)
		{
			locations = doc.getElementsByTagName(LOCATIONS_TAG).item(0);
			try
			{
				if (locations.getChildNodes().getLength() >= 2)
				{
					locations.removeChild(locations.getChildNodes().item(1));
				}
			}
			catch(Exception e)
			{ }
		}
		else
		{
			node = doc.getFirstChild();
			node.appendChild(doc.createElement(LOCATIONS_TAG));
		}

		if (module.getChildNodes().getLength() == 0)
		{
			node = module.createElement(MODULE_TAG);
			module.appendChild(node);
		}

		if (module.getElementsByTagName(URL_TAG).getLength() == 0)
		{
			node = module.getElementsByTagName(MODULE_TAG).item(0);
			node.appendChild(module.createElement(URL_TAG));
		}

		if (module.getElementsByTagName(LOCAL_TAG).getLength() == 0)
		{
			node = module.getElementsByTagName(MODULE_TAG).item(0);
			node.appendChild(module.createElement(LOCAL_TAG));
		}

		folder = module.getElementsByTagName(LOCAL_TAG).item(0).getTextContent();
		if (folder.isEmpty())
		{
			folder = DEFAULT_LOCAL;
		}

		node = doc.getElementsByTagName(LOCATIONS_TAG).item(0);

		if (config.getProjectToBuild().size() > 0)
		{
			module = setModuleValue(module, config.getProjectToBuild().get(0).getProjectUrl(), folder);
			imported_node = doc.importNode(module.getChildNodes().item(0), true);
			node.appendChild(imported_node);
		}

		for (int i=1; i<config.getProjectToBuild().size(); i++)
		{
			module = setModuleValue(module, config.getProjectToBuild().get(i).getProjectUrl(), folder + i);
			imported_node = doc.importNode(module.getChildNodes().item(0), true);
			node.appendChild(imported_node);
		}

		return JobManagerGenerator.documentToXML(doc);
	}

	public void appendToXML(BuildConfigurationModel config, Document doc) {
		doc = JobSCM.removeSCM(doc);
		doc = JobSCM.insertSCM(doc, generateXML(config));
	}

	private Document setModuleValue(Document module, String url, String folder)
	{
		Node node = module.getElementsByTagName(URL_TAG).item(0);
		node.setTextContent(url);
		node = module.getElementsByTagName(LOCAL_TAG).item(0);
		node.setTextContent(folder);
		return module;
	}
}
