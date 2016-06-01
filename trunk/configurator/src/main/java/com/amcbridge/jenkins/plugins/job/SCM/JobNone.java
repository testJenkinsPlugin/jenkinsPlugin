package com.amcbridge.jenkins.plugins.job.SCM;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import hudson.scm.NullSCM;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.amcbridge.jenkins.plugins.configurationModels.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.job.JobManagerGenerator;
import com.amcbridge.jenkins.plugins.job.JobSCM;
import com.amcbridge.jenkins.plugins.job.ElementDescription.JobElementDescription;

public class JobNone implements JobElementDescription {

    private static final String ATTRIBUTE = "class";

    @Override
    public String getElementTag() {
        return JobSCM.ELEMENT_TAG;
    }

    @Override
    public String getParentElementTag() {
        return JobSCM.PARENT_ELEMENT_TAG;
    }

    @Override
    public String generateXML(BuildConfigurationModel config) throws ParserConfigurationException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        doc.appendChild(doc.createElement(JobSCM.ELEMENT_TAG));
        Element scm = (Element) doc.getElementsByTagName(JobSCM.ELEMENT_TAG).item(0);
        NullSCM nullSCM = new NullSCM();
        String value = nullSCM.getType();
        scm.setAttribute(ATTRIBUTE, value);

        return JobManagerGenerator.documentToXML(doc);
    }

    @Override
    public void appendToXML(BuildConfigurationModel config, Document doc) throws ParserConfigurationException {
        JobSCM.removeSCM(doc);
        JobSCM.insertSCM(doc, generateXML(config));
    }
}
