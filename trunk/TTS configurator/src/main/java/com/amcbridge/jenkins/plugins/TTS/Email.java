package com.amcbridge.jenkins.plugins.TTS;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("EmailRecord")
public class Email {

	@XStreamAlias("Email")
	private String  email;

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getEmail()
	{
		return email;
	}
}
