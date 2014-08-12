package com.amcbridge.jenkins.plugins.configurator;

import java.io.IOException;
import javax.servlet.ServletException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import hudson.Extension;
import hudson.model.RootAction;

@Extension
public final class BuildConfigurator implements RootAction
{
     BuildConfiguration buildConfig;

     public BuildConfigurator()
     {
         buildConfig = new BuildConfiguration();
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

     public void doSave(final StaplerRequest request, final StaplerResponse response) throws ServletException, IOException
     { 
          try
          {
              JSONObject formData = request.getSubmittedForm();
              String textName = formData.getString("TextName");
              buildConfig = new BuildConfiguration(textName);
              buildConfig.save();
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
}
