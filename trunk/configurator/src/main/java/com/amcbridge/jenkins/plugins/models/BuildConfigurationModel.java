package com.amcbridge.jenkins.plugins.models;

import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;
import com.amcbridge.jenkins.plugins.enums.ConfigurationState;
import hudson.model.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BuildConfigurationModel {

    private String projectName;
    private String email;
    private String creator;
    private String date;
    private String rejectionReason;
    private String scm;
    private String configEmail;
    private String scriptType;
    private String preScript;
    private String postScript;
    private Boolean isJobUpdate;
    private ConfigurationState state;
    private List<ProjectToBuildModel> projectToBuild;
    private String[] scripts;
    private String[] buildMachineConfiguration;
    private String comments;
    private List<UserAccessModel> userWithAccess;
    private boolean cleanWorkspace;
    private boolean dontUseBuildServer;

    public BuildConfigurationModel() {
        initCreator();
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    private void initCreator() {
        User user = User.current();
        if (user != null) {
            creator = user.getId();
        } else {
            creator = BuildConfigurationManager.STRING_EMPTY;
        }
    }

    public void initCurrentDate() {
        DateFormat df = new SimpleDateFormat(BuildConfigurationManager.DATE_FORMAT);
        Date dateobj = new Date();
        date = df.format(dateobj);
    }

    public String getFullNameCreator() {
        return User.get(creator).getFullName();
    }

    public String getDate() {
        return date;
    }

    public void setProjectName(String value) {
        projectName = value;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setScm(String value) {
        scm = value;
    }

    public String getScm() {
        return scm;
    }

    public Boolean getJobUpdate() {
        return isJobUpdate;
    }

    public void setJobUpdate(Boolean value) {
        isJobUpdate = value;
    }

    public void setBuildMachineConfiguration(String[] value) {
        buildMachineConfiguration = value;
    }

    public String[] getBuildMachineConfiguration() {
        return buildMachineConfiguration;
    }

    public void setEmail(String value) {
        email = value;
    }

    public String getEmail() {
        return email;
    }

    public void setConfigEmail(String value) {
        configEmail = value;
    }

    public String getConfigEmail() {
        return configEmail;
    }

    public void setProjectToBuild(List<ProjectToBuildModel> value) {
        projectToBuild = value;
    }

    public List<ProjectToBuildModel> getProjectToBuild() {
        return projectToBuild;
    }

    public String getPreScript() {
        return preScript;
    }

    public void setPreScript(String value) {
        preScript = value;
    }

    public String getPostScript() {
        return postScript;
    }

    public void setPostScript(String value) {
        postScript = value;
    }

    public void setScripts(String[] value) {
        scripts = value;
    }

    public String[] getScripts() {
        return scripts;
    }

    public void setState(ConfigurationState value) {
        state = value;
    }

    public ConfigurationState getState() {
        return state;
    }

    public void setCreator(String value) {
        creator = value;
    }

    public String getCreator() {
        return creator;
    }

    public void setRejectionReason(String reject) {
        rejectionReason = reject;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public String getScriptType() {
        return scriptType;
    }

    public void setScriptType(String scriptType) {
        this.scriptType = scriptType;
    }

    public List<UserAccessModel> getUserWithAccess() {
        return userWithAccess;
    }

    public void setUserWithAccess(List<UserAccessModel> userWithAccess) {
        this.userWithAccess = userWithAccess;
    }

    public boolean isCleanWorkspace() {
        return cleanWorkspace;
    }

    public void setCleanWorkspace(boolean cleanWorkspace) {
        this.cleanWorkspace = cleanWorkspace;
    }

    public boolean isDontUseBuildServer() {
        return dontUseBuildServer;
    }

    public void setDontUseBuildServer(boolean dontUseBuildServer) {
        this.dontUseBuildServer = dontUseBuildServer;
    }

}
