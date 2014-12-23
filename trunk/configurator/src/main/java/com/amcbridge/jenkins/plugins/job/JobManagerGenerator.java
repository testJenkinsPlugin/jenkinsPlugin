package com.amcbridge.jenkins.plugins.job;

import hudson.model.Item;
import hudson.model.AbstractItem;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jenkins.model.Jenkins;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.amcbridge.jenkins.plugins.configuration.BuildConfiguration;
import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;
import com.amcbridge.jenkins.plugins.controls.SCM;
import com.amcbridge.jenkins.plugins.job.ElementDescription.JobElementDescription;
import com.amcbridge.jenkins.plugins.job.ElementDescription.JobElementDescriptionCheckBox;
import com.amcbridge.jenkins.plugins.job.SCM.JobNone;
import com.amcbridge.jenkins.plugins.job.SCM.JobSubversion;
import com.thoughtworks.xstream.XStream;

public class JobManagerGenerator {
	public static final String COMMA_SEPARATOR = ", ";

	private static final String JOB_TEMPLATE_PATH = "\\plugins\\configurator\\job\\config.xml";

	public static String convertToXML(Object obj)
	{
		XStream xstream = new XStream();
		return xstream.toXML(obj);
	}

	public static void createJob(BuildConfiguration config)
			throws FileNotFoundException, ParserConfigurationException,
			SAXException, IOException, TransformerException
	{

		if (isJobExist(config.getProjectName()))
		{
			AbstractItem item= (AbstractItem) Jenkins.getInstance().getItemByFullName(config.getProjectName());
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			Document doc = null;
			docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.parse(getJobXML(config));
			Source streamSource = new DOMSource( doc );
			item.updateByXml(streamSource);
			item.save();
		}
		else
		{
			FileInputStream fis = new FileInputStream(getJobXML(config));
			Jenkins.getInstance().createProjectFromXML(config.getProjectName(), fis);
		}
	}

	private static Boolean isJobExist(String name)
	{
		for(Item item : Jenkins.getInstance().getAllItems())
		{
			if (item.getName().equals(name))
			{
				return true;
			}
		}
		return false;
	}

	private static File getJobXML(BuildConfiguration config)
			throws ParserConfigurationException,
			SAXException, IOException, TransformerException
	{
		Document doc = loadTemplate(JOB_TEMPLATE_PATH);
		if (doc == null)
		{
			return null;
		}

		JobElementDescription jed;

		jed = new JobArtefacts();
		setElement(jed, doc, config);

		jed = new JobMailer();
		setElement(jed, doc, config);

		jed = new JobVersionFile();
		setElement(jed, doc, config);

		jed = new JobAssigneNode();
		setElement(jed, doc, config);

		jed = getSCM(config);
		setElement(jed, doc, config);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);

		File file = BuildConfigurationManager.getFileToCreateJob();
		StreamResult result = new StreamResult(file);
		transformer.transform(source, result);

		return file;
	}

	public static JobElementDescription getSCM(BuildConfiguration config)
	{
		SCM scm = getSCM(config.getScm());
		JobElementDescription jed;
		if (scm == null)
		{
			jed = new JobNone();
			return jed;
		}
		switch(scm)
		{
		case SUBVERSION:
			jed = new JobSubversion();
			break;
		default:
			jed = new JobNone();
			break;
		}
		return jed;
	}

	private static SCM getSCM(String scmName)
	{
		for(SCM scm : SCM.values())
		{
			if (scm.toString().equals(scmName))
			{
				return scm;
			}
		}
		return null;
	}

	private static void setElement(JobElementDescription element, Document document, BuildConfiguration config)
			throws ParserConfigurationException, SAXException, IOException
	{	
		if (!isNodeExist(document, element.getElementTag()))
		{
			if (!isNodeExist(document, element.getParentElementTag()))
			{
				Node mainNode = document.getFirstChild();
				mainNode.appendChild(document.createElement(element.getParentElementTag()));
			}
			Node elementNode = document.getElementsByTagName(element.getParentElementTag()).item(0);
			Node newNode = getNode(element.generateXML(config));

			if(element instanceof JobElementDescriptionCheckBox)
			{
				if (newNode.getFirstChild() == null)
				{
					((JobElementDescriptionCheckBox) element).uncheck(document);
				}
				else
				{
					((JobElementDescriptionCheckBox) element).check(document);
				}
			}

			if (newNode.getFirstChild() != null)
			{
				elementNode.appendChild(document.adoptNode(newNode.getChildNodes().item(0).cloneNode(true)));
			}
		}
		else
		{
			element.appendToXML(config, document);
		}
	}

	private static Boolean isNodeExist(Document document, String node)
	{
		if (document.getElementsByTagName(node).getLength() > 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private static Node getNode(String xml) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		if (xml.isEmpty())
		{
			return docBuilder.newDocument();
		}
		InputStream input = new ByteArrayInputStream(xml.getBytes());
		return docBuilder.parse(input);
	}

	public static Document loadTemplate(String path)
	{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		Document doc = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.parse(Jenkins.getInstance().getRootDir() + path);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return doc;
	}

	public static String documentToXML(Node node)
	{
		String result = StringUtils.EMPTY;

		TransformerFactory transformerFactory;
		Transformer transformer;
		DOMSource source;
		Writer writer;
		StreamResult sr;

		try
		{
			transformerFactory = TransformerFactory.newInstance();
			transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			source = new DOMSource(node);
			writer = new StringWriter();
			sr = new StreamResult(writer);
			transformer.transform(source, sr);
			result = writer.toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return result;
	}
}