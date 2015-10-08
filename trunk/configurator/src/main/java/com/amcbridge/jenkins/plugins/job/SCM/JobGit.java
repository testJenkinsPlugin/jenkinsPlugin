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
    private static final String LOCAL_TAG = "local";
    private static final String PLUGINS_GIT_URC_TAG = "hudson.plugins.git.UserRemoteConfig";
    private static final String DEFAULT_LOCAL = "Development";
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

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        Document doc = JobManagerGenerator.loadTemplate(TEMPLATE_PATH);
        String folder;
        Node node = null, imported_node;
        Document module = docBuilder.newDocument();
        if (doc.getElementsByTagName(PLUGINS_GIT_URC_TAG).getLength() > 0) {
            module.appendChild(module.importNode(doc.getElementsByTagName(PLUGINS_GIT_URC_TAG).item(0), true));
        }
        Node userRemoteConfigs;

        if (doc.getElementsByTagName(USER_REMOTE_CONFIGS_TAG).getLength() > 0) {
            userRemoteConfigs = doc.getElementsByTagName(USER_REMOTE_CONFIGS_TAG).item(0);
            try {
                if (userRemoteConfigs.getChildNodes().getLength() >= 2) {
                    userRemoteConfigs.removeChild(userRemoteConfigs.getChildNodes().item(1));
                }
            } catch (Exception e) {
            }
        } else {
            node = doc.getFirstChild();
            node.appendChild(doc.createElement(USER_REMOTE_CONFIGS_TAG));
        }

        if (module.getChildNodes().getLength() == 0) {
            node = module.createElement(PLUGINS_GIT_URC_TAG);
            module.appendChild(node);
        }

        if (module.getElementsByTagName(URL_TAG).getLength() == 0) {
            node = module.getElementsByTagName(PLUGINS_GIT_URC_TAG).item(0);
            Node urlTag = node.appendChild(module.createElement(URL_TAG));
            urlTag.setNodeValue(remoteUrl);
        }

//		if (doc.getElementsByTagName(PLUGINS_GIT_URC_TAG).getLength() > 0)
//		{
//			module.appendChild(module.importNode(doc.getElementsByTagName(PLUGINS_GIT_URC_TAG).item(0), true));
//		}                
        Node branches;

        if (doc.getElementsByTagName(BRANCHES_TAG).getLength() > 0) {
            branches = doc.getElementsByTagName(BRANCHES_TAG).item(0);
        } else {
            node = doc.getFirstChild();
            node.appendChild(doc.createElement(BRANCHES_TAG));
        }

        if (module.getElementsByTagName(BRANCH_NAME_TAG).getLength() == 0) {
            node = doc.getElementsByTagName(PLUGINS_GIT_BRANCH_SPEC_TAG).item(0);
        }    
//            Node branchNameTag = node.appendChild(module.createElement(BRANCH_NAME_TAG));
//            branchNameTag.setNodeValue(branchName);


        if (module.getElementsByTagName(LOCAL_TAG).getLength() == 0) {
            node = module.getElementsByTagName(PLUGINS_GIT_URC_TAG).item(0);
            node.appendChild(module.createElement(LOCAL_TAG));
        }

        folder = module.getElementsByTagName(LOCAL_TAG).item(0).getTextContent();
        if (folder.isEmpty()) {
            folder = DEFAULT_LOCAL;
        }

        node = doc.getElementsByTagName(USER_REMOTE_CONFIGS_TAG).item(0);

        for (int i = 0; i < config.getProjectToBuild().size(); i++) {
            module = setModuleValue(module, config.getProjectToBuild().get(i).getProjectUrl(),
                    config.getProjectToBuild().get(i).getLocalDirectoryPath());
            imported_node = doc.importNode(module.getChildNodes().item(0), true);
            node.appendChild(imported_node);
        }

		return JobManagerGenerator.documentToXML(doc);
	}

	public void appendToXML(BuildConfigurationModel config, Document doc) {
		doc = JobSCM.removeSCM(doc);
		doc = JobSCM.insertSCM(doc, generateXML(config));
	}

    private Document setModuleValue(Document module, String url, String folder) {
        Node node = module.getElementsByTagName(URL_TAG).item(0);
        node.setTextContent(url);
        node = module.getElementsByTagName(LOCAL_TAG).item(0);
        node.setTextContent(folder);
        return module;
    }
}