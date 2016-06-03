package com.amcbridge.jenkins.plugins.job.ElementDescription;

import org.w3c.dom.Document;
import com.amcbridge.jenkins.plugins.configurationModels.BuildConfigurationModel;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public interface JobElementDescription {

    public String getElementTag();

    public String getParentElementTag();

    public String generateXML(BuildConfigurationModel config) throws ParserConfigurationException, XPathExpressionException, IOException, SAXException;

    public void appendToXML(BuildConfigurationModel config, Document xml) throws ParserConfigurationException, XPathExpressionException, SAXException, IOException;
}
