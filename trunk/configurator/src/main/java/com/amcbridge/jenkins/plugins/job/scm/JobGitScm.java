package com.amcbridge.jenkins.plugins.job.scm;

import com.amcbridge.jenkins.plugins.models.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.job.elementdescription.JobElementDescription;
import com.amcbridge.jenkins.plugins.job.JobManagerGenerator;
import com.amcbridge.jenkins.plugins.job.JobSCM;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;

public class JobGitScm implements JobElementDescription {

    private static final String XPATH_ALL_GIT_SCM = "//hudson.plugins.git.GitSCM";
    private static final String XPATH_CONCRETE_GIT_SCM = "/scm/scms/hudson.plugins.git.GitSCM";
    protected static final String XPATH_RELATIVE_GIT_URL = "/userRemoteConfigs/hudson.plugins.git.UserRemoteConfig/url";
    protected static final String XPATH_RELATIVE_GIT_CREDENTIALS_ID = "/userRemoteConfigs/hudson.plugins.git.UserRemoteConfig/credentialsId";
    protected static final String XPATH_RELATIVE_GIT_BRANCH_NAME = "/branches/hudson.plugins.git.BranchSpec/name";
    protected static final String XPATH_RELATIVE_GIT_LOCAL_DIRECTORY_PATH = "/extensions/hudson.plugins.git.extensions.impl.RelativeTargetDirectory/relativeTargetDir";

    private static final String TEMPLATE_PATH = "/plugins/build-configurator/job/scm/git.xml";

    @Override
    public String getElementTag() {
        return JobSCM.ELEMENT_TAG;
    }

    @Override
    public String getParentElementTag() {
        return JobSCM.PARENT_ELEMENT_TAG;
    }

    @Override
    public String generateXML(BuildConfigurationModel config) throws XPathExpressionException, IOException, SAXException, ParserConfigurationException {

        if (config.getProjectToBuild() == null) {
            return StringUtils.EMPTY;
        }

        Document doc = JobManagerGenerator.loadTemplate(TEMPLATE_PATH);
        setProjectsConfigs(doc, config);
        return JobManagerGenerator.documentToXML(doc);
    }

    protected void setProjectsConfigs(Document doc, BuildConfigurationModel config) throws XPathExpressionException {

        XPath xPath = XPathFactory.newInstance().newXPath();
        XPathExpression exp;
        // XPath has 1-based indexes. So start index is 1;
        String nodeToCopyExpression = XPATH_ALL_GIT_SCM + "[1]";
        int projectsQuantity = config.getProjectToBuild().size();
        int nodesToCopyQuantity = projectsQuantity - 1;

        // copy default scm node with empty values
        for (int nodeIndex = 0; nodeIndex < nodesToCopyQuantity; nodeIndex++) {
            exp = xPath.compile(nodeToCopyExpression);
            Node gitPlNode = (Node) exp.evaluate(doc, XPathConstants.NODE);
            Node copyNode = gitPlNode.cloneNode(true);
            gitPlNode.getParentNode().appendChild(copyNode);
        }


        for (int projectIndex = 0; projectIndex < projectsQuantity; projectIndex++) {

            String scmPath = XPATH_CONCRETE_GIT_SCM + "[" + (projectIndex + 1) + "]";

            //branch name
            setSCMNodeValue(scmPath + XPATH_RELATIVE_GIT_BRANCH_NAME, config.getProjectToBuild().get(projectIndex).getBranchName(), doc);

            //url
            setSCMNodeValue(scmPath + XPATH_RELATIVE_GIT_URL, config.getProjectToBuild().get(projectIndex).getProjectUrl(), doc);

            //credentials
            setSCMNodeValue(scmPath + XPATH_RELATIVE_GIT_CREDENTIALS_ID, config.getProjectToBuild().get(projectIndex).getCredentials(), doc);

            //local folder
            setSCMNodeValue(scmPath + XPATH_RELATIVE_GIT_LOCAL_DIRECTORY_PATH, config.getProjectToBuild().get(projectIndex).getLocalDirectoryPath(), doc);


        }

    }

    protected void setSCMNodeValue(String nodePath, String value, Document doc) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        XPathExpression exp;
        exp = xPath.compile(nodePath);
        Node branchNode = (Node) exp.evaluate(doc, XPathConstants.NODE);
        branchNode.getTextContent();
        branchNode.setTextContent(value);
    }

    @Override
    public void appendToXML(BuildConfigurationModel config, Document doc) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        JobSCM.removeSCM(doc);
        JobSCM.insertSCM(doc, generateXML(config));
    }

}
