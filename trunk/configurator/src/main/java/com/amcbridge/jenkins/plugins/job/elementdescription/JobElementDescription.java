package com.amcbridge.jenkins.plugins.job.elementdescription;

import org.w3c.dom.Document;
import com.amcbridge.jenkins.plugins.models.BuildConfigurationModel;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public interface JobElementDescription {

    String getElementTag();

    String getParentElementTag();

    String generateXML(BuildConfigurationModel config) throws ParserConfigurationException, XPathExpressionException, IOException, SAXException;

    void appendToXML(BuildConfigurationModel config, Document xml) throws ParserConfigurationException, XPathExpressionException, SAXException, IOException;
}
