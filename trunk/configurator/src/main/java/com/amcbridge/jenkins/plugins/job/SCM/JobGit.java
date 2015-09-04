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

public class JobGit implements JobElementDescription{
	
	private static final String URL_TAG = "remote";
	private static final String LOCAL_TAG = "local";
	private static final String MODULE_TAG = "hudson.scm.GitSCM_-ModuleLocation";
	private static final String DEFAULT_LOCAL = "Development";
	private static final String LOCATIONS_TAG = "locations";
	
	private static final String TEMPLATE_PATH = "\\plugins\\configurator\\job\\scm\\git.xml";

    public String getElementTag() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String getParentElementTag() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String generateXML(BuildConfigurationModel config) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void appendToXML(BuildConfigurationModel config, Document xml) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
	


}
