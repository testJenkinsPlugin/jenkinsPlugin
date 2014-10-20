package com.amcbridge.jenkins.plugins.configurator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.tmatesoft.svn.core.SVNException;

import com.amcbridge.jenkins.plugins.controls.*;
import com.amcbridge.jenkins.plugins.messenger.*;

import hudson.Extension;
import hudson.model.RootAction;
import hudson.model.User;

@Extension
public final class BuildConfigurator implements RootAction {

	private MailSender mail;

	private static final String PLUGIN_NAME = "Build Configurator";
	private static final String ICON_PATH = "/plugin/configurator/icons/system_config_services.png";
	private static final String DEFAULT_PAGE_URL = "BuildConfigurator";

	public BuildConfigurator()
	{
		mail = new MailSender();
	}

	public String getDisplayName()
	{
		if (User.current() == null)
			return null;
		return PLUGIN_NAME;
	}

	public String getIconFileName()
	{
		if (User.current() == null)
			return null;
		return ICON_PATH;
	}

	public String getUrlName()
	{
		if (User.current() == null)
			return null;
		return DEFAULT_PAGE_URL;
	}

	public List<BuildConfiguration> getAllConfigurations() throws IOException, ServletException
	{
		return BuildConfigurationManager.loadAllConfigurations();
	}

	public void doCreateNewConfigurator(final StaplerRequest request,final StaplerResponse response) throws
	IOException, ServletException, ParserConfigurationException, JAXBException,
	AddressException, MessagingException
	{
		JSONObject formAttribute = request.getSubmittedForm();
		if (formAttribute.get("formResultHidden") != null && formAttribute.get("formResultHidden")
				.equals(FormResult.CANCEL.toString()))
		{
			deleteNotUploadFile(formAttribute.get("scriptsHidden").toString().split(";"));
			response.sendRedirect("../" + DEFAULT_PAGE_URL);
			return;
		}

		BuildConfiguration newConfig = new BuildConfiguration();
		newConfig.setCurrentDate();
		BuildConfiguration currentConfig = BuildConfigurationManager
				.load(formAttribute.get("projectName").toString());
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

		ConfigurationStatusMessage message = new ConfigurationStatusMessage(newConfig.getProjectName());
		message.setSubject(newConfig.getProjectName());
 
		FormResult type = FormResult.valueOf(formAttribute.get("formType").toString());

		switch (type)
		{
		case CREATE:
			newConfig.setState(ConfigurationState.NEW);
			message.setDescription(MessageDescription.CREATE.toString());
			break;
		case EDIT:
			newConfig.setState(ConfigurationState.UPDATED);
			message.setDescription(MessageDescription.CHANGE.toString());
			break;
		case APPROVED:
			newConfig.setState(ConfigurationState.APPROVED);
			newConfig.setCreator(currentConfig.getCreator());
			message.setDescription(MessageDescription.APPROVE.toString());
			if (!newConfig.getEmail().isEmpty())
			{
				message.setDestinationAddress(newConfig.getEmail().trim());
				mail.sendMail(message);
			}
			break;
		case REJECT:
			newConfig = currentConfig;
			newConfig.setState(ConfigurationState.REJECTED);
			message.setDescription(MessageDescription.REJECT.toString() + " " + formAttribute
					.get("rejectionReason").toString());
			if (!newConfig.getEmail().isEmpty())
			{
				message.setDestinationAddress(newConfig.getEmail().trim());
				mail.sendMail(message);
			}
			break;
		default:
			break;
		}

		BuildConfigurationManager.save(newConfig);
		message.setDestinationAddress(getAdminEmails());
		mail.sendMail(message);
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
		String path = BuildConfigurationManager.getUserContentFolder();
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
	public static void deleteNotUploadFile(String[] files)
	{
		String pathFolder = BuildConfigurationManager.getUserContentFolder();
		BuildConfigurationManager.deleteFiles(files, pathFolder);
	}

	@JavaScriptMethod
	public void setForDeletion(String name) throws AddressException,
	IOException, ParserConfigurationException, JAXBException, MessagingException
	{
		BuildConfigurationManager.markConfigurationForDeletion(name);
	}

	@JavaScriptMethod
	public void exportToXml() throws SVNException, IOException, InterruptedException
	{
		BuildConfigurationManager.exportToXml();
	}

	@JavaScriptMethod
	public Boolean isNameFree(String name)
	{
		return !BuildConfigurationManager.isNameUsing(name);
	}

	@JavaScriptMethod
	public void deleteConfigurationPermanently(String name)
			throws AddressException, IOException, MessagingException
	{
		BuildConfigurationManager.deleteConfigurationPermanently(name);
	}

	public static Boolean isCurrentUserCreator(String name) throws IOException
	{
		return BuildConfigurationManager.isCurrentUserCreatorOfConfiguration(name);
	}

	public static Boolean isCurrentUserAdministrator()
	{
		return BuildConfigurationManager.isCurrentUserAdministrator();
	}

	@JavaScriptMethod
	public BuildConfiguration getConfiguration(String name) throws IOException
	{
		return BuildConfigurationManager.getConfiguration(name);
	}

	public static String getAdminEmails()
	{
		return BuildConfigurationManager.getAdminEmail();
	}

	public Builder[] getBuilders()
	{
		return Builder.values();
	}

	public Platform[] getPlatform()
	{
		return Platform.values();
	}

	public SourceControlTool[] getSourceControlTools()
	{
		return SourceControlTool.values();
	}

	public BuildMachineConfiguration[] getBuildMachineConfigurations()
	{
		return BuildMachineConfiguration.values();
	}

	public Configuration[] getConfigurations()
	{
		return Configuration.values();
	}
}
