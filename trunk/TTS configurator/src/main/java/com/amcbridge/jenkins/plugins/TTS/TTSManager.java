package com.amcbridge.jenkins.plugins.TTS;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.parsers.ParserConfigurationException;

import jenkins.model.Jenkins;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;
import com.thoughtworks.xstream.XStream;

public class TTSManager {

	private List<TTSProject> projects;
	private String email;

	private static final String PROJECTS_URL = "http://ttstest.amcbridge.com/wsuserdatas.asmx/GetProjectsByUser";
	private static final String EMAIL_URL = "http://ttstest.amcbridge.com/wsuserdatas.asmx/GetEmailByUser";
	private static final String LOGIN_PARAMETER = "login";
	private static final String PASSWORD_PARAMETER = "password";
	private static final String ENCODING = "UTF-8";

	public TTSManager()
	{
		TTSConnection();
	}

	public List<TTSProject> getAllProjects()
	{
		return projects;
	}

	public List<TTSProject> getActiveProjects()
	{
		List<TTSProject> result = new ArrayList<TTSProject>();
		for (TTSProject project : projects)
		{
			if (project.isActual())
			{
				result.add(project);
			}
		}

		return result;
	}

	public List<TTSProject> getEndProjects()
	{
		List<TTSProject> result = new ArrayList<TTSProject>();
		for (TTSProject project : projects)
		{
			if (!project.isActual())
			{
				result.add(project);
			}
		}

		return result;
	}

	public String getEmail()
	{
		return email;
	}

	public int getProjectId(String name)
	{
		int result = 0;
		for (TTSProject project : projects)
		{
			if (project.getName().equals(name))
			{
				result = project.getId();
				break;
			}
		}

		return result;
	}

	private void TTSConnection()
	{
		try
		{
			loadProjects();
			loadEmail();
			checkProjectName();
		}
		catch (Exception e)
		{}
	}

	private void loadProjects()
			throws NoSuchAlgorithmException, ClientProtocolException, IOException
	{
		projects = new ArrayList<TTSProject>();
		String responseValue = doRequest(PROJECTS_URL);

		XStream xstream = new XStream();
		xstream.processAnnotations(TTSProject.class);
		xstream.setClassLoader(TTSProject.class.getClassLoader());
		xstream.alias("ArrayOfRoleRecord", projects.getClass());
		projects = (List<TTSProject>) xstream.fromXML(responseValue);
	}

	private void loadEmail()
			throws NoSuchAlgorithmException, ClientProtocolException, IOException
	{
		email = StringUtils.EMPTY;
		String responseValue = doRequest(EMAIL_URL);

		XStream xstream = new XStream();
		xstream.processAnnotations(Email.class);
		xstream.setClassLoader(Email.class.getClassLoader());
		xstream.alias("EmailRecord", Email.class);
		email = ((Email) xstream.fromXML(responseValue)).getEmail();
	}

	private String doRequest(String url)
			throws NoSuchAlgorithmException, ClientProtocolException, IOException
	{
		HttpClient client = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		String password = Jenkins.getAuthentication().getCredentials().toString();
		String login = Jenkins.getAuthentication().getPrincipal().toString();
		String pas = hashMD5(password);

		List<NameValuePair> parameters = new ArrayList<NameValuePair>(2);

		parameters.add(new BasicNameValuePair(LOGIN_PARAMETER, login));
		parameters.add(new BasicNameValuePair(PASSWORD_PARAMETER, pas));

		httppost.setEntity(new UrlEncodedFormEntity(parameters, ENCODING));

		HttpResponse response = client.execute(httppost);

		HttpEntity entity = response.getEntity();
		return EntityUtils.toString(entity, ENCODING);
	}

	private String hashMD5(String value) throws NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance("MD5");
		return (new HexBinaryAdapter()).marshal(md.digest(value.getBytes()));
	}
	
	private void checkProjectName() throws IOException, ParserConfigurationException, JAXBException
	{
		for (TTSProject project : projects)
		{
			BuildConfigurationManager.checkProjectName(project);
		}
	}
}
