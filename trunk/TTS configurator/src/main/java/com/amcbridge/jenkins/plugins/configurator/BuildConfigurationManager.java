package com.amcbridge.jenkins.plugins.configurator;

import hudson.XmlFile;
import hudson.model.Node;
import hudson.model.User;
import hudson.scm.SCMDescriptor;
import hudson.scm.SCM;
import hudson.security.AccessControlled;
import hudson.security.Permission;
import hudson.tasks.Mailer;
import hudson.tasks.Mailer.UserProperty;
import hudson.util.Iterators;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.ServletException;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.Stapler;
import org.tmatesoft.svn.core.SVNException;
import org.xml.sax.SAXException;

import com.amcbridge.jenkins.plugins.TTS.TTSManager;
import com.amcbridge.jenkins.plugins.TTS.TTSProject;
import com.amcbridge.jenkins.plugins.configuration.BuildConfiguration;
import com.amcbridge.jenkins.plugins.export.XmlExporter;
import com.amcbridge.jenkins.plugins.export.ExportSettings.Settings;
import com.amcbridge.jenkins.plugins.job.JobManagerGenerator;
import com.amcbridge.jenkins.plugins.messenger.ConfigurationStatusMessage;
import com.amcbridge.jenkins.plugins.messenger.MailSender;
import com.amcbridge.jenkins.plugins.messenger.MessageDescription;
import com.amcbridge.jenkins.plugins.vsc.CommitError;
import com.amcbridge.jenkins.plugins.vsc.SvnManager;
import com.amcbridge.jenkins.plugins.vsc.VersionControlSystem;
import com.amcbridge.jenkins.plugins.vsc.VersionControlSystemResult;

public class BuildConfigurationManager
{
	public static final String CONFIG_FILE_NAME = "config.xml";
	public static final String DATE_FORMAT = "MM/dd/yyyy";
	public static final String ENCODING = "UTF-8";
	private static final String CONFIG_JOB_FILE_NAME = "JobConfig.xml";
	private static final String BUILD_CONFIGURATOR_DIRECTORY_NAME = "\\plugins\\BuildConfiguration";
	private static final String CONTENT_FOLDER = "userContent";
	private static final String SCRIPT_FOLDER = "Scripts";
	private static final Integer MAX_FILE_SIZE = 1048576;//max file size which equal 1 mb in bytes
	private static final String[] SCRIPTS_EXTENSIONS = { "bat", "nant", "powershell", "shell",
		"ant", "maven" };
	private static final Character[] ILLEGAL_CHARS = {'\\', '/', ':', '*', '?', '"', '<', '>', '|'};

	public static final String STRING_EMPTY = "";

	private static MailSender mail = new MailSender();
	private static FolderManager FOLDER_MANAGER = new FolderManager();

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

	public static File getFileToCreateJob()
	{
		return new File(getRootDir()+ "\\" + CONFIG_JOB_FILE_NAME);
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

		String folderName = getFolderName(config.getProjectName());
		if (config.getState().equals(ConfigurationState.NEW))
		{
			FOLDER_MANAGER.add(config.getId(), getTTSProject(config.getId()));
		}

		File checkFile = new File(getRootDirectory() + "\\" + folderName);
		if (!checkFile.exists())
			checkFile.mkdirs();

		XmlFile fileWriter = getConfigFile(folderName);
		fileWriter.write(config);
		saveFile(config);
	}

	public static void saveFile(BuildConfiguration config)
	{
		String folderName = getFolderName(config.getProjectName());
		if (folderName.isEmpty() || config.getScripts().length == 0)
			return;
		String pathFolder = getRootDirectory() + "\\" + folderName +
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
		String folderName = getFolderName(nameProject);
		return new XmlFile(Jenkins.XSTREAM,	getConfigFileFor("\\" + folderName));
	}

	public static BuildConfiguration load(String nameProject) throws IOException
	{
		BuildConfiguration result = new BuildConfiguration();
		String folderName = getFolderName(nameProject);
		XmlFile config = getConfigFile(folderName);

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
		BuildConfiguration config;

		if (isCurrentUserAdministrator())
		{
			for (String folder : FOLDER_MANAGER.getFoldersName())
			{
				config = load(folder);
				if (config.getProjectName() == null)
				{
					continue;
				}
				configs.add(config);
			}
		}
		else
		{
			TTSManager ttsManager =  (TTSManager) Stapler.getCurrentRequest().getSession().getAttribute(BuildConfigurator.TTS_MANAGER);
			for (TTSProject project : ttsManager.getAllProjects())
			{
				config = load(project.getName());
				if (config.getProjectName() == null)
				{
					continue;
				}
				configs.add(config);
			}
		}
		return configs;
			}

