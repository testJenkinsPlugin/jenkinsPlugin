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
    private String projectUrl;
    private String fileToBuild;
    private String localDirectoryPath;
    private String branchName;
    private String credentials;
    private Boolean isVersionFiles;
    private List<BuilderConfigModel> builders;
    private String[] artifacts;
    private String[] versionFiles;
    private UUID guid;
    private static Logger log = Logger.getLogger(ProjectToBuildModel.class.getName());

    public ProjectToBuildModel(){}
    @DataBoundConstructor
    public ProjectToBuildModel(String projectUrl, String credentials, String branchName, String fileToBuild,
                               String artifacts, String versionFiles, String localDirectoryPath,
                               Boolean isVersionFiles, List <BuilderConfigModel> builders, String guid) {
        this.projectUrl = projectUrl;
        this.credentials = credentials;
        this.branchName = branchName;
        this.fileToBuild = fileToBuild;
        this.artifacts = BuildConfigurationManager.getPath(artifacts);
        this.versionFiles = BuildConfigurationManager.getPath(versionFiles);
        this.localDirectoryPath = localDirectoryPath;
        this.isVersionFiles = isVersionFiles;
        this.builders = builders;

        if (guid == null || "".equals(guid)) {
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
            log.log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public static Boolean isCredentialSelected(String curCredentials, String testedCredential) {
        return !((curCredentials == null) || (testedCredential == null))
                && curCredentials.equalsIgnoreCase(testedCredential);
    }

    public static String getCredentialId(String curCredentials) {
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
        if("not selected".equals(credentialsId)){
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

    public boolean isListsEqual(String[] array1, String[] array2) {
        List<String> list1 = Arrays.asList(array1);
        List<String> list2 = Arrays.asList(array2);
        return list1.containsAll(list2) && list2.containsAll(list1);
    }

    public String[] getVersionFiles() {
        return versionFiles;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public String getCredentials() {
        return credentials;
    }

    public void setProjectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBranchName() {
        return branchName;
    }

    public Boolean IsVersionFiles() {
        return isVersionFiles;
    }

    public void setFileToBuild(String fileToBuild) {
        this.fileToBuild = fileToBuild;
    }

    public String getFileToBuild() {
        return fileToBuild;
    }

    public void setLocalDirectoryPath(String localDirectoryPath) {
        this.localDirectoryPath = localDirectoryPath;
    }

    public String getLocalDirectoryPath() {
        return localDirectoryPath;
    }

    public void setBuilders(List<BuilderConfigModel> builders) {
        this.builders = builders;
    }

    public List<BuilderConfigModel> getBuilders() {
        return builders;
    }

    public String[] getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(String[] artifacts) {
        this.artifacts = artifacts;
    }

    public void setVersionFiles(String[] versionFiles) {
        this.versionFiles = versionFiles;
    }

    public UUID getGuid() {
        return guid;
    }

    public void setGuid(UUID guid) {
        this.guid = guid;
    }

}
