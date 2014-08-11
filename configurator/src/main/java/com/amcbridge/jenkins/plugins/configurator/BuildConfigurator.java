package com.amcbridge.jenkins.plugins.configurator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.thoughtworks.xstream.XStream;

import javax.servlet.ServletException;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.Extension;
import hudson.XmlFile;
import hudson.model.RootAction;

@Extension
public final class BuildConfigurator implements RootAction
{
	 BuildConfiguration buildConfig = new BuildConfiguration();
	 private String name = "BuildConfiguration";
	 
	 private static final String CONFIG_FILE_NAME = "config.xml";
	 private static final String BuildConfigurator_DIRECTORY_NAME = "configurator";
	 
     static {Jenkins.XSTREAM.processAnnotations(BuildConfiguration.class);}
     
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
     		 String TextBox1 = formData.getString("TextBox1");
     		 buildConfig = new BuildConfiguration(TextBox1);
     		 save();
       	}
       	finally
       	{
             response.sendRedirect("../BuildConfigurator");            
        }
     }

     private static File getConfigFileFor(String id) 
     {
         return new File(new File(getRootDir(), id), CONFIG_FILE_NAME);
     }
     
     private static File getRootDir() 
     {
         return new File(Jenkins.getInstance().getRootDir(), BuildConfigurator_DIRECTORY_NAME);
     }
     
     private void save() throws IOException 
     {
		XStream xs = new XStream();
		xs.alias("BuildConfiguration",  BuildConfiguration.class);
		try 
		{
			XmlFile qewrty = new XmlFile(Jenkins.XSTREAM, getConfigFileFor(name));
			qewrty.write(buildConfig); 
        } 
		catch (FileNotFoundException e1)
		{
            e1.printStackTrace();
        }
     }
		
     protected final XmlFile getConfigFile() 
     {
         return new XmlFile(Jenkins.XSTREAM, getConfigFileFor(name));
     }
     
	private void load() throws IOException 
	{
		XmlFile config = getConfigFile();
        try 
        {
            if (config.exists()) 
            {
                config.unmarshal(buildConfig);
            }
        } 
        catch (IOException e) { }
	 }
		
	public BuildConfiguration getPeople() throws IOException 
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
