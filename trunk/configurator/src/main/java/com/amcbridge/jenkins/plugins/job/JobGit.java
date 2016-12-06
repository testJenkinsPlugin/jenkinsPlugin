package com.amcbridge.jenkins.plugins.job;

import com.amcbridge.jenkins.plugins.job.scm.JobGitScm;
import com.amcbridge.jenkins.plugins.models.BuildConfigurationModel;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;

public class JobGit extends JobGitScm {

    private static final String XPATH_CONCRETE_GIT_SCM = "/scm";
    private static final String TEMPLATE_PATH = "/plugins/build-configurator/job/git/git.xml";

    @Override
    public String generateXML(BuildConfigurationModel config) throws XPathExpressionException, IOException, SAXException, ParserConfigurationException {

        if (config.getProjectToBuild() == null) {
            return StringUtils.EMPTY;
        }

        Document doc = JobManagerGenerator.loadTemplate(TEMPLATE_PATH);
        setProjectsConfigs(doc, config);
        return JobManagerGenerator.documentToXML(doc);
    }

    @Override
    protected void setProjectsConfigs(Document doc, BuildConfigurationModel config) throws XPathExpressionException {

        String scmPath = XPATH_CONCRETE_GIT_SCM;

        //branch name
        setSCMNodeValue(scmPath + XPATH_RELATIVE_GIT_BRANCH_NAME, config.getProjectToBuild().get(0).getBranchName(), doc);

        //url
        setSCMNodeValue(scmPath + XPATH_RELATIVE_GIT_URL, config.getProjectToBuild().get(0).getProjectUrl(), doc);

        //credentials
        setSCMNodeValue(scmPath + XPATH_RELATIVE_GIT_CREDENTIALS_ID, config.getProjectToBuild().get(0).getCredentials(), doc);

    }

}
