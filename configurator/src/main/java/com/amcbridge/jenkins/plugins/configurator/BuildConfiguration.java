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
    private static final String CONTENT_FOLDER = "userContent";
    private static final String SCRIPT_FOLDER = "Scripts";
    private static final String[] SCRIPTS_EXTENSIONS = {"bat", "nant", "powershell", "shell", "ant", "maven"};
    
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
    
    public static String getUserContentFolder()
    {
    	return Jenkins.getInstance().getRootDir()+"\\" + CONTENT_FOLDER;
    }
    
    public void save() throws IOException, ParserConfigurationException, JAXBException 
    {
    	if (projectName.isEmpty())
    	{
    		BuildConfigurator.deleteNotUploadFile(scripts);
    		return;
    	}
    	File checkFile = new File(getRootDirectory()+"\\"+projectName);
    	if (!checkFile.exists())
    		checkFile.mkdirs();
    	XStream xstream = new XStream();
    	xstream.alias("BuildConfiguration",  BuildConfiguration.class);
    	XmlFile fileWriter = new XmlFile(Jenkins.XSTREAM, getConfigFileFor("\\"+projectName));
        fileWriter.write(this); 
        saveFile();
    }

    public void saveFile()
    {
    	if (projectName.isEmpty() || scripts.length == 0)
	    	return;
    	String pathFolder = getRootDirectory() + "\\" + projectName + "\\" + SCRIPT_FOLDER;
    	String filePath;
    	File checkFolder = new File(pathFolder);
    	File checkFile;
    	if (!checkFolder.exists())
    		checkFolder.mkdirs();
    	for (int i=0; i<scripts.length; i++)
    	{
    		if (scripts[i].isEmpty())
    			continue;
    		filePath = getUserContentFolder() + "\\" + scripts[i];
    		checkFile = new File (filePath);
    		if (!checkFile.exists())
    		{
    			checkFile = new File (pathFolder + "\\" + scripts[i]);
    			if (!checkFile.exists() || checkFile.length() > 1048576 || checkExtension(checkFile.getName()))
    				scripts[i] = "";
    			continue;
    		}
    		checkFile.renameTo(new File (pathFolder + "\\" + checkFile.getName()));
    	}
    }
    
    private Boolean checkExtension (String fileName)
    {
    	String extension = fileName.substring(0, fileName.lastIndexOf('.'));
    	for (int i=0; i < SCRIPTS_EXTENSIONS.length ; i++)
    	{
    		if (SCRIPTS_EXTENSIONS[i] == extension)
    			return true;
    	}
    	return false;
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