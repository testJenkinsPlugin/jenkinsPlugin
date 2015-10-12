package com.amcbridge.jenkins.plugins.enums;

import java.io.File;
import java.util.List;
import jenkins.model.Jenkins;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("builders")
public class Builder {

    private static final String BUILDER = "\\plugins\\configurator\\builder\\Builders.xml";
    List<BuilderElement> builders;

    public Builder() {
        load();
    }

    public List<BuilderElement> getBuilders() {
        return builders;
    }

    private void load() {
        XStream xstreamm = new XStream();
        xstreamm.addImplicitCollection(Builder.class, "builders");
        xstreamm.processAnnotations(Builder.class);
        File file = new File(Jenkins.getInstance().getRootDir() + BUILDER);
        xstreamm.setClassLoader(BuilderElement.class.getClassLoader());
        builders = ((Builder) xstreamm.fromXML(file)).getBuilders();
    }
}
