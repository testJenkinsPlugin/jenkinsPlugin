package com.amcbridge.jenkins.plugins.configurator;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;
import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import hudson.Extension;
import hudson.model.RootAction;
import hudson.model.User;
import hudson.security.AccessControlled;
import hudson.security.Permission;
import hudson.util.Iterators;

@Extension
public final class BuildConfigurator implements RootAction
{
     private MailSender mail;
     
     public BuildConfigurator()
     {
         mail = new MailSender();
     }

     public String getDisplayName()
     {
    	 if (User.current()==null)
    		 return null;
          return "Build Configurator";
     }

     public String getIconFileName() 
     {
    	 if (User.current()==null)
    		 return null;
         return "/plugin/configurator/icons/system_config_services.png";
     }

     public String getUrlName()
     {
    	 if (User.current()==null)
    		 return null;
         return "BuildConfigurator";
     }
     
     public Object getAllConfiguration() throws IOException, ServletException
     {
    	 BuildConfiguration currenConfig;
    	 String currentUser;
    	 String[] config;
    	 File currentConfigFile;
    	 List<String[]> configurations = new ArrayList<String[]>();
    	 File file = new File(BuildConfiguration.getRootDirectory());
    	 if (!file.exists())
    		 return null;
    	 File[] directories = file.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
    	 for (int i=0; i < directories.length; i++)
    	 {
    		 currentConfigFile = BuildConfiguration.getConfigFileFor("\\"+directories[i].getName());
    		 if (!currentConfigFile.exists())
    			 continue;
    		 currenConfig = null;
    		 currenConfig = BuildConfiguration.load(directories[i].getName());
    		 currentUser = BuildConfiguration.getCurrentUserMail();
    		 if (!isCurrentUserAdministrator() && !isCurrentUserCreator(currenConfig))
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
    		 deleteNotUploadFile(formAttribute.get("scriptsHidden").toString().split(";"));
    		 response.sendRedirect("../BuildConfigurator");
    		 return;
    	 }
    	 BuildConfiguration newConfig = new BuildConfiguration();
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
    	 String message = "", messageTitle="";
    	 String type = formAttribute.get("formType").toString();
    	 if (type.equals("create"))
    	 {
    		 newConfig.setState("New");
    		 message = "New configuration '" + newConfig.getProjectName() + "' was successfully created!";
    		 messageTitle = "New configuration";
    	 }
    	 if (type.equals("edit"))
    	 {
    		 newConfig.setState("Updated");
    		 message = "Configuration '" + newConfig.getProjectName() + "' was changed.";
    		 messageTitle = "Configuration changes";
    	 }
    	 newConfig.save();
    	 mail.sendMail(getAdminEmails(), message, messageTitle);
    	 response.sendRedirect("./");
     }

     public String doUploadFile(final HttpServletRequest  request, final HttpServletResponse  response) throws FileUploadException, IOException 
     {
	     DiskFileItemFactory factory = new DiskFileItemFactory();
	     factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
    	 ServletFileUpload upload = new ServletFileUpload(factory);
    	 List items = upload.parseRequest(request);
    	 Iterator iter = items.iterator();
    	 FileItem item = (FileItem) iter.next();
    	 byte[] data = item.get();
    	 String path = BuildConfiguration.getUserContentFolder();
    	 File checkFile = new File(path);
     	 if (!checkFile.exists())
     		 checkFile.mkdirs();
     	 DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
     	 Date date = new Date();
     	 String fileName = item.getName();
     	 fileName = fileName.substring(0,fileName.lastIndexOf('.'))+ "(" + dateFormat.format(date) + ")" + fileName.substring(fileName.lastIndexOf('.'));
     	 File saveFile = new File(path, fileName);
     	 saveFile.createNewFile();
     	 OutputStream os = new FileOutputStream(saveFile);
         try {
             os.write(data);
         } finally {
             os.close();
         }
    	 return  fileName;
     }

     @JavaScriptMethod
     public static void deleteNotUploadFile(String[] files)
     {
     	String pathFolder = BuildConfiguration.getUserContentFolder();
       	File file;
     	for (int i=0; i<files.length; i++)
     	  {
     		  if (files[i] == "")
     			  continue;
     		  file = new File (pathFolder + "\\" + files[i]);
     		  file.delete();
     	  }
     }
     
     @JavaScriptMethod
     public void setForDeletion(String name) throws IOException, ParserConfigurationException, JAXBException, AddressException, MessagingException
     {
    	 BuildConfiguration config = BuildConfiguration.load(name);
    	 if (config.getState() == "For Deletion")
    		 return;
    	 config.setState("For Deletion");
    	 config.save();
    	 String message = "Configuration '" + config.getProjectName() + "' was marked for deletion!";
    	 mail.sendMail(getAdminEmails(), message, "New configuration was marked for deletion");
     }
     
     @JavaScriptMethod
     public Boolean isNameValid(String name)
     {
    	 File checkName = new File(BuildConfiguration.getRootDirectory() + "\\" + name);
    	 if (checkName.exists())
    		 return false;
    	 else
    		 return true;
     }
     
     @JavaScriptMethod
     public void deleteConfigurationPermanently(String name) throws IOException, AddressException, MessagingException
     {
    	 File checkFile = new File(BuildConfiguration.getRootDirectory() + "\\" + name);
    	 String email = BuildConfiguration.load(name).getEmail();
    	 if (checkFile.exists())
    		 FileUtils.deleteDirectory(checkFile);
    	 if (!email.isEmpty())
    	 {
    		 String message = "Your configuration '" + name + "' was successfully deleted.";
    		 mail.sendMail(getAdminEmails(), message, "Configuration was deleted.");
    	 }
     }
     
     public Boolean isCurrentUserCreator(BuildConfiguration config)
     {
    	 return BuildConfiguration.getCurrentUserMail().equals(config.getCreator());
     }
     
     public static Boolean isCurrentUserAdministrator() throws IOException, ServletException
     {
    	 Object inst = Jenkins.getInstance();
    	 Permission permission = Jenkins.ADMINISTER;
         if (inst instanceof AccessControlled)
             return ((AccessControlled)inst).hasPermission(permission);
         else {
             List<Ancestor> ancs = Stapler.getCurrentRequest().getAncestors();
             for(Ancestor anc : Iterators.reverse(ancs)) {
                 Object o = anc.getObject();
                 if (o instanceof AccessControlled) {
                     return ((AccessControlled)o).hasPermission(permission);
                 }
             }
             return Jenkins.getInstance().hasPermission(permission);
         }
     }

     @JavaScriptMethod
     public BuildConfiguration getConfiguration(String name) throws IOException
     {
    	 BuildConfiguration currenConfig = BuildConfiguration.load(name);
    	 String currentUser = BuildConfiguration.getCurrentUserMail();
		 if (!getAdminEmails().equals(currentUser) && !isCurrentUserCreator(currenConfig))
			 return null;
    	 return BuildConfiguration.load(name);
     }
     
    public static String getAdminEmails()
    {
        return JenkinsLocationConfiguration.get().getAdminAddress();
    }
}
