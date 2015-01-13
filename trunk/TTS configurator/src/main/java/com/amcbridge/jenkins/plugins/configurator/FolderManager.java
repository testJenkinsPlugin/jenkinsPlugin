package com.amcbridge.jenkins.plugins.configurator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import com.thoughtworks.xstream.XStream;

public class FolderManager {

	private static final String FILE_NAME = "dictionary.xml";
	private FolderInfo folders;

	public FolderManager()
	{
		try {
			load();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getFolderName(Integer id)
	{
		return folders.getName(id);
	}

	public Integer getFolderId(String name)
	{
		return folders.getID(name);
	}

	public void remove(Integer id) throws FileNotFoundException, UnsupportedEncodingException
	{
		folders.remove(id);
		save();
	}

	private void load() throws FileNotFoundException, UnsupportedEncodingException
	{
		folders = new FolderInfo();

		File file = new File(BuildConfigurationManager
				.getRootDirectory() + "\\" + FILE_NAME);

		if (!file.exists())
		{
			save();
		}

		XStream xstream = new XStream();
		xstream.processAnnotations(FolderInfo.class);
		xstream.setClassLoader(FolderInfo.class.getClassLoader());
		folders = (FolderInfo) xstream.fromXML(file);
	}

	public void add(Integer id, String name) throws FileNotFoundException, UnsupportedEncodingException
	{
		name = BuildConfigurationManager.getFolderName(name);
		folders.add(id, name);
		save();
	}

	private void save() throws FileNotFoundException, UnsupportedEncodingException
	{
		XStream xstream = new XStream();
		xstream.processAnnotations(FolderInfo.class);
		xstream.setClassLoader(FolderInfo.class.getClassLoader());
		File file = new File(BuildConfigurationManager
				.getRootDirectory());

		if (!file.exists())
		{
			file.mkdirs();
		}

		OutputStream os = new FileOutputStream(BuildConfigurationManager
				.getRootDirectory() + "\\" + FILE_NAME);
		Writer writer = new OutputStreamWriter(os, BuildConfigurationManager.ENCODING);
		xstream.toXML(folders, writer);
	}
}
