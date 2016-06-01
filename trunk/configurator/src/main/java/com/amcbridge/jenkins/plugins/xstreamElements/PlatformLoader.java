package com.amcbridge.jenkins.plugins.xstreamElements;

import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;
import com.amcbridge.jenkins.plugins.exceptions.JenkinsInstanceNotFoundException;
import com.thoughtworks.xstream.XStream;
import jenkins.model.Jenkins;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

//@XStreamAlias("platforms")
public class PlatformLoader {
    private static final String PLATFORMS = "/plugins/build-configurator/builder/Platforms.xml";
    private List<Platform> platformList = new LinkedList<>();

    public PlatformLoader() throws JenkinsInstanceNotFoundException {
        platformList = new LinkedList<>();
        load();
    }

    public void add(Platform platform) {
        platformList.add(platform);
    }

    public List<Platform> getPlatformList() {

        return platformList;
    }

    public void setPlatformList(List<Platform> platformList) {
        this.platformList = platformList;
    }

    public void load() throws JenkinsInstanceNotFoundException {
        XStream xstream = new XStream();
        xstream.alias("platforms", PlatformLoader.class);
        xstream.alias("platform", Platform.class);
        xstream.addImplicitCollection(PlatformLoader.class, "platformList");
        xstream.setClassLoader(com.amcbridge.jenkins.plugins.xstreamElements.PlatformLoader.class.getClassLoader());
        platformList = ((PlatformLoader) xstream.fromXML(new File(BuildConfigurationManager.getJenkins().getRootDir() + PLATFORMS))).getPlatformList();
    }
}