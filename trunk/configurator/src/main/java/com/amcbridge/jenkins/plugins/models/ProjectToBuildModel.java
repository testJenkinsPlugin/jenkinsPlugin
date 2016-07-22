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
    private String[] artefacts;
    private String[] versionFiles;
    private UUID guid;

    public ProjectToBuildModel(){}
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

    public boolean isListsEqual(String[] array1, String[] array2) {
        List<String> list1 = Arrays.asList(array1);
        List<String> list2 = Arrays.asList(array2);
        return list1.containsAll(list2) && list2.containsAll(list1);
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

}
