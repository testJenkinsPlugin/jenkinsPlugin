package com.amcbridge.jenkins.plugins.job;

import antlr.ANTLRException;
import com.amcbridge.jenkins.plugins.job.elementdescription.JobElementDescription;
import com.amcbridge.jenkins.plugins.models.BuildConfigurationModel;
import hudson.triggers.SCMTrigger;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public class JobBuildTriggerPeriodically implements JobElementDescription {
    private static final String DEFAULT_BUILD_TRIGGER = "H * * * *";
    private static final String PARENT_ELEMENT_TAG = "triggers";
    private static final String ELEMENT_TAG = "hudson.triggers.SCMTrigger";
    private static final Logger LOGGER = LoggerFactory.getLogger(JobBuildTriggerPeriodically.class);

    @Override
    public String getElementTag() {
        return "hudson.triggers.SCMTrigger";
    }

    @Override
    public String getParentElementTag() {
        return PARENT_ELEMENT_TAG;
    }

    @Override
    public String generateXML(BuildConfigurationModel config) throws ParserConfigurationException, XPathExpressionException, IOException, SAXException {
        SCMTrigger scmTrigger;
        if(config == null){
            return StringUtils.EMPTY;
        }
        try {
            scmTrigger = new SCMTrigger(config.getBuildPeriodicallyTrigger(), false);
            return JobManagerGenerator.convertToXML(scmTrigger);
        } catch (ANTLRException e) {
            LOGGER.error("Error setting \"" + config.getBuildPeriodicallyTrigger() + "\" build trigger", e);
            try {
                scmTrigger = new SCMTrigger(DEFAULT_BUILD_TRIGGER, false);
                return JobManagerGenerator.convertToXML(scmTrigger);
            } catch (ANTLRException e1) {
                LOGGER.error("Error setting default \"" + DEFAULT_BUILD_TRIGGER +" \" build trigger", e1);
                return StringUtils.EMPTY;
            }
        }
    }

    @Override
    public void appendToXML(BuildConfigurationModel config, Document xml) throws ParserConfigurationException, XPathExpressionException, SAXException, IOException {
        Node node = xml.getElementsByTagName(ELEMENT_TAG).item(0);
        if (config.getBuildPeriodicallyTrigger() == null || "".equals(config.getBuildPeriodicallyTrigger())) {
            node.getParentNode().removeChild(node);
            return;
        }
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            if ("spec".equals(nodeList.item(i).getNodeName())) {
                nodeList.item(i).setTextContent(config.getBuildPeriodicallyTrigger());
                break;
            }
        }
    }
}
