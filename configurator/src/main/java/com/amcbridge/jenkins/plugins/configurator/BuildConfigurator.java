package com.amcbridge.jenkins.plugins.configurator;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.ServletException;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import jenkins.model.JenkinsLocationConfiguration;
import net.sf.json.JSONObject;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.Extension;
import hudson.model.RootAction;

@Extension
public final class BuildConfigurator implements RootAction
{
     MailSender mail;
     
     public BuildConfigurator()
     {
         mail = new MailSender();
     }

     public String getDisplayName()
     {
          return "Build Configurator";
     }

     public String getIconFileName() 
     {
         return "/plugin/configurator/icons/system_config_services.png";
     }

     public String getUrlName()
     {
         return "BuildConfigurator";
     }
     
     public Object getAllConfiguration() throws IOException
     {
    	 BuildConfiguration currenConfig;
    	 String currentUser;
    	 String[] config;
    	 File currentConfigFile;
    	 List<String[]> configurations = new ArrayList<String[]>();
    	 File file = new File(BuildConfiguration.getRootDirectory());
    	 File[] directories = file.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
    	 for (int i=0; i < directories.length; i++)
    	 {
    		 currentConfigFile = BuildConfiguration.getConfigFileFor("\\"+directories[i].getName());
    		 if (!currentConfigFile.exists())
    			 continue;
    		 currenConfig = null;
    		 currenConfig = BuildConfiguration.load(directories[i].getName());
    		 currentUser = BuildConfiguration.getCurrentUserMail();
    		 if (!getAdminEmails().equals(currentUser) && !isCurrentUserCreator(currenConfig))
    			 continue;
    		 config = new String[4];
    		 config[0] = currenConfig.getProjectName();
    		 config[1] = currenConfig.getState();
    		 config[2] = currenConfig.getDate();
    		 if (isCurrentUserCreator(currenConfig))
    			 config[3] = "true";
    		 else
    			 config[3] = "false";
    		 configurations.add(config);
    	 }
    	return configurations.toArray();
     }
     
     public void doCreateNewConfigurator(final StaplerRequest request, final StaplerResponse response) throws IOException, ServletException, ParserConfigurationException, JAXBException, AddressException, MessagingException
     {
    	 JSONObject formAttribute = request.getSubmittedForm();
    	 if (formAttribute.get("formResultHidden")!=null && formAttribute.get("formResultHidden").toString().equals("cancel"))
    	 {
    		 response.sendRedirect("../BuildConfigurator");
    		 return;
    	 }
    	 BuildConfiguration newConfig = new BuildConfiguration();
    	 newConfig.setState("New");  	 
    	 request.bindJSON(newConfig, request.getSubmittedForm());
    	 newConfig.setFiles(formAttribute.get("fileHidden").toString().split(";"));
    	 newConfig.setArtefacts(formAttribute.get("artefactsHidden").toString().split(";"));
    	 newConfig.setVersionFile(formAttribute.get("versionFileHidden").toString().split(";"));
    	 newConfig.setScripts(formAttribute.get("scriptsHidden").toString().split(";"));
    	 if (formAttribute.get("vs2005")!=null && (Boolean)formAttribute.get("vs2005"))
    		 newConfig.addBuilders(Builder.VS_2005);
    	 if (formAttribute.get("vs2008")!=null && (Boolean)formAttribute.get("vs2008"))
    		 newConfig.addBuilders(Builder.VS_2008);
    	 if (formAttribute.get("vs2010")!=null && (Boolean)formAttribute.get("vs2010"))
    		 newConfig.addBuilders(Builder.VS_2010);
    	 if (formAttribute.get("vs2012")!=null && (Boolean)formAttribute.get("vs2012"))
    		 newConfig.addBuilders(Builder.VS_2012);
    	 if (formAttribute.get("vs2013")!=null && (Boolean)formAttribute.get("vs2013"))
    		 newConfig.addBuilders(Builder.VS_2013);
    	 if (formAttribute.get("xCode_4_1")!=null && (Boolean)formAttribute.get("xCode_4_1"))
    		 newConfig.addBuilders(Builder.XCODE_4_1);
    	 if (formAttribute.get("java8")!=null && (Boolean)formAttribute.get("java8"))
    		 newConfig.addBuilders(Builder.JAVA_8);
    	 if (formAttribute.get("x86")!=null && (Boolean)formAttribute.get("x86"))
    		 newConfig.addPlatform(Platform.X86);
    	 if (formAttribute.get("x64")!=null && (Boolean)formAttribute.get("x64"))
    		 newConfig.addPlatform(Platform.X64);
    	 if (formAttribute.get("anyCpu")!=null && (Boolean)formAttribute.get("anyCpu"))
    		 newConfig.addPlatform(Platform.ANY_CPY);
    	 if (formAttribute.get("win32")!=null && (Boolean)formAttribute.get("win32"))
    		 newConfig.addPlatform(Platform.WIN_32);
    	 newConfig.save();
    	 String message = "New configuration '" + newConfig.getProjectName() + "' was successfully created!";
    	 mail.sendMail(getAdminEmails(), message, "New configuration");
    	 response.sendRedirect("./");
     }

     public Boolean isCurrentUserCreator(BuildConfiguration config)
     {
    	 return BuildConfiguration.getCurrentUserMail().equals(config.getCreator());
     }
     
     public static Boolean isCurrentUserAdministrator()
     {
    	 String currentUser = BuildConfiguration.getCurrentUserMail();
    	 if (currentUser!="" && getAdminEmails().equals(currentUser))
    		 return true;
    	 else
    		 return false;
     }
     
     public static BuildConfiguration getConfiguration(String name) throws IOException
     {
    	 return BuildConfiguration.load(name);
     }
     
    public static String getAdminEmails()
    {
        return JenkinsLocationConfiguration.get().getAdminAddress();
    }
}
