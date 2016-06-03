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
import org.xml.sax.SAXException;

import java.io.IOException;

public class JobSubversion implements JobElementDescription {

    private static final String URL_TAG = "remote";
    private static final String LOCAL_TAG = "local";
    private static final String MODULE_TAG = "hudson.scm.SubversionSCM_-ModuleLocation";
    private static final String LOCATIONS_TAG = "locations";
    private static final String CREDENTIAL_ID_TAG = "credentialsId";

    private static final String TEMPLATE_PATH = "/plugins/build-configurator/job/scm/subversion.xml";
    @Override
    public String getElementTag() {
        return JobSCM.ELEMENT_TAG;
    }
    @Override
    public String getParentElementTag() {
        return JobSCM.PARENT_ELEMENT_TAG;
    }
    @Override
    public String generateXML(BuildConfigurationModel config) throws ParserConfigurationException, IOException, SAXException {

        if (config.getProjectToBuild() == null) {
            return StringUtils.EMPTY;
        }

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();



        Document doc = JobManagerGenerator.loadTemplate(TEMPLATE_PATH);
        Node node;
        Node importedNode;
        Document module = docBuilder.newDocument();
        if (doc.getElementsByTagName(MODULE_TAG).getLength() > 0) {
            module.appendChild(module.importNode(doc.getElementsByTagName(MODULE_TAG).item(0), true));
        }
        Node locations;

        if (doc.getElementsByTagName(LOCATIONS_TAG).getLength() > 0) {
            locations = doc.getElementsByTagName(LOCATIONS_TAG).item(0);
            if (locations.getChildNodes().getLength() >= 2) {
                locations.removeChild(locations.getChildNodes().item(1));
            }

        } else {
            node = doc.getFirstChild();
            node.appendChild(doc.createElement(LOCATIONS_TAG));
        }

        if (module.getChildNodes().getLength() == 0) {
            node = module.createElement(MODULE_TAG);
            module.appendChild(node);
        }

        if (module.getElementsByTagName(URL_TAG).getLength() == 0) {
            node = module.getElementsByTagName(MODULE_TAG).item(0);
            node.appendChild(module.createElement(URL_TAG));
        }

        if (module.getElementsByTagName(LOCAL_TAG).getLength() == 0) {
            node = module.getElementsByTagName(MODULE_TAG).item(0);
            node.appendChild(module.createElement(LOCAL_TAG));
        }

        if (module.getElementsByTagName(CREDENTIAL_ID_TAG).getLength() == 0) {
            node = module.getElementsByTagName(MODULE_TAG).item(0);
            node.appendChild(module.createElement(CREDENTIAL_ID_TAG));
        }

        node = doc.getElementsByTagName(LOCATIONS_TAG).item(0);

        for (int i = 0; i < config.getProjectToBuild().size(); i++) {
            String credentialsId = config.getProjectToBuild().get(i).getCredentials();
            module = setModuleValue(module, config.getProjectToBuild().get(i).getProjectUrl(),
                    config.getProjectToBuild().get(i).getLocalDirectoryPath());
            module = setModuleCredentialsValue(module, credentialsId);
            importedNode = doc.importNode(module.getChildNodes().item(0), true);
            node.appendChild(importedNode);
        }

        return JobManagerGenerator.documentToXML(doc);
    }

    @Override
    public void appendToXML(BuildConfigurationModel config, Document doc) throws ParserConfigurationException, IOException, SAXException {
        JobSCM.removeSCM(doc);
        JobSCM.insertSCM(doc, generateXML(config));
    }

    private Document setModuleValue(Document module, String url, String folder) {
        Node node = module.getElementsByTagName(URL_TAG).item(0);
        node.setTextContent(url);
        node = module.getElementsByTagName(LOCAL_TAG).item(0);
        node.setTextContent(folder);
        return module;
    }

    private Document setModuleCredentialsValue(Document module, String credentialsId) {
        Node node = module.getElementsByTagName(CREDENTIAL_ID_TAG).item(0);
        node.setTextContent(credentialsId);
        return module;
    }
}
