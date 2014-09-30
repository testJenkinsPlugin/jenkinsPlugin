package com.amcbridge.jenkins.plugins.configurator;

import hudson.XmlFile;
import hudson.model.User;
import hudson.tasks.Mailer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.ParserConfigurationException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import jenkins.model.Jenkins;

@XStreamAlias("buildConfiguration")
@XmlRootElement
public class BuildConfiguration {

    private static final String CONFIG_FILE_NAME = "config.xml";
    private static final String BUILD_CONFIGURATOR_DIRECTORY_NAME = "BuildConfiguration";
    
    static {Jenkins.XSTREAM.processAnnotations(BuildConfiguration.class);}

    private String state, creator, projectName, url, sourceControlTool, buildMachineConfiguration, email, configuration, userConfiguration;
    private String[] files, artefacts, versionFile, scripts;
    private List<String> builders, platforms;
    private Date date;
    
    public BuildConfiguration()
    {
    	setCreator();
 	    date = new Date();
    	builders = new ArrayList<String>();
    	platforms = new ArrayList<String>();
    }
    
    public void setState(String newState)
    {
    	state = newState;
    }
    
    public String getProjectName()
    {
    	return projectName;
    }
    
    public String getState()
    {
    	return state;
    }
    
    public String getDate()
    {
    	return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
    
    public void setUserConfiguration(String userConfig)
    {
    	userConfiguration = userConfig;
    }
    
    public void setConfiguration(String config)
    {
    	configuration = config;
    }
    
    public void setScripts(String[] Scripts)
    {
    	scripts = Scripts;
    }
    
    public void setVersionFile(String[] VersionFile)
    {
    	versionFile = VersionFile;
    }
    
    public void setArtefacts(String[] Artefacts)
    {
    	artefacts = Artefacts;
    }
    
    public void setFiles(String[] Files)
    {
    	files = Files;
    }
    
    public void addPlatform(String Platform)
    {
    	platforms.add(Platform);
    }
    
    public void addBuilders(String value)
    {
    	builders.add(value);
    }
    
    public void setEmail(String Email)
    {
    	email = Email;
    }
    
    public void setBuildMachineConfiguration(String config)
    {
    	buildMachineConfiguration = config;
    }
    
    public void setSourceControlTool(String control)
    {
    	sourceControlTool = control;
    }
    
    public void setProjectName(String name)
    {
    	projectName = name;
    }
    
    public void setUrl(String URL)
    {
    	url = URL;
    }
    
    void setCreator()
    {
    	if (User.current()!=null)
    		creator = User.current().getProperty(Mailer.UserProperty.class).getAddress();
    	else
    		creator = "";
    }
    
    public static String getCurrentUserMail()
    {
    	if (User.current()!=null)
    		return User.current().getProperty(Mailer.UserProperty.class).getAddress();
    	else
    		return "";
    }
    
    public String getCreator()
    {
        return creator;
    }

    public static File getConfigFileFor(String id) 
    {
    	
        return new File(new File(getRootDir(), id), CONFIG_FILE_NAME);
    }

    static File getRootDir() 
    {
        return new File(Jenkins.getInstance().getRootDir(), BUILD_CONFIGURATOR_DIRECTORY_NAME);
    }

    public static String getRootDirectory()
    {
    	return Jenkins.getInstance().getRootDir()+"\\" + BUILD_CONFIGURATOR_DIRECTORY_NAME;
    }
    
    public void save() throws IOException, ParserConfigurationException, JAXBException 
    {
    	if (projectName.isEmpty())
	    	return;
    	File checkFile = new File(getRootDirectory()+"\\"+projectName);
    	if (!checkFile.exists())
    		checkFile.mkdirs();
    	XStream xstream = new XStream();
    	xstream.alias("BuildConfiguration",  BuildConfiguration.class);
    	XmlFile fileWriter = new XmlFile(Jenkins.XSTREAM, getConfigFileFor("\\"+projectName));
        fileWriter.write(this); 
    }
    
    protected final static XmlFile getConfigFile(String nameProject) 
    {
        return new XmlFile(Jenkins.XSTREAM, getConfigFileFor("\\"+nameProject));
    }
    
    public static BuildConfiguration load(String nameProject) throws IOException
    {
    	BuildConfiguration result = new BuildConfiguration();
    	XmlFile config = getConfigFile(nameProject);
        if (config.exists()) 
        {
            config.unmarshal(result);
        }
    	return result;
    }
}