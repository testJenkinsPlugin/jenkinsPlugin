package com.amcbridge.jenkins.plugins.xstreamelements;

import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;
import com.amcbridge.jenkins.plugins.exceptions.JenkinsInstanceNotFoundException;
import com.thoughtworks.xstream.XStream;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ScriptTypeLoader {
    private static final String SCRIPTS = "/plugins/build-configurator/config/ScriptTypes.xml";
    private List<ScriptType> scriptTypeList = new LinkedList<>();

    public ScriptTypeLoader() throws JenkinsInstanceNotFoundException {
        scriptTypeList = new LinkedList<>();
        load();
    }


    public List<ScriptType> getScriptTypeList() {
        return scriptTypeList;
    }

    public void setScriptTypeList(List<ScriptType> scriptTypeList) {
        this.scriptTypeList = scriptTypeList;
    }

    public void add(ScriptType scriptType) {
        scriptTypeList.add(scriptType);
    }



    public void load() throws JenkinsInstanceNotFoundException {
        XStream xstream = new XStream();
        xstream.alias("scriptTypes", ScriptTypeLoader.class);
        xstream.alias("scriptType", ScriptType.class);
        xstream.addImplicitCollection(ScriptTypeLoader.class, "scriptTypeList");
        xstream.setClassLoader(com.amcbridge.jenkins.plugins.xstreamelements.ScriptTypeLoader.class.getClassLoader());
        scriptTypeList = ((ScriptTypeLoader) xstream.fromXML(new File(BuildConfigurationManager.getJenkins().getRootDir() + SCRIPTS))).getScriptTypeList();
    }
}
