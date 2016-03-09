package com.amcbridge.jenkins.plugins.job.SCM;

import com.amcbridge.jenkins.plugins.configurationModels.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.job.ElementDescription.JobElementDescription;
import com.amcbridge.jenkins.plugins.job.JobManagerGenerator;
import com.amcbridge.jenkins.plugins.job.JobSCM;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class JobGit implements JobElementDescription {

    private static final String URL_TAG = "url";
    private static final String PLUGINS_GIT_URC_TAG = "hudson.plugins.git.UserRemoteConfig";
    private static final String USER_REMOTE_CONFIGS_TAG = "userRemoteConfigs";
    private static final String BRANCHES_TAG = "branches";
    private static final String PLUGINS_GIT_BRANCH_SPEC_TAG = "hudson.plugins.git.BranchSpec";
    private static final String BRANCH_NAME_TAG = "name";
    private static final String MODULE_TAG = "hudson.plugins.git.GitSCM";
    private static final String CREDENTIAL_ID_TAG = "credentialsId";
    private static final String GLOBAL_CONFIG_NAME_TAG = "globalConfigName";
    private static final String GLOBAL_CONFIG_MAIL_TAG = "globalConfigEmail";

    private String branchName = "origin/";

    private static final String TEMPLATE_PATH = "\\plugins\\configurator\\job\\scm\\git.xml";
    private static final Logger log = Logger.getLogger(JobGit.class);

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
        Node node = null;

        deleteDefaultUserRemoteConfig(node, doc);
        makeUserRemoteConfigsPart(docBuilder, doc, config);

        deleteDefaultBranches(node, doc);
        makeBranchesPart(docBuilder, doc, config);

        return JobManagerGenerator.documentToXML(doc);
    }

    private void deleteDefaultUserRemoteConfig(Node node, Document doc) {
        Node userRemoteConfigs;
        if (doc.getElementsByTagName(USER_REMOTE_CONFIGS_TAG).getLength() > 0) {
            userRemoteConfigs = doc.getElementsByTagName(USER_REMOTE_CONFIGS_TAG).item(0);
            try {
                if (userRemoteConfigs.getChildNodes().getLength() >= 2) {
                    userRemoteConfigs.removeChild(userRemoteConfigs.getChildNodes().item(1));
                }
            } catch (DOMException e) {
                log.error(e.getLocalizedMessage());
            }
        } else {
            node = doc.getFirstChild();
            node.appendChild(doc.createElement(USER_REMOTE_CONFIGS_TAG));
        }
    }

    private void makeUserRemoteConfigsPart(DocumentBuilder docBuilder, Document doc, BuildConfigurationModel config) {
        Node node = null;
        Node imported_node = null;
        Document module = docBuilder.newDocument();

        if (doc.getElementsByTagName(PLUGINS_GIT_URC_TAG).getLength() > 0) {
            module.appendChild(module.importNode(doc.getElementsByTagName(PLUGINS_GIT_URC_TAG).item(0), true));
        }

        if (module.getChildNodes().getLength() == 0) {
            node = module.createElement(PLUGINS_GIT_URC_TAG);
            module.appendChild(node);
        }

        if (module.getElementsByTagName(URL_TAG).getLength() == 0) {
            node = module.getElementsByTagName(PLUGINS_GIT_URC_TAG).item(0);
            node.appendChild(module.createElement(URL_TAG));
        }

        if (module.getElementsByTagName(GLOBAL_CONFIG_NAME_TAG).getLength() == 0) {
            node = module.getElementsByTagName(PLUGINS_GIT_URC_TAG).item(0);
            node.appendChild(module.createElement(GLOBAL_CONFIG_NAME_TAG));
        }

        if (module.getElementsByTagName(GLOBAL_CONFIG_MAIL_TAG).getLength() == 0) {
            node = module.getElementsByTagName(PLUGINS_GIT_URC_TAG).item(0);
            node.appendChild(module.createElement(GLOBAL_CONFIG_MAIL_TAG));
        }

        if (module.getElementsByTagName(CREDENTIAL_ID_TAG).getLength() == 0) {
            node = module.getElementsByTagName(PLUGINS_GIT_URC_TAG).item(0);
            node.appendChild(module.createElement(CREDENTIAL_ID_TAG));
        }

        node = doc.getElementsByTagName(USER_REMOTE_CONFIGS_TAG).item(0);

        for (int i = 0; i < config.getProjectToBuild().size(); i++) {
            String credentialsId = config.getProjectToBuild().get(i).getCredentials();
            module = setModuleUrlValue(module, config.getProjectToBuild().get(i).getProjectUrl());
            module = setModuleCredentialsValue(module, credentialsId);
            imported_node = doc.importNode(module.getChildNodes().item(0), true);
            node.appendChild(imported_node);
        }
    }

    private String getCredentialsId(String credentialsItem) {
        String res = "";
        String[] credentialsItemArray = credentialsItem.split(";");
        if (credentialsItemArray.length > 1) {
            res = credentialsItemArray[1].trim();
        }
        return res;
    }

    private void deleteDefaultBranches(Node node, Document doc) {
        Node branches;
        if (doc.getElementsByTagName(BRANCHES_TAG).getLength() > 0) {
            branches = doc.getElementsByTagName(BRANCHES_TAG).item(0);
            try {
                if (branches.getChildNodes().getLength() >= 2) {
                    branches.removeChild(branches.getChildNodes().item(1));
                }
            } catch (DOMException e) {
                log.error(e.getLocalizedMessage());
            }
        } else {
            node = doc.getFirstChild();
            node.appendChild(doc.createElement(BRANCHES_TAG));
        }
    }

    private void makeBranchesPart(DocumentBuilder docBuilder, Document doc, BuildConfigurationModel config) {
        Node node = null;
        Node imported_node = null;
        Document module = docBuilder.newDocument();

        if (doc.getElementsByTagName(PLUGINS_GIT_BRANCH_SPEC_TAG).getLength() > 0) {
            module.appendChild(module.importNode(doc.getElementsByTagName(PLUGINS_GIT_BRANCH_SPEC_TAG).item(0), true));
        }

        if (module.getChildNodes().getLength() == 0) {
            node = module.createElement(PLUGINS_GIT_BRANCH_SPEC_TAG);
            module.appendChild(node);
        }

        if (module.getElementsByTagName(BRANCH_NAME_TAG).getLength() == 0) {
            node = module.getElementsByTagName(PLUGINS_GIT_BRANCH_SPEC_TAG).item(0);
            node.appendChild(module.createElement(BRANCH_NAME_TAG));
        }

        node = doc.getElementsByTagName(BRANCHES_TAG).item(0);

        for (int i = 0; i < config.getProjectToBuild().size(); i++) {
            module = setModuleBranchValue(module, config.getProjectToBuild().get(i).getBranchName());
            imported_node = doc.importNode(module.getChildNodes().item(0), true);
            node.appendChild(imported_node);

        }
    }

    public void appendToXML(BuildConfigurationModel config, Document doc) {
        doc = JobSCM.removeSCM(doc);
        doc = JobSCM.insertSCM(doc, generateXML(config));
    }

    private Document setModuleUrlValue(Document module, String url) {
        Node node = module.getElementsByTagName(URL_TAG).item(0);
        node.setTextContent(url);
        return module;
    }

    private Document setModuleCredentialsValue(Document module, String credentialsId) {
        Node node = module.getElementsByTagName(CREDENTIAL_ID_TAG).item(0);
        node.setTextContent(credentialsId);
        return module;
    }

    private Document setModuleBranchValue(Document module, String branch) {
        Node node = module.getElementsByTagName(BRANCH_NAME_TAG).item(0);
        node.setTextContent(branch);
        return module;
    }
}
