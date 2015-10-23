package com.amcbridge.jenkins.plugins.xmlSerialization;

public class CredentialItem {

    private String scope;
    private String id;
    private String username;
    private String description;
    private String provider;

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getDisplayName() {
        String result = username + "/***** ";
        if (!description.isEmpty()) {
            result += "(" + description + ")";
        }
        result += " ; " + id;
        return result;
    }
}
