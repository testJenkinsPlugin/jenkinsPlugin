package com.amcbridge.jenkins.plugins.xstreamelements;

public class ScriptType {

    private String scriptTypeName;
    
    public ScriptType(String scriptTypeName) {
        this.scriptTypeName = scriptTypeName;
    }

    public String getScriptTypeName() {
        return scriptTypeName;
    }

    public void setScriptTypeName(String scriptTypeName) {
        this.scriptTypeName = scriptTypeName;
    }

    @Override
    public String toString() {
        return scriptTypeName;
    }
}
