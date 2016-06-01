package com.amcbridge.jenkins.plugins.job;

import hudson.plugins.descriptionsetter.DescriptionSetterPublisher;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import com.amcbridge.jenkins.plugins.configurationModels.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.configurationModels.ProjectToBuildModel;
import com.amcbridge.jenkins.plugins.job.ElementDescription.JobElementDescription;

public class JobVersionFile implements JobElementDescription {

    private static final String ELEMENT_TAG = "hudson.plugins.descriptionsetter.DescriptionSetterPublisher";
    private static final String PARENT_ELEMENT_TAG = "publishers";
    private static final String REGEXP_TAG = "regexp";
    private static final String EXPRESSION = "\\[getting of version of the build started by hudson\\] (.*)";

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

        if (!isVersionFileSet(config)) {
            return StringUtils.EMPTY;
        }

        DescriptionSetterPublisher dsp
                = new DescriptionSetterPublisher(EXPRESSION, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, false);
        return JobManagerGenerator.convertToXML(dsp);
    }

    @Override
    public void appendToXML(BuildConfigurationModel config, Document xml) {

        if (!isVersionFileSet(config)) {
            return;
        }
        Node node = xml.getElementsByTagName(REGEXP_TAG).item(0);
        node.setTextContent(EXPRESSION);
    }

    private Boolean isVersionFileSet(BuildConfigurationModel config) {
        if (config.getProjectToBuild() == null) {
            return false;
        }

        for (ProjectToBuildModel isVersionFile : config.getProjectToBuild()) {
            if (isVersionFile.IsVersionFiles()) {
                return true;
            }
        }
        return false;
    }

}
