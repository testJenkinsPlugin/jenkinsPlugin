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

import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang.ArrayUtils;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.Stapler;
import org.tmatesoft.svn.core.SVNException;

import com.amcbridge.jenkins.plugins.configuration.BuildConfiguration;
import com.amcbridge.jenkins.plugins.export.*;
import com.amcbridge.jenkins.plugins.export.ExportSettings.Settings;
import com.amcbridge.jenkins.plugins.messenger.*;
import com.amcbridge.jenkins.plugins.vsc.CommitError;
import com.amcbridge.jenkins.plugins.vsc.SvnManager;
import com.amcbridge.jenkins.plugins.vsc.VersionControlSystem;
import com.amcbridge.jenkins.plugins.vsc.VersionControlSystemResult;

import hudson.XmlFile;
import hudson.model.User;
import hudson.security.AccessControlled;
import hudson.security.Permission;
import hudson.util.Iterators;

public class BuildConfigurationManager
{
	public static final String CONFIG_FILE_NAME = "config.xml";
	public static final String DATE_FORMAT = "MM/dd/yyyy";
	public static final String ENCODING = "UTF-8";
	private static final String BUILD_CONFIGURATOR_DIRECTORY_NAME = "\\plugins\\BuildConfiguration";
	private static final String CONTENT_FOLDER = "userContent";
	private static final String SCRIPT_FOLDER = "Scripts";
	private static final Integer MAX_FILE_SIZE = 1048576;//max file size which equal 1 mb in bytes
	private static final String[] SCRIPTS_EXTENSIONS = { "bat", "nant", "powershell", "shell",
		"ant", "maven" };

	public static final String STRING_EMPTY = "";

	private static MailSender mail = new MailSender();

	public static String getCurrentUserID()
	{
		if (User.current() != null)
			return User.current().getId();
		else
			return STRING_EMPTY;
	}

	public static File getConfigFileFor(String id)
	{
		return new File(new File(getRootDir(), id), CONFIG_FILE_NAME);
	}

	public static File getFileToExportConfigurations()
	{
		return new File(getRootDir()+ "\\" + CONFIG_FILE_NAME);
	}

	static File getRootDir()
	{
		return new File(Jenkins.getInstance().getRootDir(),
				BUILD_CONFIGURATOR_DIRECTORY_NAME);
	}

	public static String getRootDirectory()
	{
		return Jenkins.getInstance().getRootDir() + "\\"
				+ BUILD_CONFIGURATOR_DIRECTORY_NAME;
	}

	public static String getUserContentFolder()
	{
		return Jenkins.getInstance().getRootDir() + "\\" + CONTENT_FOLDER;
	}

	public static void save(BuildConfiguration config) throws IOException,
	ParserConfigurationException, JAXBException
	{
		if (config.getProjectName().isEmpty())
		{
			deleteFiles(config.getScripts(), getUserContentFolder());
			return;
		}

		File checkFile = new File(getRootDirectory() + "\\" + config.getProjectName());
		if (!checkFile.exists())
			checkFile.mkdirs();

		XmlFile fileWriter = getConfigFile(config.getProjectName());
		fileWriter.write(config);
		saveFile(config);
	}

	public static void saveFile(BuildConfiguration config)
	{
		if (config.getProjectName().isEmpty() || config.getScripts().length == 0)
			return;
		String pathFolder = getRootDirectory() + "\\" + config.getProjectName() +
				"\\" + SCRIPT_FOLDER;
		String filePath;
		File checkFolder = new File(pathFolder);
		File checkFile;
		if (!checkFolder.exists())
			checkFolder.mkdirs();

		String [] scripts = config.getScripts();

		for (int i = 0; i < scripts.length; i++)
		{
			if (scripts[i].isEmpty())
				continue;
			filePath = getUserContentFolder() + "\\" + scripts[i];
			checkFile = new File(filePath);
			if (!checkFile.exists())
			{
				checkFile = new File(pathFolder + "\\" + scripts[i]);
				if (!checkFile.exists())
					scripts[i] = STRING_EMPTY;
				continue;
			}
			if (checkFile.length() < MAX_FILE_SIZE && checkExtension(checkFile.getName()))
			{
				checkFile.renameTo(new File(pathFolder + "\\"
						+ checkFile.getName()));
			} 
			else
			{
				scripts[i] = STRING_EMPTY;
			}
		}

		config.setScripts(scripts);
		checkFile = new File(pathFolder);
		File[] listOfFiles = checkFile.listFiles();

		for (int i = 0; i < listOfFiles.length; i++)
		{
			if (ArrayUtils.indexOf(scripts, listOfFiles[i].getName()) == -1)
			{
				listOfFiles[i].delete();
			}
		}
	}

	private static Boolean checkExtension(String fileName)
	{
		String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
		for (int i = 0; i < SCRIPTS_EXTENSIONS.length; i++)
		{
			if (SCRIPTS_EXTENSIONS[i].equals(extension))
				return true;
		}
		return false;
	}

