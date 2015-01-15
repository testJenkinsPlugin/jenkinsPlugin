package com.amcbridge.jenkins.plugins.TTS;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("RoleRecord")
public class TTSProject implements Cloneable {

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

	public void setName(String value)
	{
		name = value;
	}

	public String getRole()
	{
		return role;
	}

	public Boolean isActual()
	{
		return actual;
	}

	public TTSProject clone() throws CloneNotSupportedException
	{
		return (TTSProject)super.clone();
	}
}