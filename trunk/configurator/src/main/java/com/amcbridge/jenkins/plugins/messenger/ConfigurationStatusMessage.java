package com.amcbridge.jenkins.plugins.messenger;

import org.apache.commons.lang.StringUtils;

public class ConfigurationStatusMessage implements MessageInfo {

    private String subject, sendTo, sendCC, description, projectName;

    public ConfigurationStatusMessage(String subject,
            String sendTo, String sendCC, String description, String projectName) {
        this.subject = subject;
        this.sendTo = sendTo;
        this.sendCC = sendCC;
        this.description = description;
        this.projectName = projectName;
    }

    public ConfigurationStatusMessage(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String value) {
        projectName = value;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String value) {
        subject = value;
    }

    public String getDestinationAddress() {
        return sendTo;
    }

    public void setDestinationAddress(String value) {
        sendTo = value;
    }

    public String getCC() {
        return sendCC;
    }

    public void setCC(String value) {
        if (value.contains(" ")) {
            sendCC = StringUtils.EMPTY;
        } else {
            sendCC = value;
        }
    }

    public void setDescription(String value) {
        description = value;
    }

    public String getMassageText() {
        return projectName + " - " + description;
    }
}