	protected final static XmlFile getConfigFile(String nameProject)
	{
		return new XmlFile(Jenkins.XSTREAM,	getConfigFileFor("\\" + nameProject));
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

	public static List<BuildConfiguration> loadAllConfigurations()
			throws IOException, ServletException, JAXBException
			{
		List<BuildConfiguration> configs = new ArrayList<BuildConfiguration>();
		File file = new File(getRootDirectory());

		if (!file.exists())
			return null;

		File[] directories = file.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
		for (int i = 0; i < directories.length; i++)
		{
			if (!isCurrentUserAdministrator() &&
					!isCurrentUserCreatorOfConfiguration(directories[i].getName()))
				continue;
			configs.add(load(directories[i].getName()));
		}
		return configs;
			}

	public static void deleteFiles(String[] files, String pathFolder)
	{
		File file;
		for (int i = 0; i < files.length; i++)
		{
			if (files[i].isEmpty())
				continue;
			file = new File(pathFolder + "\\" + files[i]);
			file.delete();
		}
	}

	public static void markConfigurationForDeletion(String name)
			throws IOException, ParserConfigurationException,
			JAXBException, AddressException, MessagingException
	{
		BuildConfiguration config = load(name);
		if (config.getState() == ConfigurationState.FOR_DELETION)
			return;
		config.setState(ConfigurationState.FOR_DELETION);
		config.setCurrentDate();
		save(config);
		ConfigurationStatusMessage message = new ConfigurationStatusMessage(config.getProjectName(),
				getAdminEmail(), MessageDescription.MARKED_FOR_DELETION.toString(),
				config.getProjectName());
		mail.sendMail(message);
	}
	
	public static void restoreConfiguration(String name) throws IOException, ParserConfigurationException,
	JAXBException, AddressException, MessagingException
	{
		BuildConfiguration config = load(name);
		config.setState(ConfigurationState.UPDATED);
		config.setCurrentDate();
		save(config);
		ConfigurationStatusMessage message = new ConfigurationStatusMessage(config.getProjectName(),
		getAdminEmail(), MessageDescription.RESTORE.toString(),
		config.getProjectName());
		mail.sendMail(message);

	}

	public static VersionControlSystemResult exportToXml()
			throws SVNException, IOException, InterruptedException
	{
		XmlExporter xmlExporter = new XmlExporter();
		String path = xmlExporter.exportToXml();
		VersionControlSystem svn = new SvnManager();

		Settings settings = new Settings();
		if (!settings.isSettingsSet())
		{
			VersionControlSystemResult result = new VersionControlSystemResult(false);
			result.setErrorMassage(CommitError.NONE_PROPERTY.toString());
			return result;
		}
		return svn.doCommit(path, settings.getUrl(), settings.getLogin(), settings.getPassword());
	}

	public static Boolean isNameUsing(String name)
	{
		File checkName = new File(getRootDirectory() + "\\" + name);
		if (checkName.exists())
			return true;
		else
			return false;
	}

	public static void deleteConfigurationPermanently(String name) throws IOException,
	AddressException, MessagingException, JAXBException
	{
		File checkFile = new File(getRootDirectory() + "\\" + name);
		BuildConfiguration config = load(name);
		if (checkFile.exists())
			FileUtils.deleteDirectory(checkFile);

		ConfigurationStatusMessage message = new ConfigurationStatusMessage(config.getProjectName());
		message.setSubject(config.getProjectName());
		message.setDescription(MessageDescription.DELETE_PERMANENTLY.toString());

		if (!config.getEmail().isEmpty())
		{
			message.setDestinationAddress(config.getEmail().trim());
			mail.sendMail(message);
		}

		message.setDestinationAddress(getAdminEmail());
		mail.sendMail(message);
	}

	public static Boolean isCurrentUserCreatorOfConfiguration(String name) throws IOException, JAXBException
	{
		return load(name).getCreator().equals(getCurrentUserID());
	}

	public static String[] getPath (String value)
	{
		if (value.equals(STRING_EMPTY))
			return new String[0];
		if (value.lastIndexOf(';') == value.length()-1)
		{
			value = value.substring(0, value.lastIndexOf(';'));
		}
		return value.split(";");
	}

	public static Boolean isCurrentUserAdministrator()
	{
		Object inst = Jenkins.getInstance();
		Permission permission = Jenkins.ADMINISTER;

		if (inst instanceof AccessControlled)
			return ((AccessControlled) inst).hasPermission(permission);
		else
		{
			List<Ancestor> ancs = Stapler.getCurrentRequest().getAncestors();
			for (Ancestor anc : Iterators.reverse(ancs))
			{
				Object o = anc.getObject();
				if (o instanceof AccessControlled)
				{
					return ((AccessControlled) o).hasPermission(permission);
				}
			}
			return Jenkins.getInstance().hasPermission(permission);
		}
	}

	public static BuildConfiguration getConfiguration(String name) throws IOException, JAXBException
	{
		BuildConfiguration currenConfig = load(name);
		if (!isCurrentUserAdministrator() && !isCurrentUserCreatorOfConfiguration(name))
			return null;
		return currenConfig;
	}

	public static String getAdminEmail()
	{
		return JenkinsLocationConfiguration.get().getAdminAddress();
	}
}