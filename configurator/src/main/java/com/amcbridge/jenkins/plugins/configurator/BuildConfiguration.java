package com.amcbridge.jenkins.plugins.configurator;

import hudson.XmlFile;
import hudson.model.User;
import hudson.tasks.Mailer;

import java.io.File;
import java.io.IOException;

import jenkins.model.Jenkins;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("buildConfiguration")
public class BuildConfiguration {

    private static final String CONFIG_FILE_NAME = "config.xml";
    private static final String BUILD_CONFIGURATOR_DIRECTORY_NAME = "BuildConfiguration";

    static {Jenkins.XSTREAM.processAnnotations(BuildConfiguration.class);}

    private String entityName;
    private String creator;

    public BuildConfiguration(String name)
    {
    	initialize(name);
    }

    public BuildConfiguration()
    {
    	initialize("");
    }
    
    void initialize(String name)
    {
    	this.entityName = name;
        setCreator();
    }
    
    void setCreator()
    {
        creator = User.current().getProperty(Mailer.UserProperty.class).getAddress();
    }

    public String getName()
    {
        return entityName;
    }

    public void setName(String name) 
    {
        this.entityName = name;
    }

    public String getCreator()
    {
        return creator;
    }
    
    public void setCreator(String creat) 
    {
        this.creator = creat;
    }

    private static File getConfigFileFor(String id) 
    {
        return new File(new File(getRootDir(), id), CONFIG_FILE_NAME);
    }

    static File getRootDir() 
    {
        return new File(Jenkins.getInstance().getRootDir(), BUILD_CONFIGURATOR_DIRECTORY_NAME);
    }

    public void save() throws IOException 
    {
        XStream xs = new XStream();
        xs.alias("BuildConfiguration",  BuildConfiguration.class);
        XmlFile fileWriter = new XmlFile(Jenkins.XSTREAM, getConfigFileFor(BUILD_CONFIGURATOR_DIRECTORY_NAME));
        fileWriter.write(this); 
    }

    protected final XmlFile getConfigFile() 
    {
        return new XmlFile(Jenkins.XSTREAM, getConfigFileFor(BUILD_CONFIGURATOR_DIRECTORY_NAME));
    }

    public void load() throws IOException
    {
        XmlFile config = getConfigFile();
        if (config.exists()) 
        {
            config.unmarshal(this);
        }
    }
}