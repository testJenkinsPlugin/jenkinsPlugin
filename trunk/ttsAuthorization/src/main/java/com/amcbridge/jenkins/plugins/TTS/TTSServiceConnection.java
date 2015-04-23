package com.amcbridge.jenkins.plugins.TTS;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

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

import com.thoughtworks.xstream.XStream;

public class TTSServiceConnection {

	private static final String AUTHENTICATE_URL = "https://tts.amcbridge.com/wsuserdatas.asmx/Authenticate";
	private static final String LOGIN_PARAMETER = "login";
	private static final String PASSWORD_PARAMETER = "password";
	private static final String ENCODING = "UTF-8";

	public static Boolean checkUser(String login, String password)
			throws NoSuchAlgorithmException, ClientProtocolException, IOException
	{	
		HttpClient client = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(AUTHENTICATE_URL);
		String pas = hashMD5(password);

		List<NameValuePair> parameters = new ArrayList<NameValuePair>(2);

		parameters.add(new BasicNameValuePair(LOGIN_PARAMETER, login));
		parameters.add(new BasicNameValuePair(PASSWORD_PARAMETER, pas));

		httppost.setEntity(new UrlEncodedFormEntity(parameters, ENCODING));

		HttpResponse response = client.execute(httppost);

		HttpEntity entity = response.getEntity();
		String responseValue = EntityUtils.toString(entity, ENCODING);

		XStream xstream = new XStream();
		return (Boolean) xstream.fromXML(responseValue);
	}

	private static String hashMD5(String value) throws NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance("MD5");
		return (new HexBinaryAdapter()).marshal(md.digest(value.getBytes()));
	}
}
