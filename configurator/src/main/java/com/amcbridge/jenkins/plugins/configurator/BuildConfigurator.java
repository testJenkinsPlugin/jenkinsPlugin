package com.amcbridge.jenkins.plugins.configurator;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.ServletException;

import jenkins.model.JenkinsLocationConfiguration;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.Extension;
import hudson.model.RootAction;

@Extension
public final class BuildConfigurator implements RootAction
{
     BuildConfiguration buildConfig;
     MailSender mail;
     
     public BuildConfigurator()
     {
         buildConfig = new BuildConfiguration();
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

     public void doSave(final StaplerRequest request, final StaplerResponse response) throws ServletException, IOException, AddressException, MessagingException
     { 
          try
          {
              JSONObject formData = request.getSubmittedForm();
              String textName = formData.getString("TextName");
              buildConfig = new BuildConfiguration(textName);
              buildConfig.save();
              mail.sendMail(getAdminEmails(), buildConfig.getCreator());
           }
           finally
           {
             response.sendRedirect("../BuildConfigurator");            
           }
     }

    private void load() throws IOException
    {
        buildConfig.load();
    }

    public BuildConfiguration getBuildConfig() throws IOException 
    {
        try
        {
            if (buildConfig.getName() == "")
            { 
                load();
            }
        }
        finally
        {
            return buildConfig;
        }
    }
    
    public String getAdminEmails()
    {
        return JenkinsLocationConfiguration.get().getAdminAddress();
    }
}
