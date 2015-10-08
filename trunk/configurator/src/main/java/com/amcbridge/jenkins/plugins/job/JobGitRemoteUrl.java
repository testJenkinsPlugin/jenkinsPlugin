/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amcbridge.jenkins.plugins.job;

import org.w3c.dom.Document;

import com.amcbridge.jenkins.plugins.configurationModels.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.job.ElementDescription.JobElementDescription;

public class JobGitRemoteUrl implements JobElementDescription {

    private static final String ELEMENT_TAG = "repository";
    private static final String PARENT_ELEMENT_TAG = "project";


    public String getElementTag() {
        return ELEMENT_TAG;
    }

    public String getParentElementTag() {
        return PARENT_ELEMENT_TAG;
    }

    public String generateXML(BuildConfigurationModel config) {

        return JobManagerGenerator.convertToXML("");
    }

    public void appendToXML(BuildConfigurationModel config, Document xml) {

    }

}
