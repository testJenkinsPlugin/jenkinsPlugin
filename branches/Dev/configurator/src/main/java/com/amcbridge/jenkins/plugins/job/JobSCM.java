package com.amcbridge.jenkins.plugins.job;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class JobSCM {

	public static final String ELEMENT_TAG = "scm";
	public static final String PARENT_ELEMENT_TAG = "project";

	public static Document removeSCM(Document doc)
	{
		Node scm;
		if (doc.getElementsByTagName(ELEMENT_TAG).getLength() > 0)
		{
			scm = doc.getElementsByTagName(ELEMENT_TAG).item(0);

			Node parentNode = doc.getElementsByTagName(PARENT_ELEMENT_TAG).item(0);
			parentNode.removeChild(scm); 
		}
		return doc;
	}

	public static Document insertSCM(Document doc, String xml)
	{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		Node scm;
		try
		{
			docBuilder = docFactory.newDocumentBuilder();
			InputStream inputSCM = new ByteArrayInputStream(xml.getBytes());
			scm = docBuilder.parse(inputSCM);
			Node node = doc.importNode(scm.getChildNodes().item(0), true);
			doc.getChildNodes().item(0).appendChild(node);
		}
		catch (Exception e)
		{}
		return doc;
	}
}
