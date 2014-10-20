package com.amcbridge.jenkins.plugins.configurator;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.DirectoryFileFilter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("configurations")
public class XmlExporter
{
	private static final String XML_TITLE = "<?xml version='1.0' encoding='UTF-8'?>\n";

	private List<BuildConfiguration> configurations;

	public List<BuildConfiguration> getConfigurations()
	{
		return configurations;
	}

	public void setConfigurations(List<BuildConfiguration> value)
	{
		configurations = value;
	}

	public XmlExporter()
	{
		configurations = new ArrayList<BuildConfiguration>();
	}

	public String exportToXml() throws IOException
	{
		List<BuildConfiguration> configs = new ArrayList<BuildConfiguration>();
		BuildConfiguration config = null;
		File file = new File(BuildConfigurationManager.getRootDirectory());

		if (!file.exists())
			return BuildConfigurationManager.STRING_EMPTY;

		File[] directories = file.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
		for (int i = 0; i < directories.length; i++)
		{
			config = BuildConfigurationManager.load(directories[i].getName());
			if (config.getState().equals(ConfigurationState.APPROVED))
			{
				config.setState(null);
				configs.add(config);
			}
		}
		configurations = configs;
		return saveConfigurations();
	}

	String saveConfigurations() throws IOException
	{
		String path;
		XStream xstream = new XStream();
		xstream.processAnnotations(XmlExporter.class);
		xstream.addImplicitCollection(XmlExporter.class, "configurations");
		File outputFile = BuildConfigurationManager.getFileToExportConfigurations();
		if (!outputFile.exists())
			outputFile.createNewFile();
		path = outputFile.getPath();
		FileOutputStream fos = new FileOutputStream(path);
		try
		{
			fos.write(XML_TITLE.getBytes());
			fos.write(xstream.toXML(this).getBytes());
		}
		finally
		{
			fos.close();
		}
		return path;
	}
}