	public static List<BuildConfiguration> getActiveConfigurations()
			throws IOException, ServletException, JAXBException
			{
		List<BuildConfiguration> configs = new ArrayList<BuildConfiguration>();
		BuildConfiguration config;

		if (isCurrentUserAdministrator())
		{
			for (String folder : FOLDER_MANAGER.getFoldersName())
			{
				config = load(folder);
				if (config.getProjectName() == null || !FOLDER_MANAGER.isActual(config.getId()))
				{
					continue;
				}
				configs.add(config);
			}
		}
		else
		{
			TTSManager ttsManager =  (TTSManager) Stapler.getCurrentRequest().getSession().getAttribute(BuildConfigurator.TTS_MANAGER);
			for (TTSProject project : ttsManager.getActiveProjects())
			{
				config = load(project.getName());
				if (config.getProjectName() == null)
				{
					continue;
				}
				configs.add(config);
			}
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
				getAdminEmail(), StringUtils.EMPTY, MessageDescription.MARKED_FOR_DELETION.toString(),
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
				getAdminEmail(), getUserMailAddress(config), MessageDescription.RESTORE.toString(),
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
		return svn.doCommit(path, settings.getUrl(), settings.getLogin(),
				settings.getPassword(), settings.getCommitMessage());
	}

	public static Boolean isNameUsing(String name)
	{
		String folderName = getFolderName(name);
		File checkName = new File(getRootDirectory() + "\\" + folderName);
		if (checkName.exists())
			return true;
		else
			return false;
	}

	public static void deleteConfigurationPermanently(String name) throws IOException,
	AddressException, MessagingException, JAXBException
	{
		String folderName = getFolderName(name);
		File checkFile = new File(getRootDirectory() + "\\" + folderName);
		BuildConfiguration config = load(name);
		if (checkFile.exists())
			FileUtils.deleteDirectory(checkFile);

		FOLDER_MANAGER.remove(config.getId());

		ConfigurationStatusMessage message = new ConfigurationStatusMessage(config.getProjectName());
		message.setSubject(config.getProjectName());
		message.setDescription(MessageDescription.DELETE_PERMANENTLY.toString());

		message.setCC(getUserMailAddress(config));
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

	public static List<String> getSCM()
	{
		List<String> result = new ArrayList<String>();
		for (SCMDescriptor<?> scm : SCM.all())
		{
			if (isSupportedSCM(scm))
			{
				result.add(scm.getDisplayName());
			}
		}
		return result;
	}

	public static List<TTSProject> getProjectName() throws IOException
	{
		List<TTSProject> result = new ArrayList<TTSProject>();
		TTSManager ttsManager = (TTSManager) Stapler.getCurrentRequest().getSession().getAttribute(BuildConfigurator.TTS_MANAGER);
		for (TTSProject project : ttsManager.getActiveProjects())
		{
			BuildConfiguration test = load(project.getName());
			ConfigurationState state = test.getState();
			if (state == null)
			{
				result.add(project);
			}
		}
		return result;
	}

	private static Boolean isSupportedSCM(SCMDescriptor<?> scm)
	{
		for (com.amcbridge.jenkins.plugins.controls.SCM suportSCM :
			com.amcbridge.jenkins.plugins.controls.SCM.values())
		{
			if (suportSCM.toString().equals(scm.getDisplayName()))
			{
				return true;
			}
		}
		return false;
	}

	public static List<String> getNodesName()
	{
		List<String> result = new ArrayList<String>();
		for (Node node : Jenkins.getInstance().getNodes())
		{
			result.add(node.getNodeName());
		}

		return result;
	}

	public static String getUserMailAddress(BuildConfiguration config)
	{
		if (config.getConfigEmail() != null)
		{
			String[] address = config.getConfigEmail().split(" ");
			return StringUtils.join(address, ",");
		}
		return StringUtils.EMPTY;
	}

	public static void createJob(String name)
			throws IOException, ParserConfigurationException,
			SAXException, TransformerException, JAXBException
	{
		BuildConfiguration config = load(name);
		JobManagerGenerator.createJob(config);
		config.setJobUpdate(true);
		save(config);
	}


	public static String getFolderName(String projectName)
	{
		for (Character character : ILLEGAL_CHARS)
		{
			if (projectName.indexOf(character) != -1)
			{
				projectName = projectName.replaceAll(character.toString(), " ");
			}
		}
		return projectName;
	}

	public static void checkProjectName(TTSProject project)
			throws IOException, ParserConfigurationException, JAXBException
	{
		if (!FOLDER_MANAGER.getFolderName(project.getId()).equals(StringUtils.EMPTY) && 
				(!FOLDER_MANAGER.getFolderName(project.getId())
						.equals(getFolderName(project.getName())) || FOLDER_MANAGER.isActual(project.getId()) != project.isActual())
				)
		{
			BuildConfiguration config = load(FOLDER_MANAGER.getFolderName(project.getId()));
			config.setProjectName(project.getName());

			File oldFolder = new File(getRootDirectory() + "\\"	+
					FOLDER_MANAGER.getFolderName(project.getId()));
			File newFolder = new File(getRootDirectory() + "\\"	+ getFolderName(project.getName()));
			oldFolder.renameTo(newFolder);
			save(config);
			FOLDER_MANAGER.remove(project.getId());
			FOLDER_MANAGER.add(project.getId(), getTTSProject(project.getId()));
		}
	}

	public static TTSProject getTTSProject(Integer id)
	{
		TTSManager tts = (TTSManager)Stapler.getCurrentRequest()
				.getSession().getAttribute(BuildConfigurator.TTS_MANAGER);
		return tts.getProject(id);
	}
}