package com.amcbridge.jenkins.plugins.configurator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.amcbridge.jenkins.plugins.xmlSerialization.Job;
import com.amcbridge.jenkins.plugins.xmlSerialization.XmlExporter;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;

public class ApproveConfigurationManager {

	private static final String FILE_NAME = "approveConfigurations.xml";
	private static List<Job> configurations;
	
	public ApproveConfigurationManager()
	{
		if (configurations == null)
		{
			load();
		}
	}
	
	private void load()
	{
		configurations = new ArrayList<Job>();
		
		File rootDirectory = new File(BuildConfigurationManager.getRootDirectory());
		if (!rootDirectory.exists())
		{
			rootDirectory.mkdirs();
		}
		
		File file = getFile();
		if (!file.exists())
		{
			try {
				save();
			} catch (IOException e) {}
			return;
		}

		XStream xstream = new XStream(new PureJavaReflectionProvider());
		xstream.processAnnotations(XmlExporter.class);
		xstream.setClassLoader(XmlExporter.class.getClassLoader());
		xstream.addImplicitCollection(XmlExporter.class, "configurations"); 
		configurations = (List<Job>) xstream.fromXML(file);
	}
	
	private void save() throws IOException
	{
		XStream xstream = new XStream();
		xstream.processAnnotations(XmlExporter.class);
		xstream.addImplicitCollection(XmlExporter.class, "configurations");
		
		File file = getFile();
		if (!file.exists())
		{
			file.createNewFile();
		}
		
		FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
		Writer out = new OutputStreamWriter(fos, BuildConfigurationManager.ENCODING);
		
		try
		{
			out.write(xstream.toXML(configurations));
		}
		finally
		{
			out.close();
			fos.close();
		}
	}
	
	public void add(Job job) throws IOException
	{
		Job old = get(job.getName());
		if (old != null)
		{
			configurations.remove(old);
		}
		configurations.add(job);
		save();
	}
	
	public void remove(String name) throws IOException
	{
		Job job = get(name);
		if (job != null)
		{
			configurations.remove(job);
			save();
		}
	}
	
	private File getFile()
	{
		return new File (BuildConfigurationManager.getRootDirectory() + "\\" + FILE_NAME);
	}
	
	public Job get(String name)
	{
		for (Job job : configurations)
		{
			if (job.getName().equals(name))
			{
				return job;
			}
		}
		return null;
	}
}
