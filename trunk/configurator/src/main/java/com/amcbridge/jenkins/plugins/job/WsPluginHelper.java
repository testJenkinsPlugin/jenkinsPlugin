package com.amcbridge.jenkins.plugins.job;


import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;
import com.amcbridge.jenkins.plugins.exceptions.JenkinsInstanceNotFoundException;
import com.amcbridge.jenkins.plugins.models.BuildConfigurationModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;

class WsPluginHelper {
    private static final String XPATH_WS_CLEANUP_PLUGIN = "/project/buildWrappers/hudson.plugins.ws__cleanup.PreBuildCleanup";

    private static final String XPATH_WS_CLEANUP_PLUGIN_PATTERN_NODE = "/project/buildWrappers/hudson.plugins.ws__cleanup.PreBuildCleanup/patterns/hudson.plugins.ws__cleanup.Pattern";

    private WsPluginHelper() {
    }

    private static boolean isWsPluginInstalled() throws JenkinsInstanceNotFoundException {
        if (BuildConfigurationManager.getJenkins().getPlugin("ws-cleanup") != null) {
            return true;
        }
        return false;
    }

    static void setWsPluginJobName(Document doc, BuildConfigurationModel config) throws XPathExpressionException {
        if (isWsPluginIncluded(doc) && isExcludePatternIncluded(doc, config.getProjectName())) {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Node patternWithName = (Node) xPath.evaluate(XPATH_WS_CLEANUP_PLUGIN_PATTERN_NODE + "/pattern", doc, XPathConstants.NODE);
            patternWithName.setTextContent(config.getProjectName() + ".xml");
            patternWithName.setNodeValue(config.getProjectName() + ".xml");
        }
    }

    static void wsPluginConfigure(Document doc, BuildConfigurationModel config) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        if (config.isCleanWorkspace()) {
            createWsPluginNode(doc, config);
        } else {
            deletePluginFromJobXml(doc);
        }
    }

    // config set to clean WS
    private static void createWsPluginNode(Document doc, BuildConfigurationModel config) throws XPathExpressionException, IOException, SAXException, ParserConfigurationException {
        XPath xPath;
        if (isWsPluginIncluded(doc)) {
            if (isExcludePatternIncluded(doc, config.getProjectName())) {
                return;
            } else {
                Document jobTemplate = JobManagerGenerator.loadTemplate(JobManagerGenerator.JOB_TEMPLATE_PATH);
//                setWsPluginJobName(jobTemplate,jobName);
                xPath = XPathFactory.newInstance().newXPath();
                Node patternNodeTemplate = (Node) xPath.evaluate(XPATH_WS_CLEANUP_PLUGIN_PATTERN_NODE, jobTemplate, XPathConstants.NODE);
                Node listOfPatternsNode = (Node) xPath.evaluate("/project/buildWrappers/hudson.plugins.ws__cleanup.PreBuildCleanup/patterns", doc, XPathConstants.NODE);
                patternNodeTemplate = doc.importNode(patternNodeTemplate, true);

                listOfPatternsNode.appendChild(patternNodeTemplate);

            }
        } else {
            Document jobTemplate = JobManagerGenerator.loadTemplate(JobManagerGenerator.JOB_TEMPLATE_PATH);
//            setWsPluginJobName(jobTemplate,jobName);
            xPath = XPathFactory.newInstance().newXPath();
            Node pluginNode = (Node) xPath.evaluate(XPATH_WS_CLEANUP_PLUGIN, jobTemplate, XPathConstants.NODE);
            Node buildWrappersNode = doc.getElementsByTagName("buildWrappers").item(0);
            //TODO: set job name to exclude
            pluginNode = doc.importNode(pluginNode, true);
            buildWrappersNode.appendChild(pluginNode);
        }

    }

    private static void deletePluginFromJobXml(Document doc) throws XPathExpressionException {
        Node pluginNode = getPluginNode(doc);
        if (pluginNode != null && pluginNode.getParentNode() != null) {
            pluginNode.getParentNode().removeChild(pluginNode);
        }
    }

    private static boolean isWsPluginIncluded(Document doc) throws XPathExpressionException {
        if (getPluginNode(doc) != null) {
            return true;
        }
        return false;
    }

    private static Node getPluginNode(Document doc) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        return (Node) xPath.evaluate(XPATH_WS_CLEANUP_PLUGIN, doc, XPathConstants.NODE);
    }

    private static boolean isExcludePatternIncluded(Document doc, String jobName) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList patternsList = (NodeList) xPath.evaluate(XPATH_WS_CLEANUP_PLUGIN_PATTERN_NODE, doc, XPathConstants.NODESET);

        for (int i = 0; i < patternsList.getLength(); i++) {
            Node pattern = patternsList.item(i);
            if ((isNodeContainText("pattern", jobName + ".xml", pattern) || isNodeContainText("pattern", "configFileName", pattern))
                    && isNodeContainText("type", "EXCLUDE", pattern)) {
                return true;
            }

        }
        return false;
    }

    private static boolean isNodeContainText(String nodeName, String nodeText, Node nodeForSearch) {
        NodeList nodeList = ((Element) nodeForSearch).getElementsByTagName(nodeName);
        if (nodeList != null && nodeList.item(0) != null) {
            String nodeContext = nodeList.item(0).getTextContent();
            if (nodeContext != null && nodeContext.equals(nodeText)) {
                return true;
            }
        }
        return false;
    }
}
