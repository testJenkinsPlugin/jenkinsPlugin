package com.amcbridge.jenkins.plugins.xstreamElements;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.File;
import java.util.List;

import jenkins.model.Jenkins;

@XStreamAlias("scms")
public class SCMLoader {

    private static final String SCM = "\\plugins\\build-configurator\\builder\\SCM.xml";
    List<com.amcbridge.jenkins.plugins.xstreamElements.SCM> scms;

    public SCMLoader() {
        load();
    }

    public List<com.amcbridge.jenkins.plugins.xstreamElements.SCM> getSCMs() {
        return scms;
    }

    private void load() {
        XStream xstream = new XStream();
        xstream.addImplicitCollection(SCMLoader.class, "scms");
        xstream.processAnnotations(SCMLoader.class);
        File file = new File(Jenkins.getInstance().getRootDir() + SCM);
        xstream.setClassLoader(com.amcbridge.jenkins.plugins.xstreamElements.SCM.class.getClassLoader());
        scms = ((SCMLoader) xstream.fromXML(file)).getSCMs();
    }

}