package com.amcbridge.jenkins.plugins.job;

import hudson.plugins.descriptionsetter.DescriptionSetterPublisher;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import com.amcbridge.jenkins.plugins.models.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.models.ProjectToBuildModel;
import com.amcbridge.jenkins.plugins.job.elementdescription.JobElementDescription;

public class JobVersionFile implements JobElementDescription {

    private static final String ELEMENT_TAG = "hudson.plugins.descriptionsetter.DescriptionSetterPublisher";
    private static final String PARENT_ELEMENT_TAG = "publishers";
    private static final String REGEXP_TAG = "regexp";
    private static final String EXPRESSION = "\\[getting of version of the build started by hudson\\] (.*)";
    private String regExp;

    public JobVersionFile(String regExp){
        this.regExp = regExp;
    }
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
        DescriptionSetterPublisher dsp
                = new DescriptionSetterPublisher(generateExpression(), StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, false);
        return JobManagerGenerator.convertToXML(dsp);
    }

    @Override
    public void appendToXML(BuildConfigurationModel config, Document xml) {
        Node node = xml.getElementsByTagName(REGEXP_TAG).item(0);
        node.setTextContent(generateExpression());
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

    private String generateExpression() {
        String expression = regExp;
        if (regExp == null || "".equals(regExp)) {
            expression = EXPRESSION;
        }
        return expression;
    }
}
