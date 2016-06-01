package com.amcbridge.jenkins.plugins.xstreamElements;

import java.io.File;
import java.util.List;

import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;
import com.amcbridge.jenkins.plugins.exceptions.JenkinsInstanceNotFoundException;
import jenkins.model.Jenkins;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("builders")
public class BuilderLoader {

    private static final String BUILDER = "/plugins/build-configurator/builder/Builders.xml";
    List<Builder> builders;

    public BuilderLoader() throws JenkinsInstanceNotFoundException {
        load();
    }

    public List<Builder> getBuilders() {
        return builders;
    }

    private void load() throws JenkinsInstanceNotFoundException {
        XStream xstream = new XStream();
        xstream.addImplicitCollection(BuilderLoader.class, "builders");
        xstream.processAnnotations(BuilderLoader.class);
        File file = new File(BuildConfigurationManager.getJenkins().getRootDir() + BUILDER);
        xstream.setClassLoader(Builder.class.getClassLoader());
        builders = ((BuilderLoader) xstream.fromXML(file)).getBuilders();
    }
}
