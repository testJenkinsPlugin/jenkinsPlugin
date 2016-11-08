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
    private String regExp;
    private String pollSCMTrigger;
    private String buildPeriodicallyTrigger;
    private boolean buildOnCommitTrigger;

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

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setScm(String scm) {
        this.scm = scm;
    }

    public String getScm() {
        return scm;
    }

    public Boolean getJobUpdate() {
        return isJobUpdate;
    }

    public void setJobUpdate(Boolean isJobUpdate) {
        this.isJobUpdate = isJobUpdate;
    }

    public void setBuildMachineConfiguration(String[] buildMachineConfiguration) {
        this.buildMachineConfiguration = buildMachineConfiguration;
    }

    public String[] getBuildMachineConfiguration() {
        return buildMachineConfiguration;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setConfigEmail(String configEmail) {
        this.configEmail = configEmail;
    }

    public String getConfigEmail() {
        return configEmail;
    }

    public void setProjectToBuild(List<ProjectToBuildModel> projectToBuild) {
        this.projectToBuild = projectToBuild;
    }

    public List<ProjectToBuildModel> getProjectToBuild() {
        return projectToBuild;
    }

    public String getPreScript() {
        return preScript;
    }

    public void setPreScript(String preScript) {
        this.preScript = preScript;
    }

    public String getPostScript() {
        return postScript;
    }

    public void setPostScript(String postScript) {
        this.postScript = postScript;
    }

    public void setScripts(String[] scripts) {
        this.scripts = scripts;
    }

    public String[] getScripts() {
        return scripts;
    }

    public void setState(ConfigurationState state) {
        this.state = state;
    }

    public ConfigurationState getState() {
        return state;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreator() {
        return creator;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
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

    public String getRegExp() {
        return regExp;
    }

    public void setRegExp(String regExp) {
        this.regExp = regExp;
    }

    public String getPollSCMTrigger() {
        return pollSCMTrigger;
    }

    public void setPollSCMTrigger(String pollSCMTrigger) {
        this.pollSCMTrigger = pollSCMTrigger;
    }

    public String getBuildPeriodicallyTrigger() {
        return buildPeriodicallyTrigger;
    }

    public void setBuildPeriodicallyTrigger(String buildPeriodicallyTrigger) {
        this.buildPeriodicallyTrigger = buildPeriodicallyTrigger;
    }

    public boolean isBuildOnCommitTrigger() {
        return buildOnCommitTrigger;
    }

    public void setBuildOnCommitTrigger(boolean buildOnCommitTrigger) {
        this.buildOnCommitTrigger = buildOnCommitTrigger;
    }
}
