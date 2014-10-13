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
public final class BuildConfigurator implements RootAction {
	private MailSender mail;

	public BuildConfigurator()
	{
		mail = new MailSender();
	}

	public String getDisplayName()
	{
		if (User.current() == null)
			return null;
		return "Build Configurator";
	}

	public String getIconFileName()
	{
		if (User.current() == null)
			return null;
		return "/plugin/configurator/icons/system_config_services.png";
	}

	public String getUrlName()
	{
		if (User.current() == null)
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

		for (int i = 0; i < directories.length; i++)
		{
			currentConfigFile = BuildConfiguration.getConfigFileFor("\\" + directories[i].getName());
			if (!currentConfigFile.exists())
				continue;
			currenConfig = null;
			currenConfig = BuildConfiguration.load(directories[i].getName());
			currentUser = BuildConfiguration.getCurrentUserMail();
			if (!isCurrentUserAdministrator() && !isCurrentUserCreator(currenConfig))
				continue;
			config = new String[3];
			config[0] = currenConfig.getProjectName();
			config[1] = currenConfig.getState();
			config[2] = currenConfig.getDate();
			configurations.add(config);
		}
		return configurations.toArray();
	}

	public void doCreateNewConfigurator(final StaplerRequest request,final StaplerResponse response) throws
	IOException, ServletException, ParserConfigurationException, JAXBException,
	AddressException, MessagingException
	{
		JSONObject formAttribute = request.getSubmittedForm();
		if (formAttribute.get("formResultHidden") != null && formAttribute.get("formResultHidden")
				.toString().equals("cancel"))
		{
			deleteNotUploadFile(formAttribute.get("scriptsHidden").toString().split(";"));
			response.sendRedirect("../BuildConfigurator");
			return;
		}

		BuildConfiguration newConfig = new BuildConfiguration();
		BuildConfiguration currentConfig = BuildConfiguration.load(formAttribute.get("projectName").toString());
		request.bindJSON(newConfig, request.getSubmittedForm());
		newConfig.setFiles(formAttribute.get("fileHidden").toString().split(";"));
		newConfig.setArtefacts(formAttribute.get("artefactsHidden").toString().split(";"));
		newConfig.setVersionFile(formAttribute.get("versionFileHidden").toString().split(";"));
		newConfig.setScripts(formAttribute.get("scriptsHidden").toString().split(";"));

		for (Builder build : Builder.values()) 
		{
			if (formAttribute.get(build.name()) != null	&& (Boolean) formAttribute.get(build.name()))
				newConfig.addBuilders(build);
		}

		for (Platform platform : Platform.values())
		{
			if (formAttribute.get(platform.name()) != null && (Boolean) formAttribute.get(platform.name()))
				newConfig.addPlatform(platform);
		}

		String message = BuildConfiguration.STRING_EMPTY, messageTitle = BuildConfiguration.STRING_EMPTY;
		String type = formAttribute.get("formType").toString();

		if (type.equals("create"))
		{
			newConfig.setState(ConfigurationState.NEW);
			message = "New configuration '" + newConfig.getProjectName()
					+ "' was successfully created!";
			messageTitle = "New configuration";
		}
		if (type.equals("edit"))
		{
			newConfig.setState(ConfigurationState.UPDATED);
			message = "Configuration '" + newConfig.getProjectName()
					+ "' was changed.";
			messageTitle = "Configuration changes";
		}
		if (type.equals("approved"))
		{
			newConfig.setState(ConfigurationState.APPROVED);
			newConfig.setCreator(currentConfig.getCreator());
			message = "Configuration '" + newConfig.getProjectName()
					+ "' was successfully approved.";
			messageTitle = "Configuration approved";
			if (!newConfig.getEmail().isEmpty())
				mail.sendMail(getAdminEmails(), message, messageTitle);
		}
		if (type.equals("reject"))
		{
			newConfig = currentConfig;
			newConfig.setState(ConfigurationState.REJECTED);
			message = "Configuration '" + newConfig.getProjectName()
					+ "' was rejected by administrator. The reasons of rejection is: "
					+ formAttribute.get("rejectionReason").toString();
			messageTitle = "Configuration rejected";
			if (!newConfig.getEmail().isEmpty())
				mail.sendMail(getAdminEmails(), message, messageTitle);
		}

		newConfig.save();
		mail.sendMail(getAdminEmails(), message, messageTitle);
		response.sendRedirect("./");
	}

	public String doUploadFile(final HttpServletRequest request, final HttpServletResponse response)
			throws FileUploadException, IOException
	{
		DiskFileItemFactory factory = new DiskFileItemFactory();
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
		fileName = fileName.substring(0, fileName.lastIndexOf('.')) + "("
				+ dateFormat.format(date) + ")"
				+ fileName.substring(fileName.lastIndexOf('.'));
		File saveFile = new File(path, fileName);
		saveFile.createNewFile();
		OutputStream os = new FileOutputStream(saveFile);
		try {
			os.write(data);
		} finally {
			os.close();
		}
		return fileName;
	}

	@JavaScriptMethod
	public static void deleteNotUploadFile(String[] files) {
		String pathFolder = BuildConfiguration.getUserContentFolder();
		File file;
		for (int i = 0; i < files.length; i++)
		{
			if (files[i].isEmpty())
				continue;
			file = new File(pathFolder + "\\" + files[i]);
			file.delete();
		}
	}

	@JavaScriptMethod
	public void setForDeletion(String name) throws IOException,	ParserConfigurationException,
	JAXBException, AddressException, MessagingException
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
	public void deleteConfigurationPermanently(String name) throws IOException,
	AddressException, MessagingException
	{
		File checkFile = new File(BuildConfiguration.getRootDirectory() + "\\" + name);
		String email = BuildConfiguration.load(name).getEmail();
		if (checkFile.exists())
			FileUtils.deleteDirectory(checkFile);

		if (!email.isEmpty())
		{
			String message = "Your configuration '" + name + "' was successfully deleted.";
			mail.sendMail(email, message, "Configuration was deleted.");
		}

		String message = "Configuration '" + name + "' was successfully deleted.";
		mail.sendMail(getAdminEmails(), message, "Configuration was deleted.");
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

	@JavaScriptMethod
	public BuildConfiguration getConfiguration(String name) throws IOException,	ServletException
	{
		BuildConfiguration currenConfig = BuildConfiguration.load(name);
		String currentUser = BuildConfiguration.getCurrentUserMail();
		if (!isCurrentUserAdministrator() && !isCurrentUserCreator(currenConfig))
			return null;
		return BuildConfiguration.load(name);
	}

	public static String getAdminEmails()
	{
		return JenkinsLocationConfiguration.get().getAdminAddress();
	}

	public Builder[] getBuilder()
	{
		return Builder.values();
	}

	public Platform[] getPlatform()
	{
		return Platform.values();
	}

	public SourceControlTool[] getSourceControlTool()
	{
		return SourceControlTool.values();
	}

	public BuildMachineConfiguration[] getBuildMachineConfiguration()
	{
		return BuildMachineConfiguration.values();
	}

	public Configuration[] getConfigurations()
	{
		return Configuration.values();
	}
}
