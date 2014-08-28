package com.amcbridge.jenkins.plugins.configurator;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.ServletException;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import jenkins.model.JenkinsLocationConfiguration;

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
     
     public void doCreateNewConfigurator(final StaplerRequest request, final StaplerResponse response) throws IOException, ServletException, ParserConfigurationException, JAXBException, AddressException, MessagingException
     {
    	 if (request.getParameter("cancelButton")!=null)
    	 {
    		 response.sendRedirect("../BuildConfigurator");
    		 return;
    	 }
    	 BuildConfiguration newConfig = new BuildConfiguration();
    	 newConfig.setState("New");  	 
    	 request.bindJSON(newConfig, request.getSubmittedForm());
    	 newConfig.setFiles(request.getParameter("fileHidden").split(";"));
    	 newConfig.setArtefacts(request.getParameter("artefactsHidden").split(";"));
    	 newConfig.setVersionFile(request.getParameter("versionFileHidden").split(";"));
    	 newConfig.setScripts(request.getParameter("scriptsHidden").split(";"));
    	 if (request.getParameter("vs2005")!=null && request.getParameter("vs2005").equals("on"))
    		 newConfig.addBuilders(Builder.VS_2005);
    	 if (request.getParameter("vs2008")!=null && request.getParameter("vs2008").equals("on"))
    		 newConfig.addBuilders(Builder.VS_2008);
    	 if (request.getParameter("vs2010")!=null && request.getParameter("vs2010").equals("on"))
    		 newConfig.addBuilders(Builder.VS_2010);
    	 if (request.getParameter("vs2012")!=null && request.getParameter("vs2012").equals("on"))
    		 newConfig.addBuilders(Builder.VS_2012);
    	 if (request.getParameter("vs2013")!=null && request.getParameter("vs2013").equals("on"))
    		 newConfig.addBuilders(Builder.VS_2013);
    	 if (request.getParameter("xCode_4_1")!=null && request.getParameter("xCode_4_1").equals("on"))
    		 newConfig.addBuilders(Builder.XCODE_4_1);
    	 if (request.getParameter("java8")!=null && request.getParameter("java8").equals("on"))
    		 newConfig.addBuilders(Builder.JAVA_8);
    	 if (request.getParameter("x86")!=null && request.getParameter("x86").equals("on"))
    		 newConfig.addPlatform(Platform.X86);
    	 if (request.getParameter("x64")!=null && request.getParameter("x64").equals("on"))
    		 newConfig.addPlatform(Platform.X64);
    	 if (request.getParameter("anyCpu")!=null && request.getParameter("anyCpu").equals("on"))
    		 newConfig.addPlatform(Platform.ANY_CPY);
    	 if (request.getParameter("win32")!=null && request.getParameter("win32").equals("on"))
    		 newConfig.addPlatform(Platform.WIN_32);
    	 newConfig.save();
    	 String message = "New configuration '" + newConfig.getProjectName() + "' was successfully created!";
    	 mail.sendMail(getAdminEmails(), message, "New configuration");
    	 response.sendRedirect("./");
     }
    
    public String getAdminEmails()
    {
        return JenkinsLocationConfiguration.get().getAdminAddress();
    }
}
