package com.amcbridge.jenkins.plugins.job;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.amcbridge.jenkins.plugins.configurator.BuildConfigurator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import com.amcbridge.jenkins.plugins.models.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.job.elementdescription.JobElementDescriptionCheckBox;

public class JobAssignedNode implements JobElementDescriptionCheckBox {

    private static final String ELEMENT_TAG = "assignedNode";
    private static final String PARENT_ELEMENT_TAG = "project";
    private static final String NODE_SEPARATOR = " || ";
    private static final String CHECK_TAG = "canRoam";
    private static final Logger logger = LoggerFactory.getLogger(JobAssignedNode.class);

    @Override
    public String getElementTag() {
        return ELEMENT_TAG;
    }

    @Override
    public String getParentElementTag() {
        return PARENT_ELEMENT_TAG;
    }

    @Override
    public String generateXML(BuildConfigurationModel config) {
        DocumentBuilderFactory docFactory;
        DocumentBuilder docBuilder;
        Document doc;
        Node node = null;

        if (getNodes(config).isEmpty()) {
            return StringUtils.EMPTY;
        }
        try {
            docFactory = DocumentBuilderFactory.newInstance();
            docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.newDocument();
            node = doc.createElement(ELEMENT_TAG);
            node.setTextContent(getNodes(config));
        } catch (Exception e) {
            logger.error("Generate XML error", e);
        }
        return JobManagerGenerator.documentToXML(node);
    }

    @Override
    public void appendToXML(BuildConfigurationModel config, Document xml) {
        Node node = xml.getElementsByTagName(ELEMENT_TAG).item(0);
        String nodes = getNodes(config);
        if (nodes.isEmpty()) {
            return;
        }
        nodes = getNodes(config);
        node.setTextContent(nodes);
    }

    private String getNodes(BuildConfigurationModel config) {
        String result = StringUtils.EMPTY;
        if (config.getBuildMachineConfiguration() == null
                || config.getBuildMachineConfiguration().length == 0) {
            return result;
        }
        result = StringUtils.join(config.getBuildMachineConfiguration(), NODE_SEPARATOR);
        return result;
    }

    @Override
    public void unCheck(Document doc) {
        if (doc.getElementsByTagName(CHECK_TAG).getLength() != 0) {
            Node node = doc.getElementsByTagName(CHECK_TAG).item(0);
            node.setTextContent(Boolean.TRUE.toString());
        }
    }

    @Override
    public void check(Document doc) {
        if (doc.getElementsByTagName(CHECK_TAG).getLength() == 0) {
            return;
        }
        Node node = doc.getElementsByTagName(CHECK_TAG).item(0);
        node.setTextContent(Boolean.FALSE.toString());
    }
}
