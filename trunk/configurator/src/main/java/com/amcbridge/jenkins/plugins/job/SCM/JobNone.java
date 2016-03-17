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

    public String getElementTag() {
        return JobSCM.ELEMENT_TAG;
    }

    public String getParentElementTag() {
        return JobSCM.PARENT_ELEMENT_TAG;
    }

    public String generateXML(BuildConfigurationModel config) {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        Document doc = null;
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.newDocument();
            doc.appendChild(doc.createElement(JobSCM.ELEMENT_TAG));
            Element scm = (Element) doc.getElementsByTagName(JobSCM.ELEMENT_TAG).item(0);
            NullSCM nullSCM = new NullSCM();
            String value = nullSCM.getType();
            scm.setAttribute(ATTRIBUTE, value);
        } catch (ParserConfigurationException e) {
        }
        return JobManagerGenerator.documentToXML(doc);
    }

    public void appendToXML(BuildConfigurationModel config, Document doc) {
        JobSCM.removeSCM(doc);
        JobSCM.insertSCM(doc, generateXML(config));
    }
}
