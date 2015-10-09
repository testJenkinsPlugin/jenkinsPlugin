package com.amcbridge.jenkins.plugins.job.SCM;

import com.amcbridge.jenkins.plugins.configurationModels.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.job.ElementDescription.JobElementDescription;
import com.amcbridge.jenkins.plugins.job.JobManagerGenerator;
import com.amcbridge.jenkins.plugins.job.JobSCM;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class JobGit implements JobElementDescription {
    private static final String URL_TAG = "url";
    private static final String PLUGINS_GIT_URC_TAG = "hudson.plugins.git.UserRemoteConfig";
    private static final String USER_REMOTE_CONFIGS_TAG = "userRemoteConfigs";
    private static final String BRANCHES_TAG = "branches";
    private static final String PLUGINS_GIT_BRANCH_SPEC_TAG = "hudson.plugins.git.BranchSpec";
    private static final String BRANCH_NAME_TAG = "name";
    private String remoteUrl;
    private String branchName = "origin/";

    private static final String TEMPLATE_PATH = "\\plugins\\configurator\\job\\scm\\git.xml";

    public JobGit(String remoteUrl, String branchName){
        this.remoteUrl = remoteUrl;
        this.branchName = branchName;
    }


    public String getElementTag() {
		return JobSCM.ELEMENT_TAG;
	}

	public String getParentElementTag() {
		return JobSCM.PARENT_ELEMENT_TAG;
	}

	public String generateXML(BuildConfigurationModel config) {

        if (config.getProjectToBuild() == null) {
            return StringUtils.EMPTY;
        }

        Document doc = JobManagerGenerator.loadTemplate(TEMPLATE_PATH);
        Node userRemoteConfigs = doc.getElementsByTagName(USER_REMOTE_CONFIGS_TAG).item(0);
        for (int k = 0; k < userRemoteConfigs.getChildNodes().getLength(); k++) {
            if (userRemoteConfigs.getChildNodes().item(k).getNodeName().equals(PLUGINS_GIT_URC_TAG)) {
                Node branchTagNode = userRemoteConfigs.getChildNodes().item(k);
                for (int j = 0; j < branchTagNode.getChildNodes().getLength(); j++) {
                    if (branchTagNode.getChildNodes().item(j).getNodeName().equals(URL_TAG)) {
                        branchTagNode.getChildNodes().item(j).setTextContent(remoteUrl);
                        break;
                    }
                }
            }
        }

        Node branches = doc.getElementsByTagName(BRANCHES_TAG).item(0);
        for (int k = 0; k < branches.getChildNodes().getLength(); k++) {
            if (branches.getChildNodes().item(k).getNodeName().equals(PLUGINS_GIT_BRANCH_SPEC_TAG)) {
                Node branchTagNode = branches.getChildNodes().item(k);
                for (int j = 0; j < branchTagNode.getChildNodes().getLength(); j++) {
                    if (branchTagNode.getChildNodes().item(j).getNodeName().equals(BRANCH_NAME_TAG)) {
                        branchTagNode.getChildNodes().item(j).setTextContent(branchName);
                        break;
                    }
                }
            }
        }

		return JobManagerGenerator.documentToXML(doc);
	}

	public void appendToXML(BuildConfigurationModel config, Document doc) {
		doc = JobSCM.removeSCM(doc);
		doc = JobSCM.insertSCM(doc, generateXML(config));
	}

}