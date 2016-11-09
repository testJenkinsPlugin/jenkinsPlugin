package com.amcbridge.jenkins.plugins.job;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import hudson.tasks.ArtifactArchiver;
import com.amcbridge.jenkins.plugins.models.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.models.ProjectToBuildModel;
import com.amcbridge.jenkins.plugins.job.elementdescription.JobElementDescription;

public class JobArtifacts implements JobElementDescription {

    private static final String ELEMENT_TAG = "hudson.tasks.ArtifactArchiver";
    private static final String PARENT_ELEMENT_TAG = "publishers";
    private static final String PATH_TAG = "artifacts";

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
        String artf = getArtifacts(config);
        if (artf.isEmpty()) {
            return StringUtils.EMPTY;
        }
        ArtifactArchiver artifact = new ArtifactArchiver(artf, StringUtils.EMPTY, false);
        return JobManagerGenerator.convertToXML(artifact);
    }

    @Override
    public void appendToXML(BuildConfigurationModel config, Document xml) {
        Node node = xml.getElementsByTagName(PATH_TAG).item(0);
        String artf = getArtifacts(config);
        String artifacts;

        if (artf.isEmpty()) {
            return;
        }
        artifacts = getArtifacts(config);
        node.setTextContent(artifacts);
    }

    private String getArtifacts(BuildConfigurationModel config) {
        String result = StringUtils.EMPTY;
        String local;
        if (config.getProjectToBuild() == null) {
            return result;
        }

        for (ProjectToBuildModel artf : config.getProjectToBuild()) {
            local = StringUtils.join(artf.getArtifacts(), JobManagerGenerator.COMMA_SEPARATOR);
            if (result.length() > 0 && local.length() > 0) {
                result += JobManagerGenerator.COMMA_SEPARATOR + local;
            } else {
                result += local;
            }
        }
        return result;
    }

}
