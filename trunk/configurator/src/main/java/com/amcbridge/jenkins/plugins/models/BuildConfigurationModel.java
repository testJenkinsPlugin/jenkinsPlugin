package com.amcbridge.jenkins.plugins.models;

import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;
import com.amcbridge.jenkins.plugins.enums.ConfigurationState;
import hudson.model.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public BuildConfigurationModel() {
        initCreator();
    }

    private void initCreator() {
        User user = User.current();
        if (user != null) {
            creator = user.getId();
        } else {
            creator = BuildConfigurationManager.STRING_EMPTY;
        }
//        cleanWorkspace = true;
    }

    public void initCurrentDate() {
        DateFormat df = new SimpleDateFormat(BuildConfigurationManager.DATE_FORMAT);
        Date dateobj = new Date();
        date = df.format(dateobj);
    }

    public String getFullNameCreator() {
        User user = User.get(creator);
        String fullname = user.getFullName();
        return fullname;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BuildConfigurationModel that = (BuildConfigurationModel) o;

        if (cleanWorkspace != that.cleanWorkspace) return false;
        if (projectName != null ? !projectName.equals(that.projectName) : that.projectName != null) return false;
        if (email != null ? !email.equals(that.email) : that.email != null) return false;
        if (creator != null ? !creator.equals(that.creator) : that.creator != null) return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        if (rejectionReason != null ? !rejectionReason.equals(that.rejectionReason) : that.rejectionReason != null)
            return false;
        if (scm != null ? !scm.equals(that.scm) : that.scm != null) return false;
        if (configEmail != null ? !configEmail.equals(that.configEmail) : that.configEmail != null) return false;
        if (scriptType != null ? !scriptType.equals(that.scriptType) : that.scriptType != null) return false;
        if (preScript != null ? !preScript.equals(that.preScript) : that.preScript != null) return false;
        if (postScript != null ? !postScript.equals(that.postScript) : that.postScript != null) return false;
        if (isJobUpdate != null ? !isJobUpdate.equals(that.isJobUpdate) : that.isJobUpdate != null) return false;
        if (state != that.state) return false;
        if (projectToBuild != null ? !projectToBuild.equals(that.projectToBuild) : that.projectToBuild != null)
            return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(scripts, that.scripts)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(buildMachineConfiguration, that.buildMachineConfiguration)) return false;
        if (comments != null ? !comments.equals(that.comments) : that.comments != null) return false;
        return userWithAccess != null ? userWithAccess.equals(that.userWithAccess) : that.userWithAccess == null;

    }

    @Override
    public int hashCode() {
        int result = projectName != null ? projectName.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (creator != null ? creator.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (rejectionReason != null ? rejectionReason.hashCode() : 0);
        result = 31 * result + (scm != null ? scm.hashCode() : 0);
        result = 31 * result + (configEmail != null ? configEmail.hashCode() : 0);
        result = 31 * result + (scriptType != null ? scriptType.hashCode() : 0);
        result = 31 * result + (preScript != null ? preScript.hashCode() : 0);
        result = 31 * result + (postScript != null ? postScript.hashCode() : 0);
        result = 31 * result + (isJobUpdate != null ? isJobUpdate.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (projectToBuild != null ? projectToBuild.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(scripts);
        result = 31 * result + Arrays.hashCode(buildMachineConfiguration);
        result = 31 * result + (comments != null ? comments.hashCode() : 0);
        result = 31 * result + (userWithAccess != null ? userWithAccess.hashCode() : 0);
        result = 31 * result + (cleanWorkspace ? 1 : 0);
        return result;
    }
}
