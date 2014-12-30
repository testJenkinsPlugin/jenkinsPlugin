package com.amcbridge.jenkins.plugins.TTS;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("RoleRecord")
public class TTSProject {

	@XStreamAlias("ID")
	private int id;
	@XStreamAlias("Name")
	private String name;
	@XStreamAlias("Role")
	private String role;
	@XStreamAlias("Position")
	private String position;
	@XStreamAlias("Actual")
	private Boolean actual;

	public int getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getRole()
	{
		return role;
	}

	public Boolean isActual()
	{
		return actual;
	}
}