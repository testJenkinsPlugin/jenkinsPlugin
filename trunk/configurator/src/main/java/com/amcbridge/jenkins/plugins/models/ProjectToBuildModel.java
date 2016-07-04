package com.amcbridge.jenkins.plugins.models;

import com.amcbridge.jenkins.plugins.serialization.CredentialItem;
import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;

public class ProjectToBuildModel {
    private String projectUrl, fileToBuild, localDirectoryPath, branchName, credentials;
    private Boolean isVersionFiles;
    private List<BuilderConfigModel> builders;
    private String[] artefacts, versionFiles;
    private UUID guid;

    public ProjectToBuildModel() {
    }


    @DataBoundConstructor
    public ProjectToBuildModel(String projectUrl, String credentials, String branchName, String fileToBuild,
                               String artefacts, String versionFiles, String localDirectoryPath,
                               Boolean isVersionFiles, List <BuilderConfigModel> builders, String guid) {
        this.projectUrl = projectUrl;
        this.credentials = credentials;
        this.branchName = branchName;
        this.fileToBuild = fileToBuild;
        this.artefacts = BuildConfigurationManager.getPath(artefacts);
        this.versionFiles = BuildConfigurationManager.getPath(versionFiles);
        this.localDirectoryPath = localDirectoryPath;
        this.isVersionFiles = isVersionFiles;
        this.builders = builders;

        if (guid == null || guid.equals("")) {
            this.guid = UUID.randomUUID();
        } else {
            this.guid = UUID.fromString(guid);
        }
    }

    public static List<String> getCredentialsList() {
        List<String> result = new ArrayList<>();
        try {
            List<CredentialItem> items = BuildConfigurationManager.openCredentials();
            if (!items.isEmpty()) {
                for (CredentialItem item : items) {
                    result.add(item.getDisplayName());
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ProjectToBuildModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public static Boolean isCredentialSelected(String curCredentials, String testedCredential) {
        if ((curCredentials == null) || (testedCredential == null)) {
            return false;
        } else {
            return curCredentials.equalsIgnoreCase(testedCredential);
        }
    }

    public static String getCredentialId(String curCredentials) {
        // credential looks like: "credentialsName";"credentialsId"
        int idPosition = 1;
        String res = "";
        if (curCredentials.isEmpty()) {
            return res;
        }
        String[] splittedCredentials = curCredentials.split(";");
        if (splittedCredentials.length > 1) {
            return splittedCredentials[idPosition].trim();
        } else {
            return res;
        }
    }

    public static String getCredentialsNameById(String credentialsId){
        if(credentialsId.equals("not selected")){
            return credentialsId;
        }
        List<String> credentialsList = getCredentialsList();
        for (String credentialsFull: credentialsList){
            String credentialsToCheck = getCredentialId(credentialsFull);
            if (credentialsToCheck.equals(credentialsId)){
                return getCredentialName(credentialsFull);
            }
        }
        return "Credentials with given ID not found on server";
    }

    public static String getCredentialName(String curCredentials) {
        // credential looks like: "credentialsName";"credentialsId"
        int credentialsNamePosition = 0;
        String res = "";
        if (curCredentials.isEmpty()) {
            return res;
        }
        String[] splittedCredentials = curCredentials.split(";");
        if (splittedCredentials.length > 1) {
            res = splittedCredentials[credentialsNamePosition].trim();
        }
        return res;

    }


    public String[] getVersionFiles() {
        return versionFiles;
    }

    public void setCredentials(String value) {
        credentials = value;
    }

    public String getCredentials() {
        return credentials;
    }

    public void setProjectUrl(String value) {
        projectUrl = value;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public void setBranchName(String value) {
        branchName = value;
    }

    public String getBranchName() {
        return branchName;
    }

    public Boolean IsVersionFiles() {
        return isVersionFiles;
    }

    public void setFileToBuild(String value) {
        fileToBuild = value;
    }

    public String getFileToBuild() {
        return fileToBuild;
    }

    public void setLocalDirectoryPath(String value) {
        localDirectoryPath = value;
    }

    public String getLocalDirectoryPath() {
        return localDirectoryPath;
    }

    public void setBuilders(List<BuilderConfigModel> value) {
        builders = value;
    }

    public List<BuilderConfigModel> getBuilders() {
        return builders;
    }

    public String[] getArtefacts() {
        return artefacts;
    }

    public void setArtefacts(String[] values) {
        artefacts = values;
    }

    public void setVersionFiles(String[] values) {
        versionFiles = values;
    }

    public UUID getGuid() {
        return guid;
    }

    public void setGuid(UUID guid) {
        this.guid = guid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectToBuildModel that = (ProjectToBuildModel) o;

        if (projectUrl != null ? !projectUrl.equals(that.projectUrl) : that.projectUrl != null) return false;
        if (fileToBuild != null ? !fileToBuild.equals(that.fileToBuild) : that.fileToBuild != null) return false;
        if (localDirectoryPath != null ? !localDirectoryPath.equals(that.localDirectoryPath) : that.localDirectoryPath != null)
            return false;
        if (branchName != null ? !branchName.equals(that.branchName) : that.branchName != null) return false;
        if (credentials != null ? !credentials.equals(that.credentials) : that.credentials != null) return false;
        if (isVersionFiles != null ? !isVersionFiles.equals(that.isVersionFiles) : that.isVersionFiles != null)
            return false;
        if (builders != null ? !builders.equals(that.builders) : that.builders != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(artefacts, that.artefacts)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(versionFiles, that.versionFiles)) return false;
        return guid != null ? guid.equals(that.guid) : that.guid == null;

    }

    @Override
    public int hashCode() {
        int result = projectUrl != null ? projectUrl.hashCode() : 0;
        result = 31 * result + (fileToBuild != null ? fileToBuild.hashCode() : 0);
        result = 31 * result + (localDirectoryPath != null ? localDirectoryPath.hashCode() : 0);
        result = 31 * result + (branchName != null ? branchName.hashCode() : 0);
        result = 31 * result + (credentials != null ? credentials.hashCode() : 0);
        result = 31 * result + (isVersionFiles != null ? isVersionFiles.hashCode() : 0);
        result = 31 * result + (builders != null ? builders.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(artefacts);
        result = 31 * result + Arrays.hashCode(versionFiles);
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        return result;
    }
}
